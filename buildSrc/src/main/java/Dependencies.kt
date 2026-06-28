/*
 * Copyright (C) 2017-2024 Hazuki
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

object Build {
    // CHANGED: compileSdk 30 -> 35 (Play Store requires 34+; 35 is current stable)
    const val compile_sdk_version = 35
    // CHANGED: build_tools_version removed — AGP 8.x manages this automatically
    // const val build_tools_version = "30.0.3"  <-- removed
    const val min_sdk_version = 23
    // CHANGED: targetSdk 30 -> 35
    const val target_sdk_version = 35
}

object AppVersions {
    const val version_name = "6.1.1"
    const val version_code = 410023
}

object Versions {
    const val androidX = "1.1.0"
    // CHANGED: appCompat 1.2.0 -> 1.7.0
    const val appCompat = "1.7.0"
    // CHANGED: activityX 1.2.0 -> 1.9.3
    const val activityX = "1.9.3"
    // CHANGED: androidKTX 1.3.2 -> 1.15.0
    const val androidKTX = "1.15.0"
    // CHANGED: fragmentKtx 1.3.0 -> 1.8.5
    const val fragmentKtx = "1.8.5"
    // CHANGED: recyclerView 1.1.0 -> 1.3.2
    const val recyclerView = "1.3.2"
    // CHANGED: androidxRoom 2.2.6 -> 2.6.1
    const val androidxRoom = "2.6.1"
    // CHANGED: lifeCycle 2.3.0 -> 2.8.7
    const val lifeCycle = "2.8.7"
    // CHANGED: material 1.3.0 -> 1.12.0
    const val material = "1.12.0"
    // CHANGED: support_fix_lib 1.1.1 -> 1.2.1
    const val support_fix_lib = "1.2.1"
    // CHANGED: support_constraint_lib 2.0.4 -> 2.2.0
    const val support_constraint_lib = "2.2.0"
    const val documentFile = "1.0.1"
    const val print = "1.0.0"
    // CHANGED: swipeRefreshLayout 1.1.0 -> 1.1.0 (no update, already latest)
    const val swipeRefreshLayout = "1.1.0"
    // CHANGED: moshi 1.11.0 -> 1.15.1
    const val moshi = "1.15.1"
    // CHANGED: okhttp 4.9.1 -> 4.12.0
    const val okhttp = "4.12.0"
    // CHANGED: okio 2.10.0 -> 3.9.0
    const val okio = "3.9.0"
    // NOTE: kvs_schema is unmaintained and was jcenter-only; consider replacing with
    // DataStore or SharedPreferences-KTX. Left here for now to avoid breaking changes.
    const val kvs_schema = "5.1.0"
    // CHANGED: kotlin 1.4.31 -> 2.0.21
    const val kotlin = "2.0.21"
    // CHANGED: kotlin_coroutines 1.4.2 -> 1.9.0
    const val kotlin_coroutines = "1.9.0"
    const val junit = "4.13.2"
    const val assertk = "0.28.1"
    // CHANGED: espresso 3.3.0 -> 3.6.1
    const val espresso = "3.6.1"
    // CHANGED: testCore 1.3.0 -> 1.6.1
    const val testCore = "1.6.1"
    // CHANGED: runner 1.1.2 -> 1.2.1
    const val runner = "1.2.1"
    const val header_decor = "0.2.8"
    const val materialprogressbar = "1.6.1"
    // CHANGED: mockito 3.8.0 -> 5.14.2
    const val mockito = "5.14.2"
    // NOTE: powermock is incompatible with Mockito 5.x and is effectively abandoned.
    // Removed from Libs below. Migrate affected tests to mockito-inline or MockK instead.
    // const val powermock = "2.0.9"  <-- removed
    // CHANGED: jsoup 1.13.1 -> 1.18.1
    const val jsoup = "1.18.1"
    // CHANGED: re2j 1.5 -> 1.7
    const val re2j = "1.7"
    // CHANGED: webkit 1.4.0 -> 1.12.1
    const val webkit = "1.12.1"
}

object AndroidX {
    const val annotations = "androidx.annotation:annotation:${Versions.androidX}"
    const val appcompat = "androidx.appcompat:appcompat:${Versions.appCompat}"
    const val design = "com.google.android.material:material:${Versions.material}"
    const val recyclerView = "androidx.recyclerview:recyclerview:${Versions.recyclerView}"
    const val constraintLayout = "androidx.constraintlayout:constraintlayout:${Versions.support_constraint_lib}"
    const val fix_preference = "androidx.preference:preference-ktx:${Versions.support_fix_lib}"
    const val KTX = "androidx.core:core-ktx:${Versions.androidKTX}"
    const val fragmentKtx = "androidx.fragment:fragment-ktx:${Versions.fragmentKtx}"
    // CHANGED: typo fixed: activty -> activity
    const val activity = "androidx.activity:activity-ktx:${Versions.activityX}"
    const val room = "androidx.room:room-runtime:${Versions.androidxRoom}"
    const val roomKtx = "androidx.room:room-ktx:${Versions.androidxRoom}"
    // CHANGED: room-compiler is now consumed via ksp, not kapt
    const val roomCompiler = "androidx.room:room-compiler:${Versions.androidxRoom}"
    const val documentFile = "androidx.documentfile:documentfile:${Versions.documentFile}"
    const val swipeRefreshLayout = "androidx.swiperefreshlayout:swiperefreshlayout:${Versions.swipeRefreshLayout}"
    // CHANGED: lifecycle-common-java8 -> lifecycle-runtime-ktx (common-java8 is deprecated)
    const val lifecycle = "androidx.lifecycle:lifecycle-runtime-ktx:${Versions.lifeCycle}"
    const val print = "androidx.print:print:${Versions.print}"
    const val webkit = "androidx.webkit:webkit:${Versions.webkit}"
}

object Libs {
    const val support_annotations = AndroidX.annotations
    const val support_appcompat_v7 = AndroidX.appcompat
    const val support_design = AndroidX.design
    const val support_recyclerview = AndroidX.recyclerView
    const val support_constraint_layout = AndroidX.constraintLayout
    const val support_fix_preference = AndroidX.fix_preference
    const val androidKTX = AndroidX.KTX

    // JSON
    const val moshi = "com.squareup.moshi:moshi:${Versions.moshi}"
    const val moshiCodeGen = "com.squareup.moshi:moshi-kotlin-codegen:${Versions.moshi}"
    const val okio = "com.squareup.okio:okio:${Versions.okio}"

    // Kotlin
    // CHANGED: kotlin-stdlib-jdk7 -> kotlin-stdlib (jdk7/jdk8 variants merged into stdlib since Kotlin 1.8)
    const val kotlin_stdlib = "org.jetbrains.kotlin:kotlin-stdlib:${Versions.kotlin}"
    // CHANGED: kotlin-android-extensions removed (plugin was deprecated in 1.7, removed in 2.0)
    // const val kotlin_android_extensions = ...  <-- removed
    const val kotlin_coroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.kotlin_coroutines}"
    const val kotlin_coroutines_android = "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.kotlin_coroutines}"

    // NOTE: kvs_schema was published on jcenter only and is unmaintained.
    // If still needed, you must either vendor the jar or migrate to DataStore/SharedPreferences-KTX.
    const val kvs_schema = "com.rejasupotaro:kvs-schema:${Versions.kvs_schema}"
    const val kvs_schema_compiler = "com.rejasupotaro:kvs-schema-compiler:${Versions.kvs_schema}"

    // Test
    const val junit = "junit:junit:${Versions.junit}"
    const val espressoCore = "androidx.test.espresso:espresso-core:${Versions.espresso}"
    const val androidTestCore = "androidx.test:core:${Versions.testCore}"
    const val androidRunner = "androidx.test.ext:junit:${Versions.runner}"
    const val assertk = "com.willowtreeapps.assertk:assertk-jvm:${Versions.assertk}"
    const val mockito = "org.mockito:mockito-core:${Versions.mockito}"
    // CHANGED: powermock removed — incompatible with Mockito 5.x and abandoned upstream
    // const val powerMockJunit = ...  <-- removed
    // const val powerMockMockito = ...  <-- removed

    // Other
    const val okhttp = "com.squareup.okhttp3:okhttp:${Versions.okhttp}"
    const val header_decor = "ca.barrenechea.header-decor:header-decor:${Versions.header_decor}"
    const val materialProgressBar = "me.zhanghai.android.materialprogressbar:library:${Versions.materialprogressbar}"
    const val jsoup = "org.jsoup:jsoup:${Versions.jsoup}"
    const val re2j = "com.google.re2j:re2j:${Versions.re2j}"
}

object Dependencies {
    // CHANGED: kotlin_plugin 1.4.31 -> 2.0.21
    const val kotlin_plugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}"
}

object Dagger {
    // CHANGED: hilt 2.33-beta -> 2.51.1 (stable)
    private const val hiltVersion = "2.51.1"

    const val hiltPlugin = "com.google.dagger:hilt-android-gradle-plugin:$hiltVersion"
    const val hilt = "com.google.dagger:hilt-android:$hiltVersion"
    const val hiltCompiler = "com.google.dagger:hilt-compiler:$hiltVersion"
}
