package com.example.booknest3

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EditDraftFragment : Fragment() {

    private lateinit var titleEditText: TextInputEditText
    private lateinit var contentEditText: TextInputEditText

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var currentDraftId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        currentDraftId = arguments?.getString("DRAFT_ID")
        return inflater.inflate(R.layout.fragment_edit_draft, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        titleEditText = view.findViewById(R.id.title_edit_text)
        contentEditText = view.findViewById(R.id.content_edit_text)
        val saveButton = view.findViewById<Button>(R.id.save_button)
        val deleteButton = view.findViewById<Button>(R.id.delete_button)

        if (currentDraftId != null) {
            loadDraftData()
            deleteButton.visibility = View.VISIBLE
        } else {
            deleteButton.visibility = View.GONE
        }

        saveButton.setOnClickListener { saveDraft() }
        deleteButton.setOnClickListener { deleteDraft() }
    }

    private fun loadDraftData() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId).collection("drafts").document(currentDraftId!!)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val draft = document.toObject(Draft::class.java)
                    titleEditText.setText(draft?.title)
                    contentEditText.setText(draft?.content)
                }
            }
    }

    private fun saveDraft() {
        val userId = auth.currentUser?.uid ?: return
        val title = titleEditText.text.toString().trim()
        val content = contentEditText.text.toString().trim()

        val draft = Draft(title = title, content = content)

        val collectionRef = db.collection("users").document(userId).collection("drafts")

        val task = if (currentDraftId == null) {
            // Create new draft
            collectionRef.add(draft)
        } else {
            // Update existing draft
            collectionRef.document(currentDraftId!!).set(draft)
        }

        task.addOnSuccessListener {
            Toast.makeText(requireContext(), "Draft saved!", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Error saving draft: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteDraft() {
        val userId = auth.currentUser?.uid ?: return
        currentDraftId?.let {
            db.collection("users").document(userId).collection("drafts").document(it)
                .delete()
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Draft deleted!", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
                }.addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Error deleting draft: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
