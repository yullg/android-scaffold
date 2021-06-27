package com.yullg.android.scaffold.ui

import androidx.fragment.app.FragmentActivity
import com.yullg.android.scaffold.core.ActionProxy
import com.yullg.android.scaffold.core.ThrottledActionProxy
import com.yullg.android.scaffold.ui.dialog.*

interface UIConfig {

    val clickThrottledActionProxyCreator: () -> ActionProxy<Unit>

    val defaultTipDialogHandlerCreator: (FragmentActivity) -> BaseDialogHandler<TipDialogMetadata>

    val defaultWaitDialogHandlerCreator: (FragmentActivity) -> BaseDialogHandler<WaitDialogMetadata>

    val defaultAlertDialogHandlerCreator: (FragmentActivity) -> BaseDialogHandler<AlertDialogMetadata>

    val defaultCustomDialogHandlerCreator: (FragmentActivity) -> BaseDialogHandler<CustomDialogMetadata>

    val defaultCustomBottomSheetDialogHandlerCreator: (FragmentActivity) -> BaseDialogHandler<CustomBottomSheetDialogMetadata>

    val defaultTipDialogCancelable: Boolean

    val defaultTipDialogShowDuration: Long

    val defaultWaitDialogCancelable: Boolean

    val defaultWaitDialogShowDuration: Long

    val defaultAlertDialogCancelable: Boolean

    val defaultAlertDialogShowDuration: Long

    val defaultCustomDialogCancelable: Boolean

    val defaultCustomDialogShowDuration: Long

    val defaultCustomBottomSheetDialogCancelable: Boolean

    val defaultCustomBottomSheetDialogShowDuration: Long

}

open class MutableUIConfig private constructor() : UIConfig {

    override var clickThrottledActionProxyCreator: () -> ActionProxy<Unit> =
        { ThrottledActionProxy(1000) {} }

    override var defaultTipDialogHandlerCreator: (FragmentActivity) -> BaseDialogHandler<TipDialogMetadata> =
        { activity -> DefaultTipDialogHandler(activity) }

    override var defaultWaitDialogHandlerCreator: (FragmentActivity) -> BaseDialogHandler<WaitDialogMetadata> =
        { activity -> DefaultWaitDialogHandler(activity) }

    override var defaultAlertDialogHandlerCreator: (FragmentActivity) -> BaseDialogHandler<AlertDialogMetadata> =
        { activity -> DefaultAlertDialogHandler(activity) }

    override var defaultCustomDialogHandlerCreator: (FragmentActivity) -> BaseDialogHandler<CustomDialogMetadata> =
        { activity -> DefaultCustomDialogHandler(activity) }

    override var defaultCustomBottomSheetDialogHandlerCreator: (FragmentActivity) -> BaseDialogHandler<CustomBottomSheetDialogMetadata> =
        { activity -> DefaultCustomBottomSheetDialogHandler(activity) }

    override var defaultTipDialogCancelable: Boolean = true

    override var defaultTipDialogShowDuration: Long = 2000

    override var defaultWaitDialogCancelable: Boolean = false

    override var defaultWaitDialogShowDuration: Long = 0

    override var defaultAlertDialogCancelable: Boolean = true

    override var defaultAlertDialogShowDuration: Long = 0

    override var defaultCustomDialogCancelable: Boolean = true

    override var defaultCustomDialogShowDuration: Long = 0

    override var defaultCustomBottomSheetDialogCancelable: Boolean = true

    override var defaultCustomBottomSheetDialogShowDuration: Long = 0

    internal companion object : MutableUIConfig()

}