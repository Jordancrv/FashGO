export type ReturnRequest = {
  p_operacion: string
  p_venta: string
  p_detalle: string
  p_cantidad: number
  p_motivo: string
}

export function remainingUnits(sold: number, returns: { cantidad: number }[]) {
  return Math.max(0, sold - returns.reduce((sum, item) => sum + item.cantidad, 0))
}

export function validReturn(quantity: string, available: number, reason: string) {
  return /^\d+$/.test(quantity) && Number(quantity) > 0 && Number(quantity) <= available && !!reason.trim()
}
