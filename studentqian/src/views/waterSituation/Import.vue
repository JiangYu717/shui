<template>
  <div class="import-page">
    <div class="page-header">
      <h2>水情数据导入</h2>
    </div>
    <el-card class="import-card">
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
      </div>
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
          <el-table-column prop="importTime" label="导入时间" />
          <el-table-column prop="status" label="状态">
            <template #default="scope">
              <el-tag :type="scope.row.status === '成功' ? 'success' : 'danger'">
                {{ scope.row.status }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="message" label="备注" />
        </el-table>
      </div>
    </el-card>
  </div>
</template>
<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { UploadFilled, Download } from '@element-plus/icons-vue'
import { importWaterSituation } from '@/api/waterSituation'
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
const handleSuccess = (response, file) => {
  ElMessage.success('文件上传成功')
  importHistory.value.unshift({
    fileName: file.name || '未知文件',
    importTime: new Date().toLocaleString(),
    status: '成功',
    message: response?.message || '导入成功'
  })
}
const handleError = (error, file) => {
  const errorMessage = error.response?.data?.message || error.message || '未知错误'
  ElMessage.error('文件上传失败：' + errorMessage)
  importHistory.value.unshift({
    fileName: file?.name || '未知文件',
    importTime: new Date().toLocaleString(),
    status: '失败',
    message: errorMessage
  })
}
const customUpload = async ({ file }) => {
  try {
    await importWaterSituation(file)
    handleSuccess({}, file)
  } catch (error) {
    handleError(error, file)
  }
}
const downloadTemplate = () => {
  const apiUrl = 'http://localhost:8080'
  const link = document.createElement('a')
  link.href = `${apiUrl}/api/waterSituation/template/download`
  link.download = '水情数据导入模板.xlsx'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
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
</style> 