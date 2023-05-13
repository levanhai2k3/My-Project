package com.example.bookapp.activities

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.bookapp.R
import com.example.bookapp.databinding.ActivityForgotPasswordBinding
import com.google.firebase.auth.FirebaseAuth
import java.util.regex.Pattern

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding

    private lateinit var firebaseAuth: FirebaseAuth

    //progressbar
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()

        //setup progress dialog
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.btnSubmit.setOnClickListener {
            validateData()
        }

        binding.btnBack.setOnClickListener {
            onBackPressed()
        }
    }

    private var email = ""
    private fun validateData() {
        email = binding.edtEmail.text.toString().trim()

        if (email.isEmpty()) {
            Toast.makeText(this, "Enter email....", Toast.LENGTH_SHORT).show()
        }else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid email pattern....", Toast.LENGTH_SHORT).show()
        } else {
            resetPassword()
        }
    }

    private fun resetPassword() {
        progressDialog.setMessage("Sending password reset instruction to $email")
        progressDialog.show()
        firebaseAuth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Instruction sent to $email", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e->
                Toast.makeText(this, "Failed to send due to  ${e.message}", Toast.LENGTH_SHORT).show()
                progressDialog.dismiss()
            }
    }
}