import { useEffect, useRef, useState } from 'react'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import { supabase } from './lib/supabase'
import { remainingUnits, validReturn, type ReturnRequest } from './lib/returns'
import type { Profile } from './types'
import './history.css'

const pageSize = 25
const fields = 'id,codigo_orden,fecha,canal,total,estado,cliente,direccion,monto_recibido,cambio,estado_envio'
const currency = (value: number) => '$' + value.toLocaleString('es-CL', {maximumFractionDigits: 2})
const dateTime = (value: string) => new Date(value).toLocaleString('es-CO', {dateStyle: 'short', timeStyle: 'short'})
type Sale = {id: string; codigo_orden: string; fecha: string; canal: string; total: number; estado: string; cliente: string; direccion: string; monto_recibido: number | null; cambio: number | null; estado_envio: string}
type Line = {id: string; cantidad: number; precio_unitario: number; nombre_producto: string | null; codigo_producto: string | null; productos: {nombre: string; codigo: string} | null; ubicaciones: {codigo: string} | null; detalle_devolucion: {cantidad: number; devoluciones: {fecha: string; motivo: string}}[]}
const order = (sale: Sale) => sale.codigo_orden || sale.id.slice(0,8).toUpperCase()

function SaleDetail({sale, profile, close}: {sale: Sale; profile: Profile; close: () => void}) {
  const dialog = useRef<HTMLDialogElement>(null)
  const running = useRef(false)
  const [busy, setBusy] = useState(false)
  const [lineId, setLineId] = useState('')
  const [quantity, setQuantity] = useState('1')
  const [reason, setReason] = useState('')
  const [notice, setNotice] = useState('')
  const key = 'fashgo:devolucion:' + profile.id + ':' + sale.id
  const [pending, setPending] = useState<ReturnRequest | null>(() => JSON.parse(localStorage.getItem(key) || 'null'))
  const cache = useQueryClient()
  const detail = useQuery({queryKey: ['sale-lines',profile.id,sale.id], queryFn: async () => {
    const {data,error} = await supabase.from('detalle_venta')
      .select('id,cantidad,precio_unitario,nombre_producto,codigo_producto,productos(nombre,codigo),ubicaciones(codigo),detalle_devolucion(cantidad,devoluciones(fecha,motivo))')
      .eq('venta_id',sale.id).order('id').returns<Line[]>()
    if (error) throw error
    return data
  }})
  useEffect(() => {
    const panel = dialog.current!
    panel.showModal()
    return () => panel.close()
  }, [])
  async function submit(request: ReturnRequest) {
    if (running.current) return
    running.current = true
    setBusy(true)
    setNotice('')
    try {
      await navigator.locks.request(key, async () => {
        const saved: ReturnRequest | null = JSON.parse(localStorage.getItem(key) || 'null')
        if (saved && saved.p_operacion !== request.p_operacion) {
          setPending(saved)
          throw new Error('Hay una devolución anterior pendiente de confirmar.')
        }
        localStorage.setItem(key, JSON.stringify(request))
        setPending(request)
        const {data,error} = await supabase.rpc('registrar_devolucion',request)
        if (error) {
          if (error.code === 'P0001' || error.code === '42501') {
            localStorage.removeItem(key)
            setPending(null)
          }
          throw new Error(error.message)
        }
        if (data !== request.p_operacion) throw new Error('Respuesta de devolución sin confirmar.')
        localStorage.removeItem(key)
        setPending(null)
        setLineId('')
        setNotice('Devolución registrada.')
      })
    } catch (error) {
      setNotice(error instanceof Error ? error.message : 'No se pudo confirmar la devolución.')
    } finally {
      await Promise.all(['sale-lines','products','audit','sales-history'].map(name => cache.invalidateQueries({queryKey: [name,profile.id]})))
      running.current = false
      setBusy(false)
    }
  }
  return <dialog className="inventory-panel history-detail" ref={dialog} aria-labelledby="receipt-title" onCancel={e => {e.preventDefault(); if (!busy) close()}}>
    <header><span className="eyebrow">Detalle de venta</span><button disabled={busy} onClick={close}>Cerrar</button></header>
    <h2 id="receipt-title">{order(sale)}</h2>
    <p>{dateTime(sale.fecha)} · {sale.estado}</p>
    <dl><div><dt>Cliente</dt><dd>{sale.cliente}</dd></div><div><dt>Canal</dt><dd>{sale.canal}</dd></div>
      {sale.direccion && <div><dt>Entrega · {sale.estado_envio}</dt><dd>{sale.direccion}</dd></div>}
      {sale.monto_recibido !== null && <div><dt>Recibido</dt><dd>{currency(sale.monto_recibido)}</dd></div>}
      {sale.cambio !== null && <div><dt>Cambio</dt><dd>{currency(sale.cambio)}</dd></div>}</dl>
    <div className="product-total"><span>Total de venta</span><b>{currency(sale.total)}</b></div>
    {detail.isPending && <p>Cargando artículos…</p>}
    {detail.isError && <button onClick={() => detail.refetch()}>Reintentar consulta de artículos</button>}
    {notice && <p role="status">{notice}</p>}
    {pending && <section><h3>Devolución pendiente</h3><p>{pending.p_cantidad} uds. · {pending.p_motivo}</p>
      <button disabled={busy} onClick={() => submit(pending)}>Confirmar operación pendiente</button></section>}
    {detail.data?.map(line => {
      const available = remainingUnits(line.cantidad,line.detalle_devolucion)
      return <article key={line.id}><h3>{line.nombre_producto || line.productos?.nombre || 'Producto'}</h3>
        <small className="sku">{line.codigo_producto || line.productos?.codigo} · {line.ubicaciones?.codigo}</small>
        <p>{line.cantidad} × {currency(line.precio_unitario)} · <b>{currency(line.cantidad * line.precio_unitario)}</b></p>
        {line.detalle_devolucion.map((r,i) => <p key={i}>Devuelto · {r.cantidad} uds. · {dateTime(r.devoluciones.fecha)} · {r.devoluciones.motivo}</p>)}
        {sale.estado === 'confirmada' && available > 0 && !pending && (lineId !== line.id
          ? <button disabled={busy} onClick={() => {setLineId(line.id); setQuantity('1'); setReason('')}}>Devolver artículo</button>
          : <form onSubmit={e => {e.preventDefault(); if (validReturn(quantity,available,reason)) void submit({p_operacion:crypto.randomUUID(),p_venta:sale.id,p_detalle:line.id,p_cantidad:Number(quantity),p_motivo:reason.trim()})}}>
            <label>Cantidad · {available} disponibles<input type="number" min="1" max={available} step="1" required value={quantity} onChange={e => setQuantity(e.target.value)}/></label>
            <label>Motivo<input required value={reason} onChange={e => setReason(e.target.value)}/></label>
            <div className="panel-actions"><button className="action" disabled={busy || !validReturn(quantity,available,reason)}>Confirmar devolución</button>
            <button type="button" onClick={() => setLineId('')}>Cancelar</button></div>
          </form>)}
      </article>
    })}
  </dialog>
}

export function SalesHistory({profile}: {profile: Profile}) {
  const [page,setPage] = useState(0)
  const [selected,setSelected] = useState('')
  const history = useQuery({queryKey: ['sales-history',profile.id,profile.rol,page], queryFn: async () => {
    const {data,error,count} = await supabase.from('ventas').select(fields,{count:'exact'})
      .order('fecha',{ascending:false}).order('id').range(page * pageSize,(page+1)*pageSize-1)
    if (error) throw error
    return {sales:data, count:count ?? 0}
  }})
  const sale = history.data?.sales.find(row => row.id === selected)
  return <section className="sales-history"><h1 className="page-title">Historial de ventas</h1>
    <div className="panel"><div className="panel-head"><h2>{profile.rol === 'dueno' ? 'Todas las ventas' : 'Mis ventas'}</h2>{history.data && <span className="badge">{history.data.count}</span>}</div>
      {history.isPending && <p>Cargando ventas…</p>}
      {history.isError && <button onClick={() => history.refetch()}>Reintentar consulta de ventas</button>}
      {history.data?.count === 0 && <p>Sin ventas registradas.</p>}
      {history.data?.sales.map(row => <button className="history-row" key={row.id} onClick={() => setSelected(row.id)}>
        <span><b>{order(row)}</b><small>{dateTime(row.fecha)}</small></span><span>{row.cliente}<small>{row.canal}</small></span><span>{row.estado}</span><strong>{currency(row.total)}</strong></button>)}
      {history.data && history.data.count > pageSize && <nav aria-label="Páginas de ventas" className="panel-actions">
        <button disabled={!page} onClick={() => setPage(page-1)}>Anterior</button><span>Página {page+1} de {Math.ceil(history.data.count/pageSize)}</span>
        <button disabled={(page+1)*pageSize >= history.data.count} onClick={() => setPage(page+1)}>Siguiente</button></nav>}
    </div>{sale && <SaleDetail key={sale.id} sale={sale} profile={profile} close={() => setSelected('')}/>}</section>
}
