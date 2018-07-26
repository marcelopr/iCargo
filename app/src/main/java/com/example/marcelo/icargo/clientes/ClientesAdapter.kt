package com.example.marcelo.icargo.clientes

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.marcelo.icargo.R
import com.example.marcelo.icargo.interfaces.ClienteInterface
import kotlinx.android.synthetic.main.item_cliente.view.*
import com.example.marcelo.icargo.model.Cliente
import com.example.marcelo.icargo.utils.Mascara

//private var clienteListener: ClienteInterface? = null
private var ultimaLetra: String? = null

class ClientesAdapter(private val clientes: List<Cliente>,
                      private val context: Context,
                      private val clienteListener: ClienteInterface ) : RecyclerView.Adapter<ClientesAdapter.ViewHolderClientes>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderClientes {
        val view = LayoutInflater.from(context).inflate(R.layout.item_cliente, parent, false)
        return ViewHolderClientes(view)
    }

    override fun getItemCount(): Int {
        if (!clientes.isEmpty()) {
            return clientes.size
        }
        return 0
    }

    override fun onBindViewHolder(holder: ViewHolderClientes, position: Int) {
        holder?.let {
            val cliente = clientes[position]
            mostraLetra(cliente.nome, holder)
            holder.bindView(cliente)
            holder.itemView.setOnClickListener({
                clienteListener.infoCliente(cliente, holder.adapterPosition)
            })
        }
    }

    fun mostraLetra(nome: String?, holder: ViewHolderClientes) {
        if (!nome?.substring(0, 1)?.toUpperCase().equals(ultimaLetra)) {
            holder.itemView.tvLetraPaciente.visibility = View.VISIBLE
            holder.itemView.tvLetraPaciente?.text = nome?.substring(0, 1)
        } else {
            holder.itemView.tvLetraPaciente.text = null
            holder.itemView.tvLetraPaciente.visibility = View.GONE
        }
        ultimaLetra = nome?.substring(0, 1)?.toUpperCase()
    }

    class ViewHolderClientes(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindView(cliente: Cliente) {
            itemView.tvNomeCliente.text = cliente.nome
            if (cliente.endereco.toString().isBlank()){
                itemView.tvEnderecoCliente.text = "Endereço N/I"
            }else{
                itemView.tvEnderecoCliente.text = cliente.endereco
            }
            if (cliente.telefone1.isNullOrEmpty() && cliente.telefone2.isNullOrEmpty()){
                itemView.tvTelefoneCliente.text = "Telefone N/I"
            }else {
                itemView.tvTelefoneCliente.text = Mascara.mascTelefone(cliente.telefone1.toString())
                if (!cliente.telefone2.isNullOrBlank()) {
                    itemView.tvTelefoneCliente.append(" / ${Mascara.mascTelefone(cliente.telefone2.toString())} ")
                }
            }
        }

    }

}
