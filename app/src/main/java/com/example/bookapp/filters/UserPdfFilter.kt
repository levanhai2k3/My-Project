package com.example.bookapp.filters

import android.widget.Filter
import com.example.bookapp.adapters.UserPdfAdapter
import com.example.bookapp.models.PdfModel

class UserPdfFilter: Filter {

    //array list which we want to search
    var filterList: ArrayList<PdfModel>

    //adapter in which filter need to be implemented
    var userPdfAdapter : UserPdfAdapter


    //constructor
    constructor(filterList: ArrayList<PdfModel>, userPdfAdapter: UserPdfAdapter) : super() {
        this.filterList = filterList
        this.userPdfAdapter = userPdfAdapter
    }

    override fun performFiltering(constraint: CharSequence): FilterResults {
        var constraint: CharSequence? = constraint
        val results = FilterResults()

        //value to be search should not be null and not empty
        if (constraint != null && constraint.isNotEmpty()) {

            //change to uppercase or lower case
            constraint = constraint.toString().uppercase()
            val filteredModels = ArrayList<PdfModel>()

            for (i in filterList.indices) {
                if (filterList[i].title.uppercase().contains(constraint)) {
                    //searched value matched with title , add to list
                    filteredModels.add(filterList[i])
                }
            }
            //return filtered list and size
            results.count = filteredModels.size
            results.values = filteredModels
        } else {
            //return original
            results.count = filterList.size
            results.values = filterList
        }
        return results

    }

    override fun publishResults(constraint: CharSequence, results: FilterResults) {

        userPdfAdapter.pdfArrayList = results.values as ArrayList<PdfModel>

        //notify change
        userPdfAdapter.notifyDataSetChanged()
    }
}