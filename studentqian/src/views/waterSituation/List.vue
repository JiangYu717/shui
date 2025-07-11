<template>
  <div class="water-situation-list">
    <div class="page-header">
      <h2>水情数据管理</h2>
      <el-button type="primary" @click="handleAdd">新增水情数据</el-button>
    </div>
    <el-form :inline="true" :model="searchForm" class="search-form" @submit.prevent>
      <el-form-item label="库名">
        <el-input v-model="searchForm.reservoirName" placeholder="请输入库名" clearable @keyup.enter="handleSearch" @clear="handleSearch" />
      </el-form-item>
      <el-form-item label="日期">
        <el-date-picker v-model="searchForm.date" type="date" placeholder="请选择日期" value-format="YYYY-MM-DD" clearable @change="handleSearch" />
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
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <el-form-item label="库名" prop="reservoirName">
          <el-input v-model="form.reservoirName" placeholder="请输入库名" />
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
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>
<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getWaterSituationList, addWaterSituation, updateWaterSituation, deleteWaterSituation } from '@/api/waterSituation'
const searchForm = reactive({ reservoirName: '', date: '' })
const tableData = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref(null)
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
function handleSearch() {
  loading.value = true
  getWaterSituationList({
    page: currentPage.value,
    pageSize: pageSize.value,
    reservoirName: searchForm.reservoirName,
    date: searchForm.date
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
  handleSearch()
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
  dialogTitle.value = '新增水情数据'
  dialogVisible.value = true
}

function handleEdit(row) {
  Object.assign(form, row)
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

function handleSubmit() {
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
      })
    } else {
      addWaterSituation(form).then(() => {
        ElMessage.success('新增成功')
        dialogVisible.value = false
        handleSearch()
      })
    }
  })
}

function handleSizeChange(val) {
  pageSize.value = val
  currentPage.value = 1
  handleSearch()
}

function handleCurrentChange(val) {
  currentPage.value = val
  handleSearch()
}

onMounted(() => {
  handleSearch()
})
</script>
<style scoped>
.water-situation-list { padding: 24px; }
.page-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; }
.search-form { margin-bottom: 16px; }
.pagination { margin-top: 16px; text-align: right; }
</style> 