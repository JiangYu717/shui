<template>
  <div class="section-monitor-list">
    <el-card>
      <div class="page-header">
        <h2>监测断面管理</h2>
        <div class="header-actions">
          <el-button type="success" @click="handleImport">
            <el-icon><Upload /></el-icon>
            数据导入
          </el-button>
          <el-button type="warning" @click="handleExport">
            <el-icon><Download /></el-icon>
            数据导出
          </el-button>
          <el-button type="primary" @click="openDialog(null)">新增</el-button>
        </div>
      </div>
      <div class="search-bar">
        <el-form :inline="true" :model="search" class="demo-form-inline">
          <el-form-item label="监测点名称">
            <el-input v-model="search.monitorPointName" placeholder="请输入监测点名称" clearable />
          </el-form-item>
          <el-form-item label="水库名称">
            <el-input v-model="search.reservoirName" placeholder="请输入水库名称" clearable />
          </el-form-item>
          <el-form-item label="查询字段">
            <el-select v-model="selectedField" placeholder="请选择查询字段" @change="handleFieldChange" clearable>
              <el-option label="年份" value="year" />
              <el-option label="月份" value="month" />
              <el-option label="氧气" value="oxygen" />
              <el-option label="高锰酸盐" value="potassiumPermanganate" />
              <el-option label="化学需氧量" value="cod" />
              <el-option label="流量" value="flow" />
              <el-option label="水深" value="waterDepth" />
              <el-option label="总氮" value="totalNitrogen" />
              <el-option label="总磷" value="totalPhosphorus" />
            </el-select>
          </el-form-item>
          <el-form-item v-if="selectedField" :label="getFieldLabel(selectedField)">
            <el-input-number 
              v-model="search[selectedField + 'Min']" 
              :placeholder="'最小值'" 
              :min="getFieldMin(selectedField)"
              :max="getFieldMax(selectedField)"
              :step="getFieldStep(selectedField)"
              style="width: 120px;"
              clearable
            />
            <span style="margin: 0 8px;">-</span>
            <el-input-number 
              v-model="search[selectedField + 'Max']" 
              :placeholder="'最大值'" 
              :min="getFieldMin(selectedField)"
              :max="getFieldMax(selectedField)"
              :step="getFieldStep(selectedField)"
              style="width: 120px;"
              clearable
            />
            <span style="margin-left: 8px; color: #909399; font-size: 12px;">({{ getFieldUnit(selectedField) }})</span>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="fetchList">查询</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
      <el-table :data="list" border stripe style="width: 100%; margin-top: 20px;">
        <el-table-column prop="monitorPointName" label="监测点名称" />
        <el-table-column prop="reservoirName" label="水库名称" />
        <el-table-column prop="year" label="年份" width="80" />
        <el-table-column prop="month" label="月份" width="60" />
        <el-table-column prop="oxygen" label="氧气(mg/l)" />
        <el-table-column prop="potassiumPermanganate" label="高锰酸盐(mg/l)" />
        <el-table-column prop="cod" label="化学需氧量(mg/l)" />
        <el-table-column prop="flow" label="流量(m³/s)" />
        <el-table-column prop="waterDepth" label="水深(m)" />
        <el-table-column prop="totalNitrogen" label="总氮(mg/l)" />
        <el-table-column prop="totalPhosphorus" label="总磷(mg/l)" />
        <el-table-column label="操作" width="160">
          <template #default="scope">
            <el-button size="small" @click="openDialog(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        background
        layout="total, prev, pager, next, jumper"
        :total="total"
        :current-page="query.pageNum"
        :page-size="query.pageSize"
        @current-change="handlePageChange"
        style="margin-top: 20px; text-align: right;"
      />
    </el-card>
    
    <!-- 编辑/新增对话框 -->
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="700px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="160px">
        <el-form-item label="监测点名称" prop="monitorPointName">
          <el-input 
            v-model="form.monitorPointName" 
            placeholder="请输入监测点名称"
            @blur="checkMonitorPointNameDuplicate"
            :class="{ 'is-error': monitorPointNameError }"
          />
          <div v-if="monitorPointNameError || errorMessage" class="error-message">{{ errorMessage || '监测点名称已存在，请使用其他名称' }}</div>
        </el-form-item>
        <el-form-item label="水库名称" prop="reservoirName">
          <el-input v-model="form.reservoirName" placeholder="请输入水库名称" />
        </el-form-item>
        <el-form-item label="年份" prop="year">
          <el-input-number v-model="form.year" :min="1900" :max="2100" placeholder="请输入年份" />
        </el-form-item>
        <el-form-item label="月份" prop="month">
          <el-input-number v-model="form.month" :min="1" :max="12" placeholder="请输入月份" />
        </el-form-item>
        <el-form-item label="氧气(mg/l)" prop="oxygen">
          <el-input-number v-model="form.oxygen" :min="0" :precision="3" placeholder="请输入氧气含量" />
        </el-form-item>
        <el-form-item label="高锰酸盐(mg/l)" prop="potassiumPermanganate">
          <el-input-number v-model="form.potassiumPermanganate" :min="0" :precision="3" placeholder="请输入高锰酸盐含量" />
        </el-form-item>
        <el-form-item label="化学需氧量(mg/l)" prop="cod">
          <el-input-number v-model="form.cod" :min="0" :precision="3" placeholder="请输入化学需氧量" />
        </el-form-item>
        <el-form-item label="流量(m³/s)" prop="flow">
          <el-input-number v-model="form.flow" :min="0" :precision="3" placeholder="请输入流量" />
        </el-form-item>
        <el-form-item label="水深(m)" prop="waterDepth">
          <el-input-number v-model="form.waterDepth" :min="0" :precision="3" placeholder="请输入水深" />
        </el-form-item>
        <el-form-item label="总氮(mg/l)" prop="totalNitrogen">
          <el-input-number v-model="form.totalNitrogen" :min="0" :precision="3" placeholder="请输入总氮含量" />
        </el-form-item>
        <el-form-item label="总磷(mg/l)" prop="totalPhosphorus">
          <el-input-number v-model="form.totalPhosphorus" :min="0" :precision="3" placeholder="请输入总磷含量" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :disabled="monitorPointNameError">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { fetchSectionMonitorList, createSectionMonitor, updateSectionMonitor, deleteSectionMonitor, checkMonitorPointName } from '@/api/sectionMonitor'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Upload, Download } from '@element-plus/icons-vue'

export default {
  name: 'SectionMonitorList',
  components: {
    Upload,
    Download
  },
  data() {
    return {
      list: [],
      total: 0,
      query: {
        pageNum: 1,
        pageSize: 10
      },
      search: {
        monitorPointName: '',
        reservoirName: '',
        yearMin: null,
        yearMax: null,
        monthMin: null,
        monthMax: null,
        oxygenMin: null,
        oxygenMax: null,
        potassiumPermanganateMin: null,
        potassiumPermanganateMax: null,
        codMin: null,
        codMax: null,
        flowMin: null,
        flowMax: null,
        waterDepthMin: null,
        waterDepthMax: null,
        totalNitrogenMin: null,
        totalNitrogenMax: null,
        totalPhosphorusMin: null,
        totalPhosphorusMax: null
      },
      dialogVisible: false,
      dialogTitle: '',
      monitorPointNameError: false,
      errorMessage: '',
      originalMonitorPointName: '',
      originalReservoirName: '',
      form: {
        id: null,
        monitorPointName: '',
        reservoirName: '',
        year: '',
        month: '',
        oxygen: '',
        potassiumPermanganate: '',
        cod: '',
        flow: '',
        waterDepth: '',
        totalNitrogen: '',
        totalPhosphorus: ''
      },
      rules: {
        monitorPointName: [{ required: true, message: '请输入监测点名称', trigger: 'blur' }],
        reservoirName: [{ required: true, message: '请输入水库名称', trigger: 'blur' }],
        year: [{ required: true, message: '请输入年份', trigger: 'blur' }],
        month: [{ required: true, message: '请输入月份', trigger: 'blur' }]
      },
      selectedField: '',
    }
  },
  created() {
    this.fetchList()
  },
  methods: {
    // 检查监测点名称是否重复
    async checkMonitorPointNameDuplicate() {
      if (!this.form.monitorPointName || this.form.monitorPointName.trim() === '' || !this.form.reservoirName || this.form.reservoirName.trim() === '') {
        this.monitorPointNameError = false
        return
      }
      // 如果是编辑模式且监测点名称和水库名称都没有改变，不检查
      if (this.form.id && this.form.monitorPointName === this.originalMonitorPointName && this.form.reservoirName === this.originalReservoirName) {
        this.monitorPointNameError = false
        return
      }
      try {
        const res = await checkMonitorPointName(this.form.monitorPointName, this.form.reservoirName)
        this.monitorPointNameError = res.data // 这里才是真正的布尔值
      } catch (error) {
        console.error('检查监测点名称失败:', error)
      }
    },
    
    fetchList() {
      const params = {
        page: this.query.pageNum,
        pageSize: this.query.pageSize,
        monitorPointName: this.search.monitorPointName,
        reservoirName: this.search.reservoirName,
        yearMin: this.search.yearMin,
        yearMax: this.search.yearMax,
        monthMin: this.search.monthMin,
        monthMax: this.search.monthMax,
        oxygenMin: this.search.oxygenMin,
        oxygenMax: this.search.oxygenMax,
        potassiumPermanganateMin: this.search.potassiumPermanganateMin,
        potassiumPermanganateMax: this.search.potassiumPermanganateMax,
        codMin: this.search.codMin,
        codMax: this.search.codMax,
        flowMin: this.search.flowMin,
        flowMax: this.search.flowMax,
        waterDepthMin: this.search.waterDepthMin,
        waterDepthMax: this.search.waterDepthMax,
        totalNitrogenMin: this.search.totalNitrogenMin,
        totalNitrogenMax: this.search.totalNitrogenMax,
        totalPhosphorusMin: this.search.totalPhosphorusMin,
        totalPhosphorusMax: this.search.totalPhosphorusMax
      }
      fetchSectionMonitorList(params).then(res => {
        this.list = res.data.list
        this.total = res.data.total
      })
    },
    resetSearch() {
      this.search = {
        monitorPointName: '',
        reservoirName: '',
        yearMin: null,
        yearMax: null,
        monthMin: null,
        monthMax: null,
        oxygenMin: null,
        oxygenMax: null,
        potassiumPermanganateMin: null,
        potassiumPermanganateMax: null,
        codMin: null,
        codMax: null,
        flowMin: null,
        flowMax: null,
        waterDepthMin: null,
        waterDepthMax: null,
        totalNitrogenMin: null,
        totalNitrogenMax: null,
        totalPhosphorusMin: null,
        totalPhosphorusMax: null
      }
      this.selectedField = ''
      this.fetchList()
    },
    openDialog(row) {
      if (row) {
        this.dialogTitle = '编辑监测断面数据'
        this.form = { ...row }
        this.originalMonitorPointName = row.monitorPointName
        this.originalReservoirName = row.reservoirName
      } else {
        this.dialogTitle = '新增监测断面数据'
        this.form = {
          id: null,
          monitorPointName: '',
          reservoirName: '',
          year: '',
          month: '',
          oxygen: '',
          potassiumPermanganate: '',
          cod: '',
          flow: '',
          waterDepth: '',
          totalNitrogen: '',
          totalPhosphorus: ''
        }
        this.originalMonitorPointName = ''
        this.originalReservoirName = ''
      }
      this.monitorPointNameError = false
      this.errorMessage = ''
      this.dialogVisible = true
    },
    handleSubmit() {
      if (this.monitorPointNameError) {
        this.errorMessage = '监测点名称已存在，请使用其他名称';
        ElMessage.error(this.errorMessage)
        return
      }
      
      this.$refs.formRef.validate((valid) => {
        if (valid) {
          if (this.form.id) {
            updateSectionMonitor(this.form).then(() => {
              ElMessage.success('修改成功')
              this.dialogVisible = false
              this.fetchList()
              this.errorMessage = ''
            }).catch(error => {
              this.errorMessage = error.response?.data?.message || error.message || '修改失败'
              ElMessage.error(this.errorMessage)
            })
          } else {
            createSectionMonitor(this.form).then(() => {
              ElMessage.success('新增成功')
              this.dialogVisible = false
              this.fetchList()
              this.errorMessage = ''
            }).catch(error => {
              this.errorMessage = error.response?.data?.message || error.message || '新增失败'
              ElMessage.error(this.errorMessage)
            })
          }
        }
      })
    },
    handleDelete(row) {
      ElMessageBox.confirm('确定要删除这条数据吗？', '提示', { type: 'warning' })
        .then(() => {
          return deleteSectionMonitor(row.id)
        })
        .then(() => {
          ElMessage.success('删除成功')
          this.fetchList()
        })
    },
    handlePageChange(page) {
      this.query.pageNum = page
      this.fetchList()
    },
    // 数据导入
    handleImport() {
      // 跳转到导入页面
      this.$router.push('/sectionMonitor/import')
    },
    // 数据导出
    handleExport() {
      // 跳转到导出页面
      this.$router.push('/sectionMonitor/export')
    },
    handleFieldChange() {
      // 当选择字段变化时，清空所有字段的最小值和最大值
      const fields = ['year', 'month', 'oxygen', 'potassiumPermanganate', 'cod', 'flow', 'waterDepth', 'totalNitrogen', 'totalPhosphorus']
      fields.forEach(field => {
        this.search[field + 'Min'] = null
        this.search[field + 'Max'] = null
      })
    },
    getFieldLabel(field) {
      switch (field) {
        case 'year':
          return '年份'
        case 'month':
          return '月份'
        case 'oxygen':
          return '氧气'
        case 'potassiumPermanganate':
          return '高锰酸盐'
        case 'cod':
          return '化学需氧量'
        case 'flow':
          return '流量'
        case 'waterDepth':
          return '水深'
        case 'totalNitrogen':
          return '总氮'
        case 'totalPhosphorus':
          return '总磷'
        default:
          return ''
      }
    },
    getFieldMin(field) {
      switch (field) {
        case 'year':
          return 1900
        case 'month':
          return 1
        case 'oxygen':
          return 0
        case 'potassiumPermanganate':
          return 0
        case 'cod':
          return 0
        case 'flow':
          return 0
        case 'waterDepth':
          return 0
        case 'totalNitrogen':
          return 0
        case 'totalPhosphorus':
          return 0
        default:
          return 0
      }
    },
    getFieldMax(field) {
      switch (field) {
        case 'year':
          return 2100
        case 'month':
          return 12
        case 'oxygen':
          return 100 // 假设最大值为100
        case 'potassiumPermanganate':
          return 100 // 假设最大值为100
        case 'cod':
          return 100 // 假设最大值为100
        case 'flow':
          return 1000 // 假设最大值为1000
        case 'waterDepth':
          return 10 // 假设最大值为10
        case 'totalNitrogen':
          return 100 // 假设最大值为100
        case 'totalPhosphorus':
          return 10 // 假设最大值为10
        default:
          return 100
      }
    },
    getFieldStep(field) {
      switch (field) {
        case 'year':
          return 1
        case 'month':
          return 1
        case 'oxygen':
          return 0.1
        case 'potassiumPermanganate':
          return 0.1
        case 'cod':
          return 0.1
        case 'flow':
          return 0.1
        case 'waterDepth':
          return 0.1
        case 'totalNitrogen':
          return 0.1
        case 'totalPhosphorus':
          return 0.1
        default:
          return 1
      }
    },
    getFieldUnit(field) {
      switch (field) {
        case 'year':
          return '年'
        case 'month':
          return '月'
        case 'oxygen':
          return 'mg/l'
        case 'potassiumPermanganate':
          return 'mg/l'
        case 'cod':
          return 'mg/l'
        case 'flow':
          return 'm³/s'
        case 'waterDepth':
          return 'm'
        case 'totalNitrogen':
          return 'mg/l'
        case 'totalPhosphorus':
          return 'mg/l'
        default:
          return ''
      }
    }
  }
}
</script>

<style scoped>
.error-message {
  color: #f56c6c;
  font-size: 12px;
  margin-top: 4px;
}

.is-error {
  border-color: #f56c6c;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}

.search-bar {
  margin-bottom: 20px;
  padding: 20px;
  background: #f5f5f5;
  border-radius: 4px;
}

/* 优化模态框表单布局 */
:deep(.el-form-item__label) {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  line-height: 1.5;
}

:deep(.el-form-item) {
  margin-bottom: 20px;
}

:deep(.el-input-number) {
  width: 100%;
}

:deep(.el-date-picker) {
  width: 100%;
}
</style> 