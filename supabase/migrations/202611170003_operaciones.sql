-- Operaciones atómicas: las tablas siguen sin permitir escrituras directas.
begin;

create function public.registrar_ingreso(p_operacion uuid, p_producto uuid, p_ubicacion uuid, p_cantidad integer)
returns uuid language plpgsql security definer set search_path = '' as $$
declare v_existente public.movimientos_inventario%rowtype;
begin
  if not exists (select 1 from public.usuarios where id = auth.uid() and activo and rol = 'dueno') then
    raise exception 'Solo la dueña puede registrar ingresos';
  end if;
  if p_operacion is null or p_cantidad is null or p_cantidad <= 0 then raise exception 'Cantidad inválida'; end if;
  perform pg_catalog.pg_advisory_xact_lock(pg_catalog.hashtextextended(p_operacion::text, 0));
  select * into v_existente from public.movimientos_inventario where id = p_operacion;
  if found then
    if v_existente.tipo = 'entrada' and v_existente.producto_id = p_producto and v_existente.ubicacion_id = p_ubicacion and v_existente.cantidad = p_cantidad and v_existente.responsable_id = auth.uid() then return p_operacion; end if;
    raise exception 'Identificador de operación ya utilizado';
  end if;
  if not exists (select 1 from public.productos where id = p_producto and activo) then raise exception 'Producto no disponible'; end if;
  insert into public.existencias(producto_id, ubicacion_id, cantidad) values (p_producto, p_ubicacion, p_cantidad)
    on conflict (producto_id, ubicacion_id) do update set cantidad = public.existencias.cantidad + excluded.cantidad;
  insert into public.movimientos_inventario(id, producto_id, ubicacion_id, tipo, cantidad, responsable_id)
    values (p_operacion, p_producto, p_ubicacion, 'entrada', p_cantidad, auth.uid());
  return p_operacion;
end $$;

create function public.registrar_venta(p_operacion uuid, p_producto uuid, p_ubicacion uuid, p_cantidad integer, p_canal text)
returns uuid language plpgsql security definer set search_path = '' as $$
declare v_precio numeric(12,2); v_existente public.ventas%rowtype;
begin
  if not exists (select 1 from public.usuarios where id = auth.uid() and activo and rol in ('dueno','vendedor')) then raise exception 'Usuario no autorizado'; end if;
  if p_operacion is null or p_cantidad is null or p_cantidad <= 0 or p_canal is null or pg_catalog.btrim(p_canal) = '' then raise exception 'Datos de venta inválidos'; end if;
  perform pg_catalog.pg_advisory_xact_lock(pg_catalog.hashtextextended(p_operacion::text, 0));
  select * into v_existente from public.ventas where id = p_operacion;
  if found then
    if v_existente.vendedor_id = auth.uid() and v_existente.canal = p_canal and (select count(*) from public.detalle_venta where venta_id = p_operacion) = 1 and exists (select 1 from public.detalle_venta where venta_id = p_operacion and producto_id = p_producto and ubicacion_id = p_ubicacion and cantidad = p_cantidad) then return p_operacion; end if;
    raise exception 'Identificador de operación ya utilizado';
  end if;
  select precio into v_precio from public.productos where id = p_producto and activo;
  if not found then raise exception 'Producto no disponible'; end if;
  update public.existencias set cantidad = cantidad - p_cantidad where producto_id = p_producto and ubicacion_id = p_ubicacion and cantidad >= p_cantidad;
  if not found then raise exception 'Stock insuficiente en la ubicación seleccionada'; end if;
  insert into public.ventas(id, vendedor_id, canal, total, estado) values (p_operacion, auth.uid(), p_canal, v_precio * p_cantidad, 'confirmada');
  insert into public.detalle_venta(venta_id, producto_id, ubicacion_id, cantidad, precio_unitario) values (p_operacion, p_producto, p_ubicacion, p_cantidad, v_precio);
  insert into public.movimientos_inventario(producto_id, ubicacion_id, venta_id, tipo, cantidad, responsable_id) values (p_producto, p_ubicacion, p_operacion, 'salida', -p_cantidad, auth.uid());
  return p_operacion;
end $$;

revoke all on function public.registrar_ingreso(uuid,uuid,uuid,integer) from public, anon;
revoke all on function public.registrar_venta(uuid,uuid,uuid,integer,text) from public, anon;
grant execute on function public.registrar_ingreso(uuid,uuid,uuid,integer) to authenticated;
grant execute on function public.registrar_venta(uuid,uuid,uuid,integer,text) to authenticated;
commit;
