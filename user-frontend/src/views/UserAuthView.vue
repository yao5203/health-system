<template>
  <section class="auth-layout">
    <section class="panel auth-card auth-card-centered">
      <h2>{{ mode === 'login' ? '用户登录' : '用户注册' }}</h2>
      <div class="tab-row">
        <button class="mini-tab" :class="{ active: mode === 'login' }" @click="mode = 'login'">登录</button>
        <button class="mini-tab" :class="{ active: mode === 'register' }" @click="mode = 'register'">注册</button>
      </div>

      <form v-if="mode === 'login'" class="form-grid" @submit.prevent="login">
        <label><span>用户名</span><input v-model.trim="loginForm.username" type="text" placeholder="请输入用户名" /></label>
        <label><span>密码</span><input v-model.trim="loginForm.password" type="password" placeholder="请输入密码" /></label>
        <button class="primary-btn" :disabled="loading.login">{{ loading.login ? '登录中...' : '登录' }}</button>
      </form>

      <form v-else class="form-grid" @submit.prevent="register">
        <label><span>用户名</span><input v-model.trim="registerForm.username" type="text" placeholder="请输入用户名" /></label>
        <label><span>密码</span><input v-model.trim="registerForm.password" type="password" placeholder="请输入密码" /></label>
        <label><span>年龄</span><input v-model.number="registerForm.age" type="number" min="1" placeholder="请输入年龄" /></label>
        <label>
          <span>性别</span>
          <select v-model="registerForm.gender">
            <option value="">请选择</option>
            <option value="男">男</option>
            <option value="女">女</option>
          </select>
        </label>
        <label><span>手机号</span><input v-model.trim="registerForm.phone" type="text" placeholder="请输入手机号" /></label>
        <button class="primary-btn" :disabled="loading.register">{{ loading.register ? '提交中...' : '注册' }}</button>
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

const loading = reactive({
  login: false,
  register: false
})

const feedback = reactive({
  text: '',
  type: 'success'
})

const loginForm = reactive({
  username: '',
  password: ''
})

const registerForm = reactive({
  username: '',
  password: '',
  age: null,
  gender: '',
  phone: ''
})

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
    const result = await api.userLogin(loginForm)
    session.clearUserFlow()
    session.setUser(result.user)
    session.setEntrySplash({
      targetPath: '/user/center/overview',
      userName: result.user?.username || loginForm.username,
      userType: 'user'
    })
    await router.push('/user/center/overview')
  } catch (error) {
    setFeedback(error.message, 'error')
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
    const result = await api.userRegister(registerForm)
    if (result.user) {
      session.clearUserFlow()
      session.setUser(result.user)
      session.setEntrySplash({
        targetPath: '/user/center/overview',
        userName: result.user?.username || registerForm.username,
        userType: 'user'
      })
      await router.push('/user/center/overview')
      return
    }
    setFeedback(result.message || '注册成功', 'success')
    mode.value = 'login'
  } catch (error) {
    setFeedback(error.message, 'error')
  } finally {
    loading.register = false
  }
}

</script>
