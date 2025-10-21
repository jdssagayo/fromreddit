package com.example.booknest3

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class DraftListFragment : Fragment() {

    private lateinit var draftsRecyclerView: RecyclerView
    private lateinit var draftAdapter: DraftAdapter
    private val draftList = mutableListOf<Draft>()

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
        draftAdapter = DraftAdapter(draftList) { draft ->
            navigateToEditDraft(draft.id)
        }
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

                draftList.clear()
                snapshots?.forEach { doc ->
                    val draft = doc.toObject(Draft::class.java)
                    draftList.add(draft)
                }
                draftAdapter.notifyDataSetChanged()
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
}
