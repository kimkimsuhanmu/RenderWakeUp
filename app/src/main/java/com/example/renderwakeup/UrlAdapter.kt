package com.example.renderwakeup

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.renderwakeup.data.model.PingStatus
import com.example.renderwakeup.data.model.UrlEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * URL 목록을 표시하기 위한 RecyclerView 어댑터
 */
class UrlAdapter(
    private val onPingNow: (UrlEntity) -> Unit,
    private val onEdit: (UrlEntity) -> Unit,
    private val onDelete: (UrlEntity) -> Unit
) : ListAdapter<UrlEntity, UrlAdapter.UrlViewHolder>(UrlDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UrlViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_url, parent, false)
        return UrlViewHolder(view)
    }

    override fun onBindViewHolder(holder: UrlViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class UrlViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvUrl: TextView = itemView.findViewById(R.id.tvUrl)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        private val tvInterval: TextView = itemView.findViewById(R.id.tvInterval)
        private val tvLastPing: TextView = itemView.findViewById(R.id.tvLastPing)
        private val btnPingNow: ImageButton = itemView.findViewById(R.id.btnPingNow)
        private val btnEdit: ImageButton = itemView.findViewById(R.id.btnEdit)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)

        private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

        fun bind(url: UrlEntity) {
            tvUrl.text = url.url
            
            // 상태 표시
            val statusText = when (url.status) {
                PingStatus.SUCCESS -> itemView.context.getString(R.string.status_success)
                PingStatus.ERROR -> itemView.context.getString(R.string.status_error)
                PingStatus.PENDING -> itemView.context.getString(R.string.status_pending)
            }
            tvStatus.text = statusText
            
            // 상태에 따른 색상 설정
            val statusColor = when (url.status) {
                PingStatus.SUCCESS -> android.graphics.Color.parseColor("#4CAF50") // 녹색
                PingStatus.ERROR -> android.graphics.Color.parseColor("#F44336") // 빨간색
                PingStatus.PENDING -> android.graphics.Color.parseColor("#9E9E9E") // 회색
            }
            tvStatus.setTextColor(statusColor)
            
            // 핑 간격 표시
            tvInterval.text = itemView.context.getString(R.string.interval_format, url.interval)
            
            // 마지막 핑 시간 표시
            val lastPingText = if (url.lastPingTime != null) {
                itemView.context.getString(
                    R.string.status_last_ping, 
                    dateFormat.format(url.lastPingTime)
                )
            } else {
                itemView.context.getString(R.string.status_never_pinged)
            }
            tvLastPing.text = lastPingText
            
            // 버튼 클릭 리스너 설정
            btnPingNow.setOnClickListener { onPingNow(url) }
            btnEdit.setOnClickListener { onEdit(url) }
            btnDelete.setOnClickListener { onDelete(url) }
        }
    }
}

/**
 * URL 목록의 변경 사항을 효율적으로 처리하기 위한 DiffUtil 콜백
 */
class UrlDiffCallback : DiffUtil.ItemCallback<UrlEntity>() {
    override fun areItemsTheSame(oldItem: UrlEntity, newItem: UrlEntity): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: UrlEntity, newItem: UrlEntity): Boolean {
        return oldItem == newItem
    }
}
