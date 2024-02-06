package com.vendetta.miinventario

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import android.widget.SimpleAdapter
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.vendetta.miinventario.data.ConnectionClass
import com.vendetta.miinventario.databinding.ActivityFacturaPageBinding
import java.io.InputStream
import java.io.OutputStream
import android.Manifest
import android.content.pm.PackageManager
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.vendetta.miinventario.adapter.FacturaAdapter
import com.vendetta.miinventario.adapter.ProductosAdapter
import com.vendetta.miinventario.data.FacturaProvider
import com.vendetta.miinventario.data.ProductosProvider
import java.io.IOException
import java.lang.StringBuilder
import java.nio.charset.Charset
import java.text.DecimalFormat
import java.util.UUID

class FacturaPage : AppCompatActivity() {

    lateinit var binding: ActivityFacturaPageBinding
    private var btPermission = false
    var bluetoothAdapter : BluetoothAdapter? = null
    var socket : BluetoothSocket? = null
    var bluetoothDevice :BluetoothDevice? = null
    var outputStream : OutputStream? = null
    var inputStream : InputStream? = null
    var workerThread: Thread? = null
    lateinit var readBuffer: ByteArray
    private var readBufferPosition = 0
    private val MY_PERMISSIONS_REQUEST_BLUETOOTH_SCAN = 1


    @Volatile
    var stopWorker = false
    private var value = ""
    private val connectionClass : ConnectionClass = ConnectionClass()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFacturaPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initAdapter()

        binding.printerName.isFocusable=false

        binding.selectBt.setOnClickListener {
            println("Funcion")
            checkPermission()
        }

        binding.btnPrint.setOnClickListener {

            if(btPermission){
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
                        != PackageManager.PERMISSION_GRANTED
                    ) {
                        // Si no tienes el permiso, solicítalo.
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(Manifest.permission.BLUETOOTH_SCAN),
                            MY_PERMISSIONS_REQUEST_BLUETOOTH_SCAN
                        )
                    } else {
                        // Si ya tienes el permiso, puedes proceder.
                        print_inv()
                    }
                }else{
                    print_inv()
                }

            }else{
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
                    != PackageManager.PERMISSION_GRANTED) {
                    // Si no tienes el permiso, solicítalo.
                    ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.BLUETOOTH_SCAN),
                        MY_PERMISSIONS_REQUEST_BLUETOOTH_SCAN)
                } else {
                    // Si ya tienes el permiso, puedes proceder.
                    checkPermission()
                }
                    }else
                {
                    checkPermission()
                }

            }
        }
    }

    private fun initAdapter() {
        val recyclerView = binding.recycleFactura
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = FacturaAdapter(FacturaProvider.facturaList)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_BLUETOOTH_SCAN -> {
                // Aquí manejas la respuesta del usuario a tu solicitud de permiso de Bluetooth
            }
            else -> {
                // Ignora todas las demás solicitudes de permisos
            }
        }
    }

fun scanBt(view:View){
    checkPermission()
}
    fun print(view: View){
    if(btPermission){
        print_inv()
    }else{
        checkPermission()
    }
    }

    fun checkPermission(){
        println("Hello world")
        val bluetoothManager : BluetoothManager = getSystemService(BluetoothManager::class.java)
        val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
        if(bluetoothAdapter == null){
            //No soporta bluetooth
            println("No soporta")
        }
        else{
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
                println("Paso Version S")
                bluetoothPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
            }else{
                println("NO Paso Version S")
                bluetoothPermissionLauncher.launch(Manifest.permission.BLUETOOTH_ADMIN)
            }
        }
    }
    private val bluetoothPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ){
        isGranted : Boolean ->
        if(isGranted){
            val bluetoothManager: BluetoothManager = getSystemService(BluetoothManager::class.java)
            val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
            btPermission = true
            if(bluetoothAdapter?.isEnabled == false){
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                btActivityResultLaucher.launch(enableBtIntent)
            }
            else{
                btScan()

            }
        }
    }

    private val btActivityResultLaucher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ){
        result: ActivityResult ->
        if(result.resultCode == RESULT_OK){
            btScan()
        }
    }

    @SuppressLint("MissingPermission")
    fun btScan(){
        val bluetoothManager : BluetoothManager = getSystemService(BluetoothManager::class.java)
        val bluetoothAdapter : BluetoothAdapter? = bluetoothManager.adapter
        val builder = android.app.AlertDialog.Builder(this@FacturaPage)
        val inflater = layoutInflater
        val dialogView:View = inflater.inflate(R.layout.scan_bt,null)
        builder.setCancelable(false)
        builder.setView(dialogView)
        val btlst = dialogView.findViewById<ListView>(R.id.bt_list)
        val dialog = builder.create()
        val pairedDevices:Set<BluetoothDevice> = bluetoothAdapter?.bondedDevices as Set<BluetoothDevice>
        val ADAhere: SimpleAdapter
        var data: MutableList<Map<String?, Any?>?>? = null
        data = ArrayList()
        if(pairedDevices.isNotEmpty()){
            val datanum1 : MutableMap<String?, Any?> = HashMap()
            datanum1["A"] = ""
            datanum1["B"] = ""
            data.add(datanum1)
            for (device in pairedDevices){
                val datanum: MutableMap<String?, Any?> = HashMap()
                datanum["A"] = device.name
                datanum["B"] = device.address
                data.add(datanum)
            }
            val fromwhere = arrayOf("A")
            val viewswhere = intArrayOf(R.id.item_name)
            ADAhere = SimpleAdapter(this@FacturaPage,data,R.layout.item_factura,fromwhere,viewswhere)
            btlst.adapter = ADAhere
            ADAhere.notifyDataSetChanged()
            btlst.onItemClickListener = AdapterView.OnItemClickListener{adapterview, view, position,l ->
                val string = ADAhere.getItem(position) as HashMap<String,String>
                val prnName = string["A"]
                binding.printerName.setText(prnName)
                connectionClass.printer_name = prnName.toString()
                dialog.dismiss()
            }
        }else{
            val value = "No device found"
            Toast.makeText(this, value,Toast.LENGTH_SHORT).show()
            return
        }
        dialog.show()
    }
    fun beginListenForData(){
    try {
        val handler = Handler()
        val delimeter: Byte = 10
        stopWorker = false
        readBufferPosition = 0
        readBuffer = ByteArray(1024)
        workerThread = Thread{
            while (!Thread.currentThread().isInterrupted && !stopWorker){
                try {
                    val bytesAvailable = inputStream!!.available()
                    if(bytesAvailable > 0){
                        val packetBytes = ByteArray(bytesAvailable)
                        inputStream!!.read(packetBytes)
                        for(i in 0 until bytesAvailable){
                            val b = packetBytes[i]
                            if(b == delimeter){
                                val encodedBytes = ByteArray(readBufferPosition)
                                System.arraycopy(
                                    readBuffer,0,
                                    encodedBytes,0,
                                    encodedBytes.size
                                )

                                //specidy ASCII Encoding
                                val data = String(encodedBytes,Charset.forName("US-ASCII"))
                                readBufferPosition = 0
                                //tell the user data were sent to bluetooth printer device
                                handler.post { Log.d("e",data)}
                            } else{
                                readBuffer[readBufferPosition++] = b
                            }

                        }
                    }
                } catch (ex: IOException){
                    stopWorker = true
                }
            }
        }
        workerThread!!.start()
    } catch (e: java.lang.Exception){
        e.printStackTrace()
    }
    }
@SuppressLint("MissingPermission")
    fun InitPrinter(){
        var prname:String = ""
        prname = connectionClass.printer_name.toString()
        val bluetoothManager: BluetoothManager = getSystemService(BluetoothManager::class.java)
        val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
        try {
            if(bluetoothAdapter != null){
                if (!bluetoothAdapter.isEnabled) {
                    val enableBluetoothAdapter = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    btActivityResultLaucher.launch(enableBluetoothAdapter)
                }
            }
            val pairedDevices = bluetoothAdapter?.bondedDevices
            if (pairedDevices != null) {
                if (pairedDevices.size > 0) {
                    if (pairedDevices != null){
                        for (device in pairedDevices){
                            if(device.name == prname){ //note you will need to change this to match the name
                                bluetoothDevice = device
                                val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
                                val m = bluetoothDevice!!.javaClass.getMethod(
                                    "createRfcommSocket", *arrayOf<Class<*>?>(
                                        Int::class.javaPrimitiveType
                                    )
                                )
                                socket = m.invoke(bluetoothDevice,1) as BluetoothSocket
                                bluetoothAdapter?.cancelDiscovery()
                                socket!!.connect()
                                outputStream = socket!!.outputStream
                                inputStream = socket!!.inputStream
                                beginListenForData()
                                break
                            }
                        }
                    }
                }else{
                    value = "No Devices found"
                    Toast.makeText(this, value,Toast.LENGTH_SHORT).show()
                    return
                }
            }

        }catch (ex: java.lang.Exception){
            println("Error 2")
            println(ex.message)
            Toast.makeText(this, "BlueTooth Printer Not Connected",Toast.LENGTH_SHORT).show()
            socket = null
        }
    }

    fun print_inv(){
try {
            var str : String
            var invhdr: String = "EL oferton\n\n"// Header 1 Bussiness Name
            var addr :String = "El oferton"
            var mo:String = "Telf:2523-2637\n\n"//Numeros tercera linea //Telefono
            var gstin: String = "GST no"
            var billno: String = "1"
            var billdt : String = "19-01-2024 20:00"
            var tblno:String ="1"
            var stw: String =""
            var msg: String ="Muchas gracias!"
            var amtwd : String="One Hundred Five Only"

            var wnm = "Self"
            val logName = "Admin"
            var amt = 100.0
            var gst = 5.0
            var gamt = 100.00
            var cmpname: String = "Factura Comercial\n\n" //Second Line

            var textData = StringBuilder()
            var textData1 = StringBuilder()
            var textData2 = StringBuilder()
            var textData3 = StringBuilder()
            var textData4 = StringBuilder()

            if(invhdr.isNotEmpty()){
                textData.append(""" $invhdr""".trimIndent())
            }
            textData.append("""$cmpname""".trimIndent())
            if(mo.isNotEmpty()){
                textData.append("""$mo""".trimIndent())
            }
//            if(gstin.isNotEmpty()){
//                textData.append("""$gstin""".trimIndent())
//            }
            str = ""
            str = String.format("%-14s %17s","N#:$billno","Local:$tblno")

            textData.append("""$str""".trimIndent())
            textData.append("Fecha:$billdt\n")

            textData1.append("--------------------------------")
            textData1.append(
                """
                    Productos
                    """.trimIndent()
            )
            str = ""
            str = String.format("%-11s %9s, %10s","\n\nNombre","Cantidad", "Valor")
            textData1.append(
                """
                    $str
                    """.trimIndent()
            )
            textData1.append("--------------------------------\n")
            //var df = DecimalFormat("0.00")
            var itmname: String
            var rt : String?
            var qty: String
            var amount: String?
            for (i in 0 until 2){
                val price = 10
//                itmname = "Item $i"
                rt = price.toString()
                qty = "Camiseta"
                amount = "400"
//                textData1.append(itmname+"\n")
                str=""
                str = String.format("%-10s %9s, %9s",qty,rt, amount)
                textData1.append(str+"\n")
            }
            textData1.append("--------------------------------\n")
            str = ""

//            str = String.format("%-11s %9s, %10s",wnm,"Total:",amt)
//            textData1.append(str+"\n")
//            str=""
//
//            str = String.format("%-11s %9s, %10s",logName,"Gst", gst)
//            textData1.append(str+"\n")

            str= ""
            str = String.format("%-7s %8s","Total:",gamt)
            textData2.append(str+"\n")




            if(msg.isNotEmpty()){
                textData4.append(msg+"\n")
            }
            textData4.append("Mi inventario\n\n\n\n")
            IntentPrint(textData.toString(),textData1.toString(),textData2.toString(),textData3.toString(),textData4.toString())
    }catch (ex: java.lang.Exception){
        value += "$ex\nExcep IntentPrint \n"
        println("Error 3")
        println(ex.message)

        Toast.makeText(this, value,Toast.LENGTH_SHORT).show()
    }

    }

    fun IntentPrint(txtValue:String,
                    txtValue1:String,
                    txtValue2:String,
                    txtValue3:String,
                    txtValue4:String)
    {
        if(connectionClass.printer_name.trim().isNotEmpty()){
            val buffer = txtValue1.toByteArray()
            val PrintHeader = byteArrayOf(0xAA.toByte(),0x55,2,0)
            PrintHeader[3] = buffer.size.toByte()
            InitPrinter()
            if(PrintHeader.size > 128){
                value += "\nValue is more than 128 size\n"
                println("Error 4")
                Toast.makeText(this,value, Toast.LENGTH_SHORT).show()
            }else{
                try {
                    if(socket!=null){
                        try {
                            val SP = byteArrayOf(0x1B,0x40)
                            outputStream!!.write(SP)
                            Thread.sleep(1000)
                        }catch (e: InterruptedException){
                            e.printStackTrace()
                        }
                        val FONT_1X = byteArrayOf(0x1B,0x21,0x00)
                        outputStream!!.write(FONT_1X)
                        val ALIGN_CENTER = byteArrayOf(0x1B,0x61,1)
                        outputStream!!.write(ALIGN_CENTER)
                        outputStream!!.write(txtValue.toByteArray())
                        val ALIGN_LEFT = byteArrayOf(0x1B,0x61,0)
                        outputStream!!.write(ALIGN_LEFT)
                        outputStream!!.write(txtValue1.toByteArray())
                        val FONT_2X = byteArrayOf(0x1B,0x21,0x30)
                        outputStream!!.write(FONT_2X)
                        outputStream!!.write(txtValue2.toByteArray())
                        outputStream!!.write(FONT_1X)
                        outputStream!!.write(ALIGN_LEFT)
                        outputStream!!.write(txtValue3.toByteArray())
                        outputStream!!.write(ALIGN_CENTER)
                        outputStream!!.write(txtValue4.toByteArray())
                        val FEED_PAPER_AND_CUT = byteArrayOf(0x1D,0x56,66,0x00)
                        outputStream!!.write(FEED_PAPER_AND_CUT)
                        outputStream!!.flush()
                        outputStream!!.close()
                        socket!!.close()
                    }
                }catch (ex: java.lang.Exception){
                    println("Error 1")
                    Toast.makeText(this,ex.message.toString(),Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}