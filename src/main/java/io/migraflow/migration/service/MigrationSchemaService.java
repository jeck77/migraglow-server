package io.migraflow.migration.service;

import io.migraflow.migration.dto.ColumnInfoResponse;
import io.migraflow.migration.dto.DbConnectionRequest;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;

/**
 * AS-IS/TO-BE DB에 직접 접속해 테이블·컬럼 메타데이터를 조회하는 서비스. 이관 작업 등록 화면에서 담당자가
 * 이관 대상 테이블/컬럼을 고를 수 있도록 지원하며, 조회 결과를 별도로 저장하지 않는 stateless 조회 전용 기능이다.
 */
@Service
public class MigrationSchemaService {

    private static final Map<String, Integer> DEFAULT_PORTS = Map.of(
            "MYSQL", 3306, "POSTGRESQL", 5432, "ORACLE", 1521, "MSSQL", 1433);

    /**
     * 지정한 DB에 접속해 조회 가능한 테이블 목록을 반환한다.
     *
     * @param connection AS-IS/TO-BE DB 접속 정보
     * @return 테이블명 목록
     * @throws MigrationSchemaConnectionException DB 접속에 실패한 경우
     */
    public List<String> listTables(DbConnectionRequest connection) {
        try (Connection conn = openConnection(connection)) {
            DatabaseMetaData metaData = conn.getMetaData();
            List<String> tables = new ArrayList<>();
            try (ResultSet rs = metaData.getTables(conn.getCatalog(), conn.getSchema(), "%", new String[]{"TABLE"})) {
                while (rs.next()) {
                    tables.add(rs.getString("TABLE_NAME"));
                }
            }
            return tables;
        } catch (SQLException e) {
            throw new MigrationSchemaConnectionException(e);
        }
    }

    /**
     * 지정한 DB의 특정 테이블에 속한 컬럼 목록을 반환한다.
     *
     * @param connection AS-IS/TO-BE DB 접속 정보
     * @param tableName  컬럼 목록을 조회할 테이블명
     * @return 컬럼 메타데이터 목록
     * @throws MigrationSchemaConnectionException DB 접속에 실패한 경우
     */
    public List<ColumnInfoResponse> listColumns(DbConnectionRequest connection, String tableName) {
        try (Connection conn = openConnection(connection)) {
            DatabaseMetaData metaData = conn.getMetaData();
            Set<String> primaryKeyColumns = new HashSet<>();
            try (ResultSet rs = metaData.getPrimaryKeys(conn.getCatalog(), conn.getSchema(), tableName)) {
                while (rs.next()) {
                    primaryKeyColumns.add(rs.getString("COLUMN_NAME"));
                }
            }
            Map<String, ForeignKeyInfo> foreignKeys = new HashMap<>();
            try (ResultSet rs = metaData.getImportedKeys(conn.getCatalog(), conn.getSchema(), tableName)) {
                while (rs.next()) {
                    foreignKeys.put(
                            rs.getString("FKCOLUMN_NAME"),
                            new ForeignKeyInfo(rs.getString("PKTABLE_NAME"), rs.getString("PKCOLUMN_NAME")));
                }
            }
            List<ColumnInfoResponse> columns = new ArrayList<>();
            try (ResultSet rs = metaData.getColumns(conn.getCatalog(), conn.getSchema(), tableName, "%")) {
                while (rs.next()) {
                    String columnName = rs.getString("COLUMN_NAME");
                    ForeignKeyInfo fk = foreignKeys.get(columnName);
                    columns.add(new ColumnInfoResponse(
                            columnName,
                            rs.getString("TYPE_NAME"),
                            rs.getInt("NULLABLE") == DatabaseMetaData.columnNullable,
                            rs.getString("COLUMN_DEF"),
                            primaryKeyColumns.contains(columnName),
                            fk != null,
                            fk == null ? null : fk.referencedTable(),
                            fk == null ? null : fk.referencedColumn()
                    ));
                }
            }
            return columns;
        } catch (SQLException e) {
            throw new MigrationSchemaConnectionException(e);
        }
    }

    /**
     * 접속 정보로 새 JDBC 커넥션을 연다. 호출부가 try-with-resources로 반드시 닫아야 한다.
     *
     * @param connection AS-IS/TO-BE DB 접속 정보
     * @return 새로 연 JDBC 커넥션
     * @throws SQLException 접속에 실패한 경우
     */
    private Connection openConnection(DbConnectionRequest connection) throws SQLException {
        return DriverManager.getConnection(buildJdbcUrl(connection), connection.username(), connection.password());
    }

    /**
     * 접속 정보의 호스트/포트/DB명을 벤더별 JDBC URL 형식으로 조립한다. 포트가 비어있으면 벤더 기본 포트를 쓴다.
     *
     * @param connection AS-IS/TO-BE DB 접속 정보
     * @return 조립된 JDBC URL
     * @throws IllegalArgumentException 지원하지 않는 dbType인 경우 (화면의 "DB 종류" 선택지가 이미 4종으로 제한하므로
     *                                   API를 직접 호출하지 않는 한 발생하지 않는다)
     */
    private String buildJdbcUrl(DbConnectionRequest connection) {
        String host = connection.host();
        String database = connection.database();
        String port = (connection.port() == null || connection.port().isBlank())
                ? String.valueOf(DEFAULT_PORTS.getOrDefault(connection.dbType(), 0))
                : connection.port();
        return switch (connection.dbType()) {
            case "MYSQL" -> "jdbc:mysql://" + host + ":" + port + "/" + database;
            case "POSTGRESQL" -> "jdbc:postgresql://" + host + ":" + port + "/" + database;
            case "ORACLE" -> "jdbc:oracle:thin:@" + host + ":" + port + ":" + database;
            case "MSSQL" -> "jdbc:sqlserver://" + host + ":" + port + ";databaseName=" + database;
            default -> throw new IllegalArgumentException("Unsupported dbType: " + connection.dbType());
        };
    }

    /**
     * {@code getImportedKeys} 조회 결과를 컬럼별로 잠깐 들고 있기 위한 내부 전용 구조체.
     *
     * @param referencedTable  FK가 참조하는 테이블명
     * @param referencedColumn FK가 참조하는 컬럼명
     */
    private record ForeignKeyInfo(String referencedTable, String referencedColumn) {
    }
}
