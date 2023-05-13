package com.example.bookapp.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.example.bookapp.R
import com.example.bookapp.adapters.FavoritePdfAdapter
import com.example.bookapp.databinding.ActivityProfileBinding
import com.example.bookapp.models.PdfModel
import com.example.bookapp.others.MyApplication
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProfileActivity : AppCompatActivity() {

    //viewbinding
    private lateinit var binding: ActivityProfileBinding

    //firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    //array list to hold books
    private lateinit var booksArrayList: ArrayList<PdfModel>
    private lateinit var favoritePdfAdapter: FavoritePdfAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        loadUserInfo()
        loadFavoriteBooks()

        //btn back
        binding.btnBack.setOnClickListener {
            onBackPressed()
        }

        //open edit profile
        binding.btnEditProfile.setOnClickListener {
            startActivity(Intent(this, ProfileEditActivity::class.java))
        }

    }

    private fun loadUserInfo() {

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    //get user info
                    val email = "${snapshot.child("email").value}"
                    val name = "${snapshot.child("name").value}"
                    val profileImage = "${snapshot.child("profileImage").value}"
                    val timestamp = "${snapshot.child("timestamp").value}"
                    val uid = "${snapshot.child("uid").value}"
                    val userType = "${snapshot.child("userType").value}"

                    //convert timestamp to proper date format
                    val formattedDate = MyApplication.formatTimeStamp(timestamp.toLong())

                    //set data
                    binding.tvName.text = name
                    binding.tvEmail.text = email
                    binding.tvMemberDate.text = formattedDate
                    binding.tvAccountType.text = userType

                    //set images

                    try {
                        Glide.with(this@ProfileActivity)
                            .load(profileImage)
                            .placeholder(R.drawable.ic_grayperson_24)
                            .into(binding.ivProfile)

                    } catch (e: Exception) {

                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }

    private fun loadFavoriteBooks() {

        //init arraylist
        booksArrayList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("Favorites")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    booksArrayList.clear()
                    for (ds in snapshot.children) {

                        //get only id of the book, rest of the info we have loaded
                        val bookId = "${ds.child("bookId").value}"

                        //set to model
                        val pdfModel = PdfModel()
                        pdfModel.id = bookId

                        //add model to list
                        booksArrayList.add(pdfModel)
                    }
                    //set number of favorite books
                    binding.tvFavoriteBookCount.text = "${booksArrayList.size}"

                    //set up adapter
                    favoritePdfAdapter = FavoritePdfAdapter(this@ProfileActivity, booksArrayList)

                    //set adapter to recyclerview
                    binding.rvFavorite.adapter = favoritePdfAdapter

                    favoritePdfAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }
}