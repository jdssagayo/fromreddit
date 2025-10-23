package com.example.booknest3

import android.content.Context
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import de.hdodenhof.circleimageview.CircleImageView
import java.lang.IllegalArgumentException
import java.text.SimpleDateFormat
import java.util.Locale

class ReviewAdapter(
    private val reviews: List<Review>,
    private val onOptionSelected: (Review, String) -> Unit
) : RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    inner class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userAvatar: CircleImageView = itemView.findViewById(R.id.user_avatar)
        val userName: TextView = itemView.findViewById(R.id.user_name)
        val reviewRating: RatingBar = itemView.findViewById(R.id.review_rating)
        val reviewDate: TextView = itemView.findViewById(R.id.review_date)
        val reviewText: TextView = itemView.findViewById(R.id.review_text)
        val moreOptions: ImageView = itemView.findViewById(R.id.more_options)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_review, parent, false)
        return ReviewViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val review = reviews[position]
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        holder.userName.text = review.username
        holder.reviewRating.rating = review.rating
        holder.reviewText.text = review.comment

        // Format the date
        review.timestamp?.let {
            val sdf = SimpleDateFormat("MM/dd/yy", Locale.getDefault())
            holder.reviewDate.text = sdf.format(it)
        }

        // Load user avatar
        loadAvatar(holder.itemView.context, review.userProfileUrl, holder.userAvatar)

        // Handle "more options" button visibility and click
        if (currentUserId != null && review.userId == currentUserId) {
            holder.moreOptions.visibility = View.VISIBLE
            holder.moreOptions.setOnClickListener { view ->
                showPopupMenu(view, review)
            }
        } else {
            holder.moreOptions.visibility = View.GONE
        }
    }

    private fun showPopupMenu(view: View, review: Review) {
        val popup = PopupMenu(view.context, view)
        popup.inflate(R.menu.review_options_menu)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_edit_review -> {
                    onOptionSelected(review, "edit")
                    true
                }
                R.id.menu_delete_review -> {
                    onOptionSelected(review, "delete")
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun loadAvatar(context: Context, imageUrl: String?, imageView: ImageView) {
        if (!imageUrl.isNullOrEmpty()) {
            try {
                if (imageUrl.startsWith("http")) {
                    Glide.with(context).load(imageUrl).into(imageView)
                } else {
                    val imageBytes = Base64.decode(imageUrl, Base64.DEFAULT)
                    Glide.with(context).load(imageBytes).into(imageView)
                }
            } catch (e: IllegalArgumentException) {
                imageView.setImageResource(R.drawable.ic_profile)
            }
        } else {
            imageView.setImageResource(R.drawable.ic_profile)
        }
    }

    override fun getItemCount() = reviews.size
}
