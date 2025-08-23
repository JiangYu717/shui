<template>
  <div class="water-quality-list-container">
    <div class="page-header">
      <h2>Water Quality Data</h2>
      <el-button type="primary" @click="showAddDialog">
        <el-icon><Plus /></el-icon>
        Add Record
      </el-button>
    </div>
    
    <!-- Search and Filter -->
    <el-form :inline="true" class="search-form">
      <el-form-item label="Reservoir">
        <el-select
          v-model="searchForm.reservoirId"
          placeholder="Select reservoir"
          clearable
          @change="handleSearch"
        >
          <el-option
            v-for="reservoir in reservoirs"
            :key="reservoir.reservoirId"
            :label="reservoir.reservoirName"
            :value="reservoir.reservoirId"
          />
        </el-select>
      </el-form-item>
      
      <el-form-item label="Date Range">
        <el-date-picker
          v-model="searchForm.dateRange"
          type="daterange"
          range-separator="to"
          start-placeholder="Start date"
          end-placeholder="End date"
          @change="handleSearch"
        />
      </el-form-item>
      
      <el-form-item label="Risk Level">
        <el-select
          v-model="searchForm.riskLevel"
          placeholder="Select risk level"
          clearable
          @change="handleSearch"
        >
          <el-option label="Normal" value="normal" />
          <el-option label="Warning" value="warning" />
          <el-option label="Danger" value="danger" />
        </el-select>
      </el-form-item>
      
      <el-form-item>
        <el-button type="primary" @click="handleSearch">
          <el-icon><Search /></el-icon>
          Search
        </el-button>
        <el-button @click="resetSearch">Reset</el-button>
      </el-form-item>
    </el-form>
    
    <!-- Data Table -->
    <el-table
      :data="waterQualityData"
      style="width: 100%"
      v-loading="loading"
    >
      <el-table-column prop="reservoirName" label="Reservoir" />
      <el-table-column prop="measurementDate" label="Date">
        <template #default="scope">
          {{ formatDate(scope.row.measurementDate) }}
        </template>
      </el-table-column>
      <el-table-column prop="phValue" label="pH">
        <template #default="scope">
          <el-tag
            :type="getPhValueType(scope.row.phValue)"
            size="small"
          >
            {{ scope.row.phValue }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="dissolvedOxygen" label="DO (mg/L)">
        <template #default="scope">
          <el-tag
            :type="getDoValueType(scope.row.dissolvedOxygen)"
            size="small"
          >
            {{ scope.row.dissolvedOxygen }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="turbidity" label="Turbidity (NTU)" />
      <el-table-column prop="riskLevel" label="Risk Level">
        <template #default="scope">
          <el-tag :type="getRiskLevelType(scope.row.riskLevel)">
            {{ scope.row.riskLevel }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="inspector" label="Inspector" />
      <el-table-column label="Operations" width="150">
        <template #default="scope">
          <el-button-group>
            <el-button
              type="warning"
              @click="editRecord(scope.row)"
            >
              <el-icon><Edit /></el-icon>
            </el-button>
            <el-button
              type="danger"
              @click="confirmDelete(scope.row)"
            >
              <el-icon><Delete /></el-icon>
            </el-button>
          </el-button-group>
        </template>
      </el-table-column>
    </el-table>
    
    <!-- Pagination -->
    <div class="pagination-container">
      <el-pagination
        :current-page="currentPage"
        :page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        @update:current-page="currentPage = $event"
        @update:page-size="pageSize = $event"
      />
    </div>
    
    <!-- Add/Edit Dialog -->
    <el-dialog
      :title="dialogTitle"
      v-model="dialogVisible"
      width="50%"
    >
      <el-form
        ref="dataFormRef"
        :model="dataForm"
        :rules="rules"
        label-width="140px"
      >
        <el-form-item label="Reservoir" prop="reservoirId">
          <el-select
            v-model="dataForm.reservoirId"
            placeholder="Select reservoir"
          >
            <el-option
              v-for="reservoir in reservoirs"
              :key="reservoir.reservoirId"
              :label="reservoir.reservoirName"
              :value="reservoir.reservoirId"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="Measurement Date" prop="measurementDate">
          <el-date-picker
            v-model="dataForm.measurementDate"
            type="datetime"
            placeholder="Select date and time"
          />
        </el-form-item>
        
        <el-form-item label="pH Value" prop="phValue">
          <el-input-number
            v-model="dataForm.phValue"
            :min="0"
            :max="14"
            :precision="2"
            :step="0.1"
          />
        </el-form-item>
        
        <el-form-item label="Dissolved Oxygen" prop="dissolvedOxygen">
          <el-input-number
            v-model="dataForm.dissolvedOxygen"
            :min="0"
            :precision="2"
            :step="0.1"
          />
        </el-form-item>
        
        <el-form-item label="Turbidity" prop="turbidity">
          <el-input-number
            v-model="dataForm.turbidity"
            :min="0"
            :precision="2"
            :step="0.1"
          />
        </el-form-item>
        
        <el-form-item label="Inspector" prop="inspector">
          <el-input v-model="dataForm.inspector" />
        </el-form-item>
        
        <el-form-item label="Notes" prop="notes">
          <el-input
            type="textarea"
            v-model="dataForm.notes"
            :rows="3"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">Cancel</el-button>
          <el-button type="primary" @click="handleSubmit">
            {{ isEdit ? 'Update' : 'Add' }}
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted, computed } from 'vue'
import { useStore } from 'vuex'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Edit, Delete } from '@element-plus/icons-vue'

export default {
  name: 'WaterQualityList',
  components: {
    Plus,
    Search,
    Edit,
    Delete
  },
  setup() {
    const store = useStore()
    const loading = ref(false)
    const dialogVisible = ref(false)
    const isEdit = ref(false)
    const dataFormRef = ref(null)
    const currentPage = ref(1)
    const pageSize = ref(10)
    const total = ref(0)
    
    const searchForm = reactive({
      reservoirId: null,
      dateRange: null,
      riskLevel: null
    })
    
    const formData = reactive({
      reservoirId: null,
      measurementDate: null,
      phValue: null,
      dissolvedOxygen: null,
      turbidity: null,
      inspector: '',
      notes: ''
    })
    
    const rules = {
      reservoirId: [
        { required: true, message: 'Please select reservoir', trigger: 'change' }
      ],
      measurementDate: [
        { required: true, message: 'Please select measurement date', trigger: 'change' }
      ],
      phValue: [
        { required: true, message: 'Please enter pH value', trigger: 'blur' }
      ],
      dissolvedOxygen: [
        { required: true, message: 'Please enter dissolved oxygen', trigger: 'blur' }
      ],
      turbidity: [
        { required: true, message: 'Please enter turbidity', trigger: 'blur' }
      ],
      inspector: [
        { required: true, message: 'Please enter inspector name', trigger: 'blur' }
      ]
    }
    
    const reservoirs = computed(() => store.state.reservoir.reservoirs)
    const waterQualityData = computed(() => store.state.waterQuality.waterQualityData)
    const dialogTitle = computed(() => isEdit.value ? 'Edit Record' : 'Add Record')
    
    const loadData = async () => {
      loading.value = true
      try {
        await store.dispatch('reservoir/fetchReservoirs')
        await store.dispatch('waterQuality/fetchWaterQualityData', {
          page: currentPage.value,
          pageSize: pageSize.value,
          ...searchForm
        })
        total.value = store.state.waterQuality.total
      } catch (error) {
        ElMessage.error('Failed to load data')
      } finally {
        loading.value = false
      }
    }
    
    const handleSearch = () => {
      currentPage.value = 1
      loadData()
    }
    
    const resetSearch = () => {
      Object.keys(searchForm).forEach(key => {
        searchForm[key] = null
      })
      handleSearch()
    }
    
    const showAddDialog = () => {
      isEdit.value = false
      Object.keys(formData).forEach(key => {
        formData[key] = null
      })
      dialogVisible.value = true
    }
    
    const editRecord = (record) => {
      isEdit.value = true
      Object.keys(formData).forEach(key => {
        formData[key] = record[key]
      })
      dialogVisible.value = true
    }
    
    const handleSubmit = async () => {
      if (!dataFormRef.value) return
      
      try {
        await dataFormRef.value.validate()
        
        if (isEdit.value) {
          await store.dispatch('waterQuality/updateWaterQualityData', {
            dataId: formData.dataId,
            data: formData
          })
          ElMessage.success('Record updated successfully')
        } else {
          await store.dispatch('waterQuality/createWaterQualityData', formData)
          ElMessage.success('Record added successfully')
        }
        
        dialogVisible.value = false
        loadData()
      } catch (error) {
        ElMessage.error(error.toString())
      }
    }
    
    const confirmDelete = (record) => {
      ElMessageBox.confirm(
        'Are you sure to delete this record?',
        'Warning',
        {
          confirmButtonText: 'Delete',
          cancelButtonText: 'Cancel',
          type: 'warning'
        }
      ).then(async () => {
        try {
          await store.dispatch('waterQuality/deleteWaterQualityData', record.dataId)
          ElMessage.success('Record deleted successfully')
          loadData()
        } catch (error) {
          ElMessage.error('Failed to delete record')
        }
      })
    }
    
    const handleSizeChange = (val) => {
      pageSize.value = val
      loadData()
    }
    
    const handleCurrentChange = (val) => {
      currentPage.value = val
      loadData()
    }
    
    const formatDate = (date) => {
      return new Date(date).toLocaleString()
    }
    
    const getPhValueType = (value) => {
      if (value < 6.5 || value > 8.5) return 'danger'
      if (value < 7.0 || value > 8.0) return 'warning'
      return 'success'
    }
    
    const getDoValueType = (value) => {
      if (value < 4) return 'danger'
      if (value < 6) return 'warning'
      return 'success'
    }
    
    const getRiskLevelType = (level) => {
      switch (level) {
        case 'warning':
          return 'warning'
        case 'danger':
          return 'danger'
        default:
          return 'success'
      }
    }
    
    onMounted(() => {
      loadData()
    })
    
    return {
      loading,
      dialogVisible,
      dialogTitle,
      isEdit,
      dataFormRef,
      dataForm: formData,
      rules,
      currentPage,
      pageSize,
      total,
      searchForm,
      reservoirs,
      waterQualityData,
      handleSearch,
      resetSearch,
      showAddDialog,
      editRecord,
      confirmDelete,
      handleSubmit,
      handleSizeChange,
      handleCurrentChange,
      formatDate,
      getPhValueType,
      getDoValueType,
      getRiskLevelType
    }
  }
}
</script>

<style scoped>
.water-quality-list-container {
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
  padding: 20px;
  background-color: #fff;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

:deep(.el-button-group) {
  display: flex;
  gap: 5px;
}
</style> 