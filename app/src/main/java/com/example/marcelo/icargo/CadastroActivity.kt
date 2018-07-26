package com.example.marcelo.icargo

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_cadastro.*
import org.jetbrains.anko.longToast

class CadastroActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastro)

        initListeners()
    }

    private fun initListeners() {
        btnCriarConta.setOnClickListener({
            validaDados()
        })

        llRealizarLogin.setOnClickListener({
            intentLogin()
        })
    }

    private fun validaDados() {
        try {
            val nome = etNome.text.toString()
            val email = etEmailLogin.text.toString()
            val senha = etSenhaLogin.text.toString()
            val senha2 = etSenha2.text.toString()

            if (nome.isEmpty()) {
                longToast(getString(R.string.aviso_informe_nome))
                return
            }
            if (email.isEmpty()) {
                longToast(getString(R.string.aviso_informe_email))
                return
            }
            if (senha.isEmpty()) {
                longToast(getString(R.string.aviso_informe_senha))
                return
            }
            if (senha2.isEmpty()) {
                longToast(getString(R.string.aviso_repita_senha))
                return
            }
            if (etSenhaLogin.text.toString() != etSenha2.text.toString()) {
                longToast(getString(R.string.aviso_senha_igual))
                return
            }
            criarConta(email, senha)
        } catch (e: Exception) {
            longToast(getString(R.string.erro_validar_dados))
        }
    }

    private fun criarConta(email: String, senha: String) {
        progressBar(true)
        val auth: FirebaseAuth = FirebaseAuth.getInstance()
        auth.createUserWithEmailAndPassword(email, senha)
                .addOnCompleteListener({
                    if (it.isSuccessful) {

                        val uid = auth.currentUser?.uid.toString()

                        Log.d("INFO_CADASTRO", "id: " + uid)

                        val userMap = HashMap<String, Any>()
                        userMap.put("nome", etNome.text.toString())
                        userMap.put("email", etEmailLogin.text.toString())
                        userMap.put("uid", uid)

                        val db = FirebaseFirestore.getInstance()
                        db.collection("Usuarios")
                                .document(uid)
                                .set(userMap)
                                .addOnCompleteListener({
                                    longToast(getString(R.string.aviso_conta_criada))
                                    progressBar(false)
                                    intentLogin()
                                })
                    } else {
                        longToast(getString(R.string.erro_criar_conta))
                        progressBar(false)
                    }
                })
    }

    private fun intentLogin() {
        val intentMain = Intent(this, LoginActivity::class.java)
        startActivity(intentMain)
        finish()
    }

    private fun progressBar(mostrar: Boolean) {
        if (mostrar) {
            btnCriarConta.isEnabled = false
            pbCriarConta.visibility = View.VISIBLE
            return
        }
        btnCriarConta.isEnabled = true
        pbCriarConta.visibility = View.GONE
    }

}