<template>
  <section class="dashboard-layout user-workspace-layout">
    <aside class="panel dashboard-side user-workspace-side">
      <div class="workspace-profile">
        <h2>{{ user?.username || '用户工作台' }}</h2>
      </div>

      <div class="nav-stack user-nav-stack">
        <RouterLink class="nav-btn user-nav-btn" active-class="active" :to="{ name: 'user-center-overview' }">
          健康总览
        </RouterLink>
        <RouterLink class="nav-btn user-nav-btn" active-class="active" :to="{ name: 'user-center-favorites' }">
          收藏方案
        </RouterLink>
        <RouterLink class="nav-btn user-nav-btn" active-class="active" :to="{ name: 'user-center-checkin' }">
          每日打卡
        </RouterLink>
        <RouterLink class="nav-btn user-nav-btn" active-class="active" :to="{ name: 'user-center-articles' }">
          健康资讯
        </RouterLink>
        <RouterLink class="nav-btn user-nav-btn" active-class="active" :to="{ name: 'user-center-consultation' }">
          医生咨询
        </RouterLink>
        <RouterLink class="nav-btn user-nav-btn" active-class="active" :to="{ name: 'user-center-feedback' }">
          意见反馈
        </RouterLink>
      </div>

      <div class="workspace-side-actions">
        <button class="primary-btn" @click="router.push('/user/health')">开始新一轮测评</button>
        <button v-if="hasRecentRecommendation" class="ghost-btn" @click="router.push('/user/recommendation')">查看最近康养方案</button>
        <button class="ghost-btn" @click="logout">退出登录</button>
      </div>
    </aside>

    <main class="content-stack user-workspace-main">
      <section class="panel dashboard-top user-workspace-top">
        <div>
          <h2>{{ route.meta.workspaceTitle || '用户工作台' }}</h2>
        </div>
      </section>

      <RouterView v-slot="{ Component, route: childRoute }">
        <Transition name="workspace-fade" mode="out-in">
          <component :is="Component" :key="childRoute.fullPath" />
        </Transition>
      </RouterView>
    </main>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { RouterLink, RouterView, useRoute, useRouter } from 'vue-router'
import { session } from '../session'

const router = useRouter()
const route = useRoute()
const user = ref(null)

const hasRecentRecommendation = computed(() => {
  return Boolean(session.getReport() || session.getRecommendation() || session.getSelectedFavorite())
})

function logout() {
  session.clearUserFlow()
  router.push('/user/auth')
}

onMounted(() => {
  user.value = session.getUser()
  if (!user.value) {
    router.replace('/user/auth')
  }
})
</script>
