package com.vendetta.miinventario

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresApi
import com.google.android.gms.ads.AdRequest
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_finanzas_home.*
import java.util.*
import kotlin.collections.ArrayList

class FinanzasHome : AppCompatActivity() {
    private var daySelected = Calendar.getInstance().get(Calendar.DAY_OF_MONTH).toString()
    private var monthSelected = (Calendar.getInstance().get(Calendar.MONTH)+1).toString()
    private var yearSelected = Calendar.getInstance().get(Calendar.YEAR).toString()
    private var database = ""
    private var list = arrayListOf<DataSnapshot>()
    private var listMes = arrayListOf<String>("Enero","Febrero","Marzo","Abril","Mayo","Junio","Julio","Agosto",
    "Septiembre","Octubre","Noviembre","Diciembre")
    val fireData = Firebase.firestore
    var ventasYear = "0"; var gananciasYear = "0"; var ventasMonth = "0"; var gananciasMonth = "0"; var ventasToday ="0"; var gananciasToday="0"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_finanzas_home)
        banner_finanzas.loadAd(AdRequest.Builder().build())
        loadPreferences()
        initCalendar()
        tipDay()


        showGanancias.setOnClickListener {
            if (showGanancias.isChecked){
                infoGanancias.visibility = View.GONE
            }
            else{
                infoGanancias.visibility = View.VISIBLE
            }
        }

    }

    private fun tipDay() {
        var list = arrayOf(
            "\"El dinero no lo es todo, pero la falta de dinero lo es.\"",
            "\"Un centavo ahorrado es un centavo ganado.\"",
            "\"No gastes lo que no tienes.\"",
            "\"La paciencia es la clave para el éxito financiero a largo plazo.\"",
            "\"No pongas todos los huevos en la misma canasta.\"",
            "\"El dinero no crece en los árboles.\"",
            "\"Las pequeñas inversiones hoy pueden convertirse en grandes ganancias mañana.",
            "\"El interés compuesto es el octavo milagro del mundo.\"",
            "\"Un buen presupuesto es la base de una buena planificación financiera.\"",
            "\"El ahorro es una disciplina, no un sacrificio.\"",
            "\"El conocimiento financiero es la clave para tomar decisiones informadas.\"",
            "\"No te endeudes más de lo que puedes permitirte pagar.\"",
            "\"El éxito financiero no es cuestión de suerte, es cuestión de hábitos.\"",
            "\"El tiempo es tu mejor amigo o tu peor enemigo en términos de inversión.\"",
            "\"La diversificación es esencial para minimizar el riesgo en la inversión.\"",
            "\"El dinero es una herramienta, úsala sabiamente.\"",
            "\"El control de gastos es la clave para mantenerse dentro del presupuesto.\"",
            "\"La educación financiera es el camino hacia la independencia financiera.\"",
            "\"Aprende a decir 'no' a los gastos innecesarios, es una forma efectiva de ahorrar dinero.\"",
            "\"La educación financiera es la clave para tomar decisiones informadas en las finanzas.\"",
            "\"La mejor forma de predecir tu futuro financiero es creándolo.\"",
            "\"El dinero no es el fin, es el medio para alcanzar tus metas.\"",
            "\"Invierte en lo que conoces, no te metas en inversiones que no entiendes.\"",
            "\"No hay atajos para la riqueza, es un camino largo y constante.\"",
            "\"Las deudas se pagan, el crédito se gana.\"",
            "\"El ahorro no es sacrificio, es inversión en tu futuro.\"",
            "\"El miedo es tu peor enemigo en las finanzas, no dejes que te paralice.\"",
            "\"El éxito financiero no es cuánto ganas, sino cuánto guardas.\"",
            "\"Invierte en ti mismo, tus conocimientos y habilidades son tu mejor activo.\"",
            "\"Gasta menos de lo que ganas, esa es la clave para tener finanzas saludables.\"",
            "\"El tiempo es dinero, pero el dinero no puede comprar tiempo.\"",
            "\"La deuda es una losa que te arrastra hacia abajo.\"",
            "\"Invierte en ti mismo, tus conocimientos y habilidades son tu mejor activo.\"",
            "\"Gasta menos de lo que ganas, esa es la clave para tener finanzas saludables.\"",
            "\"El tiempo es dinero, pero el dinero no puede comprar tiempo.\"",
            "\"La deuda es una losa que te arrastra hacia abajo.\"",
            "\"El éxito financiero no es un accidente, es el resultado de una planificación cuidadosa y de una ejecución constante.\"",
            "\"La paciencia es una virtud en las finanzas, no trates de hacerte rico de la noche a la mañana.\"",
            "\"No pongas todos tus huevos en una canasta, diversifica tus inversiones.\"",
            "\"No tienes que ser rico para empezar a invertir, pero debes empezar a invertir para ser rico.\"",
            "\"El ahorro no es sacrificio, es inversión en tu futuro.\"",
            "\"El miedo es tu peor enemigo en las finanzas, no dejes que te paralice.\"",
            "\"El éxito financiero no es cuánto ganas, sino cuánto guardas.\"",
            "\"Las pequeñas cosas marcan la diferencia en las finanzas, ahorra en los gastos pequeños y verás grandes resultados.\"",
            "\"Invierte en lo que conoces, no te metas en inversiones que no entiendes.\"",
            "\"No hay atajos para la riqueza, es un camino largo y constante.\"",
            "\"Las deudas se pagan, el crédito se gana.\"",
            "\"La mejor forma de predecir tu futuro financiero es creándolo.\"",
            "\"El dinero no es el fin, es el medio para alcanzar tus metas.\"",
            "\"El éxito financiero es una cuestión de hábitos, desarrolla buenos hábitos financieros y tendrás éxito.\"",
            "\"Aprende a decir 'no' a los gastos innecesarios, es una forma efectiva de ahorrar dinero.\"",
            "\"La educación financiera es la clave para tomar decisiones informadas en las finanzas.\"",
            "\"No te preocupes por los pequeños reveses financieros, enfócate en el panorama general a largo plazo.\"",
            "\"Las mejores inversiones son aquellas que haces en ti mismo y en tu educación financiera.\"",
            "\"Ahorra un poco cada día, y verás que sumado con el tiempo, se convierte en una cantidad significativa.\"",
            "\"El dinero no es todo, pero te da opciones.\"",
            "\"No existe una fórmula mágica para el éxito financiero, pero hay principios básicos que pueden ayudarte a alcanzarlo.\"",
            "\"La clave para alcanzar la independencia financiera es gastar menos de lo que ganas.\"",
            "\"No te endeudes por bienes que no te generen ingresos pasivos.\"",
            "\"No hay edad para empezar a planificar tu futuro financiero, mientras antes mejor.\"",
            "\"No te dejes llevar por las emociones al tomar decisiones financieras, siempre analiza las opciones con la cabeza fría.\"",
            "\"No dejes que el miedo te impida tomar decisiones financieras inteligentes.\"",
            "\"Siempre ten una reserva de emergencia, la vida puede ser impredecible.\"",
            "\"La constancia y la disciplina son claves para lograr la libertad financiera.\"",
            "\"No te compares con los demás, cada uno tiene su propia situación financiera.\"",
            "\"El ahorro es una actitud, no una cantidad de dinero.\"",
            "\"Aprende a distinguir entre un gasto y una inversión, la segunda te hará crecer tu patrimonio.\"",
            "\"No confundas el tener dinero con el ser rico, la verdadera riqueza es el disfrute de la vida sin preocupaciones.\"",
            "\"No te pierdas en el día a día, siempre ten en mente tus objetivos financieros a largo plazo.\"",
            "\"La inversión más rentable es la que haces en tu educación y formación profesional.\"",
            "\"No te endeudes por bienes que no puedes pagar con tus ingresos actuales.\""
        )
        textView27.text = list.random().toString()
    }

    override fun onStart() {
        super.onStart()
        loadInfoDatabase()
        tipDay()
    }

    fun loadInfoDatabase(){
        readFireStore()

    }



    fun readFireStore(){

        fireData.collection("db1").document(database).collection("Finanzas").document(yearSelected).addSnapshotListener { it, error ->

            ventasYear = (it?.data?.get("ventas")?:"0").toString()
            gananciasYear = (it?.data?.get("ganancias")?:"0").toString()

            fireData.collection("db1").document(database).collection("Finanzas").document(yearSelected).collection(monthSelected).document("ventas").addSnapshotListener { it, error ->
                    ventasMonth = it?.data?.get("ventas").toString()
                    if (ventasMonth == "null") { ventasMonth = "0" }

                    fireData.collection("db1").document(database).collection("Finanzas").document("$yearSelected/$monthSelected/$daySelected").addSnapshotListener { it, error ->
                        ventasToday = it?.data?.get("ventas").toString()
                        if(ventasToday == "null"){ventasToday="0"}

                        fireData.collection("db1").document(database).collection("Finanzas").document(yearSelected).collection(monthSelected).document("ganancias").addSnapshotListener { it, error ->
                            gananciasMonth = it?.data?.get("ganancias").toString()
                            if(gananciasMonth == "null"){gananciasMonth = "0"}

                            fireData.collection("db1").document(database).collection("Finanzas").document("$yearSelected/$monthSelected/$daySelected").addSnapshotListener { it, error ->
                                gananciasToday = it?.data?.get("ganancias").toString()
                                if(gananciasToday == "null"){gananciasToday = "0"}
                                readInfo()
                            }

                        }

                    }

                }
        }


    }

    @SuppressLint("SetTextI18n")
    fun readInfo(){

        fechaHoyFinanzas.text = "$daySelected/$monthSelected/$yearSelected"
        fechaMesFinanzas.text = "${listMes[(monthSelected.toInt()-1)]} $yearSelected"
        fechaYearFinanzas.text = yearSelected





        ventasHoyText.text = "$${ventasToday?:0.toString()}"
        ventasMesText.text =  "$${ventasMonth?:0.toString()}"
        ventasYearText.text = "$${ventasYear?:0.toString()}"

        ventasGananciasToday.text = "$${gananciasToday?:"0".toString()}"
        ventasGanaciasMes.text = "$${gananciasMonth?:"0".toString()}"
        ventasGanaciasYear.text = "$${gananciasYear?:"0".toString()}"

    }

    fun initCalendar() {
        var myCalendar = Calendar.getInstance()
        var datePicker = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
            myCalendar.set(Calendar.YEAR, year)
            myCalendar.set(Calendar.MONTH, month)
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            daySelected = myCalendar.get(Calendar.DAY_OF_MONTH).toString()
            monthSelected = (myCalendar.get(Calendar.MONTH) + 1).toString()
            yearSelected = myCalendar.get(Calendar.YEAR).toString()

            readFireStore()

        }

        btnFinanzasCalendar.setOnClickListener {

            DatePickerDialog(
                this, datePicker, myCalendar.get(android.icu.util.Calendar.YEAR),
                myCalendar.get(android.icu.util.Calendar.MONTH), myCalendar.get(android.icu.util.Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    fun loadPreferences(){
        getSharedPreferences("login_prefs", Context.MODE_PRIVATE).apply {
            database = this.getString("database","null").toString()
        }

        /*
        getSharedPreferences("login_prefs", Context.MODE_PRIVATE).apply {
            isAdmin = this.getBoolean("isAdmin",false)
        }

        getSharedPreferences("login_prefs", Context.MODE_PRIVATE).apply {
            fullname = this.getString("name","null").toString()
        }*/
    }
}