package com.example.bookapp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bookapp.R
import com.example.bookapp.databinding.ItemCommentBinding
import com.example.bookapp.models.CommentModel
import com.example.bookapp.others.MyApplication
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class CommentAdapter: RecyclerView.Adapter<CommentAdapter.CommentHolder> {
    //context
    val context: Context

    //arraylist to hold comments
    val commentArrayList: ArrayList<CommentModel>

    //view binding
    private lateinit var binding: ItemCommentBinding

    //firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    constructor(context: Context, commentArrayList: ArrayList<CommentModel>) {
        this.context = context
        this.commentArrayList = commentArrayList

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentHolder {
        binding = ItemCommentBinding.inflate(LayoutInflater.from(context), parent, false)
        return CommentHolder(binding.root)
    }

    override fun onBindViewHolder(holder: CommentHolder, position: Int) {
        //get data
        val model = commentArrayList[position]

        val id = model.id
        val bookId = model.bookId
        val comment = model.comment
        val uid = model.uid
        val timestamp = model.timestamp

        //format timestamp
        val date = MyApplication.formatTimeStamp(timestamp.toLong())


        //set data
        holder.tvDate.text = date
        holder.tvComment.text = comment

        loadUserDetails(model, holder)

        holder.itemView.setOnClickListener {
            if (firebaseAuth.currentUser != null && firebaseAuth.uid == uid) {
                deleteCommentDialog(model, holder)
            }
        }
    }

    private fun deleteCommentDialog(model: CommentModel, holder: CommentAdapter.CommentHolder) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Delete Comment")
            .setMessage("Are you sure you want delete this comment?")
            .setPositiveButton("DELETE"){d,e ->

                val bookId = model.bookId
                val commentId = model.id
                //delete comment
                val ref = FirebaseDatabase.getInstance().getReference("Books")
                ref.child(bookId).child("Comments").child(commentId)
                    .removeValue()
                    .addOnSuccessListener {
                        Toast.makeText(context, "Delete Comment Successfully", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e->
                        Toast.makeText(context, "Failed to delete comment due to ${e.message}",Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("CANCEL"){d,e->
                d.dismiss()
            }
            .show()
    }

    private fun loadUserDetails(model: CommentModel, holder: CommentAdapter.CommentHolder) {
        val uid = model.uid
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    //get name, profile image
                    val name = "${snapshot.child("name").value}"
                    val profileImage = "${snapshot.child("profileImage").value}"

                    //set data
                    holder.tvName.text = name
                    try {
                        Glide.with(context)
                            .load(profileImage)
                            .placeholder(R.drawable.ic_grayperson_24)
                            .into(holder.ivProfile)

                    } catch (e: Exception) {

                        holder.ivProfile.setImageResource(R.drawable.ic_grayperson_24)

                    }

                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }


    override fun getItemCount(): Int {
        return commentArrayList.size
    }

    inner class CommentHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val ivProfile = binding.ivProfile
        val tvName = binding.tvName
        val tvDate = binding.tvDate
        val tvComment = binding.tvComment
    }
}