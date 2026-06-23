<template>
  <div class="app-shell">
    <header class="site-header">
      <RouterLink class="brand-block" to="/">
        <span class="eyebrow">Health System</span>
        <h1>人体健康评测与康养系统</h1>
      </RouterLink>
      <nav class="switch-group">
        <RouterLink class="switch-btn" to="/user/auth">用户入口</RouterLink>
        <RouterLink class="switch-btn" to="/doctor/auth">医生入口</RouterLink>
      </nav>
    </header>

    <main class="route-shell">
      <div v-if="routeError" class="panel app-error-panel">
        <span class="eyebrow">Runtime Error</span>
        <h2>页面加载失败</h2>
        <p>{{ routeError }}</p>
        <div class="action-row">
          <button class="primary-btn" @click="resetRouteError">重试当前页面</button>
          <RouterLink class="ghost-btn" to="/">返回首页</RouterLink>
        </div>
      </div>
      <RouterView v-else v-slot="{ Component, route }">
        <Transition name="page-slide" mode="out-in">
          <component :is="Component" :key="route.fullPath" />
        </Transition>
      </RouterView>
    </main>

    <SplashScreen
      :visible="showSplash"
      :is-exiting="isSplashExiting"
      :user-name="splashUserName"
      :user-type="splashUserType"
    />
  </div>
</template>

<script setup>
import { onBeforeUnmount, onErrorCaptured, ref, watch } from 'vue'
import { RouterLink, RouterView, useRoute } from 'vue-router'
import SplashScreen from './components/SplashScreen.vue'
import { session } from './session'

const route = useRoute()
const showSplash = ref(false)
const isSplashExiting = ref(false)
const splashUserName = ref('')
const splashUserType = ref('user')
const routeError = ref('')

let enterTimer = null
let exitTimer = null

function clearTimers() {
  if (enterTimer) {
    clearTimeout(enterTimer)
    enterTimer = null
  }
  if (exitTimer) {
    clearTimeout(exitTimer)
    exitTimer = null
  }
}

function playEntrySplashIfNeeded(currentPath) {
  const payload = session.getEntrySplash()
  if (!payload || payload.targetPath !== currentPath || showSplash.value) {
    return
  }

  splashUserName.value = payload.userName || '访客'
  splashUserType.value = payload.userType || 'user'
  showSplash.value = true
  session.setEntrySplash(null)

  enterTimer = setTimeout(() => {
    isSplashExiting.value = true
    exitTimer = setTimeout(() => {
      showSplash.value = false
      isSplashExiting.value = false
    }, 450)
  }, 1400)
}

watch(
  () => route.fullPath,
  (currentPath) => {
    clearTimers()
    routeError.value = ''
    playEntrySplashIfNeeded(currentPath)
  },
  { immediate: true }
)

function resetRouteError() {
  routeError.value = ''
}

onErrorCaptured((error) => {
  routeError.value = error?.message || '页面发生未知错误。'
  return false
})

onBeforeUnmount(() => {
  clearTimers()
})
</script>
