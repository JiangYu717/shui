<template>
  <div class="reservoir-detail-container" v-loading="loading">
    <!-- Basic Information -->
    <el-card class="detail-card">
      <template #header>
        <div class="card-header">
          <h3>Reservoir Information</h3>
          <el-button-group>
            <el-button type="warning" @click="editReservoir">
              <el-icon><Edit /></el-icon>
              Edit
            </el-button>
            <el-button type="danger" @click="confirmDelete">
              <el-icon><Delete /></el-icon>
              Delete
            </el-button>
          </el-button-group>
        </div>
      </template>
      
      <el-descriptions :column="2" border>
        <el-descriptions-item label="Name">
          {{ reservoir?.reservoirName }}
        </el-descriptions-item>
        <el-descriptions-item label="Location">
          {{ reservoir?.location }}
        </el-descriptions-item>
        <el-descriptions-item label="Capacity">
          {{ formatNumber(reservoir?.capacity) }} m³
        </el-descriptions-item>
        <el-descriptions-item label="Water Level">
          {{ formatNumber(reservoir?.waterLevel) }} m
        </el-descriptions-item>
        <el-descriptions-item label="Type">
          {{ reservoir?.type }}
        </el-descriptions-item>
        <el-descriptions-item label="Status">
          <el-tag :type="getStatusType(reservoir?.status)">
            {{ reservoir?.status }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="Purpose" :span="2">
          {{ reservoir?.purpose }}
        </el-descriptions-item>
        <el-descriptions-item label="Contact Person">
          {{ reservoir?.responsiblePerson }}
        </el-descriptions-item>
        <el-descriptions-item label="Contact Phone">
          {{ reservoir?.contactPhone }}
        </el-descriptions-item>
      </el-descriptions>
    </el-card>
    
    <!-- Water Quality Data -->
    <el-card class="detail-card">
      <template #header>
        <div class="card-header">
          <h3>Water Quality Records</h3>
          <el-button type="primary" @click="showAddDataDialog">
            <el-icon><Plus /></el-icon>
            Add Record
          </el-button>
        </div>
      </template>
      
      <el-table :data="waterQualityData" style="width: 100%">
        <el-table-column prop="measurementDate" label="Date">
          <template #default="scope">
            {{ formatDate(scope.row.measurementDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="phValue" label="pH" />
        <el-table-column prop="dissolvedOxygen" label="DO (mg/L)" />
        <el-table-column prop="turbidity" label="Turbidity (NTU)" />
        <el-table-column prop="riskLevel" label="Risk Level">
          <template #default="scope">
            <el-tag :type="getRiskLevelType(scope.row.riskLevel)">
              {{ scope.row.riskLevel }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="inspector" label="Inspector" />
        <el-table-column label="Operations" width="120">
          <template #default="scope">
            <el-button-group>
              <el-button
                type="warning"
                :icon="Edit"
                @click="editWaterQualityData(scope.row)"
              />
              <el-button
                type="danger"
                :icon="Delete"
                @click="confirmDeleteData(scope.row)"
              />
            </el-button-group>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <!-- Water Quality Data Dialog -->
    <el-dialog
      :title="dataDialogTitle"
      v-model="dataDialogVisible"
      width="50%"
    >
      <el-form
        ref="dataForm"
        :model="dataForm"
        :rules="dataRules"
        label-width="140px"
      >
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
          <el-button @click="dataDialogVisible = false">Cancel</el-button>
          <el-button type="primary" @click="handleDataSubmit">
            {{ isEditData ? 'Update' : 'Add' }}
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted, computed } from 'vue'
import { useStore } from 'vuex'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Edit, Delete, Plus } from '@element-plus/icons-vue'

export default {
  name: 'ReservoirDetail',
  components: {
    Edit,
    Delete,
    Plus
  },
  setup() {
    const store = useStore()
    const router = useRouter()
    const route = useRoute()
    const loading = ref(false)
    const dataDialogVisible = ref(false)
    const isEditData = ref(false)
    const dataFormRef = ref(null)
    
    const reservoirId = computed(() => route.params.id)
    const reservoir = computed(() => store.state.reservoir.currentReservoir)
    
    const formData = reactive({
      measurementDate: null,
      phValue: null,
      dissolvedOxygen: null,
      turbidity: null,
      inspector: '',
      notes: ''
    })
    
    const dataRules = {
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
    
    const loadData = async () => {
      loading.value = true
      try {
        await store.dispatch('reservoir/fetchReservoirById', reservoirId.value)
        await store.dispatch('waterQuality/fetchWaterQualityDataByReservoir', reservoirId.value)
      } catch (error) {
        ElMessage.error('Failed to load reservoir data')
      } finally {
        loading.value = false
      }
    }
    
    const editReservoir = () => {
      router.push(`/reservoirs/${reservoirId.value}/edit`)
    }
    
    const confirmDelete = () => {
      ElMessageBox.confirm(
        `Are you sure to delete reservoir "${reservoir.value?.reservoirName}"?`,
        'Warning',
        {
          confirmButtonText: 'Delete',
          cancelButtonText: 'Cancel',
          type: 'warning'
        }
      ).then(async () => {
        try {
          await store.dispatch('reservoir/deleteReservoir', reservoirId.value)
          ElMessage.success('Reservoir deleted successfully')
          router.push('/reservoirs')
        } catch (error) {
          ElMessage.error('Failed to delete reservoir')
        }
      })
    }
    
    const showAddDataDialog = () => {
      isEditData.value = false
      Object.keys(formData).forEach(key => {
        formData[key] = null
      })
      dataDialogVisible.value = true
    }
    
    const editWaterQualityData = (data) => {
      isEditData.value = true
      Object.keys(formData).forEach(key => {
        formData[key] = data[key]
      })
      dataDialogVisible.value = true
    }
    
    const handleDataSubmit = async () => {
      if (!dataFormRef.value) return
      
      try {
        await dataFormRef.value.validate()
        
        const data = {
          ...formData,
          reservoirId: reservoirId.value
        }
        
        if (isEditData.value) {
          await store.dispatch('waterQuality/updateWaterQualityData', {
            dataId: data.dataId,
            data
          })
          ElMessage.success('Water quality data updated successfully')
        } else {
          await store.dispatch('waterQuality/createWaterQualityData', data)
          ElMessage.success('Water quality data added successfully')
        }
        
        dataDialogVisible.value = false
        loadData()
      } catch (error) {
        ElMessage.error(error.toString())
      }
    }
    
    const confirmDeleteData = (data) => {
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
          await store.dispatch('waterQuality/deleteWaterQualityData', data.dataId)
          ElMessage.success('Record deleted successfully')
          loadData()
        } catch (error) {
          ElMessage.error('Failed to delete record')
        }
      })
    }
    
    const formatNumber = (value) => {
      return value ? value.toLocaleString() : '0'
    }
    
    const formatDate = (date) => {
      return new Date(date).toLocaleString()
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
      dataDialogVisible,
      dataDialogTitle: computed(() => isEditData.value ? 'Edit Record' : 'Add Record'),
      isEditData,
      dataFormRef,
      dataForm: formData,
      dataRules,
      reservoir,
      waterQualityData: computed(() => store.state.waterQuality.waterQualityData),
      editReservoir,
      confirmDelete,
      showAddDataDialog,
      editWaterQualityData,
      confirmDeleteData,
      handleDataSubmit,
      formatDate,
      formatNumber,
      getStatusType,
      getRiskLevelType
    }
  }
}
</script>

<style scoped>
.reservoir-detail-container {
  padding: 20px;
}

.detail-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h3 {
  margin: 0;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

:deep(.el-descriptions) {
  margin: 20px 0;
}

:deep(.el-button-group) {
  display: flex;
  gap: 5px;
}
</style> 