# Frontend - US4 (versão mockada, sem backend)

Versão simplificada do front, sem chamadas HTTP: aluno e curso são fixos no
próprio componente, só para testar o fluxo visual da US4.

## Fluxo

1. A tela abre com um curso não concluído; a média fica escondida.
2. O botão "Responder pergunta para concluir o curso" mostra uma pergunta
   de múltipla escolha.
3. Acertando: o curso conclui com nota 8,5 (acima de 7,0) e libera o selo
   "Elegível para mais 3 cursos".
4. Errando: o curso conclui com nota 4,5 (abaixo de 7,0) e mostra
   "Não elegível para cursos extras".
5. O botão "Reiniciar simulação" volta ao estado inicial para testar de novo.

## Como rodar

```bash
npm install
npm run dev
```

Abre em `http://localhost:5173`. Não precisa do backend rodando.

## Personalizar

- Nome do aluno/curso e a pergunta/opções ficam no topo de `src/App.vue`
  (`student`, `course`, `question`).
- As notas usadas (8,5 / 4,5) também estão em `App.vue`, na função
  `confirmAnswer`.
