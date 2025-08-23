<template>
  <div class="water-situation-list">
    <div class="page-header">
      <h2>水情管理</h2>
      <div class="header-actions">
        <el-button type="success" @click="handleImport">
          <el-icon><upload /></el-icon>
          数据导入
        </el-button>
        <el-button type="warning" @click="handleExport">
          <el-icon><download /></el-icon>
          数据导出
        </el-button>
        <el-button type="primary" @click="handleAdd">新增</el-button>
      </div>
    </div>
    <el-form :inline="true" :model="searchForm" class="search-form" @submit.prevent>
      <el-form-item label="库名">
        <el-input v-model="searchForm.reservoirName" placeholder="请输入库名" clearable @keyup.enter="handleSearch" @clear="handleSearch" />
      </el-form-item>
      <el-form-item label="日期">
        <el-date-picker v-model="searchForm.date" type="date" placeholder="请选择日期" value-format="YYYY-MM-DD" clearable @change="handleSearch" />
      </el-form-item>
      <el-form-item label="查询字段">
        <el-select v-model="selectedField" placeholder="请选择查询字段" @change="handleFieldChange" clearable>
          <el-option label="库水位" value="waterLevel" />
          <el-option label="蓄水量" value="storage" />
          <el-option label="日平均入库流量" value="avgInflow" />
          <el-option label="日平均出库流量" value="avgOutflow" />
          <el-option label="比去年同期增减" value="yoyIncrease" />
          <el-option label="总库容" value="totalCapacity" />
          <el-option label="汛限水位" value="floodLevel" />
        </el-select>
      </el-form-item>
      <el-form-item v-if="selectedField" :label="getFieldLabel(selectedField)">
        <el-input-number 
          v-model="searchForm[selectedField + 'Min']" 
          :placeholder="'最小值'" 
          :min="0" 
          :step="getFieldStep(selectedField)"
          style="width: 120px;"
          clearable
        />
        <span style="margin: 0 8px;">-</span>
        <el-input-number 
          v-model="searchForm[selectedField + 'Max']" 
          :placeholder="'最大值'" 
          :min="0" 
          :step="getFieldStep(selectedField)"
          style="width: 120px;"
          clearable
        />
        <span style="margin-left: 8px; color: #909399; font-size: 12px;">({{ getFieldUnit(selectedField) }})</span>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </el-form-item>
    </el-form>
    <el-table :data="tableData" border style="width: 100%" v-loading="loading">
      <el-table-column prop="reservoirName" label="库名" />
      <el-table-column prop="date" label="日期" />
      <el-table-column prop="waterLevel" label="库水位(米)" />
      <el-table-column prop="storage" label="蓄水量(万立方米)" />
      <el-table-column prop="avgInflow" label="日平均入库流量(立方米/秒)" />
      <el-table-column prop="avgOutflow" label="日平均出库流量(立方米/秒)" />
      <el-table-column prop="yoyIncrease" label="比去年同期增减(万立方米)" />
      <el-table-column prop="totalCapacity" label="总库容(万立方米)" />
      <el-table-column prop="floodLevel" label="汛限水位(米)" />
      <el-table-column label="操作" width="180">
        <template #default="scope">
          <el-button type="primary" link @click="handleEdit(scope.row)">编辑</el-button>
          <el-button type="danger" link @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="pagination">
      <el-pagination
        :current-page="currentPage"
        :page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next"
        @update:current-page="currentPage = $event"
        @update:page-size="pageSize = $event"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="700px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="180px">
        <el-form-item label="库名" prop="reservoirName">
          <el-input 
            v-model="form.reservoirName" 
            placeholder="请输入库名" 
            @blur="checkReservoirNameDuplicate"
            :class="{ 'is-error': reservoirNameError }"
          />
          <div v-if="reservoirNameError" class="error-message">库名已存在，请使用其他库名</div>
        </el-form-item>
        <el-form-item label="日期" prop="date">
          <el-date-picker v-model="form.date" type="datetime" placeholder="请选择日期" value-format="YYYY-MM-DD HH:mm:ss" />
        </el-form-item>
        <el-form-item label="库水位(米)" prop="waterLevel">
          <el-input-number v-model="form.waterLevel" :min="0" :precision="2" placeholder="请输入库水位" />
        </el-form-item>
        <el-form-item label="蓄水量(万立方米)" prop="storage">
          <el-input-number v-model="form.storage" :min="0" :precision="2" placeholder="请输入蓄水量" />
        </el-form-item>
        <el-form-item label="日平均入库流量(立方米/秒)" prop="avgInflow">
          <el-input-number v-model="form.avgInflow" :min="0" :precision="3" placeholder="请输入日平均入库流量" />
        </el-form-item>
        <el-form-item label="日平均出库流量(立方米/秒)" prop="avgOutflow">
          <el-input-number v-model="form.avgOutflow" :min="0" :precision="3" placeholder="请输入日平均出库流量" />
        </el-form-item>
        <el-form-item label="比去年同期增减(万立方米)" prop="yoyIncrease">
          <el-input-number v-model="form.yoyIncrease" :precision="2" placeholder="请输入比去年同期增减" />
        </el-form-item>
        <el-form-item label="总库容(万立方米)" prop="totalCapacity">
          <el-input-number v-model="form.totalCapacity" :min="0" :precision="2" placeholder="请输入总库容" />
        </el-form-item>
        <el-form-item label="汛限水位(米)" prop="floodLevel">
          <el-input-number v-model="form.floodLevel" :min="0" :precision="2" placeholder="请输入汛限水位" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :disabled="reservoirNameError">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>
<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Upload, Download } from '@element-plus/icons-vue'
import { fetchWaterSituationList, createWaterSituation, updateWaterSituation, deleteWaterSituation, checkReservoirName } from '@/api/waterSituation'

const router = useRouter()

const searchForm = reactive({ 
  reservoirName: '', 
  date: '',
  waterLevelMin: null,
  waterLevelMax: null,
  storageMin: null, 
  storageMax: null, 
  avgInflowMin: null,
  avgInflowMax: null,
  avgOutflowMin: null,
  avgOutflowMax: null,
  yoyIncreaseMin: null,
  yoyIncreaseMax: null,
  totalCapacityMin: null, 
  totalCapacityMax: null,
  floodLevelMin: null,
  floodLevelMax: null
})
const selectedField = ref('')
const tableData = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref(null)
const reservoirNameError = ref(false)
const originalReservoirName = ref('')

const form = reactive({
  id: '',
  reservoirName: '',
  date: '',
  waterLevel: null,
  storage: null,
  avgInflow: null,
  avgOutflow: null,
  yoyIncrease: null,
  totalCapacity: null,
  floodLevel: null
})

const rules = {
  reservoirName: [{ required: true, message: '请输入库名', trigger: 'blur' }],
  date: [{ required: true, message: '请选择日期', trigger: 'change' }]
}

// 检查库名是否重复
async function checkReservoirNameDuplicate() {
  if (!form.reservoirName || form.reservoirName.trim() === '') {
    reservoirNameError.value = false
    return
  }
  
  // 如果是编辑模式且库名没有改变，不检查
  if (form.id && form.reservoirName === originalReservoirName.value) {
    reservoirNameError.value = false
    return
  }
  
  try {
    const response = await checkReservoirName(form.reservoirName)
    // 根据我们的 request.js 配置，返回的数据在 response.data 中
    reservoirNameError.value = response.data
  } catch (error) {
    console.error('检查库名失败:', error)
    // 发生错误时，重置错误状态，允许用户继续操作
    reservoirNameError.value = false
  }
}

function handleSearch() {
  loading.value = true
  fetchWaterSituationList({
    page: currentPage.value,
    pageSize: pageSize.value,
    reservoirName: searchForm.reservoirName,
    date: searchForm.date,
    waterLevelMin: searchForm.waterLevelMin,
    waterLevelMax: searchForm.waterLevelMax,
    storageMin: searchForm.storageMin,
    storageMax: searchForm.storageMax,
    avgInflowMin: searchForm.avgInflowMin,
    avgInflowMax: searchForm.avgInflowMax,
    avgOutflowMin: searchForm.avgOutflowMin,
    avgOutflowMax: searchForm.avgOutflowMax,
    yoyIncreaseMin: searchForm.yoyIncreaseMin,
    yoyIncreaseMax: searchForm.yoyIncreaseMax,
    totalCapacityMin: searchForm.totalCapacityMin,
    totalCapacityMax: searchForm.totalCapacityMax,
    floodLevelMin: searchForm.floodLevelMin,
    floodLevelMax: searchForm.floodLevelMax
  }).then(res => {
    tableData.value = res.data.list
    total.value = res.data.total
  }).finally(() => {
    loading.value = false
  })
}

function resetSearch() {
  searchForm.reservoirName = ''
  searchForm.date = ''
  searchForm.waterLevelMin = null
  searchForm.waterLevelMax = null
  searchForm.storageMin = null
  searchForm.storageMax = null
  searchForm.avgInflowMin = null
  searchForm.avgInflowMax = null
  searchForm.avgOutflowMin = null
  searchForm.avgOutflowMax = null
  searchForm.yoyIncreaseMin = null
  searchForm.yoyIncreaseMax = null
  searchForm.totalCapacityMin = null
  searchForm.totalCapacityMax = null
  searchForm.floodLevelMin = null
  searchForm.floodLevelMax = null
  selectedField.value = ''
  handleSearch()
}

function handleFieldChange(value) {
  selectedField.value = value
}

function getFieldLabel(field) {
  switch (field) {
    case 'waterLevel':
      return '库水位(米)'
    case 'storage':
      return '蓄水量(万立方米)'
    case 'avgInflow':
      return '日平均入库流量(立方米/秒)'
    case 'avgOutflow':
      return '日平均出库流量(立方米/秒)'
    case 'yoyIncrease':
      return '比去年同期增减(万立方米)'
    case 'totalCapacity':
      return '总库容(万立方米)'
    case 'floodLevel':
      return '汛限水位(米)'
    default:
      return ''
  }
}

function getFieldStep(field) {
  switch (field) {
    case 'waterLevel':
      return 0.1
    case 'storage':
      return 1000
    case 'avgInflow':
      return 0.1
    case 'avgOutflow':
      return 0.1
    case 'yoyIncrease':
      return 1000
    case 'totalCapacity':
      return 1000
    case 'floodLevel':
      return 0.1
    default:
      return 1
  }
}

function getFieldUnit(field) {
  switch (field) {
    case 'waterLevel':
      return '米'
    case 'storage':
      return '万立方米'
    case 'avgInflow':
      return '立方米/秒'
    case 'avgOutflow':
      return '立方米/秒'
    case 'yoyIncrease':
      return '万立方米'
    case 'totalCapacity':
      return '万立方米'
    case 'floodLevel':
      return '米'
    default:
      return ''
  }
}

function handleAdd() {
  Object.assign(form, {
    id: '',
    reservoirName: '',
    date: '',
    waterLevel: null,
    storage: null,
    avgInflow: null,
    avgOutflow: null,
    yoyIncrease: null,
    totalCapacity: null,
    floodLevel: null
  })
  originalReservoirName.value = ''
  reservoirNameError.value = false
  dialogTitle.value = '新增水情数据'
  dialogVisible.value = true
}

function handleEdit(row) {
  Object.assign(form, row)
  originalReservoirName.value = row.reservoirName
  reservoirNameError.value = false
  dialogTitle.value = '编辑水情数据'
  dialogVisible.value = true
}

function handleDelete(row) {
  ElMessageBox.confirm('确定要删除这条数据吗？', '提示', { type: 'warning' })
    .then(() => {
      return deleteWaterSituation(row.id)
    })
    .then(() => {
      ElMessage.success('删除成功')
      handleSearch()
    })
}

async function handleSubmit() {
  // 提交前重新检查库名
  await checkReservoirNameDuplicate()
  
  if (reservoirNameError.value) {
    ElMessage.error('库名已存在，请使用其他库名')
    return
  }
  
  formRef.value.validate().then(() => {
    // 格式化日期为 yyyy-MM-dd HH:mm:ss
    if (form.date && typeof form.date === 'string') {
      form.date = form.date.replace('T', ' ')
    }
    // 数字字段空字符串转为null
    const numericFields = [
      'waterLevel', 'storage', 'avgInflow', 'avgOutflow',
      'yoyIncrease', 'totalCapacity', 'floodLevel'
    ]
    numericFields.forEach(key => {
      if (form[key] === '' || form[key] === undefined) {
        form[key] = null
      }
    })
    
    if (form.id) {
      updateWaterSituation(form).then(() => {
        ElMessage.success('修改成功')
        dialogVisible.value = false
        handleSearch()
      }).catch(error => {
        ElMessage.error(error.message || '修改失败')
      })
    } else {
      createWaterSituation(form).then(() => {
        ElMessage.success('新增成功')
        dialogVisible.value = false
        handleSearch()
      }).catch(error => {
        ElMessage.error(error.message || '新增失败')
      })
    }
  })
}

function handleSizeChange(val) {
  pageSize.value = val
  handleSearch()
}

function handleCurrentChange(val) {
  currentPage.value = val
  handleSearch()
}

// 数据导入
function handleImport() {
  // 跳转到导入页面
  router.push('/waterSituation/import')
}

// 数据导出
function handleExport() {
  // 跳转到导出页面
  router.push('/waterSituation/export')
}

onMounted(() => {
  handleSearch()
})
</script>

<style scoped>
.water-situation-list {
  padding: 20px;
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

.search-form {
  margin-bottom: 20px;
  padding: 20px;
  background: #f5f5f5;
  border-radius: 4px;
}

.pagination {
  margin-top: 20px;
  text-align: right;
}

.error-message {
  color: #f56c6c;
  font-size: 12px;
  margin-top: 4px;
}

.is-error {
  border-color: #f56c6c;
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