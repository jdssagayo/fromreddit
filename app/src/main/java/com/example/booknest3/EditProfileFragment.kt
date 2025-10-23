package com.example.booknest3

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
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
import java.lang.IllegalArgumentException

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
                            try {
                                if (photoUrl.startsWith("http")) {
                                    // Handle old Firebase Storage URLs
                                    Glide.with(this@EditProfileFragment)
                                        .load(photoUrl)
                                        .into(profileImageView)
                                } else {
                                    // Handle new Base64 strings
                                    val imageBytes = Base64.decode(photoUrl, Base64.DEFAULT)
                                    Glide.with(this@EditProfileFragment)
                                        .load(imageBytes)
                                        .into(profileImageView)
                                }
                            } catch (e: IllegalArgumentException) {
                                Log.e("EditProfileFragment", "Error decoding Base64 image", e)
                                // Optionally set a placeholder
                            }
                        }
                    } else {
                        Log.d("EditProfileFragment", "No such document")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("EditProfileFragment", "Error loading profile", e)
                    Toast.makeText(context, "Failed to load profile: ${e.message}", Toast.LENGTH_SHORT).show()
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
            try {
                val inputStream = requireContext().contentResolver.openInputStream(selectedImageUri!!)
                val imageBytes = inputStream?.readBytes()
                inputStream?.close()
                if (imageBytes != null) {
                    val imageBase64 = Base64.encodeToString(imageBytes, Base64.DEFAULT)
                    updateProfileData(user.uid, name, username, bio, imageBase64)
                } else {
                    Toast.makeText(requireContext(), "Failed to read image", Toast.LENGTH_SHORT).show()
                    saveButton.isEnabled = true
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Image processing failed: ${e.message}", Toast.LENGTH_SHORT).show()
                saveButton.isEnabled = true
            }
        } else {
            // No new image selected, just update text fields
            updateProfileData(user.uid, name, username, bio, null)
        }
    }

    private fun updateProfileData(userId: String, name: String, username: String, bio: String, newPhotoBase64: String?) {
        val user = auth.currentUser!!

        val userMap = mutableMapOf<String, Any>(
            "username" to username,
            "bio" to bio
        )
        // Only include the photoUrl in the map if a new image was selected.
        // If it's null, Firestore won't update the field.
        if (newPhotoBase64 != null) {
            userMap["photoUrl"] = newPhotoBase64
        }

        val profileUpdatesBuilder = UserProfileChangeRequest.Builder()
            .setDisplayName(name)

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
