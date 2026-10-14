// Plain configuration object: works with the global Cypress in cypress/included
// without requiring frontend/node_modules inside the Jenkins workspace.
export default {
  e2e: {
    baseUrl: process.env.CYPRESS_BASE_URL || 'http://localhost:8082',
    supportFile: false
  }
}