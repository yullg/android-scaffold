package com.yullg.android.scaffold.ui.dialog

import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.*
import com.yullg.android.scaffold.R
import com.yullg.android.scaffold.internal.ScaffoldLogger
import com.yullg.android.scaffold.ui.UIConfig
import java.lang.ref.WeakReference

data class CustomBottomSheetDialogMetadata(
    val view: View?,
    @LayoutRes val viewLayoutResId: Int?,
    val viewBinder: ((View) -> Unit)?,
    override val cancelable: Boolean,
    override val showDuration: Long,
    override val onShowListener: DialogInterface.OnShowListener?,
    override val onDismissListener: DialogInterface.OnDismissListener?,
) : BottomSheetDialogMetadata

class CustomBottomSheetDialog(handler: BaseDialogHandler<CustomBottomSheetDialogMetadata>) :
    BottomSheetDialog<CustomBottomSheetDialogMetadata, CustomBottomSheetDialog>(handler) {

    private var view: View? = null
    private var viewLayoutResId: Int? = null
    private var viewBinder: ((View) -> Unit)? = null

    constructor(@UiContext context: Context) :
            this(UIConfig.defaultCustomBottomSheetDialogHandlerCreator(context))

    fun setView(view: View?): CustomBottomSheetDialog {
        this.view = view
        return this
    }

    fun setViewLayoutResId(@LayoutRes viewLayoutResId: Int?): CustomBottomSheetDialog {
        this.viewLayoutResId = viewLayoutResId
        return this
    }

    fun setViewBinder(viewBinder: ((CustomBottomSheetDialog, View) -> Unit)?): CustomBottomSheetDialog {
        this.viewBinder = viewBinder?.let {
            { v -> it(this@CustomBottomSheetDialog, v) }
        }
        return this
    }

    override fun buildMetadata() = CustomBottomSheetDialogMetadata(
        view = view,
        viewLayoutResId = viewLayoutResId,
        viewBinder = viewBinder,
        cancelable = cancelable ?: UIConfig.defaultCustomBottomSheetDialogCancelable,
        showDuration = showDuration ?: UIConfig.defaultCustomBottomSheetDialogShowDuration,
        onShowListener = onShowListener,
        onDismissListener = onDismissListener,
    )

    override fun resetMetadata(): CustomBottomSheetDialog {
        view = null
        viewLayoutResId = null
        viewBinder = null
        return super.resetMetadata()
    }

}

class DefaultCustomBottomSheetDialogHandler(
    @UiContext context: Context,
    override val template: DialogTemplate<CustomBottomSheetDialogMetadata> =
        CustomBottomDialogTemplate(context),
    @StyleableRes defStyleAttr: Int = R.styleable.yg_ThemeAttrDeclare_yg_dialogCustomBottomSheetStyle,
    @StyleRes defStyleRes: Int = R.style.yg_DialogCustomBottomSheetDefaultStyle,
) : BottomSheetDialogHandler<CustomBottomSheetDialogMetadata>(
    context,
    defStyleAttr,
    defStyleRes
), DialogTemplateHandler<DialogTemplate<CustomBottomSheetDialogMetadata>> {

    override fun createDialogView(
        context: Context,
        metadata: CustomBottomSheetDialogMetadata
    ): View {
        return template.onCreateView(metadata)
    }

    override fun updateDialogView(context: Context, metadata: CustomBottomSheetDialogMetadata) {
        template.onUpdateView(metadata)
    }

    override fun onDismiss() {
        try {
            template.onDestroyView()
        } finally {
            super.onDismiss()
        }
    }

}

class CustomBottomDialogTemplate(@UiContext context: Context) :
    DialogTemplate<CustomBottomSheetDialogMetadata> {

    private val contextRef = WeakReference(context)

    @RestrictTo(RestrictTo.Scope.LIBRARY, RestrictTo.Scope.SUBCLASSES)
    override fun onCreateView(metadata: CustomBottomSheetDialogMetadata): View {
        val context =
            contextRef.get() ?: throw IllegalStateException("The context has been cleared")
        val view = when {
            metadata.view != null -> metadata.view.apply {
                (parent as? ViewGroup)?.removeView(this)
            }
            metadata.viewLayoutResId != null -> LayoutInflater.from(context)
                .inflate(metadata.viewLayoutResId, FrameLayout(context))
            else -> throw IllegalArgumentException("Both 'view' and 'viewLayoutResId' of CustomBottomSheetDialog cannot be null")
        }
        metadata.viewBinder?.invoke(view)
        return view
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY, RestrictTo.Scope.SUBCLASSES)
    override fun onUpdateView(metadata: CustomBottomSheetDialogMetadata) {
        if (ScaffoldLogger.isWarnEnabled()) {
            ScaffoldLogger.warn("CustomBottomSheetDialog does not support updates")
        }
    }

}