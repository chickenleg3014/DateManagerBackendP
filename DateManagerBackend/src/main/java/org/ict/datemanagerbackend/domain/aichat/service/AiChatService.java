package org.ict.datemanagerbackend.domain.aichat.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ict.datemanagerbackend.domain.aichat.entity.AiChatMessage;
import org.ict.datemanagerbackend.domain.aichat.entity.AiChatSession;
import org.ict.datemanagerbackend.domain.aichat.repository.AiChatMessageRepository;
import org.ict.datemanagerbackend.domain.aichat.repository.AiChatSessionRepository;
import org.ict.datemanagerbackend.domain.user.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

  private final AiChatSessionRepository aiChatSessionRepository;
  private final AiChatMessageRepository aiChatMessageRepository;
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
   */
  public AiChatMessage sendMessage(User user, Long sessionId, String userText) {
    AiChatSession session = getOwnedSession(user, sessionId);

    AiChatMessage userMessage = AiChatMessage.builder()
        .session(session)
        .senderType("USER")
        .sender(user)
        .messageText(userText)
        .build();
    aiChatMessageRepository.save(userMessage);

    // 방금 저장한 유저 메시지까지 포함해서 지금까지의 대화 전체를 시간순으로 가져온다.
    List<AiChatMessage> history = aiChatMessageRepository.findBySession_IdOrderByCreatedAtAsc(sessionId);
    String aiText = requestChatCompletion(history);

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
   * AiChatMessage 목록을 OpenAI의 messages 배열 형식([{role, content}, ...])으로 바꿔서 REST
   * 요청을 보내고, 응답에서 답변 텍스트만 꺼내 반환한다.
   */
  private String requestChatCompletion(List<AiChatMessage> history) {
    List<Map<String, String>> messages = new ArrayList<>();
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
