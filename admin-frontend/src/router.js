import { createRouter, createWebHistory } from 'vue-router'
import AdminAuthView from './views/AdminAuthView.vue'
import AdminDashboardView from './views/AdminDashboardView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/admin/auth' },
    { path: '/admin/auth', name: 'admin-auth', component: AdminAuthView },
    { path: '/admin/dashboard', name: 'admin-dashboard', component: AdminDashboardView }
  ]
})

export default router
