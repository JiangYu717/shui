<template>
  <div class="section-monitor-import">
    <el-card>
      <template #header>
        <span>导入监测断面数据</span>
        <el-button style="float: right;" @click="goBack">返回</el-button>
      </template>
      <el-upload
        class="upload-demo"
        drag
        action=""
        :http-request="uploadFile"
        :show-file-list="false"
        accept=".xlsx,.xls,.csv"
      >
        <i class="el-icon-upload"></i>
        <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
        <template #tip>
          <div class="el-upload__tip">仅支持xlsx/csv格式，模板请先下载</div>
        </template>
      </el-upload>
      <el-button type="primary" @click="downloadTemplate" style="margin-top: 20px;">下载模板</el-button>
    </el-card>
  </div>
</template>

<script>
import { importSectionMonitor, downloadSectionMonitorTemplate } from '@/api/sectionMonitor'
export default {
  name: 'SectionMonitorImport',
  methods: {
    uploadFile(param) {
      const formData = new FormData()
      formData.append('file', param.file)
      importSectionMonitor(formData).then(() => {
        this.$message.success('导入成功')
        this.$router.push({ name: 'SectionMonitorList' })
      }).catch(err => {
        this.$message.error('导入失败: ' + (err.response?.data || err.message))
      })
    },
    downloadTemplate() {
      downloadSectionMonitorTemplate().then(res => {
        const blob = res.data;
        if (blob.type && blob.type.indexOf('sheet') === -1) {
          // 不是Excel，可能是错误信息
          const reader = new FileReader()
          reader.onload = function() {
            alert('下载失败：' + reader.result)
          }
          reader.readAsText(blob)
          return
        }
        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.download = 'section_monitor_template.xlsx'
        link.click()
        window.URL.revokeObjectURL(url)
      })
    },
    goBack() {
      this.$router.back()
    }
  }
}
</script>

<style scoped>
.section-monitor-import {
  padding: 20px;
}
</style> 