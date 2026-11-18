export function classify(products: readonly {id: string}[], lines: readonly {producto_id: string; cantidad: number}[]) {
  const units = new Map<string, number>()
  for (const line of lines) {
    units.set(line.producto_id, (units.get(line.producto_id) ?? 0) + line.cantidad)
  }
  const ranked = products.filter(p => units.has(p.id))
    .sort((a, b) => units.get(b.id)! - units.get(a.id)!)
  const classes = new Map(products.map(p => [p.id, 'C']))
  ranked.forEach((p, i) => classes.set(p.id, i < Math.ceil(ranked.length / 3) ? 'A' : 'B'))
  return classes
}
