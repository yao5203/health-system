<template>
  <section class="dashboard-layout doctor-apple-layout">
    <aside class="panel dashboard-side doctor-apple-side">
      <div class="workspace-profile">
        <h2>{{ doctor?.username }}</h2>
      </div>

      <div class="nav-stack user-nav-stack">
        <button class="nav-btn user-nav-btn" :class="{ active: tab === 'overview' }" @click="switchTab('overview')">
          工作概览
        </button>
        <button class="nav-btn user-nav-btn" :class="{ active: tab === 'profile' }" @click="switchTab('profile')">
          个人资料
        </button>
        <button class="nav-btn user-nav-btn" :class="{ active: tab === 'consultations' }" @click="switchTab('consultations')">
          用户咨询
        </button>
        <button class="nav-btn user-nav-btn" :class="{ active: tab === 'proposals' }" @click="switchTab('proposals')">
          修改提案
        </button>
        <button class="nav-btn user-nav-btn" :class="{ active: tab === 'references' }" @click="switchTab('references')">
          参考资料
        </button>
      </div>

      <div class="workspace-side-actions">
        <button class="ghost-btn" @click="switchTab('consultations')">去处理咨询</button>
        <button class="ghost-btn" @click="switchTab('proposals')">去写提案</button>
        <button class="ghost-btn" @click="logout">退出登录</button>
      </div>
    </aside>

    <main class="content-stack doctor-apple-main">
      <section class="panel dashboard-top doctor-apple-top">
        <div>
          <h2>{{ tabTitle }}</h2>
        </div>
        <div class="workspace-toolbar">
          <button v-if="tab !== 'overview'" class="ghost-btn small-btn" @click="switchTab('overview')">返回概览</button>
          <button
            v-if="tab === 'consultations'"
            class="ghost-btn small-btn"
            @click="loadConsultations"
          >
            刷新咨询
          </button>
          <button
            v-if="tab === 'proposals'"
            class="ghost-btn small-btn"
            @click="loadProposals"
          >
            刷新提案
          </button>
        </div>
      </section>

      <div v-if="feedback.text" class="inline-feedback" :class="feedback.type">{{ feedback.text }}</div>

      <Transition name="workspace-fade" mode="out-in">
        <div :key="tab" class="doctor-tab-stage">
          <section v-if="tab === 'overview'" class="workspace-page">
            <section class="panel apple-hero">
              <h2>医生工作台概览</h2>

              <div class="apple-stat-grid doctor-stat-grid">
                <article class="apple-stat-card">
                  <span>分配咨询</span>
                  <strong>{{ overview?.stats?.consultationCount ?? 0 }}</strong>
                  <p>管理员已分配给你的全部咨询数</p>
                </article>
                <article class="apple-stat-card">
                  <span>进行中咨询</span>
                  <strong>{{ overview?.stats?.activeConsultationCount ?? 0 }}</strong>
                  <p>仍需继续跟进的咨询会话</p>
                </article>
                <article class="apple-stat-card">
                  <span>已关闭咨询</span>
                  <strong>{{ overview?.stats?.closedConsultationCount ?? 0 }}</strong>
                  <p>已经完成处理的会话</p>
                </article>
                <article class="apple-stat-card">
                  <span>待审核提案</span>
                  <strong>{{ overview?.stats?.pendingProposalCount ?? 0 }}</strong>
                  <p>等待管理员审核的优化建议</p>
                </article>
                <article class="apple-stat-card">
                  <span>资料完整度</span>
                  <strong>{{ overview?.stats?.profileCompleteness ?? 0 }}%</strong>
                  <p>专业方向、标签和简介的完成情况</p>
                </article>
              </div>
            </section>

            <section class="workspace-section-grid">
              <article class="glass-panel">
                <div class="panel-head compact-head">
                  <h3>当前关注重点</h3>
                </div>
                <ul class="compact-list">
                  <li v-for="(item, index) in overview?.focus || []" :key="`${index}-${item}`">
                    {{ item }}
                  </li>
                </ul>
              </article>

              <article class="glass-panel">
                <div class="panel-head compact-head">
                  <h3>提案通过情况</h3>
                </div>
                <div class="summary-strip summary-strip-single">
                  <div class="summary-pill">
                    <span>已通过</span>
                    <strong>{{ overview?.stats?.approvedProposalCount ?? 0 }}</strong>
                  </div>
                  <div class="summary-pill">
                    <span>已驳回</span>
                    <strong>{{ overview?.stats?.rejectedProposalCount ?? 0 }}</strong>
                  </div>
                </div>
              </article>

              <article class="glass-panel">
                <div class="panel-head compact-head">
                  <h3>最近咨询</h3>
                  <button class="ghost-btn small-btn" @click="switchTab('consultations')">查看全部</button>
                </div>
                <div class="list-block">
                  <article
                    v-for="item in overview?.recentConsultations || []"
                    :key="`overview-consultation-${item.id}`"
                    class="record-card doctor-mini-card"
                  >
                    <div class="record-head">
                      <div>
                        <h3>{{ item.title }}</h3>
                        <p>{{ item.issueType || '未分类' }}</p>
                      </div>
                      <span class="pill">{{ item.status || 'UNKNOWN' }}</span>
                    </div>
                    <p>创建时间：{{ formatDateTime(item.createTime) }}</p>
                  </article>
                  <p v-if="!(overview?.recentConsultations || []).length" class="muted-copy trend-copy">当前还没有分配咨询。</p>
                </div>
              </article>

              <article class="glass-panel">
                <div class="panel-head compact-head">
                  <h3>最近提案</h3>
                  <button class="ghost-btn small-btn" @click="switchTab('proposals')">查看全部</button>
                </div>
                <div class="list-block">
                  <article
                    v-for="item in overview?.recentProposals || []"
                    :key="`overview-proposal-${item.id}`"
                    class="record-card doctor-mini-card"
                  >
                    <div class="record-head">
                      <div>
                        <h3>{{ item.title || `${item.targetType} 提案` }}</h3>
                        <p>{{ item.targetType }} / {{ item.actionType }}</p>
                      </div>
                      <span class="pill">{{ item.status || 'PENDING' }}</span>
                    </div>
                    <p>{{ item.reviewComment || '暂无审核意见' }}</p>
                  </article>
                  <p v-if="!(overview?.recentProposals || []).length" class="muted-copy trend-copy">当前还没有提案记录。</p>
                </div>
              </article>
            </section>
          </section>

          <section v-if="tab === 'profile'" class="two-column">
            <div class="panel stage-card">
              <div class="panel-head"><h2>个人资料</h2></div>
              <form class="form-grid" @submit.prevent="saveProfile">
                <label>
                  <span>专业方向</span>
                  <input v-model.trim="profileForm.specialty" type="text" placeholder="如：睡眠管理" />
                </label>
                <label>
                  <span>擅长标签</span>
                  <input v-model.trim="profileForm.expertiseTags" type="text" placeholder="如：睡眠,饮食,压力" />
                </label>
                <label>
                  <span>个人简介</span>
                  <textarea
                    v-model.trim="profileForm.introduction"
                    rows="6"
                    placeholder="介绍你的专业优势、适合处理的问题类型以及可提供的帮助"
                  />
                </label>
                <button class="primary-btn">保存资料</button>
              </form>
            </div>

            <div class="panel stage-card">
              <div class="panel-head"><h2>匹配提示</h2></div>
              <div class="visual-copy-list">
                <div class="visual-copy-item">
                  <label>管理员分配逻辑</label>
                  <p>管理员会参考你的专业方向和擅长标签，把更适合的问题类型分配给你处理。</p>
                </div>
                <div class="visual-copy-item">
                  <label>当前资料完整度</label>
                  <strong>{{ overview?.stats?.profileCompleteness ?? 0 }}%</strong>
                  <p>完整资料更有利于咨询匹配和协作效率。</p>
                </div>
                <div class="visual-copy-item">
                  <label>当前资料摘要</label>
                  <strong>{{ doctor?.specialty || '尚未填写专业方向' }}</strong>
                  <p>{{ doctor?.expertiseTags || '尚未填写擅长标签' }}</p>
                </div>
              </div>
            </div>
          </section>

          <section v-if="tab === 'consultations'" class="workspace-page">
            <section class="panel stage-card">
              <div class="panel-head">
                <h2>咨询筛选</h2>
              </div>
              <div class="doctor-filter-row">
                <input
                  v-model.trim="consultationFilters.keyword"
                  type="text"
                  placeholder="按标题、用户名、问题类型搜索"
                />
                <select v-model="consultationFilters.status">
                  <option value="ALL">全部状态</option>
                  <option value="ASSIGNED">已分配</option>
                  <option value="CLOSED">已关闭</option>
                  <option value="PENDING_ASSIGNMENT">待分配</option>
                </select>
              </div>
            </section>

            <section class="doctor-consultation-grid">
              <div class="panel stage-card">
                <div class="panel-head">
                  <h2>已分配咨询</h2>
                  <span class="pill">{{ filteredConsultations.length }} 条</span>
                </div>
                <div class="list-block">
                  <article
                    v-for="item in filteredConsultations"
                    :key="item.id"
                    class="record-card"
                    :class="{ 'record-card-active': selectedConsultation?.id === item.id }"
                    @click="selectConsultation(item)"
                  >
                    <div class="record-head">
                      <div>
                        <h3>{{ item.title }}</h3>
                        <p>{{ item.issueType || '未分类' }} / {{ item.username || `用户#${item.userId}` }}</p>
                      </div>
                      <span class="pill">{{ item.status }}</span>
                    </div>
                    <p>{{ item.detail || '暂无补充说明' }}</p>
                    <p v-if="item.adminNote"><strong>管理员备注：</strong>{{ item.adminNote }}</p>
                  </article>
                  <p v-if="!filteredConsultations.length" class="muted-copy trend-copy">当前没有符合条件的咨询。</p>
                </div>
              </div>

              <div class="doctor-consultation-detail">
                <section v-if="selectedConsultation" class="panel stage-card">
                  <div class="panel-head">
                    <div>
                      <span class="eyebrow">Conversation</span>
                      <h2>{{ selectedConsultation.title }}</h2>
                    </div>
                    <button class="ghost-btn small-btn" @click="closeConsultation">关闭会话</button>
                  </div>

                  <div class="doctor-meta-strip">
                    <div class="summary-pill">
                      <span>咨询状态</span>
                      <strong>{{ selectedConsultation.status }}</strong>
                    </div>
                    <div class="summary-pill">
                      <span>问题类型</span>
                      <strong>{{ selectedConsultation.issueType || '未分类' }}</strong>
                    </div>
                    <div class="summary-pill">
                      <span>偏好标签</span>
                      <strong>{{ selectedConsultation.preferredTag || '无' }}</strong>
                    </div>
                  </div>

                  <div class="chat-thread">
                    <div
                      v-for="message in selectedConsultation.messages || []"
                      :key="message.id"
                      class="chat-bubble"
                      :class="message.senderType === 'DOCTOR' ? 'is-self' : 'is-other'"
                    >
                      <strong>{{ message.senderName || message.senderType }}</strong>
                      <p>{{ message.content }}</p>
                    </div>
                  </div>

                  <div class="template-section">
                    <div class="panel-head compact-head">
                      <h3>快捷回复建议</h3>
                    </div>
                    <div class="action-row doctor-template-row">
                      <button
                        v-for="(item, index) in quickReplyTemplates"
                        :key="`template-${index}`"
                        class="ghost-btn small-btn"
                        @click="applyTemplate(item)"
                      >
                        用这条
                      </button>
                    </div>
                    <div class="list-block doctor-template-preview">
                      <article
                        v-for="(item, index) in quickReplyTemplates"
                        :key="`template-preview-${index}`"
                        class="record-card doctor-mini-card"
                      >
                        <p>{{ item }}</p>
                      </article>
                    </div>
                  </div>

                  <form class="chat-input-row" @submit.prevent="sendConsultationMessage">
                    <input
                      v-model.trim="consultationMessage"
                      type="text"
                      placeholder="结合用户当前健康情况，给出更具体、更稳妥的专业建议"
                    />
                    <button class="primary-btn">发送</button>
                  </form>
                </section>

                <section v-if="selectedConsultationSnapshot" class="doctor-snapshot-grid">
                  <article class="panel stage-card">
                    <div class="panel-head"><h2>用户基础资料</h2></div>
                    <div class="visual-copy-list">
                      <div class="visual-copy-item">
                        <label>用户名</label>
                        <strong>{{ selectedConsultationSnapshot.userProfile?.username || '未知用户' }}</strong>
                      </div>
                      <div class="visual-copy-item">
                        <label>年龄 / 性别</label>
                        <p>
                          {{ selectedConsultationSnapshot.userProfile?.age ?? '未填写' }}
                          /
                          {{ selectedConsultationSnapshot.userProfile?.gender || '未填写' }}
                        </p>
                      </div>
                      <div class="visual-copy-item">
                        <label>手机号</label>
                        <p>{{ selectedConsultationSnapshot.userProfile?.phone || '未填写' }}</p>
                      </div>
                      <div class="visual-copy-item">
                        <label>管理员备注</label>
                        <p>{{ selectedConsultationSnapshot.consultationMeta?.adminNote || '暂无备注' }}</p>
                      </div>
                    </div>
                  </article>

                  <article class="panel stage-card">
                    <div class="panel-head"><h2>最新评测结果</h2></div>
                    <div class="summary-strip summary-strip-single doctor-summary-grid">
                      <div class="summary-pill">
                        <span>综合评分</span>
                        <strong>{{ selectedConsultationSnapshot.healthSummary?.latestScore ?? '--' }}</strong>
                      </div>
                      <div class="summary-pill">
                        <span>健康等级</span>
                        <strong>{{ selectedConsultationSnapshot.healthSummary?.latestHealthLevel || '--' }}</strong>
                      </div>
                      <div class="summary-pill">
                        <span>体质倾向</span>
                        <strong>{{ selectedConsultationSnapshot.healthSummary?.latestConstitutionType || '--' }}</strong>
                      </div>
                      <div class="summary-pill">
                        <span>最近打卡数</span>
                        <strong>{{ selectedConsultationSnapshot.healthSummary?.checkinCount ?? 0 }}</strong>
                      </div>
                    </div>
                  </article>

                  <article class="panel stage-card">
                    <div class="panel-head"><h2>最新健康指标</h2></div>
                    <div class="pressure-list">
                      <div class="pressure-item">
                        <span>血压</span>
                        <strong>{{ selectedConsultationSnapshot.healthSummary?.latestBloodPressure || '--' }}</strong>
                      </div>
                      <div class="pressure-item">
                        <span>体重</span>
                        <strong>{{ displayMetric(selectedConsultationSnapshot.healthSummary?.latestWeight, 'kg') }}</strong>
                      </div>
                      <div class="pressure-item">
                        <span>血糖</span>
                        <strong>{{ displayMetric(selectedConsultationSnapshot.healthSummary?.latestBloodSugar, '') }}</strong>
                      </div>
                      <div class="pressure-item">
                        <span>心率</span>
                        <strong>{{ displayMetric(selectedConsultationSnapshot.healthSummary?.latestHeartRate, 'bpm') }}</strong>
                      </div>
                      <div class="pressure-item">
                        <span>血氧</span>
                        <strong>{{ displayMetric(selectedConsultationSnapshot.healthSummary?.latestBloodOxygen, '%') }}</strong>
                      </div>
                    </div>
                  </article>

                  <article class="panel stage-card">
                    <div class="panel-head"><h2>近期打卡均值</h2></div>
                    <div class="pressure-list">
                      <div class="pressure-item">
                        <span>睡眠时长</span>
                        <strong>{{ displayMetric(selectedConsultationSnapshot.healthSummary?.averageSleepHours, 'h') }}</strong>
                      </div>
                      <div class="pressure-item">
                        <span>运动分钟</span>
                        <strong>{{ displayMetric(selectedConsultationSnapshot.healthSummary?.averageExerciseMinutes, 'min') }}</strong>
                      </div>
                      <div class="pressure-item">
                        <span>压力水平</span>
                        <strong>{{ displayMetric(selectedConsultationSnapshot.healthSummary?.averageStressLevel, '') }}</strong>
                      </div>
                      <div class="pressure-item">
                        <span>情绪评分</span>
                        <strong>{{ displayMetric(selectedConsultationSnapshot.healthSummary?.averageMoodScore, '') }}</strong>
                      </div>
                    </div>
                  </article>

                  <article class="panel stage-card doctor-checkin-card">
                    <div class="panel-head"><h2>最近打卡记录</h2></div>
                    <div class="list-block">
                      <article
                        v-for="item in selectedConsultationSnapshot.recentCheckins || []"
                        :key="`checkin-${item.id}`"
                        class="record-card doctor-mini-card"
                      >
                        <div class="record-head">
                          <div>
                            <h3>{{ item.checkinDate || '未知日期' }}</h3>
                            <p>{{ item.planTitle || '未关联方案' }}</p>
                          </div>
                          <span class="pill">{{ displayMetric(item.sleepHours, 'h') }}</span>
                        </div>
                        <p>
                          运动 {{ displayMetric(item.exerciseMinutes, 'min') }}，
                          压力 {{ displayMetric(item.stressLevel, '') }}，
                          情绪 {{ displayMetric(item.moodScore, '') }}
                        </p>
                        <p v-if="item.remark">{{ item.remark }}</p>
                      </article>
                      <p v-if="!(selectedConsultationSnapshot.recentCheckins || []).length" class="muted-copy trend-copy">用户近期还没有打卡记录。</p>
                    </div>
                  </article>
                </section>

                <section v-else-if="selectedConsultation" class="panel stage-card">
                  <p class="muted-copy">正在加载该用户的健康档案...</p>
                </section>

                <section v-else class="panel stage-card">
                  <p class="muted-copy">先从左侧选择一条咨询，再查看对话和健康档案。</p>
                </section>
              </div>
            </section>
          </section>

          <section v-if="tab === 'proposals'" class="two-column">
            <div class="panel stage-card">
              <div class="panel-head"><h2>提交修改提案</h2></div>
              <form class="form-grid" @submit.prevent="submitProposal">
                <label>
                  <span>提案对象</span>
                  <select v-model="proposalForm.targetType">
                    <option value="PLAN">康养方案</option>
                    <option value="QUESTION">题库题目</option>
                    <option value="ARTICLE">健康资讯</option>
                  </select>
                </label>

                <label>
                  <span>提案动作</span>
                  <select v-model="proposalForm.actionType">
                    <option value="CREATE">新增</option>
                    <option value="UPDATE">修改</option>
                    <option value="DELETE">删除</option>
                  </select>
                </label>

                <label>
                  <span>目标 ID</span>
                  <input v-model.number="proposalForm.targetId" type="number" :placeholder="targetIdPlaceholder" />
                </label>

                <label>
                  <span>提案标题</span>
                  <input
                    v-model.trim="proposalForm.title"
                    type="text"
                    placeholder="例如：建议补充湿热体质夜间睡眠题目"
                  />
                </label>

                <label>
                  <span>提案摘要</span>
                  <textarea
                    v-model.trim="proposalForm.summary"
                    rows="3"
                    placeholder="说明修改原因、适用用户和预期价值"
                  />
                </label>

                <label>
                  <span>提案载荷 JSON</span>
                  <textarea
                    v-model.trim="proposalForm.payloadJson"
                    rows="12"
                    :placeholder="payloadPlaceholder"
                  />
                </label>

                <div class="visual-copy-item">
                  <label>提醒</label>
                  <p>医生端只负责提交提案，不直接改核心数据。只有系统管理员审核通过后，后端才会正式落库。</p>
                </div>

                <button class="primary-btn">提交给系统管理员审核</button>
              </form>
            </div>

            <div class="panel stage-card">
              <div class="panel-head">
                <h2>我的提案记录</h2>
              </div>
              <input
                v-model.trim="proposalHistoryKeyword"
                type="text"
                placeholder="按标题、对象类型、状态搜索提案"
              />
              <div class="list-block doctor-list-gap">
                <article v-for="item in filteredProposals" :key="item.id" class="record-card">
                  <div class="record-head">
                    <div>
                      <h3>{{ item.title || `${item.targetType} 提案` }}</h3>
                      <p>{{ item.targetType }} / {{ item.actionType }} / {{ item.status }}</p>
                    </div>
                    <span class="pill">#{{ item.id }}</span>
                  </div>
                  <p>{{ item.summary || '暂无摘要' }}</p>
                  <p v-if="item.targetId"><strong>目标 ID：</strong>{{ item.targetId }}</p>
                  <p v-if="item.reviewComment"><strong>审核意见：</strong>{{ item.reviewComment }}</p>
                </article>
                <p v-if="!filteredProposals.length" class="muted-copy trend-copy">当前没有符合条件的提案记录。</p>
              </div>
            </div>
          </section>

          <section v-if="tab === 'references'" class="workspace-page">
            <section class="panel stage-card">
              <div class="panel-head">
                <h2>参考资料检索</h2>
              </div>
              <input
                v-model.trim="referenceKeyword"
                type="text"
                placeholder="按题目、方案、资讯标题、分类、体质搜索"
              />
            </section>

            <section class="two-column">
              <div class="panel stage-card">
                <div class="panel-head"><h2>题库参考</h2></div>
                <div class="list-block">
                  <article v-for="item in filteredReferenceQuestions" :key="item.id" class="record-card">
                    <div class="record-head">
                      <div>
                        <h3>{{ item.content }}</h3>
                        <p>ID：{{ item.id }} / {{ item.dimension }} / {{ item.category }}</p>
                      </div>
                      <span class="pill">{{ item.applicableConstitution || '通用' }}</span>
                    </div>
                    <div class="action-row">
                      <button class="ghost-btn small-btn" @click="prefillProposal('QUESTION', 'UPDATE', item)">修改这道题</button>
                      <button class="ghost-btn small-btn" @click="prefillProposal('QUESTION', 'DELETE', item)">删除这道题</button>
                    </div>
                  </article>
                </div>
              </div>

              <div class="panel stage-card">
                <div class="panel-head"><h2>方案与资讯参考</h2></div>
                <div class="list-block">
                  <article v-for="item in filteredReferencePlans" :key="`plan-${item.id}`" class="record-card">
                    <div class="record-head">
                      <div>
                        <h3>{{ item.title }}</h3>
                        <p>ID：{{ item.id }} / {{ item.constitutionType }} / {{ item.healthLevel }}</p>
                      </div>
                      <span class="pill">方案</span>
                    </div>
                    <div class="action-row">
                      <button class="ghost-btn small-btn" @click="prefillProposal('PLAN', 'UPDATE', item)">修改这条方案</button>
                      <button class="ghost-btn small-btn" @click="prefillProposal('PLAN', 'DELETE', item)">删除这条方案</button>
                    </div>
                  </article>

                  <article v-for="item in filteredReferenceArticles" :key="`article-${item.id}`" class="record-card">
                    <div class="record-head">
                      <div>
                        <h3>{{ item.title }}</h3>
                        <p>ID：{{ item.id }} / {{ item.category }}</p>
                      </div>
                      <span class="pill">资讯</span>
                    </div>
                    <div class="action-row">
                      <button class="ghost-btn small-btn" @click="prefillProposal('ARTICLE', 'UPDATE', item)">修改这篇资讯</button>
                      <button class="ghost-btn small-btn" @click="prefillProposal('ARTICLE', 'DELETE', item)">删除这篇资讯</button>
                    </div>
                  </article>
                </div>
              </div>
            </section>
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
const doctor = ref(null)
const overview = ref(null)
const tab = ref('overview')
const questions = ref([])
const referencePlans = ref([])
const referenceArticles = ref([])
const proposals = ref([])
const consultations = ref([])
const selectedConsultation = ref(null)
const selectedConsultationSnapshot = ref(null)
const consultationMessage = ref('')
const referenceKeyword = ref('')
const proposalHistoryKeyword = ref('')
const feedback = reactive({ text: '', type: 'success' })

const consultationFilters = reactive({
  keyword: '',
  status: 'ALL'
})

const profileForm = reactive({
  specialty: '',
  expertiseTags: '',
  introduction: '',
  status: 1
})

const proposalForm = reactive({
  targetType: 'PLAN',
  actionType: 'CREATE',
  targetId: null,
  title: '',
  summary: '',
  payloadJson: ''
})

const doctorId = computed(() => doctor.value?.doctorId || doctor.value?.id)

const tabTitle = computed(() => ({
  overview: '医生工作概览',
  profile: '医生个人资料',
  consultations: '用户咨询与健康档案',
  proposals: '医生修改提案',
  references: '只读参考资料'
}[tab.value] || '医生工作台'))

const targetIdPlaceholder = computed(() => {
  if (proposalForm.actionType === 'CREATE') {
    return '新增时通常不需要填写'
  }
  return '修改或删除时填写，可从参考资料里直接带入'
})

const payloadPlaceholder = computed(() => {
  if (proposalForm.actionType === 'DELETE') {
    return '删除提案通常不需要填写 JSON，只需给出目标 ID 和删除原因'
  }

  if (proposalForm.targetType === 'QUESTION') {
    return '{"content":"是否经常凌晨醒来后难以再次入睡？","type":2,"category":"睡眠","dimension":"睡眠质量评估","applicableConstitution":"阴虚体质","applicableHealthLevel":"中度亚健康","sortOrder":10,"options":[{"content":"从不","score":5},{"content":"很少","score":4},{"content":"有时","score":3},{"content":"经常","score":2},{"content":"总是","score":1}]}'
  }

  if (proposalForm.targetType === 'ARTICLE') {
    return '{"title":"睡眠节律修复建议","category":"睡眠管理","summary":"面向长期晚睡人群的睡眠管理建议","content":"正文内容...","tags":"睡眠,作息,节律","coverImage":"","status":1}'
  }

  return '{"title":"湿热体质-一般","constitutionType":"湿热体质","healthLevel":"一般","diet":"...","drink":"...","sport":"...","lifestyle":"..."}'
})

const filteredConsultations = computed(() => {
  const keyword = consultationFilters.keyword.trim().toLowerCase()
  return consultations.value.filter((item) => {
    const matchStatus = consultationFilters.status === 'ALL' || item.status === consultationFilters.status
    const matchKeyword = !keyword || [
      item.title,
      item.username,
      item.issueType,
      item.detail,
      item.preferredTag
    ].some((field) => String(field || '').toLowerCase().includes(keyword))
    return matchStatus && matchKeyword
  })
})

const filteredProposals = computed(() => {
  const keyword = proposalHistoryKeyword.value.trim().toLowerCase()
  return proposals.value.filter((item) => {
    if (!keyword) return true
    return [
      item.title,
      item.summary,
      item.targetType,
      item.actionType,
      item.status,
      item.reviewComment
    ].some((field) => String(field || '').toLowerCase().includes(keyword))
  })
})

const filteredReferenceQuestions = computed(() => {
  const keyword = referenceKeyword.value.trim().toLowerCase()
  return questions.value.filter((item) => {
    if (!keyword) return true
    return [
      item.content,
      item.dimension,
      item.category,
      item.applicableConstitution,
      item.applicableHealthLevel
    ].some((field) => String(field || '').toLowerCase().includes(keyword))
  })
})

const filteredReferencePlans = computed(() => {
  const keyword = referenceKeyword.value.trim().toLowerCase()
  return referencePlans.value.filter((item) => {
    if (!keyword) return true
    return [
      item.title,
      item.constitutionType,
      item.healthLevel,
      item.diet,
      item.drink,
      item.sport,
      item.lifestyle
    ].some((field) => String(field || '').toLowerCase().includes(keyword))
  })
})

const filteredReferenceArticles = computed(() => {
  const keyword = referenceKeyword.value.trim().toLowerCase()
  return referenceArticles.value.filter((item) => {
    if (!keyword) return true
    return [
      item.title,
      item.category,
      item.summary,
      item.tags
    ].some((field) => String(field || '').toLowerCase().includes(keyword))
  })
})

const quickReplyTemplates = computed(() => {
  const username = selectedConsultationSnapshot.value?.userProfile?.username || '你'
  const level = selectedConsultationSnapshot.value?.healthSummary?.latestHealthLevel || '当前状态'
  const constitution = selectedConsultationSnapshot.value?.healthSummary?.latestConstitutionType || '当前体质倾向'
  const avgSleep = selectedConsultationSnapshot.value?.healthSummary?.averageSleepHours
  const avgStress = selectedConsultationSnapshot.value?.healthSummary?.averageStressLevel

  return [
    `${username}，我已经看过你这次的咨询内容。结合你最近的评测结果，目前更需要先把作息、饮食和运动节奏稳定下来，再逐步做更细的调整。`,
    `${username}，从你当前的健康等级“${level}”和体质倾向“${constitution}”来看，建议你先记录这几天最明显的不适表现、出现时段和诱因，我再帮你进一步判断。`,
    `${username}，如果方便的话，你可以继续补充最近一周的睡眠、压力和饮食情况。${avgSleep ? `我看到你近期平均睡眠大约 ${avgSleep} 小时，` : ''}${avgStress ? `平均压力水平大约 ${avgStress}，` : ''}这些信息会帮助我给出更稳妥的建议。`
  ]
})

function setFeedback(text, type = 'success') {
  feedback.text = text
  feedback.type = type
}

function formatDateTime(value) {
  if (!value) return '--'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return String(value)
  return date.toLocaleString()
}

function displayMetric(value, suffix = '') {
  if (value === null || value === undefined || value === '') return '--'
  return `${value}${suffix}`
}

function applyTemplate(text) {
  consultationMessage.value = text
}

function buildPayloadJson(targetType, item) {
  if (targetType === 'QUESTION') {
    return JSON.stringify({
      content: item.content,
      type: item.type,
      category: item.category,
      dimension: item.dimension,
      applicableConstitution: item.applicableConstitution,
      applicableHealthLevel: item.applicableHealthLevel,
      sortOrder: item.sortOrder,
      isActive: item.isActive ?? 1,
      options: item.options || []
    }, null, 2)
  }

  if (targetType === 'ARTICLE') {
    return JSON.stringify({
      title: item.title,
      category: item.category,
      summary: item.summary,
      content: item.content,
      tags: item.tags,
      coverImage: item.coverImage,
      status: item.status
    }, null, 2)
  }

  return JSON.stringify({
    title: item.title,
    constitutionType: item.constitutionType,
    healthLevel: item.healthLevel,
    diet: item.diet,
    drink: item.drink,
    sport: item.sport,
    lifestyle: item.lifestyle
  }, null, 2)
}

function prefillProposal(targetType, actionType, item) {
  proposalForm.targetType = targetType
  proposalForm.actionType = actionType
  proposalForm.targetId = item.id
  proposalForm.title = `${actionType === 'DELETE' ? '建议删除' : '建议修改'}${item.title || item.content || '目标内容'}`
  proposalForm.summary = actionType === 'DELETE'
    ? '请补充删除原因、可能影响和替代建议。'
    : '请补充修改原因、预期优化点和适用人群。'
  proposalForm.payloadJson = actionType === 'DELETE' ? '' : buildPayloadJson(targetType, item)
  tab.value = 'proposals'
  setFeedback(`已将 ID ${item.id} 的${targetType === 'QUESTION' ? '题目' : targetType === 'PLAN' ? '方案' : '资讯'}带入提案表单。`)
}

async function loadOverview() {
  overview.value = await api.getDoctorOverview(doctorId.value)
}

async function loadProfile() {
  const profile = await api.getDoctorProfile(doctorId.value)
  Object.assign(doctor.value, profile)
  Object.assign(profileForm, {
    specialty: profile.specialty || '',
    expertiseTags: profile.expertiseTags || '',
    introduction: profile.introduction || '',
    status: profile.status ?? 1
  })
  session.setDoctor(doctor.value)
}

async function saveProfile() {
  const profile = await api.updateDoctorProfile(doctorId.value, profileForm)
  Object.assign(doctor.value, profile)
  session.setDoctor(doctor.value)
  await loadOverview()
  setFeedback('医生资料已更新。')
}

async function loadProposals() {
  proposals.value = await api.getDoctorProposals(doctorId.value)
}

async function submitProposal() {
  await api.submitDoctorProposal({
    proposerDoctorId: doctorId.value,
    proposerName: doctor.value.username,
    targetType: proposalForm.targetType,
    actionType: proposalForm.actionType,
    targetId: proposalForm.targetId,
    title: proposalForm.title,
    summary: proposalForm.summary,
    payloadJson: proposalForm.payloadJson
  })

  Object.assign(proposalForm, {
    targetType: 'PLAN',
    actionType: 'CREATE',
    targetId: null,
    title: '',
    summary: '',
    payloadJson: ''
  })

  await Promise.all([loadProposals(), loadOverview()])
  setFeedback('提案已提交，等待系统管理员审核。')
}

async function loadConsultations() {
  consultations.value = await api.getDoctorConsultations(doctorId.value)
  if (!consultations.value.length) {
    selectedConsultation.value = null
    selectedConsultationSnapshot.value = null
    return
  }

  const stillSelected = consultations.value.find((item) => item.id === selectedConsultation.value?.id)
  if (stillSelected) {
    selectedConsultation.value = stillSelected
  } else {
    selectedConsultation.value = consultations.value[0]
  }
  await loadSelectedSnapshot()
}

async function loadSelectedSnapshot() {
  if (!selectedConsultation.value) {
    selectedConsultationSnapshot.value = null
    return
  }
  selectedConsultationSnapshot.value = await api.getDoctorConsultationSnapshot(selectedConsultation.value.id, doctorId.value)
}

async function selectConsultation(item) {
  selectedConsultation.value = item
  await loadSelectedSnapshot()
}

async function sendConsultationMessage() {
  if (!selectedConsultation.value || !consultationMessage.value) return
  await api.sendConsultationMessage(
    selectedConsultation.value.id,
    {
      senderType: 'DOCTOR',
      senderId: doctorId.value,
      senderName: doctor.value.username,
      content: consultationMessage.value
    },
    'doctor'
  )
  consultationMessage.value = ''
  await loadConsultations()
  setFeedback('咨询消息已发送。')
}

async function closeConsultation() {
  if (!selectedConsultation.value) return
  await api.closeDoctorConsultation(selectedConsultation.value.id, doctor.value.username)
  await Promise.all([loadConsultations(), loadOverview()])
  setFeedback('咨询会话已关闭。')
}

async function loadReferences() {
  const [questionList, planList, articleList] = await Promise.all([
    api.getDoctorQuestions(),
    api.getDoctorPlans(),
    api.getDoctorArticles()
  ])
  questions.value = questionList
  referencePlans.value = planList
  referenceArticles.value = articleList
}

async function switchTab(nextTab) {
  tab.value = nextTab
  try {
    if (nextTab === 'overview') await loadOverview()
    if (nextTab === 'profile') {
      await Promise.all([loadProfile(), loadOverview()])
    }
    if (nextTab === 'consultations') await loadConsultations()
    if (nextTab === 'proposals') await loadProposals()
    if (nextTab === 'references') await loadReferences()
  } catch (error) {
    setFeedback(error.message || '医生工作台数据加载失败。', 'error')
  }
}

function logout() {
  session.clearDoctor()
  router.push('/doctor/auth')
}

onMounted(async () => {
  doctor.value = session.getDoctor()
  if (!doctor.value) {
    router.replace('/doctor/auth')
    return
  }
  await switchTab('overview')
})
</script>
