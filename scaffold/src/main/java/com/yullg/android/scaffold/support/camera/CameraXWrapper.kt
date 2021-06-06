package com.yullg.android.scaffold.support.camera

import android.content.Context
import androidx.annotation.UiThread
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import androidx.work.await

class CameraXWrapper(private val context: Context, private val lifecycleOwner: LifecycleOwner) {

    private var cameraSelectorBuilder: CameraSelector.Builder? = null
    private var previewView: PreviewView? = null
    private var previewBuilder: Preview.Builder? = null
    private var imageCaptureBuilder: ImageCapture.Builder? = null
    private var imageAnalysisBuilder: ImageAnalysis.Builder? = null

    private var cameraProvider: ProcessCameraProvider? = null
    private var preview: Preview? = null
    private var imageCapture: ImageCapture? = null
    private var imageAnalysis: ImageAnalysis? = null
    private var camera: Camera? = null

    @UiThread
    fun setCameraSelectorBuilder(cameraSelectorBuilder: CameraSelector.Builder?) {
        this.cameraSelectorBuilder = cameraSelectorBuilder
        bindCameraUseCases()
    }

    @UiThread
    fun enablePreview(previewView: PreviewView, previewBuilder: Preview.Builder) {
        this.previewView = previewView
        this.previewBuilder = previewBuilder
        bindCameraUseCases()
    }

    @UiThread
    fun disablePreview() {
        this.previewView = null
        this.previewBuilder = null
        bindCameraUseCases()
    }

    @UiThread
    fun enableImageCapture(imageCaptureBuilder: ImageCapture.Builder) {
        this.imageCaptureBuilder = imageCaptureBuilder
        bindCameraUseCases()
    }

    @UiThread
    fun disableImageCapture() {
        this.imageCaptureBuilder = null
        bindCameraUseCases()
    }

    @UiThread
    fun enableImageAnalysis(imageAnalysisBuilder: ImageAnalysis.Builder) {
        this.imageAnalysisBuilder = imageAnalysisBuilder
        bindCameraUseCases()
    }

    @UiThread
    fun disableImageAnalysis() {
        this.imageAnalysisBuilder = null
        bindCameraUseCases()
    }

    @UiThread
    suspend fun bind() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        this.cameraProvider = cameraProviderFuture.await()
        bindCameraUseCases()
    }

    @UiThread
    fun unbind() {
        try {
            this.cameraProvider?.unbindAll()
        } finally {
            this.cameraProvider = null
            this.preview = null
            this.imageCapture = null
            this.imageAnalysis = null
            this.camera = null
        }
    }

    fun <T> useCameraProvider(block: (ProcessCameraProvider?) -> T): T {
        return block(cameraProvider)
    }

    fun <T> usePreview(block: (Preview?) -> T): T {
        return block(preview)
    }

    fun <T> useImageCapture(block: (ImageCapture?) -> T): T {
        return block(imageCapture)
    }

    fun <T> useImageAnalysis(block: (ImageAnalysis?) -> T): T {
        return block(imageAnalysis)
    }

    fun <T> useCamera(block: (Camera?) -> T): T {
        return block(camera)
    }

    private fun bindCameraUseCases() {
        val localCameraProvider = cameraProvider ?: return
        val useCaseGroupBuilder = UseCaseGroup.Builder()
        val localPreviewView = previewView
        val localPreviewBuilder = previewBuilder
        if (localPreviewView != null && localPreviewBuilder != null) {
            this.preview = localPreviewBuilder.build().also { p ->
                p.setSurfaceProvider(localPreviewView.surfaceProvider)
                useCaseGroupBuilder.addUseCase(p)
            }
        }
        imageCaptureBuilder?.let { icb ->
            this.imageCapture = icb.build().also { ic ->
                useCaseGroupBuilder.addUseCase(ic)
            }
        }
        imageAnalysisBuilder?.let { iab ->
            this.imageAnalysis = iab.build().also { ia ->
                useCaseGroupBuilder.addUseCase(ia)
            }
        }
        localCameraProvider.unbindAll()
        this.camera = localCameraProvider.bindToLifecycle(
            lifecycleOwner,
            cameraSelectorBuilder?.build() ?: CameraSelector.DEFAULT_BACK_CAMERA,
            useCaseGroupBuilder.build()
        )
    }

}