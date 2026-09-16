const BASE = '/api'

async function request(path, options = {}) {
  const response = await fetch(`${BASE}${path}`, {
    headers: { 'Content-Type': 'application/json' },
    ...options
  })

  if (!response.ok) {
    const body = await response.json().catch(() => null)
    throw new Error(body?.message || `Request failed with status ${response.status}`)
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
