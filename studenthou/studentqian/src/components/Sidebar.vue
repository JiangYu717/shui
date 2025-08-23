<template>
  <el-menu
    :default-active="activeMenu"
    class="sidebar-menu"
    :collapse="isCollapse"
    background-color="#304156"
    text-color="#bfcbd9"
    active-text-color="#409EFF"
    router>
    <el-menu-item index="/waterSituation/list">
      <el-icon><WaterMeter /></el-icon>
      <span>水情管理</span>
    </el-menu-item>
    <el-menu-item index="/sectionMonitor/list">
      <el-icon><WaterMeter /></el-icon>
      <span>监测断面管理</span>
    </el-menu-item>
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
      // 根据当前路径确定激活的菜单项
      if (route.path.startsWith('/waterSituation')) {
        return '/waterSituation/list'
      } else if (route.path.startsWith('/sectionMonitor')) {
        return '/sectionMonitor/list'
      }
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