package com.example.marcelo.icargo.clientes

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.example.marcelo.icargo.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_cliente.*
import com.example.marcelo.icargo.model.Cliente
import kotlinx.android.synthetic.main.layout_toolbar.*
import org.jetbrains.anko.longToast
import org.jetbrains.anko.toast
import java.text.SimpleDateFormat
import java.util.*

class ClienteActivity : AppCompatActivity() {

    var FLAG_EDITAR: Boolean = false
    var cliente: Cliente ? = null
    var auth: FirebaseAuth = FirebaseAuth.getInstance()
    var idUsuario: String? = null
    private var clientesCollection: CollectionReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cliente)

        if (intent.extras == null){
            longToast(getString(R.string.erro_carregar_seus_dados))
            finish()
            return
        }

        intent.extras?.get("cliente")?.let {
            FLAG_EDITAR = true
            cliente = getIntent().getExtras().getSerializable("cliente") as? Cliente
            preencheCampos()
        }

        clientesCollection = FirebaseFirestore.getInstance().collection("Clientes")
        idUsuario = intent.extras.getString("idUsuario")

        initListeners()

    }

    override fun onStart() {
        super.onStart()
        setToolbar()
    }

    private fun setToolbar() {
        ivHomeToolbar.setOnClickListener { finish() }
        ivIconToolbar.setImageDrawable(resources.getDrawable(R.drawable.ic_user))
    }

    private fun initListeners() {
        btnAddCliente.setOnClickListener {
            validarDados()
        }
    }

    private fun preencheCampos(){
        Log.d("INFO_CLIENTE", ""+cliente?.nome)
        etNomeCliente.setText(cliente?.nome)
        etEnderecoCliente.setText(cliente?.endereco)
        etTelefone1Cliente.setText(cliente?.telefone1)
        etTelefone2Cliente.setText(cliente?.telefone2)
    }

    private fun validarDados() {
        if (etNomeCliente.text.toString().isEmpty()) {
            longToast(getString(R.string.aviso_informe_nome_cliente))
            return
        }
        if (FLAG_EDITAR){
            editar()
        }else {
            salvar()
        }
    }

    private fun salvar() {
        try {
            progressBar(true)
            val idCliente = data()

            val clienteMap = mapOf(
                    "nome" to etNomeCliente.text.toString(),
                    "endereco" to etEnderecoCliente.text.toString(),
                    "telefone1" to etTelefone1Cliente.text.toString(),
                    "telefone2" to etTelefone2Cliente.text.toString(),
                    "idCliente" to idCliente,
                    "idUsuario" to idUsuario
            )

            clientesCollection?.document(idCliente)?.set(clienteMap)?.addOnCompleteListener {
                if (it.isSuccessful) {
                    longToast(getString(R.string.aviso_cliente_salvo))
                    progressBar(false)
                    layoutInicial()
                    finish()
                } else {
                    Log.d("INFO_CLIENTES", it.exception.toString())
                    longToast(getString(R.string.erro_padrao))
                    progressBar(false)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            toast(getString(R.string.erro_salvar_dados_cliente))
        }
    }

    private fun editar(){
        try{
            progressBar(true)
            val clienteMap = mapOf(
                    "nome" to etNomeCliente.text.toString(),
                    "endereco" to etEnderecoCliente.text.toString(),
                    "telefone1" to etTelefone1Cliente.text.toString(),
                    "telefone2" to etTelefone2Cliente.text.toString()
            )

            val clienteDB = FirebaseFirestore.getInstance()

            clienteDB.collection("Clientes")
                    .document(cliente?.idCliente.toString())
                    .update(clienteMap)
                    .addOnCompleteListener({

                        if (it.isSuccessful){
                            progressBar(false)
                            longToast(getString(R.string.aviso_cliente_salvo))
                            finish()
                        }else{
                            progressBar(false)
                            it.exception?.printStackTrace()
                            longToast(getString(R.string.erro_salvar_dados_cliente))
                        }

                    })

        }catch (e: Exception){
            longToast(getString(R.string.erro_editar_cliente))
        }
    }

    private fun progressBar(mostrar: Boolean) {
        if (mostrar) {
            btnAddCliente.isEnabled = false
            pbAddCliente.visibility = View.VISIBLE
            return
        }
        pbAddCliente.visibility = View.INVISIBLE
        btnAddCliente.isEnabled = true
    }

    private fun data(): String {
        val dateFormat1 = SimpleDateFormat("yyyyMMddhhmmss")
        val data1 = Date()
        val cal1 = Calendar.getInstance()
        cal1.setTime(data1)
        val dataAgora = cal1.getTime()

        return dateFormat1.format(dataAgora)
    }

    private fun layoutInicial() {
        etNomeCliente?.text = null
        etEnderecoCliente?.text = null
        etTelefone1Cliente?.text = null
        etTelefone2Cliente?.text = null
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
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
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

}