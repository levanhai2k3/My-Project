package com.example.bookapp.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.bookapp.others.MyApplication
import com.example.bookapp.activities.PdfDetailActivity
import com.example.bookapp.activities.PdfEditActivity
import com.example.bookapp.databinding.ItemPdfAdminBinding
import com.example.bookapp.filters.AdminPdfFilter
import com.example.bookapp.models.PdfModel

class AdminPdfAdapter: RecyclerView.Adapter<AdminPdfAdapter.HolderPdfAdmin>, Filterable {

    private var context: Context

    public var pdfArrayList: ArrayList<PdfModel>
    private val filterList: ArrayList<PdfModel>

    private lateinit var binding: ItemPdfAdminBinding

    //filter object
    private var filter : AdminPdfFilter? = null

    constructor(context: Context, pdfArrayList: ArrayList<PdfModel>) : super() {
        this.context = context
        this.pdfArrayList = pdfArrayList
        this.filterList = pdfArrayList
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderPdfAdmin {
        binding = ItemPdfAdminBinding.inflate(LayoutInflater.from(context), parent, false)

        return HolderPdfAdmin(binding.root)
    }

    override fun onBindViewHolder(holder: HolderPdfAdmin, position: Int) {

        //get data
        val model = pdfArrayList[position]
        val pdfId = model.id
        val categoryId = model.categoryId
        val title = model.title
        val description = model.description
        val pdfUrl = model.url
        val timestamp = model.timestamp

        //convert timestamp to dd//MM//yy
        val formattedDate = MyApplication.formatTimeStamp(timestamp)

        //set data
        holder.tvTitle.text = title
        holder.tvDescription.text = description
        holder.tvDate.text = formattedDate

        //load further detail like category, pdf from url, pdf size

        //categoryid
        MyApplication.loadCategory(categoryId, holder.tvCategory)

        //load pdf thumbnail
        MyApplication.loadPdfFromUrlSinglePage(pdfUrl, title, holder.pdfView, holder.progressBar, null)

        //load pdf size
        MyApplication.loadPdfSize(pdfUrl, title, holder.tvSize)

        holder.btnMore.setOnClickListener {
            moreOptionsDialog(model, holder)
        }

        //go to pdf detail
        holder.itemView.setOnClickListener {
            val intent = Intent(context, PdfDetailActivity::class.java)
            intent.putExtra("bookId", pdfId)
            context.startActivity(intent)
        }

    }

    private fun moreOptionsDialog(model: PdfModel, holder: AdminPdfAdapter.HolderPdfAdmin) {
        //get id , url, title of book
        val bookId = model.id
        val bookUrl = model.url
        val bookTitle = model.title

        //options to show in dialog
        val options = arrayOf("Edit", "Delete")

        //alert dialog
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Choose Option")
            .setItems(options){ dialog, position ->
                if (position == 0) {
                    //Edit
                    val intent = Intent(context, PdfEditActivity::class.java)
                    intent.putExtra("bookId", bookId)
                    context.startActivity(intent)
                } else if (position == 1) {

                    // show confirmation if first u need..
                    MyApplication.deleteBook(context, bookId, bookUrl, bookTitle)
                }
            }
            .show()
    }

    override fun getItemCount(): Int {
        return pdfArrayList.size
    }



    override fun getFilter(): Filter {
        if (filter == null) {
            filter = AdminPdfFilter(filterList, this)
        }
        return filter as AdminPdfFilter
    }




    inner class HolderPdfAdmin(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val pdfView = binding.pdfView
        val progressBar = binding.progressBar
        val tvTitle = binding.tvTitle
        val tvDescription = binding.tvDescription
        val tvCategory = binding.tvCategory
        val tvSize = binding.tvSize
        val tvDate = binding.tvDate
        val btnMore = binding.btnMore
    }
}