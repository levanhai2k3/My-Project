package com.example.bookapp.activities

import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.example.bookapp.R
import com.example.bookapp.adapters.CommentAdapter
import com.example.bookapp.databinding.ActivityPdfDetailBinding
import com.example.bookapp.databinding.DialogAddCommentBinding
import com.example.bookapp.models.CommentModel
import com.example.bookapp.others.Constants
import com.example.bookapp.others.MyApplication
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.io.FileOutputStream

class PdfDetailActivity : AppCompatActivity() {

    // view binding
    private lateinit var binding: ActivityPdfDetailBinding

    private companion object{
        const val TAG = "BOOK_DETAILS_TAG"
    }
    //book id
    private var bookId = ""

    //get from fire base
    private var bookTitle = ""
    private var bookUrl = ""

    //will hold boolean
    private var isInMyFavorite = false

    //firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    //progress dialog
    private lateinit var progressDialog : ProgressDialog

    //array list to hold comment
    private lateinit var commentArrayList: ArrayList<CommentModel>

    //adapter to be set to recyclerview
    private lateinit var commentAdapter: CommentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPdfDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //get book from intent
        bookId = intent.getStringExtra("bookId")!!

        //init progressBar
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()

        if (firebaseAuth.currentUser != null) {
            checkIsFavorite()
        }

        MyApplication.incrementBookViewCount(bookId)

        loadBookDetails()
        showComment()

        //back button
        binding.btnBack.setOnClickListener {
            onBackPressed()
        }

        //read button
        binding.btnReadBook.setOnClickListener {
            val intent = Intent(this, PdfViewActivity::class.java)
            intent.putExtra("bookId", bookId)
            startActivity(intent)
        }

        //dowload click
        binding.btnDownloadBook.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "onCreate: STORAGE PERMISSION is already granted")
                downloadBook()
            } else {
                Log.d(TAG, "onCreate: STORAGE PERMISSION was not granted")
                requestStoragePermissionLauncher.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }

        //favorite click
        binding.btnFavorite.setOnClickListener {

            //check user logged
            if (firebaseAuth.currentUser == null) {
                //user not add to favorite
                Toast.makeText(this, "You're not logged in", Toast.LENGTH_SHORT).show()
            } else {
                if (isInMyFavorite) {
                    MyApplication.removeToFavorite(this, bookId)
                } else {
                    addToFavorite()
                }
            }
        }

        //show add comment dialog
        binding.btnAddComment.setOnClickListener {
            if (firebaseAuth.currentUser == null) {
                Toast.makeText(this, "You're not logged in", Toast.LENGTH_SHORT).show()
            } else {
                addCommentDialog()
            }
        }
    }

    private fun showComment() {
        //init arraylist
        commentArrayList = ArrayList()

        //db path to load comments
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId).child("Comments")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    commentArrayList.clear()

                    for (ds in snapshot.children) {
                        val model = ds.getValue(CommentModel::class.java)
                        //add to list
                        commentArrayList.add(model!!)
                    }
                    //set up adapter
                    commentAdapter = CommentAdapter(this@PdfDetailActivity, commentArrayList)
                    //set adapter
                    binding.rvComment.adapter = commentAdapter

                }



                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }

    private var comment = ""
    private fun addCommentDialog() {
        val commentAddBinding = DialogAddCommentBinding.inflate(LayoutInflater.from(this))

        //setup alert dialog
        val builder = AlertDialog.Builder(this, R.style.CustomDialog)
        builder.setView(commentAddBinding.root)

        //create and show dialog
        val alertDialog = builder.create()
        alertDialog.show()

        commentAddBinding.btnBack.setOnClickListener {
            alertDialog.dismiss()
        }
        //click , dismiss dialog
        commentAddBinding.btnSubmit.setOnClickListener {
            //get data
            comment = commentAddBinding.edtComment.text.toString().trim()

            //validate
            if (comment.isEmpty()) {
                Toast.makeText(this,"Enter comment...", Toast.LENGTH_SHORT).show()
            } else {
                alertDialog.dismiss()
                addComment()
            }
        }
    }

    private fun addComment() {
        //show progress
        progressDialog.setMessage("Adding comment")
        progressDialog.show()

        //timestamp
        val timestamp = "${System.currentTimeMillis()}"

        //setup data to add in db for comment
        val hashMap = HashMap<String, Any>()
        hashMap["id"] = "$timestamp"
        hashMap["bookId"] = "$bookId"
        hashMap["timestamp"] = "$timestamp"
        hashMap["comment"] = "$comment"
        hashMap["uid"] = "${firebaseAuth.uid}"

        //DB path to add data into it
        //Book > bookId > comments> commentId> commentData
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId).child("Comments").child(timestamp)
            .setValue(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Comment added..", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e->
                progressDialog.dismiss()
                Toast.makeText(this, "Failed to add comment due to ${e.message}..", Toast.LENGTH_SHORT).show()
            }
    }

    private val requestStoragePermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted){
                Log.d(TAG, "onCreate: STORAGE PERMISSION is  granted")
            } else {
                Log.d(TAG, "onCreate: STORAGE PERMISSION is  denied")
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
    }

    private fun downloadBook(){
        //progressBar
        Log.d(TAG, "Download Book")
        progressDialog.setMessage("Download Book")
        progressDialog.show()

        //download book from firebase storage using url
        val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl)
        storageReference.getBytes(Constants.MAX_BYTE_PDF)
            .addOnSuccessListener { bytes ->
                Log.d(TAG, "downloadBook: Book DownLoaded ....")
                saveToDownloadsFolder(bytes)

            }
            .addOnFailureListener { e->
                progressDialog.dismiss()
                Log.d(TAG, "downloadBook: Failed to Book Download book due to ${e.message}")
                Toast.makeText(this, "Failed to Book Download book due to ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveToDownloadsFolder(bytes: ByteArray?) {
        Log.d(TAG, "saveToDownloadsFolder: saving Download Book")
        val nameWithExtention = "${System.currentTimeMillis()}.pdf"
        try {
            val downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            downloadsFolder.mkdirs()

            val filePath = downloadsFolder.path + "/" + nameWithExtention
            val out = FileOutputStream(filePath)
            out.write(bytes)
            out.close()
            Toast.makeText(this, "Saving to download Folder", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "saveToDownloadsFolder:Saving to download Folder ")
            progressDialog.dismiss()

            incrementDownloadCount()

        }catch (e: Exception) {
            progressDialog.dismiss()
            Log.d(TAG, "saveToDownloadsFolder: failed to save due to ${e.message}")
            Toast.makeText(this, "Saving to download Folder", Toast.LENGTH_SHORT).show()
        }
    }

    private fun incrementDownloadCount() {
        //increment downloads count to firebase db
        Log.d(TAG,"incrementDownloadCount:")
            // 1.get previous download count
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var downloadsCount = "${snapshot.child("downloadsCount").value}"
                    Log.d(TAG, "sonDataChange: Current Download count: $downloadsCount ")

                    if (downloadsCount == "" || downloadsCount == "null") {
                        downloadsCount = "0"
                    }

                    //convert to long and increment 1
                    val newDownloadCount: Long = downloadsCount.toLong() + 1
                    Log.d(TAG, "sonDataChange: New Download count: $newDownloadCount ")

                    //setup data to update to db
                    val hashMap: HashMap<String, Any> = HashMap()
                    hashMap["downloadsCount"] = newDownloadCount

                    //2, update new incremented download to db
                    val dbRef = FirebaseDatabase.getInstance().getReference("Books")
                    dbRef.child(bookId)
                        .updateChildren(hashMap)
                        .addOnSuccessListener {
                            Log.d(TAG, "sonDataChange: Download Count incremented")
                        }
                        .addOnFailureListener { e->
                            Log.d(TAG, "sonDataChange: Failed increment due to ${e.message} ")
                        }




                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

    private fun loadBookDetails() {
        //Books> bookId> details
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    //get data
                    val categoryId = "${snapshot.child("categoryId").value}"
                    val description = "${snapshot.child("description").value}"
                    val downloadsCount = "${snapshot.child("downloadsCount").value}"
                    val timestamp = "${snapshot.child("timestamp").value}"
                    bookTitle = "${snapshot.child("title").value}"
                    val uid = "${snapshot.child("uid").value}"
                    bookUrl = "${snapshot.child("url").value}"
                    val viewCount = "${snapshot.child("viewCount").value}"

                    //format date
                    val date = MyApplication.formatTimeStamp(timestamp.toLong())

                    MyApplication.loadCategory(categoryId, binding.tvCategory)
                    //load pdf category
                    MyApplication.loadPdfFromUrlSinglePage("$bookUrl","$bookTitle", binding.pdfView, binding.progressBar, binding.tvPages)
                    //load pdf size
                    MyApplication.loadPdfSize("$bookUrl", "$bookTitle", binding.tvSize)

                    //set
                    binding.tvTitle.text = bookTitle
                    binding.tvDescription.text = description
                    binding.tvViews.text = viewCount
                    binding.tvDownLoad.text = downloadsCount
                    binding.tvDate.text = date

                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }


    private fun checkIsFavorite() {
        Log.d(TAG, "checkIsFavorite: Check if book is in fav or not")

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Favorites").child(bookId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    isInMyFavorite = snapshot.exists()
                    if (isInMyFavorite) {
                        Log.d(TAG, "onDataChange: available in favorite")
                        binding.btnFavorite.setCompoundDrawablesRelativeWithIntrinsicBounds(0,
                            R.drawable.ic_ok_favorite, 0, 0)
                        binding.btnFavorite.text = "Remove Favorite"
                    } else {
                        Log.d(TAG, "onDataChange: not available in favorite")
                        binding.btnFavorite.setCompoundDrawablesRelativeWithIntrinsicBounds(0,
                            R.drawable.ic_favorite, 0, 0)
                        binding.btnFavorite.text = "Add Favorite"
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }

    private fun addToFavorite() {

        Log.d(TAG, "addToFavorite: add to fav")
        val timestamp = System.currentTimeMillis()

        //setup data
        val hashMap = HashMap<String, Any>()
        hashMap["bookId"] = bookId
        hashMap["timestamp"] = timestamp

        //save to db
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Favorites").child(bookId)
            .setValue(hashMap)
            .addOnSuccessListener {
                Log.d(TAG, "addToFavorite: add successfully")
                Toast.makeText(this, "Added to favorite", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e->
                Log.d(TAG, "addToFavorite: fail add to fav due to ${e.message}")
                Toast.makeText(this, "fail add to fav due to ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

}