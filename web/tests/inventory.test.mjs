import test from 'node:test'
import assert from 'node:assert/strict'
import {classify} from '../src/lib/abc.ts'
import {productIdentity} from '../src/lib/productIdentity.ts'

test('ABC ranks active sellers by total units, including ties and unsold stock', () => {
  const products = Array.from('abcdef', id => ({id}))
  const ids = ['c','d','e','f','e','f','c','inactive']
  const lines = ids.map(producto_id => ({producto_id,cantidad:2}))
  assert.deepEqual([...classify(products,lines).values()], ['C','C','A','B','A','B'])
  assert.deepEqual([...classify(products,[]).values()], Array(6).fill('C'))
})

test('Lower garment SKU advances only its variant and omits sleeves', () => {
  const draft = {parte_general:'Parte Inferior',tipo:'Calzas',diseno:'Estándar',tipo_manga:'NA',tipo_cuello:'NA',color:'Marrón',talla:'M'}
  assert.deepEqual(productIdentity(draft,[{codigo:'PAI-CAL-ET-MAR-M-003'},{codigo:'other-900'}]), {
    codigo:'PAI-CAL-ET-MAR-M-004',nombre:'Calzas - Estándar - Marrón - Talla M'
  })
})
