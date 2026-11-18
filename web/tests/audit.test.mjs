import test from 'node:test'
import assert from 'node:assert/strict'
import { createClient } from '@supabase/supabase-js'
import { fetchMovements } from '../src/lib/audit.ts'

test('la bitácora conserva el filtro y desempata fechas con el UUID del último registro visible', async () => {
  let url
  const client = createClient('https://example.invalid', 'test', {
    auth: { persistSession: false, autoRefreshToken: false },
    global: { fetch: async input => {
      url = new URL(input)
      return Response.json([])
    } },
  })
  const cursor = { fecha: '2026-11-17T12:15:00.125+00:00', id: '00000000-0000-0000-0000-000000000012' }
  await fetchMovements(client, 'inventario', cursor, 26)
  assert.equal(url.pathname, '/rest/v1/auditoria_movimientos')
  assert.equal(url.searchParams.get('order'), 'fecha.desc,id.desc')
  assert.equal(url.searchParams.get('entidad'), 'eq.inventario')
  assert.equal(url.searchParams.get('limit'), '26')
  assert.equal(url.searchParams.get('or'), `(fecha.lt.${cursor.fecha},and(fecha.eq.${cursor.fecha},id.lt.${cursor.id}))`)
  await fetchMovements(client, '', undefined, 6)
  assert.equal(url.searchParams.has('or'), false)
  assert.equal(url.searchParams.has('entidad'), false)
})
