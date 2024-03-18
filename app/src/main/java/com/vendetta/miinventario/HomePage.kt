package com.vendetta.miinventario

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.vendetta.miinventario.adapter.ProductosAdapter
import com.vendetta.miinventario.adapter.VentasAdapter
import com.vendetta.miinventario.data.ProductosProvider
import com.vendetta.miinventario.data.Ventas
import com.vendetta.miinventario.data.VentasProvider
import com.vendetta.miinventario.data.structures.NuevaVentaDatos
import com.vendetta.miinventario.databinding.ActivityHomePageBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.Locale

class HomePage : AppCompatActivity() {
    lateinit var binding : ActivityHomePageBinding
    val DAY_IN_MILISECONDS = 86400000

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomePageBinding.inflate(layoutInflater)
        setContentView(binding.root)


        //Cargar los recycles views
        initRecycleViewVentas()
        initRecycleViewProductos()

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
                println(it)
                //Task Failed
            }
        }catch (e: Exception){
            println("Hay un error")
        }

    }

    private fun initRecycleViewProductos() {
        val recyclerView = binding.recycleProductos
        recyclerView.layoutManager = LinearLayoutManager(this)
        lifecycleScope.launch(Dispatchers.IO) { recyclerView.adapter = ProductosAdapter(ProductosProvider().getUser(applicationContext)) }

    }

    private fun initRecycleViewVentas() {
        val recyclerView = binding.recycleVentas
        recyclerView.layoutManager = LinearLayoutManager(this)
        lifecycleScope.launch(Dispatchers.Main) {recyclerView.adapter = VentasAdapter(VentasProvider().getAllVentas(applicationContext)){onItemClickedVentas(it)} }
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
                    startActivity(this)

                }
            }
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

        //Aqui van las entradas al grafico
        val entries = ArrayList<com.github.mikephil.charting.data.Entry>()
        entries.add(com.github.mikephil.charting.data.Entry(1f,120.00f))
        entries.add(com.github.mikephil.charting.data.Entry(2f,180.00f))
        entries.add(com.github.mikephil.charting.data.Entry(3f,240.00f))
        entries.add(com.github.mikephil.charting.data.Entry(4f,450.00f))
        entries.add(com.github.mikephil.charting.data.Entry(5f,200.00f))
        entries.add(com.github.mikephil.charting.data.Entry(6f,350.00f))
        entries.add(com.github.mikephil.charting.data.Entry(7f,450.00f))

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

    /********************************************************************************************************
     * Show Date Ranger Picker
     * Esta funcion es para desplegar el Date Range Picker y deshabilitarlo mientras se escoje una fecha
     ******************************************************************************************************/
    private fun showDateRangePicker(){
        binding.btnSelectDate.isEnabled = false
        val picker = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText("Selecciona 2 fechas")
            .build()
        picker.show(this.supportFragmentManager,"DATE_PICKER")

        picker.addOnPositiveButtonClickListener {
            val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val firstDate = simpleDateFormat.format(it.first + DAY_IN_MILISECONDS)
            val secondDate = simpleDateFormat.format(it.second + DAY_IN_MILISECONDS)
            binding.dateSelectedText.text = "$firstDate - $secondDate"
            binding.btnSelectDate.isEnabled = true
        }

        picker.addOnNegativeButtonClickListener {
            binding.btnSelectDate.isEnabled = true
        }
        picker.addOnDismissListener {
            binding.btnSelectDate.isEnabled = true
        }
    }
}