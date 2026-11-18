import { useEffect, useMemo, useRef, useState } from 'react'
import type { FormEvent } from 'react'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import { productIdentity } from './lib/productIdentity'
import { classify } from './lib/abc'
import { pages, stock, useProducts, useSalesLines } from './lib/inventory'
import { supabase } from './lib/supabase'
import type { Product, Profile } from './types'
import './inventory.css'

const fields = [
  ['nombre', 'Nombre'], ['codigo', 'SKU'], ['parte_general', 'Parte general'], ['tipo', 'Tipo'],
  ['diseno', 'Diseño'], ['tipo_manga', 'Manga'], ['tipo_cuello', 'Cuello'], ['color', 'Color'], ['talla', 'Talla'],
] as const

type Mode = 'detail' | 'create' | 'edit' | 'receive' | 'deactivate'
type Selection = { mode: Mode; id?: string }
const price = (p: Product) => '$' + Number(p.precio).toLocaleString('es-CL')

export function Inventory({profile}: {profile: Profile}) {
  const products = useProducts(profile.id)
  const sales = useSalesLines(profile.id)
  const classes = useMemo(() => sales.data && classify(products.data ?? [], sales.data), [products.data, sales.data])
  const [selection, select] = useState<Selection | null>(null)
  const [query, search] = useState('')
  const [notice, setNotice] = useState('')
  const terms = query.toLocaleLowerCase().trim().split(/\s+/)
  const visible = products.data?.filter(p => {
    const text = [p.nombre, p.codigo, p.tipo, p.color, p.talla, ...p.existencias.map(e => e.ubicaciones?.codigo)].join(' ').toLocaleLowerCase()
    return terms.every(term => text.includes(term))
  })
  const selected = products.data?.find(p => p.id === selection?.id)
  const owner = profile.rol === 'dueno'
  return <>
    <h1 className="page-title">Productos<br/>& inventario</h1>
    <div className="toolbar">
      <input className="search" type="search" aria-label="Buscar inventario" placeholder="Nombre, SKU, variante o ubicación…" value={query} onChange={e => search(e.target.value)}/>
      {owner && <button className="action" onClick={() => select({mode:'create'})}>Nuevo producto</button>}
    </div>
    <p role="status">{notice}</p>
    {sales.isError && <p role="alert">ABC no disponible. {sales.error.message} <button onClick={() => sales.refetch()}>Reintentar</button></p>}
    {products.isError && <p role="alert">{products.error.message} <button onClick={() => products.refetch()}>Reintentar</button></p>}
    {products.isLoading && <p>Cargando inventario…</p>}
    {visible && <div className="table-wrap inventory-table"><table>
      <thead><tr>{['Producto','Variante','Ubicación','ABC · 15 días','Stock','Precio'].map(h => <th key={h}>{h}</th>)}</tr></thead>
      <tbody>{visible.map(p => <tr key={p.id}>
        <td><button className="product-link" onClick={() => select({mode:'detail', id:p.id})}>{p.nombre}<small className="sku">{p.codigo}</small></button></td>
        <td>{p.color} · {p.talla}</td><td>{p.existencias.map(e => e.ubicaciones?.codigo).join(' / ') || '—'}</td>
        <td><span className="abc">{classes?.get(p.id) ?? '—'}</span></td>
        <td className={stock(p) <= p.stock_minimo ? 'stock-low' : ''}>{stock(p)} uds.</td><td>{price(p)}</td>
      </tr>)}</tbody></table>{!visible.length && <p>Sin productos para mostrar.</p>}</div>}
    {selection && (selected || selection.mode === 'create') && <ProductPanel key={selection.id ?? 'create'} product={selected}
      abc={selected && classes?.get(selected.id)} profile={profile} initial={selection.mode} close={() => select(null)} notify={setNotice}/>}
  </>
}

export function ProductPanel({product, abc, profile, initial, close, notify}: {
  product?: Product; abc?: string; profile: Profile; initial: Mode; close: () => void; notify: (msg: string) => void
}) {
  const dialog = useRef<HTMLDialogElement>(null)
  const [mode, setMode] = useState(initial)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const [productId] = useState(() => product?.id ?? crypto.randomUUID())
  const lock = useRef(false)
  const cache = useQueryClient()
  const owner = profile.rol === 'dueno'
  const locations = useQuery({
    queryKey: ['locations', profile.id], enabled: owner && (mode === 'create' || mode === 'receive'),
    queryFn: () => pages((from, to) => supabase.from('ubicaciones').select('id,codigo').order('codigo').range(from,to))
  })
  const history = useQuery({
    queryKey: ['receipts', profile.id, productId], enabled: owner && !!product && mode === 'detail',
    queryFn: async () => {
      const {data, error} = await supabase.from('movimientos_inventario').select('id,cantidad,fecha,ubicaciones(codigo)')
        .eq('producto_id',productId).eq('tipo','entrada').order('fecha',{ascending:false}).limit(20)
      if (error) throw new Error(error.message)
      return data as unknown as {id:string; cantidad:number; fecha:string; ubicaciones:{codigo:string}|null}[]
    }
  })
  useEffect(() => {
    const el = dialog.current!
    el.showModal()
    return () => el.close()
  }, [])
  function change(next: Mode) { setError(''); setMode(next) }
  async function submit(e: FormEvent<HTMLFormElement>) {
    e.preventDefault()
    if (!owner || lock.current) return
    const values = Object.fromEntries(Array.from(new FormData(e.currentTarget), ([k,v]) => [k, String(v).trim()]))
    lock.current = true
    setBusy(true)
    setError('')
    try {
      if (mode === 'receive') {
        const qty = Number(values.cantidad)
        if (!Number.isSafeInteger(qty) || qty < 1 || !values.ubicacion) throw new Error('Revisa la cantidad y la ubicación.')
        const key = ['fashgo.ingreso', profile.id, productId, values.ubicacion, qty].join('/')
        const id = localStorage.getItem(key) ?? crypto.randomUUID()
        localStorage.setItem(key, id)
        const {error} = await supabase.rpc('registrar_ingreso', {p_operacion:id, p_producto:productId, p_ubicacion:values.ubicacion, p_cantidad:qty})
        if (error) throw error
        localStorage.removeItem(key)
      } else if (mode === 'deactivate') {
        const {error} = await supabase.rpc('desactivar_producto', {p_id:productId})
        if (error) throw error
      } else {
        if (!product) {
          const suggested = productIdentity(values, cache.getQueryData<Product[]>(['products', profile.id]) ?? [])
          values.nombre ||= suggested.nombre
          values.codigo ||= suggested.codigo
        }
        const {error} = await supabase.rpc('guardar_producto', {p_id:productId, p_datos:values})
        if (error) throw error
      }
      notify(mode === 'receive' ? 'Ingreso registrado.' : mode === 'deactivate' ? 'Producto desactivado.' : 'Producto guardado.')
      await Promise.all(['products','locations','receipts','audit'].map(key => cache.invalidateQueries({queryKey:[key]})))
      close()
    } catch (err) {
      setError(err && typeof err === 'object' && 'message' in err ? String(err.message) : 'No se pudo guardar. Intenta nuevamente.')
    } finally { lock.current = false; setBusy(false) }
  }
  const titles = {detail:product?.nombre, edit:'Editar producto', create:'Nuevo producto', deactivate:'Desactivar producto', receive:'Registrar ingreso'}
  return <dialog ref={dialog} className="inventory-panel" aria-labelledby="inventory-heading" onCancel={e => {e.preventDefault(); if (!busy) close()}}>
    <header><span className="eyebrow">{product?.codigo ?? 'Inventario'}</span><button disabled={busy} onClick={close}>Cerrar</button></header>
    <h2 id="inventory-heading">{titles[mode]}</h2>
    {mode === 'detail' && product ? <>
      <div className="product-total"><strong>{stock(product)} uds.</strong><span>{price(product)}</span></div>
      <dl>{fields.slice(2).map(([k,label]) => <div key={k}><dt>{label}</dt><dd>{product[k] || '—'}</dd></div>)}
        <div><dt>Stock mínimo</dt><dd>{product.stock_minimo}</dd></div><div><dt>Stock de referencia</dt><dd>{product.stock_referencia}</dd></div>
        <div><dt>ABC · 15 días</dt><dd>{abc ?? '—'}</dd></div></dl>
      <h3>Stock por ubicación</h3>{product.existencias.map(row => <p className="alert-row" key={row.ubicacion_id}>{row.ubicaciones?.codigo}<b>{row.cantidad} uds.</b></p>)}
      {owner && <div className="panel-actions"><button className="action" onClick={() => change('receive')}>Registrar ingreso</button><button onClick={() => change('edit')}>Editar</button><button onClick={() => change('deactivate')}>Desactivar</button></div>}
      {owner && <h3>Últimos ingresos</h3>}{history.isLoading && <p>Cargando…</p>}{history.isError && <p role="alert">{history.error.message}</p>}
      {history.data?.length === 0 && <p>Sin ingresos registrados.</p>}
      {history.data?.map(r => <p key={r.id} className="alert-row">{new Date(r.fecha).toLocaleDateString('es-CL')} · {r.ubicaciones?.codigo}<b>+{r.cantidad}</b></p>)}
    </> : owner && <form onSubmit={submit}><fieldset disabled={busy}>
      {mode === 'deactivate' ? <p>¿Desactivar {product?.nombre} ({product?.codigo})? Saldrá del catálogo activo.</p> :
        mode === 'receive' ? <>
          <label>Ubicación<select name="ubicacion" required defaultValue={product?.existencias[0]?.ubicacion_id ?? ''}>
            <option value="">Seleccionar ubicación</option>{locations.data?.map(l => <option key={l.id} value={l.id}>{l.codigo}</option>)}</select></label>
          <label>Unidades recibidas<input name="cantidad" type="number" min="1" step="1" required/></label>
        </> : <div className="product-fields">
          {fields.map(([k,label]) => <label key={k}>{label}<input name={k} defaultValue={product?.[k]} readOnly={k === 'codigo' && !!product}
            placeholder={!product && (k === 'nombre' || k === 'codigo') ? 'Generar automáticamente' : undefined}
            required={!['diseno','tipo_manga','tipo_cuello'].includes(k) && (!!product || (k !== 'nombre' && k !== 'codigo'))}/></label>)}
          <label>Precio<input name="precio" type="number" min="0.01" step="0.01" required defaultValue={product?.precio}/></label>
          <label>Stock mínimo<input name="stock_minimo" type="number" min="0" required defaultValue={product?.stock_minimo ?? 3}/></label>
          {!product && <><label>Stock inicial<input name="stock_inicial" type="number" min="0" required defaultValue="0"/></label>
            <label>Ubicación inicial<input name="ubicacion" list="location-codes" required/></label>
            <datalist id="location-codes">{locations.data?.map(l => <option key={l.id} value={l.codigo}/>)}</datalist></>}
        </div>}
      {locations.isError && mode !== 'deactivate' && <p role="alert">{locations.error.message} <button type="button" onClick={() => locations.refetch()}>Reintentar</button></p>}
      {error && <p role="alert" className="error">{error}</p>}
      <div className="panel-actions"><button className="action" disabled={mode === 'receive' && !locations.data?.length}>{busy ? 'Guardando…' : mode === 'deactivate' ? 'Confirmar desactivación' : 'Guardar'}</button>
        <button type="button" onClick={() => product ? change('detail') : close()}>Cancelar</button></div>
    </fieldset></form>}
  </dialog>
}
