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
          <el-form-item label="年份">
            <el-input v-model="search.year" placeholder="年份" clearable />
          </el-form-item>
          <el-form-item label="月份">
            <el-input v-model="search.month" placeholder="月份" clearable />
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
          <div v-if="monitorPointNameError" class="error-message">监测点名称已存在，请使用其他名称</div>
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
        year: '',
        month: ''
      },
      dialogVisible: false,
      dialogTitle: '',
      monitorPointNameError: false,
      originalMonitorPointName: '',
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
      }
    }
  },
  created() {
    this.fetchList()
  },
  methods: {
    // 检查监测点名称是否重复
    async checkMonitorPointNameDuplicate() {
      if (!this.form.monitorPointName || this.form.monitorPointName.trim() === '') {
        this.monitorPointNameError = false
        return
      }
      
      // 如果是编辑模式且监测点名称没有改变，不检查
      if (this.form.id && this.form.monitorPointName === this.originalMonitorPointName) {
        this.monitorPointNameError = false
        return
      }
      
      try {
        const exists = await checkMonitorPointName(this.form.monitorPointName)
        this.monitorPointNameError = exists
      } catch (error) {
        console.error('检查监测点名称失败:', error)
      }
    },
    
    fetchList() {
      const params = {
        page: this.query.pageNum,
        pageSize: this.query.pageSize,
        ...this.search
      }
      fetchSectionMonitorList(params).then(res => {
        this.list = res.data.list
        this.total = res.data.total
      })
    },
    resetSearch() {
      this.search = { monitorPointName: '', reservoirName: '', year: '', month: '' }
      this.fetchList()
    },
    openDialog(row) {
      if (row) {
        this.dialogTitle = '编辑监测断面数据'
        this.form = { ...row }
        this.originalMonitorPointName = row.monitorPointName
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
      }
      this.monitorPointNameError = false
      this.dialogVisible = true
    },
    handleSubmit() {
      if (this.monitorPointNameError) {
        ElMessage.error('监测点名称已存在，请使用其他名称')
        return
      }
      
      this.$refs.formRef.validate((valid) => {
        if (valid) {
          if (this.form.id) {
            updateSectionMonitor(this.form).then(() => {
              ElMessage.success('修改成功')
              this.dialogVisible = false
              this.fetchList()
            }).catch(error => {
              ElMessage.error(error.message || '修改失败')
            })
          } else {
            createSectionMonitor(this.form).then(() => {
              ElMessage.success('新增成功')
              this.dialogVisible = false
              this.fetchList()
            }).catch(error => {
              ElMessage.error(error.message || '新增失败')
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