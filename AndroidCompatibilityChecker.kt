package com.yourappname.androidcloner

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log

/**
 * Classe utilitaire pour vérifier la compatibilité de l'appareil avec l'application
 */
class AndroidCompatibilityChecker(private val context: Context) {

    companion object {
        private const val TAG = "CompatibilityChecker"
        private const val MIN_SDK_VERSION = Build.VERSION_CODES.S // Android 12 (API 31)
    }

    /**
     * Vérifie si le dispositif répond aux exigences minimales
     * @return Pair<Boolean, String> (est compatible, message d'erreur ou null)
     */
    fun checkDeviceCompatibility(): Pair<Boolean, String?> {
        // Vérification de la version Android
        if (Build.VERSION.SDK_INT < MIN_SDK_VERSION) {
            val errorMsg = "Version Android non supportée. Version minimum requise: Android 12"
            Log.e(TAG, errorMsg)
            return Pair(false, errorMsg)
        }

        // Vérification des permissions système nécessaires
        if (!hasRequiredPermissions()) {
            val errorMsg = "Permissions nécessaires non accordées"
            Log.e(TAG, errorMsg)
            return Pair(false, errorMsg)
        }

        // Vérification si le device est rooté (peut être utile pour certaines fonctionnalités)
        val isRooted = isDeviceRooted()
        Log.d(TAG, "Appareil rooté: $isRooted")

        // Vérification des fonctionnalités avancées disponibles selon la version d'Android
        val advancedFeaturesInfo = checkAdvancedFeatures()
        Log.d(TAG, "Fonctionnalités avancées: $advancedFeaturesInfo")

        return Pair(true, null)
    }

    /**
     * Vérifie si les permissions nécessaires sont accordées
     */
    private fun hasRequiredPermissions(): Boolean {
        // Liste des permissions à vérifier
        val requiredPermissions = arrayOf(
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            // Ajoutez d'autres permissions nécessaires
        )

        return requiredPermissions.all { permission ->
            context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Vérification des fonctionnalités disponibles selon la version d'Android
     */
    private fun checkAdvancedFeatures(): Map<String, Boolean> {
        val featuresMap = mutableMapOf<String, Boolean>()

        // Vérification de la disponibilité des profils utilisateurs multiples
        featuresMap["multiple_users"] = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

        // Support pour les conteneurs d'applications (fonctionnalité avancée)
        featuresMap["app_containers"] = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

        // Vérification du support des fonctionnalités de clonage sur les versions plus récentes
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> { // Android 13
                featuresMap["advanced_cloning"] = false // Restrictions plus fortes
                featuresMap["work_profile_support"] = true
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S_V2 -> { // Android 12L
                featuresMap["advanced_cloning"] = true
                featuresMap["work_profile_support"] = true
            }
            else -> {
                featuresMap["advanced_cloning"] = true
                featuresMap["work_profile_support"] = true
            }
        }

        return featuresMap
    }

    /**
     * Détecte si l'appareil est rooté (peut donner plus de possibilités)
     */
    private fun isDeviceRooted(): Boolean {
        val buildTags = Build.TAGS
        if (buildTags != null && buildTags.contains("test-keys")) {
            return true
        }

        // Vérification des chemins classiques pour SuperUser
        val paths = arrayOf(
            "/system/app/Superuser.apk",
            "/system/xbin/su",
            "/system/bin/su"
        )

        return paths.any { path ->
            try {
                java.io.File(path).exists()
            } catch (e: Exception) {
                false
            }
        }
    }
}
