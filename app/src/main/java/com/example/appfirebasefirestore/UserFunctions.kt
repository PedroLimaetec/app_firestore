package com.example.appfirebasefirestore

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserFunctions {
    var userList: MutableList<User> = mutableListOf();


    val db = FirebaseFirestore.getInstance()

    fun addUser(user: HashMap<String, String>) {
        db.collection("users")
            .add(user)
    }

    fun getUser(uid: String) {
        db.collection("users").document(uid)
            .get()
        // função obsoleta
    }

    fun getAllUsers() {
        db.collection("users")
            .get()
            .addOnSuccessListener { documents ->
                userList = documents.map { document ->
                    User(
                        id = document.id,
                        nome = document.getString("nome") ?: "",
                        email = document.getString("email") ?: "",
                        telefone = document.getString("telefone") ?: "",
                        mensagem = document.getString("mensagem") ?: "",
                        senha = document.getString("senha") ?: ""
                    )
                } as MutableList<User>
            }
    }

    fun updateUser(user: HashMap<String, String>, uid: String) {
        db.collection("users").document(uid)
            .set(user)
            .addOnSuccessListener {

            }
            .addOnFailureListener { e ->

            }
    }

    fun deleteUser(uid: String) {
        db.collection("users").document(uid)
            .delete()
            .addOnSuccessListener {

            }
            .addOnFailureListener { e ->

            }
    }
}