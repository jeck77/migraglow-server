<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Migraflow · 프로젝트</title>
    <link rel="stylesheet" href="/css/app.css">
</head>
<body>
<%@ include file="/WEB-INF/jsp/common/header.jsp" %>
<main class="app-main">
    <div class="page-header">
        <h2>프로젝트</h2>
    </div>

    <div id="error" class="error" style="display:none;"></div>

    <form id="create-form" class="card">
        <div class="form-row">
            <input type="text" id="name" placeholder="프로젝트명" required>
            <input type="text" id="description" placeholder="설명 (선택)">
        </div>

        <p class="form-hint">AS-IS/TO-BE DB 접속정보는 프로젝트 소속 이관 작업들이 공유합니다. 지금 입력하지 않아도 등록은 되지만,
            이관 작업 화면에서 테이블 목록을 바로 보려면 여기서 먼저 등록해두세요. (선택)</p>

        <div class="db-schema-panel">
            <div class="db-schema-col">
                <h4>AS-IS DB</h4>
                <div class="form-row">
                    <select id="asis-db-type">
                        <option value="MYSQL">MySQL</option>
                        <option value="POSTGRESQL">PostgreSQL</option>
                        <option value="ORACLE">Oracle</option>
                        <option value="MSSQL">MS SQL Server</option>
                    </select>
                </div>
                <div class="form-row">
                    <input type="text" id="asis-host" placeholder="호스트 (예: localhost)">
                    <input type="text" id="asis-port" placeholder="포트" style="max-width:90px;">
                </div>
                <div class="form-row">
                    <input type="text" id="asis-database" placeholder="데이터베이스명">
                </div>
                <div class="form-row">
                    <input type="text" id="asis-username" placeholder="계정">
                    <input type="password" id="asis-password" placeholder="비밀번호">
                </div>
                <div class="form-row">
                    <button type="button" class="secondary" id="asis-list-tables">테이블 조회</button>
                    <span id="asis-schema-status" class="schema-status"></span>
                </div>
                <div id="asis-tables" class="schema-columns"></div>
            </div>
            <div class="db-schema-col">
                <h4>TO-BE DB</h4>
                <div class="form-row">
                    <select id="tobe-db-type">
                        <option value="MYSQL">MySQL</option>
                        <option value="POSTGRESQL">PostgreSQL</option>
                        <option value="ORACLE">Oracle</option>
                        <option value="MSSQL">MS SQL Server</option>
                    </select>
                </div>
                <div class="form-row">
                    <input type="text" id="tobe-host" placeholder="호스트 (예: localhost)">
                    <input type="text" id="tobe-port" placeholder="포트" style="max-width:90px;">
                </div>
                <div class="form-row">
                    <input type="text" id="tobe-database" placeholder="데이터베이스명">
                </div>
                <div class="form-row">
                    <input type="text" id="tobe-username" placeholder="계정">
                    <input type="password" id="tobe-password" placeholder="비밀번호">
                </div>
                <div class="form-row">
                    <button type="button" class="secondary" id="tobe-list-tables">테이블 조회</button>
                    <span id="tobe-schema-status" class="schema-status"></span>
                </div>
                <div id="tobe-tables" class="schema-columns"></div>
            </div>
        </div>

        <div class="form-row">
            <button type="submit" class="primary">프로젝트 등록</button>
        </div>
    </form>

    <div id="list" class="list">
        <div class="empty">불러오는 중...</div>
    </div>
</main>

<script src="/js/api.js"></script>
<script src="/js/projects.js"></script>
</body>
</html>
