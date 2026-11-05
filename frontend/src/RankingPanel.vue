<script setup>
import { ref, watch } from 'vue'
import { getRanking, getStudentRanking } from './api'
const props = defineProps({ studentId: {type: Number, required: true}, revision: {type: Number, default: 0} })
const page = ref(0)
const leaderboard = ref({entries: [], totalPages: 0, totalElements: 0})
const standing = ref(null)
const loading = ref(false)
const error = ref('')
let generation = 0
async function load() {
  const current = ++generation
  loading.value = true
  error.value = ''
  try {
    const [ranking, personal] = await Promise.all([getRanking(page.value), getStudentRanking(props.studentId)])
    if (current !== generation) return
    leaderboard.value = ranking
    standing.value = personal
  } catch (failure) { if (current === generation) error.value = failure.message }
  finally { if (current === generation) loading.value = false }
}
watch(() => [props.studentId, props.revision, page.value], load, {immediate: true})
</script>
<template>
  <aside class="gamification" aria-label="Ranking">
    <div class="sidebar-heading"><span class="course-kicker">Sua evolução</span><h2>Seu ranking</h2></div>
    <div class="stat"><span>Pontos</span><strong data-cy="points">{{ standing?.points ?? '—' }}</strong><small>nota × 10 por curso concluído</small></div>
    <div class="personal-standing"><span>Sua posição <strong data-cy="position">{{ standing?.position ? `#${standing.position}` : '—' }}</strong></span></div>
    <section class="card game-card">
      <div class="game-header"><h3>Ranking geral</h3><span class="muted">{{ leaderboard.totalElements }} {{ leaderboard.totalElements === 1 ? 'participante' : 'participantes' }}</span></div>
      <p v-if="loading" class="muted" role="status">Carregando ranking…</p><p v-if="error" class="quiz-error" role="alert">{{ error }}</p>
      <table class="ranking-table" data-cy="ranking-table"><thead><tr><th scope="col">Pos.</th><th scope="col">Aluno</th><th scope="col">Pontos</th></tr></thead><tbody><tr v-for="item in leaderboard.entries" :key="item.studentId" :class="{'my-ranking': item.studentId === studentId}"><td>#{{ item.position }}</td><td>{{ item.studentName }}<small v-if="item.studentId === studentId">Você</small></td><td>{{ item.points }}</td></tr></tbody></table>
      <p v-if="!loading && !leaderboard.entries.length" class="muted">O ranking começa com o primeiro curso concluído.</p>
      <div class="pager"><button class="small-button" aria-label="Página anterior" :disabled="page === 0 || loading" @click="page--">←</button><span>{{ page + 1 }} de {{ Math.max(1, leaderboard.totalPages) }}</span><button class="small-button" aria-label="Próxima página" :disabled="page + 1 >= leaderboard.totalPages || loading" @click="page++">→</button></div>
      <p class="ranking-note muted">Sua nota vale pontos. Continue aprendendo para avançar no ranking.</p>
    </section>
    <button class="refresh-progress" :disabled="loading" @click="load">↻ Atualizar ranking</button>
  </aside>
</template>
