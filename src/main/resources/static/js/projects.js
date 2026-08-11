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

function renderProjects(projects) {
  const list = document.getElementById('list');
  if (projects.length === 0) {
    list.innerHTML = '<div class="empty">등록된 프로젝트가 없습니다.</div>';
    return;
  }
  list.innerHTML = projects
    .map(function (p) {
      return (
        '<div class="list-item">' +
        '<div class="list-item-top">' +
        '<a class="list-item-title" href="/projects/' + p.id + '/jobs">' + escapeHtml(p.name) + '</a>' +
        '<span class="badge status-' + p.status + '">' + p.status + '</span>' +
        '</div>' +
        (p.description ? '<div class="list-item-meta">' + escapeHtml(p.description) + '</div>' : '') +
        '</div>'
      );
    })
    .join('');
}

function loadProjects() {
  Api.listProjects()
    .then(renderProjects)
    .catch(function (e) {
      showError(e.message);
    });
}

document.getElementById('create-form').addEventListener('submit', function (e) {
  e.preventDefault();
  const name = document.getElementById('name').value.trim();
  const description = document.getElementById('description').value.trim();
  if (!name) return;
  showError('');
  Api.createProject({ name: name, description: description || undefined })
    .then(function () {
      document.getElementById('name').value = '';
      document.getElementById('description').value = '';
      loadProjects();
    })
    .catch(function (e) {
      showError(e.message);
    });
});

loadProjects();
