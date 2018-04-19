package net.rethink.restore

import com.beust.klaxon.Klaxon
import java.io.File
import javax.swing.JFrame

/**
 * Created by Thomas Neumann on 19.04.2018.
 */
class ReStoreFrame : JFrame("ReStore") {
    init {
        super.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
        val klaxon = Klaxon()

        val confFile = File("config.json")
        if (!confFile.exists())
            confFile.writeText(klaxon.toJsonString(Config("", "", "")))

        val conf: Config = klaxon.parse<Config>(confFile.readText())!!

        val reStore = ReStore(conf)

        for (prod in reStore.getAllProducts()!!) {
            println(prod.name)
        }
    }
}