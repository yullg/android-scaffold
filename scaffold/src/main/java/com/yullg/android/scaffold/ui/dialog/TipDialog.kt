package com.yullg.android.scaffold.ui.dialog

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.*
import androidx.fragment.app.FragmentActivity
import com.yullg.android.scaffold.R
import com.yullg.android.scaffold.databinding.YgDialogTipBinding
import com.yullg.android.scaffold.ui.UIConfig
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

    constructor(fragmentActivity: FragmentActivity) :
            this(UIConfig.defaultTipDialogHandlerCreator(fragmentActivity))

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
        cancelable = cancelable ?: UIConfig.defaultTipDialogCancelable,
        showDuration = showDuration ?: UIConfig.defaultTipDialogShowDuration,
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
    fragmentActivity: FragmentActivity,
    override val template: DialogTemplate<TipDialogMetadata> = TipDialogTemplate(fragmentActivity),
    @StyleableRes defStyleAttr: Int = R.styleable.yg_ThemeAttrDeclare_yg_dialogTipStyle,
    @StyleRes defStyleRes: Int = R.style.yg_DialogTipDefaultStyle,
) : MaterialDialogHandler<TipDialogMetadata>(
    fragmentActivity,
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
            contextRef.get() ?: throw IllegalStateException("The context has been cleared")
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