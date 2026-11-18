import test from 'node:test'
import assert from 'node:assert/strict'
import { workshopPayload, recordConsumption } from '../src/lib/workshop.ts'

function form(values) {
  const data = new FormData()
  for (const [key,value] of Object.entries(values)) data.set(key,value)
  return data
}

test('derivative edit preserves classification and sends null for an unused zone', () => {
  const input = form({tipo_prenda:' Top ',subcategoria:'Crop',color:'Verde',cantidad:'2',precio_unitario:'16.50'})
  const payload = workshopPayload('productos_derivados',input,{rotacion:'Alta',decision_sugerida:'Mantener'})
  assert.equal(payload.tipo_prenda,'Top')
  assert.equal(payload.zona_auxiliar,null)
  assert.equal(payload.precio_unitario,16.5)
  assert.equal(payload.rotacion,'Alta')
  assert.equal(payload.decision_sugerida,'Mantener')
  input.set('cantidad','1.5')
  assert.throws(() => workshopPayload('productos_derivados',input), /Cantidad/)
})

test('material permits zero stock and fractional quantities up to two decimals', () => {
  const input = form({nombre:'Lino',unidad_medida:'m',cantidad:'0'})
  assert.equal(workshopPayload('materia_prima',input).cantidad,0)
  input.set('cantidad','0.25')
  assert.equal(workshopPayload('materia_prima',input).cantidad,0.25)
  input.set('cantidad','0.125')
  assert.throws(() => workshopPayload('materia_prima',input), /Cantidad/)
})

test('uncertain consumption replays the original UUID and amount even after stock changes', async () => {
  const saved = new Map()
  const sent = []
  let pending = null
  const options = {key:'user/material', available:2, quantity:2, pending,
    storage:{getItem:k=>saved.get(k),setItem:(k,v)=>saved.set(k,v),removeItem:k=>saved.delete(k)},
    onPending:v=>{pending=v},send:async request=>{
      sent.push(request)
      return {error:sent.length===1 ? {code:'',message:'network'} : null}
    }}
  await assert.rejects(recordConsumption(options), /network/)
  await assert.rejects(recordConsumption(options), /otra pestaña/)
  await recordConsumption({...options,pending,available:0,quantity:1})
  assert.deepEqual(sent[0],sent[1])
  assert.equal(sent[1].quantity,2)
  assert.equal(saved.size,0)
})
