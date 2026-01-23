package com.codebasetemplate.features.app.base

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.viewbinding.ViewBinding
import com.codebasetemplate.App
import com.codebasetemplate.core.base_ui.CoreActivity
import com.codebasetemplate.utils.DialogUtils
import com.codebasetemplate.utils.extensions.PERMISSION_WRITE_STORAGE
import com.codebasetemplate.utils.extensions.dialogLayout
import com.codebasetemplate.utils.extensions.hasPermission
import com.codebasetemplate.utils.result.OpenAppSystemSettingsContract
import com.codebasetemplate.utils.result.RequestPermissionContract

abstract class PermissionActivity<VB : ViewBinding> : CoreActivity<VB>() {
    private var deniedPermissionsDialog: AlertDialog? = null
    private var writeStoragePermissionsDialog: AlertDialog? = null

    private var openAppSystemSettingsContract = registerForActivityResult(OpenAppSystemSettingsContract()) { permissionId ->
        if (hasPermission(permissionId, hasFull = false)) {
            when (permissionId) {
                PERMISSION_WRITE_STORAGE -> goToOtherHasWriteStoragePermission()
                else -> goToOtherHasPermission()
            }
        }
    }

    // Register ActivityResult handler
    private val requestWriteStoragePermissions = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
        // Handle permission requests results
        // See the permission example in the Android platform samples: https://github.com/android/platform-samples
        if (!results.isNullOrEmpty()) {
            val readMediaImages = results[Manifest.permission.READ_MEDIA_IMAGES] ?: false

            val readMediaVideo = results[Manifest.permission.READ_MEDIA_VIDEO] ?: false

            val readMediaVisualUserSelected = results[Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED] ?: false

            val readExternalStorage = results[Manifest.permission.WRITE_EXTERNAL_STORAGE] ?: false

            if ((readMediaImages && readMediaVideo) || readMediaVisualUserSelected || readExternalStorage) {
                goToOtherHasWriteStoragePermission()
            } else {
                showDeniedPermissionsDialog(PERMISSION_WRITE_STORAGE)
            }
        }
    }

    private val requestPermissions = registerForActivityResult(RequestPermissionContract()) { data ->
        if (data.second) {
            goToOtherHasPermission()
        } else {
            showDeniedPermissionsDialog(data.first)
        }
    }

    override fun initViews(savedInstanceState: Bundle?) {
        super.initViews(savedInstanceState)

        writeStoragePermissionsDialog = DialogUtils.initWriteStorageDialog(this, isHideNavigationBar = isHideNavigationBar) { granted ->
            if (granted) {
                openAppSystemSettingsContract.launch(PERMISSION_WRITE_STORAGE)
            } else {
                requestWriteStoragePermissions.launch(
                    arrayOf(
                        Manifest.permission.READ_MEDIA_IMAGES,
                        Manifest.permission.READ_MEDIA_VIDEO,
                        Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
                    )
                )
            }
        }
    }

    fun checkPermission(permissionId: Int = PERMISSION_WRITE_STORAGE, hasFull: Boolean = true, callback: (granted: Boolean) -> Unit) {
        if (hasPermission(permissionId, hasFull = hasFull)) {
            callback.invoke(true)
        } else {
            App.instance.reOpenShowCondition.isCanShow = { false }
            //
            when (permissionId) {
                PERMISSION_WRITE_STORAGE -> {
                    // Permission request logic
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        if (hasImageAndVideoPermission()) {
                            callback.invoke(true)
                        } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED) == PackageManager.PERMISSION_GRANTED) {
                            writeStoragePermissionsDialog?.show()
                            dialogLayout(writeStoragePermissionsDialog)
                        } else {
                            requestWriteStoragePermissions.launch(
                                arrayOf(
                                    Manifest.permission.READ_MEDIA_IMAGES,
                                    Manifest.permission.READ_MEDIA_VIDEO,
                                    Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
                                )
                            )
                        }
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        requestWriteStoragePermissions.launch(
                            arrayOf(
                                Manifest.permission.READ_MEDIA_IMAGES,
                                Manifest.permission.READ_MEDIA_VIDEO
                            )
                        )
                    } else {
                        requestWriteStoragePermissions.launch(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE))
                    }
                }

                else -> {
                    requestPermissions.launch(permissionId)
                }
            }
            //
            callback.invoke(false)
        }
    }

    private fun hasImageAndVideoPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED
    }

    private fun showDeniedPermissionsDialog(permissionId: Int) {
        deniedPermissionsDialog?.dismiss()
        deniedPermissionsDialog = DialogUtils.initDeniedPermissionsDialog(this, isHideNavigationBar = isHideNavigationBar) { granted ->
            if (granted) {
                openAppSystemSettingsContract.launch(permissionId)
            }
        }
        deniedPermissionsDialog?.show()
        dialogLayout(deniedPermissionsDialog)
    }

    open fun goToOtherHasWriteStoragePermission() {}

    open fun goToOtherHasPermission() {}
}