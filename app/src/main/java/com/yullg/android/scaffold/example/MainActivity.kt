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

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        waitDialog = WaitDialog(supportFragmentManager)
        tipDialog = TipDialog(supportFragmentManager)
        alertDialog = AlertDialog(DefaultCupertinoAlertDialogHandler(supportFragmentManager))
        customDialog = CustomDialog(supportFragmentManager)
        customBottomSheetDialog = CustomBottomSheetDialog(supportFragmentManager)
        setContentView(R.layout.activity_main)
        val textView: TextView = findViewById(R.id.text_view)
        textView.setOnClickListener {
            test()
//            lifecycleScope.launch {
//                LocationSupport.getCurrentLocation(
//                    arrayOf(
//                        LocationManager.GPS_PROVIDER,
//                        LocationManager.NETWORK_PROVIDER
//                    )
//                ).let {
//                    Logger.info("getCurrentLocation:$it")
//                }
//            }
//            testCoroutineJobService()
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
        testAlert()
    }

    private fun testWait() {
        waitDialog.dismiss()
        waitDialog.apply {
            message = null
            progress = null
        }.show()
        handler.postDelayed({
            waitDialog.apply {
                message = "加载中...加载中...加载中...加载中...加载中...加载中...加载中...加载中...加载中..."
                progress = 50
            }.show()
        }, 2000);
        handler.postDelayed({
            waitDialog.apply {
                message = "加载中..."
                progress = 80
            }.show()
        }, 4000);
        handler.postDelayed({
            waitDialog.apply {
                message = "加载中..."
                progress = null
            }.show()
        }, 6000);
        handler.postDelayed({
            waitDialog.apply {
                message = null
                progress = null
            }.show()
        }, 8000);
        handler.postDelayed({
            waitDialog.dismiss()
        }, 10000);
        handler.postDelayed({
            waitDialog.apply {
                message = "第二次加载中..."
                progress = 90
            }.show()
        }, 12000);
        handler.postDelayed({
            waitDialog.dismiss()
            waitDialog.dismiss()
        }, 14000);
    }

    private fun testTip() {
        tipDialog.apply {
            resetMetadata()
            iconResId = TipDialog.ICON_RESOURCE_SUCCESS
            message = "SUCCESS"
            onDismissListener = {
                tipDialog.apply {
                    resetMetadata()
                    iconResId = com.yullg.android.scaffold.R.drawable.yg_dialog_tip_done_black_40dp
                    showDuration = 5000
                    message =
                        "报错了...报错了...报错了...报错了...报错了...报错了...报错了...报错了...报错了...报错了...报错了...报错了...报错了...报错了...报错了...报错了..."
                }.show()
            }
        }.show()
        handler.postDelayed({
            tipDialog.apply {
                resetMetadata()
                iconResId = TipDialog.ICON_RESOURCE_ERROR
                message = "ERROR"
            }.show()
        }, 3000);
        handler.postDelayed({
            tipDialog.apply {
                resetMetadata()
                iconResId = TipDialog.ICON_RESOURCE_WARNING
                message = "WARNING"
            }.show()
        }, 6000);
        handler.postDelayed({
            tipDialog.apply {
                resetMetadata()
                iconResId = TipDialog.ICON_RESOURCE_SUCCESS
                message = "SUCCESS"
            }.show()
        }, 7000);
    }

    private fun testAlert() {
        alertDialog.apply {
            resetMetadata()
            title = "退出登录"
            cancelable = false
            negativeButtonText = "取消"
            neutralButtonText = "2"
            positiveButtonText = "确定"
            positiveButtonClickListener = {
                it.dismiss()
                Log.i("TAG", "testAlert:($it)")
            }
        }.show()
    }

    private fun testCustom() {
        customDialog.apply {
            resetMetadata()
            viewLayoutResId = R.layout.custom_dialog
            viewBinder = { d, v -> Log.i("TAG", "testCustom: ($d ----- $v)") }
        }.show()
    }

    private fun testCustomBottomSheet() {
        customBottomSheetDialog.apply {
            resetMetadata()
            viewLayoutResId = R.layout.custom_dialog
            onShowListener = { Log.i("TAG", "setOnShowListener: $it") }
            onDismissListener = { Log.i("TAG", "setOnDismissListener: $it") }
        }.show()
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