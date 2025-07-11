import request from '@/utils/request'

export function fetchSectionMonitorList(query) {
  return request({
    url: '/api/sectionMonitor/list',
    method: 'get',
    params: query
  })
}

export function importSectionMonitor(data) {
  return request({
    url: '/api/sectionMonitor/import',
    method: 'post',
    data,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function exportSectionMonitor(format = 'xlsx') {
  return request({
    url: '/api/sectionMonitor/export',
    method: 'post',
    responseType: 'blob',
    params: { format }
  })
}

export function deleteSectionMonitor(id) {
  return request({
    url: `/api/sectionMonitor/delete/${id}`,
    method: 'delete'
  })
}

export function downloadSectionMonitorTemplate() {
  return request({
    url: '/api/sectionMonitor/template',
    method: 'get',
    responseType: 'blob'
  })
} 