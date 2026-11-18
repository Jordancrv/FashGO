-- Perfil consultable y bitácora automática de acciones del sistema.
begin;

create table if not exists public.auditoria_movimientos(
  id uuid primary key default gen_random_uuid(),
  entidad text not null,
  accion text not null,
  entidad_id uuid,
  detalle text not null,
  responsable_id uuid references public.usuarios(id) on delete set null,
  fecha timestamptz not null default now()
);
create index if not exists auditoria_fecha_idx on public.auditoria_movimientos(fecha desc);
create index if not exists auditoria_responsable_idx on public.auditoria_movimientos(responsable_id,fecha desc);
alter table public.auditoria_movimientos enable row level security;
revoke all on public.auditoria_movimientos from anon,authenticated;
grant select on public.auditoria_movimientos to authenticated;
drop policy if exists dueno_lee_auditoria on public.auditoria_movimientos;
create policy dueno_lee_auditoria on public.auditoria_movimientos for select to authenticated
using(public.rol_actual()='dueno');

create or replace function private.auditar_cambios() returns trigger
language plpgsql security definer set search_path='' as $$
declare v_id uuid;v_detalle text;v_entidad text;v_accion text;
begin
  if auth.uid() is null then return new; end if;
  v_entidad:=tg_argv[0];
  v_accion:=case when tg_op='INSERT' then 'creacion' else 'actualizacion' end;
  if tg_table_name='productos' then
    v_id:=new.id;
    v_detalle:=case when tg_op='INSERT' then 'Producto creado: '||new.nombre||' ('||new.codigo||')'
      else 'Producto actualizado: '||new.nombre||' ('||new.codigo||')' end;
  elsif tg_table_name='materia_prima' then
    v_id:=new.id;
    v_detalle:=case when tg_op='INSERT' then 'Materia prima creada: '||new.nombre||' - '||new.cantidad||' '||new.unidad_medida
      else 'Materia prima actualizada: '||new.nombre||' - cantidad '||new.cantidad||' '||new.unidad_medida end;
  elsif tg_table_name='productos_derivados' then
    v_id:=new.id;
    v_detalle:=case when tg_op='INSERT' then 'Producto derivado creado: '||new.tipo_prenda||' '||new.color||' talla '||new.talla
      else 'Producto derivado actualizado: '||new.tipo_prenda||' '||new.color||' talla '||new.talla end;
  else
    return new;
  end if;
  insert into public.auditoria_movimientos(entidad,accion,entidad_id,detalle,responsable_id)
  values(v_entidad,v_accion,v_id,v_detalle,auth.uid());
  return new;
end$$;

create or replace function private.auditar_inventario() returns trigger
language plpgsql security definer set search_path='' as $$
declare v_nombre text;v_codigo text;v_ubicacion text;
begin
  if auth.uid() is null then return new; end if;
  select p.nombre,p.codigo,u.codigo into v_nombre,v_codigo,v_ubicacion
  from public.productos p,public.ubicaciones u
  where p.id=new.producto_id and u.id=new.ubicacion_id;
  insert into public.auditoria_movimientos(entidad,accion,entidad_id,detalle,responsable_id,fecha)
  values('inventario',new.tipo,new.producto_id,
    coalesce(v_nombre,'Producto')||' ('||coalesce(v_codigo,'')||'): '||
    case when new.cantidad>0 then '+' else '' end||new.cantidad||' unidades en '||coalesce(v_ubicacion,'Sin ubicación')||
    case when coalesce(new.motivo,'')<>'' then '. Motivo: '||new.motivo else '' end,
    coalesce(new.responsable_id,auth.uid()),new.fecha);
  return new;
end$$;

drop trigger if exists auditar_productos on public.productos;
create trigger auditar_productos after insert or update on public.productos for each row execute function private.auditar_cambios('producto');
drop trigger if exists auditar_materia_prima on public.materia_prima;
create trigger auditar_materia_prima after insert or update on public.materia_prima for each row execute function private.auditar_cambios('materia_prima');
drop trigger if exists auditar_derivados on public.productos_derivados;
create trigger auditar_derivados after insert or update on public.productos_derivados for each row execute function private.auditar_cambios('producto_derivado');
drop trigger if exists auditar_movimientos_inventario on public.movimientos_inventario;
create trigger auditar_movimientos_inventario after insert on public.movimientos_inventario for each row execute function private.auditar_inventario();

commit;
