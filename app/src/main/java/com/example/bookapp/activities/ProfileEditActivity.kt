package com.example.bookapp.activities

import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.PopupMenu
import com.bumptech.glide.Glide
import com.example.bookapp.R
import com.example.bookapp.databinding.ActivityProfileEditBinding
import com.example.bookapp.others.MyApplication
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class ProfileEditActivity : AppCompatActivity() {

    //view binding
    private lateinit var binding: ActivityProfileEditBinding

    //firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    //porgress Dialog
    private lateinit var progressDialog: ProgressDialog

    //image uri (which we will pick)
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //setup progress
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()
        loadUserInfo()


        //go back
        binding.btnBack.setOnClickListener {
            onBackPressed()
        }

        //pick image
        binding.ivProfile.setOnClickListener {
            showImageAttachMenu()
        }
        //update
        binding.btnUpdate.setOnClickListener {
            validateData()
        }
    }

    private var name = ""
    private fun validateData() {
        //getdata
        name = binding.edtName.text.toString().trim()

        //validate data
        if (name.isEmpty()) {
            Toast.makeText(this, "Enter Name",Toast.LENGTH_SHORT ).show()
        }
        else {
            //name is entered
            if (imageUri == null) {
                updateProfile("")
            }
            else {
                //need to update with image
                upLoadImage()
            }
        }
    }

    private fun upLoadImage() {
        progressDialog.setMessage("Uploading profile image")
        progressDialog.show()

        //image path and name, use uid to replace previous
        val filePathAndName = "ProfileImages/" + firebaseAuth.uid

        //storage reference
        val reference = FirebaseStorage.getInstance().getReference(filePathAndName)
        reference.putFile(imageUri!!)
            .addOnSuccessListener { taskSnapshot ->
                val uriTask: Task<Uri> = taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful);
                val uploadedImageUrl = "${uriTask.result}"

                updateProfile(uploadedImageUrl)
            }
            .addOnFailureListener { e->
                progressDialog.dismiss()
                Toast.makeText(this, "Failed to upload image due to ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateProfile(uploadedImageUrl: String) {
        progressDialog.setMessage("Updating profile...")

        //setup info to update to db
        val hashMap: HashMap<String, Any> = HashMap()
        hashMap["name"] = "$name"
        if (imageUri !=  null) {
            hashMap["profileImage"] = uploadedImageUrl
        }

        //update to db
        val reference = FirebaseDatabase.getInstance().getReference("Users")
        reference.child(firebaseAuth.uid!!)
            .updateChildren(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e->
                progressDialog.dismiss()
                Toast.makeText(this, "Failed to upload profile due to ${e.message}", Toast.LENGTH_SHORT).show() }

    }

    private fun loadUserInfo() {

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    //get user info
                    val name = "${snapshot.child("name").value}"
                    val profileImage = "${snapshot.child("profileImage").value}"
                    val timestamp = "${snapshot.child("timestamp").value}"


                    //convert timestamp to proper date format
                    val formattedDate = MyApplication.formatTimeStamp(timestamp.toLong())

                    //set data
                    binding.edtName.setText("$name")


                    //set images

                    try {
                        Glide.with(this@ProfileEditActivity)
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

    private fun showImageAttachMenu() {
        //setup popup menu
        val popupMenu = PopupMenu(this, binding.ivProfile)
        popupMenu.menu.add(Menu.NONE, 0, 0, "Camera")
        popupMenu.menu.add(Menu.NONE, 1, 1, "Gallery")
        popupMenu.show()

        //popup menu item click
        popupMenu.setOnMenuItemClickListener { item ->
            //get id of clicked item
            val id = item.itemId
            if (id == 0) {
                //camera click
                pickImageCamera()

            } else if (id == 1) {
                //gallery click
                pickImageGallery()
            }
            true
        }
    }

    private fun pickImageCamera() {
        //intent to pick image from camera
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "Temp_Title")
        values.put(MediaStore.Images.Media.DESCRIPTION, "Temp_Description")

        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)

        cameraActivityResultLauncher.launch(intent)
    }

    private fun pickImageGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"

        galleryActivityResultLauncher.launch(intent)
    }

    //result of camera intent
    private val cameraActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult> {result ->
            //get uri of image
            if(result.resultCode == Activity.RESULT_OK) {
                val data = result.data
//                imageUri = data!!.data

                //set to imageView
                binding.ivProfile.setImageURI(imageUri)
            }
            else {
                //cancelled
                Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    )

    //result of gallery intent
    private val galleryActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult> {result ->
            //get uri of image
            if(result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                imageUri = data!!.data

                //set to imageView
                binding.ivProfile.setImageURI(imageUri)
            }
            else {
                //cancelled
                Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    )


}