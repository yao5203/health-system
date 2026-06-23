<template>
  <section class="page-grid">
    <aside class="panel intro-card">
      <p class="eyebrow">User Portal</p>
      <h2>用户健康评测中心</h2>
      <p>先录入硬性健康指标得到初步评估，再由系统从题库中动态推荐更贴合当前状态的问卷，最后融合生成健康评测结果与康养建议。</p>
      <div class="info-list">
        <div>用户注册与登录</div>
        <div>硬性指标初评</div>
        <div>动态推荐 30 题问卷</div>
        <div>融合评测与康养方案推送</div>
      </div>
    </aside>

    <main class="content-stack">
      <section class="panel">
        <div class="tab-row">
          <button class="mini-tab" :class="{ active: authMode === 'login' }" @click="authMode = 'login'">登录</button>
          <button class="mini-tab" :class="{ active: authMode === 'register' }" @click="authMode = 'register'">注册</button>
        </div>

        <form v-if="authMode === 'login'" class="form-grid" @submit.prevent="login">
          <label>
            <span>用户名</span>
            <input v-model.trim="loginForm.username" type="text" placeholder="请输入用户名" />
          </label>
          <label>
            <span>密码</span>
            <input v-model.trim="loginForm.password" type="password" placeholder="请输入密码" />
          </label>
          <button class="primary-btn" :disabled="loading.login">{{ loading.login ? '登录中...' : '用户登录' }}</button>
        </form>

        <form v-else class="form-grid" @submit.prevent="register">
          <label><span>用户名</span><input v-model.trim="registerForm.username" type="text" /></label>
          <label><span>密码</span><input v-model.trim="registerForm.password" type="password" /></label>
          <label><span>年龄</span><input v-model.number="registerForm.age" type="number" min="1" /></label>
          <label>
            <span>性别</span>
            <select v-model="registerForm.gender">
              <option value="">请选择</option>
              <option value="男">男</option>
              <option value="女">女</option>
            </select>
          </label>
          <label><span>手机号</span><input v-model.trim="registerForm.phone" type="text" /></label>
          <button class="primary-btn" :disabled="loading.register">{{ loading.register ? '提交中...' : '注册账号' }}</button>
        </form>
      </section>

      <section v-if="currentUser" class="panel">
        <div class="panel-head">
          <div>
            <p class="eyebrow">User Center</p>
            <h2>欢迎，{{ currentUser.username }}</h2>
          </div>
          <button class="ghost-btn" @click="logout">退出登录</button>
        </div>

        <div class="stats-grid">
          <div class="stat-card"><span>用户ID</span><strong>{{ currentUser.id }}</strong></div>
          <div class="stat-card"><span>年龄</span><strong>{{ currentUser.age ?? '-' }}</strong></div>
          <div class="stat-card"><span>性别</span><strong>{{ currentUser.gender || '-' }}</strong></div>
          <div class="stat-card"><span>手机</span><strong>{{ currentUser.phone || '-' }}</strong></div>
        </div>
      </section>

      <section v-if="currentUser" class="two-column">
        <div class="panel">
          <div class="panel-head">
            <div>
              <p class="eyebrow">Step 1</p>
              <h2>硬性指标初评</h2>
            </div>
          </div>

          <form class="form-grid" @submit.prevent="submitHealthData">
            <label><span>高压</span><input v-model.number="healthForm.bloodPressureHigh" type="number" /></label>
            <label><span>低压</span><input v-model.number="healthForm.bloodPressureLow" type="number" /></label>
            <label><span>血糖</span><input v-model.number="healthForm.bloodSugar" type="number" step="0.1" /></label>
            <label><span>心率</span><input v-model.number="healthForm.heartRate" type="number" /></label>
            <label><span>血氧</span><input v-model.number="healthForm.bloodOxygen" type="number" step="0.1" /></label>
            <label><span>身高(cm)</span><input v-model.number="healthForm.height" type="number" step="0.1" /></label>
            <label><span>体重(kg)</span><input v-model.number="healthForm.weight" type="number" step="0.1" /></label>
            <button class="primary-btn" :disabled="loading.health">{{ loading.health ? '提交中...' : '提交健康数据并生成问卷' }}</button>
          </form>

          <div v-if="initialAssessment.rawText" class="result-panel">
            <h3>初步健康评估</h3>
            <pre>{{ initialAssessment.rawText }}</pre>
          </div>

          <div v-if="recommendedMeta" class="recommend-panel">
            <div class="recommend-item">
              <span>初步评分</span>
              <strong>{{ recommendedMeta.initialScore ?? '-' }}</strong>
            </div>
            <div class="recommend-item">
              <span>初步等级</span>
              <strong>{{ recommendedMeta.initialHealthLevel }}</strong>
            </div>
            <div class="recommend-item">
              <span>初步体质</span>
              <strong>{{ recommendedMeta.initialConstitution }}</strong>
            </div>
            <div class="recommend-item">
              <span>推荐题量</span>
              <strong>{{ questions.length }}</strong>
            </div>
          </div>
        </div>

        <div class="panel">
          <div class="panel-head">
            <div>
              <p class="eyebrow">Step 2</p>
              <h2>动态推荐问卷</h2>
            </div>
            <button class="ghost-btn" :disabled="!canLoadRecommendedQuestions" @click="loadRecommendedQuestions">重新推荐</button>
          </div>

          <div v-if="!recommendedMeta" class="empty-state">请先提交健康数据，系统会根据初步评估自动推荐约 30 道更适合当前状态的题目。</div>
          <div v-else-if="questions.length === 0" class="empty-state">当前没有可用题目，请先在医生端维护题库并启用题目。</div>

          <form v-else class="question-list" @submit.prevent="submitQuestionnaire">
            <div class="dimension-summary">
              <span v-for="item in dimensionSummary" :key="item.dimension" class="dimension-tag">
                {{ item.dimension }} {{ item.count }}题
              </span>
            </div>

            <article v-for="question in questions" :key="question.id" class="question-card">
              <div class="question-head">
                <h3>{{ question.content }}</h3>
                <span class="pill">{{ question.dimension }}</span>
              </div>
              <p>{{ question.category }} / 适用体质：{{ question.applicableConstitution || '通用' }} / 适用等级：{{ question.applicableHealthLevel || '通用' }}</p>
              <div class="radio-grid">
                <label
                  v-for="option in question.options"
                  :key="option.id"
                  class="radio-card"
                  :class="{ checked: answers[question.id] === option.id }"
                >
                  <input v-model="answers[question.id]" type="radio" :name="`q-${question.id}`" :value="option.id" />
                  <span>{{ option.content }}</span>
                  <em>{{ option.score }} 分</em>
                </label>
              </div>
            </article>
            <button class="primary-btn" :disabled="loading.questionnaire">{{ loading.questionnaire ? '提交中...' : '提交动态问卷' }}</button>
          </form>

          <div v-if="questionnaireResultText" class="result-panel">
            <h3>融合评测结果</h3>
            <pre>{{ questionnaireResultText }}</pre>
          </div>
        </div>
      </section>
    </main>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { api } from '../api'

const emit = defineEmits(['notify'])

const authMode = ref('login')
const currentUser = ref(null)
const questions = ref([])
const questionnaireResultText = ref('')
const recommendedMeta = ref(null)

const initialAssessment = reactive({
  rawText: ''
})

const loading = reactive({
  login: false,
  register: false,
  health: false,
  questionnaire: false
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

const healthForm = reactive({
  bloodPressureHigh: null,
  bloodPressureLow: null,
  bloodSugar: null,
  heartRate: null,
  bloodOxygen: null,
  height: null,
  weight: null
})

const answers = reactive({})

const canLoadRecommendedQuestions = computed(() => Boolean(currentUser.value?.id))

const dimensionSummary = computed(() => {
  const counter = new Map()
  for (const question of questions.value) {
    const key = question.dimension || '未分类'
    counter.set(key, (counter.get(key) || 0) + 1)
  }
  return Array.from(counter.entries()).map(([dimension, count]) => ({ dimension, count }))
})

function notify(text, type = 'success') {
  emit('notify', { text, type })
}

function clearAnswers() {
  Object.keys(answers).forEach((key) => {
    delete answers[key]
  })
}

async function register() {
  if (!registerForm.username || !registerForm.password) {
    notify('请填写用户名和密码', 'error')
    return
  }
  loading.register = true
  try {
    const result = await api.userRegister(registerForm)
    notify(result.message || '注册成功')
    if (result.user) {
      currentUser.value = result.user
      localStorage.setItem('health-user', JSON.stringify(result.user))
      clearAnswers()
    } else {
      authMode.value = 'login'
    }
  } catch (error) {
    notify(error.message, 'error')
  } finally {
    loading.register = false
  }
}

async function login() {
  if (!loginForm.username || !loginForm.password) {
    notify('请输入用户名和密码', 'error')
    return
  }
  loading.login = true
  try {
    const result = await api.userLogin(loginForm)
    currentUser.value = result.user
    localStorage.setItem('health-user', JSON.stringify(result.user))
    notify(result.message || '登录成功')
    clearAnswers()
  } catch (error) {
    notify(error.message, 'error')
  } finally {
    loading.login = false
  }
}

async function loadRecommendedQuestions() {
  if (!currentUser.value?.id) {
    notify('请先登录用户端', 'error')
    return
  }
  try {
    const result = await api.getRecommendedQuestions(currentUser.value.id)
    recommendedMeta.value = result
    questions.value = result.questions || []
    clearAnswers()
    notify(`系统已为你推荐 ${questions.value.length} 道题目`)
  } catch (error) {
    notify(error.message, 'error')
  }
}

async function submitHealthData() {
  if (!currentUser.value?.id) {
    notify('请先登录用户端', 'error')
    return
  }
  loading.health = true
  try {
    const result = await api.addHealthData({
      userId: currentUser.value.id,
      bloodPressureHigh: healthForm.bloodPressureHigh,
      bloodPressureLow: healthForm.bloodPressureLow,
      bloodSugar: healthForm.bloodSugar,
      heartRate: healthForm.heartRate,
      bloodOxygen: healthForm.bloodOxygen,
      height: healthForm.height,
      weight: healthForm.weight
    })
    initialAssessment.rawText = String(result || '')
    questionnaireResultText.value = ''
    await loadRecommendedQuestions()
    notify('健康数据提交成功，已生成动态问卷')
  } catch (error) {
    notify(error.message, 'error')
  } finally {
    loading.health = false
  }
}

async function submitQuestionnaire() {
  if (!currentUser.value?.id) {
    notify('请先登录用户端', 'error')
    return
  }
  const payload = questions.value.map((question) => ({
    questionId: question.id,
    optionId: answers[question.id]
  }))
  if (payload.some((item) => !item.optionId)) {
    notify('请完成当前动态问卷的全部题目后再提交', 'error')
    return
  }
  loading.questionnaire = true
  try {
    const result = await api.submitQuestionnaire(currentUser.value.id, payload)
    questionnaireResultText.value = String(result || '')
    notify('动态问卷提交成功')
  } catch (error) {
    notify(error.message, 'error')
  } finally {
    loading.questionnaire = false
  }
}

function logout() {
  currentUser.value = null
  questions.value = []
  recommendedMeta.value = null
  initialAssessment.rawText = ''
  questionnaireResultText.value = ''
  clearAnswers()
  localStorage.removeItem('health-user')
  notify('用户已退出登录')
}

onMounted(() => {
  const cached = localStorage.getItem('health-user')
  if (cached) {
    try {
      currentUser.value = JSON.parse(cached)
    } catch {
      localStorage.removeItem('health-user')
    }
  }
})
</script>
