<template>
  <section class="workspace-page">
    <div class="workspace-hero apple-hero">
      <div>
        <span class="eyebrow">Health Knowledge</span>
        <h2>健康资讯与科普中心</h2>
        <p>围绕猜你喜欢、热门分类和最新发布三种推荐方式，按更清晰的内容层级来浏览文章。</p>
      </div>
      <div class="workspace-toolbar">
        <button class="ghost-btn" @click="router.push('/user/center/overview')">返回健康总览</button>
        <button class="primary-btn" @click="refreshHome">刷新内容</button>
      </div>
    </div>

    <div class="workspace-section-grid" v-if="homeData">
      <section class="glass-panel section-span-2">
        <div class="panel-head">
          <div>
            <span class="eyebrow">Guess You Like</span>
            <h3>猜你喜欢</h3>
          </div>
        </div>
        <div class="article-cards article-cards-wide">
          <article
            v-for="item in homeData.guessYouLike || []"
            :key="`guess-${item.id}`"
            class="article-card"
            :class="{ active: selectedArticle?.id === item.id }"
            @click="selectArticle(item)"
          >
            <span class="pill">{{ item.category }}</span>
            <h4>{{ item.title }}</h4>
            <p>{{ item.summary }}</p>
          </article>
        </div>
      </section>

      <section class="glass-panel section-span-2">
        <div class="panel-head">
          <div>
            <span class="eyebrow">Hot Categories</span>
            <h3>热门分类</h3>
          </div>
        </div>
        <div class="trend-grid">
          <article
            v-for="group in homeData.hotCategories || []"
            :key="group.category"
            class="trend-card article-category-card"
            @click="openCategory(group.category)"
          >
            <div class="trend-card-head">
              <h4>{{ group.category }}</h4>
              <span class="pill">{{ group.count }} 篇</span>
            </div>
            <p class="category-copy">点击分类卡片可查看该分类全部文章。</p>
            <div class="category-preview-list">
              <button
                v-for="item in group.articles || []"
                :key="`hot-${item.id}`"
                type="button"
                class="ghost-btn small-btn"
                @click.stop="selectArticle(item)"
              >
                {{ item.title }}
              </button>
            </div>
          </article>
        </div>
      </section>

      <section class="glass-panel">
        <div class="panel-head">
          <div>
            <span class="eyebrow">Search</span>
            <h3>文章搜索</h3>
          </div>
        </div>
        <div class="form-grid">
          <input v-model.trim="filters.keyword" type="text" placeholder="搜索标题、摘要、标签或分类" />
          <select v-model="filters.category">
            <option value="">全部分类</option>
            <option value="养生知识">养生知识</option>
            <option value="慢病预防">慢病预防</option>
            <option value="饮食指南">饮食指南</option>
            <option value="睡眠管理">睡眠管理</option>
            <option value="心理放松">心理放松</option>
          </select>
          <button class="primary-btn" @click="searchArticles">搜索</button>
        </div>

        <div v-if="searchResults.length" class="article-mini-list article-search-list">
          <article
            v-for="item in searchResults"
            :key="`search-${item.id}`"
            class="article-mini-item"
            :class="{ active: selectedArticle?.id === item.id }"
            @click="selectArticle(item)"
          >
            <span>{{ item.category }}</span>
            <strong>{{ item.title }}</strong>
            <p>{{ item.summary }}</p>
          </article>
        </div>
      </section>

      <section class="glass-panel">
        <div class="panel-head">
          <div>
            <span class="eyebrow">Latest</span>
            <h3>最新发布</h3>
          </div>
        </div>
        <div class="article-mini-list">
          <article
            v-for="item in homeData.latestArticles || []"
            :key="`latest-${item.id}`"
            class="article-mini-item"
            :class="{ active: selectedArticle?.id === item.id }"
            @click="selectArticle(item)"
          >
            <span>{{ item.category }}</span>
            <strong>{{ item.title }}</strong>
            <p>{{ item.summary }}</p>
          </article>
        </div>
      </section>

      <section class="glass-panel section-span-2">
        <div class="panel-head">
          <div>
            <span class="eyebrow">Article Detail</span>
            <h3>{{ selectedArticle?.title || '文章详情' }}</h3>
          </div>
          <span v-if="selectedArticle" class="pill">{{ selectedArticle.category }}</span>
        </div>
        <div v-if="selectedArticle" class="article-detail-copy">
          <p class="article-detail-summary">{{ selectedArticle.summary }}</p>
          <p>{{ selectedArticle.content }}</p>
        </div>
        <p v-else class="muted-copy trend-copy">从左侧任意推荐区或搜索结果中选择一篇文章即可查看详情。</p>
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
const homeData = ref(null)
const searchResults = ref([])
const selectedArticle = ref(null)
const filters = reactive({
  keyword: '',
  category: ''
})

async function loadHome(seed = Date.now()) {
  if (!user.value?.id) return
  homeData.value = await api.getUserArticleHome(user.value.id, seed)
  const first =
    homeData.value?.guessYouLike?.[0] ||
    homeData.value?.latestArticles?.[0] ||
    homeData.value?.hotCategories?.[0]?.articles?.[0] ||
    null
  if (!selectedArticle.value || !containsArticle(selectedArticle.value.id)) {
    selectedArticle.value = first
  }
}

function containsArticle(id) {
  const list = [
    ...(homeData.value?.guessYouLike || []),
    ...(homeData.value?.latestArticles || []),
    ...((homeData.value?.hotCategories || []).flatMap((group) => group.articles || []))
  ]
  return list.some((item) => item.id === id)
}

async function refreshHome() {
  searchResults.value = []
  await loadHome(Date.now())
}

async function searchArticles() {
  searchResults.value = await api.getUserArticles(filters)
  if (searchResults.value.length) {
    selectedArticle.value = searchResults.value[0]
  }
}

async function openCategory(category) {
  filters.category = category
  filters.keyword = ''
  searchResults.value = await api.getUserArticles({ category })
  if (searchResults.value.length) {
    selectedArticle.value = searchResults.value[0]
  }
}

function selectArticle(item) {
  selectedArticle.value = item
}

onMounted(async () => {
  user.value = session.getUser()
  if (!user.value) {
    router.replace('/user/auth')
    return
  }
  await loadHome()
})
</script>
