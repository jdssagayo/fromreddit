package com.example.booknest3

import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class EditDraftFragment : Fragment(), SaveDraftDialogFragment.SaveDraftListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toolbar: Toolbar
    private lateinit var editorViewPager: ViewPager2
    private lateinit var wordCountTextView: TextView

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var currentDraftId: String? = null

    private lateinit var editorAdapter: EditorPageAdapter
    private val pageDataList = mutableListOf<Page>()
    private var initialPageIds = mutableSetOf<String>() // To track deletions

    private val autoSaveHandler = Handler(Looper.getMainLooper())
    private var autoSaveRunnable: Runnable? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        currentDraftId = arguments?.getString("DRAFT_ID")
        return inflater.inflate(R.layout.fragment_edit_draft, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        drawerLayout = view.findViewById(R.id.drawer_layout_edit)
        toolbar = view.findViewById(R.id.toolbar_edit)
        editorViewPager = view.findViewById(R.id.editor_view_pager)
        wordCountTextView = view.findViewById(R.id.word_count)

        setupToolbar()
        setupViewPager()

        if (currentDraftId != null) {
            loadDraftAndPages()
        } else {
            createNewDraftAndSetup()
        }
    }

    fun addNewPage() {
        pageDataList.add(Page(documentId = null, content = ""))
        editorAdapter.notifyItemInserted(pageDataList.size - 1)
        editorViewPager.currentItem = pageDataList.size - 1
        scheduleAutoSave(now = true)
    }

    fun deletePage(pageId: String) {
        val pageIndex = pageDataList.indexOfFirst { it.documentId == pageId }
        if (pageIndex != -1) {
            pageDataList.removeAt(pageIndex)
            editorAdapter.notifyItemRemoved(pageIndex)
            scheduleAutoSave(now = true)
        }
    }

    fun scrollToPage(position: Int) {
        if (position >= 0 && position < pageDataList.size) {
            editorViewPager.currentItem = position
        }
    }

    private fun setupToolbar() {
        toolbar.setNavigationOnClickListener { drawerLayout.openDrawer(GravityCompat.START) }
        toolbar.navigationIcon?.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
        toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_save_draft_toolbar -> {
                    scheduleAutoSave(now = true)
                    true
                }
                R.id.action_edit_details -> {
                    showSaveDraftDialog()
                    true
                }
                R.id.action_delete_draft_toolbar -> {
                    // Implement delete logic if needed
                    true
                }
                else -> false
            }
        }
    }

    private fun setupViewPager() {
        editorAdapter = EditorPageAdapter(pageDataList) {
            updateWordCount()
            scheduleAutoSave()
        }
        editorViewPager.adapter = editorAdapter
    }

    private fun scheduleAutoSave(now: Boolean = false) {
        autoSaveRunnable?.let { autoSaveHandler.removeCallbacks(it) }
        autoSaveRunnable = Runnable { saveAllPages() }
        if (now) {
            autoSaveHandler.post(autoSaveRunnable!!)
        } else {
            autoSaveHandler.postDelayed(autoSaveRunnable!!, 2500) // 2.5-second delay
        }
    }

    private fun saveAllPages() {
        val userId = auth.currentUser?.uid ?: return
        val draftId = currentDraftId ?: return
        
        val pagesRef = db.collection("users").document(userId).collection("drafts").document(draftId).collection("pages")
        val batch = db.batch()

        val currentPageIds = pageDataList.mapNotNull { it.documentId }.toSet()
        val idsToDelete = initialPageIds - currentPageIds
        idsToDelete.forEach { pageId ->
            batch.delete(pagesRef.document(pageId))
        }

        pageDataList.forEachIndexed { index, page ->
            val pageNumber = index + 1
            if (page.documentId != null) {
                batch.update(pagesRef.document(page.documentId!!), mapOf("content" to page.content, "pageNumber" to pageNumber))
            } else {
                val newPageRef = pagesRef.document()
                val pageMap = mapOf("content" to page.content, "pageNumber" to pageNumber)
                batch.set(newPageRef, pageMap)
                page.documentId = newPageRef.id
            }
        }

        batch.commit().addOnSuccessListener {
            initialPageIds = pageDataList.mapNotNull { it.documentId }.toMutableSet()
            view?.let { Snackbar.make(it, "Draft saved!", Snackbar.LENGTH_SHORT).show() }
        }.addOnFailureListener { e ->
             view?.let { Snackbar.make(it, "Save failed: ${e.message}", Snackbar.LENGTH_LONG).show() }
        }
    }

    private fun loadDraftAndPages() {
        val userId = auth.currentUser?.uid ?: return
        val draftId = currentDraftId ?: return

        db.collection("users").document(userId).collection("drafts").document(draftId).get()
            .addOnSuccessListener { doc -> toolbar.title = doc.getString("title") ?: "Untitled Book" }

        db.collection("users").document(userId).collection("drafts").document(draftId)
            .collection("pages").orderBy("pageNumber", Query.Direction.ASCENDING).get()
            .addOnSuccessListener { pagesSnapshot ->
                val loadedPages = pagesSnapshot.documents.map { doc ->
                    Page(documentId = doc.id, content = doc.getString("content") ?: "")
                }
                
                initialPageIds.clear()
                initialPageIds.addAll(loadedPages.mapNotNull { it.documentId })

                pageDataList.clear()
                if (loadedPages.isEmpty()) {
                    pageDataList.add(Page(documentId = null, content = ""))
                } else {
                    pageDataList.addAll(loadedPages)
                }

                editorAdapter.notifyDataSetChanged()
                updateWordCount()
                setupDrawer()
            }
    }

     private fun createNewDraftAndSetup() {
         val userId = auth.currentUser?.uid ?: return
        val newDraftData = hashMapOf(
            "title" to "Untitled Book",
            "description" to "",
            "authorId" to userId,
            "createdAt" to FieldValue.serverTimestamp(),
            "lastModified" to FieldValue.serverTimestamp()
        )

        db.collection("users").document(userId).collection("drafts").add(newDraftData)
            .addOnSuccessListener { draftRef ->
                currentDraftId = draftRef.id
                toolbar.title = "Untitled Book"
                
                pageDataList.clear()
                initialPageIds.clear()
                pageDataList.add(Page(documentId = null, content = "")) 
                
                editorAdapter.notifyDataSetChanged()
                updateWordCount()
                setupDrawer()
                scheduleAutoSave(now = true)
            }
    }

    private fun setupDrawer() {
        currentDraftId?.let {
            val contentsFragment = ContentsFragment.newInstance(it)
            childFragmentManager.beginTransaction()
                .replace(R.id.sidebar_container, contentsFragment)
                .commit()
        }
    }

    private fun updateWordCount() {
        val totalWords = pageDataList.sumOf { it.content.trim().split(Regex("\\s+")).filter { w -> w.isNotEmpty() }.size }
        wordCountTextView.text = "$totalWords words"
    }

    override fun onDraftSaved(title: String, description: String) {
        val userId = auth.currentUser?.uid ?: return
        val draftId = currentDraftId ?: return
        val updates = mapOf(
            "title" to title,
            "description" to description,
            "lastModified" to FieldValue.serverTimestamp()
        )
        db.collection("users").document(userId).collection("drafts").document(draftId)
            .update(updates)
            .addOnSuccessListener {
                toolbar.title = title
                view?.let { Snackbar.make(it, "Details saved!", Snackbar.LENGTH_SHORT).show() }
            }
    }

    private fun showSaveDraftDialog() {
         val userId = auth.currentUser?.uid ?: return
        val draftId = currentDraftId ?: return
        db.collection("users").document(userId).collection("drafts").document(draftId).get()
            .addOnSuccessListener { document ->
                val currentTitle = document.getString("title") ?: ""
                val currentDescription = document.getString("description") ?: ""
                val dialog = SaveDraftDialogFragment.newInstance(currentTitle, currentDescription)
                dialog.setSaveDraftListener(this)
                dialog.show(parentFragmentManager, "SaveDraftDialogFragment")
            }
    }
}
