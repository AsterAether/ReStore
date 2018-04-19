package net.rethink.restore

import com.beust.klaxon.Json
import java.util.*

/**
 * Created by Thomas Neumann on 19.04.2018.
 */
data class Product(val name: String)

data class ProductQueryResult(@Json(name = "_items") val items: List<Product>)

data class Config(val basePath: String, val username: String, val password: String)