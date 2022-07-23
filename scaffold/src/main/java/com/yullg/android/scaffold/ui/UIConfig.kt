package com.yullg.android.scaffold.ui

import androidx.fragment.app.FragmentManager
import com.yullg.android.scaffold.core.ActionProxy
import com.yullg.android.scaffold.core.ThrottledActionProxy
import com.yullg.android.scaffold.ui.dialog.*

interface UIConfig {

    val clickThrottledActionProxyCreator: () -> ActionProxy<Unit, Unit?>

    val defaultTipDialogHandlerCreator: (FragmentManager) -> DialogHandler<TipDialogMetadata>

    val defaultWaitDialogHandlerCreator: (FragmentManager) -> DialogHandler<WaitDialogMetadata>

    val defaultAlertDialogHandlerCreator: (FragmentManager) -> DialogHandler<AlertDialogMetadata>

    val defaultCustomDialogHandlerCreator: (FragmentManager) -> DialogHandler<CustomDialogMetadata>

    val defaultCustomBottomSheetDialogHandlerCreator: (FragmentManager) -> DialogHandler<CustomBottomSheetDialogMetadata>

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

    override var clickThrottledActionProxyCreator: () -> ActionProxy<Unit, Unit?> =
        { ThrottledActionProxy(1000) }

    override var defaultTipDialogHandlerCreator: (FragmentManager) -> DialogHandler<TipDialogMetadata> =
        { DefaultTipDialogHandler(it) }

    override var defaultWaitDialogHandlerCreator: (FragmentManager) -> DialogHandler<WaitDialogMetadata> =
        { DefaultCircularWaitDialogHandler(it) }

    override var defaultAlertDialogHandlerCreator: (FragmentManager) -> DialogHandler<AlertDialogMetadata> =
        { DefaultMaterialAlertDialogHandler(it) }

    override var defaultCustomDialogHandlerCreator: (FragmentManager) -> DialogHandler<CustomDialogMetadata> =
        { DefaultCustomDialogHandler(it) }

    override var defaultCustomBottomSheetDialogHandlerCreator: (FragmentManager) -> DialogHandler<CustomBottomSheetDialogMetadata> =
        { DefaultCustomBottomSheetDialogHandler(it) }

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