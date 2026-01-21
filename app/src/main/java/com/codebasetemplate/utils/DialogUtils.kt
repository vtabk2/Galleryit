package com.codebasetemplate.utils

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.core.graphics.drawable.toDrawable
import com.codebasetemplate.databinding.DialogDeniedPermissionsBinding
import com.codebasetemplate.databinding.DialogWriteStorageBinding
import com.core.utilities.util.hideNavigationBar

object DialogUtils {
    fun initDeniedPermissionsDialog(context: Context, isHideNavigationBar: Boolean, callback: (granted: Boolean) -> Unit): AlertDialog {
        val builder = AlertDialog.Builder(context)
        val binding = DialogDeniedPermissionsBinding.inflate(LayoutInflater.from(context), null, false)
        builder.setView(binding.root)
        val deniedPermissionsDialog = builder.create()
        deniedPermissionsDialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        binding.tvDeniedPermissionsClose.setOnClickListener {
            deniedPermissionsDialog.dismiss()
            callback(false)
        }
        binding.tvDeniedPermissionsSettings.setOnClickListener {
            deniedPermissionsDialog.dismiss()
            callback(true)
        }

        if (isHideNavigationBar) {
            deniedPermissionsDialog.hideNavigationBar()
        }

        return deniedPermissionsDialog
    }

    fun initWriteStorageDialog(context: Context, isHideNavigationBar: Boolean, callback: (granted: Boolean) -> Unit): AlertDialog {
        val builder = AlertDialog.Builder(context)
        val binding = DialogWriteStorageBinding.inflate(LayoutInflater.from(context), null, false)
        builder.setView(binding.root)
        val writeStorageDialog = builder.create()
        writeStorageDialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        binding.tvDeniedPermissionsClose.setOnClickListener {
            writeStorageDialog.dismiss()
            callback(false)
        }
        binding.tvDeniedPermissionsSettings.setOnClickListener {
            writeStorageDialog.dismiss()
            callback(true)
        }

        if (isHideNavigationBar) {
            writeStorageDialog.hideNavigationBar()
        }

        return writeStorageDialog
    }
}