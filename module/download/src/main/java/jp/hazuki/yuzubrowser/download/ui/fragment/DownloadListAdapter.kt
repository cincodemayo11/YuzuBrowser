/*
 * Copyright (C) 2017-2019 Hazuki
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jp.hazuki.yuzubrowser.download.ui.fragment

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.text.format.DateFormat
import android.text.format.Formatter
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import androidx.core.util.forEach
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import jp.hazuki.yuzubrowser.core.utility.extensions.binarySearchLong
import jp.hazuki.yuzubrowser.core.utility.extensions.getResColor
import jp.hazuki.yuzubrowser.download.R
import jp.hazuki.yuzubrowser.download.core.data.DownloadFileInfo
import jp.hazuki.yuzubrowser.download.core.utils.getNotificationString
import jp.hazuki.yuzubrowser.download.databinding.FragmentDownloadListItemBinding
import jp.hazuki.yuzubrowser.download.repository.DownloadsDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

// CHANGED: removed StickyHeaderAdapter/StickyHeaderDecoration (header-decor library was built
// against android.support.v7 and is incompatible with AndroidX). Replaced with a two-viewtype
// adapter pattern: VIEW_TYPE_HEADER and VIEW_TYPE_ITEM. Headers are inserted inline into the
// displayed list when the date group changes. The decoration field and clearHeaderCache() calls
// in reload()/loadMore() are no longer needed and have been removed.

class DownloadListAdapter(
    private val context: Context,
        private val lifecycleOwner: LifecycleOwner,
            private val dao: DownloadsDao,
                private val listener: OnRecyclerMenuListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // Raw download items (no headers)
    private val items = mutableListOf<DownloadFileInfo>()

    // Displayed list: either a Long (header timestamp) or a DownloadFileInfo (item)
    private val displayList = mutableListOf<Any>()

    private val inflater = LayoutInflater.from(context)
    private val calendar = Calendar.getInstance()
    private val dateFormat = DateFormat.getLongDateFormat(context)
    private val timeFormatter = TimeFormatter()

    var selectedItemCount: Int = 0
    private set

    init {
        GlobalScope.launch(Dispatchers.Main) {
            items.addAll(dao.getList(0, 100))
            rebuildDisplayList()
            notifyDataSetChanged()
        }
    }

    private val itemSelected = SparseBooleanArray()
    private val foregroundOverlay = ColorDrawable(context.getResColor(R.color.selected_overlay))

    var isMultiSelectMode = false
    set(value) {
        if (value != field) {
            field = value
            if (!value) {
                itemSelected.clear()
                selectedItemCount = 0
            }
            notifyDataSetChanged()
        }
    }

    // Rebuild displayList by inserting a header Long whenever the date group changes
    private fun rebuildDisplayList() {
        displayList.clear()
        var lastHeaderId = -1L
        for (item in items) {
            val headerId = getDateId(item.startTime)
            if (headerId != lastHeaderId) {
                displayList.add(headerId)
                lastHeaderId = headerId
            }
            displayList.add(item)
        }
    }

    private fun getDateId(timeMillis: Long): Long {
        calendar.timeInMillis = timeMillis
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    override fun getItemViewType(position: Int): Int {
        return if (displayList[position] is Long) VIEW_TYPE_HEADER else VIEW_TYPE_ITEM
    }

    override fun getItemCount() = displayList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_HEADER) {
            val view = inflater.inflate(R.layout.recycler_view_header, parent, false)
            HeaderHolder(view)
        } else {
            val binding = FragmentDownloadListItemBinding.inflate(inflater, parent, false)
            InfoHolder(lifecycleOwner, binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.size > 0 && holder is InfoHolder) {
            val itemPosition = getItemPosition(position)
            if (itemPosition >= 0) update(holder, itemPosition, payloads)
        } else {
            onBindViewHolder(holder, position)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderHolder -> {
                val timestamp = displayList[position] as Long
                holder.header.text = dateFormat.format(Date(timestamp))
            }
            is InfoHolder -> {
                val itemPosition = getItemPosition(position)
                if (itemPosition < 0) return
                    val item = items[itemPosition]

                    holder.binding.formatter = timeFormatter
                    holder.binding.info = item
                    holder.binding.foreground.background =
                    if (isMultiSelectMode && isSelected(itemPosition)) foregroundOverlay else null

                        holder.binding.urlTextView.text = if (item.url.startsWith("data:")) {
                            var end = item.url.indexOf(';')
                            if (end < 0) end = item.url.indexOf(',')
                                item.url.substring(5, end)
                        } else {
                            Uri.parse(item.url).host
                        }

                        updateState(holder, item)

                        holder.itemView.setOnClickListener {
                            val pos = getItemPosition(holder.adapterPosition)
                            if (isMultiSelectMode) toggle(pos)
                                else listener.onRecyclerItemClicked(it, pos)
                        }
                        holder.binding.overflowButton.setOnClickListener {
                            val pos = getItemPosition(holder.adapterPosition)
                            if (isMultiSelectMode) {
                                toggle(pos)
                            } else {
                                val popupMenu = PopupMenu(context, it)
                                listener.onCreateContextMenu(popupMenu.menu, pos)
                                popupMenu.show()
                            }
                        }
                        holder.itemView.setOnLongClickListener {
                            listener.onRecyclerItemLongClicked(it, getItemPosition(holder.adapterPosition))
                            true
                        }
            }
        }
    }

    // Convert a displayList position to an items[] index (skipping headers)
    private fun getItemPosition(displayPosition: Int): Int {
        if (displayPosition < 0 || displayPosition >= displayList.size) return -1
            val entry = displayList[displayPosition]
            if (entry is Long) return -1
                return items.indexOf(entry as DownloadFileInfo)
    }

    private fun update(holder: InfoHolder, position: Int, payloads: MutableList<Any>) {
        when (payloads[0] as? String) {
            PAYLOAD_UPDATE_STATE -> updateState(holder, items[position])
        }
    }

    private fun updateState(holder: InfoHolder, info: DownloadFileInfo) {
        holder.binding.apply {
            when (info.state) {
                DownloadFileInfo.STATE_DOWNLOADED -> {
                    statusTextView.setText(R.string.download_success)
                    if (info.size < 0) {
                        sizeTextView.setText(R.string.unknown)
                    } else {
                        sizeTextView.text = Formatter.formatFileSize(context, info.size)
                    }
                    sizeTextView.visibility = View.VISIBLE
                    splitTextView.visibility = View.VISIBLE
                    progressBar.visibility = View.GONE
                }
                DownloadFileInfo.STATE_CANCELED -> {
                    statusTextView.setText(R.string.download_cancel)
                    sizeTextView.visibility = View.GONE
                    splitTextView.visibility = View.GONE
                    progressBar.visibility = View.GONE
                }
                DownloadFileInfo.STATE_DOWNLOADING -> {
                    statusTextView.text = info.getNotificationString(context)
                    sizeTextView.visibility = View.GONE
                    splitTextView.visibility = View.GONE
                    progressBar.run {
                        visibility = View.VISIBLE
                        progress = info.currentSize.toInt()
                        max = info.size.toInt()
                        isIndeterminate = info.size <= 0
                    }
                }
                DownloadFileInfo.STATE_PAUSED,
                DownloadFileInfo.STATE_UNKNOWN_ERROR or DownloadFileInfo.STATE_PAUSED -> {
                    statusTextView.setText(R.string.download_paused)
                    sizeTextView.visibility = View.GONE
                    splitTextView.visibility = View.GONE
                    progressBar.visibility = View.GONE
                }
                else -> {
                    statusTextView.setText(R.string.download_fail)
                    sizeTextView.visibility = View.GONE
                    splitTextView.visibility = View.GONE
                    progressBar.visibility = View.GONE
                }
            }
        }
    }

    fun update(info: DownloadFileInfo) {
        val index = indexOf(info)
        if (index >= 0) {
            items[index] = info
            rebuildDisplayList()
            notifyItemChanged(index, PAYLOAD_UPDATE_STATE)
        }
    }

    operator fun get(position: Int): DownloadFileInfo = items[position]

    fun remove(position: Int) {
        if (position >= 0) {
            items.removeAt(position)
            rebuildDisplayList()
            notifyDataSetChanged()
        }
    }

    fun remove(info: DownloadFileInfo) = remove(indexOf(info))

    fun indexOf(info: DownloadFileInfo): Int {
        return items.binarySearchLong { info.id - it.id }
    }

    fun reload() {
        GlobalScope.launch(Dispatchers.Main) {
            items.clear()
            rebuildDisplayList()
            notifyDataSetChanged()
            items.addAll(dao.getList(items.size, 100))
            rebuildDisplayList()
            notifyDataSetChanged()
        }
    }

    fun loadMore() {
        GlobalScope.launch(Dispatchers.Main) {
            items.addAll(dao.getList(items.size, 100))
            rebuildDisplayList()
            notifyDataSetChanged()
        }
    }

    fun toggle(position: Int) {
        setSelect(position, !itemSelected.get(position, false))
    }

    fun setSelect(position: Int, isSelect: Boolean) {
        val old = itemSelected.get(position, false)
        itemSelected.put(position, isSelect)
        if (old != isSelect) {
            rebuildDisplayList()
            notifyDataSetChanged()
            if (isSelect) selectedItemCount++ else selectedItemCount--
                if (selectedItemCount == 0) listener.onCancelMultiSelectMode()
                    else listener.onSelectionStateChange(selectedItemCount)
        }
    }

    private fun isSelected(position: Int): Boolean = itemSelected.get(position, false)

        fun getSelectedItems(): List<DownloadFileInfo> {
            val selected = ArrayList<DownloadFileInfo>()
            itemSelected.forEach { key, value ->
                if (value) selected.add(items[key])
            }
            return selected
        }

        class InfoHolder(
            val lifecycleOwner: LifecycleOwner,
            val binding: FragmentDownloadListItemBinding,
        ) : RecyclerView.ViewHolder(binding.root) {
            init {
                binding.lifecycleOwner = lifecycleOwner
            }
        }

        class HeaderHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var header: TextView = itemView as TextView
        }

        companion object {
            const val PAYLOAD_UPDATE_STATE = "update_state"
            private const val VIEW_TYPE_HEADER = 0
            private const val VIEW_TYPE_ITEM = 1
        }
}
