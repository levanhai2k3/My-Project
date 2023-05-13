package com.example.bookapp.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager

import com.example.bookapp.adapters.AdminPdfAdapter
import com.example.bookapp.databinding.ActivityPdfListAdminBinding
import com.example.bookapp.models.PdfModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PdfListAdminActivity : AppCompatActivity() {
    //view binding
    private lateinit var binding: ActivityPdfListAdminBinding

    private companion object {
        const val TAG = "PDF_LIST_ADMIN_TAG"
    }

    //category id, title
    private var categoryId = ""
    private var category = ""

    //array list to hold books
    private lateinit var pdfArrayList: ArrayList<PdfModel>

    //adapter
    private lateinit var adapterPdfAdmin: AdminPdfAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfListAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //get from intent
        categoryId = intent.getStringExtra("categoryId")!!
        category = intent.getStringExtra("category")!!

        //set pdf category
        binding.tvSubTitle.text = category

        //set up RecyclerView
        binding.rvBook.layoutManager = LinearLayoutManager(this)
        pdfArrayList = ArrayList()
        adapterPdfAdmin = AdminPdfAdapter(this, pdfArrayList)
        binding.rvBook.adapter = adapterPdfAdmin

        //load book
        loadPdfList()

        //search
        binding.edtSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //do nothing
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                //filter data
                try {
                    adapterPdfAdmin.filter!!.filter(s)
                }catch (e: Exception) {
                    Log.d(TAG,"onTextChanged: ${e.message}")
                }

            }

            override fun afterTextChanged(s: Editable?) {
                //do nothing
            }
        })


        //back btn
        binding.btnBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun loadPdfList() {
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.orderByChild("categoryId").equalTo(categoryId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    pdfArrayList.clear()
                    for (ds in snapshot.children) {
                        //get data
                        val model = ds.getValue(PdfModel::class.java)

                        //add to list
                        if (model != null) {
                            pdfArrayList.add(model)
                            Log.d(TAG,"onDataChange: ${model.title} ${model.categoryId}")
                        }
                    }
                    adapterPdfAdmin = AdminPdfAdapter(this@PdfListAdminActivity, pdfArrayList)
                    binding.rvBook.adapter = adapterPdfAdmin
                    //notify adapter
                    adapterPdfAdmin.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "loadPdfList: ${error.message}")
                }
            })
    }
}