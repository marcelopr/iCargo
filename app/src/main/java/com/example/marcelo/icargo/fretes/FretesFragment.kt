package com.example.marcelo.icargo.fretes


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils

import com.example.marcelo.icargo.R
import com.example.marcelo.icargo.eventbus.nomeEB
import com.example.marcelo.icargo.utils.ReferenciasFirestore
import com.example.marcelo.icargo.eventbus.posicaoEB
import com.example.marcelo.icargo.interfaces.FreteInterface
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_fretes.*
import com.example.marcelo.icargo.model.Frete
import com.example.marcelo.icargo.utils.ReturnData
import com.google.firebase.firestore.*
import kotlinx.android.synthetic.main.layout_fretes_hoje.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.jetbrains.anko.longToast
import org.jetbrains.anko.toast

class FretesFragment : Fragment() {

    lateinit var auth: FirebaseAuth
    private var listaFretes: ArrayList<Frete> = ArrayList()
    private var fretesAdapter: FretesAdapter? = null
    lateinit var idUsuario: String
    private var listenerFrete: FreteInterface? = null
    private var FRETES_HOJE : Int = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        Log.d("INFO_FRETES", "create")

        val view: View = inflater.inflate(R.layout.fragment_fretes, container, false)

        auth = FirebaseAuth.getInstance()
        idUsuario = auth.currentUser!!.uid

        EventBus.getDefault().register(this)

        this.setRecyclerView() // se colocar no fretesHoje, atualiza a cada evento, caso nao atualize sozinho
        //this.carregaFretes(ReferenciasFirestore.fretesCollection(idUsuario), false)

        return view
    }

    override fun onStart() {
        super.onStart()
        initListeners()
        limpar()
        fretesHoje(ReferenciasFirestore.fretesCollection(idUsuario), false)
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is FreteInterface) {
            listenerFrete = context
        } else {
            throw RuntimeException(context.toString() + " implemente a interface Frete")
        }
    }

    @Subscribe
    fun onEvent(nomeEB: nomeEB) {
        try {
            limpar()
            fretesHoje(ReferenciasFirestore.fretesCollection(idUsuario).whereEqualTo("cliente", nomeEB.nome), true)
        } catch (e: Exception) {
            activity?.longToast(getString(R.string.erro_carregar_fretes))
        }
    }

    @Subscribe
    fun onEvent(posicaoEB: posicaoEB) {
        try {
            removerFrete(posicaoEB.posicao)
        } catch (e: Exception) {
            layoutFretes(getString(R.string.erro_atualizar_fretes), false)
        }
    }

    fun removerFrete(posicao: Int) {
        listaFretes.removeAt(posicao)
        fretesAdapter?.notifyItemRemoved(posicao)
        fretesAdapter?.notifyItemRangeChanged(posicao, listaFretes.size)
        if (listaFretes.isEmpty()) {
            layoutFretes(getString(R.string.aviso_sem_fretes), false)
        }
    }

    override fun onDetach() {
        super.onDetach()
        listenerFrete = null
        fretesAdapter = null
    }

    private fun initListeners() {
        fabFretes?.setOnClickListener {
            val intentFrete = Intent(activity, FreteActivity::class.java)
            startActivity(intentFrete)
            activity?.overridePendingTransition(R.anim.from_bottom, R.anim.to_top)
        }
    }

    private fun setRecyclerView() {
        fretesAdapter = FretesAdapter(idUsuario, listaFretes, activity!!, listenerFrete!!)
        val layoutManager = LinearLayoutManager(activity)
        rvFretes?.layoutManager = layoutManager
        rvFretes?.adapter = fretesAdapter
    }

    public fun fretesHoje(referencia: Query, pesquisa: Boolean) {
        referencia.addSnapshotListener(EventListener<QuerySnapshot>() { snapshots, exception ->
            if (exception != null) {
                Log.d("INFO_FRETES", "exception")
                layoutFretes(getString(R.string.erro_carregar_fretes_ativos), false)
                activity?.toast(getString(R.string.erro_carregar_fretes))
                return@EventListener
            }

            if (snapshots.isEmpty) {
                Log.d("INFO_FRETES", "snapshots.empty")
                layoutFretes(getString(R.string.aviso_sem_fretes), false)

            } else {

                for (doc in snapshots.documentChanges) {

                    when (doc.type) {

                        DocumentChange.Type.ADDED -> {

                            val frete = doc.document.toObject(Frete::class.java)

                            if (!frete.realizado) {
                                Log.d("INFO_FRETES", "added")



                                listaFretes.add(frete)

                                if (frete.data.equals(ReturnData.dataString("dd/MM"))) {
                                    FRETES_HOJE += 1
                                }

                                atualizaTextoFretesHoje(FRETES_HOJE)

                                fretesAdapter?.notifyDataSetChanged()

                            }

                        }

                        DocumentChange.Type.MODIFIED -> {

                            val frete = doc.document.toObject(Frete::class.java)

                            if (!frete.realizado) {

                                var i : Int = 0
                                for (item in listaFretes) {
                                    if (item.idFrete.equals(frete.idFrete)) {
                                        listaFretes[i] = frete
                                    }
                                    i += 1
                                }
                                fretesAdapter?.notifyDataSetChanged()

                            }
                            Log.d("INFO_FRETES", "modified")
                            return@EventListener
                        }

                        DocumentChange.Type.REMOVED -> {
                            Log.d("INFO_FRETES", "removed")
                            if (doc.document["data"].equals(ReturnData.dataString("dd/MM"))) {
                                FRETES_HOJE = FRETES_HOJE - 1
                                atualizaTextoFretesHoje(FRETES_HOJE)
                            }
                        }

                    }
                }

                if (listaFretes.isEmpty()) {
                    layoutFretes(getString(R.string.aviso_sem_fretes), false)
                    Log.d("INFO_FRETES", "lista vazia")
                } else {
                    setRecyclerView()
                    layoutFretes(null, true)
                    Log.d("INFO_FRETES", "lista populada")
                }

            }
        })
    }

    private fun limpar(){
        FRETES_HOJE = 0
        listaFretes.clear()
    }

    private fun atualizaTextoFretesHoje(contador: Int) {
        tvNumeroFretesHoje?.text = contador.toString()
        if (contador == 1) {
            tvFretesHoje?.text = "frete\nhoje"
        } else {
            tvFretesHoje?.text = "fretes\nhoje"
        }
    }

    private fun carregaFretes(referencia: Query, pesquisa: Boolean) {
        referencia.get().addOnCompleteListener {
            if (it.isSuccessful) {
                if (it.result.size() == 0) {
                    if (pesquisa) {
                        activity?.toast("Nenhum resultado")
                    } else {
                        layoutFretes(getString(R.string.aviso_sem_fretes), false)
                    }
                } else {
                    layoutFretes(null, true)
                    try {
                        if (pesquisa) {
                            listaFretes.clear()
                            fretesAdapter?.notifyDataSetChanged()
                        }
                        for (document in it.result) {
                            val frete = document.toObject<Frete>(Frete::class.java)
                            if (!frete.realizado) {
                                listaFretes.add(frete)
                            }
                        }

                        if (listaFretes.isEmpty()) {
                            layoutFretes(getString(R.string.aviso_sem_fretes), false)
                            return@addOnCompleteListener
                        }

                        fretesAdapter = FretesAdapter(idUsuario, listaFretes, activity!!, listenerFrete!!)
                        val layoutManager = LinearLayoutManager(activity)
                        rvFretes?.layoutManager = layoutManager
                        rvFretes?.adapter = fretesAdapter

                        //fretesHoje()

                    } catch (e: Exception) {
                        try {
                            layoutFretes(getString(R.string.erro_carregar_fretes), false)
                        } catch (ignored: Exception) {
                        }
                    }
                }
            } else {
                it.exception?.printStackTrace()
                layoutFretes(getString(R.string.erro_carregar_fretes), false)
            }
        }
    }

    private fun layoutFretes(mensagem: String?, mostrar: Boolean) {
        try {
            tvSemFretes?.text = mensagem
            if (mostrar) {
                pbFretes?.visibility = View.GONE
                tvSemFretes?.visibility = View.GONE
                includeMain?.visibility = View.VISIBLE
                rvFretes?.visibility = View.VISIBLE
                return
            }
            includeMain?.visibility = View.INVISIBLE
            rvFretes?.visibility = View.GONE
            pbFretes?.visibility = View.GONE
            tvSemFretes?.visibility = View.VISIBLE
            tvSemFretes?.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.from_bottom))
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }

}