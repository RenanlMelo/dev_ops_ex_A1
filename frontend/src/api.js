const BASE = '/api'

async function request(path, options = {}) {
  const response = await fetch(`${BASE}${path}`, {
    headers: { 'Content-Type': 'application/json' },
    ...options
  })

  if (!response.ok) {
    const body = await response.json().catch(() => null)
    const error = new Error(body?.message || `Request failed with status ${response.status}`)
    error.status = response.status
    throw error
  }

  if (response.status === 204) return null
  return response.json()
}

export function createStudent(name) {
  return request('/students', { method: 'POST', body: JSON.stringify({ name }) })
}

export function createCourse(name) {
  return request('/courses', { method: 'POST', body: JSON.stringify({ name }) })
}

export function createEnrollment(courseId, studentId) {
  return request('/enrollments', {
    method: 'POST',
    body: JSON.stringify({ courseId, studentId })
  })
}

export function completeEnrollment(enrollmentId, grade) {
  return request(`/enrollments/${enrollmentId}/complete`, {
    method: 'POST',
    body: JSON.stringify({ grade })
  })
}

export function getEligibility(studentId) {
  return request(`/students/${studentId}/eligible-for-extra-courses`)
}

export const listStudents = (search = '') => request(`/students?limit=100&search=${encodeURIComponent(search)}`)
export const listCourses = () => request('/courses?limit=100')
export const getLearningSpace = () => request('/learning-space')
export const getRanking = (page = 0) => request(`/ranking?page=${page}&size=5`)
export const getStudentRanking = id => request(`/ranking/students/${id}`)
export const getAchievements = id => request(`/students/${id}/achievements`)
export const getWallet = id => request(`/students/${id}/wallet`)
export const getRewards = () => request('/rewards')
export const getRedemptions = id => request(`/students/${id}/redemptions`)
export const redeemReward = (id, rewardCode, requestId) => request(`/students/${id}/redemptions`, {
  method: 'POST', body: JSON.stringify({ rewardCode, requestId })
})
