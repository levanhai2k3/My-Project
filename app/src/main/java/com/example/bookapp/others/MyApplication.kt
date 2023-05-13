package com.example.bookapp.others

import android.app.Application
import android.app.ProgressDialog
import android.content.Context
import android.text.format.DateFormat
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.example.bookapp.activities.PdfDetailActivity
import com.github.barteksc.pdfviewer.PDFView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

import java.util.*
import kotlin.collections.HashMap

class MyApplication: Application() {

    override fun onCreate() {
        super.onCreate()
    }

    companion object {

        fun formatTimeStamp(timestamp: Long): String {
            val cal = Calendar.getInstance(Locale.ENGLISH)
            cal.timeInMillis = timestamp

            return DateFormat.format("dd/MM/yyyy", cal).toString()
        }

        fun loadPdfSize(pdfUrl: String, pdfTitle: String, tvSize: TextView) {
            val TAG = "PDF_SIZE_TAG"

            val ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
            ref.metadata
                .addOnSuccessListener {storageMetadata ->
                    Log.d(TAG, "loadPdfSize:  got metadata ")
                    val bytes = storageMetadata.sizeBytes.toDouble()
                    Log.d(TAG, "loadPdfSize:  Size Bytes $bytes ")

                    val kb = bytes/1024
                    val mb = kb/1024
                    if (mb>1){
                        tvSize.text = "${String.format("%.2f", mb)} MB"
                    } else if (kb >=1) {
                        tvSize.text = "${String.format("%.2f", kb)} KB"
                    }else {
                        tvSize.text = "${String.format("%.2f", bytes)} bytes"
                    }
                }
                .addOnFailureListener { e->
                    Log.d(TAG, "loadPdfSize: Failed to get metadata due to ${e.message}")
                }
        }


        fun loadPdfFromUrlSinglePage(
            pdfUrl: String,
            pdfTitle: String,
            pdfView: PDFView,
            progressBar: ProgressBar,
            tvPages: TextView?
        ) {
            val TAG = "PDF_THUMBNAIL_TAG"

            val ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
            ref.getBytes(Constants.MAX_BYTE_PDF)
                .addOnSuccessListener { bytes ->
                    Log.d(TAG, "loadPdfSize:  Size Bytes $bytes ")
                    //Set to PDFView
                    pdfView.fromBytes(bytes)
                        .pages(0)
                        .spacing(0)
                        .swipeHorizontal(false)
                        .enableSwipe(false)
                        .onError { t->
                            progressBar.visibility = View.INVISIBLE
                            Log.d(TAG, "loadPdfFromUrlSinglePage: ${t.message} ")
                        }
                        .onPageError { page, t ->
                            progressBar.visibility = View.INVISIBLE
                            Log.d(TAG, "loadPdfFromUrlSinglePage: ${t.message} ")
                        }
                        .onLoad { nbPages ->
                            Log.d(TAG, "loadPdfFromUrlSinglePage: $nbPages ")
                            progressBar.visibility = View.INVISIBLE

                            if (tvPages != null) {
                                tvPages.text = "$nbPages"
                            }
                        }
                        .load()
                }
                .addOnFailureListener { e->
                    Log.d(TAG, "loadPdfSize: Failed to get metadata due to ${e.message}")
                }
        }


        fun loadCategory(categoryId: String, tvCategory: TextView) {
            val ref = FirebaseDatabase.getInstance().getReference("Categories")
            ref.child(categoryId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val category = "${snapshot.child("category").value}"
                        tvCategory.text = category
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })
        }

        fun deleteBook(context: Context,//use when required e.g for progressDialog, Toast
                       bookId: String,//to delete book in db
                       bookUrl: String,// delete book from firebase storage
                       bookTitle: String) { //show in dialog

            val TAG = "DELETE_BOOK_TAG"

            Log.d(TAG, "deleteBook: Deleting..")

                //progress dialog
            val progressDialog = ProgressDialog(context)
            progressDialog.setTitle("Please wait")
            progressDialog.setMessage("Deleting $bookTitle")
            progressDialog.setCanceledOnTouchOutside(false)
            progressDialog.show()


            Log.d(TAG, "deleteBook: Deleting from storage")
            val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl)
            storageReference.delete()
                .addOnSuccessListener {
                    Log.d(TAG, "deleteBook: Deleting from storage")
                    Log.d(TAG, "deleteBook: Deleting from db now..")

                    val ref = FirebaseDatabase.getInstance().getReference("Books")
                    ref.child(bookId)
                        .removeValue()
                        .addOnSuccessListener {
                            progressDialog.dismiss()
                            Log.d(TAG,"Deleted from db too...")
                            Toast.makeText(context, "Successfully deleted..", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e->
                            progressDialog.dismiss()
                            Log.d(TAG,"Failed to delete from storage due to ${e.message}")
                            Toast.makeText(context, "Failed to delete due to ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
                .addOnFailureListener { e->
                    progressDialog.dismiss()
                    Log.d(TAG,"Failed to delete from storage due to ${e.message}")
                    Toast.makeText(context, "Failed to delete due to ${e.message}", Toast.LENGTH_SHORT).show()
                }

        }

        fun incrementBookViewCount(bookId: String) {
            //get current book view count
            val ref = FirebaseDatabase.getInstance().getReference("Books")
            ref.child(bookId)
                .addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        //get view count
                        var viewCount = "${snapshot.child("viewCount").value}"

                        if (viewCount == "" || viewCount == null) {
                            viewCount = "0"
                        }

                        val newViewCount = viewCount.toLong() + 1

                        //setup data to update data
                        val hashMap = HashMap<String, Any>()
                        hashMap["viewCount"] = newViewCount

                        //set to db
                        val dbRef = FirebaseDatabase.getInstance().getReference("Books")
                        dbRef.child(bookId)
                            .updateChildren(hashMap)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                })
        }


        public fun removeToFavorite(context: Context, bookId: String) {
            val TAG = "REMOVE_FAV_TAG"
            Log.d(TAG, "removeToFavorite: Removing from fav")

            val firebaseAuth = FirebaseAuth.getInstance()
            val ref = FirebaseDatabase.getInstance().getReference("Users")
            ref.child(firebaseAuth.uid!!).child("Favorites").child(bookId)
                .removeValue()
                .addOnSuccessListener {
                    Log.d(TAG,"removeToFavorite: remove fav")
                    Toast.makeText(context, "Removed to favorite", Toast.LENGTH_SHORT).show()

                }
                .addOnFailureListener {e->
                    Log.d(TAG, "removeToFavorite: fail remove due to ${e.message}")
                    Toast.makeText(context, "fail remove to fav due to ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }


    }

}