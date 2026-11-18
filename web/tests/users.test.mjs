import test from 'node:test'
import assert from 'node:assert/strict'
import {normalizedRut} from '../src/lib/users.ts'

test('normalizes Chilean RUT with numeric, zero and K check digits', () => {
  assert.equal(normalizedRut('123456785'), '12.345.678-5')
  assert.equal(normalizedRut('6-k'), '6-K')
  assert.equal(normalizedRut('14-0'), '14-0')
})
test('rejects malformed identifiers and incorrect check digits', () => {
  for (const value of ['', '12345678-4', 'abc6-k', '1234567890']) assert.equal(normalizedRut(value), null)
})
