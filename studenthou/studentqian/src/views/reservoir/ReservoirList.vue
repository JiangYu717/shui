<template>
  <div class="reservoir-list-container">
    <div class="page-header">
      <h2>Reservoir Management</h2>
      <el-button type="primary" @click="showCreateDialog">
        <el-icon><Plus /></el-icon>
        Add Reservoir
      </el-button>
    </div>
    
    <div class="search-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="Search reservoirs..."
        clearable
        @input="handleSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
    </div>
    
    <el-table
      :data="reservoirs"
      style="width: 100%"
      v-loading="loading"
    >
      <el-table-column prop="reservoirName" label="Name" />
      <el-table-column prop="location" label="Location" />
      <el-table-column prop="capacity" label="Capacity (m³)">
        <template #default="scope">
          {{ formatNumber(scope.row.capacity) }}
        </template>
      </el-table-column>
      <el-table-column prop="waterLevel" label="Water Level (m)">
        <template #default="scope">
          {{ formatNumber(scope.row.waterLevel) }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="Status">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.status)">
            {{ scope.row.status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="Operations" width="200">
        <template #default="scope">
          <el-button-group>
            <el-button
              type="primary"
              :icon="View"
              @click="viewReservoir(scope.row)"
            >
              View
            </el-button>
            <el-button
              type="warning"
              :icon="Edit"
              @click="editReservoir(scope.row)"
            >
              Edit
            </el-button>
            <el-button
              type="danger"
              :icon="Delete"
              @click="confirmDelete(scope.row)"
            >
              Delete
            </el-button>
          </el-button-group>
        </template>
      </el-table-column>
    </el-table>
    
    <!-- Create/Edit Dialog -->
    <el-dialog
      :title="dialogTitle"
      v-model="dialogVisible"
      width="50%"
    >
      <el-form
        ref="reservoirForm"
        :model="reservoirForm"
        :rules="rules"
        label-width="120px"
      >
        <el-form-item label="Name" prop="reservoirName">
          <el-input v-model="reservoirForm.reservoirName" />
        </el-form-item>
        
        <el-form-item label="Location" prop="location">
          <el-input v-model="reservoirForm.location" />
        </el-form-item>
        
        <el-form-item label="Capacity (m³)" prop="capacity">
          <el-input-number
            v-model="reservoirForm.capacity"
            :min="0"
            :precision="2"
            :step="1000"
          />
        </el-form-item>
        
        <el-form-item label="Water Level (m)" prop="waterLevel">
          <el-input-number
            v-model="reservoirForm.waterLevel"
            :min="0"
            :precision="2"
            :step="1"
          />
        </el-form-item>
        
        <el-form-item label="Type" prop="type">
          <el-select v-model="reservoirForm.type">
            <el-option label="Large" value="large" />
            <el-option label="Medium" value="medium" />
            <el-option label="Small" value="small" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="Purpose" prop="purpose">
          <el-input type="textarea" v-model="reservoirForm.purpose" />
        </el-form-item>
        
        <el-form-item label="Contact Person" prop="responsiblePerson">
          <el-input v-model="reservoirForm.responsiblePerson" />
        </el-form-item>
        
        <el-form-item label="Contact Phone" prop="contactPhone">
          <el-input v-model="reservoirForm.contactPhone" />
        </el-form-item>
        
        <el-form-item label="Status" prop="status">
          <el-select v-model="reservoirForm.status">
            <el-option label="Normal" value="normal" />
            <el-option label="Maintenance" value="maintenance" />
            <el-option label="Risk" value="risk" />
          </el-select>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">Cancel</el-button>
          <el-button type="primary" @click="handleSubmit">
            {{ isEdit ? 'Update' : 'Create' }}
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted, computed } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'

export default {
  name: 'ReservoirList',
  components: {
    Plus,
    Search
  },
  setup() {
    const store = useStore()
    const router = useRouter()
    const loading = ref(false)
    const searchKeyword = ref('')
    const dialogVisible = ref(false)
    const isEdit = ref(false)
    const reservoirFormRef = ref(null)
    
    const reservoirs = computed(() => store.state.reservoir.reservoirs)
    const dialogTitle = computed(() => isEdit.value ? 'Edit Reservoir' : 'Create Reservoir')
    
    const formData = reactive({
      reservoirName: '',
      location: '',
      capacity: 0,
      waterLevel: 0,
      type: '',
      purpose: '',
      responsiblePerson: '',
      contactPhone: '',
      status: 'normal'
    })
    
    const rules = {
      reservoirName: [
        { required: true, message: 'Please enter reservoir name', trigger: 'blur' }
      ],
      location: [
        { required: true, message: 'Please enter location', trigger: 'blur' }
      ],
      capacity: [
        { required: true, message: 'Please enter capacity', trigger: 'blur' }
      ],
      waterLevel: [
        { required: true, message: 'Please enter water level', trigger: 'blur' }
      ],
      type: [
        { required: true, message: 'Please select type', trigger: 'change' }
      ],
      status: [
        { required: true, message: 'Please select status', trigger: 'change' }
      ]
    }
    
    const loadReservoirs = async () => {
      loading.value = true
      try {
        await store.dispatch('reservoir/fetchReservoirs')
      } catch (error) {
        ElMessage.error('Failed to load reservoirs')
      } finally {
        loading.value = false
      }
    }
    
    const handleSearch = async () => {
      if (searchKeyword.value) {
        loading.value = true
        try {
          await store.dispatch('reservoir/searchReservoirs', searchKeyword.value)
        } catch (error) {
          ElMessage.error('Search failed')
        } finally {
          loading.value = false
        }
      } else {
        loadReservoirs()
      }
    }
    
    const showCreateDialog = () => {
      isEdit.value = false
      Object.keys(formData).forEach(key => {
        formData[key] = ''
      })
      formData.status = 'normal'
      dialogVisible.value = true
    }
    
    const editReservoir = (reservoir) => {
      isEdit.value = true
      Object.keys(formData).forEach(key => {
        formData[key] = reservoir[key]
      })
      dialogVisible.value = true
    }
    
    const handleSubmit = async () => {
      if (!reservoirFormRef.value) return
      
      try {
        await reservoirFormRef.value.validate()
        
        if (isEdit.value) {
          await store.dispatch('reservoir/updateReservoir', {
            reservoirId: formData.reservoirId,
            reservoirData: formData
          })
          ElMessage.success('Reservoir updated successfully')
        } else {
          await store.dispatch('reservoir/createReservoir', formData)
          ElMessage.success('Reservoir created successfully')
        }
        
        dialogVisible.value = false
        loadReservoirs()
      } catch (error) {
        ElMessage.error(error.toString())
      }
    }
    
    const confirmDelete = (reservoir) => {
      ElMessageBox.confirm(
        `Are you sure to delete reservoir "${reservoir.reservoirName}"?`,
        'Warning',
        {
          confirmButtonText: 'Delete',
          cancelButtonText: 'Cancel',
          type: 'warning'
        }
      ).then(async () => {
        try {
          await store.dispatch('reservoir/deleteReservoir', reservoir.reservoirId)
          ElMessage.success('Reservoir deleted successfully')
          loadReservoirs()
        } catch (error) {
          ElMessage.error('Failed to delete reservoir')
        }
      })
    }
    
    const viewReservoir = (reservoir) => {
      router.push(`/reservoirs/${reservoir.reservoirId}`)
    }
    
    const formatNumber = (value) => {
      return value ? value.toLocaleString() : '0'
    }
    
    const getStatusType = (status) => {
      switch (status) {
        case 'maintenance':
          return 'warning'
        case 'risk':
          return 'danger'
        default:
          return 'success'
      }
    }
    
    onMounted(() => {
      loadReservoirs()
    })
    
    return {
      loading,
      searchKeyword,
      dialogVisible,
      dialogTitle,
      isEdit,
      reservoirFormRef,
      reservoirForm: formData,
      rules,
      reservoirs,
      showCreateDialog,
      editReservoir,
      handleSubmit,
      confirmDelete,
      viewReservoir,
      handleSearch,
      formatNumber,
      getStatusType
    }
  }
}
</script>

<style scoped>
.reservoir-list-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.search-bar {
  margin-bottom: 20px;
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