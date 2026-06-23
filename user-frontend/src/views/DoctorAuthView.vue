<template>
  <section class="auth-layout">
    <section class="panel auth-card auth-card-centered">
      <h2>{{ mode === 'login' ? '医生登录' : '医生注册' }}</h2>
      <div class="tab-row">
        <button class="mini-tab" :class="{ active: mode === 'login' }" @click="mode = 'login'">
          登录
        </button>
        <button class="mini-tab" :class="{ active: mode === 'register' }" @click="mode = 'register'">
          注册
        </button>
      </div>

      <form v-if="mode === 'login'" class="form-grid" @submit.prevent="login">
        <label>
          <span>用户名</span>
          <input
            v-model.trim="loginForm.username"
            type="text"
            placeholder="请输入医生用户名"
          />
        </label>
        <label>
          <span>密码</span>
          <input
            v-model.trim="loginForm.password"
            type="password"
            placeholder="请输入密码"
          />
        </label>
        <button class="primary-btn" :disabled="loading.login">
          {{ loading.login ? '登录中...' : '登录' }}
        </button>
      </form>

      <form v-else class="form-grid" @submit.prevent="register">
        <label>
          <span>用户名</span>
          <input
            v-model.trim="registerForm.username"
            type="text"
            placeholder="请输入医生用户名"
          />
        </label>
        <label>
          <span>密码</span>
          <input
            v-model.trim="registerForm.password"
            type="password"
            placeholder="请输入密码"
          />
        </label>
        <label>
          <span>专业方向</span>
          <input
            v-model.trim="registerForm.specialty"
            type="text"
            placeholder="如：睡眠管理 / 营养调理 / 运动康复"
          />
        </label>
        <label>
          <span>擅长标签</span>
          <input
            v-model.trim="registerForm.expertiseTags"
            type="text"
            placeholder="逗号分隔，如：睡眠,压力,饮食"
          />
        </label>
        <label>
          <span>个人简介</span>
          <textarea
            v-model.trim="registerForm.introduction"
            rows="4"
            placeholder="简要说明你的专业背景与可提供的帮助"
          />
        </label>
        <button class="primary-btn" :disabled="loading.register">
          {{ loading.register ? '提交中...' : '注册' }}
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
const mode = ref('login')
const loading = reactive({ login: false, register: false })
const feedback = reactive({ text: '', type: 'success' })

const loginForm = reactive({ username: '', password: '' })
const registerForm = reactive({ username: '', password: '', specialty: '', expertiseTags: '', introduction: '' })

function setFeedback(text, type = 'success') {
  feedback.text = text
  feedback.type = type
}

async function login() {
  if (!loginForm.username || !loginForm.password) {
    setFeedback('请输入用户名和密码', 'error')
    return
  }

  loading.login = true
  try {
    const result = await api.doctorLogin(loginForm)
    session.setDoctor(result)
    session.setEntrySplash({
      targetPath: '/doctor/dashboard',
      userName: result?.username || loginForm.username,
      userType: 'doctor'
    })
    await router.push('/doctor/dashboard')
  } catch (error) {
    setFeedback(error.message || '登录失败，请稍后重试', 'error')
    loading.login = false
  }
}

async function register() {
  if (!registerForm.username || !registerForm.password) {
    setFeedback('请填写用户名和密码', 'error')
    return
  }

  loading.register = true
  try {
    const result = await api.doctorRegister(registerForm)
    setFeedback(result.message || '注册成功，请登录', 'success')
    registerForm.username = ''
    registerForm.password = ''
    registerForm.specialty = ''
    registerForm.expertiseTags = ''
    registerForm.introduction = ''
    mode.value = 'login'
  } catch (error) {
    setFeedback(error.message || '注册失败，请稍后重试', 'error')
  } finally {
    loading.register = false
  }
}
</script>
