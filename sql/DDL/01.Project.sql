create table PROJECT
(
    ID           bigint auto_increment comment '프로젝트 ID' primary key,
    NAME         varchar(100)                        not null comment '프로젝트명',
    DESCRIPTION  text                                null comment '프로젝트 설명',
    SOURCE_CONFIG json                                null comment 'AS-IS DB 접속 설정 (dbType/jdbcUrl/username/password, 테이블명은 미포함)',
    TARGET_CONFIG json                                null comment 'TO-BE DB 접속 설정 (dbType/jdbcUrl/username/password, 테이블명은 미포함)',
    STATUS       int       default 0                 not null comment '프로젝트 상태',
    CREATE_DATE  timestamp default CURRENT_TIMESTAMP not null comment '생성일',
    UPDATED_DATE timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '수정일'
);