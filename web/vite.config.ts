import react from '@vitejs/plugin-react'
import { defineConfig, loadEnv } from 'vite'

// https://vite.dev/config/
export default defineConfig(({ command, mode }) => {
  if (command === 'build') {
    const env = loadEnv(mode, process.cwd(), 'VITE_')
    const missing = ['VITE_SUPABASE_URL', 'VITE_SUPABASE_PUBLISHABLE_KEY']
      .filter(name => !env[name]?.trim())
    if (missing.length) {
      throw new Error('Faltan variables de compilación: ' + missing.join(', '))
    }
  }
  return { plugins: [react()] }
})
