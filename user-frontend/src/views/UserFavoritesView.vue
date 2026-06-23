<template>
  <section class="workspace-page">
    <div class="workspace-hero apple-hero">
      <div>
        <span class="eyebrow">Favorite Plans</span>
        <h2>收藏方案记录</h2>
        <p>按收藏日期回看你保存过的康养方案，随时重新打开当时的详细内容。</p>
      </div>
      <div class="workspace-toolbar">
        <button class="ghost-btn" @click="router.push('/user/center/overview')">返回健康总览</button>
        <button class="primary-btn" @click="loadFavorites">刷新列表</button>
      </div>
    </div>

    <div v-if="loadError" class="inline-feedback error">{{ loadError }}</div>

    <div class="workspace-section-grid">
      <section class="glass-panel">
        <div class="panel-head">
          <div>
            <span class="eyebrow">Date Filter</span>
            <h3>按日期查找</h3>
          </div>
        </div>
        <div class="checkin-filter">
          <input v-model="selectedDate" type="date" />
          <button class="ghost-btn small-btn" @click="applyDateFilter">查找</button>
        </div>
        <p class="muted-copy trend-copy">
          不选日期时显示全部收藏；选择日期后，只显示该日期收藏的康养方案。
        </p>
      </section>

      <section class="glass-panel">
        <div class="panel-head">
          <div>
            <span class="eyebrow">Summary</span>
            <h3>收藏概览</h3>
          </div>
        </div>
        <div class="summary-strip summary-strip-single">
          <div class="summary-pill">
            <span>收藏总数</span>
            <strong>{{ favorites.length }}</strong>
          </div>
          <div class="summary-pill">
            <span>筛选结果</span>
            <strong>{{ filteredFavorites.length }}</strong>
          </div>
        </div>
      </section>

      <section class="glass-panel section-span-2">
        <div class="panel-head">
          <div>
            <span class="eyebrow">Favorite Records</span>
            <h3>{{ selectedDate ? `${selectedDate} 的收藏方案` : '全部收藏方案' }}</h3>
          </div>
        </div>
        <div class="list-block">
          <article
            v-for="item in filteredFavorites"
            :key="item.id"
            class="record-card favorite-record-card"
            @click="openFavorite(item)"
          >
            <div class="record-head">
              <div>
                <h3>{{ item.planTitle }}</h3>
                <p>{{ item.constitutionType }} / {{ item.healthLevel }}</p>
              </div>
              <span class="pill">{{ formatDate(item.createTime, 'date') }}</span>
            </div>
            <p><strong>运动建议：</strong>{{ item.sport || '暂无' }}</p>
            <p><strong>作息建议：</strong>{{ item.lifestyle || '暂无' }}</p>
          </article>
          <p v-if="!filteredFavorites.length" class="muted-copy trend-copy">
            当前筛选条件下没有收藏记录。你可以先在康养方案页收藏几条，再回到这里查看。
          </p>
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
const favorites = ref([])
const selectedDate = ref('')
const activeDate = ref('')
const loadError = ref('')

const filteredFavorites = computed(() => {
  if (!activeDate.value) return favorites.value
  return favorites.value.filter((item) => formatDate(item.createTime, 'date') === activeDate.value)
})

async function loadFavorites() {
  if (!user.value?.id) {
    router.replace('/user/auth')
    return
  }
  loadError.value = ''
  try {
    favorites.value = await api.getFavorites(user.value.id)
  } catch (error) {
    loadError.value = error.message || '收藏方案加载失败。'
  }
}

function applyDateFilter() {
  activeDate.value = selectedDate.value
}

function openFavorite(item) {
  session.setSelectedFavorite(item)
  router.push('/user/recommendation')
}

function formatDate(value, mode = 'datetime') {
  if (!value) return ''
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return mode === 'date' ? String(value).slice(0, 10) : value
  }
  const base = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
  if (mode === 'date') return base
  return `${base} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

onMounted(async () => {
  user.value = session.getUser()
  if (!user.value) {
    router.replace('/user/auth')
    return
  }
  await loadFavorites()
})
</script>
