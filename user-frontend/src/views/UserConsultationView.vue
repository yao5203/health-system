<template>
  <section class="workspace-page">
    <div class="workspace-hero apple-hero">
      <div>
        <span class="eyebrow">Consultation</span>
        <h2>医生咨询</h2>
        <p>提交你的睡眠、饮食、运动或情绪困扰，由系统管理员匹配合适医生，再进入一对一对话。</p>
      </div>
    </div>

    <div v-if="errorText" class="inline-feedback error">{{ errorText }}</div>

    <div class="workspace-section-grid">
      <section class="glass-panel">
        <div class="panel-head">
          <h3>发起咨询申请</h3>
        </div>
        <form class="form-grid" @submit.prevent="submitConsultation">
          <label>
            <span>问题类型</span>
            <select v-model="form.issueType">
              <option value="睡眠问题">睡眠问题</option>
              <option value="饮食问题">饮食问题</option>
              <option value="运动问题">运动问题</option>
              <option value="压力情绪">压力情绪</option>
              <option value="综合调理">综合调理</option>
            </select>
          </label>
          <label>
            <span>咨询标题</span>
            <input v-model.trim="form.title" type="text" placeholder="如：最近夜间易醒，想改善睡眠" />
          </label>
          <label>
            <span>希望匹配方向</span>
            <input v-model.trim="form.preferredTag" type="text" placeholder="如：睡眠、营养、压力管理" />
          </label>
          <label>
            <span>详细说明</span>
            <textarea v-model.trim="form.detail" rows="5" placeholder="描述你的情况、持续时间、你最想解决的问题..."></textarea>
          </label>
          <button class="primary-btn" :disabled="submitting">
            {{ submitting ? '提交中...' : '提交咨询申请' }}
          </button>
        </form>
      </section>

      <section class="glass-panel section-span-2">
        <div class="panel-head">
          <h3>我的咨询记录</h3>
          <button class="ghost-btn small-btn" @click="loadConsultations">刷新</button>
        </div>
        <div class="list-block">
          <article
            v-for="item in consultations"
            :key="item.id"
            class="record-card"
            :class="{ 'record-card-active': selectedConsultation?.id === item.id }"
            @click="selectConsultation(item)"
          >
            <div class="record-head">
              <div>
                <h3>{{ item.title }}</h3>
                <p>{{ item.issueType }} / {{ item.status }}</p>
              </div>
              <span class="pill">{{ item.doctorName || '待匹配医生' }}</span>
            </div>
            <p>{{ item.detail || '暂无补充说明' }}</p>
          </article>
          <p v-if="!consultations.length" class="muted-copy trend-copy">你还没有发起过咨询申请。</p>
        </div>
      </section>

      <section class="glass-panel section-span-3" v-if="selectedConsultation">
        <div class="panel-head">
          <div>
            <span class="eyebrow">Conversation</span>
            <h3>{{ selectedConsultation.title }}</h3>
          </div>
          <span class="pill">{{ selectedConsultation.doctorName || '待匹配' }}</span>
        </div>

        <div class="visual-copy-list">
          <div class="visual-copy-item">
            <label>当前状态</label>
            <strong>{{ selectedConsultation.status }}</strong>
            <p>{{ selectedConsultation.adminNote || '系统管理员尚未补充匹配说明。' }}</p>
          </div>
        </div>

        <div class="chat-thread">
          <div
            v-for="message in selectedConsultation.messages || []"
            :key="message.id"
            class="chat-bubble"
            :class="message.senderType === 'USER' ? 'is-self' : 'is-other'"
          >
            <strong>{{ message.senderName || message.senderType }}</strong>
            <p>{{ message.content }}</p>
          </div>
        </div>

        <form class="chat-input-row" @submit.prevent="sendMessage">
          <input v-model.trim="messageText" type="text" placeholder="继续补充情况，或等待医生回复后继续交流" />
          <button class="primary-btn" :disabled="!selectedConsultation">发送</button>
        </form>
      </section>
    </div>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { api } from '../api'
import { session } from '../session'

const user = ref(null)
const consultations = ref([])
const selectedConsultation = ref(null)
const submitting = ref(false)
const errorText = ref('')
const messageText = ref('')

const form = reactive({
  issueType: '睡眠问题',
  title: '',
  preferredTag: '',
  detail: ''
})

async function loadConsultations() {
  if (!user.value?.id) return
  errorText.value = ''
  try {
    consultations.value = await api.getUserConsultations(user.value.id)
    if (selectedConsultation.value) {
      selectedConsultation.value = consultations.value.find((item) => item.id === selectedConsultation.value.id) || consultations.value[0] || null
    } else {
      selectedConsultation.value = consultations.value[0] || null
    }
  } catch (error) {
    errorText.value = error.message || '咨询记录加载失败'
  }
}

async function submitConsultation() {
  if (!user.value?.id) return
  submitting.value = true
  try {
    await api.applyConsultation({
      userId: user.value.id,
      issueType: form.issueType,
      title: form.title,
      detail: form.detail,
      preferredTag: form.preferredTag
    })
    form.title = ''
    form.detail = ''
    form.preferredTag = ''
    await loadConsultations()
  } catch (error) {
    errorText.value = error.message || '咨询申请提交失败'
  } finally {
    submitting.value = false
  }
}

function selectConsultation(item) {
  selectedConsultation.value = item
}

async function sendMessage() {
  if (!selectedConsultation.value || !messageText.value) return
  try {
    await api.sendConsultationMessage(selectedConsultation.value.id, {
      senderType: 'USER',
      senderId: user.value.id,
      senderName: user.value.username,
      content: messageText.value
    }, 'user')
    messageText.value = ''
    await loadConsultations()
  } catch (error) {
    errorText.value = error.message || '消息发送失败'
  }
}

onMounted(async () => {
  user.value = session.getUser()
  await loadConsultations()
})
</script>
