package com.example.bookapp.activities

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.bookapp.databinding.ActivityPdfEditBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PdfEditActivity : AppCompatActivity() {


    private companion object {
        private const val TAG = "PDF_EDIT_TAG"
    }
    //view binding
    private lateinit var binding: ActivityPdfEditBinding

    //book id get from intent start from pdfAdminApdater
    private var bookId = ""

    //progress
    private lateinit var progressDialog: ProgressDialog

    //array list hold category title
    private lateinit var categoryTitleArrayList: ArrayList<String>

    //array list hold category id
    private lateinit var categoryIdArrayList: ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bookId = intent.getStringExtra("bookId")!!

        //progress dialog
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        loadCategories()
        loadBookInfo()

        //back btn
        binding.btnBack.setOnClickListener {
            onBackPressed()
        }

        //pick category
        binding.tvCategory.setOnClickListener {
            categoryDialog()
        }

        //btn update
        binding.btnUpdate.setOnClickListener {
            validateData()
        }
    }

    private fun loadBookInfo() {
        Log.d(TAG,"loadBookInfo: Loading book info")

        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    //get book info
                    selectCategoryId = snapshot.child("categoryId").value.toString()
                    val description = snapshot.child("description").value.toString()
                    val title = snapshot.child("title").value.toString()

                    //set to view
                    binding.edtTitle.setText(title)
                    binding.edtDescription.setText(description)

                    //load book
                    Log.d(TAG, "onDataChange: load book category info")
                    val refBookCategory = FirebaseDatabase.getInstance().getReference("Categories")
                    refBookCategory.child(selectCategoryId)
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                //get category
                                val category = snapshot.child("category").value
                                //set to text view
                                binding.tvCategory.text = category.toString()



                            }

                            override fun onCancelled(error: DatabaseError) {
                                TODO("Not yet implemented")
                            }

                        })
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })

    }

    private var title = ""
    private var description = ""
    private fun validateData() {
        //getdata
        title = binding.edtTitle.text.toString().trim()
        description = binding.edtDescription.text.toString().trim()

        //validate Data
        if (title.isEmpty()) {
            Toast.makeText(this, "Enter Title..", Toast.LENGTH_SHORT).show()
        } else if (description.isEmpty()) {
            Toast.makeText(this, "Enter Description...", Toast.LENGTH_SHORT).show()
        } else if (selectCategoryId.isEmpty()) {
            Toast.makeText(this, "Pick Category", Toast.LENGTH_SHORT).show()
        } else {
            updateData()
        }
    }

    private fun updateData() {
        Log.d(TAG, "updateData: Start update pdf info...")

        //show progress
        progressDialog.setMessage("Updating Book info")
        progressDialog.show()

        //setup data to update to db
        val hashMap = HashMap<String, Any>()
        hashMap["title"] = "$title"
        hashMap["description"] = "$description"
        hashMap["categoryId"] = "$selectCategoryId"

        //start update
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .updateChildren(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Log.d(TAG,"UpdateData: Update Successfully")
                Toast.makeText(this, "Update Successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e->
                Log.d(TAG,"UpdateData: Failed updating due to ${e.message}")
                progressDialog.dismiss()
                Toast.makeText(this, "Failed updating due to ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private var selectCategoryId = ""
    private var selectCategoryTitle = ""

    private fun categoryDialog() {
        //show dialog
        val categoriesArray = arrayOfNulls<String>(categoryTitleArrayList.size)
        for (i in categoryTitleArrayList.indices) {
            categoriesArray[i] = categoryTitleArrayList[i]
        }

        //alert dialog
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Choose Category")
            .setItems(categoriesArray) {dialog, position ->
                //save click
                selectCategoryId = categoryIdArrayList[position]
                selectCategoryTitle = categoryTitleArrayList[position]

                //set to textview
                binding.tvCategory.text = selectCategoryTitle
            }
            .show()
    }

    private fun loadCategories() {
        Log.d(TAG,"loadCategories: Loading categories...")

        categoryIdArrayList = ArrayList()
        categoryTitleArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                categoryIdArrayList.clear()
                categoryTitleArrayList.clear()

                for (ds in snapshot.children) {

                    val id = "${ds.child("id").value}"
                    val category = "${ds.child("category").value}"


                    categoryIdArrayList.add(id)
                    categoryTitleArrayList.add(category)

                    Log.d(TAG, "onDataChange: Category ID $id")
                    Log.d(TAG, "onDataChange: Category $category")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }
}