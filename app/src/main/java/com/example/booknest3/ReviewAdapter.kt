package com.example.booknest3

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.Locale

class ReviewAdapter(private val reviews: List<Review>) : RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

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

        holder.userName.text = review.userName
        holder.reviewRating.rating = review.rating
        holder.reviewText.text = review.reviewText

        // Format the date
        review.timestamp?.let {
            val sdf = SimpleDateFormat("MM/dd/yy", Locale.getDefault())
            holder.reviewDate.text = sdf.format(it)
        }

        // Load user avatar
        if (!review.userAvatarUrl.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(review.userAvatarUrl)
                .into(holder.userAvatar)
        } else {
            holder.userAvatar.setImageResource(R.drawable.ic_profile) // Default avatar
        }

        // Handle more options click if needed in the future
        holder.moreOptions.setOnClickListener {
            // Implement more options logic here
        }
    }

    override fun getItemCount() = reviews.size
}
