package com.yullg.android.scaffold.ui.dialog

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.LayoutRes
import androidx.annotation.StyleRes
import androidx.annotation.StyleableRes
import androidx.fragment.app.FragmentManager
import com.yullg.android.scaffold.R
import com.yullg.android.scaffold.app.ScaffoldConfig
import com.yullg.android.scaffold.internal.ScaffoldLogger

data class CustomDialogMetadata(
    val view: View?,
    @LayoutRes val viewLayoutResId: Int?,
    val viewBinder: ((View) -> Unit)?,
    override val cancelable: Boolean,
    override val showDuration: Long,
    override val onShowListener: (() -> Unit)?,
    override val onDismissListener: (() -> Unit)?,
) : MaterialDialogMetadata

class CustomDialog(handler: DialogHandler<CustomDialogMetadata>) :
    BaseDialog<CustomDialogMetadata, CustomDialog>(handler) {

    var view: View? = null

    @LayoutRes
    var viewLayoutResId: Int? = null
    var viewBinder: ((CustomDialog, View) -> Unit)? = null

    constructor(fragmentManager: FragmentManager) :
            this(ScaffoldConfig.UI.defaultCustomDialogHandlerCreator(fragmentManager))

    override fun buildMetadata() = CustomDialogMetadata(
        view = view,
        viewLayoutResId = viewLayoutResId,
        viewBinder = convertViewBinder(viewBinder),
        cancelable = cancelable ?: ScaffoldConfig.UI.defaultCustomDialogCancelable,
        showDuration = showDuration ?: ScaffoldConfig.UI.defaultCustomDialogShowDuration,
        onShowListener = convertOnShowOrDismissListener(this, onShowListener),
        onDismissListener = convertOnShowOrDismissListener(this, onDismissListener),
    )

    override fun resetMetadata() {
        super.resetMetadata()
        view = null
        viewLayoutResId = null
        viewBinder = null
    }

    private fun convertViewBinder(binder: ((CustomDialog, View) -> Unit)?): ((View) -> Unit)? {
        return binder?.let { br ->
            { v -> br(this, v) }
        }
    }

}

class DefaultCustomDialogHandler(fragmentManager: FragmentManager) :
    MaterialDialogHandler<CustomDialogMetadata>(
        fragmentManager,
        R.style.yg_DialogCustomDefaultStyle
    ) {

    override fun createDialogView(
        dialogShell: NormalDialogShell,
        metadata: CustomDialogMetadata
    ): View {
        val view = when {
            metadata.view != null -> metadata.view.apply {
                (parent as? ViewGroup)?.removeView(this)
            }
            metadata.viewLayoutResId != null -> LayoutInflater.from(dialogShell.requireContext())
                .inflate(metadata.viewLayoutResId, FrameLayout(dialogShell.requireContext()))
            else -> throw IllegalArgumentException("Both 'view' and 'viewLayoutResId' of CustomDialog cannot be null")
        }
        metadata.viewBinder?.invoke(view)
        return view
    }

    override fun updateDialogView(dialogShell: NormalDialogShell, metadata: CustomDialogMetadata) {
        ScaffoldLogger.warn("[Dialog] CustomDialog does not support updates")
    }

}