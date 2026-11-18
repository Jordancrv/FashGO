-- Cada vendedor puede consultar sus propias ventas y detalles.
begin;
create policy vendedor_lee_sus_ventas on public.ventas
  for select to authenticated using (
    vendedor_id = auth.uid() and exists (
      select 1 from public.usuarios where id = auth.uid() and activo and rol = 'vendedor'
    )
  );
create policy vendedor_lee_sus_detalles on public.detalle_venta
  for select to authenticated using (
    exists (select 1 from public.ventas where id = venta_id and vendedor_id = auth.uid())
    and exists (select 1 from public.usuarios where id = auth.uid() and activo and rol = 'vendedor')
  );
commit;
