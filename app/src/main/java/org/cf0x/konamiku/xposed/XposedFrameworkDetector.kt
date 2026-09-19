package org.cf0x.konamiku.xposed

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

object XposedFrameworkDetector {
    private data class Candidate(val packageName: String, val displayName: String)
    private data class Detection(val name: String, val version: String)

    private val candidates = listOf(
        Candidate(packageName = "org.matrix.vector.manager", displayName = "Vector"),
        Candidate(packageName = "org.lsposed.manager", displayName = "LSPosed"),
    )

    private fun packageVersion(context: Context, packageName: String): String? = runCatching {
        val pm = context.packageManager
        val info = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            pm.getPackageInfo(packageName, 0)
        }
        info.versionName?.takeIf { it.isNotBlank() }
    }.getOrNull()

    private fun detectInstalledFramework(context: Context): Detection? {
        return candidates.firstNotNullOfOrNull { candidate ->
            packageVersion(context, candidate.packageName)?.let { version ->
                Detection(name = candidate.displayName, version = version)
            }
        }
    }

    fun fillMissingState(context: Context) {
        if (XposedState.frameworkName.isNotBlank() && XposedState.frameworkVersion.isNotBlank()) return
        val detected = detectInstalledFramework(context) ?: return
        if (XposedState.frameworkName.isBlank()) XposedState.frameworkName = detected.name
        if (XposedState.frameworkVersion.isBlank()) XposedState.frameworkVersion = detected.version
    }
}
