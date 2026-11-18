# Migración de las pantallas originales a Supabase

## Decisiones confirmadas

- Reutilizar las pantallas originales y su navegación; retirar las pantallas simplificadas cuando exista equivalencia funcional.
- Supabase es la fuente de datos. No iniciar Room, ejecutar semillas locales ni usar datos locales como respaldo silencioso.
- Inventario inicial ya desplegado: 35 productos, 636 unidades, 24 ubicaciones. No volver a cargar seed.sql: ya existen operaciones reales del prototipo.
- Los identificadores de dominio y navegación serán UUID representados como String. No convertir UUID a Long ni usar índices de listas como identidades.
- La aplicación necesita conexión. Ante un fallo debe conservar el formulario, indicar el error y no mostrar éxito.

## Capas de la aplicación

Pantalla Compose original → ViewModel → interfaz de repositorio → implementación Supabase → REST/RPC.

Los ViewModels reciben los repositorios desde un contenedor de dependencias. No crean bases de datos ni DAO. Los modelos de interfaz no llevan anotaciones Room. El cliente HTTP solo transporta solicitudes; los repositorios traducen los campos PostgreSQL y las respuestas a modelos de dominio.

Una sesión compartida administra usuario, rol, token y renovación. Al salir elimina los datos en memoria. No se guardan contraseñas. Las credenciales administrativas nunca se incorporan al APK.

## Correspondencia funcional y pendientes

| Pantalla original | Fuente / operación remota | Trabajo pendiente |
| --- | --- | --- |
| HomeScreen / HomeViewModel | Productos, existencias y ventas del día | Cambiar repositorios; reponer navegación; refrescar al volver |
| ProductListScreen / ProductDetailScreen / LowStockScreen | Productos + existencias + ubicaciones | Modelos UUID, atributos completos, filtros y QR estable |
| ProductFormScreen | Guardar producto mediante RPC de dueño | Alta y edición; preservar código/QR; stock separado de atributos |
| InventoryScreen | Existencias y movimientos | Selección de ubicación; ingresos, ajustes y traslados atómicos |
| NewSaleScreen / SaleViewModel | RPC de venta completa | Carrito de varios productos; ubicación por línea; canal, cliente, dirección y pago |
| SaleHistoryScreen / SaleDetailScreen | Ventas y detalles | Paginación, detalle histórico, anulación con reposición |
| SalesChannelsScreen / ChannelDetailScreen | Ventas filtradas por canal | Estado de envío separado del estado contable |
| ReportsScreen / AbcVentasScreen | Agregaciones de ventas confirmadas | Intervalos de fechas y zona horaria; excluir anulaciones; definir clasificación sin inventarla |
| SettingsScreen | Perfiles y servicio administrativo de Auth | Roles protegidos; alta de cuentas en servidor; nunca usar service_role en Android |
| MateriaPrimaScreen | Nueva tabla materia_prima | Modelo, repositorio, permisos y formulario original |
| ProductoDerivadoScreen | Nueva tabla productos_derivados | Modelo, repositorio y permisos; empezar vacía, no importar datos de prueba |

## Contratos de operaciones

- Venta completa: UUID de operación, canal, cliente, dirección, pago y líneas con producto, ubicación y cantidad. El servidor calcula precios y total; bloquea existencias en orden estable, valida stock y registra venta, detalles y movimientos en una transacción.
- Reintentos: conservar el mismo UUID y contenido hasta confirmar el resultado, incluso tras recrear la pantalla. Una clave reutilizada con otro contenido se rechaza. Tras una respuesta incierta, consultar estado antes de crear otra operación.
- Anulación: solo dueño; transición confirmada → anulada una sola vez; reposición por las ubicaciones originales y movimientos compensatorios.
- Ingreso/ajuste/traslado: responsable obtenido de auth.uid(); motivo obligatorio para ajustes; cantidades no negativas; traslado registra salida y entrada conjuntamente.
- Producto: código y UUID permanentes; importes decimales; desactivación en lugar de borrado. No distribuir nuevamente el stock al editar una ficha.
- Perfil: alta por servicio administrativo; vendedor sin posibilidad de cambiar su rol; evitar eliminar o desactivar al último dueño.

## Evolución de PostgreSQL

Las migraciones 001–004 ya fueron aplicadas por el usuario. Crear nuevas migraciones incrementales; no modificar ni repetir esas cargas.

Agregar a ventas los campos cliente, dirección, código de orden, estado de envío y datos de pago necesarios para los formularios originales. Guardar nombre y código históricos en detalle_venta. Agregar materias primas y derivados vacíos. Los permisos de escritura continúan cerrados salvo RPC autorizadas; los roles se comprueban en servidor.

Las funciones actuales registrar_venta y registrar_ingreso sirven al flujo simplificado. Mantenerlas mientras se introduce venta completa, para no romper la versión instalada. No conceder escritura directa sobre existencias o movimientos.

## Orden de implementación

1. Modelos UUID, sesión renovable, interfaces y repositorios remotos con paginación y errores.
2. Conectar inicio, lista, detalle y stock bajo originales; comprobar ausencia de Room en el flujo activo.
3. Migraciones de producto/inventario; conectar formularios originales, ajustes y traslados.
4. Migración de venta completa y anulación; conectar carrito, historial, detalles y canales.
5. Reportes, ABC, usuarios, materias primas y derivados.
6. Retirar rutas simplificadas y referencias activas a DAO; compilar y comprobar flujos completos.

## Verificación necesaria

Compilar no verifica PostgreSQL. Probar permisos como vendedor, dueño y usuario inactivo; repetir solicitudes; stock insuficiente; dos ventas simultáneas; anulación repetida; sesión vencida; pérdida de conexión tras guardar; retorno desde formularios y reinicio de app. Comprobar que el stock coincide con movimientos y que no aparece inventario semilla local.

## Estado de este documento

Esta es la estructura de trabajo, no una declaración de implementación terminada. Actualmente siguen activas las pantallas simplificadas y los ViewModels originales todavía dependen de Room. El siguiente cambio de código debe sustituir esas dependencias antes de restaurar sus rutas.
