package com.example.fashgo.util

import com.example.fashgo.data.entity.ProductSalesStat
import com.example.fashgo.data.entity.Producto

data class InventorySummary(
    val totalSkus: Int = 0,
    val totalUnits: Int = 0,
    val totalValue: Double = 0.0,
    val redCount: Int = 0,
    val yellowCount: Int = 0,
    val greenCount: Int = 0
)

object InventoryAnalytics {
    fun buildSummary(products: List<Producto>): InventorySummary {
        return InventorySummary(
            totalSkus = products.size,
            totalUnits = products.sumOf { it.stock },
            totalValue = products.sumOf { it.stock * it.precio },
            redCount = products.count { it.semaforoEstado == "ROJO" },
            yellowCount = products.count { it.semaforoEstado == "AMARILLO" },
            greenCount = products.count { it.semaforoEstado == "VERDE" }
        )
    }

    fun classifyAbcBySales(
        products: List<Producto>,
        salesStats: List<ProductSalesStat>
    ): Map<Long, String> {
        val soldByProductId = salesStats.associate { it.productoId to it.unidadesVendidas }
        val activeProducts = products.filter { (soldByProductId[it.id] ?: 0) > 0 }
            .sortedByDescending { soldByProductId[it.id] ?: 0 }

        if (activeProducts.isEmpty()) {
            return products.associate { it.id to it.clasificacionAbc }
        }

        val topCut = kotlin.math.ceil(activeProducts.size / 3.0).toInt().coerceAtLeast(1)
        val midCut = kotlin.math.ceil((activeProducts.size * 2) / 3.0).toInt().coerceAtLeast(topCut)

        return buildMap(products.size) {
            activeProducts.forEachIndexed { index, product ->
                put(
                    product.id,
                    when {
                        index < topCut -> "A"
                        index < midCut -> "B"
                        else -> "B"
                    }
                )
            }
            products
                .asSequence()
                .filter { !containsKey(it.id) }
                .forEach { put(it.id, "C") }
        }
    }
}
