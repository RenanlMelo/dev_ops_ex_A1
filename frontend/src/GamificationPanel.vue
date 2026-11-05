<script setup>
import { computed, ref, watch } from 'vue'
import { getRanking, getStudentRanking, getAchievements, getWallet, getRewards, getRedemptions, redeemReward } from './api'

const props = defineProps({ studentId: { type: Number, default: null }, revision: { type: Number, default: 0 } })
const tab = ref('ranking')
const page = ref(0)
const leaderboard = ref({ entries: [], page: 0, totalPages: 0, totalElements: 0 })
const wallet = ref({ balance: 0, entries: [] })
const achievements = ref({ badges: [], points: 0, approvedCourses: 0 })
const standing = ref(null)
const rewards = ref([])
const redemptions = ref([])
const loading = ref(false)
const redeeming = ref(false)
const error = ref('')
const message = ref('')
const earnedBadges = computed(() => achievements.value.badges.filter(b => b.earnedAt))
const pendingRequests = new Map()
let generation = 0

function newRequestId() {
  // getRandomValues also works on HTTP hostnames used by Docker/Cypress.
  return Array.from(crypto.getRandomValues(new Uint8Array(16)), value => value.toString(16).padStart(2, '0')).join('')
}

async function load() {
  const request = ++generation
  const id = props.studentId
  loading.value = true
  error.value = ''
  try {
    const [ranking, catalog, personal] = await Promise.all([
      getRanking(page.value), getRewards(), id ? Promise.all([
        getWallet(id), getAchievements(id), getStudentRanking(id), getRedemptions(id)
      ]) : Promise.resolve(null)
    ])
    if (request !== generation) return
    leaderboard.value = ranking
    rewards.value = catalog
    wallet.value = personal?.[0] || { balance: 0, entries: [] }
    achievements.value = personal?.[1] || { badges: [], points: 0, approvedCourses: 0 }
    standing.value = personal?.[2] || null
    redemptions.value = personal?.[3] || []
  } catch (failure) { if (request === generation) error.value = failure.message }
  finally { if (request === generation) loading.value = false }
}

async function redeem(reward) {
  if (!props.studentId || redeeming.value) return
  redeeming.value = true
  message.value = ''
  error.value = ''
  const id = props.studentId
  const key = `${id}:${reward.code}`
  try {
    if (!pendingRequests.has(key)) pendingRequests.set(key, newRequestId())
    await redeemReward(id, reward.code, pendingRequests.get(key))
    pendingRequests.delete(key)
    await load()
    if (props.studentId === id) message.value = `Resgate registrado: ${reward.name}.`
  } catch (failure) {
    if (failure.status && failure.status < 500) pendingRequests.delete(key)
    error.value = failure.message
  } finally { redeeming.value = false }
}

function date(value) { return new Date(value).toLocaleString('pt-BR') }
watch(() => [props.studentId, props.revision, page.value], () => { message.value = ''; load() }, { immediate: true })
</script>

<template>
<aside class="gamification" aria-label="Gamificação">
  <div class="sidebar-heading"><span class="course-kicker">Cada passo conta</span><h2>Seu progresso</h2></div>
  <div class="stat-grid" aria-live="polite">
    <div class="stat"><span>Pontos</span><strong data-cy="points">{{ achievements.points }}</strong><small>nota × 10 por curso</small></div>
    <div class="stat stat-coins"><span>Moedas</span><strong data-cy="coins">{{ wallet.balance }}</strong><small>50 por aprovação</small></div>
  </div>
  <div class="personal-standing"><span>Sua posição <strong data-cy="position">{{ standing?.position ? `#${standing.position}` : '—' }}</strong></span><span>{{ earnedBadges.length }} conquistas</span></div>
  <section class="card game-card">
    <div class="game-tabs" role="group" aria-label="Áreas da gamificação">
      <button @click="tab = 'ranking'" :aria-pressed="tab === 'ranking'" data-cy="ranking-tab">Ranking</button>
      <button @click="tab = 'progress'" :aria-pressed="tab === 'progress'" data-cy="achievements-tab">Conquistas</button>
      <button @click="tab = 'rewards'" :aria-pressed="tab === 'rewards'" data-cy="rewards-tab">Recompensas</button>
    </div>
    <p v-if="loading" class="muted" role="status">Carregando seu progresso…</p>
    <p v-if="error" class="quiz-error" role="alert">{{ error }}</p>
    <p v-if="message" class="success-message" role="status">{{ message }}</p>

    <div v-if="tab === 'progress' && studentId">
      <p class="muted">{{ achievements.approvedCourses }} {{ achievements.approvedCourses === 1 ? 'curso' : 'cursos' }} com nota acima de 7. As conquistas ficam salvas no seu perfil.</p>
      <div class="badge-list">
        <article v-for="badge in achievements.badges" :key="badge.code" class="achievement" :class="{ unlocked: badge.earnedAt }" :data-cy="`badge-${badge.code}`">
          <span class="achievement-icon" aria-hidden="true">{{ badge.earnedAt ? '★' : '☆' }}</span>
          <div><h3>{{ badge.name }}</h3><p>{{ badge.description }}</p><small>{{ badge.earnedAt ? `Conquistado em ${date(badge.earnedAt)}` : 'Ainda não conquistado' }}</small></div>
        </article>
      </div>
      <h3 class="ledger-title">Últimas movimentações de moedas</h3>
      <p v-if="!wallet.entries.length" class="muted">Conclua um curso com nota acima de 7 para ganhar suas primeiras moedas.</p>
      <ul class="wallet-list">
        <li v-for="entry in wallet.entries" :key="entry.reference"><div><span>{{ entry.description }}</span><small>{{ date(entry.createdAt) }}</small></div><strong :class="entry.amount > 0 ? 'positive' : 'negative'">{{ entry.amount > 0 ? '+' : '' }}{{ entry.amount }}</strong></li>
      </ul>
    </div>

    <div v-if="tab === 'ranking'">
      <div class="game-header"><h3>Ranking geral</h3><span class="muted">{{ leaderboard.totalElements }} {{ leaderboard.totalElements === 1 ? 'participante' : 'participantes' }}</span></div>
      <div class="table-scroll"><table class="ranking-table" data-cy="ranking-table">
        <thead><tr><th scope="col">Pos.</th><th scope="col">Aluno</th><th scope="col">Pontos</th></tr></thead>
        <tbody><tr v-for="entry in leaderboard.entries" :key="entry.studentId" :class="{ 'my-ranking': entry.studentId === studentId }"><td>#{{ entry.position }}</td><td>{{ entry.studentName }}<small v-if="entry.studentId === studentId">Você</small></td><td>{{ entry.points }}</td></tr></tbody>
      </table></div>
      <p v-if="!leaderboard.entries.length" class="empty-state">O ranking começa com o primeiro curso concluído.</p>
      <div class="pager"><button class="small-button" @click="page--" :disabled="page === 0 || loading" aria-label="Página anterior">←</button><span>{{ page + 1 }} de {{ Math.max(1, leaderboard.totalPages) }}</span><button class="small-button" @click="page++" :disabled="page + 1 >= leaderboard.totalPages || loading" aria-label="Próxima página">→</button></div>
      <p class="muted ranking-note">Sua nota vale pontos. Continue aprendendo para avançar no ranking.</p>
    </div>

    <div v-if="tab === 'rewards'">
      <p class="muted">Use suas moedas para registrar o direito a um benefício. Você pode acompanhar cada resgate no histórico abaixo.</p>
      <div class="reward-list"><article v-for="reward in rewards" :key="reward.code" class="reward">
        <div><span class="reward-price">{{ reward.cost }} moedas</span><h3>{{ reward.name }}</h3><p>{{ reward.description }}</p></div>
        <button class="small-button reward-button" :data-cy="`redeem-${reward.code}`" @click="redeem(reward)" :disabled="!studentId || redeeming || loading || wallet.balance < reward.cost">{{ wallet.balance < reward.cost ? 'Saldo insuficiente' : 'Resgatar' }}</button>
      </article></div>
      <h3 class="ledger-title">Meus resgates</h3>
      <p v-if="!redemptions.length" class="muted">Seus benefícios resgatados aparecerão aqui.</p>
      <ul class="wallet-list" data-cy="redemptions"><li v-for="item in redemptions" :key="item.requestId"><div><span>{{ item.rewardName }}</span><small>{{ date(item.redeemedAt) }}</small></div><strong>{{ item.cost }} moedas</strong></li></ul>
    </div>
  </section>
  <button class="refresh-progress" @click="load" :disabled="loading || redeeming">↻ Atualizar progresso</button>
</aside>
</template>
