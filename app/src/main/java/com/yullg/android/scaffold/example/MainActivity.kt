package com.yullg.android.scaffold.example

import android.annotation.SuppressLint
import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.content.ComponentName
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.TextView
import androidx.camera.core.Preview
import androidx.core.content.ContextCompat
import com.yullg.android.scaffold.framework.BaseActivity
import com.yullg.android.scaffold.framework.EmptyAC
import com.yullg.android.scaffold.helper.DateHelper
import com.yullg.android.scaffold.support.logger.Logger
import com.yullg.android.scaffold.support.media.CameraXWrapper
import com.yullg.android.scaffold.support.schedule.CoroutineJobService
import com.yullg.android.scaffold.ui.dialog.*
import kotlinx.coroutines.*
import kotlin.coroutines.coroutineContext

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
            testCoroutineJobService()
//            cameraXWrapper.setCameraSelectorBuilder(
//                CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_FRONT)
//            )
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
            waitDialog.setMessage("加载中...加载中...加载中...加载中...加载中...加载中...加载中...加载中...加载中...")
                .setProgress(50).show()
        }, 2000);
        handler.postDelayed({
            waitDialog.setMessage("加载中...").setProgress(80).show()
        }, 4000);
        handler.postDelayed({
            waitDialog.setMessage("加载中...").setProgress(null).show()
        }, 6000);
        handler.postDelayed({
            waitDialog.setMessage(null as String?).setProgress(null).show()
        }, 8000);
        handler.postDelayed({
            waitDialog.dismiss()
        }, 10000);
        handler.postDelayed({
            waitDialog.setMessage("第二次加载中...").setProgress(90).show()
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
                    .setMessage("报错了...报错了...报错了...报错了...报错了...报错了...报错了...报错了...报错了...报错了...报错了...报错了...报错了...报错了...报错了...报错了...")
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
            .setTitle("退出登录")
            .setCancelable(false)
            .setMessage("您确定退出登录吗？")
            .setNegativeButtonText("取消", null)
            .setNeutralButtonText("2", null)
            .setPositiveButtonText("确定", null)
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

    private fun testCoroutineJobService() {
        val jobScheduler = ContextCompat.getSystemService(this, JobScheduler::class.java)
        val jobInfo = JobInfo.Builder(
            (System.currentTimeMillis() / 1000).toInt(),
            ComponentName(this, DeviceReConnectJobScheduler::class.java)
        ).setOverrideDeadline(0)
            .build()
        jobScheduler!!.schedule(jobInfo)
    }

}

@SuppressLint("SpecifyJobSchedulerIdRange")
class DeviceReConnectJobScheduler : CoroutineJobService() {

    override suspend fun doWork(params: JobParameters) {
        Logger.info("[CoroutineJobService] doWork begin: ${coroutineContext.job.hashCode()}:${Thread.currentThread().name}")
        delay(DateHelper.MILLIS_PER_SECOND * 3)
        Logger.info("[CoroutineJobService] doWork end: ${coroutineContext.job.hashCode()}")
        jobFinished(params, false)
    }

}