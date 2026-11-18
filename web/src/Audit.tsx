import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router'
import { supabase } from './lib/supabase'
import './audit.css'

import { fetchMovements, type Cursor } from './lib/audit'

const entities: Record<string, string> = {
  producto: 'Producto', inventario: 'Inventario', materia_prima: 'Materia prima', producto_derivado: 'Producto derivado',
}
const actions: Record<string, string> = {
  creacion: 'Creación', actualizacion: 'Actualización', saldo_inicial: 'Saldo inicial', anulacion: 'Anulación', devolucion: 'Devolución',
}
const dateFormat = new Intl.DateTimeFormat('es-CO', { dateStyle: 'short', timeStyle: 'short' })
const pageSize = 25

function loadMovements(userId: string, entity: string, cursor: Cursor | undefined, limit: number) {
  return {
    queryKey: ['audit', userId, entity, cursor?.fecha, cursor?.id, limit],
    staleTime: 0,
    queryFn: () => fetchMovements(supabase, entity, cursor, limit),
  }
}

export function RecentMovements({ userId }: { userId: string }) {
  const query = useQuery(loadMovements(userId, '', undefined, 6))
  return <section className="panel">
    <div className="panel-head"><h2>Actividad reciente</h2><Link to="/movimientos">Ver movimientos</Link></div>
    {query.isPending && <p>Cargando actividad…</p>}
    {query.isError && <button onClick={() => query.refetch()}>Reintentar consulta de actividad</button>}
    {query.data?.length === 0 && <p>Todavía no hay movimientos registrados.</p>}
    <ul className="activity">{query.data?.map(row => <li key={row.id}><time dateTime={row.fecha}>{dateFormat.format(new Date(row.fecha))}</time><span>{row.detalle}</span></li>)}</ul>
  </section>
}

export function Audit({ userId }: { userId: string }) {
  const [entity, setEntity] = useState('')
  const [history, setHistory] = useState<Cursor[]>([])
  const query = useQuery(loadMovements(userId, entity, history.at(-1), pageSize + 1))
  const rows = query.data?.slice(0, pageSize) ?? []
  function nextPage() {
    const { fecha, id } = rows[rows.length - 1]
    setHistory([...history, { fecha, id }])
  }
  return <main className="audit"><h1 className="page-title">Movimientos</h1>
    <div className="audit-tools"><label>Entidad <select value={entity} onChange={e => { setEntity(e.target.value); setHistory([]) }}>
      <option value="">Todas las entidades</option>
      {Object.entries(entities).map(([key, label]) => <option key={key} value={key}>{label}</option>)}
    </select></label><button disabled={query.isFetching} onClick={() => { if (history.length) setHistory([]); else void query.refetch() }}>Actualizar</button></div>
    {query.isPending && <p role="status">Cargando movimientos…</p>}
    {query.isError && <p role="alert">No se pudo consultar la bitácora. <button onClick={() => query.refetch()}>Reintentar</button></p>}
    {query.isSuccess && !rows.length && <p>No hay movimientos para esta consulta.</p>}
    <ol className="audit-list">{rows.map(row => {
      const user = row.usuarios
      const name = user ? [user.nombre, user.apellido].filter(Boolean).join(' ') || user.correo : 'Sistema'
      return <li key={row.id}>
        <time dateTime={row.fecha}>{dateFormat.format(new Date(row.fecha))}</time>
        <div><span className="eyebrow">{entities[row.entidad] ?? row.entidad} · {actions[row.accion] ?? row.accion}</span><p>{row.detalle}</p></div>
        <span className="audit-user">{name}</span>
      </li>
    })}</ol>
    <nav aria-label="Páginas de movimientos" className="audit-pages"><button disabled={!history.length} onClick={() => setHistory(history.slice(0,-1))}>Anterior</button>
      <span>Página {history.length + 1}</span><button disabled={(query.data?.length ?? 0) <= pageSize} onClick={nextPage}>Siguiente</button></nav>
  </main>
}
