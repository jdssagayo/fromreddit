package com.example.booknest3

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class EditProfileFragment : Fragment() {

    private lateinit var nameEditText: EditText
    private lateinit var usernameEditText: EditText
    private lateinit var bioEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var profileImageView: ImageView
    private lateinit var changePhotoTextView: TextView

    private var selectedImageUri: Uri? = null

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private val pickImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            selectedImageUri = it.data?.data
            Glide.with(this).load(selectedImageUri).into(profileImageView)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit_profile, container, false)

        nameEditText = view.findViewById(R.id.name_value)
        usernameEditText = view.findViewById(R.id.username_value)
        bioEditText = view.findViewById(R.id.bio_value)
        saveButton = view.findViewById(R.id.save_button)
        profileImageView = view.findViewById(R.id.profile_image)
        changePhotoTextView = view.findViewById(R.id.change_photo_text)

        loadUserProfile()

        changePhotoTextView.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            pickImage.launch(intent)
        }

        saveButton.setOnClickListener {
            saveUserProfile()
        }

        view.findViewById<View>(R.id.back_arrow).setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        return view
    }

    private fun loadUserProfile() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        nameEditText.setText(auth.currentUser?.displayName)
                        usernameEditText.setText(document.getString("username"))
                        bioEditText.setText(document.getString("bio"))
                        val photoUrl = document.getString("photoUrl")
                        if (!photoUrl.isNullOrEmpty()) {
                            Glide.with(this@EditProfileFragment).load(photoUrl).into(profileImageView)
                        }
                    }
                }
        }
    }

    private fun saveUserProfile() {
        val name = nameEditText.text.toString().trim()
        val username = usernameEditText.text.toString().trim()
        val bio = bioEditText.text.toString().trim()
        val user = auth.currentUser ?: return

        saveButton.isEnabled = false // Prevent multiple clicks

        if (selectedImageUri != null) {
            val storageRef = storage.reference.child("profile_images/${user.uid}")
            storageRef.putFile(selectedImageUri!!)
                .addOnSuccessListener { 
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        updateProfileData(user.uid, name, username, bio, uri.toString())
                    }
                }.addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Image upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    saveButton.isEnabled = true
                }
        } else {
            // No new image selected, just update text fields
            updateProfileData(user.uid, name, username, bio, null)
        }
    }

    private fun updateProfileData(userId: String, name: String, username: String, bio: String, newPhotoUrl: String?) {
        val user = auth.currentUser!!

        val userMap = mutableMapOf<String, Any>(
            "username" to username,
            "bio" to bio
        )
        if (newPhotoUrl != null) {
            userMap["photoUrl"] = newPhotoUrl
        }

        val profileUpdatesBuilder = UserProfileChangeRequest.Builder()
            .setDisplayName(name)
        if (newPhotoUrl != null) {
            profileUpdatesBuilder.setPhotoUri(Uri.parse(newPhotoUrl))
        }

        user.updateProfile(profileUpdatesBuilder.build()).addOnCompleteListener { authTask ->
            if (authTask.isSuccessful) {
                db.collection("users").document(userId)
                    .update(userMap)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show()
                        parentFragmentManager.popBackStack()
                    }
                    .addOnFailureListener { firestoreError ->
                        Toast.makeText(requireContext(), "Firestore update failed: ${firestoreError.message}", Toast.LENGTH_SHORT).show()
                        saveButton.isEnabled = true
                    }
            } else {
                Toast.makeText(requireContext(), "Auth profile update failed", Toast.LENGTH_SHORT).show()
                saveButton.isEnabled = true
            }
        }
    }
}
