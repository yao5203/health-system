<template>
  <section class="workspace-page">
    <div class="workspace-hero apple-hero">
      <div>
        <span class="eyebrow">Feedback Center</span>
        <h2>意见反馈与优化建议</h2>
        <p>把使用过程中的困惑、建议和体验问题沉淀下来，帮助系统持续优化。</p>
      </div>
      <div class="workspace-toolbar">
        <button class="ghost-btn" @click="router.push('/user/center/overview')">返回健康总览</button>
        <button class="primary-btn" @click="loadFeedbacks">刷新反馈</button>
      </div>
    </div>

    <div v-if="message" class="inline-feedback success">{{ message }}</div>
    <div v-if="loadError" class="inline-feedback error">{{ loadError }}</div>

    <div class="workspace-section-grid">
      <section class="glass-panel">
        <div class="panel-head">
          <div>
            <span class="eyebrow">Submit</span>
            <h3>提交新的反馈</h3>
          </div>
        </div>
        <form class="form-grid" @submit.prevent="submitFeedback">
          <label>
            <span>使用反馈与建议</span>
            <textarea v-model.trim="feedbackForm.content" rows="8" placeholder="例如：某个页面信息过多、某个按钮不明显、某个功能还可以增强"></textarea>
          </label>
          <button class="primary-btn" :disabled="feedbackLoading || !feedbackForm.content.trim()">
            {{ feedbackLoading ? '提交中...' : '提交反馈' }}
          </button>
        </form>
      </section>

      <section class="glass-panel">
        <div class="panel-head">
          <div>
            <span class="eyebrow">Guidance</span>
            <h3>建议怎么写更有效</h3>
          </div>
        </div>
        <div class="visual-copy-list">
          <div class="visual-copy-item">
            <label>问题场景</label>
            <p>描述你在什么页面、什么操作下遇到了困惑或不顺畅。</p>
          </div>
          <div class="visual-copy-item">
            <label>体验感受</label>
            <p>写出哪里让你觉得不清楚、突兀或不稳定，方便后续精准优化。</p>
          </div>
          <div class="visual-copy-item">
            <label>期望改进</label>
            <p>如果你已经有更理想的交互方式，也可以直接写出来。</p>
          </div>
        </div>
      </section>

      <section class="glass-panel section-span-2">
        <div class="panel-head">
          <div>
            <span class="eyebrow">History</span>
            <h3>最近反馈记录</h3>
          </div>
        </div>
        <div class="list-block">
          <article v-for="item in feedbacks" :key="item.id" class="record-card">
            <div class="record-head">
              <div>
                <h3>反馈记录 #{{ item.id }}</h3>
                <p>{{ formatDate(item.createTime) }}</p>
              </div>
              <span class="pill">用户反馈</span>
            </div>
            <p>{{ item.content }}</p>
          </article>
          <p v-if="!feedbacks.length" class="muted-copy trend-copy">当前还没有反馈记录，欢迎提交第一条建议。</p>
        </div>
      </section>
    </div>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '../api'
import { session } from '../session'

const router = useRouter()
const user = ref(null)
const feedbacks = ref([])
const loadError = ref('')
const feedbackLoading = ref(false)
const message = ref('')

const feedbackForm = reactive({
  content: ''
})

async function loadFeedbacks() {
  if (!user.value?.id) {
    router.replace('/user/auth')
    return
  }
  loadError.value = ''
  try {
    feedbacks.value = await api.getFeedbacks(user.value.id)
  } catch (error) {
    loadError.value = error.message || '反馈记录加载失败。'
  }
}

async function submitFeedback() {
  if (!user.value?.id || !feedbackForm.content.trim()) return
  feedbackLoading.value = true
  message.value = ''
  try {
    await api.saveFeedback({
      userId: user.value.id,
      content: feedbackForm.content.trim()
    })
    feedbackForm.content = ''
    message.value = '反馈已提交，感谢你的建议。'
    await loadFeedbacks()
  } finally {
    feedbackLoading.value = false
  }
}

function formatDate(value) {
  if (!value) return '最近'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

onMounted(async () => {
  user.value = session.getUser()
  if (!user.value) {
    router.replace('/user/auth')
    return
  }
  await loadFeedbacks()
})
</script>
