package com.yullg.android.scaffold.ui.dialog

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.*
import androidx.fragment.app.FragmentActivity
import com.yullg.android.scaffold.R
import com.yullg.android.scaffold.app.ScaffoldConfig
import com.yullg.android.scaffold.databinding.YgDialogWaitCircularBinding
import com.yullg.android.scaffold.databinding.YgDialogWaitLinearBinding
import java.lang.ref.WeakReference

data class WaitDialogMetadata(
    @StringRes val messageResId: Int?,
    val message: CharSequence?,
    val progress: Int?,
    override val cancelable: Boolean,
    override val showDuration: Long,
    override val onShowListener: (() -> Unit)?,
    override val onDismissListener: (() -> Unit)?,
) : MaterialDialogMetadata

class WaitDialog(handler: BaseDialogHandler<WaitDialogMetadata>) :
    MaterialDialog<WaitDialogMetadata, WaitDialog>(handler) {

    private var messageResId: Int? = null
    private var message: CharSequence? = null
    private var progress: Int? = null

    constructor(activity: FragmentActivity) :
            this(ScaffoldConfig.UI.defaultWaitDialogHandlerCreator(activity))

    fun setMessageResource(@StringRes resId: Int?): WaitDialog {
        this.messageResId = resId
        return this
    }

    fun setMessage(message: CharSequence?): WaitDialog {
        this.message = message
        return this
    }

    fun setProgress(progress: Int?): WaitDialog {
        this.progress = progress
        return this
    }

    override fun buildMetadata() = WaitDialogMetadata(
        messageResId = messageResId,
        message = message,
        progress = progress,
        cancelable = cancelable ?: ScaffoldConfig.UI.defaultWaitDialogCancelable,
        showDuration = showDuration ?: ScaffoldConfig.UI.defaultWaitDialogShowDuration,
        onShowListener = onShowListener,
        onDismissListener = onDismissListener,
    )

    override fun resetMetadata(): WaitDialog {
        messageResId = null
        message = null
        progress = null
        return super.resetMetadata()
    }

}

class DefaultWaitDialogHandler(
    activity: FragmentActivity,
    override val template: DialogTemplate<WaitDialogMetadata> =
        CircularWaitDialogTemplate(activity),
    @StyleableRes defStyleAttr: Int = R.styleable.yg_ThemeAttrDeclare_yg_dialogWaitStyle,
    @StyleRes defStyleRes: Int = R.style.yg_DialogWaitDefaultStyle
) : MaterialDialogHandler<WaitDialogMetadata>(
    activity,
    defStyleAttr,
    defStyleRes,
), DialogTemplateHandler<DialogTemplate<WaitDialogMetadata>> {

    override fun createDialogView(context: Context, metadata: WaitDialogMetadata): View {
        return template.onCreateView(metadata)
    }

    override fun updateDialogView(context: Context, metadata: WaitDialogMetadata) {
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

class CircularWaitDialogTemplate(@UiContext context: Context) :
    DialogTemplate<WaitDialogMetadata> {

    private val contextRef = WeakReference(context)

    val binding: YgDialogWaitCircularBinding by lazy {
        val context =
            contextRef.get() ?: throw IllegalStateException("Context has been reclaimed")
        YgDialogWaitCircularBinding.inflate(LayoutInflater.from(context))
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY, RestrictTo.Scope.SUBCLASSES)
    override fun onCreateView(metadata: WaitDialogMetadata): View {
        bindData(metadata)
        return binding.root.apply {
            (parent as? ViewGroup)?.removeView(this)
        }
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY, RestrictTo.Scope.SUBCLASSES)
    override fun onUpdateView(metadata: WaitDialogMetadata) {
        bindData(metadata)
    }

    private fun bindData(metadata: WaitDialogMetadata) {
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

class LinearWaitDialogTemplate(@UiContext context: Context) :
    DialogTemplate<WaitDialogMetadata> {

    private val contextRef = WeakReference(context)

    val binding: YgDialogWaitLinearBinding by lazy {
        val context =
            contextRef.get() ?: throw IllegalStateException("Context has been reclaimed")
        YgDialogWaitLinearBinding.inflate(LayoutInflater.from(context))
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY, RestrictTo.Scope.SUBCLASSES)
    override fun onCreateView(metadata: WaitDialogMetadata): View {
        bindData(metadata)
        return binding.root.apply {
            (parent as? ViewGroup)?.removeView(this)
        }
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY, RestrictTo.Scope.SUBCLASSES)
    override fun onUpdateView(metadata: WaitDialogMetadata) {
        bindData(metadata)
    }

    private fun bindData(metadata: WaitDialogMetadata) {
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