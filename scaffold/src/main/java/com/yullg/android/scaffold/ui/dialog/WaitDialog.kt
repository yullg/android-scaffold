package com.yullg.android.scaffold.ui.dialog

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.annotation.StyleRes
import androidx.annotation.StyleableRes
import androidx.fragment.app.FragmentManager
import com.yullg.android.scaffold.R
import com.yullg.android.scaffold.app.ScaffoldConfig
import com.yullg.android.scaffold.databinding.YgDialogWaitCircularBinding
import com.yullg.android.scaffold.databinding.YgDialogWaitLinearBinding

data class WaitDialogMetadata(
    @StringRes val messageResId: Int?,
    val message: CharSequence?,
    val progress: Int?,
    override val cancelable: Boolean,
    override val showDuration: Long,
    override val onShowListener: (() -> Unit)?,
    override val onDismissListener: (() -> Unit)?,
) : MaterialDialogMetadata

class WaitDialog(handler: DialogHandler<WaitDialogMetadata>) :
    BaseDialog<WaitDialogMetadata, WaitDialog>(handler) {

    @StringRes
    var messageResId: Int? = null
    var message: CharSequence? = null
    var progress: Int? = null

    constructor(fragmentManager: FragmentManager) :
            this(ScaffoldConfig.UI.defaultWaitDialogHandlerCreator(fragmentManager))

    override fun buildMetadata() = WaitDialogMetadata(
        messageResId = messageResId,
        message = message,
        progress = progress,
        cancelable = cancelable ?: ScaffoldConfig.UI.defaultWaitDialogCancelable,
        showDuration = showDuration ?: ScaffoldConfig.UI.defaultWaitDialogShowDuration,
        onShowListener = convertOnShowOrDismissListener(this, onShowListener),
        onDismissListener = convertOnShowOrDismissListener(this, onDismissListener),
    )

    override fun resetMetadata() {
        super.resetMetadata()
        messageResId = null
        message = null
        progress = null
    }

}

class DefaultCircularWaitDialogHandler(
    fragmentManager: FragmentManager,
    @StyleableRes defStyleAttr: Int = R.styleable.yg_ThemeAttrDeclare_yg_dialogWaitStyle,
    @StyleRes defStyleRes: Int = R.style.yg_DialogWaitDefaultStyle
) : MaterialDialogHandler<WaitDialogMetadata>(
    fragmentManager,
    defStyleAttr,
    defStyleRes,
) {

    private var binding: YgDialogWaitCircularBinding? = null

    override fun createDialogView(
        dialogShell: NormalDialogShell,
        metadata: WaitDialogMetadata
    ): View {
        val localBinding = binding ?: YgDialogWaitCircularBinding.inflate(
            LayoutInflater.from(dialogShell.requireContext())
        )
        bindData(localBinding, metadata)
        this.binding = localBinding
        return localBinding.root.apply {
            (parent as? ViewGroup)?.removeView(this)
        }
    }

    override fun updateDialogView(dialogShell: NormalDialogShell, metadata: WaitDialogMetadata) {
        binding?.let { bindData(it, metadata) }
    }

    private fun bindData(binding: YgDialogWaitCircularBinding, metadata: WaitDialogMetadata) {
        if (metadata.progress != null) {
            binding.ygIndicator.setProgressCompat(metadata.progress, true)
        } else if (!binding.ygIndicator.isIndeterminate) {
            binding.ygIndicator.visibility = View.INVISIBLE
            binding.ygIndicator.isIndeterminate = true
            binding.ygIndicator.visibility = View.VISIBLE
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

class DefaultLinearWaitDialogHandler(
    fragmentManager: FragmentManager,
    @StyleableRes defStyleAttr: Int = R.styleable.yg_ThemeAttrDeclare_yg_dialogWaitStyle,
    @StyleRes defStyleRes: Int = R.style.yg_DialogWaitDefaultStyle
) : MaterialDialogHandler<WaitDialogMetadata>(
    fragmentManager,
    defStyleAttr,
    defStyleRes,
) {

    private var binding: YgDialogWaitLinearBinding? = null

    override fun createDialogView(
        dialogShell: NormalDialogShell,
        metadata: WaitDialogMetadata
    ): View {
        val localBinding = binding ?: YgDialogWaitLinearBinding.inflate(
            LayoutInflater.from(dialogShell.requireContext())
        )
        bindData(localBinding, metadata)
        this.binding = localBinding
        return localBinding.root.apply {
            (parent as? ViewGroup)?.removeView(this)
        }
    }

    override fun updateDialogView(dialogShell: NormalDialogShell, metadata: WaitDialogMetadata) {
        binding?.let { bindData(it, metadata) }
    }

    private fun bindData(binding: YgDialogWaitLinearBinding, metadata: WaitDialogMetadata) {
        if (metadata.progress != null) {
            binding.ygIndicator.setProgressCompat(metadata.progress, true)
        } else if (!binding.ygIndicator.isIndeterminate) {
            binding.ygIndicator.visibility = View.INVISIBLE
            binding.ygIndicator.isIndeterminate = true
            binding.ygIndicator.visibility = View.VISIBLE
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