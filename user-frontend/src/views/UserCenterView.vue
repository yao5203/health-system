<template>
  <section class="workspace-page" v-if="dashboard">
    <div class="workspace-hero apple-hero">
      <div>
        <h2>个人健康总览</h2>
      </div>
      <div class="workspace-toolbar">
        <div class="range-toggle apple-toggle">
          <button class="mini-tab" :class="{ active: range === 'week' }" @click="changeRange('week')">近 7 天</button>
          <button class="mini-tab" :class="{ active: range === 'month' }" @click="changeRange('month')">近 30 天</button>
        </div>
        <button class="ghost-btn" @click="router.push('/user/health')">开始新测评</button>
        <button v-if="hasRecentRecommendation" class="ghost-btn" @click="openCurrentRecommendation">最近一次康养方案</button>
      </div>
    </div>

    <div v-if="loadError" class="inline-feedback error">
      {{ loadError }}
    </div>

    <div v-if="!hasHealthData" class="workspace-section-grid">
      <section class="glass-panel section-span-2">
        <div class="panel-head">
          <div>
            <h3>你还没有健康测评记录</h3>
          </div>
          <span class="pill">新用户</span>
        </div>
        <div class="action-row" style="margin-top: 20px">
          <button class="primary-btn" @click="router.push('/user/health')">开始第一轮测评</button>
          <button class="ghost-btn" @click="router.push('/user/center/articles')">先去看看健康资讯</button>
        </div>
      </section>
    </div>

    <div v-else class="workspace-section-grid">
      <section class="glass-panel section-span-2">
        <div class="panel-head">
          <div>
            <h3>周期融合评测</h3>
          </div>
          <span class="pill">{{ summarySnapshot?.riskLevel || '-' }}</span>
        </div>

        <div class="apple-stat-grid">
          <article v-for="item in summaryCards" :key="item.label" class="apple-stat-card">
            <span>{{ item.label }}</span>
            <strong>{{ item.value }}</strong>
            <p>{{ item.note }}</p>
          </article>
        </div>
      </section>

      <section class="glass-panel">
        <div class="panel-head compact-head">
          <h3>体质与风险解读</h3>
          <span class="pill">{{ latestConstitution }}</span>
        </div>
        <div class="visual-copy-list">
          <div class="visual-copy-item">
            <label>{{ summarySnapshot?.rangeLabel || '周期融合体质' }}</label>
            <strong>{{ summarySnapshot?.constitution || '-' }}</strong>
            <p>{{ summarySnapshot?.constitutionDescription || '暂无描述。' }}</p>
          </div>
          <div class="visual-copy-item">
            <label>风险等级</label>
            <strong>{{ summarySnapshot?.riskLevel || '-' }}</strong>
            <p>系统会综合硬性指标、问卷结果与规则配置，形成更细分的健康判断。</p>
          </div>
        </div>
      </section>

      <section class="glass-panel">
        <div class="panel-head compact-head">
          <h3>多维度评分</h3>
          <span class="pill">融合结果</span>
        </div>
        <div class="dimension-bars">
          <div v-for="item in dimensionBars" :key="item.label" class="dimension-bar-row">
            <span>{{ item.label }}</span>
            <div class="dimension-bar-track">
              <div class="dimension-bar-fill" :style="{ width: `${item.value}%` }"></div>
            </div>
            <strong>{{ item.value }}</strong>
          </div>
        </div>
      </section>

      <section class="glass-panel section-span-2">
        <div class="panel-head">
          <div>
            <h3>健康趋势分析</h3>
          </div>
        </div>

        <div class="trend-grid">
          <div class="trend-card apple-trend-card">
            <div class="trend-card-head">
              <h4>体重趋势</h4>
              <span>有效 {{ dashboard.history?.weightTrend?.length || 0 }} 天 / 第 {{ weightTrendPage + 1 }} 页</span>
            </div>
            <div class="chart-stage">
              <div class="chart-y-axis">
                <span v-for="label in weightAxisLabels" :key="label">{{ label }}</span>
              </div>
              <div class="chart-main">
                <svg viewBox="0 0 320 160" class="trend-svg" @mouseleave="weightHoverIndex = -1">
                  <polyline :points="weightPolyline" class="line line-orange"></polyline>
                  <circle
                    v-for="(point, index) in weightTrendPageData"
                    :key="`${point.date}-${index}`"
                    :cx="weightPointPositions[index]?.x || 0"
                    :cy="weightPointPositions[index]?.y || 0"
                    r="4"
                    class="line-point"
                    @mouseenter="weightHoverIndex = index"
                    @mouseleave="weightHoverIndex = -1"
                  />
                </svg>
                <div v-if="weightHoverPoint" class="chart-tooltip point-tooltip" :style="weightTooltipStyle">
                  {{ weightHoverPoint.date }} / {{ weightHoverPoint.value }}kg
                </div>
                <div class="trend-axis">
                  <span v-for="point in weightTrendPageData" :key="point.date">{{ point.date }}</span>
                </div>
              </div>
            </div>
            <div class="chart-pagination">
              <button class="ghost-btn small-btn" :disabled="weightTrendPage === 0" @click="weightTrendPage -= 1">上一页</button>
              <button class="ghost-btn small-btn" :disabled="weightTrendPage >= weightTrendPageCount - 1" @click="weightTrendPage += 1">下一页</button>
            </div>
            <p v-if="weightTrendPageCount <= 1" class="muted-copy trend-copy">
              当前周期内有效体重记录不足 {{ weightPageSize + 1 }} 天，因此暂无更多分页。
            </p>
          </div>

          <div class="trend-card apple-trend-card">
            <div class="trend-card-head">
              <h4>睡眠趋势</h4>
              <span>时长柱状图</span>
            </div>
            <div class="chart-stage">
              <div class="chart-y-axis">
                <span v-for="label in sleepAxisLabels" :key="label">{{ label }}</span>
              </div>
              <div class="chart-main">
                <div class="sleep-bars with-axis">
                  <div v-for="point in (dashboard.history?.sleepTrend || [])" :key="point.date" class="sleep-bar-item">
                    <div class="sleep-bar">
                      <div class="sleep-bar-fill" :style="{ height: `${sleepBarHeight(point.value)}px` }"></div>
                    </div>
                    <label>{{ point.date }}</label>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div class="trend-card apple-trend-card pressure-card">
            <div class="trend-card-head">
              <h4>血压趋势</h4>
              <span>有效 {{ dashboard.history?.pressureTrend?.length || 0 }} 天 / 第 {{ pressurePage + 1 }} 页</span>
            </div>
            <div class="pressure-list">
              <div v-for="point in pressurePageData" :key="point.date" class="pressure-item">
                <span>{{ point.date }}</span>
                <strong>{{ point.high || '-' }}/{{ point.low || '-' }}</strong>
              </div>
            </div>
            <div class="chart-pagination">
              <button class="ghost-btn small-btn" :disabled="pressurePage === 0" @click="pressurePage -= 1">上一页</button>
              <button class="ghost-btn small-btn" :disabled="pressurePage >= pressurePageCount - 1" @click="pressurePage += 1">下一页</button>
            </div>
            <p v-if="pressurePageCount <= 1" class="muted-copy trend-copy">
              当前周期内有效血压记录不足 {{ pressurePageSize + 1 }} 天，因此暂无更多分页。
            </p>
          </div>
        </div>
      </section>

      <section class="glass-panel">
        <div class="panel-head compact-head">
          <h3>周期总结</h3>
          <span class="pill">{{ range === 'week' ? '近 7 天' : '近 30 天' }}</span>
        </div>
        <div class="summary-strip summary-strip-single">
          <div class="summary-pill">
            <span>周打卡</span>
            <strong>{{ dashboard.weeklyMonthly?.weekly?.checkinCount || 0 }}</strong>
          </div>
          <div class="summary-pill">
            <span>周均睡眠</span>
            <strong>{{ dashboard.weeklyMonthly?.weekly?.avgSleepHours || 0 }}h</strong>
          </div>
          <div class="summary-pill">
            <span>月均运动</span>
            <strong>{{ dashboard.weeklyMonthly?.monthly?.avgExerciseMinutes || 0 }} 分</strong>
          </div>
          <div class="summary-pill">
            <span>月均压力</span>
            <strong>{{ dashboard.weeklyMonthly?.monthly?.avgStressLevel || 0 }}</strong>
          </div>
        </div>
      </section>

      <section class="glass-panel">
        <div class="panel-head compact-head">
          <h3>快捷入口</h3>
          <span class="pill">Workspace</span>
        </div>
        <div class="workspace-shortcuts">
          <button class="workspace-action" @click="router.push('/user/center/checkin')">
            <strong>每日打卡</strong>
            <span>记录睡眠、运动和备注，保留每日痕迹。</span>
          </button>
          <button class="workspace-action" @click="router.push('/user/center/articles')">
            <strong>健康资讯</strong>
            <span>浏览猜你喜欢、热门分类和最新发布。</span>
          </button>
          <button class="workspace-action" @click="router.push('/user/center/consultation')">
            <strong>医生咨询</strong>
            <span>提交睡眠、饮食或运动困扰，等待系统管理员匹配医生。</span>
          </button>
          <button class="workspace-action" @click="router.push('/user/center/feedback')">
            <strong>意见反馈</strong>
            <span>提交体验问题和优化建议，帮助系统迭代。</span>
          </button>
        </div>
      </section>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '../api'
import { session } from '../session'

const router = useRouter()
const user = ref(null)
const dashboard = ref(null)
const range = ref('week')
const weightTrendPage = ref(0)
const weightHoverIndex = ref(-1)
const pressurePage = ref(0)
const loadError = ref('')

const summarySnapshot = computed(() => dashboard.value?.summary?.snapshot || null)
const hasHealthData = computed(() => Boolean(dashboard.value?.summary?.hasData && summarySnapshot.value))
const latestConstitution = computed(() => summarySnapshot.value?.constitution || '-')
const summaryCards = computed(() => dashboard.value?.summary?.metrics || [])
const dimensionBars = computed(() => {
  const dimensions = summarySnapshot.value?.dimensions || {}
  return Object.entries(dimensions).map(([label, value]) => ({ label, value }))
})
const weightPageSize = 6
const pressurePageSize = 3
const weightTrendPageData = computed(() => paginateList(dashboard.value?.history?.weightTrend || [], weightTrendPage.value, weightPageSize))
const weightTrendPageCount = computed(() => Math.max(1, Math.ceil((dashboard.value?.history?.weightTrend || []).length / weightPageSize)))
const pressurePageData = computed(() => paginateList(dashboard.value?.history?.pressureTrend || [], pressurePage.value, pressurePageSize))
const pressurePageCount = computed(() => Math.max(1, Math.ceil((dashboard.value?.history?.pressureTrend || []).length / pressurePageSize)))
const weightPolyline = computed(() => buildPolyline(weightTrendPageData.value.map((item) => item.value)))
const weightAxisLabels = computed(() => buildWeightAxisLabels(weightTrendPageData.value))
const weightPointPositions = computed(() => buildPointPositions(weightTrendPageData.value.map((item) => item.value)))
const weightHoverPoint = computed(() => {
  if (weightHoverIndex.value < 0) return null
  return weightTrendPageData.value[weightHoverIndex.value] || null
})
const weightTooltipStyle = computed(() => {
  if (weightHoverIndex.value < 0 || !weightPointPositions.value[weightHoverIndex.value]) {
    return {}
  }
  const point = weightPointPositions.value[weightHoverIndex.value]
  return {
    left: `${Math.max(8, point.x - 16)}px`,
    top: `${Math.max(6, point.y - 42)}px`
  }
})
const sleepAxisLabels = computed(() => {
  const values = (dashboard.value?.history?.sleepTrend || [])
    .map((item) => item.value)
    .filter((value) => value !== null && value !== undefined)
  if (!values.length) return ['10h', '8h', '6h', '4h']
  const max = Math.max(...values, 8)
  const min = Math.min(...values, 4)
  const step = (max - min) / 3 || 1
  return [max, max - step, min + step, min].map((value) => `${Math.round(value * 10) / 10}h`)
})
const hasRecentRecommendation = computed(() => Boolean(dashboard.value?.summary?.recentResult))

function paginateList(list, page, size) {
  const start = page * size
  return list.slice(start, start + size)
}

function buildPolyline(values) {
  const valid = values.filter((value) => value !== null && value !== undefined)
  if (!valid.length) return '0,140 320,140'
  const min = Math.min(...valid)
  const max = Math.max(...valid)
  const span = max - min || 1
  return values.map((value, index) => {
    const x = values.length === 1 ? 160 : (index / (values.length - 1)) * 300 + 10
    const yValue = value == null ? min : value
    const y = 130 - ((yValue - min) / span) * 90
    return `${x},${y}`
  }).join(' ')
}

function buildPointPositions(values) {
  const valid = values.filter((value) => value !== null && value !== undefined)
  if (!valid.length) return []
  const min = Math.min(...valid)
  const max = Math.max(...valid)
  const span = max - min || 1
  return values.map((value, index) => {
    const x = values.length === 1 ? 160 : (index / (values.length - 1)) * 300 + 10
    const yValue = value == null ? min : value
    const y = 130 - ((yValue - min) / span) * 90
    return { x, y }
  })
}

function buildWeightAxisLabels(points) {
  const values = points.map((item) => item.value).filter((value) => value !== null && value !== undefined)
  if (!values.length) return ['0', '0', '0', '0']
  const max = Math.max(...values)
  const min = Math.min(...values)
  const step = (max - min) / 3 || 1
  return [max, max - step, min + step, min].map((value) => `${Math.round(value * 10) / 10}kg`)
}

function sleepBarHeight(value) {
  if (value == null || value === undefined) return 6
  return Math.max(14, Math.min(110, value * 12))
}

async function loadDashboard() {
  if (!user.value?.id) {
    router.replace('/user/auth')
    return
  }
  loadError.value = ''
  try {
    dashboard.value = await api.getUserDashboard(user.value.id, range.value)
    weightTrendPage.value = Math.min(weightTrendPage.value, weightTrendPageCount.value - 1)
    pressurePage.value = Math.min(pressurePage.value, pressurePageCount.value - 1)
  } catch (error) {
    dashboard.value = null
    loadError.value = error.message || '健康中心数据加载失败，请检查后端是否启动。'
  }
}

async function changeRange(nextRange) {
  range.value = nextRange
  await loadDashboard()
}

function openCurrentRecommendation() {
  session.setSelectedFavorite(null)
  router.push('/user/recommendation')
}

onMounted(async () => {
  user.value = session.getUser()
  if (!user.value) {
    router.replace('/user/auth')
    return
  }
  await loadDashboard()
})
</script>
