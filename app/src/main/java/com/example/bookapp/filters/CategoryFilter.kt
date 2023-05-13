package com.example.bookapp.filters

import android.widget.Filter
import com.example.bookapp.adapters.CategoryAdapter
import com.example.bookapp.models.CategoryModel


class CategoryFilter: Filter {
    //arrlist want to search
    private var filterList: ArrayList<CategoryModel>

    //adapter
    private var categoryAdapter: CategoryAdapter

    constructor(filterList: ArrayList<CategoryModel>, categoryAdapter: CategoryAdapter) : super() {
        this.filterList = filterList
        this.categoryAdapter = categoryAdapter
    }

    override fun performFiltering(constraint: CharSequence?): FilterResults {
        var constraint = constraint
        val results = FilterResults()

        if (constraint != null && constraint.isNotEmpty()) {
            constraint = constraint.toString().uppercase()
            val filteredModels: ArrayList<CategoryModel> = ArrayList()

            for (i in 0 until filterList.size) {
                //validate
                if (filterList[i].category.uppercase().contains(constraint)) {
                    //add to filterList
                    filteredModels.add(filterList[i])
                }
            }
            results.count= filteredModels.size
            results.values = filteredModels
        }else {
            //search value is either null or empty
            results.count = filterList.size
            results.values = filterList
        }
        return results
    }

    override fun publishResults(constraint: CharSequence?, results: FilterResults) {
        //apply filter changes
        categoryAdapter.categoryArrayList = results.values as ArrayList<CategoryModel>

        //notify changes
        categoryAdapter.notifyDataSetChanged()


    }
}
