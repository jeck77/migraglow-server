<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Migraflow · 프로젝트 수정</title>
    <link rel="stylesheet" href="/css/app.css">
</head>
<body>
<%@ include file="/WEB-INF/jsp/common/header.jsp" %>
<main class="app-main">
    <div class="breadcrumb"><a href="/">프로젝트</a> / <span id="project-name">...</span> 수정</div>
    <div class="page-header">
        <h2>프로젝트 수정</h2>
    </div>

    <div id="error" class="error" style="display:none;"></div>

    <form id="edit-form" class="card">
        <div class="form-row">
            <input type="text" id="name" placeholder="프로젝트명" required>
            <input type="text" id="description" placeholder="설명 (선택)">
        </div>

        <p class="form-hint">
            기존에 등록한 AS-IS/TO-BE 접속정보가 자동으로 채워져 있습니다. 보안상 <strong>비밀번호만은 다시 보여줄 수 없어 항상 비워둔 채로</strong> 시작합니다 —
            <strong>비밀번호를 비워두면 기존 비밀번호가 그대로 유지</strong>되고, 입력하면 새 비밀번호로 교체됩니다. 나머지 항목은 화면에 보이는 값 그대로 저장됩니다.
        </p>

        <div class="db-schema-panel">
            <div class="db-schema-col">
                <h4>AS-IS DB <span id="asis-current-status" class="mapping-side-label"></span></h4>
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
                    <input type="password" id="asis-password" placeholder="비밀번호 (비워두면 기존 값 유지)">
                </div>
                <div class="form-row">
                    <button type="button" class="secondary" id="asis-list-tables">테이블 조회</button>
                    <span id="asis-schema-status" class="schema-status"></span>
                </div>
                <div id="asis-tables" class="schema-columns"></div>
            </div>
            <div class="db-schema-col">
                <h4>TO-BE DB <span id="tobe-current-status" class="mapping-side-label"></span></h4>
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
                    <input type="password" id="tobe-password" placeholder="비밀번호 (비워두면 기존 값 유지)">
                </div>
                <div class="form-row">
                    <button type="button" class="secondary" id="tobe-list-tables">테이블 조회</button>
                    <span id="tobe-schema-status" class="schema-status"></span>
                </div>
                <div id="tobe-tables" class="schema-columns"></div>
            </div>
        </div>

        <div class="form-row">
            <button type="submit" class="primary">저장</button>
            <a class="secondary" id="cancel-link" href="/">취소</a>
        </div>
    </form>
</main>

<script>
    window.PROJECT_ID = ${projectId};
</script>
<script src="/js/api.js"></script>
<script src="/js/project-edit.js"></script>
</body>
</html>
