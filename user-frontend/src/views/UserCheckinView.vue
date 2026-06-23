<template>
  <section class="workspace-page">
    <div class="workspace-hero apple-hero">
      <div>
        <span class="eyebrow">Daily Check-in</span>
        <h2>每日打卡与记录检索</h2>
        <p>把睡眠、运动、压力和备注按天记录下来，形成更可回看的健康过程数据。</p>
      </div>
      <div class="workspace-toolbar">
        <button class="ghost-btn" @click="router.push('/user/center/overview')">返回健康总览</button>
        <button class="primary-btn" @click="loadData">刷新记录</button>
      </div>
    </div>

    <div v-if="message" class="inline-feedback success">{{ message }}</div>
    <div v-if="loadError" class="inline-feedback error">{{ loadError }}</div>

    <div class="workspace-section-grid">
      <section class="glass-panel">
        <div class="panel-head">
          <div>
            <span class="eyebrow">Check-in Form</span>
            <h3>今日打卡</h3>
          </div>
        </div>
        <form class="form-grid" @submit.prevent="submitCheckin">
          <label><span>睡眠小时</span><input v-model.number="checkinForm.sleepHours" type="number" step="0.1" placeholder="7.5" /></label>
          <label><span>运动分钟</span><input v-model.number="checkinForm.exerciseMinutes" type="number" placeholder="45" /></label>
          <label><span>压力值（1-10）</span><input v-model.number="checkinForm.stressLevel" type="number" min="1" max="10" /></label>
          <label><span>情绪值（1-10）</span><input v-model.number="checkinForm.moodScore" type="number" min="1" max="10" /></label>
          <label><span>今日体重</span><input v-model.number="checkinForm.weight" type="number" step="0.1" /></label>
          <label><span>备注</span><textarea v-model.trim="checkinForm.remark" rows="4" placeholder="记录今天的状态、触发因素或执行感受"></textarea></label>
          <button class="primary-btn" :disabled="checkinLoading">{{ checkinLoading ? '提交中...' : '完成今日打卡' }}</button>
        </form>
      </section>

      <section class="glass-panel">
        <div class="panel-head">
          <div>
            <span class="eyebrow">History Lookup</span>
            <h3>按日期查看记录</h3>
          </div>
        </div>
        <div class="checkin-filter">
          <input v-model="selectedCheckinDate" type="date" />
          <button class="ghost-btn small-btn" @click="findCheckinByDate">查找</button>
        </div>
        <div v-if="selectedCheckin" class="checkin-detail">
          <strong>{{ selectedCheckin.checkinDate }}</strong>
          <p>睡眠：{{ selectedCheckin.sleepHours || '-' }} 小时</p>
          <p>运动：{{ selectedCheckin.exerciseMinutes || '-' }} 分钟</p>
          <p>压力：{{ selectedCheckin.stressLevel || '-' }}</p>
          <p>情绪：{{ selectedCheckin.moodScore || '-' }}</p>
          <p>体重：{{ selectedCheckin.weight || '-' }}</p>
          <p>备注：{{ selectedCheckin.remark || '无' }}</p>
        </div>
        <p v-else class="muted-copy trend-copy">选择某一天后，可以查看当日打卡详情与备注。</p>
      </section>

      <section class="glass-panel section-span-2">
        <div class="panel-head">
          <div>
            <span class="eyebrow">Recent Records</span>
            <h3>最近打卡记录</h3>
          </div>
        </div>
        <div class="list-block">
          <article v-for="item in checkins.slice(0, 10)" :key="item.id" class="record-card">
            <div class="record-head">
              <div>
                <h3>{{ item.checkinDate }}</h3>
                <p>{{ item.planTitle || '日常健康打卡' }}</p>
              </div>
              <span class="pill">{{ item.sleepHours || '-' }}h</span>
            </div>
            <p>运动 {{ item.exerciseMinutes || '-' }} 分钟 / 压力 {{ item.stressLevel || '-' }} / 情绪 {{ item.moodScore || '-' }}</p>
            <p>{{ item.remark || '暂无备注。' }}</p>
          </article>
          <p v-if="!checkins.length" class="muted-copy trend-copy">当前还没有打卡记录，今天开始留下一条吧。</p>
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
const dashboard = ref(null)
const checkins = ref([])
const selectedCheckinDate = ref('')
const selectedCheckin = ref(null)
const checkinLoading = ref(false)
const loadError = ref('')
const message = ref('')

const checkinForm = reactive({
  sleepHours: null,
  exerciseMinutes: null,
  stressLevel: null,
  moodScore: null,
  weight: null,
  remark: ''
})

async function loadData() {
  if (!user.value?.id) {
    router.replace('/user/auth')
    return
  }
  loadError.value = ''
  try {
    const [dashboardData, checkinData] = await Promise.all([
      api.getUserDashboard(user.value.id, 'week'),
      api.getCheckins(user.value.id)
    ])
    dashboard.value = dashboardData
    checkins.value = checkinData || []
    if (selectedCheckinDate.value) {
      findCheckinByDate()
    }
  } catch (error) {
    loadError.value = error.message || '打卡数据加载失败。'
  }
}

async function submitCheckin() {
  if (!user.value?.id) return
  checkinLoading.value = true
  message.value = ''
  try {
    await api.saveCheckin({
      userId: user.value.id,
      planTitle: dashboard.value?.favorites?.[0]?.planTitle || dashboard.value?.summary?.recentResult?.constitutionType || '日常康养打卡',
      ...checkinForm
    })
    Object.assign(checkinForm, {
      sleepHours: null,
      exerciseMinutes: null,
      stressLevel: null,
      moodScore: null,
      weight: null,
      remark: ''
    })
    message.value = '今日打卡已保存。'
    await loadData()
  } finally {
    checkinLoading.value = false
  }
}

function findCheckinByDate() {
  if (!selectedCheckinDate.value) {
    selectedCheckin.value = null
    return
  }
  selectedCheckin.value = checkins.value.find((item) => item.checkinDate === selectedCheckinDate.value) || null
}

onMounted(async () => {
  user.value = session.getUser()
  if (!user.value) {
    router.replace('/user/auth')
    return
  }
  await loadData()
})
</script>
