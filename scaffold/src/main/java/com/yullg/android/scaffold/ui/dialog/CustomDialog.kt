package com.yullg.android.scaffold.ui.dialog

import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.*
import androidx.fragment.app.FragmentActivity
import com.yullg.android.scaffold.R
import com.yullg.android.scaffold.internal.ScaffoldLogger
import com.yullg.android.scaffold.ui.UIConfig
import java.lang.ref.WeakReference

data class CustomDialogMetadata(
    val view: View?,
    @LayoutRes val viewLayoutResId: Int?,
    val viewBinder: ((View) -> Unit)?,
    override val cancelable: Boolean,
    override val showDuration: Long,
    override val onShowListener: DialogInterface.OnShowListener?,
    override val onDismissListener: DialogInterface.OnDismissListener?,
) : MaterialDialogMetadata

class CustomDialog(handler: BaseDialogHandler<CustomDialogMetadata>) :
    MaterialDialog<CustomDialogMetadata, CustomDialog>(handler) {

    private var view: View? = null
    private var viewLayoutResId: Int? = null
    private var viewBinder: ((View) -> Unit)? = null

    constructor(fragmentActivity: FragmentActivity) :
            this(UIConfig.defaultCustomDialogHandlerCreator(fragmentActivity))

    fun setView(view: View?): CustomDialog {
        this.view = view
        return this
    }

    fun setViewLayoutResId(@LayoutRes viewLayoutResId: Int?): CustomDialog {
        this.viewLayoutResId = viewLayoutResId
        return this
    }

    fun setViewBinder(viewBinder: ((CustomDialog, View) -> Unit)?): CustomDialog {
        this.viewBinder = viewBinder?.let {
            { v -> it(this@CustomDialog, v) }
        }
        return this
    }

    override fun buildMetadata() = CustomDialogMetadata(
        view = view,
        viewLayoutResId = viewLayoutResId,
        viewBinder = viewBinder,
        cancelable = cancelable ?: UIConfig.defaultCustomDialogCancelable,
        showDuration = showDuration ?: UIConfig.defaultCustomDialogShowDuration,
        onShowListener = onShowListener,
        onDismissListener = onDismissListener,
    )

    override fun resetMetadata(): CustomDialog {
        view = null
        viewLayoutResId = null
        viewBinder = null
        return super.resetMetadata()
    }

}

class DefaultCustomDialogHandler(
    fragmentActivity: FragmentActivity,
    override val template: DialogTemplate<CustomDialogMetadata> =
        CustomDialogTemplate(fragmentActivity),
    @StyleableRes defStyleAttr: Int = R.styleable.yg_ThemeAttrDeclare_yg_dialogCustomStyle,
    @StyleRes defStyleRes: Int = R.style.yg_DialogCustomDefaultStyle
) : MaterialDialogHandler<CustomDialogMetadata>(
    fragmentActivity,
    defStyleAttr,
    defStyleRes,
), DialogTemplateHandler<DialogTemplate<CustomDialogMetadata>> {

    override fun createDialogView(context: Context, metadata: CustomDialogMetadata): View {
        return template.onCreateView(metadata)
    }

    override fun updateDialogView(context: Context, metadata: CustomDialogMetadata) {
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

class CustomDialogTemplate(@UiContext context: Context) :
    DialogTemplate<CustomDialogMetadata> {

    private val contextRef = WeakReference(context)

    @RestrictTo(RestrictTo.Scope.LIBRARY, RestrictTo.Scope.SUBCLASSES)
    override fun onCreateView(metadata: CustomDialogMetadata): View {
        val context =
            contextRef.get() ?: throw IllegalStateException("The context has been cleared")
        val view = when {
            metadata.view != null -> metadata.view.apply {
                (parent as? ViewGroup)?.removeView(this)
            }
            metadata.viewLayoutResId != null -> LayoutInflater.from(context)
                .inflate(metadata.viewLayoutResId, FrameLayout(context))
            else -> throw IllegalArgumentException("Both 'view' and 'viewLayoutResId' of CustomDialog cannot be null")
        }
        metadata.viewBinder?.invoke(view)
        return view
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY, RestrictTo.Scope.SUBCLASSES)
    override fun onUpdateView(metadata: CustomDialogMetadata) {
        if (ScaffoldLogger.isWarnEnabled()) {
            ScaffoldLogger.warn("CustomDialog does not support updates")
        }
    }

}