import request from '@/utils/request'

export function getWaterSituationList(params) {
  return request({
    url: '/api/waterSituation/list',
    method: 'get',
    params
  })
}

export function addWaterSituation(data) {
  return request({
    url: '/api/waterSituation/create',
    method: 'post',
    data
  })
}

export function updateWaterSituation(data) {
  return request({
    url: '/api/waterSituation/update',
    method: 'post',
    data
  })
}

export function deleteWaterSituation(id) {
  return request({
    url: `/api/waterSituation/delete/${id}`,
    method: 'delete'
  })
}

export function importWaterSituation(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/api/waterSituation/import',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function exportWaterSituation(format = 'xlsx') {
  return request({
    url: '/api/waterSituation/export',
    method: 'get',
    params: { format },
    responseType: 'blob'
  })
} 