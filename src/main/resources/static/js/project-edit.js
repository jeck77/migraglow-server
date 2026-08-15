const projectId = window.PROJECT_ID;

function returnUrl() {
  const from = new URLSearchParams(window.location.search).get('from');
  if (from && from.startsWith('/') && !from.startsWith('//')) {
    return from;
  }
  return '/';
}

function escapeHtml(str) {
  const div = document.createElement('div');
  div.textContent = str == null ? '' : String(str);
  return div.innerHTML;
}

function showError(message) {
  const el = document.getElementById('error');
  if (!message) {
    el.style.display = 'none';
    el.textContent = '';
    return;
  }
  el.style.display = 'block';
  el.textContent = message;
}

var DB_TYPE_INFO = {
  MYSQL: { port: 3306, dbLabel: '데이터베이스명' },
  POSTGRESQL: { port: 5432, dbLabel: '데이터베이스명' },
  ORACLE: { port: 1521, dbLabel: 'SID' },
  MSSQL: { port: 1433, dbLabel: '데이터베이스명' },
};

function readConnection(prefix) {
  const dbType = document.getElementById(prefix + '-db-type').value;
  const info = DB_TYPE_INFO[dbType];
  return {
    dbType: dbType,
    host: document.getElementById(prefix + '-host').value.trim(),
    port: document.getElementById(prefix + '-port').value.trim() || String(info.port),
    database: document.getElementById(prefix + '-database').value.trim(),
    username: document.getElementById(prefix + '-username').value.trim(),
    password: document.getElementById(prefix + '-password').value,
  };
}

function applyPrefill(prefix, dbType, host, port, database, username) {
  if (!dbType) return;
  document.getElementById(prefix + '-db-type').value = dbType;
  document.getElementById(prefix + '-db-type').dispatchEvent(new Event('change'));
  document.getElementById(prefix + '-host').value = host || '';
  document.getElementById(prefix + '-port').value = port || '';
  document.getElementById(prefix + '-database').value = database || '';
  document.getElementById(prefix + '-username').value = username || '';
}

function connectionIsFilled(prefix) {
  return (
    document.getElementById(prefix + '-host').value.trim() &&
    document.getElementById(prefix + '-database').value.trim() &&
    document.getElementById(prefix + '-username').value.trim()
  );
}

function wireDbTypeSelect(prefix) {
  const select = document.getElementById(prefix + '-db-type');
  const portInput = document.getElementById(prefix + '-port');
  const databaseInput = document.getElementById(prefix + '-database');

  function applyDbType() {
    const info = DB_TYPE_INFO[select.value];
    portInput.placeholder = '포트 (' + info.port + ')';
    databaseInput.placeholder = info.dbLabel;
  }

  select.addEventListener('change', applyDbType);
  applyDbType();
}

function wireConnectionTest(prefix) {
  const button = document.getElementById(prefix + '-list-tables');
  const statusEl = document.getElementById(prefix + '-schema-status');

  button.addEventListener('click', function () {
    showError('');
    statusEl.textContent = '';
    statusEl.style.color = '';
    if (!connectionIsFilled(prefix)) {
      statusEl.style.color = 'var(--color-danger)';
      statusEl.textContent = '호스트, 데이터베이스명, 계정을 입력해주세요.';
      return;
    }
    button.disabled = true;
    statusEl.textContent = '조회 중...';
    Api.listSchemaTables(readConnection(prefix))
      .then(function (tables) {
        statusEl.textContent = tables.length + '개 테이블 확인됨';
        document.getElementById(prefix + '-tables').innerHTML = tables
          .map(function (t) { return '<div class="schema-column-row"><span>' + escapeHtml(t) + '</span></div>'; })
          .join('');
      })
      .catch(function (e) {
        statusEl.style.color = 'var(--color-danger)';
        statusEl.textContent = e.message;
      })
      .finally(function () {
        button.disabled = false;
      });
  });
}

wireDbTypeSelect('asis');
wireDbTypeSelect('tobe');
wireConnectionTest('asis');
wireConnectionTest('tobe');

Api.getProject(projectId)
  .then(function (p) {
    document.getElementById('project-name').textContent = p.name;
    document.getElementById('name').value = p.name;
    document.getElementById('description').value = p.description || '';
    document.getElementById('cancel-link').href = returnUrl();
    document.getElementById('asis-current-status').textContent = p.sourceConfigured ? '현재 등록됨' : '현재 미등록';
    document.getElementById('tobe-current-status').textContent = p.targetConfigured ? '현재 등록됨' : '현재 미등록';
    applyPrefill('asis', p.sourceDbType, p.sourceHost, p.sourcePort, p.sourceDatabase, p.sourceUsername);
    applyPrefill('tobe', p.targetDbType, p.targetHost, p.targetPort, p.targetDatabase, p.targetUsername);
  })
  .catch(function (e) {
    showError(e.message);
  });

document.getElementById('edit-form').addEventListener('submit', function (e) {
  e.preventDefault();
  const name = document.getElementById('name').value.trim();
  const description = document.getElementById('description').value.trim();
  if (!name) return;

  const body = { name: name, description: description || undefined };
  if (connectionIsFilled('asis')) {
    body.sourceConfig = JSON.stringify(readConnection('asis'));
  }
  if (connectionIsFilled('tobe')) {
    body.targetConfig = JSON.stringify(readConnection('tobe'));
  }

  showError('');
  Api.updateProject(projectId, body)
    .then(function () {
      window.location.href = returnUrl();
    })
    .catch(function (e) {
      showError(e.message);
    });
});
