import { useMemo, useState } from 'react'
import { ProductPanel } from './Inventory'
import { classify } from './lib/abc'
import { inactiveSalesProducts, lowStockProducts, stock } from './lib/alerts'
import { useProducts, useSalesLines } from './lib/inventory'
import type { Profile } from './types'
import './alerts.css'

export function Alerts({profile}: {profile: Profile}) {
  const [tab, setTab] = useState<'stock' | 'sales'>('stock')
  const [selectedId, select] = useState('')
  const [notice, notify] = useState('')
  const products = useProducts(profile.id)
  const abcSales = useSalesLines(profile.id)
  const recent = useSalesLines(profile.id, 30)
  const classes = useMemo(() => products.data && abcSales.data && classify(products.data, abcSales.data), [products.data, abcSales.data])
  const low = useMemo(() => products.data && lowStockProducts(products.data), [products.data])
  const inactive = useMemo(() => products.data && recent.data && inactiveSalesProducts(products.data, recent.data), [products.data, recent.data])
  const isStock = tab === 'stock'
  const visible = isStock ? low : inactive
  const selected = products.data?.find(p => p.id === selectedId)
  return <>
    <h1 className="page-title">Alertas</h1>
    <div className="alert-filters" role="group" aria-label="Tipo de alerta">
      <button aria-pressed={isStock} onClick={() => setTab('stock')}>Stock mínimo <b>{low?.length ?? '—'}</b></button>
      <button aria-pressed={!isStock} onClick={() => setTab('sales')}>Sin movimiento <b>{inactive?.length ?? '—'}</b></button>
    </div>
    <p>{isStock ? 'Existencias totales iguales o inferiores al mínimo del producto.'
      : profile.rol === 'dueno' ? 'Productos sin ventas confirmadas en los últimos 30 días.'
      : 'Productos sin ventas confirmadas por ti en los últimos 30 días.'}</p>
    {notice && <p role="status">{notice}</p>}
    {[products, ...(isStock ? [] : [recent]), abcSales].map((q, i) => q.isError && <p key={i} role="alert">
      {q === abcSales ? 'ABC no disponible.' : 'Error al consultar las alertas.'} {q.error.message} <button onClick={() => q.refetch()}>Reintentar</button>
    </p>)}
    {(products.isLoading || (!isStock && recent.isLoading)) && <p role="status">Cargando alertas…</p>}
    {visible?.length === 0 && <p role="status">{isStock ? 'Todos los productos superan su stock mínimo.' : 'Ningún producto cumple este criterio.'}</p>}
    {!!visible?.length && <div className="table-wrap alert-table"><table>
      <thead><tr><th>Producto</th><th>Ubicación</th><th>Stock</th><th>Mínimo</th><th>ABC · 15 días</th></tr></thead>
      <tbody>{visible.map(p => <tr key={p.id}>
        <td><button className="product-link" onClick={() => select(p.id)}>{p.nombre}<small className="sku">{p.codigo}</small></button></td>
        <td aria-label="Ubicación">{p.existencias.map(e => e.ubicaciones?.codigo).filter(Boolean).join(' / ') || 'Sin ubicación'}</td>
        <td data-label="Stock">{stock(p)} uds.</td><td data-label="Mínimo">{p.stock_minimo} uds.</td>
        <td data-label="ABC"><span className="abc">{classes?.get(p.id) ?? '—'}</span></td>
      </tr>)}</tbody>
    </table></div>}
    {selected && <ProductPanel key={selected.id} product={selected} profile={profile} initial="detail" abc={classes?.get(selected.id)} close={() => select('')} notify={notify}/>}
  </>
}
