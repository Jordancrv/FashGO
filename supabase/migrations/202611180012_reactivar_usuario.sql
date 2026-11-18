begin;

create or replace function public.reactivar_usuario(p_id uuid)
returns void
language plpgsql
security definer
set search_path = ''
as $$
begin
  perform private.autorizar(true);

  update public.usuarios
  set activo = true
  where id = p_id;

  if not found then
    raise exception 'Usuario no encontrado';
  end if;
end;
$$;

revoke all on function public.reactivar_usuario(uuid) from public, anon;
grant execute on function public.reactivar_usuario(uuid) to authenticated;

commit;
