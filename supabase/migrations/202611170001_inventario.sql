-- Esquema inicial: la aplicación aún no tiene permisos de escritura.
begin;

create table public.usuarios (
  id uuid primary key references auth.users(id) on delete restrict,
  nombre text not null,
  rol text not null check (rol in ('dueno', 'vendedor')),
  activo boolean not null default true
);
create table public.productos (
  id uuid primary key default gen_random_uuid(),
  codigo text not null unique,
  nombre text not null,
  parte_general text not null,
  tipo text not null,
  diseno text not null default '',
  tipo_manga text not null default '',
  tipo_cuello text not null default '',
  color text not null,
  talla text not null,
  precio numeric(12,2) not null check (precio >= 0),
  stock_minimo integer not null default 0 check (stock_minimo >= 0),
  activo boolean not null default true
);
create table public.ubicaciones (
  id uuid primary key default gen_random_uuid(),
  codigo text not null unique,
  estante text,
  seccion text,
  descripcion text
);
create table public.existencias (
  producto_id uuid not null references public.productos(id) on delete restrict,
  ubicacion_id uuid not null references public.ubicaciones(id) on delete restrict,
  cantidad integer not null check (cantidad >= 0),
  primary key (producto_id, ubicacion_id)
);
create table public.ventas (
  id uuid primary key default gen_random_uuid(),
  fecha timestamptz not null default now(),
  vendedor_id uuid references public.usuarios(id) on delete restrict,
  canal text not null,
  total numeric(12,2) not null check (total >= 0),
  estado text not null check (estado in ('confirmada', 'anulada'))
);
create table public.detalle_venta (
  id uuid primary key default gen_random_uuid(),
  venta_id uuid not null references public.ventas(id) on delete restrict,
  producto_id uuid not null references public.productos(id) on delete restrict,
  ubicacion_id uuid not null references public.ubicaciones(id) on delete restrict,
  cantidad integer not null check (cantidad > 0),
  precio_unitario numeric(12,2) not null check (precio_unitario >= 0)
);
create table public.movimientos_inventario (
  id uuid primary key default gen_random_uuid(),
  producto_id uuid not null references public.productos(id) on delete restrict,
  ubicacion_id uuid not null references public.ubicaciones(id) on delete restrict,
  venta_id uuid references public.ventas(id) on delete restrict,
  tipo text not null check (tipo in ('saldo_inicial', 'entrada', 'salida', 'ajuste', 'traslado', 'anulacion')),
  cantidad integer not null check (cantidad <> 0),
  fecha timestamptz not null default now(),
  responsable_id uuid references public.usuarios(id) on delete restrict,
  motivo text
);
create index movimientos_producto_fecha on public.movimientos_inventario(producto_id, fecha);
create index detalle_venta_venta on public.detalle_venta(venta_id);

-- Sin políticas de acceso, los clientes quedan bloqueados hasta configurar roles.
alter table public.usuarios enable row level security;
alter table public.productos enable row level security;
alter table public.ubicaciones enable row level security;
alter table public.existencias enable row level security;
alter table public.ventas enable row level security;
alter table public.detalle_venta enable row level security;
alter table public.movimientos_inventario enable row level security;
commit;
