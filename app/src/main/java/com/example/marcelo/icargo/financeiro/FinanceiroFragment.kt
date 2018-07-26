package com.example.marcelo.icargo.financeiro


import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils

import com.example.marcelo.icargo.R
import com.example.marcelo.icargo.utils.ReferenciasFirestore
import com.example.marcelo.icargo.interfaces.FinanceiroInterface
import com.example.marcelo.icargo.model.Financeiro
import kotlinx.android.synthetic.main.fragment_financeiro.*

class FinanceiroFragment : Fragment() {

    private var listenerMes: FinanceiroInterface? = null
    lateinit var idUsuario: String
    private var adapterFinanceiro: FinanceiroAdapter? = null
    private var listaMeses: ArrayList<Financeiro> = ArrayList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.fragment_financeiro, container, false)

        if (arguments == null) {
            layoutFinanceiro(getString(R.string.erro_carregar_financeiro), false)
            return view
        }

        idUsuario = arguments!!.getString("idUsuario")

        buscaFinanceiro()

        return view
    }

    fun buscaFinanceiro(){
        ReferenciasFirestore.financeiroDocument(idUsuario).get().addOnCompleteListener {
            val documento = it.result
            if (it.isSuccessful){

                if (documento.exists()){
                    documento.data.forEach { mes, lucro ->
                        val mes = Financeiro(mes, lucro as Long)
                        Log.d("INFO_FINANCEIRO", ""+mes.mes.toString())
                        listaMeses?.add(mes)
                    }

                    listaMeses.sortBy { it.mes }
                    listaMeses.reverse()
                    adapterFinanceiro = FinanceiroAdapter(listaMeses, activity!!, listenerMes!!)
                    val layoutManager = LinearLayoutManager(activity)
                    rvFinanceiro?.layoutManager = layoutManager
                    rvFinanceiro?.itemAnimator = DefaultItemAnimator()
                    rvFinanceiro?.adapter = adapterFinanceiro

                    layoutFinanceiro(null, true)
                }else{
                    layoutFinanceiro(getString(R.string.aviso_sem_registros_financeiros), false)
                }

            }else{
                layoutFinanceiro(getString(R.string.erro_carregar_financeiro), false)
            }
        }
    }

    fun layoutFinanceiro(mensagem: String?, mostrar: Boolean) {
        try {
            if (mostrar) {
                tvAvisoFinanceiro?.visibility = View.GONE
                pbFinanceiro.visibility = View.GONE
                rvFinanceiro?.visibility = View.VISIBLE
                return
            }
            rvFinanceiro?.visibility = View.GONE
            pbFinanceiro?.visibility = View.GONE
            tvAvisoFinanceiro?.text = mensagem
            tvAvisoFinanceiro?.visibility = View.VISIBLE
            tvAvisoFinanceiro?.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.from_bottom))
        }catch (e: IllegalStateException){}
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is FinanceiroInterface) {
            listenerMes = context
        } else {
            throw RuntimeException(context.toString() + " implemente a interface RVFinanceiro")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listenerMes = null
    }

}