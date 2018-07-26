package com.example.marcelo.icargo.model

import java.io.Serializable

class Cliente  : Serializable{

    var nome : String? = null
    var endereco : String? = null
    var telefone1 : String? = null
    var telefone2 : String? = null
    var idCliente : String? = null
    var idUsuario : String? = null

    constructor(){}

    constructor(nome: String, endereco: String, idCliente: String, telefone1: String, telefone2: String, idUsuario: String){
        this.nome = nome
        this.endereco = endereco
        this.idCliente = idCliente
        this.telefone1 = telefone1
        this.telefone2 = telefone2
        this.idUsuario = idUsuario
    }

}