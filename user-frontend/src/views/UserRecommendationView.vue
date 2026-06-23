<template>
  <section class="center-layout">
    <div class="panel center-hero apple-hero">
      <div>
        <span class="eyebrow">Step 4</span>
        <h2>个性化康养方案推荐</h2>
        <p>系统会把基础方案与饮食、运动、睡眠和心理放松模板组合起来，形成更细的康养建议。</p>
      </div>
      <div class="report-actions">
        <button v-if="!selectedFavorite" class="ghost-btn" @click="router.push('/user/report')">返回健康报告</button>
        <button class="ghost-btn" @click="goCenter">健康中心</button>
        <button class="primary-btn" @click="restartFlow">重新开始评测</button>
      </div>
    </div>

    <section class="center-grid" v-if="healthPlan">
      <div class="center-main">
        <div class="panel focus-summary glass-panel">
          <div class="panel-head">
            <div>
              <span class="eyebrow">Recommendation</span>
              <h3>{{ selectedFavorite ? '收藏方案详情' : '系统组合推荐' }}</h3>
            </div>
            <div class="range-toggle">
              <span class="pill">{{ displayConstitution }}</span>
              <span class="pill">{{ displayHealthLevel }}</span>
            </div>
          </div>

          <div class="inline-feedback success" style="margin-bottom: 20px;">
            <strong>{{ selectedFavorite ? '方案说明：' : '健康画像解读：' }}</strong>{{ displayConstitutionDescription }}
          </div>

          <div class="action-row" style="margin-bottom: 20px;">
            <button class="primary-btn" :disabled="favoriteLoading" @click="favoritePlan">
              {{ favoriteLoading ? '收藏中...' : '收藏当前方案' }}
            </button>
            <button class="ghost-btn" @click="router.push('/user/center/articles')">查看健康资讯</button>
          </div>

          <div v-if="favoriteMessage" class="inline-feedback success" style="margin-bottom: 20px;">{{ favoriteMessage }}</div>

          <div class="trend-grid" style="grid-template-columns: repeat(2, 1fr);">
            <article v-for="item in recommendationCards" :key="item.label" class="trend-card plan-module-card">
              <div class="trend-card-head">
                <h4>{{ item.title }}</h4>
                <span class="pill">{{ item.label }}</span>
              </div>
              <p>{{ item.content }}</p>
            </article>
          </div>
        </div>
      </div>

      <aside class="center-side">
        <div class="panel compact-panel glass-panel">
          <div class="panel-head">
            <div>
              <span class="eyebrow">Plan Summary</span>
              <h3>方案概览</h3>
            </div>
          </div>
          <div class="visual-copy-list">
            <div class="visual-copy-item">
              <label>方案标题</label>
              <strong>{{ healthPlan.title || '康养方案' }}</strong>
            </div>
            <div class="visual-copy-item">
              <label>最终融合评分</label>
              <strong>{{ report?.score ?? '-' }}</strong>
            </div>
            <div class="visual-copy-item">
              <label>风险等级</label>
              <strong>{{ report?.riskLevel || '-' }}</strong>
            </div>
            <div class="visual-copy-item">
              <label>硬性指标体质</label>
              <strong>{{ report?.healthConstitution || '-' }}</strong>
            </div>
            <div class="visual-copy-item">
              <label>问卷体质</label>
              <strong>{{ report?.questionnaireConstitution || '-' }}</strong>
            </div>
            <div class="visual-copy-item">
              <label>最终健康画像</label>
              <strong>{{ report?.finalConstitution || selectedFavorite?.constitutionType || '-' }}</strong>
            </div>
            <div class="visual-copy-item" v-if="report?.primaryConstitution">
              <label>主导体质</label>
              <strong>{{ report.primaryConstitution }}</strong>
            </div>
          </div>
        </div>

        <div class="panel compact-panel glass-panel" v-if="recommendationReasons.length">
          <div class="panel-head">
            <div>
              <span class="eyebrow">Why</span>
              <h3>推荐原因</h3>
            </div>
          </div>
          <div class="visual-copy-list">
            <div v-for="reason in recommendationReasons" :key="reason" class="visual-copy-item">
              <p>{{ reason }}</p>
            </div>
          </div>
        </div>
      </aside>
    </section>

    <section v-else class="panel glass-panel" style="padding: 48px; text-align: center;">
      <span class="eyebrow">No Recommendation</span>
      <h3 style="margin: 16px 0;">当前还没有可展示的康养方案</h3>
      <p style="margin-bottom: 24px;">请先完成健康初评和动态问卷，并确认数据库中已有对应的康养方案。</p>
      <button class="primary-btn" @click="router.push('/user/health')">前往健康初评</button>
    </section>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '../api'
import { session } from '../session'

const router = useRouter()
const user = ref(null)
const assessment = ref(null)
const report = ref(null)
const selectedFavorite = ref(null)
const favoriteLoading = ref(false)
const favoriteMessage = ref('')

const healthPlan = computed(() => {
  if (selectedFavorite.value) {
    return {
      id: selectedFavorite.value.planId,
      title: selectedFavorite.value.planTitle,
      diet: selectedFavorite.value.diet,
      drink: selectedFavorite.value.drink,
      sport: selectedFavorite.value.sport,
      lifestyle: selectedFavorite.value.lifestyle
    }
  }
  return report.value?.healthPlan || null
})

const displayConstitution = computed(() => {
  return selectedFavorite.value?.constitutionType || report.value?.finalConstitution || '待分析'
})

const displayHealthLevel = computed(() => {
  return selectedFavorite.value?.healthLevel || report.value?.healthLevel || '待分析'
})

const displayConstitutionDescription = computed(() => {
  if (report.value?.finalConstitutionDescription && !selectedFavorite.value) {
    return report.value.finalConstitutionDescription
  }
  return '该方案来自历史收藏记录，可作为后续日常康养参考。'
})

const recommendationReasons = computed(() => {
  return healthPlan.value?.recommendationReasons || report.value?.healthPlan?.recommendationReasons || []
})

const recommendationCards = computed(() => {
  if (!healthPlan.value) return []
  return [
    { label: '饮食模板', title: '饮食与热量管理', content: healthPlan.value.mealAdvice || healthPlan.value.diet },
    { label: '饮品模板', title: '饮品与水分调理', content: healthPlan.value.drink },
    { label: '运动模板', title: '运动强度与频次', content: healthPlan.value.exerciseAdvice || healthPlan.value.sport },
    { label: '睡眠模板', title: '作息与睡眠改善', content: healthPlan.value.sleepAdvice || healthPlan.value.lifestyle },
    { label: '心理模板', title: '心理放松与压力缓解', content: healthPlan.value.stressAdvice || '每天预留 10-15 分钟进行呼吸训练、轻步行或正念放松。' }
  ]
})

function restartFlow() {
  session.setAssessment(null)
  session.setRecommendation(null)
  session.setReport(null)
  session.setSelectedFavorite(null)
  router.push('/user/health')
}

function goCenter() {
  router.push('/user/center')
}

async function favoritePlan() {
  if (!user.value?.id || !healthPlan.value) return
  favoriteLoading.value = true
  favoriteMessage.value = ''
  try {
    const result = await api.saveFavorite({
      userId: user.value.id,
      planId: healthPlan.value.id,
      planTitle: healthPlan.value.title,
      constitutionType: report.value?.finalConstitution || selectedFavorite.value?.constitutionType,
      healthLevel: report.value?.healthLevel || selectedFavorite.value?.healthLevel,
      diet: healthPlan.value.mealAdvice || healthPlan.value.diet,
      drink: healthPlan.value.drink,
      sport: healthPlan.value.exerciseAdvice || healthPlan.value.sport,
      lifestyle: healthPlan.value.sleepAdvice || healthPlan.value.lifestyle
    })
    favoriteMessage.value = result.message || '收藏成功'
  } finally {
    favoriteLoading.value = false
  }
}

onMounted(() => {
  user.value = session.getUser()
  assessment.value = session.getAssessment()
  report.value = session.getReport()
  selectedFavorite.value = session.getSelectedFavorite()
  if (!user.value) {
    router.replace('/user/auth')
    return
  }
  if (!report.value && !selectedFavorite.value) {
    router.replace('/user/report')
  }
})
</script>
