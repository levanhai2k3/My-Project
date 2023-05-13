package com.example.bookapp.filters

import android.widget.Filter
import com.example.bookapp.adapters.AdminPdfAdapter
import com.example.bookapp.models.PdfModel


class AdminPdfFilter : Filter {

    var filterList: ArrayList<PdfModel>

    var adminPdfAdapter: AdminPdfAdapter

    //constructor
    constructor(filterList: ArrayList<PdfModel>, adminPdfAdapter: AdminPdfAdapter) {
        this.filterList = filterList
        this.adminPdfAdapter = adminPdfAdapter
    }

    override fun performFiltering(constraint: CharSequence?): FilterResults {
        var constraint: CharSequence? = constraint
        val results = FilterResults()
        if (constraint != null && constraint.isNotEmpty()) {
            constraint = constraint.toString().lowercase()
            var filteredModels = ArrayList<PdfModel>()
            for (i in filterList.indices){
                if (filterList[i].title.lowercase().contains(constraint)) {
                    filteredModels.add(filterList[i])
                }
            }
            results.count = filteredModels.size
            results.values = filteredModels
        } else {
            results.count = filterList.size
            results.values = filterList
        }
        return results
    }

    override fun publishResults(constraint: CharSequence, results: FilterResults) {
        adminPdfAdapter.pdfArrayList = results.values as ArrayList<PdfModel>

        adminPdfAdapter.notifyDataSetChanged()
    }


}