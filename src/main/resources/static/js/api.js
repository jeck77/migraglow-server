const API_BASE = '/api';

async function apiRequest(path, options) {
  const res = await fetch(API_BASE + path, {
    headers: { 'Content-Type': 'application/json' },
    ...options,
  });
  if (!res.ok) {
    const text = await res.text();
    throw new Error(text || ('요청이 실패했습니다 (' + res.status + ')'));
  }
  if (res.status === 204) {
    return undefined;
  }
  return res.json();
}

const Api = {
  listProjects: () => apiRequest('/projects'),
  createProject: (body) => apiRequest('/projects', { method: 'POST', body: JSON.stringify(body) }),
  getProject: (id) => apiRequest('/projects/' + id),
  updateProject: (id, body) => apiRequest('/projects/' + id, { method: 'PUT', body: JSON.stringify(body) }),

  listMigrationJobs: (projectId) => apiRequest('/projects/' + projectId + '/migration-jobs'),
  getJob: (jobId) => apiRequest('/migration-jobs/' + jobId),
  createMigrationJob: (projectId, body) =>
    apiRequest('/projects/' + projectId + '/migration-jobs', {
      method: 'POST',
      body: JSON.stringify(body),
    }),
  submitMigrationJob: (jobId, actorId) =>
    apiRequest('/migration-jobs/' + jobId + '/submit', {
      method: 'POST',
      body: JSON.stringify({ actorId }),
    }),
  approveMigrationJob: (jobId, actorId) =>
    apiRequest('/migration-jobs/' + jobId + '/approve', {
      method: 'POST',
      body: JSON.stringify({ actorId }),
    }),
  rejectMigrationJob: (jobId, actorId, reason) =>
    apiRequest('/migration-jobs/' + jobId + '/reject', {
      method: 'POST',
      body: JSON.stringify({ actorId, reason }),
    }),
  getApprovalHistory: (jobId) => apiRequest('/migration-jobs/' + jobId + '/approval-history'),

  listSchemaTables: (connection) =>
    apiRequest('/migration-schema/tables', { method: 'POST', body: JSON.stringify(connection) }),
  listSchemaColumns: (connection, tableName) =>
    apiRequest('/migration-schema/columns', {
      method: 'POST',
      body: JSON.stringify({ connection: connection, tableName: tableName }),
    }),

  listProjectSourceTables: (projectId) => apiRequest('/projects/' + projectId + '/source-tables'),
  listProjectTargetTables: (projectId) => apiRequest('/projects/' + projectId + '/target-tables'),
  listProjectSourceColumns: (projectId, tableName) =>
    apiRequest('/projects/' + projectId + '/source-columns?tableName=' + encodeURIComponent(tableName)),
  listProjectTargetColumns: (projectId, tableName) =>
    apiRequest('/projects/' + projectId + '/target-columns?tableName=' + encodeURIComponent(tableName)),

  getSourceColumns: (jobId) => apiRequest('/migration-jobs/' + jobId + '/source-columns'),
  getTargetColumns: (jobId) => apiRequest('/migration-jobs/' + jobId + '/target-columns'),

  listMappingRules: (targetEntityName) =>
    apiRequest('/mapping-rules?targetEntityName=' + encodeURIComponent(targetEntityName)),
  createMappingRule: (body) => apiRequest('/mapping-rules', { method: 'POST', body: JSON.stringify(body) }),
  deactivateMappingRule: (ruleId) => apiRequest('/mapping-rules/' + ruleId + '/deactivate', { method: 'POST' }),
};
