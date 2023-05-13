package com.example.bookapp.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bookapp.activities.PdfDetailActivity
import com.example.bookapp.databinding.ItemPdfFavoriteBinding
import com.example.bookapp.models.PdfModel
import com.example.bookapp.others.MyApplication
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FavoritePdfAdapter: RecyclerView.Adapter<FavoritePdfAdapter.FavoritePdfHolder> {

    //view binding
    private lateinit var binding: ItemPdfFavoriteBinding

    //context
    private var context: Context
    //array list to hold books
    private var booksArrayList: ArrayList<PdfModel>

    //constructor
    constructor(context: Context, booksArrayList: ArrayList<PdfModel>) {
        this.context = context
        this.booksArrayList = booksArrayList
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoritePdfHolder {
        binding = ItemPdfFavoriteBinding.inflate(LayoutInflater.from(context), parent, false)
        return FavoritePdfHolder(binding.root)
    }

    override fun onBindViewHolder(holder: FavoritePdfHolder, position: Int) {
        //get data
        val model = booksArrayList[position]

        loadBookDetails(model, holder)

        //click item favorite
        holder.itemView.setOnClickListener {
            val intent = Intent(context, PdfDetailActivity::class.java)
            intent.putExtra("bookId", model.id)
            context.startActivity(intent)
        }

        //remove favorite
        holder.btnRemoveFav.setOnClickListener {
            MyApplication.removeToFavorite(context, model.id)
        }
    }

    private fun loadBookDetails(model: PdfModel, holder: FavoritePdfAdapter.FavoritePdfHolder) {
        val bookId = model.id

        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                   //get book info
                    val categoryId = "${snapshot.child("categoryId").value}"
                    val description = "${snapshot.child("description").value}"
                    val downloadsCount = "${snapshot.child("downloadsCount").value}"
                    val timestamp = "${snapshot.child("timestamp").value}"
                    val title = "${snapshot.child("title").value}"
                    val uid = "${snapshot.child("uid").value}"
                    val url = "${snapshot.child("url").value}"
                    val viewCount = "${snapshot.child("viewCount").value}"

                    //set data to model
                    model.isFavorite = true
                    model.title = title
                    model.description = description
                    model.categoryId = categoryId
                    model.timestamp = timestamp.toLong()
                    model.uid = uid
                    model.url = url
                    model.viewCount = viewCount.toLong()
                    model.downloadsCount = downloadsCount.toLong()



                    //format data
                    val date = MyApplication.formatTimeStamp(timestamp.toLong())
                    MyApplication.loadCategory("$categoryId", holder.tvCategory)
                    MyApplication.loadPdfFromUrlSinglePage("$url", "$title", holder.pdfView, holder.progressBar, null)
                    MyApplication.loadPdfSize("$url", "$title", holder.tvSize)

                    holder.tvTitle.text = title
                    holder.tvDescription.text = description
                    holder.tvDate.text = date
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }

    override fun getItemCount(): Int {
        return booksArrayList.size
    }


    inner class FavoritePdfHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //init UI Views
        var pdfView = binding.pdfView
        var progressBar = binding.progressBar
        var tvTitle = binding.tvTitle
        var tvDescription = binding.tvDescription
        var tvCategory = binding.tvCategory
        var tvSize = binding.tvSize
        var tvDate = binding.tvDate
        var btnRemoveFav = binding.btnRemoveFav
    }
}


