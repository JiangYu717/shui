<template>
  <el-menu
    :default-active="activeMenu"
    class="sidebar-menu"
    :collapse="isCollapse"
    background-color="#304156"
    text-color="#bfcbd9"
    active-text-color="#409EFF"
    router>
    <el-sub-menu index="/waterSituation">
      <template #title>
        <el-icon><WaterMeter /></el-icon>
        <span>水情管理</span>
      </template>
      <el-menu-item index="/waterSituation/list">数据管理</el-menu-item>
      <el-menu-item index="/waterSituation/import">数据导入</el-menu-item>
      <el-menu-item index="/waterSituation/export">数据导出</el-menu-item>
    </el-sub-menu>
    <el-sub-menu index="/sectionMonitor">
      <template #title>
        <el-icon><WaterMeter /></el-icon>
        <span>监测断面管理</span>
      </template>
      <el-menu-item index="/sectionMonitor/list">数据管理</el-menu-item>
      <el-menu-item index="/sectionMonitor/import">数据导入</el-menu-item>
      <el-menu-item index="/sectionMonitor/export">数据导出</el-menu-item>
    </el-sub-menu>
  </el-menu>
</template>

<script>
import { ref, computed } from 'vue'
import { useRoute } from 'vue-router'
import { WaterCup } from '@element-plus/icons-vue'

export default {
  name: 'SidebarMenu',
  setup() {
    const route = useRoute()
    const isCollapse = ref(false)

    const activeMenu = computed(() => {
      return route.path
    })

    return {
      isCollapse,
      activeMenu,
      WaterMeter: WaterCup
    }
  }
}
</script>

<style scoped>
.sidebar-menu {
  height: 100%;
  border-right: none;
}
.sidebar-menu:not(.el-menu--collapse) {
  width: 200px;
}
</style> 