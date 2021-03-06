package com.yullg.android.scaffold.ui.dialog

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.*
import androidx.fragment.app.FragmentActivity
import com.yullg.android.scaffold.R
import com.yullg.android.scaffold.app.ScaffoldConfig
import com.yullg.android.scaffold.databinding.YgDialogTipBinding
import java.lang.ref.WeakReference

data class TipDialogMetadata(
    @DrawableRes val iconResId: Int?,
    val icon: Drawable?,
    @StringRes val messageResId: Int?,
    val message: CharSequence?,
    override val cancelable: Boolean,
    override val showDuration: Long,
    override val onShowListener: (() -> Unit)?,
    override val onDismissListener: (() -> Unit)?,
) : MaterialDialogMetadata

class TipDialog(handler: BaseDialogHandler<TipDialogMetadata>) :
    MaterialDialog<TipDialogMetadata, TipDialog>(handler) {

    private var iconResId: Int? = null
    private var icon: Drawable? = null
    private var messageResId: Int? = null
    private var message: CharSequence? = null

    constructor(activity: FragmentActivity) :
            this(ScaffoldConfig.UI.defaultTipDialogHandlerCreator(activity))

    fun setIconResource(@DrawableRes resId: Int?): TipDialog {
        this.iconResId = resId
        return this
    }

    fun setIcon(icon: Drawable?): TipDialog {
        this.icon = icon
        return this
    }

    fun setMessageResource(@StringRes resId: Int?): TipDialog {
        this.messageResId = resId
        return this
    }

    fun setMessage(message: CharSequence?): TipDialog {
        this.message = message
        return this
    }

    override fun buildMetadata() = TipDialogMetadata(
        iconResId = iconResId,
        icon = icon,
        messageResId = messageResId,
        message = message,
        cancelable = cancelable ?: ScaffoldConfig.UI.defaultTipDialogCancelable,
        showDuration = showDuration ?: ScaffoldConfig.UI.defaultTipDialogShowDuration,
        onShowListener = onShowListener,
        onDismissListener = onDismissListener,
    )

    override fun resetMetadata(): TipDialog {
        iconResId = null
        icon = null
        messageResId = null
        message = null
        return super.resetMetadata()
    }

    companion object {

        @DrawableRes
        val ICON_RESOURCE_SUCCESS = R.drawable.yg_dialog_tip_done_black_40dp

        @DrawableRes
        val ICON_RESOURCE_ERROR = R.drawable.yg_dialog_tip_error_black_40dp

        @DrawableRes
        val ICON_RESOURCE_WARNING = R.drawable.yg_dialog_tip_warning_black_40dp

    }

}

class DefaultTipDialogHandler(
    activity: FragmentActivity,
    override val template: DialogTemplate<TipDialogMetadata> = TipDialogTemplate(activity),
    @StyleableRes defStyleAttr: Int = R.styleable.yg_ThemeAttrDeclare_yg_dialogTipStyle,
    @StyleRes defStyleRes: Int = R.style.yg_DialogTipDefaultStyle,
) : MaterialDialogHandler<TipDialogMetadata>(
    activity,
    defStyleAttr,
    defStyleRes,
), DialogTemplateHandler<DialogTemplate<TipDialogMetadata>> {

    override fun createDialogView(context: Context, metadata: TipDialogMetadata): View {
        return template.onCreateView(metadata)
    }

    override fun updateDialogView(context: Context, metadata: TipDialogMetadata) {
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

class TipDialogTemplate(@UiContext context: Context) :
    DialogTemplate<TipDialogMetadata> {

    private val contextRef = WeakReference(context)

    val binding: YgDialogTipBinding by lazy {
        val context =
            contextRef.get() ?: throw IllegalStateException("Context has been reclaimed")
        YgDialogTipBinding.inflate(LayoutInflater.from(context))
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY, RestrictTo.Scope.SUBCLASSES)
    override fun onCreateView(metadata: TipDialogMetadata): View {
        bindData(metadata)
        return binding.root.apply {
            (parent as? ViewGroup)?.removeView(this)
        }
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY, RestrictTo.Scope.SUBCLASSES)
    override fun onUpdateView(metadata: TipDialogMetadata) {
        bindData(metadata)
    }

    private fun bindData(metadata: TipDialogMetadata) {
        if (metadata.iconResId != null) {
            binding.ygIcon.setImageResource(metadata.iconResId)
            binding.ygIcon.visibility = View.VISIBLE
        } else if (metadata.icon != null) {
            binding.ygIcon.setImageDrawable(metadata.icon)
            binding.ygIcon.visibility = View.VISIBLE
        } else {
            binding.ygIcon.visibility = View.GONE
        }
        if (metadata.messageResId != null) {
            binding.ygMessage.setText(metadata.messageResId)
            binding.ygMessage.visibility = View.VISIBLE
        } else if (metadata.message != null) {
            binding.ygMessage.text = metadata.message
            binding.ygMessage.visibility = View.VISIBLE
        } else {
            binding.ygMessage.visibility = View.GONE
        }
    }

}