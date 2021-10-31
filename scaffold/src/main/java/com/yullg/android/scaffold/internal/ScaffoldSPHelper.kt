package com.yullg.android.scaffold.internal

import androidx.annotation.RestrictTo
import com.yullg.android.scaffold.app.ScaffoldConstants
import com.yullg.android.scaffold.helper.ISharedPreferences
import com.yullg.android.scaffold.helper.SPHelper

@RestrictTo(RestrictTo.Scope.LIBRARY)
object ScaffoldSPHelper : ISharedPreferences by SPHelper(ScaffoldConstants.SP.NAME_SCAFFOLD)