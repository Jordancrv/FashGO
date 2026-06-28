# FashGo Data Connect

Esta carpeta deja preparada la base para migrar el inventario centralizado a Firebase Data Connect.

Antes de usarla:

1. Reemplaza los valores placeholder en `dataconnect.yaml`.
2. Vincula el proyecto con tu Firebase real.
3. Genera el SDK cliente con la CLI de Firebase.
4. Importa el catalogo inicial desde `app/src/main/assets/seed_inventory.json`.

Pasos sugeridos:

```powershell
firebase login
firebase use <tu-proyecto>
firebase init dataconnect
firebase deploy --only dataconnect
```

Modelo preparado:

- `Product`: inventario centralizado con codigo, QR, stock, semaforo y atributos del catalogo.
- `StockIngress`: movimientos de ingreso.
- `Sale` y `SaleItem`: ventas y detalle para recalcular stock y KPIs.
- `AppUser`: dueno y vendedor.

La app Android actual ya usa el mismo modelo de datos en Room, por lo que la siguiente etapa es cambiar el repositorio local por el SDK generado de Data Connect.
