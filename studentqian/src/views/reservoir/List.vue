<template>
  <div class="reservoir-list">
    <div class="page-header">
      <h2>水库数据管理</h2>
      <el-button type="primary" @click="handleAdd">新增水库</el-button>
    </div>

    <!-- 搜索表单 -->
    <el-form :inline="true" :model="searchForm" class="search-form" @submit.prevent>
      <el-form-item label="水库名称">
        <el-input
          v-model="searchForm.reservoirName"
          placeholder="请输入水库名称"
          clearable
          @keyup.enter="handleSearch"
          @clear="handleSearch"
        />
      </el-form-item>
      <el-form-item label="所在地区">
        <el-input
          v-model="searchForm.location"
          placeholder="请输入所在地区"
          clearable
          @keyup.enter="handleSearch"
          @clear="handleSearch"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 数据表格 -->
    <el-table :data="tableData" border style="width: 100%" v-loading="loading">
      <el-table-column prop="reservoirName" label="水库名称" />
      <el-table-column prop="location" label="所在地区" />
      <el-table-column prop="capacity" label="库容(万立方米)">
        <template #default="scope">
          {{ scope.row.capacity ? scope.row.capacity.toFixed(2) : '0.00' }}
        </template>
      </el-table-column>
      <el-table-column prop="waterLevel" label="水位(米)">
        <template #default="scope">
          {{ scope.row.waterLevel ? scope.row.waterLevel.toFixed(2) : '暂无数据' }}
        </template>
      </el-table-column>
      <el-table-column prop="type" label="水库类型" />
      <el-table-column prop="purpose" label="用途" />
      <el-table-column prop="responsiblePerson" label="负责人" />
      <el-table-column prop="status" label="状态">
        <template #default="{ row }">
          <el-tag :type="row.status === 'normal' ? 'success' : row.status === 'maintenance' ? 'warning' : 'danger'">
            {{ getStatusLabel(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="scope">
          <el-button type="primary" link @click="handleEdit(scope.row)">编辑</el-button>
          <el-button type="danger" link @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
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

    <!-- 新增/编辑对话框 -->
    <el-dialog
      :title="dialogTitle"
      v-model="dialogVisible"
      width="600px"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
      >
        <el-form-item label="水库名称" prop="reservoirName">
          <el-input v-model="form.reservoirName" placeholder="请输入水库名称" />
        </el-form-item>
        <el-form-item label="所在地区" prop="location">
          <el-input v-model="form.location" placeholder="请输入所在地区" />
        </el-form-item>
        <el-form-item label="库容" prop="capacity">
          <el-input-number v-model="form.capacity" :min="0" :precision="2" placeholder="请输入库容" />
          <span class="unit-text">万立方米</span>
        </el-form-item>
        <el-form-item label="水位" prop="waterLevel">
          <el-input-number v-model="form.waterLevel" :min="0" :precision="2" placeholder="请输入水位" />
          <span class="unit-text">米</span>
        </el-form-item>
        <el-form-item label="水库类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择水库类型">
            <el-option label="大型水库" value="大型水库" />
            <el-option label="中型水库" value="中型水库" />
            <el-option label="小型水库" value="小型水库" />
          </el-select>
        </el-form-item>
        <el-form-item label="用途" prop="purpose">
          <el-input v-model="form.purpose" placeholder="请输入用途" />
        </el-form-item>
        <el-form-item label="负责人" prop="responsiblePerson">
          <el-input v-model="form.responsiblePerson" placeholder="请输入负责人姓名" />
        </el-form-item>
        <el-form-item label="联系电话" prop="contactPhone">
          <el-input v-model="form.contactPhone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="建设日期" prop="constructionDate">
          <el-date-picker
            v-model="form.constructionDate"
            type="date"
            placeholder="请选择建设日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="最后维护日期" prop="lastMaintenanceDate">
          <el-date-picker
            v-model="form.lastMaintenanceDate"
            type="date"
            placeholder="请选择最后维护日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" placeholder="请选择状态">
            <el-option
              v-for="option in statusOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
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

<script>
import { ref, reactive, watch, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getReservoirList, addReservoir, updateReservoir, deleteReservoir } from '@/api/reservoir'

export default {
  name: 'ReservoirList',
  setup() {
    // 搜索表单数据
    const searchForm = reactive({
      reservoirName: '',
      location: ''
    })

    // 表格数据
    const tableData = ref([])
    const currentPage = ref(1)
    const pageSize = ref(10)
    const total = ref(0)
    const loading = ref(false)

    // 表单数据
    const dialogVisible = ref(false)
    const dialogTitle = ref('')
    const formRef = ref(null)
    const form = reactive({
      reservoirId: '',
      reservoirName: '',
      location: '',
      capacity: null,
      waterLevel: null,
      type: '',
      purpose: '',
      responsiblePerson: '',
      contactPhone: '',
      constructionDate: '',
      lastMaintenanceDate: '',
      status: 'normal'
    })

    // 表单验证规则
    const rules = {
      reservoirName: [
        { required: true, message: '请输入水库名称', trigger: 'blur' },
        { min: 2, max: 50, message: '水库名称长度应在2-50个字符之间', trigger: 'blur' }
      ],
      location: [
        { required: true, message: '请输入所在地区', trigger: 'blur' }
      ],
      capacity: [
        { required: true, message: '请输入库容', trigger: 'blur' },
        { type: 'number', min: 0, message: '库容必须大于0', trigger: 'blur' }
      ],
      type: [
        { required: true, message: '请选择水库类型', trigger: 'change' }
      ],
      waterLevel: [
        { type: 'number', min: 0, message: '水位必须大于0', trigger: 'blur' }
      ],
      purpose: [
        { max: 200, message: '用途描述不能超过200个字符', trigger: 'blur' }
      ],
      responsiblePerson: [
        { max: 100, message: '负责人姓名不能超过100个字符', trigger: 'blur' }
      ],
      contactPhone: [
        { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
      ],
      constructionDate: [
        { required: true, message: '请选择建设日期', trigger: 'change' }
      ],
      lastMaintenanceDate: [
        { required: true, message: '请选择最后维护日期', trigger: 'change' }
      ],
      status: [
        { required: true, message: '请选择状态', trigger: 'change' }
      ]
    }

    // 获取列表数据
    const getList = async () => {
      console.log('开始获取水库列表数据...')
      try {
        loading.value = true
        const params = {
          pageNum: currentPage.value,
          pageSize: pageSize.value,
          ...searchForm
        }
        
        // 移除空字符串参数
        Object.keys(params).forEach(key => {
          if (params[key] === '') {
            delete params[key]
          }
        })
        
        console.log('发送查询参数:', params)
        const res = await getReservoirList(params)
        
        console.log('API原始响应:', res)
        
        if (res && res.data) {
          const { list, total: totalCount, pageNum, pageSize: size } = res.data
          console.log('解析到的列表数据:', list)
          
          if (list && list.length > 0) {
            console.log('第一条数据示例:', list[0])
          } else {
            console.log('查询结果为空')
          }
          
          tableData.value = list || []
          total.value = totalCount || 0
          currentPage.value = pageNum || 1
          pageSize.value = size || 10
          
          if (list.length === 0) {
            ElMessage.info('未找到匹配的数据')
          }
          
          console.log('更新后的表格数据:', {
            tableData: tableData.value,
            total: total.value,
            currentPage: currentPage.value,
            pageSize: pageSize.value
          })
        } else {
          console.warn('API响应格式不正确')
          tableData.value = []
          total.value = 0
        }
      } catch (error) {
        console.error('获取水库列表失败:', error)
        ElMessage.error('获取水库列表失败')
        tableData.value = []
        total.value = 0
      } finally {
        loading.value = false
      }
    }

    // 搜索
    const handleSearch = async () => {
      console.log('执行搜索，搜索条件：', searchForm)
      currentPage.value = 1
      await getList()
    }

    // 重置搜索
    const resetSearch = () => {
      searchForm.reservoirName = ''
      searchForm.location = ''
      handleSearch()
    }

    // 重置表单
    const resetForm = () => {
      Object.assign(form, {
        reservoirId: '',
        reservoirName: '',
        location: '',
        capacity: null,
        waterLevel: null,
        type: '',
        purpose: '',
        responsiblePerson: '',
        contactPhone: '',
        constructionDate: '',
        lastMaintenanceDate: '',
        status: 'normal'
      })
      if (formRef.value) {
        formRef.value.resetFields()
      }
    }

    // 新增水库
    const handleAdd = () => {
      dialogTitle.value = '新增水库'
      resetForm()
      dialogVisible.value = true
    }

    // 编辑水库
    const handleEdit = (row) => {
      dialogTitle.value = '编辑水库'
      Object.assign(form, {
        ...row,
        status: row.status || 'normal',
        constructionDate: row.constructionDate ? row.constructionDate.split('T')[0] : '',
        lastMaintenanceDate: row.lastMaintenanceDate ? row.lastMaintenanceDate.split('T')[0] : ''
      })
      dialogVisible.value = true
    }

    // 删除水库
    const handleDelete = (row) => {
      ElMessageBox.confirm(`确认删除水库"${row.reservoirName}"吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const res = await deleteReservoir(row.reservoirId)
          if (res && res.message) {
            ElMessage.success(res.message)
          } else {
            ElMessage.success('删除成功')
          }
          getList()
        } catch (error) {
          console.error('删除失败:', error)
          ElMessage.error('删除失败')
        }
      }).catch(() => {})
    }

    // 提交表单
    const handleSubmit = async () => {
      if (!formRef.value) return
      
      await formRef.value.validate(async (valid) => {
        if (valid) {
          loading.value = true
          try {
            if (form.reservoirId) {
              // 更新
              await updateReservoir(form.reservoirId, form)
              ElMessage.success('更新成功')
            } else {
              // 新增
              await addReservoir(form)
              ElMessage.success('添加成功')
            }
            dialogVisible.value = false
            getList()
          } catch (error) {
            ElMessage.error('保存失败: ' + error.message)
          } finally {
            loading.value = false
          }
        }
      })
    }

    // 分页
    const handleSizeChange = (val) => {
      pageSize.value = val
      getList()
    }

    const handleCurrentChange = (val) => {
      currentPage.value = val
      getList()
    }

    // 监听分页变化
    watch([currentPage, pageSize], () => {
      getList()
    })

    // 初始化时获取列表数据
    onMounted(() => {
      console.log('组件已挂载，开始获取数据')
      getList()
    })

    // 状态选项
    const statusOptions = [
      { label: '正常', value: 'normal' },
      { label: '维护中', value: 'maintenance' },
      { label: '风险', value: 'risk' }
    ]

    // 获取状态的中文显示
    const getStatusLabel = (value) => {
      return statusOptions.find(option => option.value === value)?.label || '正常'
    }

    return {
      searchForm,
      tableData,
      currentPage,
      pageSize,
      total,
      dialogVisible,
      dialogTitle,
      formRef,
      form,
      rules,
      handleSearch,
      resetSearch,
      handleAdd,
      handleEdit,
      handleDelete,
      handleSubmit,
      handleSizeChange,
      handleCurrentChange,
      loading,
      resetForm,
      statusOptions,
      getStatusLabel
    }
  }
}
</script>

<style scoped>
.reservoir-list {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.search-form {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.unit-text {
  margin-left: 8px;
  color: #666;
}

.el-input-number {
  width: 200px;
}

.el-date-picker {
  width: 200px;
}
</style> 