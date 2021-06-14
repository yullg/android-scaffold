package com.yullg.android.scaffold.ui.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

interface DialogShell {

    fun show(activity: FragmentActivity)

    fun dismiss(activity: FragmentActivity)

    fun isShowing(activity: FragmentActivity): Boolean?

}

internal class MaterialDialogShell : DialogFragment(), DialogShell {

    var createDialogCallback: ((MaterialDialogShell) -> Dialog)? = null
    var dismissDialogCallback: ((MaterialDialogShell) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showsDialog = (createDialogCallback != null && dismissDialogCallback != null)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return createDialogCallback?.invoke(this) ?: super.onCreateDialog(savedInstanceState)
    }

    override fun onDismiss(dialog: DialogInterface) {
        try {
            dismissDialogCallback?.invoke(this)
        } finally {
            super.onDismiss(dialog)
        }
    }

    override fun show(activity: FragmentActivity) = show(activity.supportFragmentManager, null)

    override fun dismiss(activity: FragmentActivity) = dismissAllowingStateLoss()

    override fun isShowing(activity: FragmentActivity): Boolean? = dialog?.isShowing

}

internal class BottomSheetDialogShell : BottomSheetDialogFragment(), DialogShell {

    var createDialogCallback: ((BottomSheetDialogShell) -> Dialog)? = null
    var dismissDialogCallback: ((BottomSheetDialogShell) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showsDialog = (createDialogCallback != null && dismissDialogCallback != null)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return createDialogCallback?.invoke(this) ?: super.onCreateDialog(savedInstanceState)
    }

    override fun onDismiss(dialog: DialogInterface) {
        try {
            dismissDialogCallback?.invoke(this)
        } finally {
            super.onDismiss(dialog)
        }
    }

    override fun show(activity: FragmentActivity) = show(activity.supportFragmentManager, null)

    override fun dismiss(activity: FragmentActivity) = dismissAllowingStateLoss()

    override fun isShowing(activity: FragmentActivity): Boolean? = dialog?.isShowing

}