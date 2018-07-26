package com.example.marcelo.icargo.financeiro

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.widget.LinearLayoutManager
import android.widget.TextView
import com.example.marcelo.icargo.R
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import com.example.marcelo.icargo.model.Financeiro
import android.view.ViewGroup
import android.widget.ProgressBar
import com.example.marcelo.icargo.model.Frete
import com.google.firebase.firestore.FirebaseFirestore
import org.jetbrains.anko.longToast
import java.util.*


@SuppressLint("ValidFragment")
class FragmentDialogFinanceiro() : DialogFragment() {

    lateinit var idUsuario: String
    private lateinit var mes: Financeiro
    private lateinit var menuContext: Context

    constructor(idUsuario: String, mes: Financeiro, context: Context) : this() {
        this.idUsuario = idUsuario
        this.mes = mes
        this.menuContext = context
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = activity?.layoutInflater?.inflate(R.layout.dialog_financeiro_relative, null)
        val alert = AlertDialog.Builder(activity)
        alert.setView(view)

        val tvLucro = view?.findViewById<TextView>(R.id.tvLucro)
        val tvData = view?.findViewById<TextView>(R.id.tvData)
        val tvVoltar = view?.findViewById<TextView>(R.id.tvVoltar)
        val rvFretes = view?.findViewById<RecyclerView>(R.id.rvFretes)
        val pbFretes = view?.findViewById<ProgressBar>(R.id.pbFretes)

        tvLucro?.text = context?.getString(R.string.label_RS)
        tvLucro?.append(" "+mes.lucro.toString())
        tvLucro?.append(context?.getString(R.string.centavos))
        tvData?.text = FinanceiroAdapter.mesToString(mes.mes, context!!)
        tvData?.append(" " + mes.mes.substring(0, 4))

        tvVoltar?.setOnClickListener { this.dismiss() }

        var mesModel: String = mes.mes.substring(6, 7)
        if (mesModel.length == 1) {
            mesModel = "0$mesModel"
        }
        val mesInteresse = mes.mes.substring(0, 4) + mesModel

        FirebaseFirestore.getInstance()
                .collection("Fretes")
                .whereEqualTo("idUsuario", idUsuario)
                .whereEqualTo("realizado", true)
                .orderBy("idFrete")
                .startAt(mesInteresse)
                .endAt(mesFinal())
                .get()
                .addOnCompleteListener {

                    if (it.isSuccessful) {

                        if (it.result.size() == 0) {
                            erroDialog()
                            Log.d("INFO_FINANCEIRO", "0 respostas")
                        } else {
                            try {

                                val listaFretes: ArrayList<Frete> = ArrayList()

                                for (document in it.result) {
                                    listaFretes.add(document.toObject<Frete>(Frete::class.java))
                                }

                                listaFretes.reverse()
                                val fretesAdapter = FretesFinanceiroAdapter(idUsuario, listaFretes, activity!!)
                                val layoutManager = LinearLayoutManager(menuContext)
                                rvFretes?.layoutManager = layoutManager as RecyclerView.LayoutManager?
                                rvFretes?.adapter = fretesAdapter
                                pbFretes?.visibility = View.GONE
                                rvFretes?.visibility = View.VISIBLE

                            } catch (e: Exception) {
                                e.printStackTrace()
                                erroDialog()
                            }
                        }

                    } else {
                        it.exception?.printStackTrace()
                        erroDialog()
                    }

                }

        return alert.create()
    }

    private fun mesFinal(): String {

        var mesModel: String = mes.mes.substring(6, 7)
        if (mesModel.length == 1) {
            mesModel = "0$mesModel"
        }

        val mesInt: Int

        mesInt = if (mesModel.equals("12")) {
            1
        } else {
            mesModel.toInt() + 1
        }

        if (mesInt < 10) {
            return "${mes.mes.substring(0, 4)}0$mesInt"
        } else {
            return "${mes.mes.substring(0, 4)}$mesInt"
        }
    }

    private fun erroDialog() {
        menuContext.longToast(getString(R.string.erro_carregar_fretes))
        dismiss()
    }

    override fun onStart() {
        super.onStart()

        val dialog = dialog
        if (dialog != null) {
            dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        }
    }

}