import { useRef, useState } from 'react'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import { checkout, saleRequest, type CartLine } from './lib/checkout'
import { stock, useProducts } from './lib/inventory'
import { supabase } from './lib/supabase'
import './sale.css'

type Pending = ReturnType<typeof saleRequest>
const money = (amount: number) => '$' + amount.toLocaleString('es-CL', { maximumFractionDigits: 2 })

function Receipt({ id, userId }: { id: string; userId: string }) {
  const sale = useQuery({queryKey: ['sale-receipt', userId, id], queryFn: async () => {
    const {data, error} = await supabase.from('ventas').select('codigo_orden,total,monto_recibido,cambio').eq('id', id).single()
    if (error) throw error
    return data
  }})
  return <section className="sale-receipt" role="status"><strong>Venta confirmada · {sale.data?.codigo_orden ?? 'FG-' + id.slice(0,8).toUpperCase()}</strong>
    {sale.data && <p>Total {money(sale.data.total)} · Recibido {money(sale.data.monto_recibido)} · Cambio {money(sale.data.cambio)}</p>}
    {sale.isError && <button onClick={() => sale.refetch()}>Volver a consultar el cambio confirmado</button>}
  </section>
}

export function NewSale({ userId }: { userId: string }) {
  const key = 'fashgo:venta-presencial:' + userId
  function readPending(): Pending | null {
    const saved = localStorage.getItem(key)
    return saved ? JSON.parse(saved) : null
  }
  const [pending, setPending] = useState<Pending | null>(() => {
    try { return readPending() } catch { return null }
  })
  const [draft, setDraft] = useState<CartLine[]>([])
  const [cash, setCash] = useState('')
  const [search, setSearch] = useState('')
  const [limit, setLimit] = useState(20)
  const [error, setError] = useState('')
  const [receipt, setReceipt] = useState('')
  const [busy, setBusy] = useState(false)
  const running = useRef(false)
  const catalog = useProducts(userId)
  const cache = useQueryClient()
  const products = catalog.data ?? []
  const lines = pending?.p_lineas ?? draft
  const amount = pending ? String(pending.p_monto_recibido) : cash
  const bill = checkout(lines, products, amount)
  const matches = products.filter(p => (p.nombre + ' ' + p.codigo).toLowerCase().includes(search.trim().toLowerCase()))
  function quantity(id: string, value: number) {
    setDraft(current => [...current.filter(l => l.producto_id !== id), ...(value ? [{producto_id:id, cantidad:value}] : [])])
    setReceipt('')
    setError('')
  }
  async function submit() {
    if (running.current || (!pending && bill.problem)) return
    running.current = true
    setBusy(true)
    setError('')
    try {
      await navigator.locks.request(key, async () => {
        const saved = readPending()
        if (saved && saved.p_operacion !== pending?.p_operacion) {
          setPending(saved)
          throw new Error('Hay una venta pendiente. Revisa el carrito antes de reintentar.')
        }
        if (pending && !saved) {
          setDraft([]); setCash(''); setPending(null)
          throw new Error('La operación se resolvió en otra pestaña. Consulta el historial antes de repetir el cobro.')
        }
        const request = saved ?? saleRequest(crypto.randomUUID(), draft, bill.received!)
        localStorage.setItem(key, JSON.stringify(request))
        setPending(request)
        const result = await supabase.rpc('registrar_venta_completa', request)
        if (result.error) {
          if (result.error.code === 'P0001' && /Stock insuficiente|Producto no disponible|Monto recibido insuficiente/i.test(result.error.message)) {
            localStorage.removeItem(key)
            setDraft(request.p_lineas); setCash(String(request.p_monto_recibido)); setPending(null)
            void catalog.refetch()
          }
          throw new Error(result.error.message)
        }
        if (result.data !== request.p_operacion) throw new Error('Respuesta de cobro sin confirmar.')
        localStorage.removeItem(key)
        setReceipt(request.p_operacion); setPending(null); setDraft([]); setCash('')
        await Promise.all(['products','today-sales','abc-sales','audit','sales-history'].map(name => cache.invalidateQueries({queryKey:[name,userId]})))
      })
    } catch (e) { setError(e instanceof Error ? e.message : 'Error al confirmar la venta.') }
    finally { running.current = false; setBusy(false) }
  }
  return <><h1 className="page-title">Nueva venta</h1>
    {receipt && <Receipt id={receipt} userId={userId}/>}
    <div className="sale-layout"><section className="panel"><div className="panel-head"><h2>Productos</h2><a href="#sale-cart">Carrito ({lines.reduce((n,l) => n+l.cantidad,0)})</a></div>
      <input className="search" aria-label="Buscar por nombre o SKU" placeholder="Buscar por nombre o SKU" value={search} onChange={e => {setSearch(e.target.value); setLimit(20)}}/>
      {catalog.isLoading && <p>Cargando catálogo…</p>}
      {catalog.isError && <button onClick={() => catalog.refetch()}>Error de catálogo · Reintentar</button>}
      {catalog.isSuccess && !matches.length && <p>Sin productos para esta búsqueda.</p>}
      {matches.slice(0,limit).map(p => {
        const count = lines.find(l => l.producto_id === p.id)?.cantidad ?? 0
        const available = stock(p)
        return <div className="sale-row" key={p.id}><div><b>{p.nombre}</b><small>{p.codigo} · {available} uds.</small></div><span>{money(p.precio)}</span>
          <button aria-label={'Agregar ' + p.nombre} disabled={busy || !!pending || count >= available} onClick={() => quantity(p.id,count+1)}>Agregar</button></div>
      })}
      {matches.length > limit && <button onClick={() => setLimit(limit+20)}>Ver más productos</button>}
    </section><aside className="panel sale-cart" id="sale-cart"><div className="panel-head"><h2>Carrito</h2><span className="eyebrow">Presencial</span></div>
      {!lines.length && <p>Elige los productos de la venta.</p>}
      {lines.map(l => {
        const p = products.find(p => p.id === l.producto_id)
        return <div className="cart-line" key={l.producto_id}><b>{p?.nombre ?? l.producto_id}</b>
          <small>{p && money(p.precio) + ' × '}{l.cantidad}</small>
          {!pending && <div className="cart-controls"><label>Cantidad <input type="number" min="1" step="1" value={l.cantidad} disabled={busy} onChange={e => {
            const n = e.target.valueAsNumber
            if (Number.isSafeInteger(n) && n > 0) quantity(l.producto_id,n)
          }}/></label><button disabled={busy} onClick={() => quantity(l.producto_id,0)}>Quitar</button></div>}
        </div>
      })}
      {!pending && <div className="product-total">Total <b>{money(bill.total/100)}</b></div>}
      <label className="field">Monto recibido<input inputMode="decimal" disabled={!!pending || busy} value={amount} onChange={e => setCash(e.target.value)}/></label>
      {!pending && <p>Cambio <b>{money(bill.change/100)}</b></p>}
      {pending && <p role="status">Cobro pendiente de confirmación. El reintento conserva la operación original.</p>}
      {error && <p role="alert" className="error">{error}</p>}
      {!pending && lines.length > 0 && bill.problem && <p>{bill.problem}</p>}
      <button className="action" disabled={busy || (!pending && !!bill.problem)} onClick={submit}>{busy ? 'Confirmando…' : pending ? 'Reintentar confirmación' : 'Confirmar venta'}</button>
    </aside></div></>
}
