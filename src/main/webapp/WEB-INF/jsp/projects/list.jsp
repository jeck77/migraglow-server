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
<header class="app-header">
    <a href="/" class="app-title">Migraflow</a>
</header>
<main class="app-main">
    <div class="page-header">
        <h2>프로젝트</h2>
    </div>

    <div id="error" class="error" style="display:none;"></div>

    <form id="create-form" class="card">
        <div class="form-row">
            <input type="text" id="name" placeholder="프로젝트명" required>
            <input type="text" id="description" placeholder="설명 (선택)">
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
