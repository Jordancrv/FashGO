export function normalizedRut(value: string) {
  const raw = value.trim().replace(/[.-]/g, '').toUpperCase()
  if (!/^\d{1,8}[\dK]$/.test(raw)) return null
  const body = raw.slice(0, -1)
  const sum = Array.from(body).reverse().reduce((total, digit, index) =>
    total + Number(digit) * (2 + index % 6), 0)
  const remainder = 11 - sum % 11
  const verifier = remainder === 11 ? '0' : remainder === 10 ? 'K' : String(remainder)
  if (raw.at(-1) !== verifier) return null
  return body.replace(/\B(?=(\d{3})+(?!\d))/g, '.') + '-' + verifier
}
