#!/usr/bin/env bash
# modernize_gradle.sh
# Run from the root of the YuzuBrowser repo:
#   chmod +x modernize_gradle.sh && ./modernize_gradle.sh
#
# What this does:
#   - Replaces deprecated AGP 8.x syntax across all submodule build.gradle files
#   - Does NOT touch: app/build.gradle, build.gradle, gradle.properties,
#     gradle/wrapper/gradle-wrapper.properties, settings.gradle
#     (those are already done manually)
#   - Does NOT add namespace declarations (requires reading each AndroidManifest.xml)
#     A report of modules needing namespace is printed at the end.

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CHANGED=()
SKIPPED=()

# All submodule build.gradle files — excludes root and app
SUBMODULE_GRADLE_FILES=$(find "$ROOT_DIR" -name "build.gradle" \
    ! -path "$ROOT_DIR/build.gradle" \
    ! -path "$ROOT_DIR/app/build.gradle" \
    ! -path "$ROOT_DIR/buildSrc/*")

echo "============================================"
echo " YuzuBrowser Gradle Modernization Script"
echo "============================================"
echo ""

for FILE in $SUBMODULE_GRADLE_FILES; do
    RELATIVE="${FILE#$ROOT_DIR/}"
    echo "Processing: $RELATIVE"
    cp "$FILE" "${FILE}.bak"

    # ----------------------------------------------------------------
    # 1. apply plugin -> plugins {} block
    #    Only for the common library module pattern.
    #    Handles the three most common combinations.
    # ----------------------------------------------------------------

    # com.android.library + kotlin-android + kotlin-kapt + hilt
    sed -i \
        "/apply plugin: 'com.android.library'/{
            N;N;N
            s/apply plugin: 'com.android.library'\napply plugin: 'kotlin-android'\napply plugin: 'kotlin-kapt'\napply plugin: 'dagger.hilt.android.plugin'/plugins {\n    id 'com.android.library'\n    id 'kotlin-android'\n    id 'com.google.devtools.ksp'\n    id 'dagger.hilt.android.plugin'\n}/
        }" "$FILE"

    # com.android.library + kotlin-android + kotlin-kapt (no hilt)
    sed -i \
        "/apply plugin: 'com.android.library'/{
            N;N
            s/apply plugin: 'com.android.library'\napply plugin: 'kotlin-android'\napply plugin: 'kotlin-kapt'/plugins {\n    id 'com.android.library'\n    id 'kotlin-android'\n    id 'com.google.devtools.ksp'\n}/
        }" "$FILE"

    # com.android.library + kotlin-android (no kapt)
    sed -i \
        "/apply plugin: 'com.android.library'/{
            N
            s/apply plugin: 'com.android.library'\napply plugin: 'kotlin-android'/plugins {\n    id 'com.android.library'\n    id 'kotlin-android'\n}/
        }" "$FILE"

    # ----------------------------------------------------------------
    # 2. compileSdkVersion / minSdkVersion / targetSdkVersion
    # ----------------------------------------------------------------
    sed -i \
        -e 's/compileSdkVersion /compileSdk /g' \
        -e 's/minSdkVersion /minSdk /g' \
        -e 's/targetSdkVersion /targetSdk /g' \
        "$FILE"

    # ----------------------------------------------------------------
    # 3. kapt -> ksp
    # ----------------------------------------------------------------
    sed -i \
        -e 's/\bkapt\b\(.*Libs\.\)/ksp\1/g' \
        -e 's/\bkapt\b\(.*Dagger\.\)/ksp\1/g' \
        -e 's/\bkaptTest\b/kspTest/g' \
        -e 's/\bkaptAndroidTest\b/kspAndroidTest/g' \
        "$FILE"

    # ----------------------------------------------------------------
    # 4. Java 8 -> Java 17 in compileOptions/kotlinOptions
    # ----------------------------------------------------------------
    sed -i \
        -e 's/VERSION_1_8/VERSION_17/g' \
        -e 's/jvmTarget = "1\.8"/jvmTarget = "17"/g' \
        "$FILE"

    # ----------------------------------------------------------------
    # 5. Remove versionCode/versionName from library modules
    # ----------------------------------------------------------------
    sed -i \
        -e '/versionCode 1$/d' \
        -e '/versionName "1\.0"$/d' \
        "$FILE"

    # ----------------------------------------------------------------
    # 6. proguard-android.txt -> proguard-android-optimize.txt
    #    (only where it's not already the optimize variant)
    # ----------------------------------------------------------------
    sed -i \
        "s/getDefaultProguardFile('proguard-android\.txt')/getDefaultProguardFile('proguard-android-optimize.txt')/g" \
        "$FILE"

    # ----------------------------------------------------------------
    # 7. Remove jcenter()
    # ----------------------------------------------------------------
    sed -i '/^\s*jcenter()\s*$/d' "$FILE"

    # ----------------------------------------------------------------
    # 8. Remove repositories {} blocks that only contained jcenter/mavenCentral
    #    (now centralized in settings.gradle)
    #    This removes empty repositories {} blocks left after jcenter removal.
    # ----------------------------------------------------------------
    # Remove standalone maven { url 'https://jitpack.io' } blocks
    # (jitpack is now in settings.gradle)
    sed -i "/maven { url 'https:\/\/jitpack\.io' }/d" "$FILE"

    # Remove now-empty repositories {} blocks (simple case: block with nothing left)
    perl -i -0pe "s/\nrepositories \{\s*\n\s*\}//g" "$FILE"

    # ----------------------------------------------------------------
    # 9. Add missing compileOptions + kotlinOptions if absent
    # ----------------------------------------------------------------
    if ! grep -q "compileOptions" "$FILE"; then
        # Insert after the closing brace of defaultConfig block
        perl -i -0pe "s/(testInstrumentationRunner.*\n\s*\})/\$1\n\n    compileOptions {\n        sourceCompatibility JavaVersion.VERSION_17\n        targetCompatibility JavaVersion.VERSION_17\n    }\n\n    kotlinOptions {\n        jvmTarget = \"17\"\n    }/g" "$FILE"
        echo "  → Added missing compileOptions/kotlinOptions"
    fi

    CHANGED+=("$RELATIVE")
    echo "  ✓ Done"
    echo ""
done

# ----------------------------------------------------------------
# Namespace report
# ----------------------------------------------------------------
echo "============================================"
echo " Namespace check (manual action required)"
echo "============================================"
echo ""
echo "AGP 8.x requires a 'namespace' in every library module's build.gradle."
echo "The value should match the 'package' attribute in each AndroidManifest.xml."
echo ""
echo "Modules that need namespace added:"
echo ""

for FILE in $SUBMODULE_GRADLE_FILES; do
    RELATIVE="${FILE#$ROOT_DIR/}"
    MODULE_DIR="$(dirname "$FILE")"

    if ! grep -q "namespace" "$FILE"; then
        # Try to read package from AndroidManifest.xml
        MANIFEST="$MODULE_DIR/src/main/AndroidManifest.xml"
        if [ -f "$MANIFEST" ]; then
            PACKAGE=$(grep -o 'package="[^"]*"' "$MANIFEST" | head -1 | sed 's/package="//;s/"//')
            if [ -n "$PACKAGE" ]; then
                echo "  $RELATIVE"
                echo "    → Add inside android {}: namespace \"$PACKAGE\""
            else
                echo "  $RELATIVE"
                echo "    → Could not detect package; check $MANIFEST manually"
            fi
        else
            echo "  $RELATIVE"
            echo "    → No AndroidManifest.xml found at expected path"
        fi
        echo ""
    fi
done

echo "============================================"
echo " Summary"
echo "============================================"
echo "  Files processed: ${#CHANGED[@]}"
echo ""
echo "  Backups saved as <filename>.bak alongside each file."
echo "  To remove backups after verifying: find . -name '*.bak' -delete"
echo ""
echo "  Next steps:"
echo "  1. Add namespace declarations listed above"
echo "  2. Sync project in Android Studio"
echo "  3. Fix any remaining compile errors (likely in source files using"
echo "     deprecated WebView APIs or kotlin-android-extensions synthetics)"
echo ""
