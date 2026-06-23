<template>
  <section class="center-layout">
    <div class="panel center-hero apple-hero">
      <div>
        <span class="eyebrow">Step 2</span>
        <h2>动态问卷答题</h2>
        <p>系统根据硬性指标初评结果抽取 30 道题。你可以通过题号导航自由跳转，不需要一直点击下一题。</p>
      </div>
      <div class="report-actions">
        <button class="ghost-btn" @click="reloadQuestions">重新推荐</button>
        <button class="ghost-btn" @click="router.push('/user/health')">返回上一步</button>
      </div>
    </div>

    <section class="center-grid" style="grid-template-columns: 340px 1fr;">
      <aside class="center-side">
        <div class="panel compact-panel glass-panel">
          <div class="panel-head">
            <div>
              <span class="eyebrow">Progress</span>
              <h3>答题进度</h3>
            </div>
          </div>
          <div class="summary-strip summary-strip-single" style="margin-top: 0;">
            <div class="summary-pill">
              <span>已完成</span>
              <strong>{{ answeredCount }} / {{ questions.length }}</strong>
            </div>
            <div class="summary-pill">
              <span>用时</span>
              <strong>{{ elapsedLabel }}</strong>
            </div>
          </div>
          <div class="dimension-bar-track" style="margin-top: 16px;">
            <div class="dimension-bar-fill" :style="{ width: progressPercent + '%' }"></div>
          </div>
        </div>

        <div class="panel compact-panel glass-panel" v-if="recommendation">
          <div class="panel-head">
            <div>
              <span class="eyebrow">Initial</span>
              <h3>初评依据</h3>
            </div>
          </div>
          <div class="visual-copy-list">
            <div class="visual-copy-item">
              <label>初步评分</label>
              <strong>{{ recommendation.initialScore ?? '-' }}</strong>
            </div>
            <div class="visual-copy-item">
              <label>初步体质</label>
              <strong>{{ recommendation.initialConstitution }}</strong>
            </div>
            <div class="visual-copy-item">
              <label>推荐理由</label>
              <p>{{ recommendation.recommendReason || '系统会保证四个维度均有题目覆盖。' }}</p>
            </div>
          </div>
        </div>

        <div class="panel compact-panel glass-panel" v-if="questions.length">
          <div class="panel-head">
            <div>
              <span class="eyebrow">Question Map</span>
              <h3>题号导航</h3>
            </div>
          </div>
          <div class="question-jump-grid">
            <button
              v-for="(question, index) in questions"
              :key="question.id"
              type="button"
              class="question-jump-btn"
              :class="{
                active: currentIndex === index,
                answered: Boolean(answers[question.id])
              }"
              @click="currentIndex = index"
            >
              {{ index + 1 }}
            </button>
          </div>
          <p class="muted-copy" style="padding: 12px 0 0; text-align: left;">
            蓝色代表当前题，绿色代表已作答。漏答时系统会自动定位到第一道未完成题。
          </p>
        </div>
      </aside>

      <div class="center-main">
        <div class="panel focus-summary glass-panel">
          <div class="panel-head">
            <div>
              <span class="eyebrow">Questionnaire</span>
              <h3>推荐问卷</h3>
            </div>
            <div class="range-toggle" v-if="questions.length">
              <span class="pill">{{ currentQuestion?.dimension || '-' }}</span>
            </div>
          </div>

          <div v-if="loading" class="muted-copy">正在生成动态问卷...</div>
          <div v-else-if="questions.length === 0" class="muted-copy">当前暂无可用题目，请先由医生端启用题库。</div>

          <form v-else class="form-grid" @submit.prevent="submitQuestionnaire">
            <div v-if="submitMessage" class="inline-feedback" :class="submitStateClass">{{ submitMessage }}</div>

            <article v-if="currentQuestion" :key="currentQuestion.id" class="record-card question-focus-card">
              <div class="record-head">
                <div>
                  <span class="eyebrow">Q{{ currentIndex + 1 }}</span>
                  <h3>{{ currentQuestion.content }}</h3>
                </div>
                <span class="pill">{{ currentQuestion.category }}</span>
              </div>
              <p class="question-meta">
                {{ currentQuestion.dimension }} / 适用体质：{{ currentQuestion.applicableConstitution || '通用' }}
              </p>

              <div class="question-options">
                <label
                  v-for="option in currentQuestion.options"
                  :key="option.id"
                  class="radio-card"
                  :class="{ checked: answers[currentQuestion.id] === option.id }"
                >
                  <input
                    :checked="answers[currentQuestion.id] === option.id"
                    type="radio"
                    :name="`q-${currentQuestion.id}`"
                    :value="option.id"
                    @change="handleAnswerChange(currentQuestion.id, option.id)"
                  />
                  <span>{{ option.content }}</span>
                </label>
              </div>
            </article>

            <div class="question-action-bar">
              <button type="button" class="ghost-btn" :disabled="currentIndex === 0" @click="goPrev">上一题</button>
              <button
                v-if="currentIndex < questions.length - 1"
                type="button"
                class="ghost-btn"
                @click="goNext"
              >
                下一题
              </button>
              <button class="primary-btn" :disabled="submitting">
                {{ submitting ? '生成报告中...' : '完成答题并生成健康报告' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </section>
  </section>
</template>

<script setup>
import { computed, onMounted, onUnmounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '../api'
import { session } from '../session'

const router = useRouter()
const user = ref(null)
const recommendation = ref(null)
const assessment = ref(null)
const questions = ref([])
const loading = ref(false)
const submitting = ref(false)
const currentIndex = ref(0)
const submitMessage = ref('')
const submitState = ref('')
const answers = reactive({})
const startedAt = ref(Date.now())
const now = ref(Date.now())
let timer = null

const currentQuestion = computed(() => questions.value[currentIndex.value] || null)

const answeredCount = computed(() => {
  return questions.value.filter((question) => Boolean(answers[question.id])).length
})

const progressPercent = computed(() => {
  if (!questions.value.length) return 0
  return Math.round((answeredCount.value / questions.value.length) * 100)
})

const elapsedSeconds = computed(() => Math.max(0, Math.round((now.value - startedAt.value) / 1000)))

const elapsedLabel = computed(() => {
  const minutes = Math.floor(elapsedSeconds.value / 60)
  const seconds = elapsedSeconds.value % 60
  return `${minutes}:${String(seconds).padStart(2, '0')}`
})

const submitStateClass = computed(() => {
  return submitState.value === 'error' ? 'error' : 'success'
})

function clearAnswers() {
  Object.keys(answers).forEach((key) => delete answers[key])
}

function goPrev() {
  if (currentIndex.value > 0) currentIndex.value -= 1
}

function goNext() {
  if (currentIndex.value < questions.value.length - 1) currentIndex.value += 1
}

function handleAnswerChange(questionId, optionId) {
  answers[questionId] = optionId
  if (currentIndex.value < questions.value.length - 1) {
    window.setTimeout(() => {
      if (answers[questionId] === optionId) {
        goNext()
      }
    }, 180)
  }
}

async function loadQuestions() {
  if (!user.value?.id) {
    await router.replace('/user/auth')
    return
  }
  loading.value = true
  submitMessage.value = ''
  submitState.value = ''
  try {
    const result = await api.getRecommendedQuestions(user.value.id)
    const payload = {
      ...result,
      assessmentToken: assessment.value?.assessmentToken || null
    }
    recommendation.value = payload
    questions.value = payload.questions || []
    session.setRecommendation(payload)
    clearAnswers()
    currentIndex.value = 0
    startedAt.value = Date.now()
  } finally {
    loading.value = false
  }
}

async function reloadQuestions() {
  await loadQuestions()
}

async function submitQuestionnaire() {
  submitMessage.value = ''
  submitState.value = ''

  if (!questions.value.length) {
    submitState.value = 'error'
    submitMessage.value = '当前没有可提交的问卷题目，请先重新生成问卷。'
    return
  }

  const payloadAnswers = questions.value.map((question) => ({
    questionId: question.id,
    optionId: answers[question.id]
  }))

  if (payloadAnswers.some((item) => !item.optionId)) {
    const firstMissingIndex = payloadAnswers.findIndex((item) => !item.optionId)
    if (firstMissingIndex >= 0) currentIndex.value = firstMissingIndex
    submitState.value = 'error'
    submitMessage.value = '还有题目未作答，系统已自动定位到第一道未完成题。'
    return
  }

  submitting.value = true
  try {
    const result = await api.submitQuestionnaire(user.value.id, {
      answers: payloadAnswers,
      durationSeconds: elapsedSeconds.value,
      startedAt: startedAt.value,
      completedAt: Date.now()
    })
    if (!result || !result.finalConstitution) {
      throw new Error('报告生成失败，请重新提交问卷。')
    }
    session.setReport({
      ...result,
      text: result.summary || '',
      submittedAt: Date.now()
    })
    submitState.value = 'success'
    submitMessage.value = '健康报告生成成功，正在跳转...'
    await router.push('/user/report')
  } catch (error) {
    submitState.value = 'error'
    submitMessage.value = error?.message || '提交问卷失败，请稍后重试。'
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  timer = window.setInterval(() => {
    now.value = Date.now()
  }, 1000)

  user.value = session.getUser()
  assessment.value = session.getAssessment()
  if (!user.value) {
    router.replace('/user/auth')
    return
  }
  if (!assessment.value) {
    router.replace('/user/health')
    return
  }
  const savedRecommendation = session.getRecommendation()
  const isCurrentRecommendation =
    savedRecommendation?.questions?.length &&
    savedRecommendation.assessmentToken &&
    savedRecommendation.assessmentToken === assessment.value.assessmentToken

  if (isCurrentRecommendation) {
    recommendation.value = savedRecommendation
    questions.value = savedRecommendation.questions
    currentIndex.value = 0
    startedAt.value = Date.now()
  } else {
    await loadQuestions()
  }
})

onUnmounted(() => {
  if (timer) window.clearInterval(timer)
})
</script>
