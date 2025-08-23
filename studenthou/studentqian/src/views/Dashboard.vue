<template>
  <div class="dashboard">
    <div class="dashboard-header">
      <h2>水库数据概览</h2>
    </div>

    <!-- 数据卡片 -->
    <el-row :gutter="20" class="data-cards">
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>总水库数量</span>
            </div>
          </template>
          <div class="card-content">
            <span class="number">{{ statistics.totalCount }}</span>
            <span class="unit">个</span>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>正常运行</span>
            </div>
          </template>
          <div class="card-content">
            <span class="number success">{{ statistics.normalCount }}</span>
            <span class="unit">个</span>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>异常状态</span>
            </div>
          </template>
          <div class="card-content">
            <span class="number warning">{{ statistics.warningCount }}</span>
            <span class="unit">个</span>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>总库容量</span>
            </div>
          </template>
          <div class="card-content">
            <span class="number">{{ statistics.totalCapacity }}</span>
            <span class="unit">万m³</span>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="charts-container">
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>水库类型分布</span>
            </div>
          </template>
          <div class="chart" ref="pieChartRef"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>近期数据记录</span>
            </div>
          </template>
          <div class="chart" ref="lineChartRef"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { ref, onMounted, reactive, nextTick, onUnmounted } from 'vue'
import * as echarts from 'echarts'

export default {
  name: 'DashboardPage',
  setup() {
    // 统计数据
    const statistics = reactive({
      totalCount: 0,
      normalCount: 0,
      warningCount: 0,
      totalCapacity: 0
    })

    // 图表引用
    const pieChartRef = ref(null)
    const lineChartRef = ref(null)
    let pieChart = null
    let lineChart = null

    // 初始化饼图
    const initPieChart = () => {
      if (!pieChartRef.value) return
      
      // 如果已经初始化过，先销毁
      if (pieChart) {
        pieChart.dispose()
      }
      
      pieChart = echarts.init(pieChartRef.value)
      const option = {
        tooltip: {
          trigger: 'item',
          formatter: '{a} <br/>{b}: {c} ({d}%)'
        },
        legend: {
          orient: 'vertical',
          left: 'left'
        },
        series: [
          {
            name: '水库类型',
            type: 'pie',
            radius: ['50%', '70%'],
            avoidLabelOverlap: false,
            label: {
              show: false,
              position: 'center'
            },
            emphasis: {
              label: {
                show: true,
                fontSize: '16',
                fontWeight: 'bold'
              }
            },
            labelLine: {
              show: false
            },
            data: [
              { value: 35, name: '大型水库' },
              { value: 45, name: '中型水库' },
              { value: 20, name: '小型水库' }
            ]
          }
        ]
      }
      pieChart.setOption(option)
    }

    // 初始化折线图
    const initLineChart = () => {
      if (!lineChartRef.value) return
      
      // 如果已经初始化过，先销毁
      if (lineChart) {
        lineChart.dispose()
      }
      
      lineChart = echarts.init(lineChartRef.value)
      const option = {
        tooltip: {
          trigger: 'axis'
        },
        xAxis: {
          type: 'category',
          data: ['1月', '2月', '3月', '4月', '5月', '6月']
        },
        yAxis: {
          type: 'value',
          name: '数据记录数'
        },
        series: [
          {
            name: '记录数量',
            type: 'line',
            smooth: true,
            data: [120, 132, 101, 134, 90, 230]
          }
        ]
      }
      lineChart.setOption(option)
    }

    // 处理窗口大小变化
    const handleResize = () => {
      pieChart?.resize()
      lineChart?.resize()
    }

    // 获取统计数据
    const getStatistics = async () => {
      try {
        // TODO: 调用API获取实际数据
        // const res = await api.getStatistics()
        // Object.assign(statistics, res.data)
        
        // 模拟数据
        Object.assign(statistics, {
          totalCount: 100,
          normalCount: 85,
          warningCount: 15,
          totalCapacity: 50000
        })
      } catch (error) {
        console.error('获取统计数据失败:', error)
      }
    }

    // 页面加载时初始化
    onMounted(() => {
      getStatistics()
      // 使用 nextTick 确保 DOM 已经渲染
      nextTick(() => {
        initPieChart()
        initLineChart()
        // 添加窗口大小变化监听
        window.addEventListener('resize', handleResize)
      })
    })

    // 组件卸载时清理
    onUnmounted(() => {
      window.removeEventListener('resize', handleResize)
      pieChart?.dispose()
      lineChart?.dispose()
    })

    return {
      statistics,
      pieChartRef,
      lineChartRef
    }
  }
}
</script>

<style scoped>
.dashboard {
  padding: 20px;
}

.dashboard-header {
  margin-bottom: 20px;
}

.data-cards {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-content {
  text-align: center;
  padding: 10px 0;
}

.card-content .number {
  font-size: 24px;
  font-weight: bold;
  margin-right: 5px;
}

.card-content .number.success {
  color: #67c23a;
}

.card-content .number.warning {
  color: #e6a23c;
}

.card-content .unit {
  font-size: 14px;
  color: #909399;
}

.charts-container {
  margin-top: 20px;
}

.chart-card {
  margin-bottom: 20px;
}

.chart {
  height: 300px;
}
</style> 