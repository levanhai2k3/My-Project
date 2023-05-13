package com.example.bookapp.activities

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.example.bookapp.databinding.ActivityPdfAddBinding
import com.example.bookapp.models.CategoryModel
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class PdfAddActivity : AppCompatActivity() {

    //view binding
    private lateinit var binding: ActivityPdfAddBinding

    //firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    //progress dialog
    private lateinit var progressDialog: ProgressDialog

    //arrlist hold pdf
    private lateinit var categoryArrayList: ArrayList<CategoryModel>

    //url of picked pdf
    private var pdfUri: Uri? = null

    //TAG
    private val TAG = "PDF_ADD_TAG"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfAddBinding.inflate(layoutInflater)
        setContentView(binding.root)


        firebaseAuth = FirebaseAuth.getInstance()
        loadPdfCategories()

        //setup progress dialog
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)


        //click back
        binding.btnBack.setOnClickListener {
            onBackPressed()
        }

        //show category pick dialog
        binding.tvCategory.setOnClickListener {
            categoryPickDialog()
        }

        //pick pdf intent
        binding.btnAttachPdf.setOnClickListener {
            pdfPickIntent()
        }

        binding.btnUpload.setOnClickListener {
            /*1,Validate Data
            * 2,upload to firebase storage
            * 3,get url uploaded pdf
            * 4,upload pdf info to firebase DB*/

            validateData()
        }

    }

    private var title = ""
    private var description = ""
    private var category = ""
    private fun validateData() {
        Log.d(TAG, "validateData: validating data")

        //get data
        title = binding.edtTitle.text.toString().trim()
        description = binding.edtDescription.text.toString().trim()
        category = binding.tvCategory.text.toString().trim()

        if (title.isEmpty()) {
            Toast.makeText(this, "Enter Title....", Toast.LENGTH_SHORT).show()
        } else if (description.isEmpty()) {
            Toast.makeText(this, "Enter Description....", Toast.LENGTH_SHORT).show()
        } else if (category.isEmpty()) {
            Toast.makeText(this, "Pick Category....", Toast.LENGTH_SHORT).show()
        } else if (pdfUri == null){
            Toast.makeText(this, "Pick PDF....", Toast.LENGTH_SHORT).show()
        } else {
            //data validate, begin upload
            uploadPdfToStorage()
        }
    }

    private fun uploadPdfToStorage() {
        Log.d(TAG, "uploadPdfToStorage: upload to storage...")
        //progress dialog
        progressDialog.setMessage("Upload PDF")
        progressDialog.show()

        val timestamp = System.currentTimeMillis()

        //path of pdf in firebase storage
        val filePathAndName = "Books/$timestamp"
        //storage reference
        val storageReference = FirebaseStorage.getInstance().getReference(filePathAndName)
        storageReference.putFile(pdfUri!!)
            .addOnSuccessListener { taskSnapshot->
                Log.d(TAG,"uploadPdfToStorage: PDF uploaded now getting url...")

                //* 3,get url uploaded pdf
                val uriTask: Task<Uri> = taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful);
                val uploadedPdfUrl = "${uriTask.result}"

                uploadPdfInfoToDb(uploadedPdfUrl, timestamp)

            }
            .addOnFailureListener { e->
                Log.d(TAG,"uploadPdfToStorage: failed upload due to ${e.message}")
                progressDialog.dismiss()
                Toast.makeText(this, "Failed upload due to ${e.message}", Toast.LENGTH_SHORT).show()
            }

    }

    private fun uploadPdfInfoToDb(uploadedPdfUrl: String, timestamp: Long) {
        //* 4,upload pdf info to firebase DB*/

        Log.d(TAG,"uploadPdfInfoToDb: upload to DB" )
        progressDialog.setMessage("Upload Pdf info ....")

        //uid of current user
        val uid = firebaseAuth.uid

        //setup data to upload
        val hashMap:HashMap<String, Any> = HashMap()
        hashMap["uid"] = "$uid"
        hashMap["id"] = "$timestamp"
        hashMap["title"] = "$title"
        hashMap["description"] = "$description"
        hashMap["categoryId"] = "$selectedCategoryID"
        hashMap["url"] = "$uploadedPdfUrl"
        hashMap["timestamp"] = timestamp
        hashMap["viewCount"] = 0
        hashMap["downloadsCount"] = 0

        //db reference DB > Books > BookId > (Book info)
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child("$timestamp")
            .setValue(hashMap)
            .addOnSuccessListener {
                Log.d(TAG,"uploadPdfToStorage: upload to database")
                progressDialog.dismiss()
                Toast.makeText(this, "Successfully uploaded", Toast.LENGTH_SHORT).show()
                pdfUri = null
            }
            .addOnFailureListener { e->
                Log.d(TAG,"uploadPdfToStorage: failed upload due to ${e.message}")
                progressDialog.dismiss()
                Toast.makeText(this, "Failed upload due to ${e.message}", Toast.LENGTH_SHORT).show()
            }


    }

    private fun loadPdfCategories() {
        Log.d(TAG, "LoadPdfCategories: Loading pdf categories")

        //input arr
        categoryArrayList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                categoryArrayList.clear()
                for (ds in snapshot.children){
                    //get data
                    val model = ds.getValue(CategoryModel::class.java)
                    categoryArrayList.add(model!!)
                    Log.d(TAG, "onDataChange: ${model.category}")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private var selectedCategoryID = ""
    private var selectedCategoryTitle = ""


    private fun categoryPickDialog() {
        Log.d(TAG,"categoryPickDialog: Show category pdf pick dialog")

        // get string arr of categories from arrlist
        val categoryArray = arrayOfNulls<String>(categoryArrayList.size)
        for (i in categoryArrayList.indices) {
            categoryArray[i] = categoryArrayList[i].category
        }
        //alert dialog
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Pick Category")
            .setItems(categoryArray){dialog, which ->
                selectedCategoryTitle = categoryArrayList[which].category
                selectedCategoryID = categoryArrayList[which].id

                //set category to textview
                binding.tvCategory.text = selectedCategoryTitle

                Log.d(TAG,"categoryPickDialog: Selected CategoryID: $selectedCategoryID ")
                Log.d(TAG,"categoryPickDialog: Selected CategoryTitle: $selectedCategoryTitle ")
            }
            .show()
    }

    private fun pdfPickIntent() {
        Log.d(TAG, "pdfPickIntent: Start pdf pick intent")

        val intent = Intent()
        intent.type = "application/pdf"
        intent.action = Intent.ACTION_GET_CONTENT
        pdfActivityResultLauncher.launch(intent)
    }

    val pdfActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult>{result ->
            if (result.resultCode == RESULT_OK){
                Log.d(TAG, "PDF Picked")
                pdfUri = result.data!!.data
            } else {
                Log.d(TAG, "PDF pick cancelled")
                Toast.makeText(this, "Cancelled",Toast.LENGTH_SHORT).show()
            }
        }
    )
}