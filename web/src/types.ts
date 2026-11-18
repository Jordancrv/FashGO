export type Profile = { id: string; nombre: string; apellido: string; correo: string; rol: 'dueno' | 'vendedor' }
export type Product = {
  id: string
  codigo: string
  nombre: string
  parte_general: string
  tipo: string
  diseno: string
  tipo_manga: string
  tipo_cuello: string
  color: string
  talla: string
  precio: number
  stock_minimo: number
  stock_referencia: number
  existencias: { cantidad: number; ubicacion_id: string; ubicaciones: { codigo: string } | null }[]
}
