import request from '@/utils/request'

export function fetchSectionMonitorList(params) {
  return request({
    url: '/api/sectionMonitor/list',
    method: 'get',
    params
  })
}

export function createSectionMonitor(data) {
  return request({
    url: '/api/sectionMonitor/create',
    method: 'post',
    data
  })
}

export function updateSectionMonitor(data) {
  return request({
    url: '/api/sectionMonitor/update',
    method: 'post',
    data
  })
}

export function deleteSectionMonitor(id) {
  return request({
    url: `/api/sectionMonitor/delete/${id}`,
    method: 'delete'
  })
}

export function getSectionMonitorDetail(id) {
  return request({
    url: `/api/sectionMonitor/detail/${id}`,
    method: 'get'
  })
}

export function importSectionMonitor(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/api/sectionMonitor/import',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export function batchImportSectionMonitor(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/api/sectionMonitor/batchImport',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export function checkMonitorPointName(monitorPointName, reservoirName) {
  return request({
    url: '/api/sectionMonitor/checkMonitorPointName',
    method: 'get',
    params: { monitorPointName, reservoirName }
  })
}

export function downloadTemplate() {
  return request({
    url: '/api/sectionMonitor/template',
    method: 'get',
    responseType: 'blob'
  })
}

export function exportSectionMonitor(params) {
  return request({
    url: '/api/sectionMonitor/export',
    method: 'post',
    params,
    responseType: 'blob'
  })
} 