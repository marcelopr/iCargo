package com.example.marcelo.icargo

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_login.*
import org.jetbrains.anko.longToast
import org.jetbrains.anko.toast

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        initListeners()

    }

    fun initListeners(){
        btnEntrarLogin.setOnClickListener{
            validaDados(etEmailLogin.text.toString(), etSenhaLogin.text.toString())
        }

        llCriarConta.setOnClickListener(View.OnClickListener {
            val intentCadastro = Intent(this, CadastroActivity::class.java)
            startActivity(intentCadastro)
            finish()
        })
    }

    fun validaDados(email: String, senha: String){
        if (email.isEmpty()){
            toast(getString(R.string.aviso_informe_email))
            return
        }
        if (senha.isEmpty()){
            toast(getString(R.string.aviso_informe_senha))
            return
        }
        if (senha.length < 6){
            longToast(getString(R.string.aviso_senha_curta))
            return
        }

        login(email, senha)
    }

    private fun login(email: String, senha: String){
        progressBar(true)

        val auth: FirebaseAuth = FirebaseAuth.getInstance()

        try {
            auth.signInWithEmailAndPassword(email, senha)
                    .addOnCompleteListener(this, {
                        if (it.isSuccessful) {
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            longToast(getString(R.string.aviso_erro_login))
                            progressBar(false)
                        }
                    })
        }catch (e: Exception){
            e.printStackTrace()
            progressBar(false)
            toast(getString(R.string.aviso_erro_login))
        }

    }

    private fun progressBar(show: Boolean){
        if (show){
            pbEntrar.visibility = View.VISIBLE
            btnEntrarLogin.isEnabled = false
            return
        }
        pbEntrar.visibility = View.INVISIBLE
        btnEntrarLogin.isEnabled = true
    }

}