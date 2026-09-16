<script setup>
import { computed, reactive, ref } from 'vue'

// Curso mockado (sem backend) - só para testar o fluxo do front.
const course = reactive({
  name: 'Introdução à Educação Continuada Gamificada',
  completed: false,
  grade: null
})

const question = {
  prompt: '10/10 - Qual é a nota mínima para um aluno ter direito a mais 3 cursos extras?',
  options: ['5,0', '6,0', '7,0', '9,0'],
  correctIndex: 2
}

const OPTION_LETTERS = ['A', 'B', 'C', 'D']

const steps = [
  { id: 1, label: 'Curso' },
  { id: 2, label: 'Avaliação' },
  { id: 3, label: 'Resultado' }
]

const quizStarted = ref(false)
const selectedOption = ref(null)
const answered = ref(false)
const wasCorrect = ref(false)
const confettiPieces = ref([])

const isEligibleForExtraCourses = computed(() => {
  return course.completed && course.grade !== null && course.grade > 7.0
})

const currentStep = computed(() => {
  if (!quizStarted) return 1
  if (!answered) return 2
  return 3
})

const gradePercent = computed(() => {
  if (course.grade === null) return 0
  return Math.min(100, Math.max(0, (course.grade / 10) * 100))
})

const CONFETTI_COLORS = ['#4f46e5', '#22c55e', '#f59e0b', '#ef4444', '#a855f7', '#06b6d4']

const CORNERS = [
  { top: '0%', left: '0%', baseAngle: 225 },
  { top: '0%', left: '100%', baseAngle: 315 },
  { top: '100%', left: '0%', baseAngle: 135 },
  { top: '100%', left: '100%', baseAngle: 45 }
]

function buildConfetti() {
  const pieces = []
  let id = 0

  CORNERS.forEach((corner) => {
    for (let i = 0; i < 6; i++) {
      const angle = corner.baseAngle + (Math.random() * 60 - 30)
      const distance = 50 + Math.random() * 60
      const rad = (angle * Math.PI) / 180
      const dx = Math.cos(rad) * distance
      const dy = Math.sin(rad) * distance

      pieces.push({
        id: id++,
        style: {
          top: corner.top,
          left: corner.left,
          '--dx': `${dx}px`,
          '--dy': `${dy}px`,
          '--rotation': `${Math.random() * 720 - 360}deg`,
          '--delay': `${Math.random() * 120}ms`,
          '--color': CONFETTI_COLORS[Math.floor(Math.random() * CONFETTI_COLORS.length)],
          '--size': `${6 + Math.random() * 6}px`,
          '--radius': Math.random() > 0.5 ? '50%' : '2px'
        }
      })
    }
  })

  return pieces
}

function startQuiz() {
  quizStarted.value = true
  selectedOption.value = null
  answered.value = false
}

function confirmAnswer() {
  if (selectedOption.value === null) return

  wasCorrect.value = selectedOption.value === question.correctIndex
  answered.value = true

  course.completed = true
  course.grade = wasCorrect.value ? 8.5 : 4.5

  confettiPieces.value = wasCorrect.value ? buildConfetti() : []
}

function resetSimulation() {
  course.completed = false
  course.grade = null
  quizStarted.value = false
  selectedOption.value = null
  answered.value = false
  confettiPieces.value = []
}
</script>

<template>
  <main class="page">
    <header class="page-header">
      <span class="eyebrow">US4 · Plataforma EAD</span>
      <h1>Educação Continuada Gamificada</h1>
      <p class="subtitle">Média por curso concluído e acesso a 3 cursos extras (nota &gt; 7,0)</p>
    </header>

    <section class="card">
      <div class="course-head">
        <div class="course-head-text">
          <span class="course-kicker">Curso</span>
          <h2>{{ course.name }}</h2>
        </div>

        <span
          class="badge"
          :class="course.completed ? 'badge-neutral' : 'badge-pending'"
          :tabindex="!course.completed ? 0 : -1"
        >
          {{ course.completed ? 'Curso concluído' : 'Curso não concluído' }}
          <span v-if="!course.completed" class="badge-tooltip">
            Sua média fica disponível somente depois que o curso for concluído.
          </span>
        </span>
      </div>

      <div class="progress-track">
        <div
          v-for="step in steps"
          :key="step.id"
          class="progress-segment"
          :class="{ 'is-done': currentStep > step.id, 'is-active': currentStep === step.id }"
        ></div>
      </div>
      <div class="progress-labels">
        <span
          v-for="step in steps"
          :key="step.id"
          :class="{ 'is-done': currentStep > step.id, 'is-current': currentStep === step.id }"
        >
          {{ step.label }}
        </span>
      </div>

      <Transition name="fade-slide" mode="out-in">
        <div v-if="!course.completed" key="pending" class="stage stage-locked">
          <div class="status-panel">
            <span class="status-panel-icon" aria-hidden="true">
              <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <rect x="4.5" y="10.5" width="15" height="9" rx="2.5"></rect>
                <path d="M8 10.5V7.5a4 4 0 0 1 8 0v3"></path>
              </svg>
            </span>
            <div class="status-panel-info">
              <span class="status-panel-label">Média do curso</span>
              <span class="status-panel-value">Não disponível</span>
            </div>
          </div>

          <Transition name="fade-slide" mode="out-in">
            <button
              v-if="!quizStarted"
              key="start-btn"
              class="btn-primary btn-block"
              @click="startQuiz"
            >
              Retomar Avaliação final
            </button>

            <div v-else key="quiz" class="quiz-card">
              <div class="quiz-meta">
                <span class="quiz-tag">Avaliação final</span>
                <span class="quiz-progress">Pergunta 1 de 1</span>
              </div>

              <p class="quiz-prompt">{{ question.prompt }}</p>

              <div class="quiz-options">
                <label
                  v-for="(option, index) in question.options"
                  :key="index"
                  class="quiz-option"
                  :class="{ selected: selectedOption === index }"
                >
                  <input
                    type="radio"
                    name="quiz-option"
                    :value="index"
                    v-model="selectedOption"
                    :disabled="answered"
                  />
                  <span class="option-letter">{{ OPTION_LETTERS[index] }}</span>
                  <span class="option-text">{{ option }}</span>
                  <svg class="option-check" viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M20 6 9 17l-5-5"></path>
                  </svg>
                </label>
              </div>

              <button
                class="btn-primary btn-block"
                :disabled="selectedOption === null || answered"
                @click="confirmAnswer"
              >
                Confirmar resposta
              </button>
            </div>
          </Transition>
        </div>

        <div v-else key="result" class="stage stage-result">
          <div class="result-icon" :class="wasCorrect ? 'is-success' : 'is-fail'">
            <svg v-if="wasCorrect" viewBox="0 0 24 24" width="26" height="26" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
              <path d="M20 6 9 17l-5-5"></path>
            </svg>
            <svg v-else viewBox="0 0 24 24" width="24" height="24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
              <path d="M18 6 6 18M6 6l12 12"></path>
            </svg>
          </div>

          <p class="feedback" :class="wasCorrect ? 'feedback-ok' : 'feedback-fail'">
            {{ wasCorrect ? 'Você acertou a pergunta!' : 'Você errou a pergunta.' }}
          </p>

          <div class="score-gauge-wrapper">
            <div class="score-gauge" :style="{ '--pct': gradePercent + '%', '--gauge-color': wasCorrect ? 'var(--success)' : 'var(--danger)' }">
              <div class="score-gauge-inner">
                <span class="score-value">{{ course.grade.toFixed(1) }}</span>
                <span class="score-max">Média final</span>
              </div>
            </div>

            <span
              v-for="piece in confettiPieces"
              :key="piece.id"
              class="confetti-piece"
              :style="piece.style"
            ></span>
          </div>

          <div class="eligibility-chip" :class="isEligibleForExtraCourses ? 'is-eligible' : 'is-not-eligible'">
            {{
              isEligibleForExtraCourses
                ? '🎉 Elegível para mais 3 cursos'
                : 'Não elegível para cursos extras'
            }}
          </div>

          <button class="btn-ghost" @click="resetSimulation">Reiniciar simulação</button>
        </div>
      </Transition>
    </section>
  </main>
</template>
