const USER_KEY = 'health-user'
const DOCTOR_KEY = 'health-doctor'
const ADMIN_KEY = 'health-admin'
const USER_ASSESSMENT_KEY = 'health-user-assessment'
const USER_RECOMMENDATION_KEY = 'health-user-recommendation'
const USER_REPORT_KEY = 'health-user-report'
const USER_SELECTED_FAVORITE_KEY = 'health-user-selected-favorite'
const ENTRY_SPLASH_KEY = 'health-entry-splash'

function readJson(key) {
  const raw = localStorage.getItem(key)
  if (!raw) return null
  try {
    return JSON.parse(raw)
  } catch {
    localStorage.removeItem(key)
    return null
  }
}

function writeJson(key, value) {
  if (value === null || value === undefined) {
    localStorage.removeItem(key)
    return
  }
  localStorage.setItem(key, JSON.stringify(value))
}

export const session = {
  getUser() {
    return readJson(USER_KEY)
  },
  setUser(user) {
    writeJson(USER_KEY, user)
  },
  clearUserFlow() {
    localStorage.removeItem(USER_KEY)
    localStorage.removeItem(USER_ASSESSMENT_KEY)
    localStorage.removeItem(USER_RECOMMENDATION_KEY)
    localStorage.removeItem(USER_REPORT_KEY)
    localStorage.removeItem(USER_SELECTED_FAVORITE_KEY)
    localStorage.removeItem(ENTRY_SPLASH_KEY)
  },
  getDoctor() {
    return readJson(DOCTOR_KEY)
  },
  setDoctor(doctor) {
    writeJson(DOCTOR_KEY, doctor)
  },
  clearDoctor() {
    localStorage.removeItem(DOCTOR_KEY)
    localStorage.removeItem(ENTRY_SPLASH_KEY)
  },
  getAdmin() {
    return readJson(ADMIN_KEY)
  },
  setAdmin(admin) {
    writeJson(ADMIN_KEY, admin)
  },
  clearAdmin() {
    localStorage.removeItem(ADMIN_KEY)
    localStorage.removeItem(ENTRY_SPLASH_KEY)
  },
  getAssessment() {
    return readJson(USER_ASSESSMENT_KEY)
  },
  setAssessment(payload) {
    writeJson(USER_ASSESSMENT_KEY, payload)
  },
  getRecommendation() {
    return readJson(USER_RECOMMENDATION_KEY)
  },
  setRecommendation(payload) {
    writeJson(USER_RECOMMENDATION_KEY, payload)
  },
  getReport() {
    return readJson(USER_REPORT_KEY)
  },
  setReport(payload) {
    writeJson(USER_REPORT_KEY, payload)
  },
  getSelectedFavorite() {
    return readJson(USER_SELECTED_FAVORITE_KEY)
  },
  setSelectedFavorite(payload) {
    writeJson(USER_SELECTED_FAVORITE_KEY, payload)
  },
  getEntrySplash() {
    return readJson(ENTRY_SPLASH_KEY)
  },
  setEntrySplash(payload) {
    writeJson(ENTRY_SPLASH_KEY, payload)
  }
}
