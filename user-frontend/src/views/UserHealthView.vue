<template>
  <section class="auth-layout step-layout">
    <aside class="panel auth-side">
      <span class="eyebrow">Step 1</span>
      <h2>硬性指标初评</h2>
      <p>
        先根据你当前知道的硬性健康指标生成初步评估。不了解的数据可以留空，
        系统只分析你实际填写的部分，并结合测量场景解释结果。
      </p>
      <div class="progress-list">
        <div class="progress-item active">1. 健康初评</div>
        <div class="progress-item">2. 动态问卷</div>
        <div class="progress-item">3. 健康报告</div>
        <div class="progress-item">4. 康养推荐</div>
      </div>
      <div class="progress-item" style="margin-top: 16px" v-if="user">
        当前用户：{{ user.username }}
      </div>
    </aside>

    <section class="panel auth-card">
      <div class="panel-head">
        <div>
          <span class="eyebrow">Initial Assessment</span>
          <h3>录入硬性健康指标</h3>
        </div>
        <div class="action-row">
          <button class="ghost-btn small-btn" @click="router.push('/user/center')">返回健康中心</button>
          <button class="ghost-btn small-btn" @click="logout">退出登录</button>
        </div>
      </div>

      <form class="form-grid" @submit.prevent="submitHealthData">
        <div class="two-column" style="grid-template-columns: 1fr 1fr; gap: 16px">
          <label>
            <span>高压</span>
            <input v-model.number="form.bloodPressureHigh" type="number" placeholder="知道就填，可留空" />
            <small class="field-hint">静息状态下通常可参考 90~120 mmHg。</small>
          </label>
          <label>
            <span>低压</span>
            <input v-model.number="form.bloodPressureLow" type="number" placeholder="知道就填，可留空" />
            <small class="field-hint">静息状态下通常可参考 60~80 mmHg。</small>
          </label>
        </div>

        <label>
          <span>血压测量场景</span>
          <select v-model="form.bloodPressureContext">
            <option value="RESTING">静息</option>
            <option value="AFTER_EXERCISE">运动后</option>
          </select>
          <small class="field-hint">只有同时填写高压和低压时，血压才会参与分析。</small>
        </label>

        <label>
          <span>血糖</span>
          <input v-model.number="form.bloodSugar" type="number" step="0.1" placeholder="知道就填，可留空" />
          <small class="field-hint">
            空腹常见参考范围约为 3.9~6.1 mmol/L，餐后 2 小时一般可参考 7.8 mmol/L 以下。
            如不清楚可留空。
          </small>
        </label>

        <label>
          <span>血糖测量场景</span>
          <select v-model="form.bloodSugarContext">
            <option value="FASTING">空腹</option>
            <option value="POSTPRANDIAL">餐后 2 小时</option>
            <option value="RANDOM">随机</option>
          </select>
        </label>

        <div class="two-column" style="grid-template-columns: 1fr 1fr; gap: 16px">
          <label>
            <span>心率</span>
            <input v-model.number="form.heartRate" type="number" placeholder="知道就填，可留空" />
            <small class="field-hint">成年人静息心率通常可参考 60~100 次/分钟。</small>
          </label>
          <label>
            <span>血氧</span>
            <input v-model.number="form.bloodOxygen" type="number" step="0.1" placeholder="知道就填，可留空" />
            <small class="field-hint">大多数成年人静息血氧常见在 95%~100% 之间，不清楚时可留空。</small>
          </label>
        </div>

        <div class="two-column" style="grid-template-columns: 1fr 1fr; gap: 16px">
          <label>
            <span>心率测量场景</span>
            <select v-model="form.heartRateContext">
              <option value="RESTING">静息</option>
              <option value="AFTER_EXERCISE">运动后</option>
            </select>
          </label>
          <label>
            <span>血氧测量场景</span>
            <select v-model="form.bloodOxygenContext">
              <option value="RESTING">静息</option>
              <option value="AFTER_EXERCISE">运动后</option>
            </select>
          </label>
        </div>

        <div class="two-column" style="grid-template-columns: 1fr 1fr; gap: 16px">
          <label>
            <span>身高(cm)</span>
            <input v-model.number="form.height" type="number" step="0.1" placeholder="建议填写，和体重配合分析" />
            <small class="field-hint">身高和体重一起填写后，系统才会计算 BMI、体脂率和基础代谢。</small>
          </label>
          <label>
            <span>体重(kg)</span>
            <input v-model.number="form.weight" type="number" step="0.1" placeholder="建议填写，和身高配合分析" />
            <small class="field-hint">如果暂时不清楚血糖、血氧等数据，也可以先基于身高体重做初步评估。</small>
          </label>
        </div>

        <p class="muted-copy" style="margin-top: 4px; text-align: left">
          说明：血压需要高压和低压同时填写才会参与分析；身高和体重需要同时填写才会计算 BMI / 体脂率；
          其余指标可以单独填写。
        </p>

        <button class="primary-btn" :disabled="submitting" style="margin-top: 8px">
          {{ submitting ? '分析中...' : '生成初步评估并进入动态问卷' }}
        </button>
      </form>

      <div v-if="preview.rawText" class="inline-feedback success" style="margin-top: 20px; white-space: pre-wrap">
        <strong>初步结果预览</strong><br />
        {{ preview.rawText }}
      </div>
    </section>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '../api'
import { session } from '../session'

const router = useRouter()
const user = ref(null)
const submitting = ref(false)

const form = reactive({
  bloodPressureHigh: null,
  bloodPressureLow: null,
  bloodPressureContext: 'RESTING',
  bloodSugar: null,
  bloodSugarContext: 'FASTING',
  heartRate: null,
  heartRateContext: 'RESTING',
  bloodOxygen: null,
  bloodOxygenContext: 'RESTING',
  height: null,
  weight: null
})

const preview = reactive({
  rawText: '',
  score: null,
  healthLevel: '',
  initialConstitution: '',
  constitutionDescription: ''
})

async function submitHealthData() {
  if (!user.value?.id) {
    await router.push('/user/auth')
    return
  }

  submitting.value = true
  try {
    const result = await api.addHealthData({
      userId: user.value.id,
      bloodPressureHigh: nullableNumber(form.bloodPressureHigh),
      bloodPressureLow: nullableNumber(form.bloodPressureLow),
      bloodPressureContext: form.bloodPressureContext,
      bloodSugar: nullableNumber(form.bloodSugar),
      bloodSugarContext: form.bloodSugarContext,
      heartRate: nullableNumber(form.heartRate),
      heartRateContext: form.heartRateContext,
      bloodOxygen: nullableNumber(form.bloodOxygen),
      bloodOxygenContext: form.bloodOxygenContext,
      height: nullableNumber(form.height),
      weight: nullableNumber(form.weight)
    })

    const assessmentToken = Date.now()
    preview.rawText = `评分：${result.score}
等级：${result.healthLevel}
初步体质：${result.initialConstitution}
体质特点：${result.constitutionDescription}
${result.message || ''}`
    preview.score = result.score
    preview.healthLevel = result.healthLevel
    preview.initialConstitution = result.initialConstitution
    preview.constitutionDescription = result.constitutionDescription

    session.setRecommendation(null)
    session.setReport(null)
    session.setAssessment({
      ...result,
      assessmentToken,
      rawText: preview.rawText,
      form: { ...form }
    })
    await router.push('/user/questionnaire')
  } finally {
    submitting.value = false
  }
}

function logout() {
  session.clearUserFlow()
  router.push('/user/auth')
}

function nullableNumber(value) {
  return value === '' || value === undefined || value === null || Number.isNaN(value) ? null : Number(value)
}

onMounted(() => {
  user.value = session.getUser()
  if (!user.value) {
    router.replace('/user/auth')
  }
})
</script>
