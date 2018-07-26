package com.example.marcelo.icargo.financeiro

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.marcelo.icargo.R
import com.example.marcelo.icargo.model.Frete
import kotlinx.android.synthetic.main.item_frete.view.*

class FretesFinanceiroAdapter(private val idUsuario: String,
                              private val fretes: List<Frete>,
                              private val context: Context) :
        RecyclerView.Adapter<FretesFinanceiroAdapter.ViewHolderFretes>(
        ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderFretes {
        val view = LayoutInflater.from(context).inflate(R.layout.item_frete_dialog, parent, false)
        return ViewHolderFretes(view)
    }

    override fun getItemCount(): Int {
        if (fretes.isEmpty()) {
            return 0
        }
        return fretes.size
    }

    override fun onBindViewHolder(holder: ViewHolderFretes, position: Int) {
        holder.bindView(fretes[position], context)
    }

    class ViewHolderFretes(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindView(frete: Frete, context: Context) {
            itemView.tvCargaFrete.text = frete.carga
            itemView.tvDataFrete.text = frete.data
            itemView.tvValorFrete.text = context.getString(R.string.label_RS)+" "
            itemView.tvValorFrete.append(frete.valor.toString())
            itemView.tvValorFrete.append(context.getString(R.string.centavos))
        }
    }

}