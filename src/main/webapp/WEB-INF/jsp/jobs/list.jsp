<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Migraflow · 이관 작업</title>
    <link rel="stylesheet" href="/css/app.css">
</head>
<body>
<header class="app-header">
    <a href="/" class="app-title">Migraflow</a>
</header>
<main class="app-main">
    <div class="breadcrumb"><a href="/">프로젝트</a> / <span id="project-name">...</span></div>
    <div class="page-header">
        <h2>이관 작업</h2>
    </div>

    <div id="error" class="error" style="display:none;"></div>

    <div class="card">
        <div class="form-row">
            <label>담당자 ID: <input type="number" id="actor-id" value="1" style="width:60px;"></label>
        </div>
        <form id="create-form">
            <div class="form-row">
                <input type="text" id="name" placeholder="작업명" required>
                <input type="text" id="target-entity-name" placeholder="TO-BE 대상 엔티티 (예: MEMBER)" required>
                <select id="source-type">
                    <option value="DB">DB</option>
                    <option value="API">API</option>
                    <option value="FILE">FILE</option>
                </select>
            </div>
            <div class="form-row">
                <textarea id="source-config" placeholder="소스 설정 (선택, JSON 문자열)"></textarea>
            </div>
            <div class="form-row">
                <button type="submit" class="primary">이관 작업 등록</button>
            </div>
        </form>
    </div>

    <div id="list" class="list">
        <div class="empty">불러오는 중...</div>
    </div>
</main>

<script>
    window.PROJECT_ID = ${projectId};
</script>
<script src="/js/api.js"></script>
<script src="/js/jobs.js"></script>
</body>
</html>
