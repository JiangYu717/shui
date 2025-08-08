import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'Layout',
    component: () => import('../views/Layout.vue'),
    redirect: '/waterSituation/list',
    children: [
      {
        path: 'waterSituation/list',
        name: 'WaterSituationList',
        component: () => import('../views/waterSituation/List.vue')
      },
      {
        path: 'waterSituation/import',
        name: 'WaterSituationImport',
        component: () => import('../views/waterSituation/Import.vue')
      },
      {
        path: 'waterSituation/export',
        name: 'WaterSituationExport',
        component: () => import('../views/waterSituation/Export.vue')
      },
      {
        path: 'sectionMonitor/list',
        name: 'SectionMonitorList',
        component: () => import('../views/sectionMonitor/List.vue')
      },
      {
        path: 'sectionMonitor/import',
        name: 'SectionMonitorImport',
        component: () => import('../views/sectionMonitor/Import.vue')
      },
      {
        path: 'sectionMonitor/export',
        name: 'SectionMonitorExport',
        component: () => import('../views/sectionMonitor/Export.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router 