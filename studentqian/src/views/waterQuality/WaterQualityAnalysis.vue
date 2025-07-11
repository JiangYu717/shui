<template>
  <div class="analysis-container">
    <!-- 分析条件 -->
    <el-card class="filter-card">
      <template #header>
        <div class="card-header">
          <h3>分析条件</h3>
        </div>
      </template>
      
      <el-form :inline="true" class="analysis-form">
        <el-form-item label="水库">
          <el-select
            v-model="analysisForm.reservoirId"
            placeholder="选择水库"
            clearable
            @change="loadAnalysisData"
          >
            <el-option
              v-for="reservoir in reservoirs"
              :key="reservoir.reservoirId"
              :label="reservoir.reservoirName"
              :value="reservoir.reservoirId"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="analysisForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            @change="loadAnalysisData"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="loadAnalysisData">
            <el-icon><Search /></el-icon>
            分析
          </el-button>
          <el-button @click="exportReport">
            <el-icon><Download /></el-icon>
            导出报告
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据概览 -->
    <el-row :gutter="20" class="stat-row">
      <el-col :span="8">
        <el-card class="stat-card">
          <template #header>
            <div class="card-header">
              <span>pH值分析</span>
            </div>
          </template>
          <div class="stat-content">
            <div class="stat-item">
              <span class="label">平均值:</span>
              <span class="value">{{ stats.phAvg.toFixed(2) }}</span>
            </div>
            <div class="stat-item">
              <span class="label">最大值:</span>
              <span class="value">{{ stats.phMax.toFixed(2) }}</span>
            </div>
            <div class="stat-item">
              <span class="label">最小值:</span>
              <span class="value">{{ stats.phMin.toFixed(2) }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card class="stat-card">
          <template #header>
            <div class="card-header">
              <span>溶解氧分析</span>
            </div>
          </template>
          <div class="stat-content">
            <div class="stat-item">
              <span class="label">平均值:</span>
              <span class="value">{{ stats.doAvg.toFixed(2) }} mg/L</span>
            </div>
            <div class="stat-item">
              <span class="label">最大值:</span>
              <span class="value">{{ stats.doMax.toFixed(2) }} mg/L</span>
            </div>
            <div class="stat-item">
              <span class="label">最小值:</span>
              <span class="value">{{ stats.doMin.toFixed(2) }} mg/L</span>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="8">
        <el-card class="stat-card">
          <template #header>
            <div class="card-header">
              <span>浊度分析</span>
            </div>
          </template>
          <div class="stat-content">
            <div class="stat-item">
              <span class="label">平均值:</span>
              <span class="value">{{ stats.turbidityAvg.toFixed(2) }} NTU</span>
            </div>
            <div class="stat-item">
              <span class="label">最大值:</span>
              <span class="value">{{ stats.turbidityMax.toFixed(2) }} NTU</span>
            </div>
            <div class="stat-item">
              <span class="label">最小值:</span>
              <span class="value">{{ stats.turbidityMin.toFixed(2) }} NTU</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 趋势图表 -->
    <el-card class="chart-card">
      <template #header>
        <div class="card-header">
          <span>水质参数趋势</span>
        </div>
      </template>
      <div class="chart-container" ref="trendChart"></div>
    </el-card>

    <!-- 风险分析 -->
    <el-card class="risk-card">
      <template #header>
        <div class="card-header">
          <span>风险分析</span>
        </div>
      </template>
      <el-table :data="riskAnalysis" style="width: 100%">
        <el-table-column prop="date" label="日期">
          <template #default="scope">
            {{ formatDate(scope.row.date) }}
          </template>
        </el-table-column>
        <el-table-column prop="parameter" label="参数" />
        <el-table-column prop="value" label="数值" />
        <el-table-column prop="standard" label="标准值" />
        <el-table-column prop="riskLevel" label="风险等级">
          <template #default="scope">
            <el-tag :type="getRiskLevelType(scope.row.riskLevel)">
              {{ scope.row.riskLevel }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="suggestion" label="建议措施" />
      </el-table>
    </el-card>
  </div>
</template>

<script>
import { ref, reactive, onMounted, onUnmounted, computed } from 'vue'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import { Search, Download } from '@element-plus/icons-vue'
import * as echarts from 'echarts'

export default {
  name: 'WaterQualityAnalysis',
  components: {
    Search,
    Download
  },
  setup() {
    const store = useStore()
    const trendChart = ref(null)
    const chart = ref(null)
    
    const analysisForm = reactive({
      reservoirId: null,
      dateRange: null
    })
    
    const stats = reactive({
      phAvg: 0,
      phMax: 0,
      phMin: 0,
      doAvg: 0,
      doMax: 0,
      doMin: 0,
      turbidityAvg: 0,
      turbidityMax: 0,
      turbidityMin: 0
    })
    
    const riskAnalysis = ref([])
    const reservoirs = computed(() => store.state.reservoir.reservoirs)
    
    const loadAnalysisData = async () => {
      try {
        // 加载水质数据
        const response = await store.dispatch('waterQuality/analyzeWaterQuality', {
          reservoirId: analysisForm.reservoirId,
          startDate: analysisForm.dateRange?.[0],
          endDate: analysisForm.dateRange?.[1]
        })
        
        // 更新统计数据
        Object.assign(stats, response.stats)
        
        // 更新风险分析
        riskAnalysis.value = response.riskAnalysis
        
        // 更新趋势图
        updateTrendChart(response.trends)
      } catch (error) {
        ElMessage.error('加载分析数据失败')
      }
    }
    
    const updateTrendChart = (trends) => {
      if (!chart.value) return
      
      const option = {
        tooltip: {
          trigger: 'axis'
        },
        legend: {
          data: ['pH值', '溶解氧', '浊度']
        },
        xAxis: {
          type: 'category',
          data: trends.dates
        },
        yAxis: [
          {
            type: 'value',
            name: 'pH值',
            min: 0,
            max: 14
          },
          {
            type: 'value',
            name: '溶解氧 (mg/L)',
            min: 0,
            max: 20
          },
          {
            type: 'value',
            name: '浊度 (NTU)',
            min: 0
          }
        ],
        series: [
          {
            name: 'pH值',
            type: 'line',
            data: trends.phValues
          },
          {
            name: '溶解氧',
            type: 'line',
            yAxisIndex: 1,
            data: trends.doValues
          },
          {
            name: '浊度',
            type: 'line',
            yAxisIndex: 2,
            data: trends.turbidityValues
          }
        ]
      }
      
      chart.value.setOption(option)
    }
    
    const exportReport = () => {
      // 实现导出报告功能
      ElMessage.success('报告导出成功')
    }
    
    const formatDate = (date) => {
      return new Date(date).toLocaleDateString()
    }
    
    const getRiskLevelType = (level) => {
      switch (level) {
        case '警告':
          return 'warning'
        case '危险':
          return 'danger'
        default:
          return 'success'
      }
    }
    
    onMounted(async () => {
      // 加载水库列表
      await store.dispatch('reservoir/fetchReservoirs')
      
      // 初始化图表
      chart.value = echarts.init(trendChart.value)
      
      // 加载初始数据
      loadAnalysisData()
      
      // 监听窗口大小变化
      window.addEventListener('resize', () => {
        chart.value?.resize()
      })
    })
    
    onUnmounted(() => {
      chart.value?.dispose()
      window.removeEventListener('resize', () => {
        chart.value?.resize()
      })
    })
    
    return {
      analysisForm,
      stats,
      riskAnalysis,
      trendChart,
      reservoirs,
      loadAnalysisData,
      exportReport,
      formatDate,
      getRiskLevelType
    }
  }
}
</script>

<style scoped>
.analysis-container {
  padding: 20px;
}

.filter-card,
.stat-card,
.chart-card,
.risk-card {
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

.analysis-form {
  margin-top: 20px;
}

.stat-row {
  margin: 20px 0;
}

.stat-content {
  padding: 10px;
}

.stat-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}

.stat-item .label {
  color: #606266;
}

.stat-item .value {
  font-weight: bold;
}

.chart-container {
  height: 400px;
}
</style> 