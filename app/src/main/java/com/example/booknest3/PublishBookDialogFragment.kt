package com.example.booknest3

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.cardview.widget.CardView
import androidx.fragment.app.DialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID

class PublishBookDialogFragment : DialogFragment() {

    private lateinit var bookTitleInput: EditText
    private lateinit var authorNameInput: EditText
    private lateinit var bookDescriptionInput: EditText
    private lateinit var coverImagePreview: ImageView
    private lateinit var uploadPlaceholder: LinearLayout
    private lateinit var bookCoverUploadArea: CardView

    private var imageUri: Uri? = null
    private var draftId: String? = null

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val pickImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let {
                imageUri = it
                coverImagePreview.setImageURI(imageUri)
                coverImagePreview.visibility = View.VISIBLE
                uploadPlaceholder.visibility = View.GONE
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        draftId = arguments?.getString(ARG_DRAFT_ID)
        return inflater.inflate(R.layout.dialog_publish_book, container, false)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.let {
            val width = (resources.displayMetrics.widthPixels * 0.90).toInt()
            it.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT)
            it.setGravity(Gravity.CENTER)
            it.setBackgroundDrawableResource(android.R.color.transparent)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bookTitleInput = view.findViewById(R.id.book_title_input_publish)
        authorNameInput = view.findViewById(R.id.author_name_input_publish)
        bookDescriptionInput = view.findViewById(R.id.book_description_input)
        coverImagePreview = view.findViewById(R.id.cover_image_preview)
        uploadPlaceholder = view.findViewById(R.id.upload_placeholder)
        bookCoverUploadArea = view.findViewById(R.id.book_cover_upload_area)

        arguments?.getString(ARG_BOOK_TITLE)?.let {
            bookTitleInput.setText(it)
        }

        bookCoverUploadArea.setOnClickListener {
            val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickImage.launch(galleryIntent)
        }

        view.findViewById<View>(R.id.close_dialog_button).setOnClickListener { dismiss() }
        view.findViewById<View>(R.id.cancel_publish_button).setOnClickListener { dismiss() }
        view.findViewById<View>(R.id.export_now_button).setOnClickListener { publishBook() }
    }

    private fun publishBook() {
        val title = bookTitleInput.text.toString().trim()
        val author = authorNameInput.text.toString().trim()
        val description = bookDescriptionInput.text.toString().trim()
        val userId = auth.currentUser?.uid

        if (title.isEmpty() || author.isEmpty() || userId == null || draftId == null) {
            Toast.makeText(requireContext(), "Title, author, and image are required.", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("users").document(userId).collection("drafts").document(draftId!!)
            .collection("pages").orderBy("pageNumber").get()
            .addOnSuccessListener { pagesSnapshot ->
                val fullContent = pagesSnapshot.documents.joinToString(separator = "\n\n") { doc ->
                    doc.getString("content") ?: ""
                }

                if (imageUri != null) {
                    try {
                        val inputStream = requireContext().contentResolver.openInputStream(imageUri!!)
                        val imageBytes = inputStream?.readBytes()
                        inputStream?.close()
                        if (imageBytes != null) {
                            val imageBase64 = Base64.encodeToString(imageBytes, Base64.DEFAULT)
                            createBookDocument(userId, title, author, description, imageBase64, fullContent)
                        } else {
                            Toast.makeText(requireContext(), "Failed to read image", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), "Image processing failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    createBookDocument(userId, title, author, description, null, fullContent)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to fetch draft pages: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun createBookDocument(userId: String, title: String, author: String, description: String, coverImageBase64: String?, content: String) {
        val bookData = hashMapOf(
            "title" to title,
            "author" to author,
            "description" to description,
            "authorId" to userId,
            "content" to content,
            "coverImageUrl" to coverImageBase64,
            "publishedAt" to FieldValue.serverTimestamp()
        )

        db.collection("books").add(bookData)
            .addOnSuccessListener {
                deleteDraft(userId, draftId!!)
                Toast.makeText(requireContext(), "Book published!", Toast.LENGTH_SHORT).show()
                dismiss()
                (activity as? MainActivity)?.navigateToHome()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Publishing failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteDraft(userId: String, draftId: String) {
        val draftRef = db.collection("users").document(userId).collection("drafts").document(draftId)
        draftRef.collection("pages").get().addOnSuccessListener { pages ->
            val batch = db.batch()
            pages.documents.forEach { batch.delete(it.reference) }
            batch.delete(draftRef)
            batch.commit()
        }
    }

    companion object {
        private const val ARG_DRAFT_ID = "draft_id"
        private const val ARG_BOOK_TITLE = "book_title"

        fun newInstance(draftId: String, bookTitle: String): PublishBookDialogFragment {
            val args = Bundle()
            args.putString(ARG_DRAFT_ID, draftId)
            args.putString(ARG_BOOK_TITLE, bookTitle)
            val fragment = PublishBookDialogFragment()
            fragment.arguments = args
            return fragment
        }
    }
}
