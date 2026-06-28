v6.1.1
Patching

Removed firebase/crashalytics code remnants
Given the application a new appID (monaka.yuzubrowser) to better distinguish it from the original app

v6.1.0 (2026)
Modernization

Migrated to AGP 8.4.2, Kotlin 2.0.21, Gradle 8.9
Updated targetSdk to 35 (Android 15)
Replaced jcenter with mavenCentral
Migrated annotation processing from kapt to KSP
Updated all dependencies to current stable versions


Bug Fixes

"Open in Another" App now works


Removed

Fabric/Crashlytics (service shut down)
Firebase Analytics and Crashlytics
