<template>
  <section class="dashboard-layout doctor-apple-layout">
    <aside class="panel dashboard-side doctor-apple-side">
      <div>
        <h2>{{ admin?.displayName || admin?.username }}</h2>
      </div>
      <div class="nav-stack user-nav-stack">
        <button class="nav-btn user-nav-btn" :class="{ active: tab === 'overview' }" @click="switchTab('overview')">数据统计大盘</button>
        <button class="nav-btn user-nav-btn" :class="{ active: tab === 'proposals' }" @click="switchTab('proposals')">医生提案审核</button>
        <button class="nav-btn user-nav-btn" :class="{ active: tab === 'consultations' }" @click="switchTab('consultations')">医患匹配</button>
        <button class="nav-btn user-nav-btn" :class="{ active: tab === 'doctors' }" @click="switchTab('doctors')">医生管理</button>
        <button class="nav-btn user-nav-btn" :class="{ active: tab === 'users' }" @click="switchTab('users')">用户管理</button>
        <button class="nav-btn user-nav-btn" :class="{ active: tab === 'rules' }" @click="switchTab('rules')">评测规则</button>
        <button class="nav-btn user-nav-btn" :class="{ active: tab === 'plans' }" @click="switchTab('plans')">康养方案</button>
        <button class="nav-btn user-nav-btn" :class="{ active: tab === 'questions' }" @click="switchTab('questions')">题库管理</button>
        <button class="nav-btn user-nav-btn" :class="{ active: tab === 'articles' }" @click="switchTab('articles')">资讯管理</button>
        <button class="nav-btn user-nav-btn" :class="{ active: tab === 'feedbacks' }" @click="switchTab('feedbacks')">用户反馈</button>
        <button class="nav-btn user-nav-btn" :class="{ active: tab === 'healthData' }" @click="switchTab('healthData')">健康数据</button>
        <button class="nav-btn user-nav-btn" :class="{ active: tab === 'results' }" @click="switchTab('results')">评测结果</button>
      </div>
      <button class="ghost-btn" @click="logout">退出登录</button>
    </aside>

    <main class="content-stack doctor-apple-main">
      <section class="panel dashboard-top doctor-apple-top">
        <div>
          <h2>{{ tabTitle }}</h2>
        </div>
      </section>

      <div v-if="loadError" class="inline-feedback error">{{ loadError }}</div>

      <Transition name="workspace-fade" mode="out-in">
        <div :key="tab" class="doctor-tab-stage">
          <section v-if="tab === 'overview'" class="content-stack">
            <div class="stats-grid">
              <article v-for="item in statCards" :key="item.label" class="panel stats-card">
                <span>{{ item.label }}</span>
                <strong>{{ item.value }}</strong>
                <p>{{ item.note }}</p>
              </article>
            </div>
          </section>

          <section v-if="tab === 'proposals'" class="two-column">
            <div class="panel stage-card">
              <div class="panel-head">
                <h2>待审核提案</h2>
                <button class="ghost-btn small-btn" @click="loadProposals">刷新</button>
              </div>
              <div class="list-block">
                <article
                  v-for="item in proposals"
                  :key="item.id"
                  class="record-card"
                  :class="{ 'record-card-active': selectedProposal?.id === item.id }"
                  @click="selectedProposal = item"
                >
                  <div class="record-head">
                    <div>
                      <h3>{{ item.title || `${item.targetType} 提案` }}</h3>
                      <p>{{ item.proposerName || '医生' }} / {{ item.targetType }} / {{ item.actionType }}</p>
                    </div>
                    <span class="pill">{{ item.status }}</span>
                  </div>
                  <p>{{ item.summary || '暂无摘要' }}</p>
                </article>
              </div>
            </div>

            <div class="panel stage-card" v-if="selectedProposal">
              <div class="panel-head"><h2>审核提案</h2></div>
              <div class="visual-copy-list" style="margin-bottom: 16px;">
                <div class="visual-copy-item">
                  <label>提案内容</label>
                  <p>{{ selectedProposal.summary }}</p>
                </div>
                <div class="visual-copy-item">
                  <label>载荷 JSON</label>
                  <pre class="code-block">{{ selectedProposal.payloadJson }}</pre>
                </div>
              </div>
              <form class="form-grid" @submit.prevent="approveProposal">
                <label>
                  <span>审核意见</span>
                  <textarea v-model.trim="proposalReviewComment" rows="5" placeholder="填写通过或驳回原因"></textarea>
                </label>
                <div class="action-row">
                  <button type="button" class="ghost-btn" @click="rejectProposal">驳回</button>
                  <button class="primary-btn">通过并正式落库</button>
                </div>
              </form>
            </div>
          </section>

          <section v-if="tab === 'consultations'" class="two-column">
            <div class="panel stage-card">
              <div class="panel-head">
                <h2>待分配咨询</h2>
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
                      <p>{{ item.issueType }} / {{ item.username || `用户#${item.userId}` }}</p>
                    </div>
                    <span class="pill">{{ item.status }}</span>
                  </div>
                  <p>{{ item.detail || '暂无详细说明' }}</p>
                </article>
              </div>
            </div>

            <div class="panel stage-card" v-if="selectedConsultation">
              <div class="panel-head"><h2>匹配医生</h2></div>
              <div class="visual-copy-list">
                <div class="visual-copy-item">
                  <label>当前咨询</label>
                  <strong>{{ selectedConsultation.title }}</strong>
                  <p>{{ selectedConsultation.detail }}</p>
                </div>
              </div>
              <label style="display: block; margin: 16px 0 8px;">
                <span>选择医生</span>
                <select v-model.number="consultationAssignDoctorId">
                  <option :value="null">请选择医生</option>
                  <option v-for="doctor in matchedDoctors" :key="doctor.doctorId" :value="doctor.doctorId">
                    {{ doctor.username }} / {{ doctor.specialty || '未填写方向' }}
                  </option>
                </select>
              </label>
              <label style="display: block;">
                <span>管理员备注</span>
                <textarea v-model.trim="consultationAssignNote" rows="4" placeholder="说明分配原因，便于医生快速进入状态"></textarea>
              </label>
              <button class="primary-btn" style="margin-top: 16px;" @click="assignConsultation">确认分配</button>
            </div>
          </section>

          <section v-if="tab === 'doctors'" class="panel stage-card">
            <div class="panel-head">
              <h2>医生目录</h2>
              <button class="ghost-btn small-btn" @click="loadDoctors">刷新</button>
            </div>
            <div class="list-block">
              <article v-for="item in doctors" :key="item.id || item.doctorId" class="record-card">
                <div class="record-head">
                  <div>
                    <h3>{{ item.username }}</h3>
                    <p>{{ item.specialty || '未填写专业方向' }}</p>
                  </div>
                  <span class="pill">{{ item.status === 0 ? '停用' : '可分配' }}</span>
                </div>
                <p><strong>擅长标签：</strong>{{ item.expertiseTags || '暂无' }}</p>
                <p><strong>简介：</strong>{{ item.introduction || '暂无' }}</p>
              </article>
            </div>
          </section>

          <section v-if="tab === 'users'" class="panel stage-card">
            <div class="panel-head"><h2>用户管理</h2><button class="ghost-btn small-btn" @click="loadUsers">刷新</button></div>
            <div class="table-wrap">
              <table>
                <thead><tr><th>ID</th><th>用户名</th><th>年龄</th><th>性别</th><th>手机号</th><th>最新等级</th><th>最新结论</th></tr></thead>
                <tbody>
                  <tr v-for="item in users" :key="item.id">
                    <td>{{ item.id }}</td><td>{{ item.username }}</td><td>{{ item.age ?? '-' }}</td><td>{{ item.gender || '-' }}</td><td>{{ item.phone || '-' }}</td><td>{{ item.latestHealthLevel || '-' }}</td><td>{{ item.latestConstitution || '-' }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </section>

          <section v-if="tab === 'rules'" class="two-column">
            <div class="panel stage-card">
              <div class="panel-head"><h2>评测规则配置</h2></div>
              <form class="form-grid" @submit.prevent="submitRule">
                <label><span>规则标识</span><input v-model.trim="ruleForm.ruleKey" type="text" /></label>
                <label><span>规则名称</span><input v-model.trim="ruleForm.ruleName" type="text" /></label>
                <label><span>规则值</span><input v-model.trim="ruleForm.ruleValue" type="text" /></label>
                <label><span>说明</span><textarea v-model.trim="ruleForm.description" rows="4"></textarea></label>
                <button class="primary-btn">保存规则</button>
              </form>
            </div>
            <div class="panel stage-card">
              <div class="panel-head"><h2>规则列表</h2><button class="ghost-btn small-btn" @click="loadRules">刷新</button></div>
              <div class="list-block">
                <article v-for="item in rules" :key="item.id" class="record-card">
                  <div class="record-head">
                    <div><h3>{{ item.ruleName || item.ruleKey }}</h3><p>{{ item.ruleKey }}</p></div>
                    <span class="pill">{{ item.ruleValue }}</span>
                  </div>
                  <p>{{ item.description }}</p>
                  <div class="action-row"><button class="ghost-btn small-btn" @click="editRule(item)">编辑</button></div>
                </article>
              </div>
            </div>
          </section>

          <section v-if="tab === 'plans'" class="two-column">
            <div class="panel stage-card">
              <div class="panel-head"><h2>{{ planForm.id ? '编辑康养方案' : '新增康养方案' }}</h2><button class="text-btn" @click="resetPlanForm">清空</button></div>
              <form class="form-grid" @submit.prevent="submitPlan">
                <label><span>标题</span><input v-model.trim="planForm.title" type="text" /></label>
                <label><span>体质类型</span><input v-model.trim="planForm.constitutionType" type="text" /></label>
                <label><span>健康等级</span><input v-model.trim="planForm.healthLevel" type="text" /></label>
                <label><span>饮食建议</span><textarea v-model.trim="planForm.diet" rows="3"></textarea></label>
                <label><span>饮品建议</span><textarea v-model.trim="planForm.drink" rows="3"></textarea></label>
                <label><span>运动建议</span><textarea v-model.trim="planForm.sport" rows="3"></textarea></label>
                <label><span>作息建议</span><textarea v-model.trim="planForm.lifestyle" rows="3"></textarea></label>
                <button class="primary-btn">{{ planForm.id ? '保存修改' : '新增方案' }}</button>
              </form>
            </div>
            <div class="panel stage-card">
              <div class="panel-head"><h2>方案列表</h2><button class="ghost-btn small-btn" @click="loadPlans">刷新</button></div>
              <div class="list-block">
                <article v-for="plan in plans" :key="plan.id" class="record-card">
                  <div class="record-head"><div><h3>{{ plan.title }}</h3><p>{{ plan.constitutionType }} / {{ plan.healthLevel }}</p></div><span class="pill">ID {{ plan.id }}</span></div>
                  <p><strong>饮食：</strong>{{ plan.diet }}</p>
                  <div class="action-row"><button class="ghost-btn small-btn" @click="editPlan(plan)">编辑</button><button class="danger-btn small-btn" @click="removePlan(plan.id)">删除</button></div>
                </article>
              </div>
            </div>
          </section>

          <section v-if="tab === 'questions'" class="two-column">
            <div class="panel stage-card">
              <div class="panel-head"><h2>{{ questionForm.id ? '编辑题目' : '新增题目' }}</h2><button class="text-btn" @click="resetQuestionForm">清空</button></div>
              <form class="form-grid" @submit.prevent="submitQuestion">
                <label><span>题目内容</span><textarea v-model.trim="questionForm.content" rows="3"></textarea></label>
                <label><span>题目类型</span><select v-model.number="questionForm.type"><option :value="1">正向题</option><option :value="2">负向题</option></select></label>
                <label><span>体质分类</span><input v-model.trim="questionForm.category" type="text" /></label>
                <label><span>评估维度</span><select v-model="questionForm.dimension"><option value="身体健康状况">身体健康状况</option><option value="生活习惯评估">生活习惯评估</option><option value="心理健康状况">心理健康状况</option><option value="睡眠质量评估">睡眠质量评估</option></select></label>
                <label><span>适用体质</span><input v-model.trim="questionForm.applicableConstitution" type="text" /></label>
                <label><span>适用等级</span><input v-model.trim="questionForm.applicableHealthLevel" type="text" /></label>
                <label><span>排序值</span><input v-model.number="questionForm.sortOrder" type="number" /></label>
                <div class="option-editor">
                  <div class="panel-head"><h3>选项</h3><button type="button" class="ghost-btn small-btn" @click="addOption">新增</button></div>
                  <div class="option-editor-list">
                    <div v-for="(option, index) in questionForm.options" :key="index" class="option-editor-row">
                      <input v-model.trim="option.content" type="text" />
                      <input v-model.number="option.score" type="number" />
                      <button type="button" class="danger-btn small-btn" :disabled="questionForm.options.length <= 1" @click="removeOption(index)">删除</button>
                    </div>
                  </div>
                </div>
                <button class="primary-btn">{{ questionForm.id ? '保存修改' : '新增题目' }}</button>
              </form>
            </div>
            <div class="panel stage-card">
              <div class="panel-head"><h2>题库列表</h2><button class="ghost-btn small-btn" @click="loadQuestions">刷新</button></div>
              <div class="list-block">
                <article v-for="question in questions" :key="question.id" class="record-card">
                  <div class="record-head"><div><h3>{{ question.content }}</h3><p>{{ question.dimension }} / {{ question.category }}</p></div><span class="pill">ID {{ question.id }}</span></div>
                  <div class="action-row"><button class="ghost-btn small-btn" @click="editQuestion(question)">编辑</button><button class="danger-btn small-btn" @click="removeQuestion(question.id)">删除</button></div>
                </article>
              </div>
            </div>
          </section>

          <section v-if="tab === 'articles'" class="two-column">
            <div class="panel stage-card">
              <div class="panel-head"><h2>{{ articleForm.id ? '编辑资讯' : '发布资讯' }}</h2><button class="text-btn" @click="resetArticleForm">清空</button></div>
              <form class="form-grid" @submit.prevent="submitArticle">
                <label><span>标题</span><input v-model.trim="articleForm.title" type="text" /></label>
                <label><span>分类</span><input v-model.trim="articleForm.category" type="text" /></label>
                <label><span>摘要</span><textarea v-model.trim="articleForm.summary" rows="3"></textarea></label>
                <label><span>正文</span><textarea v-model.trim="articleForm.content" rows="7"></textarea></label>
                <label><span>标签</span><input v-model.trim="articleForm.tags" type="text" /></label>
                <label><span>封面图</span><input v-model.trim="articleForm.coverImage" type="text" /></label>
                <label><span>状态</span><select v-model.number="articleForm.status"><option :value="1">发布</option><option :value="0">草稿</option></select></label>
                <button class="primary-btn">{{ articleForm.id ? '保存资讯' : '发布资讯' }}</button>
              </form>
            </div>
            <div class="panel stage-card">
              <div class="panel-head"><h2>资讯列表</h2><button class="ghost-btn small-btn" @click="loadArticles">刷新</button></div>
              <div class="list-block">
                <article v-for="item in articles" :key="item.id" class="record-card">
                  <div class="record-head"><div><h3>{{ item.title }}</h3><p>{{ item.category }} / {{ item.status === 1 ? '已发布' : '草稿' }}</p></div><span class="pill">ID {{ item.id }}</span></div>
                  <p>{{ item.summary }}</p>
                  <div class="action-row"><button class="ghost-btn small-btn" @click="editArticle(item)">编辑</button><button class="danger-btn small-btn" @click="removeArticle(item.id)">删除</button></div>
                </article>
              </div>
            </div>
          </section>

          <section v-if="tab === 'feedbacks'" class="panel stage-card">
            <div class="panel-head"><h2>用户意见反馈</h2><button class="ghost-btn small-btn" @click="loadFeedbacks">刷新</button></div>
            <div class="list-block">
              <article v-for="item in feedbacks" :key="item.id" class="record-card">
                <div class="record-head"><div><h3>{{ item.username || `用户 #${item.userId}` }}</h3><p>{{ formatTime(item.createTime) }}</p></div><span class="pill">反馈</span></div>
                <p>{{ item.content }}</p>
              </article>
            </div>
          </section>

          <section v-if="tab === 'healthData'" class="panel stage-card">
            <div class="panel-head"><h2>用户健康数据</h2><button class="ghost-btn small-btn" @click="loadHealthData">刷新</button></div>
            <div class="table-wrap">
              <table>
                <thead><tr><th>用户</th><th>血压</th><th>血糖</th><th>心率</th><th>血氧</th><th>身高/体重</th><th>时间</th></tr></thead>
                <tbody>
                  <tr v-for="item in healthDataList" :key="item.id">
                    <td>{{ item.username || item.userId }}</td>
                    <td>{{ item.bloodPressureHigh ?? '-' }}/{{ item.bloodPressureLow ?? '-' }}（{{ item.bloodPressureContext || '-' }}）</td>
                    <td>{{ item.bloodSugar ?? '-' }}（{{ item.bloodSugarContext || '-' }}）</td>
                    <td>{{ item.heartRate ?? '-' }}（{{ item.heartRateContext || '-' }}）</td>
                    <td>{{ item.bloodOxygen ?? '-' }}（{{ item.bloodOxygenContext || '-' }}）</td>
                    <td>{{ item.height ?? '-' }} / {{ item.weight ?? '-' }}</td>
                    <td>{{ formatTime(item.createTime) }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </section>

          <section v-if="tab === 'results'" class="panel stage-card">
            <div class="panel-head"><h2>健康评测结果</h2><button class="ghost-btn small-btn" @click="loadResults">刷新</button></div>
            <div class="table-wrap">
              <table>
                <thead><tr><th>用户</th><th>评分</th><th>健康等级</th><th>结论</th><th>时间</th></tr></thead>
                <tbody>
                  <tr v-for="item in resultsList" :key="item.id">
                    <td>{{ item.username || item.userId }}</td><td>{{ item.score ?? '-' }}</td><td>{{ item.healthLevel || '-' }}</td><td>{{ item.constitutionType || '-' }}</td><td>{{ formatTime(item.createTime) }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </section>
        </div>
      </Transition>
    </main>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { api } from '../api'
import { session } from '../session'

const router = useRouter()
const admin = ref(null)
const tab = ref('overview')
const stats = ref(null)
const users = ref([])
const doctors = ref([])
const rules = ref([])
const plans = ref([])
const questions = ref([])
const articles = ref([])
const feedbacks = ref([])
const healthDataList = ref([])
const resultsList = ref([])
const proposals = ref([])
const selectedProposal = ref(null)
const consultations = ref([])
const selectedConsultation = ref(null)
const matchedDoctors = ref([])
const consultationAssignDoctorId = ref(null)
const consultationAssignNote = ref('')
const proposalReviewComment = ref('')
const loadError = ref('')

const planForm = reactive({ id: null, title: '', constitutionType: '', healthLevel: '', diet: '', drink: '', sport: '', lifestyle: '' })
const articleForm = reactive({ id: null, title: '', category: '', summary: '', content: '', tags: '', coverImage: '', status: 1 })
const ruleForm = reactive({ ruleKey: '', ruleName: '', ruleValue: '', description: '' })
const questionForm = reactive({
  id: null, content: '', type: 1, category: '', dimension: '身体健康状况',
  applicableConstitution: '通用', applicableHealthLevel: '通用', sortOrder: 0, isActive: 1,
  options: [{ content: '', score: 1 }, { content: '', score: 2 }]
})

const tabTitle = computed(() => ({
  overview: '数据统计大盘',
  proposals: '医生提案审核',
  consultations: '用户医生匹配',
  doctors: '医生目录',
  users: '用户管理',
  rules: '评测规则配置',
  plans: '康养方案管理',
  questions: '题库管理',
  articles: '资讯管理',
  feedbacks: '用户反馈',
  healthData: '用户健康数据',
  results: '融合评测结果'
}[tab.value] || '管理员工作台'))

const statCards = computed(() => {
  if (!stats.value) return []
  return [
    { label: '用户总量', value: stats.value.userCount || 0, note: '平台注册用户数' },
    { label: '医生数量', value: doctors.value.length || 0, note: '可匹配医生数量' },
    { label: '题库题目', value: stats.value.questionCount || 0, note: '动态题库总量' },
    { label: '方案数量', value: stats.value.planCount || 0, note: '康养方案库' },
    { label: '咨询申请', value: consultations.value.length || 0, note: '全部咨询工单数' }
  ]
})

function resetPlanForm() { Object.assign(planForm, { id: null, title: '', constitutionType: '', healthLevel: '', diet: '', drink: '', sport: '', lifestyle: '' }) }
function resetQuestionForm() {
  Object.assign(questionForm, {
    id: null, content: '', type: 1, category: '', dimension: '身体健康状况',
    applicableConstitution: '通用', applicableHealthLevel: '通用', sortOrder: 0, isActive: 1,
    options: [{ content: '', score: 1 }, { content: '', score: 2 }]
  })
}
function resetArticleForm() { Object.assign(articleForm, { id: null, title: '', category: '', summary: '', content: '', tags: '', coverImage: '', status: 1 }) }
function addOption() { questionForm.options.push({ content: '', score: 1 }) }
function removeOption(index) { if (questionForm.options.length > 1) questionForm.options.splice(index, 1) }

async function safeLoad(loader) {
  loadError.value = ''
  try {
    await loader()
  } catch (error) {
    loadError.value = error.message || '数据加载失败'
  }
}

async function loadStats() { stats.value = await api.getAdminStats() }
async function loadUsers() { users.value = await api.getAdminUsers() }
async function loadDoctors() { doctors.value = await api.getAdminDoctors() }
async function loadRules() { rules.value = await api.getAdminRules() }
async function loadPlans() { plans.value = await api.getAdminPlans() }
async function loadQuestions() { questions.value = await api.getAdminQuestions() }
async function loadArticles() { articles.value = await api.getAdminArticles() }
async function loadFeedbacks() { feedbacks.value = await api.getDoctorFeedbacks() }
async function loadHealthData() { healthDataList.value = await api.getHealthData() }
async function loadResults() { resultsList.value = await api.getResults() }
async function loadProposals() { proposals.value = await api.getAdminProposals('PENDING'); selectedProposal.value = proposals.value[0] || null }
async function loadConsultations() { consultations.value = await api.getAdminConsultations(); selectedConsultation.value = consultations.value[0] || null; await loadDoctorMatches() }
async function loadDoctorMatches() { matchedDoctors.value = await api.getDoctorMatches(selectedConsultation.value?.preferredTag || '') }

function editPlan(plan) { Object.assign(planForm, { ...plan }) }
async function submitPlan() {
  const payload = { ...planForm }; delete payload.id
  if (planForm.id) await api.updatePlan(planForm.id, payload, admin.value.username)
  else await api.createPlan(payload, admin.value.username)
  resetPlanForm(); await loadPlans()
}
async function removePlan(id) { if (window.confirm('确定删除该方案吗？')) { await api.deletePlan(id, admin.value.username); await loadPlans() } }

function editQuestion(question) {
  Object.assign(questionForm, {
    id: question.id, content: question.content, type: question.type, category: question.category, dimension: question.dimension,
    applicableConstitution: question.applicableConstitution || '通用', applicableHealthLevel: question.applicableHealthLevel || '通用',
    sortOrder: question.sortOrder ?? 0, isActive: question.isActive ?? 1,
    options: question.options.map((item) => ({ id: item.id, content: item.content, score: item.score }))
  })
}
async function submitQuestion() {
  const payload = {
    content: questionForm.content, type: Number(questionForm.type), category: questionForm.category, dimension: questionForm.dimension,
    applicableConstitution: questionForm.applicableConstitution, applicableHealthLevel: questionForm.applicableHealthLevel,
    sortOrder: Number(questionForm.sortOrder || 0), isActive: Number(questionForm.isActive ?? 1),
    options: questionForm.options.map((item) => ({ content: item.content, score: Number(item.score) }))
  }
  if (questionForm.id) await api.updateQuestion(questionForm.id, payload, admin.value.username)
  else await api.createQuestion(payload, admin.value.username)
  resetQuestionForm(); await loadQuestions()
}
async function removeQuestion(id) { if (window.confirm('确定删除该题目吗？')) { await api.deleteQuestion(id, admin.value.username); await loadQuestions() } }

function editArticle(item) { Object.assign(articleForm, { ...item }) }
async function submitArticle() {
  const payload = { ...articleForm }; delete payload.id
  if (articleForm.id) await api.updateArticle(articleForm.id, payload, admin.value.username)
  else await api.createArticle(payload, admin.value.username)
  resetArticleForm(); await loadArticles()
}
async function removeArticle(id) { if (window.confirm('确定删除该资讯吗？')) { await api.deleteArticle(id, admin.value.username); await loadArticles() } }

function editRule(item) { Object.assign(ruleForm, { ...item }) }
async function submitRule() { await api.saveAdminRule({ ...ruleForm }, admin.value.username); Object.assign(ruleForm, { ruleKey: '', ruleName: '', ruleValue: '', description: '' }); await loadRules() }

async function approveProposal() {
  if (!selectedProposal.value) return
  await api.reviewProposal(selectedProposal.value.id, {
    reviewerAdminId: admin.value.id || admin.value.adminId,
    reviewerName: admin.value.displayName || admin.value.username,
    status: 'APPROVED',
    reviewComment: proposalReviewComment.value
  })
  proposalReviewComment.value = ''
  await loadProposals()
}

async function rejectProposal() {
  if (!selectedProposal.value) return
  await api.reviewProposal(selectedProposal.value.id, {
    reviewerAdminId: admin.value.id || admin.value.adminId,
    reviewerName: admin.value.displayName || admin.value.username,
    status: 'REJECTED',
    reviewComment: proposalReviewComment.value
  })
  proposalReviewComment.value = ''
  await loadProposals()
}

async function selectConsultation(item) {
  selectedConsultation.value = item
  consultationAssignDoctorId.value = null
  consultationAssignNote.value = ''
  await loadDoctorMatches()
}

async function assignConsultation() {
  if (!selectedConsultation.value || !consultationAssignDoctorId.value) return
  await api.assignConsultation(selectedConsultation.value.id, {
    doctorId: consultationAssignDoctorId.value,
    adminId: admin.value.id || admin.value.adminId,
    adminName: admin.value.displayName || admin.value.username,
    adminNote: consultationAssignNote.value
  })
  consultationAssignDoctorId.value = null
  consultationAssignNote.value = ''
  await loadConsultations()
}

async function switchTab(nextTab) {
  tab.value = nextTab
  if (nextTab === 'overview') await safeLoad(async () => { await loadStats(); await loadDoctors(); await loadConsultations() })
  if (nextTab === 'proposals') await safeLoad(loadProposals)
  if (nextTab === 'consultations') await safeLoad(loadConsultations)
  if (nextTab === 'doctors') await safeLoad(loadDoctors)
  if (nextTab === 'users') await safeLoad(loadUsers)
  if (nextTab === 'rules') await safeLoad(loadRules)
  if (nextTab === 'plans') await safeLoad(loadPlans)
  if (nextTab === 'questions') await safeLoad(loadQuestions)
  if (nextTab === 'articles') await safeLoad(loadArticles)
  if (nextTab === 'feedbacks') await safeLoad(loadFeedbacks)
  if (nextTab === 'healthData') await safeLoad(loadHealthData)
  if (nextTab === 'results') await safeLoad(loadResults)
}

function logout() { session.clearAdmin(); router.push('/admin/auth') }
function formatTime(value) {
  if (!value) return '-'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

onMounted(async () => {
  admin.value = session.getAdmin()
  if (!admin.value) {
    router.replace('/admin/auth')
    return
  }
  await switchTab('overview')
})
</script>
