package com.example.marcelo.icargo.financeiro

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.marcelo.icargo.R
import com.example.marcelo.icargo.interfaces.FinanceiroInterface
import com.example.marcelo.icargo.model.Financeiro
import kotlinx.android.synthetic.main.item_financeiro.view.*

class FinanceiroAdapter(private val listaMeses: ArrayList<Financeiro>,
                        private val context: Context,
                        private val listenerMes: FinanceiroInterface) : RecyclerView.Adapter<FinanceiroAdapter.ViewHolderFinanceiro>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderFinanceiro {
        val view = LayoutInflater.from(context).inflate(R.layout.item_financeiro, parent, false)
        return ViewHolderFinanceiro(view)
    }

    override fun getItemCount(): Int {
        if (!listaMeses.isEmpty()) {
            return listaMeses.size
        }
        return 0
    }

    override fun onBindViewHolder(holder: ViewHolderFinanceiro, position: Int) {
        holder?.let {
            val mes = listaMeses[position]
            holder.bindView(context, mes)
            holder.itemView.setOnClickListener {
                listenerMes.infoMes(mes)
            }
        }
    }

    class ViewHolderFinanceiro(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindView(context: Context, mes: Financeiro) {
            itemView.tvMesFinanceiro.text = FinanceiroAdapter.mesToString(mes.mes, context)
            itemView.tvLucroFinanceiro.text = context.getString(R.string.label_RS)
            itemView.tvLucroFinanceiro.append(mes.lucro.toString())
            itemView.tvLucroFinanceiro.append(context.getString(R.string.centavos))
        }

    }

    companion object {
        fun mesToString(data: String, context: Context): String {
            val mes = data.substring(6, 7)
            return when (mes) {
                "1" -> context.getString(R.string.label_janeiro)
                "2" -> context.getString(R.string.label_fevereiro)
                "3" -> context.getString(R.string.label_marco)
                "4" -> context.getString(R.string.label_abril)
                "5" -> context.getString(R.string.label_maio)
                "6" -> context.getString(R.string.label_junho)
                "7" -> context.getString(R.string.label_julho)
                "8" -> context.getString(R.string.label_agosto)
                "9" -> context.getString(R.string.label_setembro)
                "10" -> context.getString(R.string.label_outubro)
                "11" -> context.getString(R.string.label_novembro)
                "12" -> context.getString(R.string.label_dezembro)
                else -> ""
            }
        }
    }

}