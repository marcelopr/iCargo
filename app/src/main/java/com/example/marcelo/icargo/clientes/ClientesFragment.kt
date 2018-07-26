package com.example.marcelo.icargo.clientes

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.LinearLayout

import com.example.marcelo.icargo.R
import com.example.marcelo.icargo.eventbus.nomeEB
import com.example.marcelo.icargo.interfaces.ClienteInterface
import com.example.marcelo.icargo.interfaces.InterfaceToolbar
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.fragment_clientes.*
import com.example.marcelo.icargo.model.Cliente
import org.jetbrains.anko.longToast
import org.jetbrains.anko.toast
import com.example.marcelo.icargo.eventbus.posicaoEB
import com.example.marcelo.icargo.utils.ReferenciasFirestore
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class ClientesFragment : Fragment(){

    private var fabAddCliente: FloatingActionButton? = null
    private var rvClientes: RecyclerView? = null
    private var etPesquisar: EditText? = null
    private var listaClientes: ArrayList<Cliente> = ArrayList()
    private var clientesAdapter: ClientesAdapter? = null
    lateinit var idUsuario: String
    lateinit var clientesCollection: Query
    private var FLAG_INICIO: Boolean = true
    private var listenerToolbar: InterfaceToolbar? = null
    private var listenerCliente: ClienteInterface? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.fragment_clientes, container, false)

        if (arguments == null) {

            layoutClientes(getString(R.string.erro_carregar_clientes), false)

        } else {

            fabAddCliente = view.findViewById(R.id.fabAddCliente)
            rvClientes = view.findViewById(R.id.rvClientes)

        }

        idUsuario = arguments!!.getString("idUsuario")
        clientesCollection = FirebaseFirestore.getInstance().collection("Clientes").whereEqualTo("idUsuario", idUsuario)

        initListeners()

        EventBus.getDefault().register(this)

        return view
    }

    override fun onStart() {
        super.onStart()
        carregaClientes(ReferenciasFirestore.clientesCollection(idUsuario))
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe
    fun onEvent(posicaoEB: posicaoEB){
        try {
            activity?.toast(getString(R.string.aviso_cliente_excluido))
            listaClientes.removeAt(posicaoEB.posicao)
            clientesAdapter?.notifyItemRemoved(posicaoEB.posicao)
            clientesAdapter?.notifyItemRangeChanged(posicaoEB.posicao, listaClientes.size);

            if (listaClientes.size == 0){
                layoutClientes(getString(R.string.aviso_sem_clientes), false)
            }
        }catch (e: Exception){
            layoutClientes(getString(R.string.erro_atualizar_clientes), false)
        }
    }

    @Subscribe
    fun onEvent(nomeEB: nomeEB){
        try {
            validaPesquisa(nomeEB.nome)
        }catch (e: Exception){

        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is ClienteInterface) {
            listenerCliente = context
        } else {
            throw RuntimeException(context.toString() + " implemente a interface Cliente")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listenerToolbar = null
        listenerCliente = null
        clientesAdapter = null
    }

    private fun initListeners() {
        fabAddCliente?.setOnClickListener {
            intentCliente()
        }
    }

    private fun carregaClientes(referencia: Query?) {
        listaClientes.clear()
        clientesAdapter?.notifyDataSetChanged()
        pbClientes.visibility = View.VISIBLE

        referencia?.get()?.addOnCompleteListener {
            if (it.isSuccessful) {

                if (it.result.size() == 0) {
                    layoutClientes(getString(R.string.aviso_sem_clientes), false)
                } else {
                    try {

                        layoutClientes("", true)

                        for (document in it.result) {
                            val cliente = document.toObject<Cliente>(Cliente::class.java)
                            listaClientes.add(cliente)
                        }

                        clientesAdapter = ClientesAdapter(listaClientes, activity!!, listenerCliente!!)
                        val layoutManager = LinearLayoutManager(activity)
                        rvClientes?.layoutManager = layoutManager
                        rvClientes?.itemAnimator = DefaultItemAnimator() as RecyclerView.ItemAnimator?
                        rvClientes?.adapter = clientesAdapter

                    }catch (ignored: Exception){}
                }

                pbClientes?.visibility = View.GONE

            } else {
                layoutClientes(getString(R.string.erro_carregar_clientes), false)
            }
        }

    }

    private fun validaPesquisa(cliente: String) {
        verificaClientes( ReferenciasFirestore.clientesCollection(idUsuario).startAt(cliente).endAt(cliente), cliente )
    }

    private fun verificaClientes(referencia: Query, cliente: String) {
        referencia.get().addOnCompleteListener {
            if (it.isSuccessful) {
                if (it.result.size() == 0) {
                    activity?.longToast("Nenhum resultado para '$cliente'")
                } else {
                    carregaClientes(referencia)
                }
            } else {
                it.exception?.printStackTrace()
                activity?.longToast(getString(R.string.erro_pesquisar_clientes))
            }
        }
    }

    private fun layoutClientes(mensagem: String, mostrar: Boolean) {
        tvSemClientes?.text = mensagem
        if (mostrar) {
            tvSemClientes?.visibility = View.GONE
            rvClientes?.visibility = View.VISIBLE
            return
        }
        rvClientes?.visibility = View.GONE
        pbClientes?.visibility = View.GONE
        tvSemClientes?.visibility = View.VISIBLE
        tvSemClientes?.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.from_bottom))
    }

    private fun intentCliente() {
        val intentCliente = Intent(activity, ClienteActivity::class.java)
        intentCliente.putExtra("idUsuario", idUsuario)
        startActivity(intentCliente)
    }

}