import { useState, type FormEvent } from 'react'
import { createClient } from '@supabase/supabase-js'
import { supabase } from './lib/supabase'
import { normalizedRut } from './lib/users'
import type { Profile } from './types'

type Props = {
  ownerId: string
  existing?: { correo: string; nombre: string; apellido: string; ruc: string; rol: Profile['rol']; sexo: string | null; fecha_nacimiento: string | null }
  onSave: () => void
  onClose: () => void
}

export function UserForm({ ownerId, existing, onSave, onClose }: Props) {
  const [registered, setRegistered] = useState(Boolean(existing))
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const fields = new FormData(event.currentTarget)
    const read = (key: string) => String(fields.get(key) ?? '').trim()
    const ruc = normalizedRut(read('ruc'))
    if (!ruc) { setError('Revisa el RUT y su dígito verificador.'); return }
    const email = read('correo').toLowerCase()
    const metadata = { nombre: read('nombre'), apellido: read('apellido'), ruc, sexo: read('sexo') }
    if (!metadata.nombre || !metadata.apellido) { setError('Completa el nombre y el apellido.'); return }
    setBusy(true)
    setError('')
    try {
      const session = await supabase.auth.getSession()
      if (session.data.session?.user.id !== ownerId) throw new Error('La sesión cambió. Vuelve a Ajustes.')
      if (!registered) {
        const registration = createClient(import.meta.env.VITE_SUPABASE_URL, import.meta.env.VITE_SUPABASE_PUBLISHABLE_KEY, {
          auth: { storageKey: 'fashgo-registration-' + crypto.randomUUID(), persistSession: false, autoRefreshToken: false, detectSessionInUrl: false }
        })
        const { data, error } = await registration.auth.signUp({
          email, password: String(fields.get('password')), options: { data: metadata }
        })
        if (error) throw error
        if (!data.user?.identities?.length) throw new Error('Revisa si la cuenta ya aparece en la lista de usuarios.')
        setRegistered(true)
      }
      const result = await supabase.rpc('registrar_perfil_por_correo', {
        p_nombre: metadata.nombre, p_apellido: metadata.apellido, p_ruc: ruc, p_correo: email,
        p_sexo: metadata.sexo, p_fecha_nacimiento: read('fecha'), p_rol: read('rol') as Profile['rol']
      })
      if (result.error) throw new Error(result.error.message)
      onSave()
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'No se pudo registrar el perfil.')
    } finally { setBusy(false) }
  }
  const [maxDate] = useState(() => {
    const today = new Date()
    return new Date(today.getTime() - today.getTimezoneOffset() * 60_000).toISOString().slice(0, 10)
  })
  return <form className="user-form panel" onSubmit={submit}>
    <h2>{registered ? 'Completar y activar perfil' : 'Crear usuario'}</h2>
    <fieldset disabled={busy}>
      <label>Nombre<input name="nombre" required defaultValue={existing?.nombre}/></label>
      <label>Apellido<input name="apellido" required defaultValue={existing?.apellido}/></label>
      <label>RUT<input name="ruc" placeholder="12.345.678-5" required defaultValue={existing?.ruc}/></label>
      <label>Correo<input name="correo" type="email" readOnly={registered} required defaultValue={existing?.correo}/></label>
      {!registered && <label>Contraseña inicial<input name="password" type="password" autoComplete="new-password" minLength={6} required/></label>}
      <label>Sexo<select name="sexo" required defaultValue={existing?.sexo ?? ''}><option value="" disabled>Seleccionar</option><option value="femenino">Femenino</option><option value="masculino">Masculino</option><option value="otro">Otro</option><option value="prefiero_no_decir">Prefiero no decir</option></select></label>
      <label>Fecha de nacimiento<input name="fecha" type="date" max={maxDate} defaultValue={existing?.fecha_nacimiento ?? ''} required/></label>
      <label>Rol<select name="rol" defaultValue={existing?.rol ?? 'vendedor'}><option value="vendedor">Vendedor</option><option value="dueno">Dueño</option></select></label>
    </fieldset>
    {error && <p role="alert" className="error">{error}</p>}
    <div className="user-actions"><button className="action" disabled={busy}>{busy ? 'Guardando…' : registered ? 'Guardar y activar' : 'Crear cuenta'}</button>
      <button type="button" className="text-action" onClick={onClose} disabled={busy}>Cerrar</button></div>
  </form>
}
