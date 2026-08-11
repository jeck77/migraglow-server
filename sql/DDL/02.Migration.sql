create table MIGRATION_JOB
(
    ID                 bigint auto_increment comment '이관 작업 ID' primary key,
    PROJECT_ID         bigint                              not null comment '소속 프로젝트 ID',
    NAME               varchar(200)                        not null comment '작업명',
    TARGET_ENTITY_NAME varchar(100)                        not null comment 'TO-BE 매핑 대상 엔티티/테이블명',
    SOURCE_TYPE        int                                 not null comment 'AS-IS 수집 방식 (0:DB, 1:API, 2:FILE)',
    SOURCE_CONFIG      json                                null comment '소스타입별 접속/조회 설정 (SOURCE_TYPE에 따라 shape 다름)',
    STATUS             int       default 0                 not null comment '작업 상태 (0:DRAFT, 1:SUBMITTED, 2:APPROVED, 3:REJECTED, 4:EXECUTING, 5:COMPLETED, 6:FAILED)',
    CREATED_BY_ID      bigint                              not null comment '등록 담당자 ID',
    SUBMITTED_BY_ID    bigint                              null comment '최근 제출 담당자 ID',
    SUBMIT_DATE        timestamp                           null comment '최근 제출 일시',
    APPROVED_BY_ID     bigint                              null comment '최근 승인자 ID',
    APPROVE_DATE       timestamp                           null comment '최근 승인 일시',
    REJECT_REASON      varchar(500)                        null comment '최근 반려 사유',
    EXECUTE_START_DATE timestamp                           null comment '최근 배치 실행 시작 일시',
    EXECUTE_END_DATE   timestamp                           null comment '최근 배치 실행 종료 일시',
    CREATE_DATE        timestamp default CURRENT_TIMESTAMP not null comment '생성일',
    UPDATED_DATE       timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '수정일',
    constraint FK_MIGRATION_JOB_PROJECT_ID
        foreign key (PROJECT_ID) references PROJECT (ID)
);

create index IDX_MIGRATION_JOB_PROJECT_ID
    on MIGRATION_JOB (PROJECT_ID);

create table MIGRATION_JOB_APPROVAL_HISTORY
(
    ID          bigint auto_increment comment '이관 작업 승인 이력 ID' primary key,
    JOB_ID      bigint                              not null comment '이관 작업 ID',
    ACTION_TYPE int                                 not null comment '액션 유형 (0:SUBMIT, 1:APPROVE, 2:REJECT)',
    ACTOR_ID    bigint                              not null comment '실행자 ID (제출자/승인자)',
    ACTION_DATE timestamp                           not null comment '액션 일시',
    REASON      varchar(500)                        null comment '반려 사유 등',
    CREATE_DATE timestamp default CURRENT_TIMESTAMP not null comment '생성일',
    constraint FK_MIGRATION_JOB_APPROVAL_HISTORY_JOB_ID
        foreign key (JOB_ID) references MIGRATION_JOB (ID)
);

create index IDX_MIGRATION_JOB_APPROVAL_HISTORY_JOB_ID
    on MIGRATION_JOB_APPROVAL_HISTORY (JOB_ID);

create table MIGRATION_MAPPING_RULE
(
    ID                 bigint auto_increment comment '매핑 규칙 ID' primary key,
    TARGET_ENTITY_NAME varchar(100)                        not null comment '매핑 대상 엔티티/테이블명',
    SOURCE_FIELD_NAME  varchar(100)                        not null comment 'AS-IS 필드명',
    TARGET_FIELD_NAME  varchar(100)                        not null comment 'TO-BE 필드명',
    RULE_TYPE          int                                 not null comment '규칙 유형 (0:DIRECT, 1:VALUE_MAP, 2:EXPRESSION)',
    EXPRESSION         varchar(500)                        null comment 'RULE_TYPE=EXPRESSION일 때 변환식',
    USE_YN             tinyint(1) default 1                not null comment '사용 여부',
    CREATE_DATE        timestamp default CURRENT_TIMESTAMP not null comment '생성일',
    UPDATED_DATE       timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '수정일',
    constraint UK_MIGRATION_MAPPING_RULE_TARGET
        unique (TARGET_ENTITY_NAME, TARGET_FIELD_NAME)
);

create table MIGRATION_MAPPING_RULE_VALUE
(
    ID           bigint auto_increment comment '매핑 규칙 값 ID' primary key,
    RULE_ID      bigint                              not null comment '매핑 규칙 ID',
    SOURCE_VALUE varchar(255)                        not null comment 'AS-IS 값',
    TARGET_VALUE varchar(255)                        not null comment 'TO-BE 값',
    CREATE_DATE  timestamp default CURRENT_TIMESTAMP not null comment '생성일',
    constraint FK_MIGRATION_MAPPING_RULE_VALUE_RULE_ID
        foreign key (RULE_ID) references MIGRATION_MAPPING_RULE (ID)
);

create index IDX_MIGRATION_MAPPING_RULE_VALUE_RULE_ID
    on MIGRATION_MAPPING_RULE_VALUE (RULE_ID);

create table MIGRATION_RECORD
(
    ID            bigint auto_increment comment '이관 대상 레코드 ID' primary key,
    JOB_ID        bigint                              not null comment '이관 작업 ID',
    SOURCE_KEY    varchar(255)                        not null comment 'AS-IS 원본 식별자',
    RAW_DATA      json                                not null comment 'AS-IS 원본 레코드 스냅샷',
    STATUS        int       default 0                 not null comment '레코드 상태 (0:COLLECTED, 1:MAPPED, 2:CORRECTED, 3:EXCLUDED, 4:EXECUTED, 5:FAILED)',
    ERROR_MESSAGE varchar(1000)                       null comment '배치 실행 실패 사유',
    CREATE_DATE   timestamp default CURRENT_TIMESTAMP not null comment '생성일',
    UPDATED_DATE  timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '수정일',
    constraint FK_MIGRATION_RECORD_JOB_ID
        foreign key (JOB_ID) references MIGRATION_JOB (ID)
);

create index IDX_MIGRATION_RECORD_JOB_ID_STATUS
    on MIGRATION_RECORD (JOB_ID, STATUS);

create table MIGRATION_RECORD_FIELD
(
    ID                bigint auto_increment comment '레코드 필드 ID' primary key,
    RECORD_ID         bigint                              not null comment '이관 대상 레코드 ID',
    RULE_ID           bigint                              null comment '자동 매핑 시 적용된 매핑 규칙 ID',
    TARGET_FIELD_NAME varchar(100)                        not null comment 'TO-BE 필드명',
    RAW_VALUE         varchar(1000)                       null comment '원본값',
    MAPPED_VALUE      varchar(1000)                       null comment '매핑 규칙 적용 후 값',
    MANUAL_VALUE      varchar(1000)                       null comment '담당자 수동 교정값',
    FINAL_VALUE       varchar(1000)                       null comment '최종값',
    IS_MANUAL_YN      tinyint(1) default 0                not null comment '수동 교정 여부',
    constraint FK_MIGRATION_RECORD_FIELD_RECORD_ID
        foreign key (RECORD_ID) references MIGRATION_RECORD (ID),
    constraint FK_MIGRATION_RECORD_FIELD_RULE_ID
        foreign key (RULE_ID) references MIGRATION_MAPPING_RULE (ID),
    constraint UK_MIGRATION_RECORD_FIELD_RECORD_TARGET
        unique (RECORD_ID, TARGET_FIELD_NAME)
);

create table MIGRATION_EXECUTION_HISTORY
(
    ID          bigint auto_increment comment '배치 실행 이력 ID' primary key,
    JOB_ID      bigint                              not null comment '이관 작업 ID',
    STATUS      int       default 0                 not null comment '실행 상태 (0:RUNNING, 1:COMPLETED, 2:FAILED)',
    START_DATE  timestamp default CURRENT_TIMESTAMP not null comment '실행 시작 일시',
    END_DATE    timestamp                           null comment '실행 종료 일시',
    SUCCESS_CNT int       default 0                 not null comment '성공 레코드 수',
    FAIL_CNT    int       default 0                 not null comment '실패 레코드 수',
    constraint FK_MIGRATION_EXECUTION_HISTORY_JOB_ID
        foreign key (JOB_ID) references MIGRATION_JOB (ID)
);

create index IDX_MIGRATION_EXECUTION_HISTORY_JOB_ID
    on MIGRATION_EXECUTION_HISTORY (JOB_ID);
