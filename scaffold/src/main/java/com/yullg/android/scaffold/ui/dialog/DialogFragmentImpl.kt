package com.yullg.android.scaffold.ui.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment

class DialogFragmentImpl : DialogFragment() {

    internal var createDialogCallback: ((DialogFragmentImpl) -> Dialog)? = null
    internal var dismissDialogCallback: ((DialogFragmentImpl) -> Unit)? = null

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

}