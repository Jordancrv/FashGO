-- Ejecutar en SQL Editor después de instalar 202611170003_operaciones.sql.
-- Todos los cambios de esta comprobación se revierten al final.
begin;
do $$
declare
  v_usuario uuid;
  v_producto uuid;
  v_ubicacion uuid;
  v_cantidad integer;
  v_final integer;
  v_ingreso uuid := gen_random_uuid();
  v_venta uuid := gen_random_uuid();
  v_rechazada boolean := false;
begin
  select id into v_usuario from public.usuarios where activo and rol = 'dueno' limit 1;
  if v_usuario is null then raise exception 'No hay una cuenta de dueña activa'; end if;
  perform set_config('request.jwt.claims', json_build_object('sub', v_usuario, 'role', 'authenticated')::text, true);
  select producto_id, ubicacion_id, cantidad into v_producto, v_ubicacion, v_cantidad
    from public.existencias where cantidad > 0 limit 1;
  if v_producto is null then raise exception 'No hay existencias para probar'; end if;

  perform public.registrar_ingreso(v_ingreso, v_producto, v_ubicacion, 2);
  perform public.registrar_ingreso(v_ingreso, v_producto, v_ubicacion, 2);
  select cantidad into v_final from public.existencias where producto_id = v_producto and ubicacion_id = v_ubicacion;
  if v_final <> v_cantidad + 2 then raise exception 'Falló el ingreso o se duplicó'; end if;

  perform public.registrar_venta(v_venta, v_producto, v_ubicacion, 1, 'Ventas Presenciales');
  perform public.registrar_venta(v_venta, v_producto, v_ubicacion, 1, 'Ventas Presenciales');
  select cantidad into v_final from public.existencias where producto_id = v_producto and ubicacion_id = v_ubicacion;
  if v_final <> v_cantidad + 1 then raise exception 'Falló la venta o se duplicó'; end if;
  if (select count(*) from public.detalle_venta where venta_id = v_venta) <> 1 then raise exception 'Detalle de venta incorrecto'; end if;

  begin
    perform public.registrar_venta(gen_random_uuid(), v_producto, v_ubicacion, v_final + 1, 'Ventas Presenciales');
  exception when others then
    v_rechazada := true;
  end;
  if not v_rechazada then raise exception 'No se rechazó la venta con stock insuficiente'; end if;
  select cantidad into v_final from public.existencias where producto_id = v_producto and ubicacion_id = v_ubicacion;
  if v_final <> v_cantidad + 1 then raise exception 'La venta rechazada cambió el stock'; end if;
  raise notice 'Comprobación satisfactoria: ingresos, ventas, reintentos y stock insuficiente';
end $$;
rollback;
