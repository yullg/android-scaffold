package com.yullg.android.scaffold.ui.dialog

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.fragment.app.FragmentManager
import com.yullg.android.scaffold.R
import com.yullg.android.scaffold.app.ScaffoldConfig
import com.yullg.android.scaffold.databinding.YgDialogTipBinding

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

class TipDialog(handler: DialogHandler<TipDialogMetadata>) :
    BaseDialog<TipDialogMetadata, TipDialog>(handler) {

    @DrawableRes
    var iconResId: Int? = null
    var icon: Drawable? = null

    @StringRes
    var messageResId: Int? = null
    var message: CharSequence? = null

    constructor(fragmentManager: FragmentManager) :
            this(ScaffoldConfig.UI.defaultTipDialogHandlerCreator(fragmentManager))

    override fun buildMetadata() = TipDialogMetadata(
        iconResId = iconResId,
        icon = icon,
        messageResId = messageResId,
        message = message,
        cancelable = cancelable ?: ScaffoldConfig.UI.defaultTipDialogCancelable,
        showDuration = showDuration ?: ScaffoldConfig.UI.defaultTipDialogShowDuration,
        onShowListener = convertOnShowOrDismissListener(this, onShowListener),
        onDismissListener = convertOnShowOrDismissListener(this, onDismissListener),
    )

    override fun resetMetadata() {
        super.resetMetadata()
        iconResId = null
        icon = null
        messageResId = null
        message = null
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

class DefaultTipDialogHandler(fragmentManager: FragmentManager) :
    MaterialDialogHandler<TipDialogMetadata>(
        fragmentManager,
        R.style.yg_DialogTipDefaultStyle
    ) {

    private var binding: YgDialogTipBinding? = null

    override fun createDialogView(
        platformDialogWrapper: NormalPlatformDialogWrapper,
        metadata: TipDialogMetadata
    ): View {
        val localBinding = binding ?: YgDialogTipBinding.inflate(
            LayoutInflater.from(platformDialogWrapper.platformDialog.requireContext())
        )
        bindData(localBinding, metadata)
        this.binding = localBinding
        return localBinding.root.apply {
            (parent as? ViewGroup)?.removeView(this)
        }
    }

    override fun updateDialogView(
        platformDialogWrapper: NormalPlatformDialogWrapper,
        metadata: TipDialogMetadata
    ) {
        binding?.let { bindData(it, metadata) }
    }

    private fun bindData(binding: YgDialogTipBinding, metadata: TipDialogMetadata) {
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