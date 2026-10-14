describe('Educação Continuada Gamificada - E2E', () => {
  it('abre a home e mostra o curso', () => {
    cy.visit('/')
    cy.contains('h1', 'Educação Continuada Gamificada')
    cy.contains('Curso não concluído')
  })

  it('completa a avaliação com a resposta correta e fica elegível para cursos extras', () => {
    cy.visit('/')

    cy.contains('button', 'Retomar Avaliação final').click()

    // Opção correta: "7,0" (índice 2 das 4 alternativas)
    cy.get('.quiz-option').eq(2).click()

    cy.contains('button', 'Confirmar resposta').click()

    cy.contains('Você acertou a pergunta!')
    cy.contains('Elegível para mais 3 cursos')
  })
})
