package com.yullg.android.scaffold.internal

import androidx.annotation.RestrictTo
import com.yullg.android.scaffold.core.Constants
import com.yullg.android.scaffold.helper.ISharedPreferences
import com.yullg.android.scaffold.helper.SPHelper

@RestrictTo(RestrictTo.Scope.LIBRARY)
object ScaffoldSPHelper : ISharedPreferences by SPHelper(Constants.SP.NAME_SCAFFOLD)