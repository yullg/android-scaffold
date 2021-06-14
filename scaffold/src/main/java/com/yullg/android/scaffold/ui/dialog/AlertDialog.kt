package com.yullg.android.scaffold.ui.dialog

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.*
import androidx.fragment.app.FragmentActivity
import com.yullg.android.scaffold.R
import com.yullg.android.scaffold.databinding.YgDialogAlertCupertinoBinding
import com.yullg.android.scaffold.databinding.YgDialogAlertMaterialBinding
import com.yullg.android.scaffold.ui.UIConfig
import java.lang.ref.WeakReference

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

class AlertDialog(handler: BaseDialogHandler<AlertDialogMetadata>) :
    MaterialDialog<AlertDialogMetadata, AlertDialog>(handler) {

    private var titleResId: Int? = null
    private var title: CharSequence? = null
    private var messageResId: Int? = null
    private var message: CharSequence? = null
    private var negativeButtonTextResId: Int? = null
    private var negativeButtonText: CharSequence? = null
    private var negativeButtonClickListener: View.OnClickListener? = null
    private var neutralButtonTextResId: Int? = null
    private var neutralButtonText: CharSequence? = null
    private var neutralButtonClickListener: View.OnClickListener? = null
    private var positiveButtonTextResId: Int? = null
    private var positiveButtonText: CharSequence? = null
    private var positiveButtonClickListener: View.OnClickListener? = null

    constructor(activity: FragmentActivity) :
            this(UIConfig.defaultAlertDialogHandlerCreator(activity))

    fun setTitleResource(@StringRes resId: Int?): AlertDialog {
        this.titleResId = resId
        return this
    }

    fun setTitle(title: CharSequence?): AlertDialog {
        this.title = title
        return this
    }

    fun setMessageResource(@StringRes resId: Int?): AlertDialog {
        this.messageResId = resId
        return this
    }

    fun setMessage(message: CharSequence?): AlertDialog {
        this.message = message
        return this
    }

    fun setNegativeButtonTextResource(
        @StringRes resId: Int?,
        listener: ((AlertDialog) -> Unit)?
    ): AlertDialog {
        this.negativeButtonTextResId = resId
        this.negativeButtonClickListener = listener?.let {
            View.OnClickListener { _ -> it(this@AlertDialog) }
        }
        return this
    }

    fun setNegativeButtonText(
        text: CharSequence?,
        listener: ((AlertDialog) -> Unit)?
    ): AlertDialog {
        this.negativeButtonText = text
        this.negativeButtonClickListener = listener?.let {
            View.OnClickListener { _ -> it(this@AlertDialog) }
        }
        return this
    }

    fun setNeutralButtonTextResource(
        @StringRes resId: Int?,
        listener: ((AlertDialog) -> Unit)?
    ): AlertDialog {
        this.neutralButtonTextResId = resId
        this.neutralButtonClickListener = listener?.let {
            View.OnClickListener { _ -> it(this@AlertDialog) }
        }
        return this
    }

    fun setNeutralButtonText(
        text: CharSequence?,
        listener: ((AlertDialog) -> Unit)?
    ): AlertDialog {
        this.neutralButtonText = text
        this.neutralButtonClickListener = listener?.let {
            View.OnClickListener { _ -> it(this@AlertDialog) }
        }
        return this
    }

    fun setPositiveButtonTextResource(
        @StringRes resId: Int?,
        listener: ((AlertDialog) -> Unit)?
    ): AlertDialog {
        this.positiveButtonTextResId = resId
        this.positiveButtonClickListener = listener?.let {
            View.OnClickListener { _ -> it(this@AlertDialog) }
        }
        return this
    }

    fun setPositiveButtonText(
        text: CharSequence?,
        listener: ((AlertDialog) -> Unit)?
    ): AlertDialog {
        this.positiveButtonText = text
        this.positiveButtonClickListener = listener?.let {
            View.OnClickListener { _ -> it(this@AlertDialog) }
        }
        return this
    }

    override fun buildMetadata() = AlertDialogMetadata(
        titleResId = titleResId,
        title = title,
        messageResId = messageResId,
        message = message,
        negativeButtonTextResId = negativeButtonTextResId,
        negativeButtonText = negativeButtonText,
        negativeButtonClickListener = negativeButtonClickListener,
        neutralButtonTextResId = neutralButtonTextResId,
        neutralButtonText = neutralButtonText,
        neutralButtonClickListener = neutralButtonClickListener,
        positiveButtonTextResId = positiveButtonTextResId,
        positiveButtonText = positiveButtonText,
        positiveButtonClickListener = positiveButtonClickListener,
        cancelable = cancelable ?: UIConfig.defaultAlertDialogCancelable,
        showDuration = showDuration ?: UIConfig.defaultAlertDialogShowDuration,
        onShowListener = onShowListener,
        onDismissListener = onDismissListener,
    )

    override fun resetMetadata(): AlertDialog {
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
        return super.resetMetadata()
    }

}

class DefaultAlertDialogHandler(
    activity: FragmentActivity,
    override val template: DialogTemplate<AlertDialogMetadata> =
        MaterialAlertDialogTemplate(activity),
    @StyleableRes defStyleAttr: Int = R.styleable.yg_ThemeAttrDeclare_yg_dialogAlertStyle,
    @StyleRes defStyleRes: Int = R.style.yg_DialogAlertDefaultStyle
) : MaterialDialogHandler<AlertDialogMetadata>(
    activity,
    defStyleAttr,
    defStyleRes,
), DialogTemplateHandler<DialogTemplate<AlertDialogMetadata>> {

    override fun createDialogView(context: Context, metadata: AlertDialogMetadata): View {
        return template.onCreateView(metadata)
    }

    override fun updateDialogView(context: Context, metadata: AlertDialogMetadata) {
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

class MaterialAlertDialogTemplate(@UiContext context: Context) :
    DialogTemplate<AlertDialogMetadata> {

    private val contextRef = WeakReference(context)

    val binding: YgDialogAlertMaterialBinding by lazy {
        val context =
            contextRef.get() ?: throw IllegalStateException("Context has been reclaimed")
        YgDialogAlertMaterialBinding.inflate(LayoutInflater.from(context))
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY, RestrictTo.Scope.SUBCLASSES)
    override fun onCreateView(metadata: AlertDialogMetadata): View {
        bindData(metadata)
        return binding.root.apply {
            (parent as? ViewGroup)?.removeView(this)
        }
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY, RestrictTo.Scope.SUBCLASSES)
    override fun onUpdateView(metadata: AlertDialogMetadata) {
        bindData(metadata)
    }

    private fun bindData(metadata: AlertDialogMetadata) {
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

class CupertinoAlertDialogTemplate(@UiContext context: Context) :
    DialogTemplate<AlertDialogMetadata> {

    private val contextRef = WeakReference(context)

    val binding: YgDialogAlertCupertinoBinding by lazy {
        val context =
            contextRef.get() ?: throw IllegalStateException("Context has been reclaimed")
        YgDialogAlertCupertinoBinding.inflate(LayoutInflater.from(context))
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY, RestrictTo.Scope.SUBCLASSES)
    override fun onCreateView(metadata: AlertDialogMetadata): View {
        bindData(metadata)
        return binding.root.apply {
            (parent as? ViewGroup)?.removeView(this)
        }
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY, RestrictTo.Scope.SUBCLASSES)
    override fun onUpdateView(metadata: AlertDialogMetadata) {
        bindData(metadata)
    }

    private fun bindData(metadata: AlertDialogMetadata) {
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