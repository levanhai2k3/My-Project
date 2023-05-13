package com.example.bookapp.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.bookapp.activities.PdfListAdminActivity
import com.example.bookapp.databinding.ItemCategoryBinding
import com.example.bookapp.filters.CategoryFilter
import com.example.bookapp.models.CategoryModel
import com.google.firebase.database.FirebaseDatabase

class CategoryAdapter: RecyclerView.Adapter<CategoryAdapter.CategoryHolder>, Filterable{

    private val context: Context
    public var categoryArrayList: ArrayList<CategoryModel>
    private var filterList: ArrayList<CategoryModel>

    private var filter: CategoryFilter? = null

    //viewbinding
    private lateinit var binding: ItemCategoryBinding

    constructor(context: Context, categoryArrayList: ArrayList<CategoryModel>) {
        this.context = context
        this.categoryArrayList = categoryArrayList
        this.filterList = categoryArrayList
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryHolder {
        binding = ItemCategoryBinding.inflate(LayoutInflater.from(context), parent,false)
        return CategoryHolder(binding.root)
    }

    override fun onBindViewHolder(holder: CategoryHolder, position: Int) {
       //get,set data........ click

        val model = categoryArrayList[position]
        val id = model.id
        val category = model.category
        val uid = model.uid
        val timestamp = model.timestamp

        //set data
        holder.tvCategory.text = category

        holder.btnDelete.setOnClickListener {
            //confirm before delete
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Delete")
                .setMessage("Are sure you want to delete this category")
                .setPositiveButton("Confirm") {a, d ->
                    Toast.makeText(context, "Deleting...", Toast.LENGTH_SHORT).show()
                    deleteCategory(model, holder)

                }
                .setNegativeButton("Cancel") {a, d ->
                    a.dismiss()
                }
                .show()
        }
        //start pdf lít admin ,also pas id, title
        holder.itemView.setOnClickListener {
            val intent = Intent(context, PdfListAdminActivity::class.java)
            intent.putExtra("categoryId", id)
            intent.putExtra("category", category)
            context.startActivity(intent)
        }
    }

    private fun deleteCategory(model: CategoryModel, holder: CategoryHolder) {
        //get id of category to delete
        val id = model.id

        //firebase Db> Categories> categoryID
        val ref = FirebaseDatabase.getInstance().getReference("Categories")
        ref.child(id)
            .removeValue()
            .addOnSuccessListener {
                Toast.makeText(context, "Deleted...", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e->
                Toast.makeText(context, "Unable to delete due to ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun getItemCount(): Int {
        return categoryArrayList.size
    }


    inner class CategoryHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        var tvCategory:TextView = binding.tvCategory
        var btnDelete:ImageButton = binding.btnDelete
    }

    override fun getFilter(): Filter {
        if (filter == null) {
            filter = CategoryFilter(filterList, this)
        }
        return filter as CategoryFilter
    }


}
