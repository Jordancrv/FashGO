import { useQuery } from '@tanstack/react-query'
import { supabase } from './supabase'
import type { Product } from '../types'

export async function pages<T>(load: (from: number, to: number) => PromiseLike<{
  data: T[] | null; error: {message: string} | null
}>) {
  const rows: T[] = []
  for (;;) {
    const {data, error} = await load(rows.length, rows.length + 499)
    if (error) throw new Error(error.message)
    if (!data?.length) return rows
    rows.push(...data)
    if (data.length < 500) return rows
  }
}

export function useProducts(userId: string) {
  return useQuery({
    queryKey: ['products', userId],
    queryFn: async () => await pages((from, to) => supabase.from('productos')
      .select('id,codigo,nombre,parte_general,tipo,diseno,tipo_manga,tipo_cuello,color,talla,precio,stock_minimo,stock_referencia,existencias(cantidad,ubicacion_id,ubicaciones(codigo))')
      .eq('activo', true).order('nombre').order('id').range(from, to)) as unknown as Product[]
  })
}

export { stock } from './alerts'

export function useSalesLines(userId: string, days: 15 | 30 = 15) {
  return useQuery({
    queryKey: ['abc-sales', userId, days],
    queryFn: () => {
      const since = new Date(Date.now() - days * 86400000).toISOString()
      return pages((from, to) => supabase.from('detalle_venta')
        .select('producto_id,cantidad,ventas!inner(fecha,estado)').eq('ventas.estado', 'confirmada')
        .gte('ventas.fecha', since).order('id').range(from, to))
    }
  })
}
