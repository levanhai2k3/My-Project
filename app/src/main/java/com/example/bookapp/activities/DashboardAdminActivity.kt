package com.example.bookapp.activities

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bookapp.adapters.CategoryAdapter
import com.example.bookapp.databinding.ActivityDashboardAdminBinding
import com.example.bookapp.models.CategoryModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DashboardAdminActivity : AppCompatActivity() {

    //view binding
    private lateinit var binding: ActivityDashboardAdminBinding

    //fireauth
    private lateinit var firebaseAuth: FirebaseAuth

    //arrlist to hold categories
    private lateinit var categoryArrayList: ArrayList<CategoryModel>

    //adapter
    private lateinit var categoryAdapter: CategoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        categoryArrayList = ArrayList()
        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()

        //click out admin dashboard
        binding.tvSubTitleAdmin.setOnClickListener {
            firebaseAuth.signOut()
            checkUser()
        }

        //search
        binding.edtSearch.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                //called as and when user type anything
                try {
                    categoryAdapter.filter.filter(s)
                }catch (e: Exception) {
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

        //logout
        binding.btnLogoutAdmin.setOnClickListener {
            firebaseAuth.signOut()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        //start add category page
        binding.btnAddCategory.setOnClickListener {
            startActivity(Intent(this, CategoryAddActivity::class.java))
        }

        //add Pdf
        binding.fabAddPdf.setOnClickListener {
            startActivity(Intent(this, PdfAddActivity::class.java))
        }


        //open profile
        binding.btnProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }





        binding.rvCategories.layoutManager = LinearLayoutManager(this)

        //setup adapter
        categoryAdapter = CategoryAdapter(this, categoryArrayList)

        //set adapter to recyclerview
        binding.rvCategories.adapter = categoryAdapter

        loadCategories()
    }

    private fun loadCategories() {
        //init arrlist
        categoryArrayList = ArrayList()

        //get all categories from firebase DB
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                //clear list before starting adding data into it
                categoryArrayList.clear()
                for (ds in snapshot.children) {
                    //get data as model
                    val model = ds.getValue(CategoryModel::class.java)

                    //add to arrlist
                    categoryArrayList.add(model!!)
                }
                //setup adapter
                categoryAdapter = CategoryAdapter(this@DashboardAdminActivity, categoryArrayList!!)

//set adapter to recyclerview
                binding.rvCategories.adapter = categoryAdapter

//notify adapter about data change
                categoryAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun checkUser() {
        //get current user
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null) {
            // not logged user, go to main screen
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        else {
            //logged in,  get and show user info
            val email = firebaseUser.email
            //set to tv of toolbar
            binding.tvSubTitleAdmin.text = email
        }
    }
}