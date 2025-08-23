import request from '@/utils/request'

export function fetchWaterSituationList(params) {
  return request({
    url: '/api/waterSituation/list',
    method: 'get',
    params
  })
}

export function createWaterSituation(data) {
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

export function getWaterSituationDetail(id) {
  return request({
    url: `/api/waterSituation/detail/${id}`,
    method: 'get'
  })
}

export function importWaterSituation(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/api/waterSituation/import',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export function batchImportWaterSituation(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/api/waterSituation/batchImport',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export function checkReservoirName(reservoirName) {
  return request({
    url: '/api/waterSituation/checkReservoirName',
    method: 'get',
    params: { reservoirName }
  })
}

export function downloadTemplate() {
  return request({
    url: '/api/waterSituation/template/download',
    method: 'get',
    responseType: 'blob'
  })
}

export function exportWaterSituation(params) {
  return request({
    url: '/api/waterSituation/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
} 