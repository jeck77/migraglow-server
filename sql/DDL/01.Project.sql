create table PROJECT
(
    ID           bigint auto_increment comment '프로젝트 ID' primary key,
    NAME         varchar(100)                        not null comment '프로젝트명',
    DESCRIPTION  text                                null comment '프로젝트 설명',
    STATUS       int       default 0                 not null comment '프로젝트 상태',
    CREATE_DATE  timestamp default CURRENT_TIMESTAMP not null comment '생성일',
    UPDATED_DATE timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '수정일'
);