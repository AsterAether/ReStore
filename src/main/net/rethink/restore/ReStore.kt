package net.rethink.restore

import com.beust.klaxon.Klaxon
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.httpGet

/**
 * Created by Thomas Neumann on 19.04.2018.
 */
class ReStore(val config: Config) {

    private val klaxon = Klaxon()

    init {
        FuelManager.instance.basePath = config.basePath
    }

    fun getAllProducts(): List<Product>? {
        val (_, _, res) = "/api/product".httpGet().responseString()
        return klaxon.parse<ProductQueryResult>(res.get())?.items
    }
}