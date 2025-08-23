<template>
  <div class="import-page">
    <div class="page-header">
      <h2>水情数据导入</h2>
    </div>
    <el-card class="import-card">
      <el-tabs v-model="activeTab" type="border-card">
        <el-tab-pane label="普通导入" name="normal">
          <div class="upload-area">
            <el-upload
              class="upload-demo"
              drag
              :http-request="customUpload"
              :before-upload="beforeUpload"
              accept=".xlsx,.xls,.csv"
            >
              <el-icon class="el-icon--upload"><upload-filled /></el-icon>
              <div class="el-upload__text">
                将文件拖到此处，或<em>点击上传</em>
              </div>
              <template #tip>
                <div class="el-upload__tip">
                  支持 .xlsx, .xls, .csv 格式文件，文件大小不超过 10MB
                </div>
              </template>
            </el-upload>
            <div class="import-info">
              <el-alert
                title="普通导入说明"
                type="info"
                :closable="false"
                show-icon
              >
                <p>• 普通导入会检查所有数据的完整性</p>
                <p>• 如果存在重复库名，整个导入会失败</p>
                <p>• 建议先使用批量导入功能</p>
              </el-alert>
            </div>
          </div>
        </el-tab-pane>
        
        <el-tab-pane label="批量导入" name="batch">
          <div class="upload-area">
            <el-upload
              class="upload-demo"
              drag
              :http-request="batchUpload"
              :before-upload="beforeUpload"
              accept=".xlsx,.xls,.csv"
            >
              <el-icon class="el-icon--upload"><upload-filled /></el-icon>
              <div class="el-upload__text">
                将文件拖到此处，或<em>点击上传</em>
              </div>
              <template #tip>
                <div class="el-upload__tip">
                  支持 .xlsx, .xls, .csv 格式文件，文件大小不超过 10MB
                </div>
              </template>
            </el-upload>
            <div class="import-info">
              <el-alert
                title="批量导入说明"
                type="success"
                :closable="false"
                show-icon
              >
                <p>• 批量导入会自动跳过重复的库名</p>
                <p>• 只导入不重复的数据，提高导入成功率</p>
                <p>• 导入完成后会显示跳过的重复库名</p>
              </el-alert>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
      
      <div class="template-download">
        <h3>模板下载</h3>
        <p>请下载并使用标准模板文件进行数据填写：</p>
        <el-button type="primary" @click="downloadTemplate">
          <el-icon><download /></el-icon>
          下载导入模板
        </el-button>
      </div>
      
      <div class="import-history" v-if="importHistory.length > 0">
        <h3>导入历史</h3>
        <el-table :data="importHistory" border style="width: 100%">
          <el-table-column prop="fileName" label="文件名称" />
          <el-table-column prop="importType" label="导入类型" />
          <el-table-column prop="importTime" label="导入时间" />
          <el-table-column prop="status" label="状态">
            <template #default="scope">
              <el-tag :type="scope.row.status === '成功' ? 'success' : 'danger'">
                {{ scope.row.status }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="message" label="备注" show-overflow-tooltip />
        </el-table>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { UploadFilled, Download } from '@element-plus/icons-vue'
import { importWaterSituation, batchImportWaterSituation, downloadTemplate as downloadTemplateApi } from '@/api/waterSituation'

const activeTab = ref('batch') // 默认选择批量导入
const importHistory = ref([])

const beforeUpload = (file) => {
  const isValidFormat = /\.(xlsx|xls|csv)$/.test(file.name.toLowerCase())
  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isValidFormat) {
    ElMessage.error('只能上传 .xlsx, .xls, .csv 格式的文件!')
    return false
  }
  if (!isLt10M) {
    ElMessage.error('文件大小不能超过 10MB!')
    return false
  }
  return true
}

const handleSuccess = (response, file, importType) => {
  let message = '导入成功'
  let status = '成功'
  
  if (response?.data) {
    // 新的 ImportResult 格式
    const result = response.data
    message = result.message || '导入成功'
    if (result.skippedReservoirs && result.skippedReservoirs.length > 0) {
      status = '部分成功'
      message += `，跳过 ${result.skippedReservoirs.length} 个重复库名`
    }
  } else {
    // 旧的格式
    message = response?.message || '导入成功'
  }
  
  ElMessage.success(message)
  importHistory.value.unshift({
    fileName: file.name || '未知文件',
    importType: importType,
    importTime: new Date().toLocaleString(),
    status: status,
    message: message
  })
}

const handleError = (error, file, importType) => {
  let errorMessage = '未知错误'
  if (error.response?.data?.message) {
    errorMessage = error.response.data.message
  } else if (error.message) {
    errorMessage = error.message
  }
  
  ElMessage.error('文件上传失败：' + errorMessage)
  importHistory.value.unshift({
    fileName: file?.name || '未知文件',
    importType: importType,
    importTime: new Date().toLocaleString(),
    status: '失败',
    message: errorMessage
  })
}

const customUpload = async ({ file }) => {
  try {
    const response = await importWaterSituation(file)
    handleSuccess(response, file, '普通导入')
  } catch (error) {
    handleError(error, file, '普通导入')
  }
}

const batchUpload = async ({ file }) => {
  try {
    const response = await batchImportWaterSituation(file)
    handleSuccess(response, file, '批量导入')
  } catch (error) {
    handleError(error, file, '批量导入')
  }
}

const downloadTemplate = async () => {
  try {
    const response = await downloadTemplateApi()
    const blob = new Blob([response.data], { 
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' 
    })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = '水情数据导入模板.xlsx'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    ElMessage.success('模板下载成功')
  } catch (error) {
    ElMessage.error('模板下载失败：' + (error.message || '未知错误'))
  }
}
</script>

<style scoped>
.import-page {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.import-card {
  margin-bottom: 20px;
}

.upload-area {
  text-align: center;
  padding: 20px 0;
}

.import-info {
  margin-top: 20px;
  max-width: 600px;
  margin-left: auto;
  margin-right: auto;
}

.import-info p {
  margin: 5px 0;
  font-size: 14px;
}

.template-download {
  margin: 20px 0;
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.template-download h3 {
  margin-top: 0;
  margin-bottom: 10px;
}

.template-download p {
  margin-bottom: 15px;
  color: #606266;
}

.import-history {
  margin-top: 30px;
}

.import-history h3 {
  margin-bottom: 15px;
}

:deep(.el-upload-dragger) {
  width: 100%;
  max-width: 500px;
  margin: 0 auto;
}

:deep(.el-tabs__content) {
  padding: 20px;
}
</style> 