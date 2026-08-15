<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Migraflow · 컬럼 매핑</title>
    <link rel="stylesheet" href="/css/app.css">
</head>
<body>
<%@ include file="/WEB-INF/jsp/common/header.jsp" %>
<main class="app-main">
    <div class="breadcrumb">
        <a href="/">프로젝트</a> / <a href="/projects/${projectId}/jobs">이관 작업</a> / <span id="job-name">...</span>
    </div>
    <div class="page-header">
        <h2>컬럼 매핑</h2>
    </div>

    <div id="error" class="error" style="display:none;"></div>

    <div class="card">
        <div class="mapping-summary">
            <div class="mapping-summary-item">
                <span class="mapping-summary-label">AS-IS 테이블</span>
                <strong id="source-table-name" class="mapping-summary-value mapping-summary-asis">...</strong>
            </div>
            <span class="mapping-summary-arrow">→</span>
            <div class="mapping-summary-item">
                <span class="mapping-summary-label">TO-BE 테이블</span>
                <strong id="target-table-name" class="mapping-summary-value mapping-summary-tobe">...</strong>
            </div>
        </div>
        <div class="list-item-meta">매핑 규칙 그룹(대상 엔티티명): <strong id="target-entity-name">...</strong></div>
    </div>

    <div id="mapping-list" class="mapping-list">
        <div class="empty">불러오는 중...</div>
    </div>
</main>

<script>
    window.PROJECT_ID = ${projectId};
    window.JOB_ID = ${jobId};
</script>
<script src="/js/api.js"></script>
<script src="/js/mapping.js"></script>
</body>
</html>
