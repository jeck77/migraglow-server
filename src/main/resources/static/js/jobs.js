const projectId = window.PROJECT_ID;
let historyOpenJobId = null;
const historyCache = {};
let rejectOpenJobId = null;

// 로그인/세션이 아직 없어 담당자 ID를 화면에서 입력받지 않고 임시로 고정한다.
// 나중에 인증이 추가되면 이 값을 실제 로그인 사용자 ID로 교체한다.
const actorId = 1;

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

function escapeHtml(str) {
  const div = document.createElement('div');
  div.textContent = str == null ? '' : String(str);
  return div.innerHTML;
}

function renderJobs(jobs) {
  const list = document.getElementById('list');
  if (jobs.length === 0) {
    list.innerHTML = '<div class="empty">등록된 이관 작업이 없습니다.</div>';
    return;
  }
  list.innerHTML = jobs.map(renderJobItem).join('');
  jobs.forEach(function (job) {
    if (historyOpenJobId === job.id && historyCache[job.id]) {
      renderHistoryInto(job.id, historyCache[job.id]);
    }
  });
}

function renderJobItem(job) {
  let actions = '<a class="secondary small" href="/projects/' + projectId + '/jobs/' + job.id + '/mapping">컬럼 매핑</a>';
  if (job.status === 'DRAFT' || job.status === 'REJECTED') {
    actions += '<button class="secondary small" data-action="submit" data-job-id="' + job.id + '">제출</button>';
  }
  if (job.status === 'SUBMITTED') {
    actions += '<button class="secondary small" data-action="approve" data-job-id="' + job.id + '">승인</button>';
    actions += '<button class="danger small" data-action="toggle-reject" data-job-id="' + job.id + '">반려</button>';
  }
  actions +=
    '<button class="secondary small" data-action="toggle-history" data-job-id="' +
    job.id +
    '">' +
    (historyOpenJobId === job.id ? '이력 숨기기' : '이력 보기') +
    '</button>';

  let rejectForm = '';
  if (rejectOpenJobId === job.id) {
    rejectForm =
      '<div class="form-row" style="margin-top:8px;">' +
      '<input type="text" class="reject-reason" data-job-id="' + job.id + '" placeholder="반려 사유">' +
      '<button class="danger" data-action="confirm-reject" data-job-id="' + job.id + '">반려 확정</button>' +
      '</div>';
  }

  return (
    '<div class="list-item" data-job-row="' + job.id + '">' +
    '<div class="list-item-top">' +
    '<span class="list-item-title">' + escapeHtml(job.name) + '</span>' +
    '<span class="badge status-' + job.status + '">' + job.status + '</span>' +
    '</div>' +
    '<div class="list-item-meta">' +
    escapeHtml(job.targetEntityName) + ' · ' + job.sourceType + ' · 등록자 ' + job.createdById +
    (job.submittedById ? ' · 제출자 ' + job.submittedById : '') +
    (job.approvedById ? ' · 승인자 ' + job.approvedById : '') +
    '</div>' +
    (job.sourceTableName || job.targetTableName
      ? '<div class="list-item-meta">' +
        escapeHtml(job.sourceTableName || '(AS-IS 미선택)') + ' → ' + escapeHtml(job.targetTableName || '(TO-BE 미선택)') +
        '</div>'
      : '') +
    (job.rejectReason ? '<div class="list-item-meta">반려 사유: ' + escapeHtml(job.rejectReason) + '</div>' : '') +
    '<div class="list-item-actions">' + actions + '</div>' +
    rejectForm +
    '<div class="history-list" id="history-' + job.id + '" style="display:' +
    (historyOpenJobId === job.id ? 'flex' : 'none') + ';"></div>' +
    '</div>'
  );
}

function renderHistoryInto(jobId, history) {
  const el = document.getElementById('history-' + jobId);
  if (!el) return;
  if (history.length === 0) {
    el.innerHTML = '<div class="history-item">이력이 없습니다.</div>';
    return;
  }
  el.innerHTML = history
    .map(function (h) {
      return (
        '<div class="history-item"><strong>' + h.actionType + '</strong> · 실행자 ' + h.actorId + ' · ' + h.actionDate +
        (h.reason ? ' · ' + escapeHtml(h.reason) : '') +
        '</div>'
      );
    })
    .join('');
}

function loadJobs() {
  Api.listMigrationJobs(projectId)
    .then(renderJobs)
    .catch(function (e) {
      showError(e.message);
    });
}

document.getElementById('list').addEventListener('click', function (e) {
  const btn = e.target.closest('button[data-action]');
  if (!btn) return;
  const jobId = Number(btn.getAttribute('data-job-id'));
  const action = btn.getAttribute('data-action');
  showError('');

  if (action === 'submit') {
    Api.submitMigrationJob(jobId, actorId).then(loadJobs).catch(function (e) { showError(e.message); });
  } else if (action === 'approve') {
    Api.approveMigrationJob(jobId, actorId).then(loadJobs).catch(function (e) { showError(e.message); });
  } else if (action === 'toggle-reject') {
    rejectOpenJobId = rejectOpenJobId === jobId ? null : jobId;
    loadJobs();
  } else if (action === 'confirm-reject') {
    const input = document.querySelector('.reject-reason[data-job-id="' + jobId + '"]');
    const reason = input ? input.value.trim() : '';
    if (!reason) return;
    Api.rejectMigrationJob(jobId, actorId, reason)
      .then(function () {
        rejectOpenJobId = null;
        loadJobs();
      })
      .catch(function (e) {
        showError(e.message);
      });
  } else if (action === 'toggle-history') {
    if (historyOpenJobId === jobId) {
      historyOpenJobId = null;
      loadJobs();
    } else {
      Api.getApprovalHistory(jobId)
        .then(function (history) {
          historyCache[jobId] = history;
          historyOpenJobId = jobId;
          loadJobs();
        })
        .catch(function (e) {
          showError(e.message);
        });
    }
  }
});

function formatColumnMeta(c) {
  const parts = [c.dataType];
  if (c.primaryKey) parts.push('PK');
  if (!c.nullable) parts.push('NOT NULL');
  if (c.defaultValue !== null && c.defaultValue !== undefined) parts.push('DEFAULT ' + c.defaultValue);
  if (c.foreignKey) parts.push('FK → ' + c.referencedTable + '.' + c.referencedColumn);
  return parts.join(' · ');
}

function renderColumns(prefix, columns) {
  const el = document.getElementById(prefix + '-columns');
  if (columns.length === 0) {
    el.innerHTML = '<div class="empty">컬럼이 없습니다.</div>';
    return;
  }
  el.innerHTML = columns
    .map(function (c) {
      return (
        '<div class="schema-column-row"><span>' + escapeHtml(c.columnName) + '</span>' +
        '<span>' + escapeHtml(formatColumnMeta(c)) + '</span></div>'
      );
    })
    .join('');
}

function wireSchemaPanel(prefix, listTablesFn, listColumnsFn) {
  const statusEl = document.getElementById(prefix + '-schema-status');
  const select = document.getElementById(prefix + '-table-select');

  statusEl.textContent = '테이블 목록 불러오는 중...';
  listTablesFn()
    .then(function (tables) {
      select.innerHTML =
        '<option value="">-- 테이블 선택 --</option>' +
        tables.map(function (t) { return '<option value="' + escapeHtml(t) + '">' + escapeHtml(t) + '</option>'; }).join('');
      statusEl.textContent = tables.length + '개 테이블';
    })
    .catch(function (e) {
      select.innerHTML = '<option value="">-- 테이블 없음 --</option>';
      statusEl.style.color = 'var(--color-danger)';
      statusEl.textContent = e.message;
    });

  select.addEventListener('change', function (e) {
    const tableName = e.target.value;
    const columnsEl = document.getElementById(prefix + '-columns');
    if (!tableName) {
      columnsEl.innerHTML = '';
      return;
    }
    showError('');
    listColumnsFn(tableName)
      .then(function (columns) {
        renderColumns(prefix, columns);
      })
      .catch(function (e) {
        showError(e.message);
      });
  });
}

wireSchemaPanel(
  'asis',
  function () { return Api.listProjectSourceTables(projectId); },
  function (tableName) { return Api.listProjectSourceColumns(projectId, tableName); }
);
wireSchemaPanel(
  'tobe',
  function () { return Api.listProjectTargetTables(projectId); },
  function (tableName) { return Api.listProjectTargetColumns(projectId, tableName); }
);

document.getElementById('source-type').addEventListener('change', function (e) {
  const isDb = e.target.value === 'DB';
  document.getElementById('db-schema-panel').style.display = isDb ? 'flex' : 'none';
  document.getElementById('source-config-row').style.display = isDb ? 'none' : 'flex';
});

document.getElementById('create-form').addEventListener('submit', function (e) {
  e.preventDefault();
  const name = document.getElementById('name').value.trim();
  const targetEntityName = document.getElementById('target-entity-name').value.trim();
  const sourceType = document.getElementById('source-type').value;
  if (!name || !targetEntityName) return;

  let sourceConfig;
  let targetConfig;
  if (sourceType === 'DB') {
    const asisTable = document.getElementById('asis-table-select').value;
    const tobeTable = document.getElementById('tobe-table-select').value;
    if (!asisTable || !tobeTable) {
      showError('AS-IS/TO-BE 테이블을 모두 선택해주세요.');
      return;
    }
    sourceConfig = JSON.stringify({ tableName: asisTable });
    targetConfig = JSON.stringify({ tableName: tobeTable });
  } else {
    const raw = document.getElementById('source-config').value.trim();
    sourceConfig = raw || undefined;
  }

  showError('');
  Api.createMigrationJob(projectId, {
    name: name,
    targetEntityName: targetEntityName,
    sourceType: sourceType,
    sourceConfig: sourceConfig,
    targetConfig: targetConfig,
    createdById: actorId,
  })
    .then(function () {
      document.getElementById('name').value = '';
      document.getElementById('target-entity-name').value = '';
      document.getElementById('source-config').value = '';
      loadJobs();
    })
    .catch(function (e) {
      showError(e.message);
    });
});

Api.getProject(projectId)
  .then(function (p) {
    document.getElementById('project-name').textContent = p.name;
  })
  .catch(function () {});

loadJobs();
