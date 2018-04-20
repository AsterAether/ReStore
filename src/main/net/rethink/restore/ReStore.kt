package net.rethink.restore

import com.beust.klaxon.Converter
import com.beust.klaxon.JsonValue
import com.beust.klaxon.Klaxon
import com.beust.klaxon.KlaxonException
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.httpGet
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Created by Thomas Neumann on 19.04.2018.
 */
class ReStore(val config: Config) {

    private val dateConverter = object : Converter {
        override fun canConvert(cls: Class<*>) = cls == LocalDate::class.java

        override fun fromJson(jv: JsonValue) =
                if (jv.string != null) {
                    LocalDateTime.parse(jv.string, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                } else {
                    throw KlaxonException("Couldn't parse date: ${jv.string}")
                }

        override fun toJson(value: Any) =
                """{"date": $value}"""
    }

    private val klaxon = Klaxon().fieldConverter(KlaxonDate::class, dateConverter)

    init {
        FuelManager.instance.basePath = config.basePath
    }

    fun login(): QueryResponse? {
        val (_, _, res) = "/api/login".httpGet().authenticate(config.username, config.password).responseString()
        return klaxon.parse<QueryResponse>(res.get())
    }

    fun getProducts(shop_id: Int): List<Product>? {
        val (_, _, res) = """/api/product?where={"shop_id":"$shop_id"}""".httpGet().responseString()
        return klaxon.parse<ProductQueryResult>(res.get())?.items
    }

    fun getAllShops(): List<Shop>? {
        val (_, _, res) = "/api/shop".httpGet().responseString()
        return klaxon.parse<ShopQueryResult>(res.get())?.items
    }

    fun orderProduct(product_id: Int, amount: Int, comment: String): QueryResponse? {
        val (_, _, res) = "/api/order/$product_id/$amount/$comment".httpGet().authenticate(config.username, config.password).responseString()
        return klaxon.parse<QueryResponse>(res.get())
    }

    fun cancelOrder(order_id: Int, reason: String): QueryResponse? {
        val (_, _, res) = "/api/order/cancel/$order_id/$reason".httpGet().authenticate(config.username, config.password).responseString()
        return klaxon.parse<QueryResponse>(res.get())
    }

    fun denyOrder(order_id: Int, reason: String): QueryResponse? {
        val (_, _, res) = "/api/order/deny/$order_id/$reason".httpGet().authenticate(config.username, config.password).responseString()
        return klaxon.parse<QueryResponse>(res.get())
    }

    fun approveOrder(order_id: Int): QueryResponse? {
        val (_, _, res) = "/api/order/approve/$order_id".httpGet().authenticate(config.username, config.password).responseString()
        return klaxon.parse<QueryResponse>(res.get())
    }

    fun finishOrder(order_id: Int): QueryResponse? {
        val (_, _, res) = "/api/order/finish/$order_id".httpGet().authenticate(config.username, config.password).responseString()
        return klaxon.parse<QueryResponse>(res.get())
    }

    fun getMyOrders(): List<Order>? {
        val (_, _, res) = "/api/orders/my".httpGet().authenticate(config.username, config.password).responseString()
        return klaxon.parse<OrdersResponse>(res.get())?.orders
    }

    fun getUnapprovedOrders(): List<Order>? {
        val (_, _, res) = "/api/orders/unapproved".httpGet().authenticate(config.username, config.password).responseString()
        return klaxon.parse<OrdersResponse>(res.get())?.orders
    }

    fun getOpenOrders(): List<Order>? {
        val (_, _, res) = "/api/orders/open".httpGet().authenticate(config.username, config.password).responseString()
        return klaxon.parse<OrdersResponse>(res.get())?.orders
    }

}