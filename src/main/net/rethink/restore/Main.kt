package net.rethink.restore

import javax.swing.UIManager

/**
 * Created by Thomas Neumann on 19.04.2018.
 */


fun main(args: Array<String>) {

    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

    val frame = ReStoreFrame()
    frame.isVisible = true
}
