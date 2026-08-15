package io.migraflow.project.dto;

/**
 * AS-IS/TO-BE 접속 정보 중 비밀번호를 제외한, 화면에 다시 보여줘도 안전한 부분만 담는 뷰.
 *
 * @param dbType   DB 벤더 구분
 * @param host     호스트
 * @param port     포트
 * @param database 데이터베이스명(Oracle은 SID)
 * @param username 접속 계정
 */
public record ProjectConnectionView(String dbType, String host, String port, String database, String username) {
}
