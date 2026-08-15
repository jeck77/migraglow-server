const jobId = window.JOB_ID;

let targetEntityName = null;
let sourceColumns = [];
let targetColumns = [];
let rulesByField = {};
const valueMapState = {};

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

var RULE_TYPE_HINTS = {
  '': '매핑을 저장하지 않으면 이 TO-BE 컬럼은 채워지지 않습니다.',
  DIRECT: 'AS-IS 컬럼 값을 보정 없이 그대로 사용합니다.',
  VALUE_MAP: '지정한 값 쌍 기준으로 AS-IS 값을 변환합니다. 기준에 없는 값은 AS-IS 값 그대로 들어갑니다.',
  FIXED_VALUE: 'AS-IS 값과 무관하게 항상 이 값으로 채웁니다.',
};

function formatColumnMeta(c) {
  const parts = [c.dataType];
  if (c.primaryKey) parts.push('PK');
  if (!c.nullable) parts.push('NOT NULL');
  if (c.defaultValue !== null && c.defaultValue !== undefined) parts.push('DEFAULT ' + c.defaultValue);
  if (c.foreignKey) parts.push('FK → ' + c.referencedTable + '.' + c.referencedColumn);
  return parts.join(' · ');
}

function renderColumnOptions(columns, selected) {
  return (
    '<option value="">-- AS-IS 컬럼 선택 --</option>' +
    columns
      .map(function (c) {
        return (
          '<option value="' + escapeHtml(c.columnName) + '"' + (c.columnName === selected ? ' selected' : '') + '>' +
          escapeHtml(c.columnName) + ' (' + escapeHtml(formatColumnMeta(c)) + ')</option>'
        );
      })
      .join('')
  );
}

function renderValueMapEditor(field) {
  const pairs = valueMapState[field] || [];
  const rows = pairs
    .map(function (p, idx) {
      return (
        '<div class="value-map-row" data-idx="' + idx + '">' +
        '<input type="text" class="vm-source" value="' + escapeHtml(p.sourceValue) + '" placeholder="AS-IS 값">' +
        '<span>→</span>' +
        '<input type="text" class="vm-target" value="' + escapeHtml(p.targetValue) + '" placeholder="TO-BE 값">' +
        '<button type="button" class="link vm-remove" data-idx="' + idx + '">삭제</button>' +
        '</div>'
      );
    })
    .join('');
  return rows + '<button type="button" class="secondary vm-add">값 추가</button>';
}

function renderRow(column) {
  const field = column.columnName;

  if (column.primaryKey) {
    return (
      '<div class="mapping-row mapping-row-pk" data-field="' + escapeHtml(field) + '">' +
      '<div class="mapping-row-head"><span class="mapping-side-label">TO-BE 컬럼</span><strong>' + escapeHtml(field) + '</strong> <span class="mapping-type">' + escapeHtml(formatColumnMeta(column)) + '</span></div>' +
      '<div class="mapping-pk-note">기본키(PK) · 시퀀스/auto_increment로 채번되어 매핑할 수 없습니다</div>' +
      '</div>'
    );
  }

  const rule = rulesByField[field];
  const ruleType = rule ? rule.ruleType : '';
  if (rule && rule.ruleType === 'VALUE_MAP' && !valueMapState[field]) {
    valueMapState[field] = rule.valueMap.map(function (v) {
      return { sourceValue: v.sourceValue, targetValue: v.targetValue };
    });
  }

  return (
    '<div class="mapping-row" data-field="' + escapeHtml(field) + '">' +
    '<div class="mapping-row-head"><span class="mapping-side-label">TO-BE 컬럼</span><strong>' + escapeHtml(field) + '</strong> <span class="mapping-type">' + escapeHtml(formatColumnMeta(column)) + '</span></div>' +
    '<div class="form-row">' +
    '<select class="map-rule-type">' +
    '<option value=""' + (ruleType === '' ? ' selected' : '') + '>매핑 안함</option>' +
    '<option value="DIRECT"' + (ruleType === 'DIRECT' ? ' selected' : '') + '>AS-IS 컬럼 그대로</option>' +
    '<option value="VALUE_MAP"' + (ruleType === 'VALUE_MAP' ? ' selected' : '') + '>AS-IS 값 변환</option>' +
    '<option value="FIXED_VALUE"' + (ruleType === 'FIXED_VALUE' ? ' selected' : '') + '>고정값</option>' +
    '</select>' +
    '<span class="mapping-side-label map-source-field-label" style="display:' + (ruleType === 'DIRECT' || ruleType === 'VALUE_MAP' ? 'inline' : 'none') + ';">← AS-IS 컬럼</span>' +
    '<select class="map-source-field" style="display:' + (ruleType === 'DIRECT' || ruleType === 'VALUE_MAP' ? 'inline-block' : 'none') + ';">' +
    renderColumnOptions(sourceColumns, rule ? rule.sourceFieldName : null) +
    '</select>' +
    '<span class="mapping-side-label map-fixed-value-label" style="display:' + (ruleType === 'FIXED_VALUE' ? 'inline' : 'none') + ';">고정값</span>' +
    '<input type="text" class="map-fixed-value" placeholder="예: ACTIVE" value="' +
    (rule && rule.ruleType === 'FIXED_VALUE' ? escapeHtml(rule.expression) : '') +
    '" style="display:' + (ruleType === 'FIXED_VALUE' ? 'inline-block' : 'none') + ';">' +
    '</div>' +
    '<div class="mapping-hint">' + RULE_TYPE_HINTS[ruleType] + '</div>' +
    '<div class="map-value-list" style="display:' + (ruleType === 'VALUE_MAP' ? 'block' : 'none') + ';">' +
    renderValueMapEditor(field) +
    '</div>' +
    '<div class="form-row">' +
    '<button type="button" class="secondary map-save">저장</button>' +
    (rule ? '<button type="button" class="danger map-remove" data-rule-id="' + rule.id + '">매핑 삭제</button>' : '') +
    '<span class="map-status"></span>' +
    '</div>' +
    '</div>'
  );
}

function renderAll() {
  document.getElementById('mapping-list').innerHTML = targetColumns.map(renderRow).join('');
}

function loadAll() {
  showError('');
  Promise.all([Api.getJob(jobId), Api.getSourceColumns(jobId), Api.getTargetColumns(jobId)])
    .then(function (results) {
      const job = results[0];
      sourceColumns = results[1];
      targetColumns = results[2];
      targetEntityName = job.targetEntityName;
      document.getElementById('job-name').textContent = job.name;
      document.getElementById('target-entity-name').textContent = targetEntityName;
      document.getElementById('source-table-name').textContent = job.sourceTableName || '(선택 안 됨)';
      document.getElementById('target-table-name').textContent = job.targetTableName || '(선택 안 됨)';
      return Api.listMappingRules(targetEntityName);
    })
    .then(function (rules) {
      rulesByField = {};
      rules.forEach(function (r) {
        rulesByField[r.targetFieldName] = r;
      });
      renderAll();
    })
    .catch(function (e) {
      showError(e.message);
    });
}

function saveRow(row, field) {
  const statusEl = row.querySelector('.map-status');
  const ruleType = row.querySelector('.map-rule-type').value;
  statusEl.textContent = '';
  statusEl.style.color = '';

  if (!ruleType) {
    statusEl.style.color = 'var(--color-danger)';
    statusEl.textContent = '매핑 유형을 선택해주세요.';
    return;
  }

  const request = {
    targetEntityName: targetEntityName,
    targetFieldName: field,
    ruleType: ruleType,
    sourceFieldName: null,
    expression: null,
    valueMap: null,
  };

  if (ruleType === 'DIRECT' || ruleType === 'VALUE_MAP') {
    const sourceField = row.querySelector('.map-source-field').value;
    if (!sourceField) {
      statusEl.style.color = 'var(--color-danger)';
      statusEl.textContent = 'AS-IS 컬럼을 선택해주세요.';
      return;
    }
    request.sourceFieldName = sourceField;
  }

  if (ruleType === 'FIXED_VALUE') {
    const fixedValue = row.querySelector('.map-fixed-value').value.trim();
    if (!fixedValue) {
      statusEl.style.color = 'var(--color-danger)';
      statusEl.textContent = '고정값을 입력해주세요.';
      return;
    }
    request.expression = fixedValue;
  }

  if (ruleType === 'VALUE_MAP') {
    const pairs = (valueMapState[field] || []).filter(function (p) {
      return p.sourceValue.trim() && p.targetValue.trim();
    });
    if (pairs.length === 0) {
      statusEl.style.color = 'var(--color-danger)';
      statusEl.textContent = '값 매핑을 한 개 이상 입력해주세요.';
      return;
    }
    request.valueMap = pairs;
  }

  statusEl.textContent = '저장 중...';
  Api.createMappingRule(request)
    .then(function () {
      loadAll();
    })
    .catch(function (e) {
      statusEl.style.color = 'var(--color-danger)';
      statusEl.textContent = e.message;
    });
}

document.getElementById('mapping-list').addEventListener('change', function (e) {
  const row = e.target.closest('.mapping-row');
  if (!row || !e.target.classList.contains('map-rule-type')) return;
  const field = row.getAttribute('data-field');
  const type = e.target.value;
  const showSource = type === 'DIRECT' || type === 'VALUE_MAP';
  row.querySelector('.map-source-field').style.display = showSource ? 'inline-block' : 'none';
  row.querySelector('.map-source-field-label').style.display = showSource ? 'inline' : 'none';
  row.querySelector('.map-fixed-value').style.display = type === 'FIXED_VALUE' ? 'inline-block' : 'none';
  row.querySelector('.map-fixed-value-label').style.display = type === 'FIXED_VALUE' ? 'inline' : 'none';
  row.querySelector('.map-value-list').style.display = type === 'VALUE_MAP' ? 'block' : 'none';
  row.querySelector('.mapping-hint').textContent = RULE_TYPE_HINTS[type];
  if (type === 'VALUE_MAP' && !valueMapState[field]) {
    valueMapState[field] = [{ sourceValue: '', targetValue: '' }];
    row.querySelector('.map-value-list').innerHTML = renderValueMapEditor(field);
  }
});

document.getElementById('mapping-list').addEventListener('input', function (e) {
  const row = e.target.closest('.mapping-row');
  if (!row) return;
  const field = row.getAttribute('data-field');
  const pairRow = e.target.closest('.value-map-row');
  if (!pairRow || !valueMapState[field]) return;
  const idx = Number(pairRow.getAttribute('data-idx'));
  const pair = valueMapState[field][idx];
  if (!pair) return;
  if (e.target.classList.contains('vm-source')) {
    pair.sourceValue = e.target.value;
  } else if (e.target.classList.contains('vm-target')) {
    pair.targetValue = e.target.value;
  }
});

document.getElementById('mapping-list').addEventListener('click', function (e) {
  const row = e.target.closest('.mapping-row');
  if (!row) return;
  const field = row.getAttribute('data-field');

  if (e.target.classList.contains('vm-add')) {
    valueMapState[field] = valueMapState[field] || [];
    valueMapState[field].push({ sourceValue: '', targetValue: '' });
    row.querySelector('.map-value-list').innerHTML = renderValueMapEditor(field);
    return;
  }
  if (e.target.classList.contains('vm-remove')) {
    const idx = Number(e.target.getAttribute('data-idx'));
    valueMapState[field].splice(idx, 1);
    row.querySelector('.map-value-list').innerHTML = renderValueMapEditor(field);
    return;
  }
  if (e.target.classList.contains('map-save')) {
    saveRow(row, field);
    return;
  }
  if (e.target.classList.contains('map-remove')) {
    const ruleId = e.target.getAttribute('data-rule-id');
    showError('');
    Api.deactivateMappingRule(ruleId)
      .then(loadAll)
      .catch(function (e) {
        showError(e.message);
      });
  }
});

loadAll();
