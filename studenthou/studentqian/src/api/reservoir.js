import request from '@/utils/request'

// 状态映射
const statusMap = {
  '正常': 'normal',
  '维护中': 'maintenance',
  '风险': 'risk'
}

// 获取水库列表
export function getReservoirList(params) {
  console.log('发送请求参数:', params)
  return request({
    url: '/api/reservoirs',
    method: 'get',
    params
  })
}

// 添加水库
export function addReservoir(data) {
  // Convert date strings to ISO format
  if (data.constructionDate) {
    data.constructionDate = data.constructionDate.split('T')[0]
  }
  if (data.lastMaintenanceDate) {
    data.lastMaintenanceDate = data.lastMaintenanceDate.split('T')[0]
  }
  
  // Convert status to English
  if (data.status && statusMap[data.status]) {
    data.status = statusMap[data.status]
  }
  
  return request({
    url: '/api/reservoirs',
    method: 'post',
    data
  })
}

// 更新水库
export function updateReservoir(id, data) {
  // Convert date strings to ISO format
  if (data.constructionDate) {
    data.constructionDate = data.constructionDate.split('T')[0]
  }
  if (data.lastMaintenanceDate) {
    data.lastMaintenanceDate = data.lastMaintenanceDate.split('T')[0]
  }
  
  // Convert status to English
  if (data.status && statusMap[data.status]) {
    data.status = statusMap[data.status]
  }
  
  return request({
    url: `/api/reservoirs/${id}`,
    method: 'put',
    data
  })
}

// 删除水库
export function deleteReservoir(id) {
  return request({
    url: `/api/reservoirs/${id}`,
    method: 'delete'
  })
}

// 获取单个水库详情
export function getReservoirById(id) {
  return request({
    url: `/api/reservoirs/${id}`,
    method: 'get'
  })
}

// 导入水库数据
export function importReservoir(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/api/reservoirs/import',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 导出水库数据
export function exportReservoir(params) {
  return request({
    url: '/api/reservoirs/export',
    method: 'get',
    params,
    responseType: 'blob',
    headers: {
      'Accept': params.format === 'xlsx' ? 
        'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' : 
        'text/csv'
    }
  })
}

export function downloadTemplate() {
  return request({
    url: '/api/reservoirs/template/download',
    method: 'get',
    responseType: 'blob'
  })
} 