-- Datos ampliados del perfil. La contraseña permanece exclusivamente en Supabase Auth.
begin;
alter table public.usuarios add column if not exists apellido text not null default '';
alter table public.usuarios add column if not exists ruc text not null default '';
alter table public.usuarios add column if not exists correo text not null default '';
alter table public.usuarios add column if not exists sexo text;
alter table public.usuarios add column if not exists fecha_nacimiento date;

update public.usuarios u
set correo=coalesce((select a.email from auth.users a where a.id=u.id),'')
where u.correo='';

drop function if exists public.registrar_perfil_usuario(uuid,text,text);
create or replace function public.registrar_perfil_usuario(
  p_id uuid,p_nombre text,p_apellido text,p_ruc text,p_correo text,
  p_sexo text,p_fecha_nacimiento date,p_rol text
) returns void language plpgsql security definer set search_path='' as $$
begin
  perform private.autorizar(true);
  if p_rol not in ('dueno','vendedor')
     or btrim(coalesce(p_nombre,''))=''
     or btrim(coalesce(p_apellido,''))=''
     or btrim(coalesce(p_ruc,''))=''
     or btrim(coalesce(p_correo,''))=''
     or p_sexo not in ('femenino','masculino','otro','prefiero_no_decir')
     or p_fecha_nacimiento is null or p_fecha_nacimiento>current_date
     or not exists(select 1 from auth.users where id=p_id and lower(email)=lower(btrim(p_correo)))
  then raise exception 'Datos de usuario inválidos'; end if;
  if exists(select 1 from public.usuarios where id<>p_id and (lower(correo)=lower(btrim(p_correo)) or ruc=btrim(p_ruc))) then
    raise exception 'El correo o RUC ya está registrado';
  end if;
  insert into public.usuarios(id,nombre,apellido,ruc,correo,sexo,fecha_nacimiento,rol,activo)
  values(p_id,btrim(p_nombre),btrim(p_apellido),btrim(p_ruc),lower(btrim(p_correo)),p_sexo,p_fecha_nacimiento,p_rol,true)
  on conflict(id) do update set nombre=excluded.nombre,apellido=excluded.apellido,ruc=excluded.ruc,
    correo=excluded.correo,sexo=excluded.sexo,fecha_nacimiento=excluded.fecha_nacimiento,
    rol=excluded.rol,activo=true;
end$$;
revoke all on function public.registrar_perfil_usuario(uuid,text,text,text,text,text,date,text) from public,anon;
grant execute on function public.registrar_perfil_usuario(uuid,text,text,text,text,text,date,text) to authenticated;
commit;
