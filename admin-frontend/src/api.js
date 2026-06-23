const defaultHeaders = {
  'Content-Type': 'application/json'
}

async function request(url, options = {}) {
  const response = await fetch(url, {
    headers: {
      ...defaultHeaders,
      ...(options.headers || {})
    },
    ...options
  })

  const text = await response.text()
  let data

  try {
    data = text ? JSON.parse(text) : null
  } catch {
    data = text
  }

  if (!response.ok) {
    throw new Error(data?.message || `请求失败: ${response.status}`)
  }

  if (data && typeof data === 'object' && data.success === false) {
    throw new Error(data.message || '请求失败')
  }

  return data
}

export const api = {
  userRegister(payload) {
    return request('/user/register', {
      method: 'POST',
      body: JSON.stringify(payload)
    })
  },
  userLogin(payload) {
    return request('/user/login', {
      method: 'POST',
      body: JSON.stringify(payload)
    })
  },
  doctorLogin(payload) {
    return request('/doctor/login', {
      method: 'POST',
      body: JSON.stringify(payload)
    })
  },
  adminLogin(payload) {
    return request('/admin/login', {
      method: 'POST',
      body: JSON.stringify(payload)
    })
  },
  doctorRegister(payload) {
    return request('/doctor/register', {
      method: 'POST',
      body: JSON.stringify(payload)
    })
  },
  getQuestions() {
    return request('/question/list')
  },
  getRecommendedQuestions(userId) {
    return request(`/question/recommend?userId=${encodeURIComponent(userId)}`)
  },
  submitQuestionnaire(userId, payload) {
    return request(`/question/submit?userId=${encodeURIComponent(userId)}`, {
      method: 'POST',
      body: JSON.stringify(payload)
    })
  },
  addHealthData(payload) {
    return request('/health/add', {
      method: 'POST',
      body: JSON.stringify(payload)
    })
  },
  getUserDashboard(userId, range = 'week') {
    return request(`/user-center/dashboard?userId=${encodeURIComponent(userId)}&range=${encodeURIComponent(range)}`)
  },
  getUserArticles(params = {}) {
    const query = new URLSearchParams()
    if (params.category) query.set('category', params.category)
    if (params.keyword) query.set('keyword', params.keyword)
    const suffix = query.toString() ? `?${query.toString()}` : ''
    return request(`/user-center/articles${suffix}`)
  },
  getUserArticleHome(userId, seed) {
    const query = new URLSearchParams()
    query.set('userId', userId)
    if (seed) query.set('seed', seed)
    return request(`/user-center/articles/home?${query.toString()}`)
  },
  getUserArticle(id) {
    return request(`/user-center/articles/${id}`)
  },
  getFavorites(userId) {
    return request(`/user-center/favorites?userId=${encodeURIComponent(userId)}`)
  },
  saveFavorite(payload) {
    return request('/user-center/favorites', {
      method: 'POST',
      body: JSON.stringify(payload)
    })
  },
  deleteFavorite(userId, planId) {
    return request(`/user-center/favorites/${encodeURIComponent(planId)}?userId=${encodeURIComponent(userId)}`, {
      method: 'DELETE'
    })
  },
  getCheckins(userId) {
    return request(`/user-center/checkins?userId=${encodeURIComponent(userId)}`)
  },
  saveCheckin(payload) {
    return request('/user-center/checkins', {
      method: 'POST',
      body: JSON.stringify(payload)
    })
  },
  getFeedbacks(userId) {
    return request(`/user-center/feedbacks?userId=${encodeURIComponent(userId)}`)
  },
  saveFeedback(payload) {
    return request('/user-center/feedbacks', {
      method: 'POST',
      body: JSON.stringify(payload)
    })
  },
  getUserConsultations(userId) {
    return request(`/user-center/consultations?userId=${encodeURIComponent(userId)}`)
  },
  applyConsultation(payload) {
    return request('/user-center/consultations', {
      method: 'POST',
      body: JSON.stringify(payload)
    })
  },
  getConsultationMessages(consultationId) {
    return request(`/user-center/consultations/${encodeURIComponent(consultationId)}/messages`)
  },
  sendConsultationMessage(consultationId, payload, actor = 'user') {
    const prefix = actor === 'doctor' ? '/doctor' : '/user-center'
    return request(`${prefix}/consultations/${encodeURIComponent(consultationId)}/messages`, {
      method: 'POST',
      body: JSON.stringify(payload)
    })
  },
  getDoctorDirectory() {
    return request('/doctor/directory')
  },
  getDoctorProfile(doctorId) {
    return request(`/doctor/profile/${encodeURIComponent(doctorId)}`)
  },
  updateDoctorProfile(doctorId, payload) {
    return request(`/doctor/profile/${encodeURIComponent(doctorId)}`, {
      method: 'PUT',
      body: JSON.stringify(payload)
    })
  },
  getDoctorPlans() {
    return request('/doctor/plans')
  },
  getDoctorArticles() {
    return request('/doctor/articles')
  },
  getAdminArticles() {
    return request('/admin/articles')
  },
  getDoctorConsultations(doctorId) {
    return request(`/doctor/consultations?doctorId=${encodeURIComponent(doctorId)}`)
  },
  closeDoctorConsultation(consultationId, operator) {
    return request(`/doctor/consultations/${encodeURIComponent(consultationId)}/close?operator=${encodeURIComponent(operator)}`, {
      method: 'POST'
    })
  },
  getDoctorProposals(doctorId) {
    return request(`/doctor/proposals?doctorId=${encodeURIComponent(doctorId)}`)
  },
  submitDoctorProposal(payload) {
    return request('/doctor/proposals', {
      method: 'POST',
      body: JSON.stringify(payload)
    })
  },
  getAdminStats() {
    return request('/admin/stats')
  },
  getAdminUsers() {
    return request('/admin/users')
  },
  getUsers() {
    return request('/admin/users')
  },
  getAdminDoctors() {
    return request('/admin/doctors')
  },
  getAdminRules() {
    return request('/admin/rules')
  },
  saveAdminRule(payload, operator) {
    return request(`/admin/rules?operator=${encodeURIComponent(operator)}`, {
      method: 'POST',
      body: JSON.stringify(payload)
    })
  },
  getAdminPlans() {
    return request('/admin/plans')
  },
  getPlans() {
    return request('/admin/plans')
  },
  createPlan(payload, operator) {
    return request(`/admin/plans?operator=${encodeURIComponent(operator)}`, {
      method: 'POST',
      body: JSON.stringify(payload)
    })
  },
  updatePlan(id, payload, operator) {
    return request(`/admin/plans/${id}?operator=${encodeURIComponent(operator)}`, {
      method: 'PUT',
      body: JSON.stringify(payload)
    })
  },
  deletePlan(id, operator) {
    return request(`/admin/plans/${id}?operator=${encodeURIComponent(operator)}`, {
      method: 'DELETE'
    })
  },
  getDoctorQuestions() {
    return request('/doctor/questions')
  },
  getAdminQuestions() {
    return request('/admin/questions')
  },
  createQuestion(payload, operator) {
    return request(`/admin/questions?operator=${encodeURIComponent(operator)}`, {
      method: 'POST',
      body: JSON.stringify(payload)
    })
  },
  updateQuestion(id, payload, operator) {
    return request(`/admin/questions/${id}?operator=${encodeURIComponent(operator)}`, {
      method: 'PUT',
      body: JSON.stringify(payload)
    })
  },
  deleteQuestion(id, operator) {
    return request(`/admin/questions/${id}?operator=${encodeURIComponent(operator)}`, {
      method: 'DELETE'
    })
  },
  getHealthData(userId) {
    const query = userId ? `?userId=${encodeURIComponent(userId)}` : ''
    return request(`/admin/health-data${query}`)
  },
  getResults(userId) {
    const query = userId ? `?userId=${encodeURIComponent(userId)}` : ''
    return request(`/admin/results${query}`)
  },
  getDoctorStats() {
    return request('/admin/stats')
  },
  getDoctorFeedbacks() {
    return request('/admin/feedbacks')
  },
  createArticle(payload, operator) {
    return request(`/admin/articles?operator=${encodeURIComponent(operator)}`, {
      method: 'POST',
      body: JSON.stringify(payload)
    })
  },
  updateArticle(id, payload, operator) {
    return request(`/admin/articles/${id}?operator=${encodeURIComponent(operator)}`, {
      method: 'PUT',
      body: JSON.stringify(payload)
    })
  },
  deleteArticle(id, operator) {
    return request(`/admin/articles/${id}?operator=${encodeURIComponent(operator)}`, {
      method: 'DELETE'
    })
  },
  getRules() {
    return request('/admin/rules')
  },
  saveRule(payload, operator) {
    return request(`/admin/rules?operator=${encodeURIComponent(operator)}`, {
      method: 'POST',
      body: JSON.stringify(payload)
    })
  },
  getAdminProposals(status = '') {
    const query = status ? `?status=${encodeURIComponent(status)}` : ''
    return request(`/admin/proposals${query}`)
  },
  reviewProposal(proposalId, payload) {
    return request(`/admin/proposals/${encodeURIComponent(proposalId)}/review`, {
      method: 'POST',
      body: JSON.stringify(payload)
    })
  },
  getAdminConsultations() {
    return request('/admin/consultations')
  },
  assignConsultation(consultationId, payload) {
    return request(`/admin/consultations/${encodeURIComponent(consultationId)}/assign`, {
      method: 'POST',
      body: JSON.stringify(payload)
    })
  },
  getDoctorMatches(preferredTag = '') {
    const query = preferredTag ? `?preferredTag=${encodeURIComponent(preferredTag)}` : ''
    return request(`/admin/doctor-matches${query}`)
  }
}
