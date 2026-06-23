<template>
  <section class="auth-layout">
    <section class="panel auth-card auth-card-centered">
      <h2>管理员登录</h2>
      <form class="form-grid" @submit.prevent="login">
        <label>
          <span>管理员账号</span>
          <input
            v-model.trim="form.username"
            type="text"
            placeholder="请输入系统管理员账号"
          />
        </label>
        <label>
          <span>密码</span>
          <input
            v-model.trim="form.password"
            type="password"
            placeholder="请输入密码"
          />
        </label>
        <button class="primary-btn" :disabled="loading">
          {{ loading ? '登录中...' : '登录' }}
        </button>
      </form>

      <div v-if="feedback.text" class="inline-feedback" :class="feedback.type">
        {{ feedback.text }}
      </div>
    </section>
  </section>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '../api'
import { session } from '../session'

const router = useRouter()
const loading = ref(false)
const form = reactive({ username: '', password: '' })
const feedback = reactive({ text: '', type: 'success' })

function setFeedback(text, type = 'success') {
  feedback.text = text
  feedback.type = type
}

async function login() {
  if (!form.username || !form.password) {
    setFeedback('请输入管理员账号和密码', 'error')
    return
  }
  loading.value = true
  try {
    const result = await api.adminLogin(form)
    session.setAdmin(result)
    session.setEntrySplash({
      targetPath: '/admin/dashboard',
      userName: result?.displayName || result?.username,
      userType: 'admin'
    })
    await router.push('/admin/dashboard')
  } catch (error) {
    setFeedback(error.message || '管理员登录失败', 'error')
  } finally {
    loading.value = false
  }
}
</script>
