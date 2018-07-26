/*
data class Frete(
        val idUsuario: String,
        val idCliente: String,
        val idFrete: String,
        val cliente: String,
        val endereco: String,
        val data: String,
        val hora: String,
        val carga: String,
        val valor: Int,
        val realizado: Boolean)
*/


package com.example.marcelo.icargo.model

import java.io.Serializable

class Frete  : Serializable{

    var idFrete : String? = null
    var idUsuario : String? = null
    var idCliente : String? = null
    var cliente : String? = null
    var endereco : String? = null
    var data : String? = null
    var hora : String? = null
    var carga : String? = null
    var valor: Int? = null
    var realizado: Boolean = false

    constructor(){}

    constructor(idUsuario: String, idCliente: String, idFrete: String, cliente: String?, endereco: String?, data: String?, hora: String?, carga: String, valor: Int, realizado: Boolean){
        this.idUsuario = idUsuario
        this.idCliente = idCliente
        this.idFrete = idFrete
        this.cliente = cliente
        this.endereco = endereco
        this.data = data
        this.hora = hora
        this.carga = carga
        this.valor = valor
        this.realizado = realizado
    }

}
