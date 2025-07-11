<template>
  <div class="section-monitor-list">
    <el-card>
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
        <el-button type="primary" @click="openDialog(null)" style="float:right;">新增</el-button>
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
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="600px">
      <el-form :model="form" label-width="120px">
        <el-form-item label="监测点名称"><el-input v-model="form.monitorPointName" /></el-form-item>
        <el-form-item label="水库名称"><el-input v-model="form.reservoirName" /></el-form-item>
        <el-form-item label="年份"><el-input v-model.number="form.year" type="number" /></el-form-item>
        <el-form-item label="月份"><el-input v-model.number="form.month" type="number" /></el-form-item>
        <el-form-item label="氧气(mg/l)"><el-input v-model="form.oxygen" /></el-form-item>
        <el-form-item label="高锰酸盐(mg/l)"><el-input v-model="form.potassiumPermanganate" /></el-form-item>
        <el-form-item label="化学需氧量(mg/l)"><el-input v-model="form.cod" /></el-form-item>
        <el-form-item label="流量(m³/s)"><el-input v-model="form.flow" /></el-form-item>
        <el-form-item label="水深(m)"><el-input v-model="form.waterDepth" /></el-form-item>
        <el-form-item label="总氮(mg/l)"><el-input v-model="form.totalNitrogen" /></el-form-item>
        <el-form-item label="总磷(mg/l)"><el-input v-model="form.totalPhosphorus" /></el-form-item>
      </el-form>
      <template v-slot:footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { fetchSectionMonitorList, deleteSectionMonitor } from '@/api/sectionMonitor'
import request from '@/utils/request'
export default {
  name: 'SectionMonitorList',
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
      }
    }
  },
  created() {
    this.fetchList()
  },
  methods: {
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
      }
      this.dialogVisible = true
    },
    submitForm() {
      const isEdit = !!this.form.id
      const url = isEdit ? '/api/sectionMonitor/update' : '/api/sectionMonitor/create'
      request({
        url,
        method: 'post',
        data: this.form
      }).then(() => {
        this.$message.success(isEdit ? '修改成功' : '新增成功')
        this.dialogVisible = false
        this.fetchList()
      })
    },
    handleDelete(row) {
      this.$confirm('确定要删除该条数据吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deleteSectionMonitor(row.id).then(() => {
          this.$message.success('删除成功')
          this.fetchList()
        })
      }).catch(() => {})
    },
    handlePageChange(page) {
      this.query.pageNum = page
      this.fetchList()
    }
  }
}
</script>

<style scoped>
.section-monitor-list {
  padding: 20px;
}
.search-bar {
  margin-bottom: 10px;
}
</style> 