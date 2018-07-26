package com.example.marcelo.icargo

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.view.*
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import com.example.marcelo.icargo.clientes.ClientesFragment
import com.example.marcelo.icargo.clientes.FragmentDialogCliente
import com.example.marcelo.icargo.fretes.FragmentDialogFrete
import com.example.marcelo.icargo.eventbus.nomeEB
import com.example.marcelo.icargo.fretes.FretesFragment
import com.example.marcelo.icargo.financeiro.FinanceiroFragment
import com.example.marcelo.icargo.financeiro.FragmentDialogFinanceiro
import com.example.marcelo.icargo.interfaces.ClienteInterface
import com.example.marcelo.icargo.interfaces.FinanceiroInterface
import com.example.marcelo.icargo.interfaces.FreteInterface
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*
import com.example.marcelo.icargo.model.Cliente
import com.example.marcelo.icargo.model.Financeiro
import com.example.marcelo.icargo.model.Frete
import org.greenrobot.eventbus.EventBus
import org.jetbrains.anko.longToast
import org.jetbrains.anko.toast
import android.app.Activity
import android.support.v7.widget.Toolbar
import android.view.inputmethod.InputMethodManager


class MainActivity : AppCompatActivity(),
        ClienteInterface,
        FreteInterface,
        FinanceiroInterface,

        FragmentDialogCliente.PermissaoPhoneListener {

    override fun infoMes(mes: Financeiro) {
        val dialogFinanceiro = FragmentDialogFinanceiro(idUsuario!!, mes, this)
        dialogFinanceiro.show(supportFragmentManager, "FragmentDialogFinanceiro")
    }

    override fun infoFrete(frete: Frete, posicao: Int) {
        val dialogFrete = FragmentDialogFrete(frete, posicao, this)
        dialogFrete.show(supportFragmentManager, "FragmentDialogFrete")
    }

    override fun infoCliente(cliente: Cliente, posicao: Int) {
        val dialogCliente = FragmentDialogCliente(cliente, posicao, applicationContext)
        dialogCliente.show(supportFragmentManager, "FragmentDialogCliente")
    }

    override fun permissao() {
        ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.CALL_PHONE),
                RECORD_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            RECORD_REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    longToast(getString(R.string.permissao_negada_chamadas))
                } else {
                    longToast(getString(R.string.permissao_realizar_chamadas))
                }
            }
        }
    }

    private var auth: FirebaseAuth? = null
    private var container: FrameLayout? = null
    private var idUsuario: String? = null
    private var FLAG_DIALOG: Boolean = false
    private val RECORD_REQUEST_CODE = 123
    private var FRAGMENTO = 1
    private var fragmentoAtual: Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        idUsuario = auth?.currentUser?.uid

        setSupportActionBar(tbMain)

        container = findViewById(R.id.container) as FrameLayout
        val navigation = findViewById(R.id.bnvMain) as BottomNavigationView
        navigation.setOnNavigationItemSelectedListener(OnNavigationItemSelectedListener)

        val fragment = FretesFragment()
        addFragment(fragment)

    }

    private val OnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_feed -> {
                FRAGMENTO = 1
                val fragment = FretesFragment()
                fragmentoAtual = fragment
                addFragment(fragment)
                item.setIcon(getDrawable(R.drawable.ic_package_selected))
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_clientes -> {
                FRAGMENTO = 2
                val fragment = ClientesFragment()
                fragmentoAtual = fragment
                addFragment(fragment)
                item.setIcon(R.drawable.ic_users_selected)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_financeiro -> {
                FRAGMENTO = 3
                val fragment = FinanceiroFragment()
                addFragment(fragment)
                item.setIcon(getDrawable(R.drawable.ic_money_selected))
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    @SuppressLint("PrivateResource")
    private fun addFragment(fragment: Fragment) {
        idUsuario?.let {
            val bundle = Bundle()
            bundle.putString("idUsuario", idUsuario)
            fragment.arguments = bundle

            supportFragmentManager
                    .beginTransaction()
                    .setCustomAnimations(R.anim.design_bottom_sheet_slide_in, R.anim.design_bottom_sheet_slide_out)
                    .replace(R.id.container, fragment, fragment.javaClass.getSimpleName())
                    .addToBackStack(fragment.javaClass.getSimpleName())
                    .commit()
            if (tbPesquisaMain.visibility == View.VISIBLE) {
                barraPesquisa(false)
                keyboard(this, false)
            }
        }
    }

    public fun hideToolbar(hide: Boolean) {
        if (hide) {
            supportActionBar?.hide()
            return
        }
        supportActionBar?.show()
    }

    private fun intentLogin() {
        auth!!.signOut()
        val intentLogin = Intent(this, LoginActivity::class.java)
        intentLogin.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intentLogin)
        finish()
    }

    private fun setToolbar(){
        val tbMain = findViewById<Toolbar>(R.id.tbMain)
        tbMain.title = " "+getString(R.string.app_name)
        tbMain.setTitleTextColor(resources.getColor(R.color.cinzaEscuro))
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        item?.let {
            when (item.itemId) {
                R.id.action_sair -> intentLogin()
                R.id.action_search -> barraPesquisa(true)
                else -> null
            }
        }
        return true
    }

    private fun barraPesquisa(mostrar: Boolean) {
        /*if (mostrar) {
            tbPesquisaMain.visibility = View.VISIBLE
            tbPesquisaMain.startAnimation(AnimationUtils.loadAnimation(this, R.anim.from_right))
            etPesquisaMain.requestFocus()
            keyboard(this, true)

            etPesquisaMain?.setOnEditorActionListener() { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    val nome = etPesquisaMain.text.toString()
                    if (nome.isBlank() || nome.isEmpty()) {
                        toast(getString(R.string.aviso_informe_o_cliente))
                    } else {
                        //passando nome para listeners ativos
                        keyboard(this, false)
                        val eventBus = nomeEB(nome)
                        EventBus.getDefault().post(eventBus)
                    }
                    true
                } else {
                    toast(getString(R.string.erro_pesquisar))
                    false
                }
            }

            ivFecharPesquisaMain.setOnClickListener {
                tbPesquisaMain.visibility = View.GONE
                tbPesquisaMain.startAnimation(AnimationUtils.loadAnimation(this, R.anim.to_right))
                if (bnvMain.selectedItemId == R.id.navigation_feed){
                    addFragment(FretesFragment())
                }else{
                    addFragment(ClientesFragment())
                }
            }
        } else {
            tbPesquisaMain.visibility = View.GONE
            tbPesquisaMain.startAnimation(AnimationUtils.loadAnimation(this, R.anim.to_right))

            bnvMain
        }*/
    }

    override fun onStart() {
        super.onStart()
        setToolbar()
    }

    override fun onStop() {
        super.onStop()
        barraPesquisa(false)
        keyboard(this, false)
    }

    private fun keyboard(activity: Activity, mostrar: Boolean) {
        val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        var view = activity.currentFocus
        if (view == null) {
            view = View(activity)
        }
        if (mostrar) {
            imm.showSoftInputFromInputMethod(view.windowToken, 0)
        } else {
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

}