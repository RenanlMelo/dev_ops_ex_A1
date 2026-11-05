describe('B1 - Ranking, moedas, conquistas e recompensas', () => {
  it('resgata a mentoria e mantém saldo e histórico após recarregar', () => {
    cy.visit('/')
    cy.get('[data-cy=coins]').should('have.text', '300')
    cy.get('[data-cy=rewards-tab]').click()
    cy.get('[data-cy=redeem-mentoria]').click()
    cy.contains('Resgate registrado: Mentoria.')
    cy.get('[data-cy=coins]').should('have.text', '150')
    cy.get('[data-cy=redemptions]').should('contain', 'Mentoria')
    cy.reload()
    cy.get('[data-cy=coins]').should('have.text', '150')
    cy.get('[data-cy=rewards-tab]').click()
    cy.get('[data-cy=redemptions]').should('contain', 'Mentoria')
  })
  it('atualiza a nota da mesma matrícula sem duplicar moedas', () => {
    const stamp = Date.now()
    let studentId
    let enrollmentId
    cy.request('POST', '/api/students', { name: `Idempotência B1 ${stamp}` }).then(({body}) => {
      studentId = body.id
      return cy.request('POST', '/api/courses', { name: `Curso ${stamp}` })
    }).then(({body}) => cy.request('POST', '/api/enrollments', {studentId, courseId:body.id}))
      .then(({body}) => {
        enrollmentId = body.id
        return cy.request('POST', `/api/enrollments/${enrollmentId}/complete`, {grade:8.5})
      }).then(() => cy.request('POST', `/api/enrollments/${enrollmentId}/complete`, {grade:10}))
      .then(() => cy.request(`/api/students/${studentId}/wallet`))
      .then(({body}) => { expect(body.balance).to.eq(50); expect(body.entries).to.have.length(1) })
      .then(() => cy.request(`/api/ranking/students/${studentId}`))
      .then(({body}) => { expect(body.points).to.eq(100); expect(body.completedCourses).to.eq(1) })
  })
  it('exibe ranking, três conquistas e resultados dos cursos concluídos', () => {
    cy.visit('/')
    cy.get('[data-cy=ranking-table]').should('contain', 'Renan')
    cy.get('[data-cy=position]').should('have.text', '#1')
    cy.get('[data-cy=achievements-tab]').click()
    ;['primeira-aprovacao', 'tres-aprovacoes', 'nota-dez'].forEach(code => {
      cy.get(`[data-cy=badge-${code}]`).should('have.class', 'unlocked')
    })
    cy.contains('button', 'Concluídos').click()
    cy.get('[data-cy=course-card]').should('have.length', 6)
    cy.contains('[data-cy=course-card]', 'Testes de Software').contains('button', 'Ver resultado').click()
    cy.get('.result-grade').should('contain', '10.0')
    cy.contains('button', 'Confirmar resposta').should('not.exist')
  })
})
