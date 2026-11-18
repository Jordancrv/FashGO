begin;

alter policy personal_lee_devoluciones on public.devoluciones
  using (exists (select 1 from public.ventas v where v.id = venta_id));
alter policy personal_lee_detalle_devolucion on public.detalle_devolucion
  using (exists (select 1 from public.devoluciones d where d.id = devolucion_id));

create or replace function public.registrar_devolucion(
  p_operacion uuid, p_venta uuid, p_detalle uuid, p_cantidad integer, p_motivo text
) returns uuid language plpgsql security definer set search_path = '' as $$
declare
  venta public.ventas%rowtype;
  linea public.detalle_venta%rowtype;
  devueltas bigint;
begin
  perform private.autorizar(false);
  select * into venta from public.ventas where id = p_venta
    and (public.rol_actual() = 'dueno' or vendedor_id = auth.uid()) for update;
  if not found then raise exception 'Venta no disponible'; end if;

  if exists (select 1 from public.devoluciones where id = p_operacion) then
    if exists (select 1 from public.devoluciones d join public.detalle_devolucion l on l.devolucion_id = d.id
      where d.id = p_operacion and d.venta_id = p_venta and d.responsable_id = auth.uid()
        and l.detalle_venta_id = p_detalle and l.cantidad = p_cantidad and d.motivo = btrim(p_motivo))
    then return p_operacion; end if;
    raise exception 'Identificador de operación en uso';
  end if;
  if venta.estado <> 'confirmada' then raise exception 'La venta está anulada'; end if;
  if coalesce(p_cantidad, 0) < 1 or btrim(coalesce(p_motivo,'')) = '' then
    raise exception 'Cantidad o motivo inválido';
  end if;
  select * into linea from public.detalle_venta where id = p_detalle and venta_id = p_venta;
  if not found then raise exception 'Detalle de venta no encontrado'; end if;
  select coalesce(sum(cantidad),0) into devueltas from public.detalle_devolucion where detalle_venta_id = p_detalle;
  if devueltas + p_cantidad > linea.cantidad then raise exception 'La devolución supera la cantidad disponible'; end if;

  insert into public.devoluciones(id, venta_id, responsable_id, motivo) values(p_operacion,p_venta,auth.uid(),btrim(p_motivo));
  insert into public.detalle_devolucion(devolucion_id,detalle_venta_id,cantidad) values(p_operacion,p_detalle,p_cantidad);
  update public.existencias set cantidad = cantidad + p_cantidad where producto_id = linea.producto_id and ubicacion_id = linea.ubicacion_id;
  if not found then raise exception 'No existe el inventario de origen'; end if;
  insert into public.movimientos_inventario(producto_id,ubicacion_id,venta_id,tipo,cantidad,responsable_id,motivo)
    values(linea.producto_id,linea.ubicacion_id,p_venta,'devolucion',p_cantidad,auth.uid(),'Devolución: ' || btrim(p_motivo));
  return p_operacion;
end $$;

create or replace function public.anular_venta(p_venta uuid) returns void
language plpgsql security definer set search_path = '' as $$
declare
  estado_actual text;
  linea record;
begin
  perform private.autorizar(true);
  select estado into estado_actual from public.ventas where id = p_venta for update;
  if not found then raise exception 'Venta no encontrada'; end if;
  if estado_actual = 'anulada' then return; end if;
  for linea in
    select d.producto_id, d.ubicacion_id, d.cantidad - coalesce(sum(r.cantidad),0) as saldo
    from public.detalle_venta d left join public.detalle_devolucion r on r.detalle_venta_id = d.id
    where d.venta_id = p_venta group by d.id
  loop
    if linea.saldo > 0 then
      update public.existencias set cantidad = cantidad + linea.saldo where producto_id = linea.producto_id and ubicacion_id = linea.ubicacion_id;
      if not found then raise exception 'No existe el inventario de origen'; end if;
      insert into public.movimientos_inventario(producto_id,ubicacion_id,venta_id,tipo,cantidad,responsable_id,motivo)
        values(linea.producto_id,linea.ubicacion_id,p_venta,'anulacion',linea.saldo,auth.uid(),'Anulación de venta');
    end if;
  end loop;
  update public.ventas set estado = 'anulada' where id = p_venta;
end $$;
commit;
