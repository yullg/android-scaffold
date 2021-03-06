package com.yullg.android.scaffold.example

import android.app.Application
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.TextView
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import com.yullg.android.scaffold.framework.BaseActivity
import com.yullg.android.scaffold.framework.BaseViewModel
import com.yullg.android.scaffold.framework.EmptyAC
import com.yullg.android.scaffold.support.camera.CameraXWrapper
import com.yullg.android.scaffold.ui.dialog.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

// 28256, 28257, 282931, 38356, 38357, 383941

class MainActivity : BaseActivity<EmptyAC>() {

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var waitDialog: WaitDialog
    private lateinit var tipDialog: TipDialog
    private lateinit var alertDialog: AlertDialog
    private lateinit var customDialog: CustomDialog
    private lateinit var customBottomSheetDialog: CustomBottomSheetDialog

    override fun newAC(): EmptyAC {
        return EmptyAC
    }

    private lateinit var cameraXWrapper: CameraXWrapper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        waitDialog = WaitDialog(this)
        tipDialog = TipDialog(this)
        alertDialog =
            AlertDialog(DefaultAlertDialogHandler(this, CupertinoAlertDialogTemplate(this)))
        customDialog = CustomDialog(this)
        customBottomSheetDialog = CustomBottomSheetDialog(this)
        setContentView(R.layout.activity_main)
        val textView: TextView = findViewById(R.id.text_view)
        textView.setOnClickListener {
            cameraXWrapper.setCameraSelectorBuilder(
                CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_FRONT)
            )
        }
        cameraXWrapper = CameraXWrapper(this, this)
        cameraXWrapper.enablePreview(findViewById(R.id.preview_view), Preview.Builder())
        GlobalScope.launch(Dispatchers.Main) {
            cameraXWrapper.bind()
        }
//        Logger.error("test", RuntimeException())
//        Logger.error("test", RuntimeException())
//        ScaffoldLogger.error("what")
//        ScaffoldLogger.error("what")
    }

    private fun test() {
    }

    private fun testWait() {
        waitDialog.dismiss()
        waitDialog.setMessage(null).setProgress(null).show()
        handler.postDelayed({
            waitDialog.setMessage("?????????...?????????...?????????...?????????...?????????...?????????...?????????...?????????...?????????...")
                .setProgress(50).show()
        }, 2000);
        handler.postDelayed({
            waitDialog.setMessage("?????????...").setProgress(80).show()
        }, 4000);
        handler.postDelayed({
            waitDialog.setMessage("?????????...").setProgress(null).show()
        }, 6000);
        handler.postDelayed({
            waitDialog.setMessage(null as String?).setProgress(null).show()
        }, 8000);
        handler.postDelayed({
            waitDialog.dismiss()
        }, 10000);
        handler.postDelayed({
            waitDialog.setMessage("??????????????????...").setProgress(90).show()
        }, 12000);
        handler.postDelayed({
            waitDialog.dismiss()
            waitDialog.dismiss()
        }, 14000);
    }

    private fun testTip() {
        tipDialog.resetMetadata().setIconResource(TipDialog.ICON_RESOURCE_SUCCESS)
            .setMessage("SUCCESS")
            .setOnDismissListener {
                tipDialog.resetMetadata().setIconResource(R.drawable.yg_dialog_tip_done_black_40dp)
                    .setShowDuration(5000)
                    .setOnDismissListener(null)
                    .setMessage(null)
                    .setMessage("?????????...?????????...?????????...?????????...?????????...?????????...?????????...?????????...?????????...?????????...?????????...?????????...?????????...?????????...?????????...?????????...")
                    .show()
            }
            .show()
        handler.postDelayed({
            tipDialog.resetMetadata().setIconResource(TipDialog.ICON_RESOURCE_ERROR)
                .setMessage("ERROR").show()
        }, 3000);
        handler.postDelayed({
            tipDialog.resetMetadata().setIconResource(TipDialog.ICON_RESOURCE_WARNING)
                .setMessage("WARNING").show()
        }, 6000);
        handler.postDelayed({
            tipDialog.resetMetadata().setIconResource(TipDialog.ICON_RESOURCE_SUCCESS)
                .setMessage("SUCCESS").show()
        }, 7000);
    }

    private fun testAlert() {
        alertDialog.resetMetadata()
            .setTitle("????????????")
            .setCancelable(false)
            .setMessage("???????????????????????????")
            .setNegativeButtonText("??????", null)
            .setNeutralButtonText("2", null)
            .setPositiveButtonText("??????", null)
            .show()
    }

    private fun testCustom() {
        customDialog.resetMetadata()
            .setViewLayoutResId(R.layout.custom_dialog)
            .setViewBinder { d, v -> Log.i("TAG", "testCustom: ($d ----- $v)") }
            .show()
    }

    private fun testCustomBottomSheet() {
        customBottomSheetDialog.resetMetadata()
            .setViewLayoutResId(R.layout.custom_dialog)
            .setOnShowListener { Log.i("TAG", "setOnShowListener: $it") }
            .setOnDismissListener { Log.i("TAG", "setOnDismissListener: $it") }
            .show()
    }

}

class MyBaseViewModel(application: Application) : BaseViewModel(application)