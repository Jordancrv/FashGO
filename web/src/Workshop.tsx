import { useEffect, useRef, useState } from 'react'
import type { FormEvent } from 'react'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import { pages } from './lib/inventory'
import { supabase } from './lib/supabase'
import { recordConsumption, workshopFields, workshopPayload } from './lib/workshop'
import type { WorkshopKind, WorkshopRow } from './lib/workshop'
import type { Profile } from './types'
import './workshop.css'

type Mode = 'detail' | 'edit' | 'consume' | 'deactivate'
function nameOf(row: WorkshopRow) { return String(row.nombre ?? row.tipo_prenda) }
const format = (value: number) => value.toLocaleString('es-CO')

export function Workshop({kind, profile}: {kind: WorkshopKind; profile: Profile}) {
  const material = kind === 'materia_prima'
  const owner = profile.rol === 'dueno'
  const [query, search] = useState('')
  const [selection, select] = useState<{row?: WorkshopRow} | null>(null)
  const [notice, notify] = useState('')
  const records = useQuery({
    queryKey: [kind, profile.id],
    queryFn: () => pages<WorkshopRow>((from, to) => supabase.from(kind).select('*').eq('activo',true)
      .order(material ? 'nombre' : 'tipo_prenda').order('id').range(from,to))
  })
  const terms = query.trim().toLocaleLowerCase().split(/\s+/)
  const rows = records.data?.filter(row => {
    const text = workshopFields[kind].map(f => row[f.key]).join(' ').toLocaleLowerCase()
    return terms.every(term => text.includes(term))
  })
  const headers = material ? ['Material','Disponible','Proveedor'] : ['Prenda','Cantidad','Precio unitario','Ubicación']
  return <>
    <h1 className="page-title">{material ? 'Materia prima' : 'Productos derivados'}</h1>
    <div className="toolbar">
      <input className="search" type="search" aria-label="Buscar registros" placeholder={material ? 'Buscar por material o proveedor…' : 'Buscar por prenda, color o ubicación…'} value={query} onChange={e => search(e.target.value)}/>
      {owner && <button className="action" onClick={() => select({})}>{material ? 'Nuevo material' : 'Nuevo derivado'}</button>}
    </div>
    <p role="status">{notice}</p>
    {records.isLoading && <p>Cargando registros…</p>}
    {records.isError && <p role="alert">{records.error.message} <button onClick={() => records.refetch()}>Reintentar</button></p>}
    {rows && <div className="table-wrap workshop-table"><table>
      <thead><tr>{headers.map(h => <th key={h}>{h}</th>)}</tr></thead>
      <tbody>{rows.map(row => <tr key={row.id}>
        <td><button className="product-link" onClick={() => select({row})}>{nameOf(row)}
          <small className="sku">{material ? row.descripcion : [row.subcategoria,row.color,row.talla].filter(Boolean).join(' · ')}</small></button></td>
        <td data-label={headers[1]}>{format(row.cantidad)} {material ? row.unidad_medida : 'uds.'}</td>
        <td data-label={headers[2]}>{material ? row.proveedor || '—' : '$' + format(Number(row.precio_unitario))}</td>
        {!material && <td data-label="Ubicación">{row.ubicacion || '—'}</td>}
      </tr>)}</tbody></table>{!rows.length && <p>Sin registros para mostrar.</p>}</div>}
    {selection && <WorkshopPanel kind={kind} profile={profile} row={selection.row} close={() => select(null)} notify={notify}/>}
  </>
}

function WorkshopPanel({kind, profile, row, close, notify}: {
  kind: WorkshopKind; profile: Profile; row?: WorkshopRow; close: () => void; notify: (message:string) => void
}) {
  const dialog = useRef<HTMLDialogElement>(null)
  const [id] = useState(() => row?.id ?? crypto.randomUUID())
  const material = kind === 'materia_prima'
  const owner = profile.rol === 'dueno'
  const pendingKey = ['fashgo','consumo',profile.id,id].join(':')
  const [mode, setMode] = useState<Mode>(row ? 'detail' : 'edit')
  const [busy, setBusy] = useState(false)
  const lock = useRef(false)
  const [error, setError] = useState('')
  const [pending, setPending] = useState<{operation:string; quantity:number} | null>(() => {
    if (!owner || !material || !row) return null
    const saved = localStorage.getItem(pendingKey)
    return saved ? JSON.parse(saved) : null
  })
  const cache = useQueryClient()
  useEffect(() => {
    const el = dialog.current!
    el.showModal()
    return () => el.close()
  }, [])
  function change(next:Mode) { setError(''); setMode(next) }
  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!owner || lock.current) return
    const form = new FormData(event.currentTarget)
    lock.current = true
    setBusy(true)
    setError('')
    try {
      if (mode === 'consume') {
        await navigator.locks.request(pendingKey, () => recordConsumption({
          key:pendingKey, available:row!.cantidad, quantity:Number(form.get('consumo')),
          pending, storage:localStorage, onPending:setPending,
          send:request => supabase.rpc('consumir_materia_prima', {
            p_id:id, p_operacion:request.operation, p_cantidad:request.quantity
          })
        }))
      } else {
        const suffix = material ? 'materia_prima' : 'derivado'
        const result = mode === 'deactivate'
          ? await supabase.rpc('desactivar_' + suffix, {p_id:id})
          : await supabase.rpc('guardar_' + suffix, {p_id:id,p_datos:workshopPayload(kind,form,row)})
        if (result.error) throw new Error(result.error.message)
      }
      await Promise.all([cache.invalidateQueries({queryKey:[kind,profile.id]}), cache.invalidateQueries({queryKey:['audit',profile.id]})])
      notify(mode === 'consume' ? 'Consumo registrado.' : mode === 'deactivate' ? 'Registro desactivado.' : 'Registro guardado.')
      close()
    } catch (e) { setError(e instanceof Error ? e.message : 'No se pudo completar la operación.') }
    finally { lock.current = false; setBusy(false) }
  }
  return <dialog ref={dialog} className="inventory-panel" aria-labelledby="workshop-title" onCancel={e => {e.preventDefault(); if (!busy) close()}}>
    <header><span className="eyebrow">{material ? 'Materia prima' : 'Productos derivados'}</span><button disabled={busy} onClick={close}>Cerrar</button></header>
    <h2 id="workshop-title">{row ? nameOf(row) : material ? 'Nuevo material' : 'Nuevo derivado'}</h2>
    {mode === 'detail' && row && <>
      <dl>{workshopFields[kind].map(f => <div key={f.key}><dt>{f.label}</dt><dd>{row[f.key] ?? '—'}</dd></div>)}
        {!material && <><div><dt>Rotación</dt><dd>{row.rotacion}</dd></div><div><dt>Decisión sugerida</dt><dd>{row.decision_sugerida}</dd></div></>}
      </dl>
      {owner && <div className="panel-actions">
        {material && <button className="action" disabled={!pending && row.cantidad <= 0} onClick={() => change('consume')}>{pending ? 'Reintentar consumo pendiente' : 'Registrar consumo'}</button>}
        <button disabled={!!pending} onClick={() => change('edit')}>Editar</button><button disabled={!!pending} onClick={() => change('deactivate')}>Desactivar</button>
      </div>}
    </>}
    {owner && mode !== 'detail' && <form onSubmit={submit}><fieldset disabled={busy}>
      {mode === 'edit' && <div className="product-fields">{workshopFields[kind].map(f => <label key={f.key}>{f.label}
        <input name={f.key} required={f.required} type={f.step ? 'number' : 'text'} min={f.min} max={f.max} step={f.step} defaultValue={row?.[f.key] ?? f.initial ?? ''}/>
      </label>)}</div>}
      {mode === 'consume' && <label>Cantidad a descontar ({row?.unidad_medida})
        <input type="number" name="consumo" step="0.01" min="0.01" max={pending ? undefined : row?.cantidad} defaultValue={pending?.quantity} required readOnly={!!pending}/>
      </label>}
      {mode === 'deactivate' && <p>¿Desactivar este registro y retirarlo de la lista de activos?</p>}
      <div className="panel-actions"><button className="action" type="submit">{busy ? 'Procesando…' : mode === 'edit' ? 'Guardar' : mode === 'consume' ? 'Confirmar consumo' : 'Confirmar desactivación'}</button>
        <button type="button" onClick={() => row ? change('detail') : close()}>Cancelar</button></div>
    </fieldset></form>}
    {error && <p role="alert">{error}</p>}
  </dialog>
}
