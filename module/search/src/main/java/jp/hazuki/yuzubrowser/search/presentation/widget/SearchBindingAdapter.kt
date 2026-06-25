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

@file:JvmName("SearchBindingAdapters")

package jp.hazuki.yuzubrowser.search.presentation.widget

import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.databinding.BindingAdapter
import androidx.databinding.BindingMethod
import androidx.databinding.BindingMethods
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import jp.hazuki.yuzubrowser.search.model.SearchSuggestModel
import jp.hazuki.yuzubrowser.search.model.provider.SearchUrl
import jp.hazuki.yuzubrowser.search.presentation.search.SearchSuggestAdapter
import jp.hazuki.yuzubrowser.search.presentation.settings.SearchUrlAdapter
import jp.hazuki.yuzubrowser.search.presentation.settings.SearchUrlDiffCallback

@BindingMethods(BindingMethod(type = SearchButton::class, attribute = "callback", method = "setActionCallback"))
class SearchBindingAdapter

@BindingAdapter("viewmodels")
fun setViewModels(recyclerView: RecyclerView, suggestModels: MutableLiveData<List<SearchSuggestModel>>?) {
    val models = suggestModels?.value ?: return
    val adapter = recyclerView.adapter as SearchSuggestAdapter
    adapter.list.run {
        clear()
        addAll(models)
    }
    adapter.notifyDataSetChanged()
    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
    if (layoutManager.reverseLayout && adapter.itemCount > 0) {
        recyclerView.scrollToPosition(0)
    }
}

@BindingAdapter("searchUrls")
fun setSearchUrl(recyclerView: RecyclerView, searchUrls: MutableLiveData<List<SearchUrl>>?) {
    val urls = searchUrls?.value ?: return
    val adapter = recyclerView.adapter as SearchUrlAdapter
    val diff = DiffUtil.calculateDiff(SearchUrlDiffCallback(adapter.list, urls), true)
    adapter.list.run {
        clear()
        addAll(urls)
    }
    diff.dispatchUpdatesTo(adapter)
}

@BindingAdapter("callback")
fun setSearchCallback(editText: EditText, callback: SearchButton.Callback?) {
    if (callback == null) return
        editText.setOnEditorActionListener { _, actionId, _ ->
            if (EditorInfo.IME_ACTION_GO == actionId) {
                callback.autoSearch()
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
}
