<template>
  <section class="page-grid">
    <aside class="panel intro-card">
      <p class="eyebrow">Doctor Portal</p>
      <h2>医生端管理中心</h2>
      <p>医生端现在支持注册、登录、康养方案管理、题库维护，以及查看用户健康数据和融合评测结果。</p>
      <div class="info-list">
        <div>医生注册与登录</div>
        <div>康养方案管理</div>
        <div>动态题库管理</div>
        <div>健康数据与评测结果查看</div>
      </div>
    </aside>

    <main class="content-stack">
      <section v-if="!doctor" class="panel">
        <div class="tab-row">
          <button class="mini-tab" :class="{ active: authMode === 'login' }" @click="authMode = 'login'">登录</button>
          <button class="mini-tab" :class="{ active: authMode === 'register' }" @click="authMode = 'register'">注册</button>
        </div>

        <form v-if="authMode === 'login'" class="form-grid" @submit.prevent="login">
          <label><span>用户名</span><input v-model.trim="loginForm.username" type="text" /></label>
          <label><span>密码</span><input v-model.trim="loginForm.password" type="password" /></label>
          <button class="primary-btn" :disabled="loading.login">{{ loading.login ? '登录中...' : '登录医生端' }}</button>
        </form>

        <form v-else class="form-grid" @submit.prevent="register">
          <label><span>用户名</span><input v-model.trim="registerForm.username" type="text" /></label>
          <label><span>密码</span><input v-model.trim="registerForm.password" type="password" /></label>
          <button class="primary-btn" :disabled="loading.register">{{ loading.register ? '提交中...' : '注册医生账号' }}</button>
        </form>
      </section>

      <template v-else>
        <section class="panel">
          <div class="panel-head">
            <div>
              <p class="eyebrow">Doctor Workspace</p>
              <h2>当前登录：{{ doctor.username }}</h2>
            </div>
            <div class="switch-group">
              <button class="switch-btn small" :class="{ active: tab === 'plans' }" @click="switchTab('plans')">康养方案</button>
              <button class="switch-btn small" :class="{ active: tab === 'questions' }" @click="switchTab('questions')">动态题库</button>
              <button class="switch-btn small" :class="{ active: tab === 'healthData' }" @click="switchTab('healthData')">健康数据</button>
              <button class="switch-btn small" :class="{ active: tab === 'results' }" @click="switchTab('results')">评测结果</button>
              <button class="ghost-btn" @click="logout">退出登录</button>
            </div>
          </div>
        </section>

        <section v-if="tab === 'plans'" class="two-column">
          <div class="panel">
            <div class="panel-head">
              <h2>{{ planForm.id ? '编辑康养方案' : '新增康养方案' }}</h2>
              <button class="text-btn" @click="resetPlanForm">清空表单</button>
            </div>
            <form class="form-grid" @submit.prevent="submitPlan">
              <label><span>标题</span><input v-model.trim="planForm.title" type="text" /></label>
              <label><span>体质类型</span><input v-model.trim="planForm.constitutionType" type="text" /></label>
              <label><span>健康等级</span><input v-model.trim="planForm.healthLevel" type="text" /></label>
              <label><span>饮食建议</span><textarea v-model.trim="planForm.diet" rows="3"></textarea></label>
              <label><span>饮品建议</span><textarea v-model.trim="planForm.drink" rows="3"></textarea></label>
              <label><span>运动建议</span><textarea v-model.trim="planForm.sport" rows="3"></textarea></label>
              <label><span>作息建议</span><textarea v-model.trim="planForm.lifestyle" rows="3"></textarea></label>
              <button class="primary-btn" :disabled="loading.plan">{{ loading.plan ? '提交中...' : planForm.id ? '保存修改' : '新增方案' }}</button>
            </form>
          </div>

          <div class="panel">
            <div class="panel-head">
              <h2>方案列表</h2>
              <button class="ghost-btn" @click="loadPlans">刷新</button>
            </div>
            <div class="list-block">
              <article v-for="plan in plans" :key="plan.id" class="record-card">
                <div class="record-head">
                  <div>
                    <h3>{{ plan.title }}</h3>
                    <p>{{ plan.constitutionType }} / {{ plan.healthLevel }}</p>
                  </div>
                  <span class="pill">ID {{ plan.id }}</span>
                </div>
                <p><strong>饮食：</strong>{{ plan.diet }}</p>
                <p><strong>饮品：</strong>{{ plan.drink }}</p>
                <p><strong>运动：</strong>{{ plan.sport }}</p>
                <p><strong>作息：</strong>{{ plan.lifestyle }}</p>
                <div class="action-row">
                  <button class="ghost-btn" @click="editPlan(plan)">编辑</button>
                  <button class="danger-btn" @click="removePlan(plan.id)">删除</button>
                </div>
              </article>
            </div>
          </div>
        </section>

        <section v-if="tab === 'questions'" class="two-column">
          <div class="panel">
            <div class="panel-head">
              <h2>{{ questionForm.id ? '编辑题库题目' : '新增题库题目' }}</h2>
              <button class="text-btn" @click="resetQuestionForm">清空表单</button>
            </div>
            <form class="form-grid" @submit.prevent="submitQuestion">
              <label><span>题目内容</span><textarea v-model.trim="questionForm.content" rows="3"></textarea></label>
              <label>
                <span>题目类型</span>
                <select v-model.number="questionForm.type">
                  <option :value="1">正向题</option>
                  <option :value="2">负向题</option>
                </select>
              </label>
              <label><span>体质分类</span><input v-model.trim="questionForm.category" type="text" placeholder="如：湿热体质" /></label>
              <label>
                <span>评估维度</span>
                <select v-model="questionForm.dimension">
                  <option value="">请选择</option>
                  <option value="身体健康状况">身体健康状况</option>
                  <option value="生活习惯评估">生活习惯评估</option>
                  <option value="心理健康状况">心理健康状况</option>
                  <option value="睡眠质量评估">睡眠质量评估</option>
                </select>
              </label>
              <label><span>适用体质</span><input v-model.trim="questionForm.applicableConstitution" type="text" placeholder="如：湿热体质 / 通用" /></label>
              <label><span>适用等级</span><input v-model.trim="questionForm.applicableHealthLevel" type="text" placeholder="如：风险 / 通用" /></label>
              <label><span>排序值</span><input v-model.number="questionForm.sortOrder" type="number" /></label>
              <label>
                <span>是否启用</span>
                <select v-model.number="questionForm.isActive">
                  <option :value="1">启用</option>
                  <option :value="0">停用</option>
                </select>
              </label>

              <div class="option-editor">
                <div class="panel-head">
                  <h3>题目选项</h3>
                  <button type="button" class="ghost-btn" @click="addOption">新增选项</button>
                </div>
                <div class="option-editor-list">
                  <div v-for="(option, index) in questionForm.options" :key="index" class="option-editor-row">
                    <input v-model.trim="option.content" type="text" :placeholder="`选项${index + 1}`" />
                    <input v-model.number="option.score" type="number" placeholder="分值" />
                    <button type="button" class="danger-btn" :disabled="questionForm.options.length <= 1" @click="removeOption(index)">删除</button>
                  </div>
                </div>
              </div>

              <button class="primary-btn" :disabled="loading.question">{{ loading.question ? '提交中...' : questionForm.id ? '保存修改' : '新增题目' }}</button>
            </form>
          </div>

          <div class="panel">
            <div class="panel-head">
              <h2>题库列表</h2>
              <button class="ghost-btn" @click="loadQuestions">刷新</button>
            </div>
            <div class="list-block">
              <article v-for="question in questions" :key="question.id" class="record-card">
                <div class="record-head">
                  <div>
                    <h3>{{ question.content }}</h3>
                    <p>{{ question.dimension }} / {{ question.category }} / {{ question.type === 1 ? '正向题' : '负向题' }}</p>
                  </div>
                  <span class="pill">ID {{ question.id }}</span>
                </div>
                <p><strong>适用体质：</strong>{{ question.applicableConstitution || '通用' }}</p>
                <p><strong>适用等级：</strong>{{ question.applicableHealthLevel || '通用' }}</p>
                <p><strong>排序/状态：</strong>{{ question.sortOrder ?? 0 }} / {{ question.isActive === 1 ? '启用' : '停用' }}</p>
                <ul class="compact-list">
                  <li v-for="option in question.options" :key="option.id || option.content">
                    {{ option.content }}（{{ option.score }} 分）
                  </li>
                </ul>
                <div class="action-row">
                  <button class="ghost-btn" @click="editQuestion(question)">编辑</button>
                  <button class="danger-btn" @click="removeQuestion(question.id)">删除</button>
                </div>
              </article>
            </div>
          </div>
        </section>

        <section v-if="tab === 'healthData'" class="panel">
          <div class="panel-head">
            <h2>用户健康数据</h2>
            <div class="inline-tools">
              <input v-model.trim="healthDataFilter" type="text" placeholder="按用户ID筛选，留空查全部" />
              <button class="primary-btn small-btn" @click="loadHealthData">查询</button>
            </div>
          </div>
          <div class="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>用户ID</th>
                  <th>用户名</th>
                  <th>年龄</th>
                  <th>性别</th>
                  <th>血压</th>
                  <th>血糖</th>
                  <th>心率</th>
                  <th>血氧</th>
                  <th>身高/体重</th>
                  <th>时间</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="item in healthDataList" :key="item.id">
                  <td>{{ item.userId }}</td>
                  <td>{{ item.username || '-' }}</td>
                  <td>{{ item.age ?? '-' }}</td>
                  <td>{{ item.gender || '-' }}</td>
                  <td>{{ item.bloodPressureHigh }}/{{ item.bloodPressureLow }}</td>
                  <td>{{ item.bloodSugar ?? '-' }}</td>
                  <td>{{ item.heartRate ?? '-' }}</td>
                  <td>{{ item.bloodOxygen ?? '-' }}</td>
                  <td>{{ item.height ?? '-' }} / {{ item.weight ?? '-' }}</td>
                  <td>{{ formatTime(item.createTime) }}</td>
                </tr>
                <tr v-if="healthDataList.length === 0"><td colspan="10" class="empty-row">暂无数据</td></tr>
              </tbody>
            </table>
          </div>
        </section>

        <section v-if="tab === 'results'" class="panel">
          <div class="panel-head">
            <h2>健康评测结果</h2>
            <div class="inline-tools">
              <input v-model.trim="resultFilter" type="text" placeholder="按用户ID筛选，留空查全部" />
              <button class="primary-btn small-btn" @click="loadResults">查询</button>
            </div>
          </div>
          <div class="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>用户ID</th>
                  <th>用户名</th>
                  <th>年龄</th>
                  <th>性别</th>
                  <th>评分</th>
                  <th>健康等级</th>
                  <th>体质类型</th>
                  <th>时间</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="item in resultsList" :key="item.id">
                  <td>{{ item.userId }}</td>
                  <td>{{ item.username || '-' }}</td>
                  <td>{{ item.age ?? '-' }}</td>
                  <td>{{ item.gender || '-' }}</td>
                  <td>{{ item.score ?? '-' }}</td>
                  <td>{{ item.healthLevel || '-' }}</td>
                  <td>{{ item.constitutionType || '-' }}</td>
                  <td>{{ formatTime(item.createTime) }}</td>
                </tr>
                <tr v-if="resultsList.length === 0"><td colspan="8" class="empty-row">暂无数据</td></tr>
              </tbody>
            </table>
          </div>
        </section>
      </template>
    </main>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { api } from '../api'

const emit = defineEmits(['notify'])

const authMode = ref('login')
const doctor = ref(null)
const tab = ref('plans')
const plans = ref([])
const questions = ref([])
const healthDataList = ref([])
const resultsList = ref([])
const healthDataFilter = ref('')
const resultFilter = ref('')

const loading = reactive({
  login: false,
  register: false,
  plan: false,
  question: false
})

const loginForm = reactive({
  username: '',
  password: ''
})

const registerForm = reactive({
  username: '',
  password: ''
})

const planForm = reactive({
  id: null,
  title: '',
  constitutionType: '',
  healthLevel: '',
  diet: '',
  drink: '',
  sport: '',
  lifestyle: ''
})

const questionForm = reactive({
  id: null,
  content: '',
  type: 1,
  category: '',
  dimension: '',
  applicableConstitution: '通用',
  applicableHealthLevel: '通用',
  sortOrder: 0,
  isActive: 1,
  options: [
    { content: '', score: 1 },
    { content: '', score: 2 }
  ]
})

function notify(text, type = 'success') {
  emit('notify', { text, type })
}

function resetPlanForm() {
  Object.assign(planForm, {
    id: null,
    title: '',
    constitutionType: '',
    healthLevel: '',
    diet: '',
    drink: '',
    sport: '',
    lifestyle: ''
  })
}

function resetQuestionForm() {
  Object.assign(questionForm, {
    id: null,
    content: '',
    type: 1,
    category: '',
    dimension: '',
    applicableConstitution: '通用',
    applicableHealthLevel: '通用',
    sortOrder: 0,
    isActive: 1,
    options: [
      { content: '', score: 1 },
      { content: '', score: 2 }
    ]
  })
}

async function register() {
  if (!registerForm.username || !registerForm.password) {
    notify('请填写医生用户名和密码', 'error')
    return
  }
  loading.register = true
  try {
    const result = await api.doctorRegister(registerForm)
    authMode.value = 'login'
    notify(result.message || '医生注册成功')
  } catch (error) {
    notify(error.message, 'error')
  } finally {
    loading.register = false
  }
}

async function login() {
  if (!loginForm.username || !loginForm.password) {
    notify('请输入医生账号和密码', 'error')
    return
  }
  loading.login = true
  try {
    const result = await api.doctorLogin(loginForm)
    doctor.value = result
    localStorage.setItem('health-doctor', JSON.stringify(result))
    notify('医生登录成功')
    await switchTab('plans')
  } catch (error) {
    notify(error.message, 'error')
  } finally {
    loading.login = false
  }
}

async function loadPlans() {
  try {
    plans.value = await api.getPlans()
  } catch (error) {
    notify(error.message, 'error')
  }
}

function editPlan(plan) {
  Object.assign(planForm, { ...plan })
}

async function submitPlan() {
  if (!planForm.title || !planForm.constitutionType || !planForm.healthLevel) {
    notify('请完整填写方案信息', 'error')
    return
  }
  loading.plan = true
  try {
    const payload = {
      title: planForm.title,
      constitutionType: planForm.constitutionType,
      healthLevel: planForm.healthLevel,
      diet: planForm.diet,
      drink: planForm.drink,
      sport: planForm.sport,
      lifestyle: planForm.lifestyle
    }
    if (planForm.id) {
      await api.updatePlan(planForm.id, payload, doctor.value.username)
      notify('康养方案已更新')
    } else {
      await api.createPlan(payload, doctor.value.username)
      notify('康养方案已新增')
    }
    resetPlanForm()
    await loadPlans()
  } catch (error) {
    notify(error.message, 'error')
  } finally {
    loading.plan = false
  }
}

async function removePlan(id) {
  if (!window.confirm('确定删除该方案吗？')) {
    return
  }
  try {
    await api.deletePlan(id, doctor.value.username)
    notify('康养方案已删除')
    await loadPlans()
  } catch (error) {
    notify(error.message, 'error')
  }
}

async function loadQuestions() {
  try {
    questions.value = await api.getDoctorQuestions()
  } catch (error) {
    notify(error.message, 'error')
  }
}

function addOption() {
  questionForm.options.push({ content: '', score: 1 })
}

function removeOption(index) {
  if (questionForm.options.length <= 1) {
    return
  }
  questionForm.options.splice(index, 1)
}

function editQuestion(question) {
  Object.assign(questionForm, {
    id: question.id,
    content: question.content,
    type: question.type,
    category: question.category,
    dimension: question.dimension,
    applicableConstitution: question.applicableConstitution || '通用',
    applicableHealthLevel: question.applicableHealthLevel || '通用',
    sortOrder: question.sortOrder ?? 0,
    isActive: question.isActive ?? 1,
    options: question.options.map((item) => ({
      id: item.id,
      content: item.content,
      score: item.score
    }))
  })
}

async function submitQuestion() {
  if (!questionForm.content || !questionForm.category || !questionForm.dimension) {
    notify('请填写题目内容、体质分类和评估维度', 'error')
    return
  }
  if (questionForm.options.some((item) => !item.content || item.score === null || item.score === '')) {
    notify('请完整填写所有选项', 'error')
    return
  }
  loading.question = true
  try {
    const payload = {
      content: questionForm.content,
      type: Number(questionForm.type),
      category: questionForm.category,
      dimension: questionForm.dimension,
      applicableConstitution: questionForm.applicableConstitution || '通用',
      applicableHealthLevel: questionForm.applicableHealthLevel || '通用',
      sortOrder: Number(questionForm.sortOrder || 0),
      isActive: Number(questionForm.isActive ?? 1),
      options: questionForm.options.map((item) => ({
        content: item.content,
        score: Number(item.score)
      }))
    }
    if (questionForm.id) {
      await api.updateQuestion(questionForm.id, payload, doctor.value.username)
      notify('题库题目已更新')
    } else {
      await api.createQuestion(payload, doctor.value.username)
      notify('题库题目已新增')
    }
    resetQuestionForm()
    await loadQuestions()
  } catch (error) {
    notify(error.message, 'error')
  } finally {
    loading.question = false
  }
}

async function removeQuestion(id) {
  if (!window.confirm('确定删除该题目吗？')) {
    return
  }
  try {
    await api.deleteQuestion(id, doctor.value.username)
    notify('题目已删除')
    await loadQuestions()
  } catch (error) {
    notify(error.message, 'error')
  }
}

async function loadHealthData() {
  try {
    healthDataList.value = await api.getHealthData(healthDataFilter.value)
  } catch (error) {
    notify(error.message, 'error')
  }
}

async function loadResults() {
  try {
    resultsList.value = await api.getResults(resultFilter.value)
  } catch (error) {
    notify(error.message, 'error')
  }
}

async function switchTab(nextTab) {
  tab.value = nextTab
  if (nextTab === 'plans') {
    await loadPlans()
  } else if (nextTab === 'questions') {
    await loadQuestions()
  } else if (nextTab === 'healthData') {
    await loadHealthData()
  } else if (nextTab === 'results') {
    await loadResults()
  }
}

function logout() {
  doctor.value = null
  localStorage.removeItem('health-doctor')
  plans.value = []
  questions.value = []
  healthDataList.value = []
  resultsList.value = []
  authMode.value = 'login'
  notify('医生已退出登录')
}

function formatTime(value) {
  if (!value) {
    return '-'
  }
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return value
  }
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

onMounted(async () => {
  const cached = localStorage.getItem('health-doctor')
  if (!cached) {
    return
  }
  try {
    doctor.value = JSON.parse(cached)
    await switchTab(tab.value)
  } catch {
    localStorage.removeItem('health-doctor')
  }
})
</script>
