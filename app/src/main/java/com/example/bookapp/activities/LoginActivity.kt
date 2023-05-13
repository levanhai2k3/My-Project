package com.example.bookapp.activities

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import com.example.bookapp.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class LoginActivity : AppCompatActivity() {

    //view binding
    private lateinit var binding: ActivityLoginBinding

    //firebase auth
    private lateinit var firebaseAuth: FirebaseAuth
    //progress dialog
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)


        //init firebaseAuth
        firebaseAuth = FirebaseAuth.getInstance()

        //will show while creating account
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        //if no accout-> go to register
        binding.tvNoAccount.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        //click login
        binding.btnLogin.setOnClickListener {
            /*Step
            * 1, Input Data
            * 2,Validate DAta
            * 3, Login firebase auth
            * 4, check usertype
            * */
            validateData()
        }
        binding.btnBack.setOnClickListener {
            onBackPressed()
        }

        binding.tvForgotPassWord.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
    }

    private var email = ""
    private var password = ""

    private fun validateData() {
        //1, Input Data
        email = binding.edtEmail.text.toString().trim()
        password = binding.edtPassword.text.toString().trim()

        // 2,Validate DAta
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid Email Pattern....", Toast.LENGTH_SHORT).show()
        }
        else if (password.isEmpty()) {
            Toast.makeText(this, "Enter your Password....", Toast.LENGTH_SHORT).show()
        }
        else {
            loginUser()
        }
    }

    private fun loginUser() {
        //* 3, Login firebase auth

        progressDialog.setMessage("Login in....")
        progressDialog.show()

        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                checkUser()
            }
            .addOnFailureListener {e ->
                progressDialog.dismiss()
                Toast.makeText(this, "Login Failed due to ${e.message}....", Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkUser() {
        //4.check user type
        progressDialog.setMessage("Check User....")

        val firebaseUser = firebaseAuth.currentUser!!

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseUser.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                   progressDialog.dismiss()

                    //get user type
                    val userType = snapshot.child("userType").value
                    if (userType == "user") {
                        startActivity(Intent(this@LoginActivity, DashboardUserActivity::class.java))
                    } else if (userType == "admin") {
                        startActivity(Intent(this@LoginActivity, DashboardAdminActivity::class.java))
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }
}