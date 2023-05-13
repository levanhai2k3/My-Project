package com.example.bookapp.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import com.example.bookapp.R
import com.example.bookapp.adapters.UserPdfAdapter
import com.example.bookapp.databinding.FragmentBooksUserBinding
import com.example.bookapp.models.PdfModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class BooksUserFragment : Fragment {

//view binding
    private lateinit var binding: FragmentBooksUserBinding

    public companion object {
        private const val TAG = "BOOK_USER_TAG"

        //receive data from activity to load book
        public fun newInstance(categoryId: String, category: String, uid: String): BooksUserFragment {
            val fragment  = BooksUserFragment()

            //put data to bundle intent
            val args = Bundle()
            args.putString("categoryId", categoryId)
            args.putString("category", category)
            args.putString("uid", uid)
            fragment.arguments = args
            return fragment
        }
    }

    private var categoryId = ""
    private var category = ""
    private var uid = ""

    private lateinit var pdfArrayList: ArrayList<PdfModel>
    private lateinit var userPdfAdapter: UserPdfAdapter

    constructor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //get arguments that we passed in newInstance method
        val args = arguments
        if (args != null) {
            categoryId = args.getString("categoryId")!!
            category = args.getString("category")!!
            uid = args.getString("uid")!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding  = FragmentBooksUserBinding.inflate(LayoutInflater.from(context), container, false)

        //load pdf
        Log.d(TAG, "onCreateView: Category: $category")

        if (category == "All" && category != null) {
            //load All book
            loadAllBook()
        }
        else if (category == "Most Viewed" && category != null) {
            //load most viewed books
            loadMostViewedDownloadedBooks("viewCount")
        }
        else if (category == "Most Downloaded" && category != null) {
            //load most download books
            loadMostViewedDownloadedBooks("downloadsCount")
        }
        else {
            //load selected category books
            loadCategorizedBooks()
        }

        //search
        binding.edtSearch.addTextChangedListener ( object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                try {
                    userPdfAdapter.filter.filter(s)
                } catch (e: Exception) {
                    Log.d(TAG, "onTextChanged: SEARCH EXCEPTION: ${e.message}")
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        return binding.root
    }

    private fun loadAllBook() {
        //init List
        pdfArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                pdfArrayList.clear()
                for (ds in snapshot.children) {
                    //get data
                    val model = ds.getValue(PdfModel::class.java)
                    //add to list
                    pdfArrayList.add(model!!)
                }
                //setup adapter
                userPdfAdapter = UserPdfAdapter(context!!, pdfArrayList)

                //set adapter to recyclerview
                binding.rvBooks.adapter = userPdfAdapter
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun loadMostViewedDownloadedBooks(orderBy: String) {
        //init List
        pdfArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.orderByChild(orderBy).limitToLast(10)
            .addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                pdfArrayList.clear()
                for (ds in snapshot.children) {
                    //get data
                    val model = ds.getValue(PdfModel::class.java)
                    //add to list
                    pdfArrayList.add(model!!)
                }
                //setup adapter
                userPdfAdapter = UserPdfAdapter(context!!, pdfArrayList)

                //set adapter to recyclerview
                binding.rvBooks.adapter = userPdfAdapter

                pdfArrayList.reverse()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun loadCategorizedBooks() {
        //init List
        pdfArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.orderByChild("categoryId").equalTo(categoryId)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    pdfArrayList.clear()
                    for (ds in snapshot.children) {
                        //get data
                        val model = ds.getValue(PdfModel::class.java)
                        //add to list
                        pdfArrayList.add(model!!)
                    }
                    //setup adapter
                    userPdfAdapter = UserPdfAdapter(context!!, pdfArrayList)

                    //set adapter to recyclerview
                    binding.rvBooks.adapter = userPdfAdapter
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }


}