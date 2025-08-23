<template>
  <div class="export-page">
    <div class="page-header">
      <h2>数据导出</h2>
    </div>

    <el-card class="export-card">
      <!-- 导出条件表单 -->
      <el-form :model="exportForm" label-width="100px" class="export-form">
        <el-form-item label="导出内容">
          <el-checkbox-group v-model="exportForm.content">
            <el-checkbox value="basic" border>基本信息</el-checkbox>
            <el-checkbox value="quality" border>水质数据</el-checkbox>
            <el-checkbox value="risk" border>风险评估</el-checkbox>
          </el-checkbox-group>
        </el-form-item>

        <el-form-item label="时间范围">
          <el-date-picker
            v-model="exportForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>

        <el-form-item label="水库选择">
          <el-select
            v-model="exportForm.reservoirs"
            multiple
            filterable
            placeholder="请选择水库"
            style="width: 100%"
          >
            <el-option
              v-for="item in reservoirOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="文件格式">
          <el-radio-group v-model="exportForm.format">
            <el-radio value="xlsx">Excel (xlsx)</el-radio>
            <el-radio value="csv">CSV</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleExport" :loading="exporting">
            <el-icon><download /></el-icon>
            开始导出
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 导出历史记录 -->
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
          <el-table-column label="操作" width="100">
            <template #default="scope">
              <el-button
                type="primary"
                link
                :disabled="scope.row.status !== '成功'"
                @click="downloadFile(scope.row)"
              >
                下载
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Download } from '@element-plus/icons-vue'
import { exportReservoir, getReservoirList } from '@/api/reservoir'

export default {
  name: 'ReservoirExport',
  setup() {
    // 导出表单数据
    const exportForm = reactive({
      content: ['basic'],
      dateRange: [],
      reservoirs: [],
      format: 'xlsx'
    })

    // 水库选项
    const reservoirOptions = ref([
      { value: 'all', label: '全部水库' }
    ])

    // 加载水库列表
    const loadReservoirs = async () => {
      try {
        const res = await getReservoirList()
        if (res && res.data && res.data.list) {
          const options = res.data.list.map(item => ({
            value: item.reservoir_id,
            label: item.reservoir_name
          }))
          reservoirOptions.value = [{ value: 'all', label: '全部水库' }, ...options]
        } else {
          console.error('获取水库列表响应格式错误:', res)
          ElMessage.error('获取水库列表失败：响应格式错误')
        }
      } catch (error) {
        console.error('获取水库列表失败:', error)
        ElMessage.error('获取水库列表失败：' + (error.message || '未知错误'))
      }
    }

    // 导出状态
    const exporting = ref(false)

    // 导出历史记录
    const exportHistory = ref([])

    // 处理导出
    const handleExport = async () => {
      if (exportForm.content.length === 0) {
        ElMessage.warning('请至少选择一项导出内容')
        return
      }

      try {
        exporting.value = true
        const params = {
          content: exportForm.content.join(','),
          startDate: exportForm.dateRange[0],
          endDate: exportForm.dateRange[1],
          reservoirs: exportForm.reservoirs.join(','),
          format: exportForm.format
        }
        
        const response = await exportReservoir(params)

        // 从响应头中获取文件名
        const contentDisposition = response.headers['content-disposition']
        let fileName = `水库数据导出_${new Date().toLocaleDateString().replace(/\//g, '-')}.${exportForm.format}`
        if (contentDisposition) {
          const fileNameMatch = contentDisposition.match(/filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/)
          if (fileNameMatch && fileNameMatch[1]) {
            fileName = fileNameMatch[1].replace(/['"]/g, '')
          }
        }

        // 处理文件下载
        const blob = new Blob([response.data], { 
          type: response.headers['content-type'] || 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' 
        })
        const link = document.createElement('a')
        link.href = window.URL.createObjectURL(blob)
        link.download = fileName
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        window.URL.revokeObjectURL(link.href)

        // 更新导出历史
        exportHistory.value.unshift({
          fileName,
          exportTime: new Date().toLocaleString(),
          status: '成功',
          blob
        })
        
        ElMessage.success('导出成功')
      } catch (error) {
        console.error('导出失败:', error)
        ElMessage.error('导出失败：' + (error.message || '未知错误'))
        
        // 更新导出历史
        exportHistory.value.unshift({
          fileName: `导出失败_${new Date().toLocaleDateString().replace(/\//g, '-')}.${exportForm.format}`,
          exportTime: new Date().toLocaleString(),
          status: '失败'
        })
      } finally {
        exporting.value = false
      }
    }

    // 下载文件
    const downloadFile = (file) => {
      if (file.blob) {
        const link = document.createElement('a')
        link.href = window.URL.createObjectURL(file.blob)
        link.download = file.fileName
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
        window.URL.revokeObjectURL(link.href)
        ElMessage.success('开始下载文件：' + file.fileName)
      } else {
        ElMessage.error('文件不存在或已过期')
      }
    }

    // 初始化
    onMounted(() => {
      loadReservoirs()
    })

    return {
      exportForm,
      reservoirOptions,
      exporting,
      exportHistory,
      handleExport,
      downloadFile,
      Download
    }
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
  max-width: 600px;
  margin: 0 auto;
}

.export-history {
  margin-top: 30px;
}

.export-history h3 {
  margin-bottom: 15px;
}

:deep(.el-checkbox.is-bordered) {
  margin-right: 10px;
}

:deep(.el-date-editor) {
  width: 100%;
}
</style> 