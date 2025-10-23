package com.example.booknest3

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class DraftListFragment : Fragment() {

    private lateinit var draftsRecyclerView: RecyclerView
    private lateinit var draftAdapter: DraftAdapter

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_draft_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        draftsRecyclerView = view.findViewById(R.id.drafts_recycler_view)
        setupRecyclerView()

        val newDraftButton = view.findViewById<Button>(R.id.new_draft_button)
        newDraftButton.setOnClickListener {
            navigateToEditDraft(null) // Pass null for a new draft
        }

        fetchDrafts()
    }

    private fun setupRecyclerView() {
        draftAdapter = DraftAdapter(emptyList(),
            onDraftClickListener = { draft ->
                navigateToEditDraft(draft.id)
            },
            onDeleteClickListener = { draft ->
                confirmDeleteDraft(draft)
            }
        )
        draftsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = draftAdapter
        }
    }

    private fun fetchDrafts() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("users").document(userId).collection("drafts")
            .orderBy("lastModified", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("DraftListFragment", "Listen failed.", e)
                    return@addSnapshotListener
                }

                val newDrafts = snapshots?.map { doc -> doc.toObject(Draft::class.java).copy(id = doc.id) } ?: emptyList()
                draftAdapter.updateDrafts(newDrafts)
            }
    }

    private fun navigateToEditDraft(draftId: String?) {
        val fragment = EditDraftFragment().apply {
            arguments = Bundle().apply {
                putString("DRAFT_ID", draftId)
            }
        }
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun confirmDeleteDraft(draft: Draft) {
        val draftTitle = draft.title?.ifEmpty { "New Draft" } ?: "New Draft"
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Draft")
            .setMessage("Are you sure you want to permanently delete '$draftTitle'? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                deleteDraft(draft)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteDraft(draft: Draft) {
        val userId = auth.currentUser?.uid ?: return
        val draftRef = db.collection("users").document(userId).collection("drafts").document(draft.id)
        val pagesRef = draftRef.collection("pages")

        // 1. First, get all the pages in the subcollection.
        pagesRef.get()
            .addOnSuccessListener { pagesSnapshot ->
                // 2. Once we have the pages, start a batch write.
                val batch = db.batch()

                // 3. Add delete operations for each page to the batch.
                for (pageDoc in pagesSnapshot) {
                    batch.delete(pageDoc.reference)
                }

                // 4. Add the delete operation for the main draft document.
                batch.delete(draftRef)

                // 5. Commit the batch.
                batch.commit()
                    .addOnSuccessListener {
                        Log.d("DraftListFragment", "Draft and all its pages deleted successfully.")
                        view?.let { Snackbar.make(it, "'${draft.title}' deleted", Snackbar.LENGTH_SHORT).show() }
                    }
                    .addOnFailureListener { e ->
                        Log.e("DraftListFragment", "Error deleting draft.", e)
                        view?.let { Snackbar.make(it, "Error deleting draft.", Snackbar.LENGTH_SHORT).show() }
                    }
            }
            .addOnFailureListener { e ->
                Log.e("DraftListFragment", "Error fetching pages for deletion.", e)
                view?.let { Snackbar.make(it, "Error starting deletion.", Snackbar.LENGTH_SHORT).show() }
            }
    }
}
