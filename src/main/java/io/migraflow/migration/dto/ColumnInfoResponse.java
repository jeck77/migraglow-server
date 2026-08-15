package io.migraflow.migration.dto;

/**
 * DB 테이블의 컬럼 한 건에 대한 메타데이터.
 *
 * @param columnName       컬럼명
 * @param dataType         DB 컬럼 타입명 (예: VARCHAR, BIGINT)
 * @param nullable         NULL 허용 여부
 * @param defaultValue     DB에 정의된 기본값 (없으면 null)
 * @param primaryKey       기본키(PK) 구성 컬럼 여부. TO-BE 쪽에서 시퀀스/auto_increment로 채번되는 PK는 매핑 대상에서
 *                         제외하도록 화면에서 이 값을 참고한다
 * @param foreignKey       외래키(FK) 구성 컬럼 여부
 * @param referencedTable  FK가 참조하는 테이블명 (FK가 아니면 null)
 * @param referencedColumn FK가 참조하는 컬럼명 (FK가 아니면 null)
 */
public record ColumnInfoResponse(
        String columnName,
        String dataType,
        boolean nullable,
        String defaultValue,
        boolean primaryKey,
        boolean foreignKey,
        String referencedTable,
        String referencedColumn
) {
}
