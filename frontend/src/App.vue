<script setup>
import { computed, onMounted, ref } from 'vue'
import { completeEnrollment, getEligibility, getLearningSpace } from './api'

const workspace = ref(null)
const search = ref('')
const filter = ref('all')
const activeId = ref(null)
const quizStarted = ref(false)
const selectedOption = ref(null)
const submitting = ref(false)
const error = ref('')
const feedback = ref('')
const eligible = ref(false)
const courses = computed(() => workspace.value?.courses || [])
const completedCount = computed(() => courses.value.filter(c => c.completed).length)
const active = computed(() => courses.value.find(c => c.id === activeId.value))
const normalize = text => text.normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase()
const visibleCourses = computed(() => courses.value.filter(c =>
  normalize(c.name).includes(normalize(search.value)) &&
  (filter.value === 'all' || (filter.value === 'done' ? c.completed : !c.completed))))
const options = ['5,0', '6,0', 'Acima de 7,0', 'Qualquer nota']
const descriptions = {
  'Arquitetura de Software': 'Organize sistemas, avalie decisões e conecte os conceitos.',
  'Fundamentos de Programação': 'Lógica, algoritmos e os primeiros passos na programação.',
  'Java e Orientação a Objetos': 'Classes, objetos e boas práticas para desenvolver com Java.',
  'Banco de Dados Relacional': 'Modele dados e explore consultas com SQL.',
  'Testes de Software': 'Crie testes e ganhe confiança nas suas entregas.',
  'Git e Integração Contínua': 'Versione seu código e automatize a integração.'
}
async function load() {
  workspace.value = await getLearningSpace()
}
async function openCourse(course) {
  activeId.value = course.id
  quizStarted.value = false
  selectedOption.value = null
  feedback.value = ''
  error.value = ''
  if (course.completed) {
    try { eligible.value = (await getEligibility(workspace.value.student.id)).eligibleForExtraCourses }
    catch (failure) { error.value = failure.message }
  }
}
async function confirmAnswer() {
  if (selectedOption.value === null || submitting.value) return
  submitting.value = true
  error.value = ''
  const correct = selectedOption.value === 2
  try {
    await completeEnrollment(active.value.enrollmentId, correct ? 8.5 : 4.5)
    await load()
    eligible.value = (await getEligibility(workspace.value.student.id)).eligibleForExtraCourses
    feedback.value = correct ? 'Você acertou a pergunta!' : 'Você errou a pergunta.'
  } catch (failure) { error.value = failure.message }
  finally { submitting.value = false }
}
onMounted(async () => { try { await load() } catch (failure) { error.value = failure.message } })
</script>

<template>
  <header class="site-header"><div class="header-inner">
    <div class="brand"><span class="brand-icon" aria-hidden="true"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.7"><path d="M12 6c-3-2-6-2-9-1v14c3-1 6-1 9 1 3-2 6-2 9-1V5c-3-1-6-1-9 1Zm0 0v14"/></svg></span><div><h1>Educação Continuada Gamificada</h1><span>Aprenda. Conquiste. Evolua.</span></div></div>
    <div class="user-profile" v-if="workspace" data-cy="user-profile"><span class="avatar" aria-hidden="true">{{ workspace.student.name.charAt(0) }}</span><div><strong>{{ workspace.student.name }}</strong><span>Meu aprendizado</span></div></div>
  </div></header>
  <main class="platform-layout">
    <section class="learning-column" aria-label="Meus cursos">
      <label class="course-search"><svg aria-hidden="true" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><circle cx="10.5" cy="10.5" r="6.5"/><path d="m16 16 5 5"/></svg><input v-model="search" data-cy="course-search" type="search" placeholder="O que você quer aprender?" aria-label="Pesquisar meus cursos" /></label>
      <p v-if="error" class="quiz-error" role="alert">{{ error }}</p>
      <p v-if="!workspace && !error" role="status" class="muted">Carregando seus cursos…</p>
      <section v-if="active" class="card evaluation-card" aria-label="Avaliação do curso">
        <div class="course-head"><div><span class="course-kicker">{{ active.completed ? 'Seu resultado' : 'Continue de onde parou' }}</span><h2>{{ active.name }}</h2></div><button class="close-button" aria-label="Fechar avaliação" :disabled="submitting" @click="activeId = null">×</button></div>
        <div v-if="active.completed" class="result-panel"><span class="result-check" aria-hidden="true">✓</span><div><p v-if="feedback" class="feedback">{{ feedback }}</p><p class="result-grade">{{ active.grade.toFixed(1) }} <span>/ 10 · Média final</span></p><p class="muted">Curso concluído. Seu resultado está salvo.</p></div><p class="eligibility-chip">{{ eligible ? 'Elegível para mais 3 cursos' : 'Não elegível para cursos extras' }}</p></div>
        <div v-else-if="!quizStarted" class="continue-panel"><p class="muted">Sua avaliação final está disponível. Responda à pergunta para concluir este curso.</p><button class="btn-primary" @click="quizStarted = true">Retomar Avaliação final</button></div>
        <div v-else class="quiz-card"><div class="quiz-meta"><span class="course-kicker">Avaliação final</span><span class="muted">Pergunta 1 de 1</span></div><p class="quiz-prompt">Qual média libera o direito a mais 3 cursos extras?</p>
          <div class="quiz-options"><label v-for="(option, index) in options" :key="option" class="quiz-option" :class="{ selected: selectedOption === index }"><input type="radio" name="quiz-option" :value="index" v-model="selectedOption" :disabled="submitting"/><span class="option-letter">{{ ['A', 'B', 'C', 'D'][index] }}</span>{{ option }}</label></div>
          <button class="btn-primary" :disabled="selectedOption === null || submitting" @click="confirmAnswer">{{ submitting ? 'Enviando…' : 'Confirmar resposta' }}</button>
        </div>
      </section>
      <div class="section-heading"><div><span class="course-kicker">Sua jornada</span><h2>Meus cursos <span class="count-pill">{{ courses.length }}</span></h2></div><span class="muted">{{ completedCount }} de {{ courses.length }} concluídos</span></div>
      <div class="course-filters" role="group" aria-label="Filtrar cursos"><button :aria-pressed="filter === 'all'" @click="filter = 'all'">Todos</button><button :aria-pressed="filter === 'progress'" @click="filter = 'progress'">Em andamento <span>{{ courses.length - completedCount }}</span></button><button :aria-pressed="filter === 'done'" @click="filter = 'done'">Concluídos <span>{{ completedCount }}</span></button></div>
      <div class="course-grid" data-cy="course-grid"><article v-for="course in visibleCourses" :key="course.id" class="card course-tile" :class="{ 'course-in-progress': !course.completed }" data-cy="course-card" :data-status="course.completed ? 'completed' : 'in-progress'">
        <div class="tile-top"><span class="course-symbol" aria-hidden="true">{{ course.completed ? '✓' : '◈' }}</span><span class="badge" :class="course.completed ? 'badge-success' : 'badge-pending'">{{ course.completed ? 'Concluído' : 'Em andamento' }}</span></div><h3>{{ course.name }}</h3><p class="course-description">{{ descriptions[course.name] || 'Continue desenvolvendo seus conhecimentos.' }}</p>
        <div class="tile-footer"><span class="course-result">{{ course.completed ? `Nota ${course.grade.toFixed(1)}` : 'Avaliação final disponível' }}</span><button :class="course.completed ? 'small-button' : 'btn-primary'" :disabled="submitting" @click="openCourse(course)">{{ course.completed ? 'Ver resultado' : 'Continuar curso' }} <span aria-hidden="true">→</span></button></div>
      </article></div>
      <p v-if="workspace && !visibleCourses.length" class="empty-state">Nenhum curso encontrado. Tente outro nome ou filtro.</p>
    </section>
  </main>
</template>
