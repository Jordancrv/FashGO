import type { Product } from '../types'

export type CartLine = { producto_id: string; cantidad: number }

export function cashCents(value: string) {
  if (!/^\d+(?:[.,]\d{1,2})?$/.test(value.trim())) return null
  const cents = Math.round(Number(value.replace(',', '.')) * 100)
  return Number.isSafeInteger(cents) && cents <= 999999999999 ? cents : null
}

export function checkout(lines: CartLine[], products: Product[], cash: string) {
  const catalog = new Map(products.map(p => [p.id, p]))
  let total = 0
  let problem = lines.length ? '' : 'Agrega al menos un producto.'
  for (const line of lines) {
    const p = catalog.get(line.producto_id)
    if (!p) { problem = 'Retira del carrito el producto que ya no está disponible.'; continue }
    const available = p.existencias.reduce((n, row) => n + row.cantidad, 0)
    if (!Number.isInteger(line.cantidad) || line.cantidad < 1 || line.cantidad > available) {
      problem = 'Revisa la cantidad de ' + p.nombre + '. Disponibles: ' + available
    }
    total += Math.round(p.precio * 100) * line.cantidad
  }
  const received = cashCents(cash)
  if (!problem && (received === null || received < total)) problem = 'Ingresa efectivo suficiente para cubrir el total.'
  return {total, received, change: received === null ? 0 : Math.max(0, received - total), problem}
}

export function saleRequest(id: string, lines: CartLine[], received: number) {
  return {p_operacion: id, p_canal: 'Ventas Presenciales', p_cliente: 'Cliente General', p_direccion: '',
    p_monto_recibido: received / 100, p_lineas: lines.map(line => ({...line}))}
}
