package com.example.bookapp.activities

import android.app.ProgressDialog
import android.content.Intent

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import com.example.bookapp.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.security.MessageDigest


class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    //firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    //progress diaolog
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //init firebaseAuth
        firebaseAuth = FirebaseAuth.getInstance()

        //will show while creating account
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        //back login
        binding.btnBack.setOnClickListener {
            onBackPressed()
        }

        //register
        binding.btnRegister.setOnClickListener {
            validateData()
        }
    }

    private var name = ""
    private var email = ""
    private var password = ""

    private fun validateData() {
        //Step
        //1, InputData
        name = binding.edtName.text.toString().trim()
        email = binding.edtRegEmail.text.toString().trim()
        password = binding.edtRegPassword.text.toString().trim()
        val cPassword = binding.edtConfirmPass.text.toString().trim()

        //2, Validate Data
        if (name.isEmpty()) {
            Toast.makeText(this, "Enter your Name....", Toast.LENGTH_SHORT).show()
        }
        else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid Email Pattern....", Toast.LENGTH_SHORT).show()
        }
        else if (password.isEmpty()) {
            Toast.makeText(this, "Enter your Password....", Toast.LENGTH_SHORT).show()
        }
        else if (cPassword.isEmpty()) {
            Toast.makeText(this, "Confirm Password....", Toast.LENGTH_SHORT).show()
        }
        else if (password != cPassword) {
            Toast.makeText(this, "Password don't match....", Toast.LENGTH_SHORT).show()
        }
        else {
            createUserAccount()
        }
    }


    private fun createUserAccount() {
        //3, Create Account - Firebase auth

        //show progress
        progressDialog.setMessage("Create Account....")
        progressDialog.show()

        //Create user in firebase auth
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                //add user info database
                updateUserInfo()
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this, "Failed creating account due to ${e.message}....", Toast.LENGTH_SHORT).show()
            }
    }

    // Mã hóa(Encode)
    val passwordHash = hashString(password, "SHA-256")

    // Function to hash a string with a given algorithm
    private fun hashString(input: String, algorithm: String): String {
        return MessageDigest.getInstance(algorithm)
            .digest(input.toByteArray())
            .fold("") { str, it -> str + "%02x".format(it) }
    }

    private fun updateUserInfo() {
        //4,Save User Info - realtime db
        progressDialog.setMessage("Saving user info...")
        //timestamp
        val timestamp = System.currentTimeMillis()

        //get current user uid
        val uid = firebaseAuth.uid

        //setup data to add in db
        val hashMap: HashMap<String, Any?> = HashMap()
        hashMap["uid"] = uid
        hashMap["email"] = email
        hashMap["password"] = passwordHash
        hashMap["name"] = name
        hashMap["profileImage"] = ""
        hashMap["userType"] = "user"
        hashMap["timestamp"] = timestamp

        //set data to database
        var ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(uid!!)
            .setValue(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Account create.....", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, DashboardUserActivity::class.java))
                finish()
            }
            .addOnFailureListener { e->
                progressDialog.dismiss()
                Toast.makeText(this, "Failed saving user info due to ${e.message}....", Toast.LENGTH_SHORT).show()
            }
    }

}