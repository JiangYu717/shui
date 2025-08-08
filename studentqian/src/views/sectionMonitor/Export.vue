<template>
  <div class="section-monitor-export">
    <el-card>
      <div class="export-title">监测断面数据导出</div>
      <el-form :inline="true" :model="form" class="export-form">
        <el-form-item label="导出内容">
          <el-checkbox-group v-model="form.contents">
            <el-checkbox label="基本信息" />
            <el-checkbox label="水质数据" />
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="form.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="yyyy-MM-dd"
            style="width: 260px;"
          />
        </el-form-item>
        <el-form-item label="文件格式">
          <el-radio-group v-model="form.format">
            <el-radio label="xlsx">Excel (xlsx)</el-radio>
            <el-radio label="csv">CSV</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleExport">导出</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import { exportSectionMonitor } from '@/api/sectionMonitor'
import { ElMessage } from 'element-plus'
export default {
  name: 'SectionMonitorExport',
  data() {
    return {
      form: {
        contents: ['基本信息', '水质数据'],
        dateRange: [],
        format: 'xlsx'
      }
    }
  },
  methods: {
    handleExport() {
      // 可根据form参数传递给后端，当前仅实现格式选择
      exportSectionMonitor({ format: this.form.format }).then(res => {
        const ext = this.form.format === 'csv' ? 'csv' : 'xlsx'
        const blob = res.data
        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.download = `section_monitor_export.${ext}`
        link.click()
        window.URL.revokeObjectURL(url)
      }).catch(error => {
        console.error('导出失败:', error)
        ElMessage.error('导出失败：' + (error.message || '未知错误'))
      })
    }
  }
}
</script>

<style scoped>
.section-monitor-export {
  padding: 20px;
}
.export-title {
  font-size: 20px;
  font-weight: bold;
  margin-bottom: 20px;
}
.export-form {
  margin-top: 20px;
}
</style> 