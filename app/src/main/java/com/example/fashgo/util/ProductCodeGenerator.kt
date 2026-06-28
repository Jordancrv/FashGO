package com.example.fashgo.util

import java.text.Normalizer
import java.util.Locale

data class ProductDraft(
    val parteGeneral: String,
    val tipo: String,
    val diseno: String = "",
    val tipoManga: String = "",
    val tipoCuello: String = "",
    val color: String = "",
    val talla: String = ""
)

object ProductCodeGenerator {
    private val partCodes = mapOf(
        "Parte Superior" to "PAS",
        "Parte Inferior" to "PAI",
        "Enterizo" to "ENT"
    )

    private val typeCodes = mapOf(
        "Blusa" to "BL",
        "Top" to "TP",
        "Top Asimetrico" to "TPA",
        "Poleron Estandar" to "POL",
        "Chaleco Estandar" to "CH",
        "Jeans" to "JNS",
        "Calzas" to "CAL",
        "Faldas" to "FAL",
        "Shorts" to "SHO",
        "Biker Shorts" to "BSH",
        "Vestido" to "VST"
    )

    private val neckCodes = mapOf(
        "Cuello Redondo" to "CR",
        "Cuello Cuadrado" to "CC",
        "NA" to "NA"
    )

    private val sleeveCodes = mapOf(
        "Manga Corta" to "MC",
        "Manga Larga" to "ML",
        "NA" to "NA"
    )

    private val designCodes = mapOf(
        "Cargo" to "CG",
        "Normal" to "NM",
        "Flare" to "FL",
        "Estándar" to "ET",
        "Estandar" to "ET"
    )

    private val colorCodes = mapOf(
        "Negro" to "NEG",
        "Blanco" to "BLA",
        "Rosado" to "ROS",
        "Rojo" to "ROJ",
        "Azul" to "AZU",
        "Celeste" to "CEL",
        "Gris" to "GRI",
        "Marron" to "MAR",
        "Fucsia" to "FUC",
        "Naranja" to "NAR",
        "Verde" to "VER",
        "Crema" to "CRE",
        "Mostaza" to "MOS",
        "Morado" to "MOR",
        "Burdeo" to "BUR",
        "Lila" to "LIL"
    )

    fun buildName(draft: ProductDraft): String {
        return listOf(
            draft.tipo,
            draft.diseno,
            draft.tipoCuello,
            draft.tipoManga,
            draft.color,
            draft.talla.takeIf { it.isNotBlank() }?.let { "Talla $it" }.orEmpty()
        )
            .filter { it.isNotBlank() && it != "NA" }
            .joinToString(" - ")
    }

    fun buildPrefix(draft: ProductDraft): String {
        val typeToken = tokenFor(draft.tipo, typeCodes, 3)
        val designOrNeck = when {
            draft.parteGeneral == "Parte Inferior" -> tokenFor(draft.diseno, designCodes, 2)
            else -> tokenFor(draft.tipoCuello, neckCodes, 2)
        }
        val sleeveToken = if (draft.parteGeneral == "Parte Inferior") "" else tokenFor(draft.tipoManga, sleeveCodes, 2)
        val colorToken = tokenFor(draft.color, colorCodes, 3)
        val sizeToken = normalizeToken(draft.talla, draft.talla.length.coerceAtLeast(1))

        return listOf(
            tokenFor(draft.parteGeneral, partCodes, 3),
            typeToken,
            designOrNeck,
            sleeveToken,
            colorToken,
            sizeToken
        ).filter { it.isNotBlank() }.joinToString("-")
    }

    fun buildCode(draft: ProductDraft, existingCodes: List<String>): String {
        val prefix = buildPrefix(draft)
        val nextSequence = existingCodes.mapNotNull(::extractSequence).maxOrNull()?.plus(1) ?: 1
        return "$prefix-${nextSequence.toString().padStart(3, '0')}"
    }

    private fun extractSequence(code: String): Int? = code.substringAfterLast('-').toIntOrNull()

    private fun tokenFor(value: String, knownCodes: Map<String, String>, fallbackLength: Int): String {
        if (value.isBlank()) return ""
        return knownCodes[value]
            ?: normalizeToken(value, fallbackLength)
    }

    private fun normalizeToken(value: String, length: Int): String {
        val normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
            .replace("\\p{Mn}+".toRegex(), "")
            .uppercase(Locale.ROOT)
            .replace("[^A-Z0-9]".toRegex(), "")
        return normalized.take(length).padEnd(length, 'X')
    }
}
