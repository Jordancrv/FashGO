import test from 'node:test'
import assert from 'node:assert/strict'
import { remainingUnits, validReturn } from '../src/lib/returns.ts'

test('successive partial returns consume the available units on the original line', () => {
  const units = remainingUnits(5,[{cantidad:1},{cantidad:2}])
  assert.equal(units,2)
  assert.equal(validReturn('2',units,'Talla incorrecta'),true)
  assert.equal(validReturn('3',units,'Talla incorrecta'),false)
  assert.equal(validReturn('1',remainingUnits(2,[{cantidad:2}]),'Talla'),false)
})

test('returns require whole positive units and a reason', () => {
  for (const amount of ['','0','-1','1.5','1e1']) assert.equal(validReturn(amount,20,'Talla'),false)
  assert.equal(validReturn('1',1,'   '),false)
})
