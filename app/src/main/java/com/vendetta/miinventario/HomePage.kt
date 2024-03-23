package com.vendetta.miinventario

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.vendetta.miinventario.adapter.ProductosAdapter
import com.vendetta.miinventario.adapter.VentasAdapter
import com.vendetta.miinventario.data.Productos
import com.vendetta.miinventario.data.ProductosProvider
import com.vendetta.miinventario.data.Ventas
import com.vendetta.miinventario.data.VentasProvider
import com.vendetta.miinventario.data.database.InventarioDatabase
import com.vendetta.miinventario.data.database.InventarioDatabase.Companion.getDatabase
import com.vendetta.miinventario.data.database.entities.FinanzasEntity
import com.vendetta.miinventario.data.structures.NuevaVentaDatos
import com.vendetta.miinventario.databinding.ActivityHomePageBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomePage : AppCompatActivity() {
    lateinit var binding : ActivityHomePageBinding
    lateinit var listaVentas: List<Ventas>
    private lateinit var miAdapterVentas: VentasAdapter
    private lateinit var miAdapterProductos: ProductosAdapter
    private lateinit var database : InventarioDatabase
    val DAY_IN_MILISECONDS = 86400000
    var date = Date().toString()

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomePageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        database = getDatabase(this)
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        date = format.format(Date()).toString()

        //Cargar los recycles views
        initRecycleViewVentas()
        initRecycleViewProductos()
        initTextWatchers()
        initFinanzas(date,date)

        //Cuando sea seleccionado el boton de salir
        binding.exitBtn.setOnClickListener {
            //Borrar los Login Prefs
            getSharedPreferences("login_prefs", MODE_PRIVATE).edit().clear().apply()
            Intent(this,MainActivity::class.java).apply { startActivity(this) }
        }

        //Cuando el boton de seleccionar es presionado desplegar date ranger picker
        binding.btnSelectDate.setOnClickListener {
            //Mostrar Date Picker
            showDateRangePicker()
            }
        //Mostrar linear chart
        configLinearChart()

        binding.addFloatingBtn.setOnClickListener {
            Intent(this,NuevaVenta::class.java).apply {startActivity(this)}
        }
        binding.btnAddVenta.setOnClickListener {
            Intent(this,NuevaVenta::class.java).apply {startActivity(this)}
        }

        binding.btnAddProducto.setOnClickListener {
            Intent(this,NuevoProducto::class.java).apply { startActivity(this) }
        }

        binding.btnBarCode.isClickable = true
        binding.btnBarCode.setOnClickListener {
            initScanner()
        }

    }

    private fun initFinanzas(startDate: String, endDate: String) {
        lifecycleScope.launch(Dispatchers.IO) {
        val listFinanzas = database.finanzasDao.getFinanzasbyDate(
            startDate,
            endDate
        )
            var totalVentas = 0.0f
            var totalProfit = 0.0f
            for(finanzas in listFinanzas){
                totalVentas += finanzas.Total
                totalProfit += finanzas.TotalGanancias
            }
            withContext(Dispatchers.Main){
                binding.totalVentasSelected.setText("$$totalVentas")
                binding.gananciasText.setText("Ganancias: ▲$$totalProfit")
            }
        }
    }

    private fun initTextWatchers() {
        //Ventas Text Watchers
        binding.searchVentaText.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Este método se invoca antes de que el texto cambie
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Este método se invoca cuando el texto está cambiando
            }

            override fun afterTextChanged(s: Editable?) {
                // Este método se invoca después de que el texto ha cambiado
                miAdapterVentas.filter.filter(s.toString())
                miAdapterVentas.notifyDataSetChanged()

            }

        })

        binding.searchProductoText.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Este método se invoca antes de que el texto cambie
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Este método se invoca cuando el texto está cambiando
            }

            override fun afterTextChanged(s: Editable?) {
                // Este método se invoca después de que el texto ha cambiado
                miAdapterProductos.filter.filter(s.toString())
                miAdapterProductos.notifyDataSetChanged()

            }

        })
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onStart() {
        super.onStart()
        //Cargar los recycles views
        binding.recycleVentas.adapter?.notifyDataSetChanged()
        binding.recycleProductos.adapter?.notifyDataSetChanged()
    }

    private fun initScanner() {
        try {
            val options =
                GmsBarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                    .allowManualInput().enableAutoZoom().build()
            val scanner = GmsBarcodeScanning.getClient(this, options)

            scanner.startScan().addOnSuccessListener { barcode ->
                val rawValue: String? = barcode.rawValue
                binding.searchProductoText.setText(rawValue)
            }.addOnCanceledListener {
                //Task cancelled
            }.addOnFailureListener {
                println(it.message)
                //Task Failed
            }
        }catch (e: Exception){
            println("Hay un error")
        }

    }

    private fun initRecycleViewProductos() {
        val recyclerView = binding.recycleProductos
        recyclerView.layoutManager = LinearLayoutManager(this)

        lifecycleScope.launch(Dispatchers.IO) {
            miAdapterProductos = ProductosAdapter(ProductosProvider().getUser(applicationContext)){onItemClickedProducto(it)}
            recyclerView.adapter = miAdapterProductos
        }

    }

    private fun initRecycleViewVentas() {
        val recyclerView = binding.recycleVentas
        recyclerView.layoutManager = LinearLayoutManager(this)
        lifecycleScope.launch(Dispatchers.Main) {
            listaVentas = VentasProvider().getAllVentas(applicationContext)
            miAdapterVentas = VentasAdapter(listaVentas){onItemClickedVentas(it)}
            recyclerView.adapter = miAdapterVentas

        }
    }

    /**************************************************************************************
     * On Item Clicked Ventas
     * When a recycle view item is clicked display this function
     ****************************************************************************************/
    fun onItemClickedVentas(ventas: Ventas) {
        lifecycleScope.launch(Dispatchers.IO) {
            val ventaList = VentasProvider().getVentabyId(applicationContext,ventas.factura) as ArrayList<NuevaVentaDatos>
            println(ventaList)
            withContext(Dispatchers.Main){
                Intent(applicationContext,FacturaPage::class.java).apply {
                    putExtra("arrayVenta",ventaList)
                    putExtra("factura_number",ventas.factura)
                    putExtra("totalPrice",ventas.totalVenta)
                    putExtra("isView",true)
                    startActivity(this)

                }
            }
        }


    }

    /**************************************************************************************
     * On Item Clicked Producto
     * When a recycle view item is clicked display this function
     ****************************************************************************************/
    fun onItemClickedProducto(producto: Productos) {

        Intent(this,NuevoProducto::class.java).apply {
            putExtra("isEdit",true)
            putExtra("arrayProducto",producto)
            startActivity(this)
        }

    }

    /************************************************************
     * ConfigLinearChart
     * Esta es la funcion para configurar el grafico de lineas
     **************************************************************/
    private fun configLinearChart() {
        //GET THE FONT
        val typeface = ResourcesCompat.getFont(this,R.font.mairyextrabold)
        //Obtener el linechart id desde el layout
        val lineChart = binding.linearChart
        var xPosition = 1f

        lifecycleScope.launch(Dispatchers.IO) {
            var listaFinanzas = database.finanzasDao.getFinanzasbyGroup().toMutableList()
            //Si al grafico le faltan elementos agregar variables vacias
            if(listaFinanzas.size < 7){
                for (i in listaFinanzas.size until 7){
                    listaFinanzas.add(FinanzasEntity(Total = 0.0f))
                }
            }
            //Si al grafico le sobran elementos
            if(listaFinanzas.size > 7){
                listaFinanzas = listaFinanzas.takeLast(7).toMutableList()
            }
            println(listaFinanzas)
        //Aqui van las entradas al grafico
        val entries = ArrayList<com.github.mikephil.charting.data.Entry>()
            for(element in listaFinanzas) {
                entries.add(com.github.mikephil.charting.data.Entry(xPosition, element.Total))
                xPosition++
//                entries.add(com.github.mikephil.charting.data.Entry(2f, 180.00f))
//                entries.add(com.github.mikephil.charting.data.Entry(3f, 240.00f))
//                entries.add(com.github.mikephil.charting.data.Entry(4f, 450.00f))
//                entries.add(com.github.mikephil.charting.data.Entry(5f, 200.00f))
//                entries.add(com.github.mikephil.charting.data.Entry(6f, 350.00f))
//                entries.add(com.github.mikephil.charting.data.Entry(7f, 450.00f))
            }

            withContext(Dispatchers.Main){

        //Aqui comienza la configuracion
        val dataSet = LineDataSet(entries, "Ventas") // Agrega entradas a tu conjunto de datos
       //Configuracion para la linea
        dataSet.color = Color.GREEN //Color de la linea
        dataSet.valueTextColor = Color.WHITE //Color del texto de la linea
        dataSet.setDrawValues(true) //Para la animacion
        dataSet.valueTextSize = 10f // El tamaño de las letras de la liena
        dataSet.valueTypeface = typeface // La fuente de las letras de la liena
        dataSet.setCircleColor(Color.WHITE) //El color del borde el circulo de la liena
        dataSet.circleHoleColor = Color.GREEN //El color del circulo de la linea
        val lineData = LineData(dataSet) //Guardar toda esa configuracion para desplegar el chart
        lineChart.data = lineData //Introducir la configuracion en el chart

        //Esta es la configuracion para la descripcion del grafico que se encuentra esquina inferior derecha
        lineChart.description = Description().apply {
            this.text = "Ultimos 7 dias"; //Texto
            this.textColor = Color.WHITE; //Color
            this.typeface = typeface; //Fuente
            this.textSize = 20f; //Tamaño de la letra
            this.yOffset = -20f //Posicion
        }
        //Esta es la configuracion para el texto que se encuentra en la esquina inferior derecha que contiene el color de la linea
        lineChart.legend.textColor = Color.WHITE //Color del texto
        lineChart.legend.typeface = typeface //Fuente
        lineChart.legend.textSize = 15f //Tamaño de la letra

        //Esta es la configuracion para las letras de la parte superior del chart
        lineChart.xAxis.textColor = Color.WHITE //Color
        lineChart.xAxis.typeface = typeface //Fuente

        //Esta es la configuracion para las letras del lado izquierdo
        lineChart.axisLeft.textColor = Color.WHITE //Color
        lineChart.axisLeft.typeface = typeface //Fuente

        //Esta es la configuracion para las letras del lado derecho
        lineChart.axisRight.textColor = Color.WHITE
        lineChart.axisRight.typeface = typeface

        //Esta es la configuracion del chart en la parte externa
        lineChart.setExtraOffsets(10f, 20f, 10f, 10f) //Padding
        lineChart.animateX(2000) //Animacion desplegandose el chart

        //Esta es una configuracion para remplazar los nombres de las entradas X position
        val stringArrays = listOf<String>("","Dia 1","Dia 2","Dia 3","Dia 4","Dia 5","Dia 6","Dia 7")
        lineChart.xAxis.valueFormatter= IndexAxisValueFormatter(stringArrays)

        lineChart.invalidate() // Refresca el gráfico
            }
        }
    }

    /********************************************************************************************************
     * Show Date Ranger Picker
     * Esta funcion es para desplegar el Date Range Picker y deshabilitarlo mientras se escoje una fecha
     ******************************************************************************************************/
    private fun showDateRangePicker(){
        binding.btnSelectDate.isEnabled = false
        val today = MaterialDatePicker.todayInUtcMilliseconds()
        val constraintsBuilder = CalendarConstraints.Builder()
        constraintsBuilder.setEnd(today)
        val picker = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText("Selecciona 2 fechas")
            .build()
        picker.show(this.supportFragmentManager,"DATE_PICKER")

        picker.addOnPositiveButtonClickListener {
            //If date selected is after today date
            if (it.second!! > today) {
                Toast.makeText(this, "No puedes seleccionar una fecha futura", Toast.LENGTH_SHORT).show()
            }
            else {
                //Convert date
                val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val simpleYearFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                var startDate = it.first + DAY_IN_MILISECONDS
                var endDate = it.second + DAY_IN_MILISECONDS
                val firstDate = simpleDateFormat.format(startDate)
                val secondDate = simpleDateFormat.format(endDate)
                initFinanzas(simpleYearFormat.format(startDate),simpleYearFormat.format(endDate))
                binding.dateSelectedText.text = "$firstDate - $secondDate"
                binding.btnSelectDate.isEnabled = true
            }
        }

        picker.addOnNegativeButtonClickListener {
            binding.btnSelectDate.isEnabled = true
        }
        picker.addOnDismissListener {
            binding.btnSelectDate.isEnabled = true
        }
    }
}