package com.yullg.android.scaffold.ui.dialog

import android.view.View
import androidx.annotation.RestrictTo

interface DialogTemplate<M : DialogMetadata> {

    @RestrictTo(RestrictTo.Scope.LIBRARY, RestrictTo.Scope.SUBCLASSES)
    fun onCreateView(metadata: M): View

    @RestrictTo(RestrictTo.Scope.LIBRARY, RestrictTo.Scope.SUBCLASSES)
    fun onUpdateView(metadata: M)

    @RestrictTo(RestrictTo.Scope.LIBRARY, RestrictTo.Scope.SUBCLASSES)
    fun onDestroyView() {
    }

}

interface DialogTemplateHandler<T : DialogTemplate<*>> {

    val template: T

}