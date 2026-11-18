export type WorkshopKind = 'materia_prima' | 'productos_derivados'
export type WorkshopRow = {id: string; cantidad: number} & Record<string, string | number | null>
type Field = {key: string; label: string; required?: boolean; step?: number; min?: number; max?: number; initial?: string}

export const workshopFields: Record<WorkshopKind, Field[]> = {
  materia_prima: [
    {key:'nombre', label:'Nombre', required:true},
    {key:'cantidad', label:'Cantidad disponible', required:true, step:0.01, min:0},
    {key:'unidad_medida', label:'Unidad de medida', required:true},
    {key:'proveedor', label:'Proveedor'},
    {key:'descripcion', label:'Descripción'},
  ],
  productos_derivados: [
    {key:'tipo_prenda', label:'Tipo de prenda', required:true},
    {key:'subcategoria', label:'Subcategoría', required:true},
    {key:'color', label:'Color', required:true},
    {key:'talla', label:'Talla', initial:'Estandar'},
    {key:'cantidad', label:'Cantidad', required:true, step:1, min:1, max:2147483647},
    {key:'precio_unitario', label:'Precio unitario', required:true, step:0.01, min:0.01},
    {key:'ubicacion', label:'Ubicación', initial:'Zona Auxiliar'},
    {key:'nivel', label:'Nivel'},
    {key:'zona_auxiliar', label:'Zona auxiliar', step:1, min:1, max:2},
    {key:'origen', label:'Origen'},
    {key:'notas', label:'Notas'},
  ],
}

export function workshopPayload(kind: WorkshopKind, form: FormData, row?: WorkshopRow) {
  const data: Record<string, string | number | null> = {}
  for (const field of workshopFields[kind]) {
    const raw = String(form.get(field.key) ?? '').trim()
    if (!raw && field.required) throw new Error('Completa el campo «' + field.label + '».')
    if (!field.step || !raw) {
      data[field.key] = raw || field.initial || (field.step ? null : '')
      continue
    }
    const value = Number(raw)
    if (!Number.isFinite(value) || value < (field.min ?? 0) || value > (field.max ?? 9999999999.99)
      || (field.step === 1 ? !Number.isInteger(value) : Number(value.toFixed(2)) !== value)) {
      throw new Error('Revisa el campo «' + field.label + '».')
    }
    data[field.key] = value
  }
  if (kind === 'productos_derivados') {
    data.rotacion = row?.rotacion ?? 'Baja'
    data.decision_sugerida = row?.decision_sugerida ?? 'Derivado'
  }
  return data
}
export type Consumption = {operation:string; quantity:number}

export async function recordConsumption(options: {
  key: string
  available: number
  quantity: number
  pending: Consumption | null
  storage: Pick<Storage, 'getItem' | 'setItem' | 'removeItem'>
  onPending: (request: Consumption | null) => void
  send: (request: Consumption) => PromiseLike<{error: {code:string; message:string} | null}>
}) {
  const {key, available, quantity, pending, storage, onPending, send} = options
  const saved = storage.getItem(key)
  const retry: Consumption | null = saved ? JSON.parse(saved) : null
  if (retry?.operation !== pending?.operation) {
    throw new Error('El consumo cambió en otra pestaña. Cierra y vuelve a abrir el detalle.')
  }
  if (!retry && (!Number.isFinite(quantity) || quantity <= 0 || quantity > available || Number(quantity.toFixed(2)) !== quantity)) {
    throw new Error('Verifica la cantidad disponible y usa hasta dos decimales.')
  }
  const request = retry ?? {quantity, operation:crypto.randomUUID()}
  storage.setItem(key, JSON.stringify(request))
  onPending(request)
  const {error} = await send(request)
  if (error && error.code !== 'P0001') throw new Error(error.message)
  storage.removeItem(key)
  onPending(null)
  if (error) throw new Error(error.message)
}
