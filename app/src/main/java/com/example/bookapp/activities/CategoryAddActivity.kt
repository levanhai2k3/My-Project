package com.example.bookapp.activities

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.bookapp.databinding.ActivityCategoryAddBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class CategoryAddActivity : AppCompatActivity() {

    //view binding
    private lateinit var binding: ActivityCategoryAddBinding

    //fireauth
    private lateinit var firebaseAuth: FirebaseAuth

    //progres dialog
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()

        //configure progress dialog
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait...")
        progressDialog.setCanceledOnTouchOutside(false)

        //go back
        binding.btnBack.setOnClickListener {
            onBackPressed()
        }

        //upload category
        binding.btnSubmit.setOnClickListener {
            validateData()
        }

    }

    private var category = ""
    private fun validateData() {
        //validate Data
        if (category.isEmpty()) {
            Toast.makeText(this, "Enter Category.....", Toast.LENGTH_SHORT).show()
        } else {
            addCategory()
        }

        //get data
        category = binding.edtCategory.text.toString().trim()
    }

    private fun addCategory() {
        //show progress
        progressDialog.show()

        //get timestamp
        var timestamp = System.currentTimeMillis()

        //setup data to add in firebase db
        val hashMap = HashMap<String, Any?>()
        hashMap["id"] = "$timestamp"
        hashMap["category"] = category
        hashMap["timestamp"] = timestamp
        hashMap["uid"] = "${firebaseAuth.uid}"

        //add to firebase DB root > categoryId > category info
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.child("$timestamp")
            .setValue(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Added Successfully....", Toast.LENGTH_LONG).show()
                binding.edtCategory.setText("")
            }
            .addOnFailureListener { e->
                progressDialog.dismiss()
                Toast.makeText(this, "Failed to add due to ${e.message}", Toast.LENGTH_LONG).show()
            }

    }
}