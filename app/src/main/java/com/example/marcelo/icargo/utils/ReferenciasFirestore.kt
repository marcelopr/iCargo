package com.example.marcelo.icargo.utils


import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ReferenciasFirestore{

    companion object {

        fun clientesCollection(idUsuario: String) : Query = FirebaseFirestore.getInstance().collection("Clientes").whereEqualTo("idUsuario", idUsuario).orderBy("nome")

        fun fretesCollection(idUsuario: String) : Query = FirebaseFirestore.getInstance().collection("Fretes").whereEqualTo("idUsuario", idUsuario).orderBy("idFrete", Query.Direction.DESCENDING)

        fun clienteDocument(idCliente: String) : DocumentReference = FirebaseFirestore.getInstance().collection("Clientes").document(idCliente)

        fun freteDocument(idFrete: String) : DocumentReference = FirebaseFirestore.getInstance().collection("Fretes").document(idFrete)

        fun financeiroDocument(idUsuario: String) : DocumentReference = FirebaseFirestore.getInstance().collection("Financeiro").document(idUsuario)


    }

}