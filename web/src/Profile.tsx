import { useState } from 'react'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import type { Profile } from './types'
import { supabase } from './lib/supabase'
import { UserForm } from './UserForm'
import './profile.css'
type FullProfile=Profile&{ruc:string;sexo:string|null;fecha_nacimiento:string|null;activo:boolean}
export function MyProfile({profile}:{profile:Profile}){const query=useQuery({queryKey:['full-profile',profile.id],queryFn:async()=>{const{data,error}=await supabase.from('usuarios').select('id,nombre,apellido,ruc,correo,sexo,fecha_nacimiento,rol,activo').eq('id',profile.id).single();if(error)throw error;return data as FullProfile}});const p=query.data;return <><h1 className="page-title">Mi perfil</h1>{query.isPending&&<p>Cargando perfil…</p>}{query.isError&&<p role="alert">{query.error.message}</p>}{p&&<div className="profile-grid"><section className="profile-identity"><div className="profile-mark">{p.nombre[0]}{p.apellido?.[0]}</div><div><span className="eyebrow">Cuenta {p.activo?'activa':'inactiva'}</span><h2>{p.nombre} {p.apellido}</h2><b>{p.rol}</b></div></section><dl className="profile-data"><div><dt>Correo</dt><dd>{p.correo}</dd></div><div><dt>RUT</dt><dd>{p.ruc||'Sin registrar'}</dd></div><div><dt>Sexo</dt><dd>{p.sexo?.replaceAll('_',' ')||'Sin registrar'}</dd></div><div><dt>Fecha de nacimiento</dt><dd>{p.fecha_nacimiento?new Date(p.fecha_nacimiento+'T12:00:00').toLocaleDateString('es-CL'):'Sin registrar'}</dd></div></dl></div>}</>}
type UserRow = Pick<FullProfile, 'id' | 'nombre' | 'apellido' | 'ruc' | 'correo' | 'rol' | 'activo' | 'sexo' | 'fecha_nacimiento'>

export function SettingsUsers({ profile }: { profile: Profile }) {
  const qc = useQueryClient()
  const [notice, setNotice] = useState('')
  const [form, setForm] = useState<UserRow | 'new' | null>(null)
  const [changing, setChanging] = useState(false)
  const [page, setPage] = useState(0)
  const owner = profile.rol === 'dueno'
  const queryKey = ['users',profile.id]
  const users = useQuery({
    queryKey: [...queryKey,page], enabled: owner,
    queryFn: async () => {
      const {data,error} = await supabase.from('usuarios')
        .select('id,nombre,apellido,ruc,correo,rol,activo,sexo,fecha_nacimiento').order('nombre').order('id').range(page*30,page*30+30)
      if (error) throw error
      return data as UserRow[]
    }
  })
  async function toggle(user: UserRow) {
    if (user.activo && !window.confirm('¿Desactivar el acceso de ' + user.correo + '?')) return
    setChanging(true)
    setNotice('')
    try {
      const {error} = await supabase.rpc(user.activo ? 'desactivar_usuario' : 'reactivar_usuario', {p_id:user.id})
      if (error) throw new Error(error.message)
      setNotice(user.activo ? 'Usuario desactivado.' : 'Usuario reactivado.')
      await qc.invalidateQueries({queryKey})
    } catch (cause) {
      setNotice(cause instanceof Error ? cause.message : 'No se pudo cambiar el estado.')
    } finally { setChanging(false) }
  }
  if (!owner) return <><h1 className="page-title">Ajustes</h1><p>Tu rol no permite administrar usuarios.</p></>
  return <><h1 className="page-title">Usuarios y permisos</h1>
    {notice && <p role="status">{notice}</p>}
    {form ? <UserForm ownerId={profile.id} existing={form === 'new' ? undefined : form}
      onSave={() => {setNotice('Perfil registrado y activo.'); setForm(null); void qc.invalidateQueries({queryKey})}}
      onClose={() => {setForm(null); void qc.invalidateQueries({queryKey})}}/> :
      <button className="action" onClick={() => {setNotice(''); setForm('new')}}>Crear usuario</button>}
    {users.isPending && <p>Cargando usuarios…</p>}
    {users.isError && <p role="alert">{users.error.message} <button onClick={() => users.refetch()}>Reintentar</button></p>}
    <div className="table-wrap users-list"><table><thead><tr><th>Usuario</th><th>RUT</th><th>Rol</th><th>Estado</th><th>Acción</th></tr></thead>
      <tbody>{users.data?.slice(0,30).map(user => <tr key={user.id}>
        <td><b>{user.nombre} {user.apellido}</b><small>{user.correo}</small></td>
        <td data-label="RUT">{user.ruc || '—'}</td><td data-label="Rol">{user.rol === 'dueno' ? 'Dueño' : 'Vendedor'}</td>
        <td><span className="badge">{user.activo ? 'Activo' : 'Inactivo'}</span></td>
        <td>{user.id !== profile.id && <div className="user-actions"><button className="text-action" disabled={changing || !!form} onClick={() => toggle(user)}>
          {user.activo ? 'Desactivar' : 'Reactivar'}</button>
          {!user.activo && <button disabled={changing || !!form} className="text-action" onClick={() => setForm(user)}>Completar perfil</button>}</div>}</td>
      </tr>)}</tbody></table></div>
    {(page > 0 || (users.data?.length ?? 0) > 30) && <div className="user-actions"><button disabled={!page} onClick={() => setPage(page-1)}>Anterior</button>
      <span>Página {page+1}</span><button disabled={(users.data?.length ?? 0) <= 30} onClick={() => setPage(page+1)}>Siguiente</button></div>}
  </>
}
