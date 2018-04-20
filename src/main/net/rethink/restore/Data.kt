package net.rethink.restore

import com.beust.klaxon.Json
import java.time.LocalDateTime
import java.util.*

/**
 * Created by Thomas Neumann on 19.04.2018.
 */

@Target(AnnotationTarget.FIELD)
annotation class KlaxonDate

data class Product(val name: String, val product_id: Int, @Json(name = "shop") val shop_id: Int, val comment: String, val price: Float)

data class ProductQueryResult(@Json(name = "_items") val items: List<Product>)

data class Shop(val name: String, val shop_id: Int, @Json(name = "owner_ref") val owner_id: Int, val description: String) {
    override fun toString(): String {
        return name
    }
}

data class ShopQueryResult(@Json(name = "_items") val items: List<Shop>)

data class Config(val basePath: String, val username: String, val password: String)

data class QueryResponse(val success: Boolean, val reason: String = "")

data class Order @JvmOverloads constructor(
        val order_id: Int,
        val amount: Int,
        val comment: String,
        val product_id: Int,
        val customer: Int,
        @KlaxonDate
        val timestamp_ordered: LocalDateTime? = null,
        @KlaxonDate
        val timestamp_done: LocalDateTime? = null,
        @KlaxonDate
        val timestamp_approved: LocalDateTime? = null,
        val product: Product? = null
)

data class OrdersResponse(@Json(name = "json_list") val orders: List<Order>)