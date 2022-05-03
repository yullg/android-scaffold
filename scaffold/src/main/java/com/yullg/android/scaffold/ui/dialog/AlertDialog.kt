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
import com.yullg.android.scaffold.databinding.YgDialogAlertCupertinoBinding
import com.yullg.android.scaffold.databinding.YgDialogAlertMaterialBinding

data class AlertDialogMetadata(
    @StringRes val titleResId: Int?,
    val title: CharSequence?,
    @StringRes val messageResId: Int?,
    val message: CharSequence?,
    @StringRes val negativeButtonTextResId: Int?,
    val negativeButtonText: CharSequence?,
    val negativeButtonClickListener: View.OnClickListener?,
    @StringRes val neutralButtonTextResId: Int?,
    val neutralButtonText: CharSequence?,
    val neutralButtonClickListener: View.OnClickListener?,
    @StringRes val positiveButtonTextResId: Int?,
    val positiveButtonText: CharSequence?,
    val positiveButtonClickListener: View.OnClickListener?,
    override val cancelable: Boolean,
    override val showDuration: Long,
    override val onShowListener: (() -> Unit)?,
    override val onDismissListener: (() -> Unit)?,
) : MaterialDialogMetadata

class AlertDialog(handler: DialogHandler<AlertDialogMetadata>) :
    BaseDialog<AlertDialogMetadata, AlertDialog>(handler) {

    @StringRes
    var titleResId: Int? = null
    var title: CharSequence? = null

    @StringRes
    var messageResId: Int? = null
    var message: CharSequence? = null

    @StringRes
    var negativeButtonTextResId: Int? = null
    var negativeButtonText: CharSequence? = null
    var negativeButtonClickListener: ((AlertDialog) -> Unit)? = null

    @StringRes
    var neutralButtonTextResId: Int? = null
    var neutralButtonText: CharSequence? = null
    var neutralButtonClickListener: ((AlertDialog) -> Unit)? = null

    @StringRes
    var positiveButtonTextResId: Int? = null
    var positiveButtonText: CharSequence? = null
    var positiveButtonClickListener: ((AlertDialog) -> Unit)? = null

    constructor(fragmentManager: FragmentManager) :
            this(ScaffoldConfig.UI.defaultAlertDialogHandlerCreator(fragmentManager))

    override fun buildMetadata() = AlertDialogMetadata(
        titleResId = titleResId,
        title = title,
        messageResId = messageResId,
        message = message,
        negativeButtonTextResId = negativeButtonTextResId,
        negativeButtonText = negativeButtonText,
        negativeButtonClickListener = convertButtonClickListener(negativeButtonClickListener),
        neutralButtonTextResId = neutralButtonTextResId,
        neutralButtonText = neutralButtonText,
        neutralButtonClickListener = convertButtonClickListener(neutralButtonClickListener),
        positiveButtonTextResId = positiveButtonTextResId,
        positiveButtonText = positiveButtonText,
        positiveButtonClickListener = convertButtonClickListener(positiveButtonClickListener),
        cancelable = cancelable ?: ScaffoldConfig.UI.defaultAlertDialogCancelable,
        showDuration = showDuration ?: ScaffoldConfig.UI.defaultAlertDialogShowDuration,
        onShowListener = convertOnShowOrDismissListener(this, onShowListener),
        onDismissListener = convertOnShowOrDismissListener(this, onDismissListener),
    )

    override fun resetMetadata() {
        super.resetMetadata()
        titleResId = null
        title = null
        messageResId = null
        message = null
        negativeButtonTextResId = null
        negativeButtonText = null
        negativeButtonClickListener = null
        neutralButtonTextResId = null
        neutralButtonText = null
        neutralButtonClickListener = null
        positiveButtonTextResId = null
        positiveButtonText = null
        positiveButtonClickListener = null
    }

    private fun convertButtonClickListener(listener: ((AlertDialog) -> Unit)?): View.OnClickListener? {
        return listener?.let { lr ->
            View.OnClickListener { lr(this) }
        }
    }

}

class DefaultMaterialAlertDialogHandler(
    fragmentManager: FragmentManager,
    @StyleableRes defStyleAttr: Int = R.styleable.yg_ThemeAttrDeclare_yg_dialogAlertStyle,
    @StyleRes defStyleRes: Int = R.style.yg_DialogAlertDefaultStyle
) : MaterialDialogHandler<AlertDialogMetadata>(
    fragmentManager,
    defStyleAttr,
    defStyleRes,
) {

    private var binding: YgDialogAlertMaterialBinding? = null

    override fun createDialogView(
        dialogShell: NormalDialogShell,
        metadata: AlertDialogMetadata
    ): View {
        val localBinding = binding ?: YgDialogAlertMaterialBinding.inflate(
            LayoutInflater.from(dialogShell.requireContext())
        )
        bindData(localBinding, metadata)
        this.binding = localBinding
        return localBinding.root.apply {
            (parent as? ViewGroup)?.removeView(this)
        }
    }

    override fun updateDialogView(dialogShell: NormalDialogShell, metadata: AlertDialogMetadata) {
        binding?.let { bindData(it, metadata) }
    }

    private fun bindData(binding: YgDialogAlertMaterialBinding, metadata: AlertDialogMetadata) {
        if (metadata.titleResId != null) {
            binding.ygTitle.setText(metadata.titleResId)
            binding.ygTitle.visibility = View.VISIBLE
        } else if (metadata.title != null) {
            binding.ygTitle.text = metadata.title
            binding.ygTitle.visibility = View.VISIBLE
        } else {
            binding.ygTitle.visibility = View.GONE
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
        binding.ygButtonNegative.setOnClickListener(metadata.negativeButtonClickListener)
        if (metadata.negativeButtonTextResId != null) {
            binding.ygButtonNegative.setText(metadata.negativeButtonTextResId)
            binding.ygButtonNegative.visibility = View.VISIBLE
        } else if (metadata.negativeButtonText != null) {
            binding.ygButtonNegative.text = metadata.negativeButtonText
            binding.ygButtonNegative.visibility = View.VISIBLE
        } else {
            binding.ygButtonNegative.visibility = View.GONE
        }
        binding.ygButtonNeutral.setOnClickListener(metadata.neutralButtonClickListener)
        if (metadata.neutralButtonTextResId != null) {
            binding.ygButtonNeutral.setText(metadata.neutralButtonTextResId)
            binding.ygButtonNeutral.visibility = View.VISIBLE
        } else if (metadata.neutralButtonText != null) {
            binding.ygButtonNeutral.text = metadata.neutralButtonText
            binding.ygButtonNeutral.visibility = View.VISIBLE
        } else {
            binding.ygButtonNeutral.visibility = View.GONE
        }
        binding.ygButtonPositive.setOnClickListener(metadata.positiveButtonClickListener)
        if (metadata.positiveButtonTextResId != null) {
            binding.ygButtonPositive.setText(metadata.positiveButtonTextResId)
            binding.ygButtonPositive.visibility = View.VISIBLE
        } else if (metadata.positiveButtonText != null) {
            binding.ygButtonPositive.text = metadata.positiveButtonText
            binding.ygButtonPositive.visibility = View.VISIBLE
        } else {
            binding.ygButtonPositive.visibility = View.GONE
        }
    }

}

class DefaultCupertinoAlertDialogHandler(
    fragmentManager: FragmentManager,
    @StyleableRes defStyleAttr: Int = R.styleable.yg_ThemeAttrDeclare_yg_dialogAlertStyle,
    @StyleRes defStyleRes: Int = R.style.yg_DialogAlertDefaultStyle
) : MaterialDialogHandler<AlertDialogMetadata>(
    fragmentManager,
    defStyleAttr,
    defStyleRes,
) {

    private var binding: YgDialogAlertCupertinoBinding? = null

    override fun createDialogView(
        dialogShell: NormalDialogShell,
        metadata: AlertDialogMetadata
    ): View {
        val localBinding = binding ?: YgDialogAlertCupertinoBinding.inflate(
            LayoutInflater.from(dialogShell.requireContext())
        )
        bindData(localBinding, metadata)
        this.binding = localBinding
        return localBinding.root.apply {
            (parent as? ViewGroup)?.removeView(this)
        }
    }

    override fun updateDialogView(dialogShell: NormalDialogShell, metadata: AlertDialogMetadata) {
        binding?.let { bindData(it, metadata) }
    }

    private fun bindData(binding: YgDialogAlertCupertinoBinding, metadata: AlertDialogMetadata) {
        if (metadata.titleResId != null) {
            binding.ygTitle.setText(metadata.titleResId)
            binding.ygTitle.visibility = View.VISIBLE
        } else if (metadata.title != null) {
            binding.ygTitle.text = metadata.title
            binding.ygTitle.visibility = View.VISIBLE
        } else {
            binding.ygTitle.visibility = View.GONE
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
        binding.ygButtonNegative.setOnClickListener(metadata.negativeButtonClickListener)
        if (metadata.negativeButtonTextResId != null) {
            binding.ygButtonNegative.setText(metadata.negativeButtonTextResId)
            binding.ygButtonNegative.visibility = View.VISIBLE
        } else if (metadata.negativeButtonText != null) {
            binding.ygButtonNegative.text = metadata.negativeButtonText
            binding.ygButtonNegative.visibility = View.VISIBLE
        } else {
            binding.ygButtonNegative.visibility = View.GONE
        }
        binding.ygButtonNeutral.setOnClickListener(metadata.neutralButtonClickListener)
        if (metadata.neutralButtonTextResId != null) {
            binding.ygButtonNeutral.setText(metadata.neutralButtonTextResId)
            binding.ygButtonNeutral.visibility = View.VISIBLE
        } else if (metadata.neutralButtonText != null) {
            binding.ygButtonNeutral.text = metadata.neutralButtonText
            binding.ygButtonNeutral.visibility = View.VISIBLE
        } else {
            binding.ygButtonNeutral.visibility = View.GONE
        }
        binding.ygButtonPositive.setOnClickListener(metadata.positiveButtonClickListener)
        if (metadata.positiveButtonTextResId != null) {
            binding.ygButtonPositive.setText(metadata.positiveButtonTextResId)
            binding.ygButtonPositive.visibility = View.VISIBLE
        } else if (metadata.positiveButtonText != null) {
            binding.ygButtonPositive.text = metadata.positiveButtonText
            binding.ygButtonPositive.visibility = View.VISIBLE
        } else {
            binding.ygButtonPositive.visibility = View.GONE
        }
        if ((metadata.negativeButtonTextResId != null || metadata.negativeButtonText != null)
            && (metadata.neutralButtonTextResId != null || metadata.neutralButtonText != null)
        ) {
            binding.ygDivider2.visibility = View.VISIBLE
        } else {
            binding.ygDivider2.visibility = View.GONE
        }
        if ((metadata.neutralButtonTextResId != null || metadata.neutralButtonText != null)
            && (metadata.positiveButtonTextResId != null || metadata.positiveButtonText != null)
        ) {
            binding.ygDivider3.visibility = View.VISIBLE
        } else if ((metadata.negativeButtonTextResId != null || metadata.negativeButtonText != null)
            && (metadata.positiveButtonTextResId != null || metadata.positiveButtonText != null)
        ) {
            binding.ygDivider3.visibility = View.VISIBLE
        } else {
            binding.ygDivider3.visibility = View.GONE
        }
    }

}