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

package jp.hazuki.yuzubrowser.adblock.repository

import android.content.Context

class AdBlockPref private constructor(context: Context) {

    private val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    var abpNextUpdateTime: Long
        get() = prefs.getLong(KEY_NEXT_UPDATE_TIME, -1L)
        set(value) {
            prefs.edit().putLong(KEY_NEXT_UPDATE_TIME, value).apply()
        }

    var abpLastUpdateTime: Long
        get() = prefs.getLong(KEY_LAST_UPDATE_TIME, -1L)
        set(value) {
            prefs.edit().putLong(KEY_LAST_UPDATE_TIME, value).apply()
        }

    companion object {
        private const val PREF_NAME = "abp"
        private const val KEY_NEXT_UPDATE_TIME = "abpNextUpdateTime"
        private const val KEY_LAST_UPDATE_TIME = "abpLastUpdateTime"

        @Volatile
        private var instance: AdBlockPref? = null

        fun get(context: Context): AdBlockPref {
            return instance ?: synchronized(this) {
                instance ?: AdBlockPref(context.applicationContext).also { instance = it }
            }
        }
    }
}
