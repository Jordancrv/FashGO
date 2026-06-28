from pathlib import Path

from openpyxl import Workbook
from openpyxl.styles import Alignment, Border, Font, PatternFill, Side
from openpyxl.utils import get_column_letter
from openpyxl.worksheet.table import Table, TableStyleInfo


OUTPUT = Path("FashGo_Cuadros_SKU_y_Flujo_v2.xlsx")


HEADER_FILL = PatternFill("solid", fgColor="1F4E78")
SECTION_FILL = PatternFill("solid", fgColor="D9EAF7")
NOTE_FILL = PatternFill("solid", fgColor="FFF2CC")
HEADER_FONT = Font(color="FFFFFF", bold=True)
BOLD = Font(bold=True)
THIN = Side(style="thin", color="B7C0C7")
BORDER = Border(left=THIN, right=THIN, top=THIN, bottom=THIN)


def style_cell(cell, fill=None, font=None, wrap=True):
    cell.border = BORDER
    cell.alignment = Alignment(vertical="top", wrap_text=wrap)
    if fill:
        cell.fill = fill
    if font:
        cell.font = font


def write_table(ws, start_row, title, headers, rows, table_name):
    ws.cell(start_row, 1, title)
    style_cell(ws.cell(start_row, 1), fill=SECTION_FILL, font=BOLD)
    ws.merge_cells(start_row=start_row, start_column=1, end_row=start_row, end_column=len(headers))
    for col, header in enumerate(headers, start=1):
        cell = ws.cell(start_row + 1, col, header)
        style_cell(cell, fill=HEADER_FILL, font=HEADER_FONT)
    for r_idx, row in enumerate(rows, start=start_row + 2):
        for c_idx, value in enumerate(row, start=1):
            cell = ws.cell(r_idx, c_idx, value)
            style_cell(cell)
    end_row = start_row + 1 + len(rows)
    end_col = len(headers)
    ref = f"A{start_row + 1}:{get_column_letter(end_col)}{end_row}"
    table = Table(displayName=table_name, ref=ref)
    table.tableStyleInfo = TableStyleInfo(
        name="TableStyleMedium2",
        showFirstColumn=False,
        showLastColumn=False,
        showRowStripes=True,
        showColumnStripes=False,
    )
    ws.add_table(table)
    return end_row


def autofit(ws):
    widths = {}
    for row in ws.iter_rows():
        for cell in row:
            if cell.value is None:
                continue
            widths[cell.column] = max(widths.get(cell.column, 0), len(str(cell.value)))
    for col_idx, width in widths.items():
        ws.column_dimensions[get_column_letter(col_idx)].width = min(max(width + 2, 12), 42)


wb = Workbook()


# Hoja 1
ws1 = wb.active
ws1.title = "Estructura SKU"
ws1["A1"] = "Cuadro de estructura de códigos SKU de prendas - FashGo"
style_cell(ws1["A1"], fill=SECTION_FILL, font=Font(bold=True, size=14))
ws1.merge_cells("A1:F1")
ws1["A3"] = (
    "En FashGo, el SKU corresponde al campo codigoProducto. El QR del producto usa el mismo valor del SKU."
)
style_cell(ws1["A3"], fill=NOTE_FILL, font=BOLD)
ws1.merge_cells("A3:F3")

headers1 = ["Componente", "Posición", "Uso", "Código/Regla", "Descripción", "Ejemplo"]
rows1 = [
    ["Parte general", "1", "Todos", "PAS / PAI / ENT", "Tipo macro de prenda: Parte Superior, Parte Inferior o Enterizo.", "PAS"],
    ["Tipo", "2", "Todos", "BL, TP, TPA, POL, CH, JNS, CAL, FAL, SHO, BSH, VST", "Familia específica de la prenda.", "BL"],
    ["Diseño o cuello", "3", "Variable", "PAI usa diseño; PAS y ENT usan cuello", "En parte inferior va el diseño. En superior/enterizo va el cuello.", "CR o CG"],
    ["Manga", "4", "PAS y ENT", "MC / ML / NA", "Tipo de manga. En parte inferior no se usa.", "MC"],
    ["Color", "5", "Todos", "NEG, BLA, ROS, ROJ, AZU, CEL, GRI...", "Color abreviado en 3 letras o token normalizado.", "NEG"],
    ["Talla", "6", "Todos", "S, M, L, 36, 38, EST...", "Talla comercial o numérica.", "S"],
    ["Secuencia", "7", "Todos", "001, 002, 003...", "Consecutivo automático por prefijo.", "001"],
    ["Estructura PAS/ENT", "Completa", "Parte superior / enterizo", "PARTE-TIPO-CUELLO-MANGA-COLOR-TALLA-SECUENCIA", "Modelo con cuello y manga.", "PAS-BL-CR-MC-NEG-S-001"],
    ["Estructura PAI", "Completa", "Parte inferior", "PARTE-TIPO-DISEÑO-COLOR-TALLA-SECUENCIA", "Modelo sin cuello ni manga.", "PAI-JNS-CG-AZU-36-001"],
    ["Regla QR", "Aplica al cierre", "Todos", "QR = SKU", "El código QR guarda el mismo valor del SKU para búsqueda y movimientos.", "PAS-BL-CR-MC-NEG-S-001"],
]
end1 = write_table(ws1, 5, "Estructura general del SKU", headers1, rows1, "TablaEstructuraSku")

rows1b = [
    ["Blusa manga corta cuello redondo negra talla S", "PAS-BL-CR-MC-NEG-S-001", "Parte superior"],
    ["Jeans cargo azul talla 36", "PAI-JNS-CG-AZU-36-001", "Parte inferior"],
    ["Vestido cuello cuadrado manga larga verde estándar", "ENT-VST-CC-ML-VER-EST-001", "Enterizo"],
    ["Biker shorts estándar burdeo talla M", "PAI-BSH-ET-BUR-M-001", "Parte inferior"],
]
write_table(
    ws1,
    end1 + 2,
    "Ejemplos de SKU listos para documento",
    ["Descripción comercial", "SKU", "Clasificación"],
    rows1b,
    "TablaEjemplosSku",
)
ws1.freeze_panes = "A6"
ws1.sheet_view.showGridLines = False
autofit(ws1)


# Hoja 2
ws2 = wb.create_sheet("Codificacion Prendas")
ws2["A1"] = "Tabla de codificación de prendas - FashGo"
style_cell(ws2["A1"], fill=SECTION_FILL, font=Font(bold=True, size=14))
ws2.merge_cells("A1:E1")
ws2["A3"] = (
    "La codificación de prendas resume los catálogos usados para construir el SKU. "
    "Se presenta en bloques compactos para facilitar su incorporación en informes."
)
style_cell(ws2["A3"], fill=NOTE_FILL, font=BOLD)
ws2.merge_cells("A3:E3")

headers2a = ["Bloque", "Valores permitidos", "Códigos", "Aplicación", "Observación"]
rows2a = [
    ["Parte general", "Parte Superior / Parte Inferior / Enterizo", "PAS / PAI / ENT", "Define el inicio del SKU.", "Es el nivel principal de clasificación."],
    ["Tipo de prenda", "Blusa, Top, Top Asimétrico, Polerón, Chaleco, Jeans, Calzas, Faldas, Shorts, Biker Shorts, Vestido", "BL, TP, TPA, POL, CH, JNS, CAL, FAL, SHO, BSH, VST", "Define la familia de producto.", "Es el segundo bloque del SKU."],
    ["Cuello o diseño", "Cuello Redondo, Cuello Cuadrado / Cargo, Normal, Flare, Estándar", "CR, CC / CG, NM, FL, ET", "En superiores y enterizos se usa cuello; en inferiores se usa diseño.", "Este bloque cambia según el tipo de prenda."],
    ["Manga", "Manga Corta / Manga Larga / No aplica", "MC / ML / NA", "Se usa en superiores y enterizos.", "En parte inferior no aparece."],
    ["Color", "Negro, Blanco, Rosado, Rojo, Azul, Celeste, Gris, Marrón, Fucsia, Naranja, Verde, Crema, Mostaza, Morado, Burdeo, Lila", "NEG, BLA, ROS, ROJ, AZU, CEL, GRI, MAR, FUC, NAR, VER, CRE, MOS, MOR, BUR, LIL", "Identifica la variante comercial.", "Normalmente se codifica en 3 letras."],
    ["Talla", "S, M, L, EST, 36, 38, 40, 42, 44...", "Se usa el mismo valor comercial", "Identifica la medida o talla de venta.", "Puede ser alfabética, numérica o estándar."],
    ["Secuencia", "001, 002, 003...", "Consecutivo automático", "Cierra el SKU y evita duplicados.", "Se incrementa por cada prefijo existente."],
]
end2a = write_table(ws2, 5, "Resumen de codificación", headers2a, rows2a, "TablaResumenCodificacion")

headers2b = ["Elemento", "Opciones principales", "Código", "Ejemplo"]
rows2b = [
    ["Parte general", "Parte Superior", "PAS", "PAS-BL-CR-MC-NEG-S-001"],
    ["Parte general", "Parte Inferior", "PAI", "PAI-JNS-CG-AZU-36-001"],
    ["Parte general", "Enterizo", "ENT", "ENT-VST-CC-ML-VER-EST-001"],
    ["Cuello", "Cuello Redondo", "CR", "PAS-BL-CR-MC-NEG-S-001"],
    ["Cuello", "Cuello Cuadrado", "CC", "ENT-VST-CC-ML-VER-EST-001"],
    ["Diseño", "Cargo", "CG", "PAI-JNS-CG-AZU-36-001"],
    ["Diseño", "Normal", "NM", "PAI-CAL-NM-NEG-M-001"],
    ["Diseño", "Flare", "FL", "PAI-CAL-FL-MAR-M-001"],
    ["Diseño", "Estándar", "ET", "PAI-BSH-ET-BUR-M-001"],
    ["Manga", "Manga Corta", "MC", "PAS-TP-CR-MC-AZU-M-001"],
    ["Manga", "Manga Larga", "ML", "PAS-POL-NA-ML-NEG-M-001"],
]
end2b = write_table(ws2, end2a + 2, "Equivalencias clave", headers2b, rows2b, "TablaEquivalenciasClave")

headers2c = ["Caso", "Construcción del código", "Resultado"]
rows2c = [
    ["Blusa negra, cuello redondo, manga corta, talla S", "PAS + BL + CR + MC + NEG + S + 001", "PAS-BL-CR-MC-NEG-S-001"],
    ["Jeans azul, diseño cargo, talla 36", "PAI + JNS + CG + AZU + 36 + 001", "PAI-JNS-CG-AZU-36-001"],
    ["Vestido verde, cuello cuadrado, manga larga, talla estándar", "ENT + VST + CC + ML + VER + EST + 001", "ENT-VST-CC-ML-VER-EST-001"],
    ["Biker shorts burdeo, diseño estándar, talla M", "PAI + BSH + ET + BUR + M + 001", "PAI-BSH-ET-BUR-M-001"],
]
write_table(ws2, end2b + 2, "Ejemplos de codificación", headers2c, rows2c, "TablaEjemplosCodificacion")
ws2.freeze_panes = "A6"
ws2.sheet_view.showGridLines = False
autofit(ws2)


# Hoja 3
ws3 = wb.create_sheet("Flujo Diario")
ws3["A1"] = "Flujo general de uso de la aplicación en un día común"
style_cell(ws3["A1"], fill=SECTION_FILL, font=Font(bold=True, size=14))
ws3.merge_cells("A1:G1")
ws3["A3"] = (
    "Flujo construido con base en los módulos visibles de la app: Home, Productos, Inventario, Nueva Venta, Reportes y alertas de stock."
)
style_cell(ws3["A3"], fill=NOTE_FILL, font=BOLD)
ws3.merge_cells("A3:G3")

headers3 = ["Momento", "Rol típico", "Módulo", "Acción", "Datos que usa", "Resultado esperado", "Impacto en control"]
rows3 = [
    ["Inicio del día", "Dueño o vendedor", "Home", "Revisar ventas del día, transacciones y alertas de bajo stock.", "KPIs diarios, conteo de productos, unidades y alertas.", "Panorama rápido del estado del negocio.", "Permite priorizar reposición y ventas."],
    ["Inicio del día", "Dueño", "Inventario Centralizado", "Buscar productos por nombre, código, color o talla.", "SKU/codigoProducto, stock, ubicación, clasificación ABC.", "Validación de disponibilidad antes de abrir atención.", "Asegura consistencia de stock físico vs sistema."],
    ["Inicio del día", "Dueño o encargado", "Inventario Centralizado", "Registrar ingresos manuales o por escaneo QR.", "QR = SKU, cantidad ingresada, fecha, producto.", "Stock actualizado y movimiento guardado.", "Incrementa stock y deja trazabilidad."],
    ["Durante el día", "Vendedor", "Nueva Venta", "Buscar producto o escanear QR para agregarlo al carrito.", "Nombre, precio, stock, SKU y ABC.", "Selección rápida del producto correcto.", "Reduce errores de venta y de identificación."],
    ["Durante el día", "Vendedor", "Nueva Venta", "Modificar cantidades del carrito según pedido del cliente.", "Stock disponible por producto.", "Carrito listo para cobrar sin exceder stock.", "Evita ventas por encima del inventario."],
    ["Durante el día", "Vendedor", "Checkout / Cobro", "Registrar monto recibido, canal, cliente y dirección si aplica.", "Total, efectivo recibido, cambio, canal de venta.", "Venta confirmada y cambio calculado.", "Descuenta stock automáticamente y genera historial."],
    ["Durante el día", "Vendedor", "Detalle de producto", "Escanear QR desde Home para ver ficha del producto.", "QR/SKU, atributos de la prenda, ubicación.", "Consulta rápida sin navegar por listas largas.", "Mejora atención y control operativo."],
    ["Mitad del día", "Dueño", "Alertas / Bajo Stock", "Revisar productos en semáforo rojo o amarillo.", "Stock actual, stock mínimo y clasificación ABC.", "Lista priorizada para reabastecimiento.", "Ayuda a decidir compras o producción."],
    ["Mitad del día", "Dueño", "Productos", "Registrar un producto nuevo si la prenda no existe.", "Parte general, tipo, cuello/diseño, manga, color, talla.", "La app sugiere SKU consecutivo automáticamente.", "Mantiene la estructura uniforme de codificación."],
    ["Mitad del día", "Dueño", "Productos", "Guardar producto nuevo con ubicación, precio y stock mínimo.", "SKU generado, precio, ubicación, stock inicial.", "Nuevo SKU disponible para venta e inventario.", "Amplía catálogo sin romper la trazabilidad."],
    ["Cierre del día", "Dueño", "Movimientos de Inventario", "Revisar historial de ingresos realizados durante el día.", "Producto, SKU, cantidad y fecha/hora.", "Confirmación de reposiciones ejecutadas.", "Auditoría operativa básica."],
    ["Cierre del día", "Dueño", "Historial / Reportes", "Revisar ventas realizadas y productos con mayor salida.", "Ventas, detalle por SKU, clasificación ABC.", "Evaluación comercial del día.", "Base para decisiones de compra, producción y promoción."],
    ["Cierre del día", "Dueño", "Inventario + ABC", "Validar si cambian prioridades A/B/C o niveles de semáforo.", "Rotación, stock restante y valor del inventario.", "Reordenar focos del día siguiente.", "Conecta operación, rentabilidad y reposición."],
]
write_table(ws3, 5, "Flujo operativo diario", headers3, rows3, "TablaFlujoDiario")
ws3.freeze_panes = "A6"
ws3.sheet_view.showGridLines = False
autofit(ws3)


wb.save(OUTPUT)
print(OUTPUT.resolve())
