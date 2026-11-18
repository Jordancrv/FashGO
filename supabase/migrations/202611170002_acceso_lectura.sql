-- Acceso inicial de solo lectura para cuentas activas de FashGO.
-- Las escrituras de inventario y ventas se habilitarán mediante operaciones
-- transaccionales después de implementar y probar sus reglas de negocio.
begin;

revoke all on public.usuarios, public.productos, public.ubicaciones,
  public.existencias, public.ventas, public.detalle_venta,
  public.movimientos_inventario from anon, authenticated;

grant usage on schema public to authenticated;
grant select on public.usuarios, public.productos, public.ubicaciones,
  public.existencias, public.ventas, public.detalle_venta,
  public.movimientos_inventario to authenticated;

create policy usuario_lee_su_perfil on public.usuarios
  for select to authenticated using (id = auth.uid() and activo);

create policy personal_lee_productos on public.productos
  for select to authenticated using (exists (
    select 1 from public.usuarios where id = auth.uid() and activo
  ));
create policy personal_lee_ubicaciones on public.ubicaciones
  for select to authenticated using (exists (
    select 1 from public.usuarios where id = auth.uid() and activo
  ));
create policy personal_lee_existencias on public.existencias
  for select to authenticated using (exists (
    select 1 from public.usuarios where id = auth.uid() and activo
  ));
create policy dueno_lee_ventas on public.ventas
  for select to authenticated using (exists (
    select 1 from public.usuarios where id = auth.uid() and activo and rol = 'dueno'
  ));
create policy dueno_lee_detalle_venta on public.detalle_venta
  for select to authenticated using (exists (
    select 1 from public.usuarios where id = auth.uid() and activo and rol = 'dueno'
  ));
create policy dueno_lee_movimientos on public.movimientos_inventario
  for select to authenticated using (exists (
    select 1 from public.usuarios where id = auth.uid() and activo and rol = 'dueno'
  ));

commit;
