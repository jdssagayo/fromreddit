package com.example.booknest3

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ContentsFragment : Fragment() {

    private lateinit var pagesRecyclerView: RecyclerView
    private lateinit var pageAdapter: PageAdapter
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var draftId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        draftId = arguments?.getString(ARG_DRAFT_ID)
        return inflater.inflate(R.layout.fragment_contents, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firestore = Firebase.firestore
        auth = FirebaseAuth.getInstance()

        pagesRecyclerView = view.findViewById(R.id.pages_recycler_view)
        val totalWordsText: TextView = view.findViewById(R.id.total_words_text)
        val totalPagesText: TextView = view.findViewById(R.id.total_pages_text)
        val newPageButton: Button = view.findViewById(R.id.new_page_button)
        val publishButton: Button = view.findViewById(R.id.publish_button)
        val closeButton: ImageButton = view.findViewById(R.id.close_contents_button)

        setupRecyclerView()
        loadPageStats(totalWordsText, totalPagesText)

        newPageButton.setOnClickListener { 
            (parentFragment as? EditDraftFragment)?.addNewPage()
        }

        publishButton.setOnClickListener {
            showPublishDialog()
        }

        closeButton.setOnClickListener {
            (activity?.findViewById(R.id.drawer_layout_edit) as? DrawerLayout)?.closeDrawer(GravityCompat.START)
        }
    }

    private fun setupRecyclerView() {
        pageAdapter = PageAdapter(emptyList(), 
            onPageClick = { position ->
                (parentFragment as? EditDraftFragment)?.scrollToPage(position)
                (activity?.findViewById(R.id.drawer_layout_edit) as? DrawerLayout)?.closeDrawer(GravityCompat.START)
            },
            onDeleteListener = { pageId ->
                (parentFragment as? EditDraftFragment)?.deletePage(pageId)
            }
        )
        pagesRecyclerView.adapter = pageAdapter
        pagesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun loadPageStats(totalWordsText: TextView, totalPagesText: TextView) {
        val userId = auth.currentUser?.uid
        if (userId == null || draftId == null) return

        firestore.collection("users").document(userId).collection("drafts").document(draftId!!)
            .collection("pages").orderBy("pageNumber", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e)
                    return@addSnapshotListener
                }

                val pages = snapshots?.documents ?: emptyList()
                pageAdapter.updatePages(pages)

                val totalPages = pages.size
                val totalWords = pages.sumOf { page ->
                    val content = page.getString("content") ?: ""
                    content.split(Regex("\\s+")).filter { it.isNotEmpty() }.size
                }
                totalPagesText.text = totalPages.toString()
                totalWordsText.text = "$totalWords words"
            }
    }

    private fun showPublishDialog() {
        val userId = auth.currentUser?.uid
        if (userId == null || draftId == null) return

        firestore.collection("users").document(userId).collection("drafts").document(draftId!!)
            .get()
            .addOnSuccessListener { document ->
                val title = document.getString("title") ?: "Untitled Book"
                val dialog = PublishBookDialogFragment.newInstance(draftId!!, title)
                dialog.show(parentFragmentManager, "PublishBookDialogFragment")
            }
    }

    companion object {
        private const val TAG = "ContentsFragment"
        private const val ARG_DRAFT_ID = "draft_id"

        fun newInstance(draftId: String): ContentsFragment {
            val fragment = ContentsFragment()
            val args = Bundle()
            args.putString(ARG_DRAFT_ID, draftId)
            fragment.arguments = args
            return fragment
        }
    }
}
