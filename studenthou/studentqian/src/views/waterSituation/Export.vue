<template>
  <div class="export-page">
    <div class="page-header">
      <h2>水情数据导出</h2>
    </div>
    <el-card class="export-card">
      <el-form :inline="false" class="export-form">
        <el-form-item label="导出内容">
          <el-checkbox-group v-model="exportContent">
            <el-checkbox label="基本信息" />
            <el-checkbox label="水质数据" />
            <el-checkbox label="风险评估" />
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" style="width: 300px;" />
        </el-form-item>
        <el-form-item label="水库选择">
          <el-select v-model="reservoir" placeholder="请选择水库" style="width: 200px;">
            <el-option label="全部水库" value="all" />
          </el-select>
        </el-form-item>
        <el-form-item label="文件格式">
          <el-radio-group v-model="format">
            <el-radio label="excel">Excel (xlsx)</el-radio>
            <el-radio label="csv">CSV</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleExport">导出</el-button>
        </el-form-item>
      </el-form>
      <div class="export-history" v-if="exportHistory.length > 0">
        <h3>导出历史</h3>
        <el-table :data="exportHistory" border style="width: 100%">
          <el-table-column prop="fileName" label="文件名称" />
          <el-table-column prop="exportTime" label="导出时间" />
          <el-table-column prop="status" label="状态">
            <template #default="scope">
              <el-tag :type="scope.row.status === '成功' ? 'success' : 'danger'">
                {{ scope.row.status }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作">
            <template #default="scope">
              <el-link type="primary" @click="downloadFile(scope.row)">下载</el-link>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>
  </div>
</template>
<script setup>
import { ref } from 'vue'
import { exportWaterSituation } from '@/api/waterSituation'
import { ElMessage } from 'element-plus'
const format = ref('excel')
const exportContent = ref(['基本信息', '水质数据', '风险评估'])
const dateRange = ref([])
const reservoir = ref('all')
const exportHistory = ref([])
function handleExport() {
  exportWaterSituation(format.value).then(res => {
    const blob = new Blob([res.data], { type: res.headers['content-type'] })
    const now = new Date()
    const fileName = `水情数据导出_${now.getFullYear()}-${now.getMonth() + 1}-${now.getDate()}.${format.value === 'csv' ? 'csv' : 'xlsx'}`
    saveFile(blob, fileName)
    exportHistory.value.unshift({
      fileName,
      exportTime: now.toLocaleString(),
      status: '成功',
      blob
    })
    ElMessage.success('导出成功')
  }).catch(() => {
    const now = new Date()
    exportHistory.value.unshift({
      fileName: '导出失败',
      exportTime: now.toLocaleString(),
      status: '失败',
      blob: null
    })
  })
}
function saveFile(blob, fileName) {
  const url = window.URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = fileName
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  window.URL.revokeObjectURL(url)
}
function downloadFile(row) {
  if (row.blob) {
    saveFile(row.blob, row.fileName)
  } else {
    ElMessage.error('文件不存在')
  }
}
</script>
<style scoped>
.export-page {
  padding: 20px;
}
.page-header {
  margin-bottom: 20px;
}
.export-card {
  margin-bottom: 20px;
}
.export-form {
  max-width: 700px;
  margin: 0 auto 20px auto;
  padding: 30px 0 10px 0;
  display: flex;
  flex-wrap: wrap;
  background: #f9f9f9;
  border-radius: 8px;
  box-shadow: 0 2px 8px #f0f1f2;
}
.export-form .el-form-item {
  min-width: 260px;
  margin-right: 32px;
  margin-bottom: 18px;
}
.export-form .el-form-item:last-child {
  margin-right: 0;
}
.export-history {
  margin-top: 30px;
}
.export-history h3 {
  margin-bottom: 15px;
}
</style> 