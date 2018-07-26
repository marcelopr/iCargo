package com.example.marcelo.icargo.fretes

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.widget.LinearLayout
import android.widget.TextView
import com.example.marcelo.icargo.R
import android.content.pm.PackageManager
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import com.example.marcelo.icargo.utils.ReferenciasFirestore
import com.example.marcelo.icargo.eventbus.posicaoEB
import com.example.marcelo.icargo.model.Frete
import com.example.marcelo.icargo.utils.Mascara
import com.google.firebase.firestore.DocumentSnapshot
import org.greenrobot.eventbus.EventBus
import org.jetbrains.anko.longToast
import org.jetbrains.anko.toast

@SuppressLint("ValidFragment")
class FragmentDialogFrete() : DialogFragment() {

    private var posicaoLista: Int? = null
    lateinit var frete: Frete
    lateinit var menuContext: Context
    private var listenerPhone: PermissaoPhoneListener? = null
    private var listenerLista: AtualizarListaListener? = null

    constructor(frete: Frete, posicaoLista: Int, context: Context) : this() {
        this.frete = frete
        this.posicaoLista = posicaoLista
        this.menuContext = context
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = activity?.layoutInflater?.inflate(R.layout.dialog_opcoes_frete, null)
        val alert = AlertDialog.Builder(activity)
        alert.setView(view)

        val llCancelar = view?.findViewById<LinearLayout>(R.id.llOpcoesCancelar)
        val llEditar = view?.findViewById<LinearLayout>(R.id.llOpcoesEditar)
        val llLigar1 = view?.findViewById<LinearLayout>(R.id.llOpcoesLigar1)
        val llLigar2 = view?.findViewById<LinearLayout>(R.id.llOpcoesLigar2)
        val llExcluir = view?.findViewById<LinearLayout>(R.id.llOpcoesExcluir)
        val pbExcluir = view?.findViewById<ProgressBar>(R.id.pbExcluirFrete)
        val tvCarga = view?.findViewById<TextView>(R.id.tvCargaFreteDialog)
        val tvNome = view?.findViewById<TextView>(R.id.tvNomeClienteDialog)
        val tvLigar1 = view?.findViewById<TextView>(R.id.tvOpcoesLigar1)
        val tvLigar2 = view?.findViewById<TextView>(R.id.tvOpcoesLigar2)

        tvCarga?.text = frete.carga
        tvNome?.text = frete.cliente

        llCancelar?.setOnClickListener {
            this.dismiss()
        }

        llEditar?.setOnClickListener {
            intentEditar(frete)
            this.dismiss()
        }

        llExcluir?.setOnClickListener {
            pbExcluir?.visibility = View.VISIBLE
            excluirFrete(frete)
            this.dismiss()
        }

        ReferenciasFirestore.clienteDocument(frete.idCliente.toString()).get().addOnCompleteListener {
            val documento: DocumentSnapshot = it.result
            if (documento.exists()) {
                val telefone1 = documento["telefone1"].toString()
                if (telefone1.isNotBlank()) {
                    tvLigar1?.text = Mascara.mascTelefone(telefone1)
                    llLigar1?.visibility = View.VISIBLE
                    llLigar1?.setOnClickListener {
                        checarPermissaoPhone(telefone1)
                        this.dismiss()
                    }
                }

                val telefone2 = documento["telefone2"].toString()
                if (telefone2.isNotBlank()){
                    tvLigar2?.text = Mascara.mascTelefone(telefone2)
                    llLigar2?.visibility = View.VISIBLE
                    llLigar2?.setOnClickListener {
                        checarPermissaoPhone(telefone1)
                        this.dismiss()
                    }
                }
            }
        }
        return alert.create()
    }

    private fun intentEditar(frete: Frete) {
        val intentCliente = Intent(activity, FreteActivity::class.java)
        intentCliente.putExtra("frete", frete)
        startActivity(intentCliente)
    }

    private fun ligarCliente(numero: String?) {
        val intent = Intent(Intent.ACTION_CALL)
        intent.data = Uri.parse("tel: " + numero)
        startActivity(intent)
    }

    private fun excluirFrete(frete: Frete) {
        ReferenciasFirestore.freteDocument(frete.idFrete!!).delete()
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        val eventBus = posicaoEB(posicaoLista!!)
                        EventBus.getDefault().post(eventBus)
                        activity?.toast(getString(R.string.aviso_frete_removido))
                        this.dismiss()
                    } else {
                        menuContext.toast(getString(R.string.erro_excluir_frete))
                    }
                }
    }


    /*override fun onStart() {
        super.onStart()

        val dialog = dialog
        if (dialog != null) {
            dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        }
    }*/

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