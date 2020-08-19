package com.yullg.android.scaffold.ui

import android.content.Context
import com.yullg.android.scaffold.ui.dialog.*
import com.yullg.android.scaffold.ui.util.ActionProxy
import com.yullg.android.scaffold.ui.util.ThrottledActionProxy

interface UIConfig {

    val throttledActionProxyCreator: () -> ActionProxy

    val sharedThrottledActionProxyCreator: () -> ActionProxy

    val defaultTipDialogHandlerCreator: (Context) -> BaseDialogHandler<TipDialogMetadata>

    val defaultWaitDialogHandlerCreator: (Context) -> BaseDialogHandler<WaitDialogMetadata>

    val defaultAlertDialogHandlerCreator: (Context) -> BaseDialogHandler<AlertDialogMetadata>

    val defaultCustomDialogHandlerCreator: (Context) -> BaseDialogHandler<CustomDialogMetadata>

    val defaultCustomBottomSheetDialogHandlerCreator: (Context) -> BaseDialogHandler<CustomBottomSheetDialogMetadata>

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

    companion object : UIConfig by MutableUIConfig

}

open class MutableUIConfig private constructor() : UIConfig {

    override var throttledActionProxyCreator: () -> ActionProxy =
        { ThrottledActionProxy(1500) }

    override var sharedThrottledActionProxyCreator: () -> ActionProxy =
        { throttledActionProxyCreator() }

    override var defaultTipDialogHandlerCreator: (Context) -> BaseDialogHandler<TipDialogMetadata> =
        { context -> DefaultTipDialogHandler(context) }

    override var defaultWaitDialogHandlerCreator: (Context) -> BaseDialogHandler<WaitDialogMetadata> =
        { context -> DefaultWaitDialogHandler(context) }

    override var defaultAlertDialogHandlerCreator: (Context) -> BaseDialogHandler<AlertDialogMetadata> =
        { context -> DefaultAlertDialogHandler(context) }

    override var defaultCustomDialogHandlerCreator: (Context) -> BaseDialogHandler<CustomDialogMetadata> =
        { context -> DefaultCustomDialogHandler(context) }

    override var defaultCustomBottomSheetDialogHandlerCreator: (Context) -> BaseDialogHandler<CustomBottomSheetDialogMetadata> =
        { context -> DefaultCustomBottomSheetDialogHandler(context) }

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