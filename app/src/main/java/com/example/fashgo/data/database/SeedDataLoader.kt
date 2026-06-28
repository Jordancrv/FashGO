package com.example.fashgo.data.database

import com.example.fashgo.data.entity.MateriaPrima
import com.example.fashgo.data.entity.Producto
import com.example.fashgo.data.entity.ProductoDerivado
import java.text.Normalizer
import java.util.Locale

object SeedDataLoader {
    fun loadProducts(): List<Producto> = listOf(
        producto("Jeans", "Parte Inferior", diseno = "Cargo", color = "Azul", talla = "36", stock = 17, precio = 9990.0, ubicacion = "E6-A"),
        producto("Jeans", "Parte Inferior", diseno = "Cargo", color = "Azul", talla = "38", stock = 17, precio = 9990.0, ubicacion = "E6-A"),
        producto("Jeans", "Parte Inferior", diseno = "Cargo", color = "Celeste", talla = "40", stock = 17, precio = 9990.0, ubicacion = "E6-B"),
        producto("Jeans", "Parte Inferior", diseno = "Cargo", color = "Negro", talla = "42", stock = 16, precio = 9990.0, ubicacion = "E6-B + ZA1"),
        producto("Jeans", "Parte Inferior", diseno = "Cargo", color = "Negro", talla = "44", stock = 16, precio = 9990.0, ubicacion = "E6-B + ZA1"),

        producto("Short", "Parte Inferior", diseno = "Denim", color = "Azul", talla = "36", stock = 17, precio = 9990.0, ubicacion = "E7-A"),
        producto("Short", "Parte Inferior", diseno = "Denim", color = "Celeste", talla = "38", stock = 17, precio = 9990.0, ubicacion = "E7-B"),
        producto("Short", "Parte Inferior", diseno = "Denim", color = "Negro", talla = "40", stock = 17, precio = 9990.0, ubicacion = "E7-B"),
        producto("Short", "Parte Inferior", diseno = "Denim", color = "Azul", talla = "42", stock = 17, precio = 9990.0, ubicacion = "E7-A + ZA2"),
        producto("Short", "Parte Inferior", diseno = "Denim", color = "Celeste", talla = "44", stock = 17, precio = 9990.0, ubicacion = "ZA2"),

        producto("Poleron", "Parte Superior", diseno = "Con Cierre", color = "Negro", talla = "EST", stock = 35, precio = 6990.0, ubicacion = "E3-A"),
        producto("Poleron", "Parte Superior", diseno = "Con Cierre", color = "Azul", talla = "EST", stock = 34, precio = 6990.0, ubicacion = "E3-B"),
        producto("Poleron", "Parte Superior", diseno = "Sin Cierre", color = "Negro", talla = "EST", stock = 32, precio = 6990.0, ubicacion = "E4-A"),
        producto("Poleron", "Parte Superior", diseno = "Sin Cierre", color = "Azul", talla = "EST", stock = 17, precio = 6990.0, ubicacion = "E4-B"),

        producto("Vestido", "Enterizo", tipoManga = "Manga Corta", tipoCuello = "Cuello Redondo", color = "Rosado", talla = "EST", stock = 17, precio = 4990.0, ubicacion = "E11-B"),
        producto("Vestido", "Enterizo", tipoManga = "Manga Corta", tipoCuello = "Cuello Redondo", color = "Verde", talla = "EST", stock = 17, precio = 4990.0, ubicacion = "E11-B"),
        producto("Vestido", "Enterizo", tipoManga = "Manga Corta", tipoCuello = "Cuello Cuadrado", color = "Celeste", talla = "EST", stock = 17, precio = 4990.0, ubicacion = "E11-A"),
        producto("Vestido", "Enterizo", tipoManga = "Manga Larga", tipoCuello = "Cuello Cuadrado", color = "Naranja", talla = "EST", stock = 17, precio = 4990.0, ubicacion = "E12-A"),
        producto("Vestido", "Enterizo", tipoManga = "Manga Larga", tipoCuello = "Cuello Cuadrado", color = "Verde", talla = "EST", stock = 16, precio = 4990.0, ubicacion = "E12-A"),

        producto("Biker Shorts", "Parte Inferior", diseno = "Estandar", color = "Naranja", talla = "EST", stock = 17, precio = 3990.0, ubicacion = "E9-C"),
        producto("Biker Shorts", "Parte Inferior", diseno = "Estandar", color = "Burdeo", talla = "EST", stock = 17, precio = 3990.0, ubicacion = "E9-B"),

        producto("Calza", "Parte Inferior", diseno = "Normal", color = "Negro", talla = "EST", stock = 17, precio = 3990.0, ubicacion = "E8-A", abc = "A"),
        producto("Calza", "Parte Inferior", diseno = "Normal", color = "Burdeo", talla = "EST", stock = 17, precio = 3990.0, ubicacion = "E8-B", abc = "A"),
        producto("Calza", "Parte Inferior", diseno = "Normal", color = "Rosado", talla = "EST", stock = 17, precio = 3990.0, ubicacion = "E8-B", abc = "B"),
        producto("Calza", "Parte Inferior", diseno = "Flare", color = "Rojo", talla = "EST", stock = 17, precio = 3990.0, ubicacion = "E8-C", abc = "B"),

        producto("Blusa", "Parte Superior", tipoManga = "Manga Corta", tipoCuello = "Cuello Redondo", color = "Negro", talla = "EST", stock = 17, precio = 2990.0, ubicacion = "E1-A", abc = "B"),
        producto("Falda", "Parte Inferior", diseno = "Estandar", color = "Rojo", talla = "EST", stock = 17, precio = 2990.0, ubicacion = "E10-A", abc = "B"),
        producto("Falda", "Parte Inferior", diseno = "Estandar", color = "Marron", talla = "EST", stock = 17, precio = 2990.0, ubicacion = "E10-B", abc = "B"),
        producto("Falda", "Parte Inferior", diseno = "Estandar", color = "Burdeo", talla = "EST", stock = 17, precio = 2990.0, ubicacion = "E10-A", abc = "B"),
        producto("Blusa", "Parte Superior", tipoManga = "Manga Corta", tipoCuello = "Cuello Redondo", color = "Rosado", talla = "EST", stock = 16, precio = 2990.0, ubicacion = "E1-A", abc = "B"),
        producto("Blusa", "Parte Superior", tipoManga = "Manga Larga", tipoCuello = "Cuello Redondo", color = "Burdeo", talla = "EST", stock = 16, precio = 2990.0, ubicacion = "E2-A", abc = "B"),
        producto("Blusa", "Parte Superior", tipoManga = "Manga Larga", tipoCuello = "Cuello Redondo", color = "Marron", talla = "EST", stock = 16, precio = 2990.0, ubicacion = "E2-A", abc = "B"),
        producto("Blusa", "Parte Superior", tipoManga = "Manga Corta", tipoCuello = "Cuello Cuadrado", color = "Azul", talla = "EST", stock = 16, precio = 2990.0, ubicacion = "E1-B", abc = "B"),
        producto("Blusa", "Parte Superior", tipoManga = "Manga Corta", tipoCuello = "Cuello Cuadrado", color = "Gris", talla = "EST", stock = 16, precio = 2990.0, ubicacion = "E1-B", abc = "B"),
        producto("Blusa", "Parte Superior", tipoManga = "Manga Larga", tipoCuello = "Cuello Cuadrado", color = "Negro", talla = "EST", stock = 16, precio = 2990.0, ubicacion = "E2-B", abc = "B"),

    )

    fun loadMateriaPrima(): List<MateriaPrima> = listOf(
        materiaPrima("Tela", "Algodon negro", 22.0, "m", 5.0),
        materiaPrima("Tela", "Algodon blanco", 18.0, "m", 5.0),
        materiaPrima("Tela", "Algodon rosado", 14.0, "m", 4.0),
        materiaPrima("Tela", "Algodon rojo", 10.0, "m", 4.0),
        materiaPrima("Tela", "Licra negra", 25.0, "m", 6.0),
        materiaPrima("Tela", "Licra gris", 15.0, "m", 4.0),
        materiaPrima("Tela", "Licra rosada", 12.0, "m", 4.0),
        materiaPrima("Tela", "Denim azul", 20.0, "m", 5.0),
        materiaPrima("Tela", "Denim celeste", 16.0, "m", 5.0),
        materiaPrima("Tela", "Denim negro", 12.0, "m", 4.0),
        materiaPrima("Tela", "Polar negro", 18.0, "m", 5.0),
        materiaPrima("Tela", "Polar azul", 14.0, "m", 5.0),
        materiaPrima("Hilo", "Negro N50", 8.0, "conos", 3.0),
        materiaPrima("Hilo", "Blanco N50", 6.0, "conos", 3.0),
        materiaPrima("Hilo", "Gris N50", 5.0, "conos", 2.0),
        materiaPrima("Hilo", "Azul N50", 4.0, "conos", 2.0),
        materiaPrima("Hilo", "Rosado N50", 4.0, "conos", 2.0),
        materiaPrima("Cierre", "Invisible 20cm negro", 40.0, "unidades", 15.0),
        materiaPrima("Cierre", "Invisible 20cm blanco", 35.0, "unidades", 15.0),
        materiaPrima("Cierre", "Con dientes poleron negro", 20.0, "unidades", 8.0),
        materiaPrima("Cierre", "Con dientes poleron azul", 15.0, "unidades", 8.0),
        materiaPrima("Elastico", "2cm ancho negro", 12.0, "m", 4.0),
        materiaPrima("Elastico", "2cm ancho blanco", 10.0, "m", 4.0),
        materiaPrima("Elastico", "4cm ancho negro", 8.0, "m", 3.0),
        materiaPrima("Boton", "Estandar negro 1.5cm", 120.0, "unidades", 40.0),
        materiaPrima("Boton", "Estandar blanco 1.5cm", 90.0, "unidades", 40.0),
        materiaPrima("Remache", "Metalico dorado", 80.0, "unidades", 30.0)
    )

    fun loadProductosDerivados(): List<ProductoDerivado> = listOf(
        derivado("Falda", "EST", "Naranja", 16, 2990.0, zonaAuxiliar = 1, origen = "Categoria C movida desde inventario activo"),
        derivado("Blusa", "CC-ML", "Rojo", 13, 2990.0, zonaAuxiliar = 1, origen = "Categoria C movida desde inventario activo"),
        derivado("Blusa", "CC-ML", "Celeste", 13, 2990.0, zonaAuxiliar = 1, origen = "Categoria C movida desde inventario activo"),
        derivado("Body", "CR-MC", "Naranja", 8, 3490.0, zonaAuxiliar = 2, origen = "Categoria C movida desde inventario activo"),
        derivado("Body", "CC-MC", "Gris", 8, 3490.0, zonaAuxiliar = 2, origen = "Categoria C movida desde inventario activo"),
        derivado("Body", "CC-ML", "Marron", 7, 3490.0, zonaAuxiliar = 2, origen = "Categoria C movida desde inventario activo")
    )

    private fun producto(
        tipo: String,
        parteGeneral: String,
        tipoManga: String = "",
        tipoCuello: String = "",
        color: String,
        talla: String,
        stock: Int,
        precio: Double,
        ubicacion: String,
        diseno: String = "",
        abc: String = "A"
    ): Producto {
        val codigo = buildCode(parteGeneral, tipo, diseno, tipoManga, tipoCuello, color, talla)
        val nombre = buildShortName(tipo, diseno, tipoCuello, tipoManga, color, talla)
        return Producto(
            nombre = nombre,
            parteGeneral = parteGeneral,
            tipo = tipo,
            diseno = diseno,
            tipoManga = tipoManga,
            tipoCuello = tipoCuello,
            color = color,
            talla = talla,
            precio = precio,
            stock = stock,
            stockReferencia = stock,
            ubicacion = ubicacion,
            codigoProducto = codigo,
            qrCode = codigo,
            stockMinimo = (stock * 0.25).toInt().coerceAtLeast(3),
            clasificacionAbc = abc
        )
    }

    private fun materiaPrima(
        nombre: String,
        descripcion: String,
        cantidad: Double,
        unidadMedida: String,
        stockMinimo: Double
    ): MateriaPrima = MateriaPrima(
        nombre = nombre,
        descripcion = descripcion,
        cantidad = cantidad,
        unidadMedida = unidadMedida,
        proveedor = "Stock minimo sugerido: $stockMinimo $unidadMedida"
    )

    private fun derivado(
        tipoPrenda: String,
        subcategoria: String,
        color: String,
        cantidad: Int,
        precioUnitario: Double,
        zonaAuxiliar: Int,
        origen: String
    ): ProductoDerivado = ProductoDerivado(
        tipoPrenda = tipoPrenda,
        subcategoria = subcategoria,
        color = color,
        talla = "EST",
        cantidad = cantidad,
        precioUnitario = precioUnitario,
        ubicacion = "Zona Auxiliar $zonaAuxiliar",
        zonaAuxiliar = zonaAuxiliar,
        origen = origen
    )

    private fun buildCode(
        parteGeneral: String,
        tipo: String,
        diseno: String,
        tipoManga: String,
        tipoCuello: String,
        color: String,
        talla: String
    ): String {
        val part = mapOf(
            "Parte Superior" to "PAS",
            "Parte Inferior" to "PAI",
            "Enterizo" to "ENT"
        )[parteGeneral] ?: "GEN"
        val type = mapOf(
            "Blusa" to "BLU",
            "Jeans" to "JNS",
            "Short" to "SHO",
            "Poleron" to "POL",
            "Vestido" to "VST",
            "Biker Shorts" to "BSH",
            "Calza" to "CAL",
            "Falda" to "FAL"
        )[tipo] ?: normalizeToken(tipo, 3)
        val variant = when {
            parteGeneral == "Parte Inferior" -> mapOf(
                "Cargo" to "CG",
                "Denim" to "DEN",
                "Estandar" to "EST",
                "Normal" to "NOR",
                "Flare" to "FLR"
            )[diseno] ?: normalizeToken(diseno, 3)
            tipoCuello.isNotBlank() -> mapOf(
                "Cuello Redondo" to "CR",
                "Cuello Cuadrado" to "CC"
            )[tipoCuello] ?: normalizeToken(tipoCuello, 2)
            else -> mapOf(
                "Con Cierre" to "CCR",
                "Sin Cierre" to "SCR"
            )[diseno] ?: normalizeToken(diseno, 3)
        }
        val sleeve = mapOf(
            "Manga Corta" to "MC",
            "Manga Larga" to "ML"
        )[tipoManga].orEmpty()
        val colorCode = mapOf(
            "Negro" to "NEG",
            "Azul" to "AZU",
            "Celeste" to "CEL",
            "Rosado" to "ROS",
            "Verde" to "VER",
            "Naranja" to "NAR",
            "Burdeo" to "BUR",
            "Rojo" to "ROJ",
            "Marron" to "MAR",
            "Gris" to "GRI"
        )[color] ?: normalizeToken(color, 3)
        val size = if (talla.equals("Estandar", ignoreCase = true)) "EST" else talla.uppercase(Locale.ROOT)
        return listOf(part, type, variant, sleeve, colorCode, size, "001")
            .filter { it.isNotBlank() }
            .joinToString("-")
    }

    private fun buildShortName(
        tipo: String,
        diseno: String,
        tipoCuello: String,
        tipoManga: String,
        color: String,
        talla: String
    ): String {
        val detail = when {
            tipoCuello.isNotBlank() || tipoManga.isNotBlank() ->
                listOf(abbrevNeck(tipoCuello), abbrevSleeve(tipoManga)).filter { it.isNotBlank() }.joinToString("-")
            diseno.isNotBlank() -> abbrevDesign(diseno)
            else -> ""
        }
        val size = if (talla.equals("Estandar", ignoreCase = true)) "EST" else talla.uppercase(Locale.ROOT)
        return listOf(tipo, detail, color, size)
            .filter { it.isNotBlank() }
            .joinToString(" - ")
    }

    private fun abbrevNeck(value: String): String = when (value) {
        "Cuello Redondo" -> "CR"
        "Cuello Cuadrado" -> "CC"
        else -> ""
    }

    private fun abbrevSleeve(value: String): String = when (value) {
        "Manga Corta" -> "MC"
        "Manga Larga" -> "ML"
        else -> ""
    }

    private fun abbrevDesign(value: String): String = when (value) {
        "Cargo" -> "CG"
        "Denim" -> "DEN"
        "Estandar" -> "EST"
        "Normal" -> "NOR"
        "Flare" -> "FLR"
        "Con Cierre" -> "CCR"
        "Sin Cierre" -> "SCR"
        else -> normalizeToken(value, 3)
    }

    private fun normalizeToken(value: String, length: Int): String {
        val normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
            .replace("\\p{Mn}+".toRegex(), "")
            .uppercase(Locale.ROOT)
            .replace("[^A-Z0-9]".toRegex(), "")
        return normalized.take(length).padEnd(length, 'X')
    }
}
