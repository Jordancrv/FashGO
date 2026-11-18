import type { SupabaseClient } from '@supabase/supabase-js'

export type Cursor = { fecha: string; id: string }
type Movement = Cursor & {
  entidad: string
  accion: string
  detalle: string
  usuarios: { nombre: string; apellido: string; correo: string } | null
}

export async function fetchMovements(client: SupabaseClient, entity: string, cursor: Cursor | undefined, limit: number) {
  let query = client.from('auditoria_movimientos')
    .select('id,fecha,entidad,accion,detalle,usuarios(nombre,apellido,correo)')
    .order('fecha', { ascending: false }).order('id', { ascending: false }).limit(limit)
  if (entity) query = query.eq('entidad', entity)
  if (cursor) query = query.or(`fecha.lt.${cursor.fecha},and(fecha.eq.${cursor.fecha},id.lt.${cursor.id})`)
  const { data, error } = await query.returns<Movement[]>()
  if (error) throw error
  return data
}
