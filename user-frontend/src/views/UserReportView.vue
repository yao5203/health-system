<template>
  <section class="center-layout">
    <div class="panel center-hero apple-hero">
      <div>
        <span class="eyebrow">Step 3</span>
        <h2>健康评测报告</h2>
        <p>报告会展示最终结论，也会解释哪些指标、哪些问卷维度和哪些体质倾向共同导致了这个判断。</p>
      </div>
      <div class="report-actions">
        <button class="ghost-btn" @click="openCurrentRecommendation">查看康养推荐</button>
        <button class="ghost-btn" @click="router.push('/user/center')">健康中心</button>
        <button class="primary-btn" @click="restartFlow">重新开始评测</button>
      </div>
    </div>

    <section class="center-grid">
      <div class="center-main">
        <div class="panel focus-summary glass-panel">
          <div class="panel-head">
            <div>
              <span class="eyebrow">Summary</span>
              <h3>综合结论</h3>
            </div>
            <span class="pill">{{ report?.riskLevel || '-' }}</span>
          </div>

          <div class="metric-ribbon" v-if="reportMetrics.length" style="grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));">
            <div v-for="item in reportMetrics" :key="item.label" class="metric-ribbon-item">
              <span>{{ item.label }}</span>
              <strong>{{ item.value }}</strong>
              <p>{{ item.note }}</p>
            </div>
          </div>

          <div class="inline-feedback success" style="margin-top: 16px;">
            <strong>最终健康画像：</strong>{{ report?.finalConstitution || '-' }}。
            {{ report?.finalConstitutionDescription || '' }}
          </div>

          <div v-if="report?.finalDecisionReason" class="inline-feedback success" style="margin-top: 12px;">
            <strong>判定方式：</strong>{{ report?.finalDecisionModeLabel || '融合判定' }}。
            {{ report?.finalDecisionReason }}
          </div>

          <div class="visual-copy-list" style="margin-top: 16px;">
            <div class="visual-copy-item">
              <label>主导体质</label>
              <strong>{{ report?.primaryConstitution || report?.questionnaireConstitution || '-' }}</strong>
              <p>{{ report?.primaryConstitutionDescription || '用于匹配康养方案的中医体质倾向。' }}</p>
            </div>
            <div class="visual-copy-item">
              <label>画像重点</label>
              <strong>{{ healthProfile.focus || '-' }}</strong>
              <p>系统会把最低维度短板、硬性指标风险和问卷倾向合并判断，而不是只输出四种体质之一。</p>
            </div>
          </div>
        </div>

        <div class="workspace-section-grid">
          <section class="glass-panel">
            <div class="panel-head compact-head">
              <h3>硬性指标影响</h3>
            </div>
            <div class="dimension-bars">
              <div v-for="item in indicatorRows" :key="item.name" class="dimension-bar-row">
                <span>{{ item.name }}</span>
                <div class="dimension-bar-track">
                  <div class="dimension-bar-fill" :style="{ width: `${item.score}%` }"></div>
                </div>
                <strong>{{ item.score }}</strong>
              </div>
            </div>
            <div class="visual-copy-list" style="margin-top: 16px;">
              <div v-for="item in riskFactors" :key="item.name" class="visual-copy-item">
                <label>{{ item.name }} 是风险贡献项</label>
                <p>{{ item.reason }}</p>
              </div>
            </div>
          </section>

          <section class="glass-panel">
            <div class="panel-head compact-head">
              <h3>问卷维度权重</h3>
            </div>
            <div class="dimension-bars">
              <div v-for="item in dimensionRows" :key="item.dimension" class="dimension-bar-row">
                <span>{{ item.dimension }}</span>
                <div class="dimension-bar-track">
                  <div class="dimension-bar-fill" :style="{ width: `${item.score}%` }"></div>
                </div>
                <strong>{{ item.score }}</strong>
              </div>
            </div>
            <div v-if="dimensionExplain?.highestDimension" class="inline-feedback success">
              最高维度：{{ dimensionExplain.highestDimension.dimension }}，
              得分 {{ dimensionExplain.highestDimension.score }}。
            </div>
          </section>

          <section class="glass-panel">
            <div class="panel-head compact-head">
              <h3>体质倾向排序</h3>
            </div>
            <div class="dimension-bars">
              <div v-for="item in finalRanking" :key="item.name" class="dimension-bar-row">
                <span>{{ item.name }}</span>
                <div class="dimension-bar-track">
                  <div class="dimension-bar-fill" :style="{ width: `${item.score}%` }"></div>
                </div>
                <strong>{{ item.score }}</strong>
              </div>
            </div>
            <p class="muted-copy" style="padding: 12px 0 0; text-align: left;">
              {{ explainability?.finalTypeReason || '最终体质由硬性指标与问卷结果融合得到。' }}
            </p>
          </section>

          <section class="glass-panel">
            <div class="panel-head compact-head">
              <h3>问卷质量控制</h3>
            </div>
            <div class="visual-copy-list">
              <div class="visual-copy-item">
                <label>质量评分</label>
                <strong>{{ quality?.qualityScore ?? '-' }}</strong>
              </div>
              <div class="visual-copy-item">
                <label>一致性等级</label>
                <strong>{{ quality?.consistencyLevel || '-' }}</strong>
              </div>
              <div class="visual-copy-item">
                <label>答题用时</label>
                <strong>{{ formatDuration(quality?.durationSeconds) }}</strong>
              </div>
              <div class="visual-copy-item">
                <label>系统提示</label>
                <p>{{ quality?.warnings || '问卷完成质量良好。' }}</p>
              </div>
            </div>
          </section>
        </div>
      </div>

      <aside class="center-side">
        <div class="panel compact-panel glass-panel">
          <div class="panel-head">
            <div>
              <span class="eyebrow">Info</span>
              <h3>报告信息</h3>
            </div>
          </div>
          <div class="visual-copy-list">
            <div class="visual-copy-item">
              <label>用户</label>
              <strong>{{ user?.username || '-' }}</strong>
            </div>
            <div class="visual-copy-item">
              <label>报告生成时间</label>
              <strong>{{ formatTime(report?.submittedAt) }}</strong>
            </div>
            <div class="visual-copy-item">
              <label>问卷体质</label>
              <strong>{{ report?.questionnaireConstitution || '-' }}</strong>
            </div>
            <div class="visual-copy-item">
              <label>硬性指标体质</label>
              <strong>{{ report?.healthConstitution || '-' }}</strong>
            </div>
          </div>
        </div>

        <div class="panel compact-panel glass-panel">
          <div class="panel-head">
            <div>
              <span class="eyebrow">Why Plan</span>
              <h3>推荐依据</h3>
            </div>
          </div>
          <p style="font-size: 0.875rem; line-height: 1.7;">
            {{ explainability?.planReason || '康养方案会根据最终体质、风险等级和维度短板组合生成。' }}
          </p>
        </div>
      </aside>
    </section>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { session } from '../session'

const router = useRouter()
const user = ref(null)
const report = ref(null)

const snapshot = computed(() => report.value?.assessmentSnapshot || {})
const explainability = computed(() => report.value?.explainability || {})
const dimensionExplain = computed(() => report.value?.dimensionExplain || explainability.value?.dimensionExplain || {})
const quality = computed(() => report.value?.questionnaireQuality || null)
const healthProfile = computed(() => report.value?.healthProfile || snapshot.value?.healthProfile || {})

const reportMetrics = computed(() => [
  { label: '最终总分', value: report.value?.score ?? '-', note: '硬性指标与问卷融合分' },
  { label: '硬性指标', value: report.value?.hardIndicatorScore ?? snapshot.value.score ?? '-', note: '身体客观指标评分' },
  { label: '问卷评分', value: report.value?.questionnaireOverallScore ?? '-', note: '四个问卷维度均值' },
  { label: 'BMI', value: snapshot.value.bmi ?? '-', note: '体重与身高基础比值' },
  { label: '体脂率', value: snapshot.value.bodyFatRate ?? '-', note: '估算身体脂肪占比' },
  { label: '风险等级', value: report.value?.riskLevel || snapshot.value.riskLevel || '-', note: '综合风险判断' }
])

const indicatorRows = computed(() => {
  const scores = snapshot.value.indicatorScores || {}
  return Object.entries(scores).map(([name, value]) => ({
    name,
    score: value.score || 0,
    reason: value.reason
  }))
})

const riskFactors = computed(() => {
  return explainability.value?.hardIndicatorReasons || snapshot.value.riskFactors || []
})

const dimensionRows = computed(() => {
  const ranking = dimensionExplain.value?.ranking || []
  return ranking.map((item) => ({
    dimension: item.dimension,
    score: item.score
  }))
})

const finalRanking = computed(() => {
  return explainability.value?.finalRanking || []
})

function restartFlow() {
  session.setAssessment(null)
  session.setRecommendation(null)
  session.setReport(null)
  session.setSelectedFavorite(null)
  router.push('/user/health')
}

function openCurrentRecommendation() {
  session.setSelectedFavorite(null)
  router.push('/user/recommendation')
}

function formatDuration(seconds) {
  if (!seconds && seconds !== 0) return '-'
  const minutes = Math.floor(seconds / 60)
  const rest = seconds % 60
  return `${minutes}分${rest}秒`
}

function formatTime(timestamp) {
  if (!timestamp) return '-'
  const date = new Date(timestamp)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

onMounted(() => {
  user.value = session.getUser()
  report.value = session.getReport()
  if (!user.value) {
    router.replace('/user/auth')
    return
  }
  if (!report.value) {
    router.replace('/user/health')
  }
})
</script>
