package com.example.marcelo.icargo.fretes

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import com.example.marcelo.icargo.R
import com.example.marcelo.icargo.utils.ReferenciasFirestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_frete.*
import com.example.marcelo.icargo.model.Cliente
import com.example.marcelo.icargo.model.Frete
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_toolbar.*
import org.jetbrains.anko.longToast
import org.jetbrains.anko.toast
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class FreteActivity : AppCompatActivity() {

    var listaClientes: ArrayList<Cliente>? = null
    var listaNomes: ArrayList<String>? = null
    lateinit var auth: FirebaseAuth
    lateinit var idUsuario: String
    var nome: String? = null
    var endereco: String? = null
    var data: String? = null
    var dataId: String? = null
    var hora: String? = null
    var horaId: String? = null
    var cliente: Cliente? = null
    var frete: Frete? = null
    var FLAG_EDITAR: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_frete)

        initAuth()
        carregaListaClientes()
        initListeners()

        intent.extras?.get("frete")?.let {
            FLAG_EDITAR = true
            frete = it as? Frete
        }

    }

    override fun onStart() {
        super.onStart()
        setToolbar()
    }

    private fun setToolbar(){
        ivHomeToolbar.setOnClickListener { finish() }
        ivIconToolbar.setImageDrawable(resources.getDrawable(R.drawable.ic_package))
    }

    private fun preencheCamposEditar() {
        for(numero in 0..listaClientes?.size!!-1){
            if(listaClientes!![numero].nome.equals(frete?.cliente)){
                spClienteFrete.setSelection(numero)
            }
        }
        etCargaFrete.setText(frete?.carga)
        etValorFrete.setText(""+frete?.valor)
        etEnderecoFrete.setText(frete?.endereco)
        tvDataFrete.setText(frete?.data)
        tvHoraFrete.setText(frete?.hora)
    }

    private fun initAuth(){
        auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null){
            toast(getString(R.string.erro_carregar_seus_dados))
            finish()
            return
        }
        idUsuario = auth.currentUser?.uid.toString()
    }

    private fun initListeners() {
        tvDataFrete.setOnClickListener {
            datePicker()
        }

        tvHoraFrete.setOnClickListener {
            timePicker()
        }

        btnAddFrete.setOnClickListener {
            validaDados()
        }

        spClienteFrete.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                cliente = listaClientes?.get(position)
                etEnderecoFrete?.setText(cliente?.endereco)
            }
        }

    }

    private fun carregaListaClientes() {
        ReferenciasFirestore.clientesCollection(idUsuario).get().addOnCompleteListener {
            if (it.isSuccessful) {
                if (it.result.size() != 0) { //Verifica se existem clientes
                    listaClientes = ArrayList()
                    listaNomes = ArrayList()

                    for (document in it.result) {
                        val cliente = document.toObject<Cliente>(Cliente::class.java)
                        listaClientes?.add(cliente)
                        listaNomes?.add(cliente.nome.toString())
                    }
                    camposLayout()
                    setSpinnerClientes()
                } else {
                    camposLayout()
                }

            } else {
                it.exception?.printStackTrace()
                longToast(getString(R.string.erro_carregar_clientes))
                finish()
            }
        }
    }

    private fun setSpinnerClientes() {
        val array_adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listaNomes)
        array_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spClienteFrete?.adapter = array_adapter
        if (FLAG_EDITAR) { preencheCamposEditar() }
    }

    private fun datePicker() {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        val datePickerdialog = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            var diaFinal = dayOfMonth.toString()
            var mesFinal = (monthOfYear+1).toString()
            if (dayOfMonth<10){
                diaFinal = "0"+dayOfMonth
            }
            if(monthOfYear < 10 ){
                mesFinal = "0"+(monthOfYear+1)
            }
            data = ""+diaFinal+"/"+mesFinal
            tvDataFrete.setText(data)
            dataId = ""+year+""+mesFinal+""+diaFinal
        }, year, month, day)
        datePickerdialog.show()
    }

    private fun timePicker() {
        val cal = Calendar.getInstance()
        val timeSetListener = TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
            cal.set(Calendar.HOUR_OF_DAY, hour)
            cal.set(Calendar.MINUTE, minute)
            hora = SimpleDateFormat("HH:mm").format(cal.time)
            tvHoraFrete.setText(hora)
            horaId = SimpleDateFormat("HHmm").format(cal.time)
        }
        TimePickerDialog(this, timeSetListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
    }

    private fun validaDados(){
        if (etCargaFrete.text.isBlank()){
            avisoValidacao(getString(R.string.aviso_informe_carga), etCargaFrete)
            return
        }
        if (etValorFrete.text.isBlank()){
            avisoValidacao(getString(R.string.aviso_informe_valor), etValorFrete)
            return
        }
        if (tvDataFrete.text.isBlank()){
            avisoValidacao(getString(R.string.aviso_informe_data), tvDataFrete)
            return
        }
        if (tvHoraFrete.text.isBlank()){
            avisoValidacao(getString(R.string.aviso_informe_horario), tvHoraFrete)
            return
        }
        salvarFrete()
    }

    private fun avisoValidacao(mensagem: String, et: EditText){
        et.setError(mensagem)
        et.requestFocus()
    }

    private fun salvarFrete(){
        val idFrete: String
        if (FLAG_EDITAR){
            idFrete = frete?.idFrete.toString()
        }else {
            idFrete = dataId + "" + horaId
        }

        val docFrete: DocumentReference = FirebaseFirestore.getInstance()
                .collection("Fretes")
                .document(idFrete)

        val frete = Frete(
                idUsuario,
                cliente?.idCliente!!,
                idFrete,
                cliente?.nome,
                etEnderecoFrete.text.toString(),
                tvDataFrete.text.toString(),
                tvHoraFrete.text.toString(),
                etCargaFrete.text.toString(),
                Integer.parseInt(etValorFrete.text.toString()),
                false)

        docFrete.set(frete).addOnCompleteListener {
            if (it.isSuccessful){
                limparCampos()
                longToast(getString(R.string.aviso_frete_salvo))
                finish()
            }else{
                longToast(getString(R.string.erro_salvar_frete))
            }
        }
    }

    private fun camposLayout() {
        pbCarregaFrete.visibility = View.GONE
        llCamposFrete.visibility = View.VISIBLE
    }

    private fun limparCampos(){
        etEnderecoFrete.text = null
        tvDataFrete.text = null
        tvHoraFrete.text = null
        etCargaFrete.text = null
        etValorFrete.text = null
    }

    /*override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.empty_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item != null) {
            if (item.itemId == android.R.id.home) {
                finish()
            }
        }
        return true
    }*/

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

}