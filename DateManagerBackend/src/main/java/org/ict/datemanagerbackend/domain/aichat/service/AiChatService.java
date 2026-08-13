package org.ict.datemanagerbackend.domain.aichat.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ict.datemanagerbackend.domain.aichat.entity.AiChatMessage;
import org.ict.datemanagerbackend.domain.aichat.entity.AiChatMessageIntent;
import org.ict.datemanagerbackend.domain.aichat.entity.AiChatMessageScore;
import org.ict.datemanagerbackend.domain.aichat.entity.AiChatSession;
import org.ict.datemanagerbackend.domain.aichat.repository.AiChatMessageIntentRepository;
import org.ict.datemanagerbackend.domain.aichat.repository.AiChatMessageRepository;
import org.ict.datemanagerbackend.domain.aichat.repository.AiChatMessageScoreRepository;
import org.ict.datemanagerbackend.domain.aichat.repository.AiChatSessionRepository;
import org.ict.datemanagerbackend.domain.place.entity.Place;
import org.ict.datemanagerbackend.domain.place.repository.PlaceRepository;
import org.ict.datemanagerbackend.domain.user.entity.User;
import org.ict.datemanagerbackend.weather.dto.WeatherResponse;
import org.ict.datemanagerbackend.weather.service.WeatherService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * OpenAI Chat Completions REST API(https://api.openai.com/v1/chat/completions) 연동 서비스.
 *
 * <p>배운 자료(chat_memory04.ipynb)와 동일한 방식으로 "대화 기억"을 구현한다 — OpenAI는 요청마다
 * 완전히 새로운 대화로 취급하므로(서버가 이전 대화를 기억하지 않음), 매번 이 세션의 지난 메시지
 * 전체를 messages 배열로 다시 만들어 보낸다. AI 응답도 매번 DB에 저장해두기 때문에, 다음 요청 때
 * 그 응답까지 포함해서 다시 보내는 식으로 대화가 이어진다.
 *
 * <p>공식 openai 자바 SDK 대신 NaverPlaceSyncService 등 기존 코드와 같은 RestTemplate 직접 호출
 * 방식을 썼다 — 배운 노트북의 REST 버전(requests로 직접 POST)과 같은 구조라서 이해하기 쉽고,
 * 이 프로젝트에 이미 있는 패턴을 그대로 재사용할 수 있다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AiChatService {

  private static final String CHAT_COMPLETIONS_URL = "https://api.openai.com/v1/chat/completions";
  private static final String MODEL = "gpt-4o-mini";

  private static final DateTimeFormatter NOW_FORMAT =
      DateTimeFormatter.ofPattern("yyyy년 M월 d일 EEEE a h시 mm분", Locale.KOREAN);

  // 상담사 캐릭터를 잡아주는 고정 system 프롬프트. 컨텍스트 메시지(현재 시각/날씨)와는 역할이 달라서
  // 따로 둔다 - 이건 세션이 바뀌어도 항상 똑같은 "역할 설정"이고, 컨텍스트는 요청마다 바뀌는 "상황 정보".
  private static final String PERSONA_SYSTEM_PROMPT = """
      너는 데이트 코스를 추천해주는 친근한 데이트 코치 '데이트매니저'야. 커플이나 소개팅을 준비하는
      사용자에게 상황에 맞는 데이트 장소·코스를 추천하고, 데이트 관련 고민(선물, 대화 주제, 갈등 등)에
      공감하며 실용적인 조언을 해줘.
      - 딱딱한 존댓말 말고 친근하고 다정한 존댓말을 써.
      - 답변은 3~4문장 이내로 짧고 구체적으로 해.
      - 데이트·연애와 관련 없는 질문(코딩, 시사, 숙제 등)에는 정중히 거절하고 데이트 상담으로 화제를 돌려줘.
      - 확실하지 않은 정보(구체적인 가게 이름, 실시간 예약 가능 여부 등)는 지어내지 말고 모른다고 말해.
      """;

  // AiChatMessageIntent.intentTag로 저장을 허용할 값 목록 - LLM이 프롬프트를 무시하고 엉뚱한
  // 문자열을 내놓을 수 있어서, 화이트리스트에 없는 값은 저장하지 않고 버린다.
  private static final List<String> INTENT_TAGS = List.of(
      "PLACE_RECOMMENDATION", "GIFT_ADVICE", "CONVERSATION_TOPIC", "RELATIONSHIP_CONCERN", "GENERAL_CHAT"
  );

  // AiChatMessageScore.scoreType 목록 - PlaceStyle의 5개 성향 축과 이름을 맞춰서, 나중에 온보딩
  // 결과와 같은 잣대로 비교/합산할 수 있게 한다.
  private static final List<String> SCORE_TYPES = List.of("ENERGY", "IMMERSION", "VIBE", "AESTHETIC", "DEPTH");

  // 대화 한 번당 참고 자료로 붙여줄 근처 장소 개수. 너무 많이 붙이면 프롬프트만 길어지고 모델이
  // 오히려 헷갈려해서, 실제 추천 카드 몇 개 보여주는 정도의 개수로 제한한다.
  private static final int NEARBY_PLACE_LIMIT = 15;

  private static final String ANALYSIS_PROMPT = """
      다음은 데이트 상담 챗봇에 사용자가 보낸 메시지야. 이 메시지를 분석해서 아래 형식의 JSON으로만
      답해(다른 설명 텍스트 없이 JSON만):
      {"intents": ["의도 태그 배열, 해당 없으면 빈 배열"], "scores": {"ENERGY": 0~100 또는 null, "IMMERSION": 0~100 또는 null, "VIBE": 0~100 또는 null, "AESTHETIC": 0~100 또는 null, "DEPTH": 0~100 또는 null}}

      의도 태그는 다음 중에서만 골라: PLACE_RECOMMENDATION(장소 추천 요청), GIFT_ADVICE(선물 고민),
      CONVERSATION_TOPIC(대화 주제 고민), RELATIONSHIP_CONCERN(연애 고민/갈등), GENERAL_CHAT(단순 잡담).
      scores는 메시지에 취향이 명확히 드러날 때만 채우고, 드러나지 않는 항목은 null로 둬.
      ENERGY는 활동적인 것을 얼마나 선호하는지, IMMERSION은 직접 참여하는 걸 얼마나 선호하는지,
      VIBE는 트렌디한 곳을 얼마나 선호하는지, AESTHETIC은 감각적인 공간을 얼마나 선호하는지,
      DEPTH는 깊이 있는 콘텐츠를 얼마나 선호하는지를 뜻해.

      사용자 메시지: "%s"
      """;

  private final AiChatSessionRepository aiChatSessionRepository;
  private final AiChatMessageRepository aiChatMessageRepository;
  private final AiChatMessageIntentRepository aiChatMessageIntentRepository;
  private final AiChatMessageScoreRepository aiChatMessageScoreRepository;
  private final WeatherService weatherService;
  private final PlaceRepository placeRepository;
  private final ObjectMapper objectMapper;
  private final RestTemplate restTemplate = new RestTemplate();

  @Value("${openai.api-key}")
  private String apiKey;

  /** 새 채팅 세션을 시작한다. */
  public AiChatSession createSession(User user, String title) {
    AiChatSession session = AiChatSession.builder()
        .user(user)
        .title(title)
        .build();
    return aiChatSessionRepository.save(session);
  }

  /**
   * 세션에 사용자 메시지를 저장하고, 대화 기록 전체를 OpenAI에 보내 응답을 받은 뒤 그 응답도
   * 저장한다. 반환값은 방금 생성된 AI 응답 메시지.
   *
   * <p>lat/lon은 선택값이다 - 프론트가 위치 권한을 안 줬거나 못 구했으면 null로 넘어오고, 이 경우
   * 날씨 없이 현재 시각만 알려준다(WeatherService 호출 자체를 생략).
   */
  public AiChatMessage sendMessage(User user, Long sessionId, String userText, Double lat, Double lon) {
    AiChatSession session = getOwnedSession(user, sessionId);

    AiChatMessage userMessage = AiChatMessage.builder()
        .session(session)
        .senderType("USER")
        .sender(user)
        .messageText(userText)
        .build();
    aiChatMessageRepository.save(userMessage);
    analyzeMessage(userMessage);

    // 상황 정보(현재 시각/날씨, 근처 실제 장소 목록)는 대화 내용이 아니라 매 요청마다 새로 만들어
    // system 메시지로만 끼워 보낼 것들이라 DB에 저장하지 않고 리스트로 모아둔다.
    List<String> situationMessages = new ArrayList<>();
    situationMessages.add(buildContextMessage(lat, lon));
    String nearbyPlacesMessage = buildNearbyPlacesMessage(lat, lon);
    if (nearbyPlacesMessage != null) {
      situationMessages.add(nearbyPlacesMessage);
    }

    // 방금 저장한 유저 메시지까지 포함해서 지금까지의 대화 전체를 시간순으로 가져온다.
    List<AiChatMessage> history = aiChatMessageRepository.findBySession_IdOrderByCreatedAtAsc(sessionId);
    String aiText = requestChatCompletion(history, situationMessages);

    AiChatMessage aiMessage = AiChatMessage.builder()
        .session(session)
        .senderType("AI")
        .sender(null) // AI 발신이면 sender는 null (엔티티 주석 참고)
        .messageText(aiText)
        .build();
    return aiChatMessageRepository.save(aiMessage);
  }

  /** 세션의 전체 메시지 이력을 오래된 순으로 반환한다. */
  public List<AiChatMessage> getMessages(User user, Long sessionId) {
    getOwnedSession(user, sessionId); // 소유권 검증(다른 유저의 세션이면 예외 발생)
    return aiChatMessageRepository.findBySession_IdOrderByCreatedAtAsc(sessionId);
  }

  private AiChatSession getOwnedSession(User user, Long sessionId) {
    Optional<AiChatSession> session = aiChatSessionRepository.findByIdAndUser_Id(sessionId, user.getId());
    return session.orElseThrow(() -> new IllegalArgumentException("세션을 찾을 수 없습니다: " + sessionId));
  }

  /**
   * "지금은 언제고 날씨는 어떤지"를 알려주는 system 메시지 내용을 만든다. GPT는 실시간 정보에
   * 접근할 수 없어서(학습 시점 기준 지식만 가짐), 이렇게 매 요청마다 현재 상태를 직접 알려줘야
   * "오늘 날씨"·"지금 몇 시" 같은 질문에 실제 값으로 답할 수 있다.
   */
  private String buildContextMessage(Double lat, Double lon) {
    StringBuilder sb = new StringBuilder("[시스템 제공 정보] 지금은 ")
        .append(LocalDateTime.now().format(NOW_FORMAT)).append("이야.");

    if (lat != null && lon != null) {
      try {
        WeatherResponse weather = weatherService.getCurrentWeather(lat, lon);
        sb.append(" 사용자 위치의 현재 날씨는 기온 ").append(weather.temp())
            .append(", ").append(weather.desc()).append("이야.");
      } catch (Exception e) {
        log.warn("챗봇 컨텍스트용 날씨 조회 실패 (lat={}, lon={})", lat, lon, e);
      }
    }

    // GPT는 "실시간 정보는 알 수 없다"고 습관적으로 발뺌하도록 강하게 학습돼 있어서, 컨텍스트로
    // 값을 줘도 시간을 물어보면 회피 답변부터 하는 경우가 많다(날씨는 상대적으로 이 습관이 덜함).
    // 그래서 "이 값은 진짜 서버 시계에서 온 정확한 값이니 그대로 답하라"고 못 박아둔다.
    sb.append(" 이 시각/날씨는 네 추측이 아니라 실제 서버 시계·기상청 API에서 방금 가져온 정확한 값이야. ")
        .append("사용자가 지금 몇 시인지, 오늘 날씨가 어떤지 물어보면 위 값을 그대로 자신 있게 답해줘. ")
        .append("'실시간 정보를 알 수 없다'거나 '기기에서 확인하라'는 식으로 답변하지 마. ")
        .append("먼저 나서서 시간·날씨 얘기를 꺼낼 필요는 없고, 물어볼 때만 답하면 돼.");
    return sb.toString();
  }

  /**
   * 위경도 근처의 실제 장소 목록을 system 메시지로 만든다. 이게 없으면 모델이 장소를 추천할 때
   * 실제로 존재하지 않는 가게 이름을 지어낼 수 있어서(할루시네이션), 실제 DB에 있는 장소만 골라
   * 쓰도록 후보 목록을 직접 쥐여준다. 위치 정보가 없으면 추천할 후보가 없다는 뜻이라 null을 반환하고,
   * 이 경우 PERSONA_SYSTEM_PROMPT의 "확실하지 않은 정보는 지어내지 말라"는 지침에 맡긴다.
   */
  private String buildNearbyPlacesMessage(Double lat, Double lon) {
    if (lat == null || lon == null) return null;

    List<Place> nearby = placeRepository.findNearestPlaces(lat, lon, NEARBY_PLACE_LIMIT);
    if (nearby.isEmpty()) return null;

    String list = nearby.stream()
        .map(p -> "- " + p.getName() + " (" + p.getCategory()
            + (p.getAddress() != null ? ", " + p.getAddress() : "") + ")")
        .collect(Collectors.joining("\n"));

    return "사용자 근처의 실제 장소 목록이야:\n" + list
        + "\n장소를 구체적으로 추천할 땐 반드시 이 목록 안에 있는 이름만 골라서 말해줘. "
        + "목록에 어울리는 곳이 없으면 없다고 솔직히 말하고, 목록에 없는 가게 이름을 지어내지 마.";
  }

  /**
   * 사용자 메시지 하나를 별도의 OpenAI 호출로 분석해서 의도 태그(AiChatMessageIntent)와
   * 성향점수(AiChatMessageScore)를 뽑아 저장한다. 실제 채팅 응답과는 무관한 부가 기능이라,
   * 분석이 실패해도(JSON 파싱 실패, API 호출 실패 등) 로그만 남기고 넘어간다 - 분석 실패 때문에
   * 사용자가 챗봇 답변을 못 받으면 안 되니까(날씨 조회 실패를 무시하는 것과 같은 이유).
   */
  private void analyzeMessage(AiChatMessage userMessage) {
    try {
      String content = requestAnalysisCompletion(userMessage.getMessageText());
      JsonNode root = objectMapper.readTree(content);

      for (JsonNode tagNode : root.path("intents")) {
        String tag = tagNode.asText(null);
        if (tag != null && INTENT_TAGS.contains(tag)) {
          aiChatMessageIntentRepository.save(
              AiChatMessageIntent.builder().message(userMessage).intentTag(tag).build());
        }
      }

      JsonNode scores = root.path("scores");
      for (String scoreType : SCORE_TYPES) {
        JsonNode valueNode = scores.path(scoreType);
        if (valueNode.isNumber()) {
          aiChatMessageScoreRepository.save(
              AiChatMessageScore.builder()
                  .message(userMessage)
                  .scoreType(scoreType)
                  .scoreValue(valueNode.asInt())
                  .build());
        }
      }
    } catch (Exception e) {
      log.warn("메시지 의도/성향점수 분석 실패 (messageId={})", userMessage.getId(), e);
    }
  }

  /**
   * 분석 전용 OpenAI 호출. response_format을 json_object로 지정해서, 응답이 항상 파싱 가능한
   * JSON 문자열로만 오도록 강제한다(안 그러면 모델이 자연어 설명을 섞어서 답할 수 있음).
   */
  private String requestAnalysisCompletion(String userText) {
    List<Map<String, String>> messages = List.of(
        Map.of("role", "user", "content", ANALYSIS_PROMPT.formatted(userText))
    );

    Map<String, Object> requestBody = new LinkedHashMap<>();
    requestBody.put("model", MODEL);
    requestBody.put("messages", messages);
    requestBody.put("response_format", Map.of("type", "json_object"));

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setBearerAuth(apiKey);

    Map<?, ?> response = restTemplate.exchange(
        CHAT_COMPLETIONS_URL,
        HttpMethod.POST,
        new HttpEntity<>(requestBody, headers),
        Map.class
    ).getBody();

    List<?> choices = (List<?>) response.get("choices");
    Map<?, ?> firstChoice = (Map<?, ?>) choices.get(0);
    Map<?, ?> message = (Map<?, ?>) firstChoice.get("message");
    return (String) message.get("content");
  }

  /**
   * AiChatMessage 목록을 OpenAI의 messages 배열 형식([{role, content}, ...])으로 바꿔서 REST
   * 요청을 보내고, 응답에서 답변 텍스트만 꺼내 반환한다. situationMessages(현재 시각/날씨, 근처
   * 장소 목록)는 대화 내용이 아니라 매 요청마다 새로 만드는 상황 정보라서 DB에 저장하지 않고
   * 매번 새로 만들어 맨 앞에 system 메시지로만 끼워 보낸다.
   */
  private String requestChatCompletion(List<AiChatMessage> history, List<String> situationMessages) {
    List<Map<String, String>> messages = new ArrayList<>();
    messages.add(Map.of("role", "system", "content", PERSONA_SYSTEM_PROMPT));
    for (String situation : situationMessages) {
      messages.add(Map.of("role", "system", "content", situation));
    }
    for (AiChatMessage m : history) {
      // OpenAI가 이해하는 role은 user/assistant/system 뿐이라, 우리 senderType(USER/AI)을 변환해준다.
      String role = "AI".equals(m.getSenderType()) ? "assistant" : "user";
      messages.add(Map.of("role", role, "content", m.getMessageText()));
    }

    Map<String, Object> requestBody = new LinkedHashMap<>();
    requestBody.put("model", MODEL);
    requestBody.put("messages", messages);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setBearerAuth(apiKey); // Authorization: Bearer {apiKey}

    // 배운 노트북(ai['choices'][0]['message']['content'])과 똑같이, 응답을 타입 없는 Map으로 받아서
    // 그대로 파고 들어간다 - 전용 응답 DTO를 만들지 않아 구조가 단순하다.
    Map<?, ?> response = restTemplate.exchange(
        CHAT_COMPLETIONS_URL,
        HttpMethod.POST,
        new HttpEntity<>(requestBody, headers),
        Map.class
    ).getBody();

    List<?> choices = (List<?>) response.get("choices");
    Map<?, ?> firstChoice = (Map<?, ?>) choices.get(0);
    Map<?, ?> message = (Map<?, ?>) firstChoice.get("message");
    return (String) message.get("content");
  }

}
