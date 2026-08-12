package io.migraflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class MigraflowServerApplication {

	/**
	 * 애플리케이션의 진입점(entry point)으로, 내장 Tomcat 기반의 Spring Boot 애플리케이션을 기동한다.
	 *
	 * @param args 커맨드라인 인자
	 */
	public static void main(String[] args) {
		SpringApplication.run(MigraflowServerApplication.class, args);
	}

}
