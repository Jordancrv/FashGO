import type { Product } from '../types'

export const stock = (product: Product) => product.existencias.reduce((sum, row) => sum + row.cantidad, 0)

export function lowStockProducts(products: Product[]) {
  return products.filter(product => stock(product) <= product.stock_minimo)
}

export function inactiveSalesProducts(products: Product[], confirmedLines: {producto_id: string}[]) {
  const sold = new Set(confirmedLines.map(line => line.producto_id))
  return products.filter(product => !sold.has(product.id))
}
