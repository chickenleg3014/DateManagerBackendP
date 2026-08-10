package org.ict.datemanagerbackend;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

// @EnableScheduling: @Scheduled가 붙은 메서드(PlaceSyncService.syncPerformances 등)를
// 스프링이 실제로 정해진 시간에 실행시켜주도록 스케줄링 기능 자체를 켜는 스위치
@EnableScheduling
@SpringBootApplication
public class DateManagerBackendApplication {

  public static void main(String[] args) {
    Dotenv dotenv = Dotenv.configure()
        .ignoreIfMissing() //환경변수 파일이 없을 시 시스템 환경변수를 읽어오는 메소드 입니다.
        .load();
    // 읽어온 환경변수를 System Property로 등록
    dotenv.entries().forEach(entry -> System.setProperty(
            entry.getKey(),
            entry.getValue()));

    SpringApplication.run(DateManagerBackendApplication.class, args);
  }

}
