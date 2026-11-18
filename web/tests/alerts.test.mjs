import test from 'node:test'
import assert from 'node:assert/strict'
import { lowStockProducts, inactiveSalesProducts } from '../src/lib/alerts.ts'

const product = (id, quantities, stock_minimo) => ({
  id, stock_minimo, existencias: quantities.map(cantidad => ({cantidad}))
})
const catalog = [product('a',[2,3],5), product('b',[],0), product('c',[3,3],5)]

test('mínimo inclusivo sobre el stock total de todas las ubicaciones', () => {
  assert.deepEqual(lowStockProducts(catalog).map(p => p.id), ['a','b'])
})

test('sin movimiento incluye los productos sin líneas confirmadas visibles', () => {
  const lines = ['a','a','x'].map(producto_id => ({producto_id}))
  assert.deepEqual(inactiveSalesProducts(catalog, lines).map(p => p.id), ['b','c'])
  assert.deepEqual(inactiveSalesProducts(catalog, []), catalog)
})
