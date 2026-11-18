-- Garantiza que toda cuenta de Authentication tenga un perfil público.
begin;

create or replace function private.crear_perfil_desde_auth()
returns trigger language plpgsql security definer set search_path='' as $$
begin
  insert into public.usuarios(id,nombre,apellido,ruc,correo,sexo,fecha_nacimiento,rol,activo)
  values(
    new.id,
    coalesce(nullif(btrim(new.raw_user_meta_data->>'nombre'),''),split_part(coalesce(new.email,''),'@',1),'Usuario'),
    coalesce(new.raw_user_meta_data->>'apellido',''),
    coalesce(new.raw_user_meta_data->>'ruc',''),
    lower(coalesce(new.email,'')),
    case when new.raw_user_meta_data->>'sexo' in ('femenino','masculino','otro','prefiero_no_decir') then new.raw_user_meta_data->>'sexo' end,
    null,
    'vendedor',
    false
  ) on conflict(id) do nothing;
  return new;
end$$;

drop trigger if exists crear_perfil_fashgo on auth.users;
create trigger crear_perfil_fashgo
after insert on auth.users for each row execute function private.crear_perfil_desde_auth();

-- Recupera cuentas que Auth creó antes de instalar el trigger.
insert into public.usuarios(id,nombre,apellido,ruc,correo,sexo,fecha_nacimiento,rol,activo)
select a.id,coalesce(nullif(split_part(coalesce(a.email,''),'@',1),''),'Usuario'),'','',lower(coalesce(a.email,'')),null,null,'vendedor',false
from auth.users a
where not exists(select 1 from public.usuarios u where u.id=a.id)
on conflict(id) do nothing;

commit;
