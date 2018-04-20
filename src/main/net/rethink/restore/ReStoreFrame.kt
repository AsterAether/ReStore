package net.rethink.restore

import com.beust.klaxon.Klaxon
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.HttpException
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.Dimension
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.io.File
import java.util.*
import javax.swing.*
import javax.swing.table.AbstractTableModel
import javax.swing.table.TableCellRenderer


/**
 * Created by Thomas Neumann on 19.04.2018.
 */
class ReStoreFrame : JFrame("ReStore") {

    private val reStore: ReStore
    private val productTableModel: ProductTableModel = ProductTableModel()
    private val myOrderTableModel: OrderTableModel = OrderTableModel()
    private val unapOrderTableModel: OrderTableModel = OrderTableModel()
    private val openOrderTableModel: OrderTableModel = OrderTableModel()

    init {
        super.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
        val klaxon = Klaxon()

        val confFile = File("config.json")
        if (!confFile.exists())
            confFile.writeText(klaxon.toJsonString(Config("", "", "")))

        val conf: Config = klaxon.parse<Config>(confFile.readText())!!

        reStore = ReStore(conf)

        try {
            reStore.login()
        } catch (e: FuelError) {
            if (e.exception is HttpException)
                JOptionPane.showMessageDialog(null, "Login incorrect!")
            throw IllegalArgumentException("Error in login: $e")
        }


        minimumSize = Dimension(500, 400)

        setLocationRelativeTo(this)

        initComponents()
    }

    private fun initComponents() {
        contentPane.layout = BorderLayout()

        val shopTab = JPanel(BorderLayout())

        val shopList = JList<Shop>(Vector(reStore.getAllShops()))
        shopList.selectionMode = ListSelectionModel.SINGLE_SELECTION
        shopList.addListSelectionListener { e -> if (!e.valueIsAdjusting) productTableModel.setData(reStore.getProducts(shopList.selectedValue.shop_id)!!) }

        val prodTable = JTable(productTableModel)

        prodTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
        prodTable.setDefaultRenderer(Object::class.java, MultiLineCellRenderer())
        prodTable.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent?) {
                if (e!!.clickCount >= 2 && prodTable.selectedRow != -1) {
                    val product = productTableModel.getProduct(prodTable.selectedRow)
                    try {

                        val am: Int = JOptionPane.showInputDialog(this@ReStoreFrame, "Please input the amount you want").toInt()
                        val comment: String = JOptionPane.showInputDialog(this@ReStoreFrame, "Please input a comment you want to add to the order")
                        val resp = reStore.orderProduct(product_id = product.product_id, amount = am, comment = comment)!!
                        if (resp.success)
                            JOptionPane.showMessageDialog(this@ReStoreFrame, "Order received")
                        else
                            JOptionPane.showMessageDialog(this@ReStoreFrame, "Error: ${resp.reason}")
                    } catch (e: NumberFormatException) {
                        JOptionPane.showMessageDialog(this@ReStoreFrame, "You need to input a number")
                        return
                    } catch (e: IllegalStateException) {
                        return
                    }
                }
            }
        })

        shopTab.add(JScrollPane(shopList), BorderLayout.WEST)
        shopTab.add(JScrollPane(prodTable), BorderLayout.CENTER)

        val myOrdersTab = JPanel(BorderLayout())

        val myOrderTable = JTable(myOrderTableModel)
        myOrderTable.setDefaultRenderer(Object::class.java, MultiLineCellRenderer())
        myOrderTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)

        val popMyOders = JPopupMenu()

        val cancelItem = JMenuItem("cancel")
        cancelItem.addActionListener { e ->
            if (myOrderTable.selectedRow != -1) {
                try {
                    val order: Order = myOrderTableModel.getOrder(myOrderTable.selectedRow)
                    val reason: String = JOptionPane.showInputDialog(this@ReStoreFrame, "Please input your reason for canceling this order")
                    val resp = reStore.cancelOrder(order.order_id, reason)!!
                    if (resp.success) {
                        JOptionPane.showMessageDialog(this@ReStoreFrame, "Order canceled")
                        myOrderTableModel.delOrder(order = order)
                    } else
                        JOptionPane.showMessageDialog(this@ReStoreFrame, "Error: ${resp.reason}")
                } catch (e: IllegalStateException) {

                }
            }
        }

        popMyOders.add(cancelItem)

        myOrderTable.componentPopupMenu = popMyOders

        myOrdersTab.add(JScrollPane(myOrderTable), BorderLayout.CENTER)

        val unapprovedTab = JPanel(BorderLayout())

        val unapTable = JTable(unapOrderTableModel)
        unapTable.setDefaultRenderer(Object::class.java, MultiLineCellRenderer())
        unapTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)

        val popUnap = JPopupMenu()

        val unDenyItem = JMenuItem("deny")
        unDenyItem.addActionListener { e ->
            if (unapTable.selectedRow != -1) {
                try {
                    val order: Order = unapOrderTableModel.getOrder(unapTable.selectedRow)
                    val reason: String = JOptionPane.showInputDialog(this@ReStoreFrame, "Please input your reason for denying this order")
                    val resp = reStore.denyOrder(order.order_id, reason)!!
                    if (resp.success) {
                        JOptionPane.showMessageDialog(this@ReStoreFrame, "Order denied")
                        unapOrderTableModel.delOrder(order = order)
                    } else
                        JOptionPane.showMessageDialog(this@ReStoreFrame, "Error: ${resp.reason}")
                } catch (e: IllegalStateException) {

                }
            }
        }

        val approveItem = JMenuItem("approve")
        approveItem.addActionListener { e ->
            if (unapTable.selectedRow != -1) {
                try {
                    val order: Order = unapOrderTableModel.getOrder(unapTable.selectedRow)
                    val resp = reStore.approveOrder(order.order_id)!!
                    if (resp.success) {
                        JOptionPane.showMessageDialog(this@ReStoreFrame, "Order approved")
                        unapOrderTableModel.delOrder(order = order)
                    } else
                        JOptionPane.showMessageDialog(this@ReStoreFrame, "Error: ${resp.reason}")
                } catch (e: IllegalStateException) {

                }
            }
        }

        popUnap.add(unDenyItem)
        popUnap.add(approveItem)

        unapTable.componentPopupMenu = popUnap

        unapprovedTab.add(JScrollPane(unapTable), BorderLayout.CENTER)

        val openTab = JPanel(BorderLayout())

        val openTable = JTable(openOrderTableModel)
        openTable.setDefaultRenderer(Object::class.java, MultiLineCellRenderer())
        openTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)

        val popOpen = JPopupMenu()

        val openDenyItem = JMenuItem("deny")
        openDenyItem.addActionListener { e ->
            if (openTable.selectedRow != -1) {
                try {
                    val order: Order = openOrderTableModel.getOrder(openTable.selectedRow)
                    val reason: String = JOptionPane.showInputDialog(this@ReStoreFrame, "Please input your reason for denying this order")
                    val resp = reStore.denyOrder(order.order_id, reason)!!
                    if (resp.success) {
                        JOptionPane.showMessageDialog(this@ReStoreFrame, "Order denied")
                        openOrderTableModel.delOrder(order = order)
                    } else
                        JOptionPane.showMessageDialog(this@ReStoreFrame, "Error: ${resp.reason}")
                } catch (e: IllegalStateException) {

                }
            }
        }

        val finishItem = JMenuItem("finish")
        finishItem.addActionListener { e ->
            if (openTable.selectedRow != -1) {
                try {
                    val order: Order = openOrderTableModel.getOrder(openTable.selectedRow)
                    val resp = reStore.finishOrder(order.order_id)!!
                    if (resp.success) {
                        JOptionPane.showMessageDialog(this@ReStoreFrame, "Order approved")
                        openOrderTableModel.delOrder(order = order)
                    } else
                        JOptionPane.showMessageDialog(this@ReStoreFrame, "Error: ${resp.reason}")
                } catch (e: IllegalStateException) {

                }
            }
        }

        popOpen.add(openDenyItem)
        popOpen.add(finishItem)

        openTable.componentPopupMenu = popOpen

        openTab.add(JScrollPane(openTable), BorderLayout.CENTER)

        val tabPane = JTabbedPane()

        tabPane.addTab("Shops", shopTab)
        tabPane.addTab("My Orders", myOrdersTab)
        tabPane.addTab("Unapproved Orders", unapprovedTab)
        tabPane.addTab("Open Orders", openTab)

        tabPane.addChangeListener { e ->
            when (tabPane.selectedComponent) {
                myOrdersTab -> {
                    myOrderTableModel.setData(reStore.getMyOrders()!!)
                }
                unapprovedTab -> {
                    unapOrderTableModel.setData(reStore.getUnapprovedOrders()!!)
                }
                openTab -> {
                    openOrderTableModel.setData(reStore.getOpenOrders()!!)
                }
                shopTab -> {

                }
            }
        }

        contentPane.add(tabPane, BorderLayout.CENTER)
    }

    inner class MultiLineCellRenderer : JTextArea(), TableCellRenderer {
        init {
            lineWrap = true
            wrapStyleWord = true
            isOpaque = true
        }

        override fun getTableCellRendererComponent(table: JTable?, value: Any?, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int): Component {
            background = if (isSelected)
                Color.LIGHT_GRAY
            else
                table!!.background

            text = value?.toString() ?: ""
            val rowHeight = preferredSize.getHeight().toInt()

            if (table!!.getRowHeight(row) < rowHeight) {
                table.setRowHeight(row,
                        rowHeight)
            }
            return this
        }

    }

    inner class OrderTableModel : AbstractTableModel() {
        private val orderList: MutableList<Order> = mutableListOf()
        private val columnNames = arrayOf("Product", "Amount", "Comment", "Ordered on", "Approved on")

        fun setData(data: List<Order>) {
            orderList.clear()
            orderList.addAll(data)
            fireTableDataChanged()
        }

        fun getOrder(index: Int): Order {
            return orderList[index]
        }

        fun delOrder(order: Order) {
            orderList.remove(order)
            fireTableDataChanged()
        }


        override fun getColumnName(column: Int): String {
            return columnNames[column]
        }

        override fun getRowCount(): Int {
            return orderList.count()
        }

        override fun getColumnCount(): Int {
            return columnNames.count()
        }

        override fun getValueAt(rowIndex: Int, columnIndex: Int): Any? {
            val order = orderList[rowIndex]
            return when (columnIndex) {
                0 -> order.product!!.name
                1 -> order.amount
                2 -> order.comment
                3 -> order.timestamp_ordered
                4 -> order.timestamp_approved
                else -> ""
            }
        }

    }

    inner class ProductTableModel : AbstractTableModel() {

        private val productList: MutableList<Product> = mutableListOf()
        private val columnNames = arrayOf("Name", "Price", "Comment")

        fun setData(data: List<Product>) {
            productList.clear()
            productList.addAll(data)
            fireTableDataChanged()
        }

        fun getProduct(index: Int): Product {
            return productList[index]
        }

        override fun getColumnName(column: Int): String {
            return columnNames[column]
        }

        override fun getRowCount(): Int {
            return productList.count()
        }

        override fun getColumnCount(): Int {
            return columnNames.count()
        }

        override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
            return when (columnIndex) {
                0 -> productList[rowIndex].name
                1 -> "%.2f€".format(productList[rowIndex].price)
                2 -> productList[rowIndex].comment
                else -> ""
            }
        }

    }
}