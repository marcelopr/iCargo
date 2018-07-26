package com.example.marcelo.icargo.utils

class Mascara {

    companion object {
        fun mascTelefone(telefone: String): String{

            return "("+telefone.substring(0, 2) + ") " + telefone.substring(2);

        }
    }

}