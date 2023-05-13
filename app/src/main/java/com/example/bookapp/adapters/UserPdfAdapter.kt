package com.example.bookapp.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.example.bookapp.others.MyApplication
import com.example.bookapp.activities.PdfDetailActivity
import com.example.bookapp.databinding.ItemPdfUserBinding
import com.example.bookapp.filters.UserPdfFilter
import com.example.bookapp.models.PdfModel

class UserPdfAdapter: RecyclerView.Adapter<UserPdfAdapter.UserPdfHolder>, Filterable{

    //context, get using constructor
    private var context: Context

    //array list to hold pdf, get using constructor
    public var pdfArrayList: ArrayList<PdfModel>

    //array list to hold filtered pdfs
    public var filterList: ArrayList<PdfModel>

    //view binding
    private lateinit var binding: ItemPdfUserBinding

    private var filter: UserPdfFilter? = null

    constructor(context: Context, pdfArrayList: ArrayList<PdfModel>) {
        this.context = context
        this.pdfArrayList = pdfArrayList
        this.filterList = pdfArrayList
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserPdfHolder {
        binding = ItemPdfUserBinding.inflate(LayoutInflater.from(context), parent, false )

        return UserPdfHolder(binding.root)
    }

    override fun onBindViewHolder(holder: UserPdfHolder, position: Int) {
        //get data
        val model = pdfArrayList[position]
        val bookId = model.id
        val categoryId = model.categoryId
        val title = model.title
        val description = model.description
        val uid = model.uid
        val url = model.url
        val timestamp = model.timestamp

        //convert time
        val date = MyApplication.formatTimeStamp(timestamp)

         //set data
        holder.tvTitle.text = title
        holder.tvDescription.text = description
        holder.tvDate.text = date

        MyApplication.loadPdfFromUrlSinglePage(url, title, holder.pdfView, holder.progressBar, null)

        MyApplication.loadCategory(categoryId, holder.tvCategory)

        MyApplication.loadPdfSize(url, title, holder.tvSize)


        holder.itemView.setOnClickListener {
            val intent = Intent(context, PdfDetailActivity::class.java)
            intent.putExtra("bookId", bookId)
            context.startActivity(intent)
        }

    }

    override fun getItemCount(): Int {
        return pdfArrayList.size
    }

    override fun getFilter(): Filter {
        if (filter == null) {
            filter = UserPdfFilter(filterList, this)

        }
        return filter as UserPdfFilter
    }

    inner class UserPdfHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        var pdfView = binding.pdfView
        var progressBar = binding.progressBar
        var tvTitle = binding.tvTitle
        var tvDescription = binding.tvDescription
        var tvCategory = binding.tvCategory
        var tvSize = binding.tvSize
        var tvDate = binding.tvDate
    }


}