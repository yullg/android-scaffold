package com.yullg.android.scaffold.ui.dialog

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.LayoutRes
import androidx.fragment.app.FragmentManager
import com.yullg.android.scaffold.R
import com.yullg.android.scaffold.app.ScaffoldConfig
import com.yullg.android.scaffold.internal.ScaffoldLogger

data class CustomBottomSheetDialogMetadata(
    val view: View?,
    @LayoutRes val viewLayoutResId: Int?,
    val viewBinder: ((View) -> Unit)?,
    override val cancelable: Boolean,
    override val showDuration: Long,
    override val onShowListener: (() -> Unit)?,
    override val onDismissListener: (() -> Unit)?,
) : BottomSheetDialogMetadata

class CustomBottomSheetDialog(handler: DialogHandler<CustomBottomSheetDialogMetadata>) :
    BaseDialog<CustomBottomSheetDialogMetadata, CustomBottomSheetDialog>(handler) {

    var view: View? = null

    @LayoutRes
    var viewLayoutResId: Int? = null
    var viewBinder: ((CustomBottomSheetDialog, View) -> Unit)? = null

    constructor(fragmentManager: FragmentManager) :
            this(ScaffoldConfig.UI.defaultCustomBottomSheetDialogHandlerCreator(fragmentManager))

    override fun buildMetadata() = CustomBottomSheetDialogMetadata(
        view = view,
        viewLayoutResId = viewLayoutResId,
        viewBinder = convertViewBinder(viewBinder),
        cancelable = cancelable ?: ScaffoldConfig.UI.defaultCustomBottomSheetDialogCancelable,
        showDuration = showDuration ?: ScaffoldConfig.UI.defaultCustomBottomSheetDialogShowDuration,
        onShowListener = convertOnShowOrDismissListener(this, onShowListener),
        onDismissListener = convertOnShowOrDismissListener(this, onDismissListener),
    )

    override fun resetMetadata() {
        super.resetMetadata()
        view = null
        viewLayoutResId = null
        viewBinder = null
    }

    private fun convertViewBinder(binder: ((CustomBottomSheetDialog, View) -> Unit)?): ((View) -> Unit)? {
        return binder?.let { br ->
            { v -> br(this, v) }
        }
    }

}

class DefaultCustomBottomSheetDialogHandler(fragmentManager: FragmentManager) :
    BottomSheetDialogHandler<CustomBottomSheetDialogMetadata>(
        fragmentManager,
        R.style.yg_DialogCustomBottomSheetDefaultStyle
    ) {

    override fun createDialogView(
        platformDialogWrapper: BottomSheetPlatformDialogWrapper,
        metadata: CustomBottomSheetDialogMetadata
    ): View {
        val view = when {
            metadata.view != null -> metadata.view.apply {
                (parent as? ViewGroup)?.removeView(this)
            }
            metadata.viewLayoutResId != null -> LayoutInflater.from(platformDialogWrapper.platformDialog.requireContext())
                .inflate(
                    metadata.viewLayoutResId,
                    FrameLayout(platformDialogWrapper.platformDialog.requireContext())
                )
            else -> throw IllegalArgumentException("Both 'view' and 'viewLayoutResId' of CustomBottomSheetDialog cannot be null")
        }
        metadata.viewBinder?.invoke(view)
        return view
    }

    override fun updateDialogView(
        platformDialogWrapper: BottomSheetPlatformDialogWrapper,
        metadata: CustomBottomSheetDialogMetadata
    ) {
        ScaffoldLogger.warn("[Dialog] CustomBottomSheetDialog does not support updates")
    }

}