import test from 'node:test'
import assert from 'node:assert/strict'
import { checkout, cashCents, saleRequest } from '../src/lib/checkout.ts'

const product = {id:'p', nombre:'Camisa', precio:35.15, existencias:[{cantidad:2},{cantidad:1}]}
const lines = [{producto_id:'p', cantidad:3}]

test('sums stock across locations and calculates change in cents', () => {
  assert.deepEqual(checkout(lines,[product],'110,50'), {total:10545,received:11050,change:505,problem:''})
  assert.ok(checkout(lines,[product],'105.44').problem)
})
test('rejects missing products, overselling and malformed cash', () => {
  assert.ok(checkout(lines,[], '200').problem)
  assert.ok(checkout([{producto_id:'p',cantidad:4}],[product],'200').problem)
  for (const value of ['1e5','3.333','','-50','Infinity']) assert.equal(cashCents(value),null)
})
test('persists a stable RPC body independent of later edits', () => {
  const draft = [{producto_id:'p',cantidad:2}]
  const request = saleRequest('uuid',draft,8000)
  const stored = JSON.stringify(request)
  draft[0].cantidad = 1
  assert.deepEqual(JSON.parse(stored),request)
  assert.equal(request.p_operacion,'uuid')
  assert.equal(request.p_monto_recibido,80)
  assert.equal(request.p_lineas[0].cantidad,2)
})
