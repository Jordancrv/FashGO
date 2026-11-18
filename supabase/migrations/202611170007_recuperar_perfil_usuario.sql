-- Permite completar un perfil cuando Auth ya creó la cuenta por correo.
begin;
create or replace function public.registrar_perfil_por_correo(
  p_nombre text,p_apellido text,p_ruc text,p_correo text,
  p_sexo text,p_fecha_nacimiento date,p_rol text
) returns uuid language plpgsql security definer set search_path='' as $$
declare v_id uuid;
begin
  perform private.autorizar(true);
  select id into v_id from auth.users where lower(email)=lower(btrim(p_correo));
  if v_id is null then raise exception 'La cuenta todavía no existe en Authentication'; end if;
  if p_rol not in ('dueno','vendedor') or btrim(coalesce(p_nombre,''))=''
     or btrim(coalesce(p_apellido,''))='' or btrim(coalesce(p_ruc,''))=''
     or p_sexo not in ('femenino','masculino','otro','prefiero_no_decir')
     or p_fecha_nacimiento is null or p_fecha_nacimiento>current_date
  then raise exception 'Datos de usuario inválidos'; end if;
  if exists(select 1 from public.usuarios where id<>v_id and (lower(correo)=lower(btrim(p_correo)) or ruc=btrim(p_ruc))) then
    raise exception 'El correo o RUC ya está registrado';
  end if;
  insert into public.usuarios(id,nombre,apellido,ruc,correo,sexo,fecha_nacimiento,rol,activo)
  values(v_id,btrim(p_nombre),btrim(p_apellido),btrim(p_ruc),lower(btrim(p_correo)),p_sexo,p_fecha_nacimiento,p_rol,true)
  on conflict(id) do update set nombre=excluded.nombre,apellido=excluded.apellido,ruc=excluded.ruc,
    correo=excluded.correo,sexo=excluded.sexo,fecha_nacimiento=excluded.fecha_nacimiento,
    rol=excluded.rol,activo=true;
  return v_id;
end$$;
revoke all on function public.registrar_perfil_por_correo(text,text,text,text,text,date,text) from public,anon;
grant execute on function public.registrar_perfil_por_correo(text,text,text,text,text,date,text) to authenticated;
commit;
