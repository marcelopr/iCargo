package com.example.marcelo.icargo.fretes

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.marcelo.icargo.R
import com.example.marcelo.icargo.interfaces.FreteInterface
import com.example.marcelo.icargo.model.Frete
import kotlinx.android.synthetic.main.item_frete.view.*
import android.graphics.drawable.Animatable
import android.graphics.drawable.AnimationDrawable
import android.os.Handler
import android.support.v4.content.ContextCompat
import com.example.marcelo.icargo.eventbus.posicaoEB
import com.example.marcelo.icargo.utils.ReferenciasFirestore
import com.example.marcelo.icargo.utils.ReturnData
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.android.synthetic.main.dialog_financeiro.view.*
import org.greenrobot.eventbus.EventBus
import org.jetbrains.anko.toast
import android.os.CountDownTimer




class FretesAdapter(private val idUsuario: String,
                    private val fretes: List<Frete>,
                    private val context: Context,
                    private val freteListener: FreteInterface) :
        RecyclerView.Adapter<FretesAdapter.ViewHolderFretes>(
        ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderFretes {
        val view = LayoutInflater.from(context).inflate(R.layout.item_frete, parent, false)
        return ViewHolderFretes(view)
    }

    override fun getItemCount(): Int {
        if (fretes.isEmpty()) {
            return 0
        }
        return fretes.size
    }

    override fun onBindViewHolder(holder: ViewHolderFretes, position: Int) {
        holder.bindView(context, idUsuario, fretes[position], holder.adapterPosition, freteListener)
    }

    class ViewHolderFretes(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bindView(context: Context, idUsuario: String, frete: Frete, posicao: Int, freteListener: FreteInterface) {
            itemView.tvNomeFrete?.text = frete.cliente
            if (!frete.endereco.isNullOrEmpty()){
                itemView.tvEnderecoFrete.visibility = View.VISIBLE
                itemView.tvEnderecoFrete.text = frete.endereco
            }
            itemView.tvCargaFrete.text = frete.carga

            if (frete.data.equals(ReturnData.dataString("dd/MM"))){
                itemView.tvDataFrete?.text = context.getString(R.string.hoje)
                itemView.tvDataFrete?.setTextColor(context.resources.getColor(R.color.verdeEscuro))
            }else {
                itemView.tvDataFrete.text = frete.data
            }

            itemView.tvDataFrete.append("\n${frete.hora}")
            itemView.tvValorFrete.text = frete.valor.toString()

            itemView.setOnClickListener {
                freteListener.infoFrete(frete, posicao)
            }

            /*if (frete.realizado) {
                animCheck(context)
            }*/

            itemView.ivCheckFrete.setOnClickListener {
                itemView.ivCheckFrete.setBackgroundResource(android.R.color.transparent)
                itemView.pbCheckFrete.visibility = View.VISIBLE
                ReferenciasFirestore.freteDocument(frete.idFrete.toString())
                        .update("realizado", !frete.realizado)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                itemView.pbCheckFrete.visibility = View.INVISIBLE
                                if (frete.realizado) {
                                    itemView.ivCheckFrete.setBackgroundResource(R.drawable.shape_ring_frete_realizado)
                                    atualizaFinanceiro(context, frete, false, idUsuario)
                                } else {
                                    atualizaFinanceiro(context, frete, true, idUsuario)
                                }
                                frete.realizado = !frete.realizado
                            } else {
                                itemView.pbCheckFrete.visibility = View.INVISIBLE
                                itemView.ivCheckFrete.setBackgroundResource(R.drawable.shape_ring_frete_realizado)
                                context.toast(context.getString(R.string.erro_atualizar_frete))
                            }
                        }
            }

        }

        fun atualizaFinanceiro(context: Context, frete: Frete, somar: Boolean, idUsuario: String) {

            val mes = frete.idFrete?.substring(0, 4) + "-" + frete.idFrete?.substring(4, 6)

            ReferenciasFirestore.financeiroDocument(idUsuario).get()
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            val document: DocumentSnapshot = it.result

                            if (document.exists()) {

                                var acumulado: Long = if (document[mes] != null) {
                                    document[mes] as Long
                                } else {
                                    0
                                }

                                if (somar) {
                                    acumulado += frete.valor!!
                                } else {
                                    acumulado -= frete.valor!!
                                }

                                //atualizado ganhos mês
                                ReferenciasFirestore.financeiroDocument(idUsuario)
                                        .update(mes, acumulado)
                                        .addOnCompleteListener {

                                            if (it.isSuccessful) {
                                                if (somar) {
                                                    animCheck(context)
                                                    object : CountDownTimer(2200, 100) {
                                                        override fun onTick(millisUntilFinished: Long) {}
                                                        override fun onFinish() {
                                                            val eventBus = posicaoEB(adapterPosition)
                                                            EventBus.getDefault().post(eventBus)
                                                            context.toast(context.getString(R.string.aviso_frete_realizado))
                                                        }
                                                    }.start()
                                                }else{
                                                    itemView.ivCheckFrete.setImageDrawable(null)
                                                }
                                            } else {
                                                context.toast(context.getString(R.string.erro_atualizar_financeiro))
                                            }

                                        }
                            } else {
                                //Primeira vez salvando, cria Referencia
                                criaReferenciaFinanceiro(context, mes, idUsuario, frete)
                            }
                        } else {
                            itemView.pbCheckFrete.visibility = View.INVISIBLE
                            context.toast(context.getString(R.string.erro_atualizar_financeiro))
                        }
                    }

        }

        fun criaReferenciaFinanceiro(context: Context, mes: String, idUsuario: String, frete: Frete) {
            val financeiroMap = mapOf(mes to frete.valor)
            ReferenciasFirestore.financeiroDocument(idUsuario).set(financeiroMap).addOnCompleteListener {
                if (it.isSuccessful) {
                    animCheck(context)
                } else {
                    itemView.pbCheckFrete.visibility = View.INVISIBLE
                    context.toast(context.getString(R.string.erro_atualizar_financeiro))
                }
            }
        }

        fun animCheck(context: Context) {
            itemView.pbCheckFrete.visibility = View.INVISIBLE
            itemView.ivCheckFrete.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.animated_check))
            (itemView.ivCheckFrete.getDrawable() as Animatable).start()
        }

    }

}