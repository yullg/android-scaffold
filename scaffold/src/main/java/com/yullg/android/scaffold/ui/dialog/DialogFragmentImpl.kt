package com.yullg.android.scaffold.ui.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment

class DialogFragmentImpl(
    private val createDialogCallback: (DialogFragmentImpl) -> Dialog,
    private val dismissDialogCallback: (DialogFragmentImpl, DialogInterface) -> Unit
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog = createDialogCallback(this)

    override fun onDismiss(dialog: DialogInterface) {
        try {
            dismissDialogCallback(this, dialog)
        } finally {
            super.onDismiss(dialog)
        }
    }

}