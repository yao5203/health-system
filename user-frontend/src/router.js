import { createRouter, createWebHistory } from 'vue-router'
import LandingView from './views/LandingView.vue'
import UserAuthView from './views/UserAuthView.vue'
import UserHealthView from './views/UserHealthView.vue'
import UserQuestionnaireView from './views/UserQuestionnaireView.vue'
import UserReportView from './views/UserReportView.vue'
import UserRecommendationView from './views/UserRecommendationView.vue'
import UserWorkspaceLayout from './views/UserWorkspaceLayout.vue'
import UserCenterView from './views/UserCenterView.vue'
import UserCheckinView from './views/UserCheckinView.vue'
import UserArticleView from './views/UserArticleView.vue'
import UserConsultationView from './views/UserConsultationView.vue'
import UserFeedbackView from './views/UserFeedbackView.vue'
import UserFavoritesView from './views/UserFavoritesView.vue'
import DoctorAuthView from './views/DoctorAuthView.vue'
import DoctorDashboardView from './views/DoctorDashboardView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', name: 'landing', component: LandingView },
    { path: '/user/auth', name: 'user-auth', component: UserAuthView },
    { path: '/user/health', name: 'user-health', component: UserHealthView },
    { path: '/user/questionnaire', name: 'user-questionnaire', component: UserQuestionnaireView },
    { path: '/user/report', name: 'user-report', component: UserReportView },
    { path: '/user/recommendation', name: 'user-recommendation', component: UserRecommendationView },
    {
      path: '/user/center',
      component: UserWorkspaceLayout,
      children: [
        { path: '', redirect: { name: 'user-center-overview' } },
        { path: 'overview', name: 'user-center-overview', component: UserCenterView, meta: { workspaceTitle: '健康总览' } },
        { path: 'favorites', name: 'user-center-favorites', component: UserFavoritesView, meta: { workspaceTitle: '收藏方案' } },
        { path: 'checkin', name: 'user-center-checkin', component: UserCheckinView, meta: { workspaceTitle: '每日打卡' } },
        { path: 'articles', name: 'user-center-articles', component: UserArticleView, meta: { workspaceTitle: '健康资讯' } },
        { path: 'consultation', name: 'user-center-consultation', component: UserConsultationView, meta: { workspaceTitle: '医生咨询' } },
        { path: 'feedback', name: 'user-center-feedback', component: UserFeedbackView, meta: { workspaceTitle: '意见反馈' } }
      ]
    },
    { path: '/user/articles', redirect: { name: 'user-center-articles' } },
    { path: '/doctor/auth', name: 'doctor-auth', component: DoctorAuthView },
    { path: '/doctor/dashboard', name: 'doctor-dashboard', component: DoctorDashboardView }
  ]
})

export default router
