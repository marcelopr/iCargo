package com.example.marcelo.icargo.clientes

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.example.marcelo.icargo.R
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.example.marcelo.icargo.model.Cliente
import android.content.pm.PackageManager
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.ViewGroup
import com.example.marcelo.icargo.eventbus.posicaoEB
import com.example.marcelo.icargo.utils.Mascara
import org.greenrobot.eventbus.EventBus

@SuppressLint("ValidFragment")
class FragmentDialogCliente() : DialogFragment() {

    private var posicaoLista: Int? = null
    lateinit var cliente: Cliente
    lateinit var menuContext: Context
    private var listenerPhone: PermissaoPhoneListener? = null
    private var listenerLista: AtualizarListaListener? = null

    constructor(cliente: Cliente, posicaoLista: Int, context: Context) : this() {
        this.cliente = cliente
        this.posicaoLista = posicaoLista
        this.menuContext = context
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {


        val view = activity?.layoutInflater?.inflate(R.layout.dialog_opcoes_cliente, null)

        val alert = AlertDialog.Builder(activity)

        alert.setView(view)

        val llCancelar = view?.findViewById<LinearLayout>(R.id.llOpcoesCancelar)
        val llEditar = view?.findViewById<LinearLayout>(R.id.llOpcoesEditar)
        val llLigar1 = view?.findViewById<LinearLayout>(R.id.llOpcoesLigar1)
        val llLigar2 = view?.findViewById<LinearLayout>(R.id.llOpcoesLigar2)
        val llExcluir = view?.findViewById<LinearLayout>(R.id.llOpcoesExcluir)
        val tvNome = view?.findViewById<TextView>(R.id.tvNomeClienteDialog)
        val tvLigar1 = view?.findViewById<TextView>(R.id.tvOpcoesLigar1)
        val tvLigar2 = view?.findViewById<TextView>(R.id.tvOpcoesLigar2)

        tvNome?.text = cliente.nome

        llCancelar?.setOnClickListener {
            this.dismiss()
        }

        llEditar?.setOnClickListener {
            intentEditar(cliente)
            this.dismiss()
        }

        llExcluir?.setOnClickListener {
            excluirCliente(cliente)
            this.dismiss()
        }

        if (!cliente.telefone1.isNullOrBlank()) {
            tvLigar1?.text = Mascara.mascTelefone(cliente.telefone1.toString())
            llLigar1?.visibility = View.VISIBLE
            llLigar1?.setOnClickListener {
                checarPermissaoPhone(cliente.telefone1)
                this.dismiss()
            }
        }

        if (!cliente.telefone2.isNullOrBlank()) {
            tvLigar2?.text = Mascara.mascTelefone(cliente.telefone2.toString())
            llLigar2?.visibility = View.VISIBLE
            llLigar2?.setOnClickListener {
                checarPermissaoPhone(cliente.telefone2)
                this.dismiss()
            }
        }

        return alert.create()
    }

    private fun intentEditar(cliente: Cliente) {
        val intentCliente = Intent(activity, ClienteActivity::class.java)
        intentCliente.putExtra("cliente", cliente)
        startActivity(intentCliente)
        //overridePendingTransition(R.anim.from_bottom, R.anim.to_top);
    }

    private fun ligarCliente(numero: String?) {
        val intent = Intent(Intent.ACTION_CALL)
        intent.data = Uri.parse("tel: " + numero)
        startActivity(intent)
    }

    private fun excluirCliente(cliente: Cliente) {
        val clienteDoc: DocumentReference =
                FirebaseFirestore.getInstance()
                        .collection("Clientes")
                        .document(cliente.idCliente!!)

        clienteDoc.delete()
                .addOnCompleteListener {
                    if (it.isSuccessful) {

                        val eventBus = posicaoEB(posicaoLista!!)
                        EventBus.getDefault().post(eventBus)

                        this.dismiss()
                    } else {
                        Log.d("INFO_DIALOG", "erro ao excluir")
                    }
                }
    }

    private fun checarPermissaoPhone(numero: String?) {
        val permission = ContextCompat.checkSelfPermission(menuContext,
                android.Manifest.permission.CALL_PHONE)

        if (permission != PackageManager.PERMISSION_GRANTED) {
            listenerPhone?.permissao()
        } else {
            ligarCliente(numero)
        }
    }

    interface PermissaoPhoneListener {
        fun permissao()
    }

    interface AtualizarListaListener {
        fun atualizar(posicao: Int?)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)


        if (context is AtualizarListaListener) {
            listenerLista = context
            Log.d("INFO_DIALOG", "1")
        } else {
            Log.d("INFO_DIALOG", "2")
            //  this.dismiss()
        }
        if (context is PermissaoPhoneListener) {
            listenerPhone = context
            Log.d("INFO_DIALOG", "3")
        } else {
            Log.d("INFO_DIALOG", "4")
            //this.dismiss()
        }
    }



}