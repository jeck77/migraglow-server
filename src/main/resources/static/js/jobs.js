const projectId = window.PROJECT_ID;
let actorId = 1;
let historyOpenJobId = null;
const historyCache = {};
let rejectOpenJobId = null;

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
  let actions = '';
  if (job.status === 'DRAFT' || job.status === 'REJECTED') {
    actions += '<button class="secondary" data-action="submit" data-job-id="' + job.id + '">제출</button>';
  }
  if (job.status === 'SUBMITTED') {
    actions += '<button class="secondary" data-action="approve" data-job-id="' + job.id + '">승인</button>';
    actions += '<button class="danger" data-action="toggle-reject" data-job-id="' + job.id + '">반려</button>';
  }
  actions +=
    '<button class="link" data-action="toggle-history" data-job-id="' +
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

document.getElementById('actor-id').addEventListener('change', function (e) {
  actorId = Number(e.target.value) || 1;
});

document.getElementById('create-form').addEventListener('submit', function (e) {
  e.preventDefault();
  const name = document.getElementById('name').value.trim();
  const targetEntityName = document.getElementById('target-entity-name').value.trim();
  const sourceType = document.getElementById('source-type').value;
  const sourceConfig = document.getElementById('source-config').value.trim();
  if (!name || !targetEntityName) return;
  showError('');
  Api.createMigrationJob(projectId, {
    name: name,
    targetEntityName: targetEntityName,
    sourceType: sourceType,
    sourceConfig: sourceConfig || undefined,
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
