package com.example.myapplication.ui.home

import android.Manifest
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.databinding.FragmentHomeBinding
import com.example.myapplication.model.BarCode
import com.example.myapplication.utils.YuvToRgbConverter
import com.google.common.util.concurrent.ListenableFuture
import com.google.firebase.FirebaseApp
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val detector = FirebaseVision.getInstance().visionBarcodeDetector
    private var pauseAnalysis = false
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    lateinit var cameraProvider: ProcessCameraProvider
    private var lensFacing: Int = CameraSelector.LENS_FACING_BACK
    private lateinit var bitmapBuffer: Bitmap
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var camera: androidx.camera.core.Camera

    //private lateinit var previewView: PreviewView
    private val viewModel by lazy { ViewModelProvider(requireActivity()).get(HomeViewModel::class.java) }

    companion object {
        private const val PERMISSIONS_REQUEST_CODE = 10
        private val PERMISSIONS_REQUIRED = arrayOf(Manifest.permission.CAMERA)

        /** Convenience method used to check if all permissions required by this app are granted */
        fun hasPermissions(context: Context) = PERMISSIONS_REQUIRED.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!hasPermissions(requireContext())) {
            // Request camera-related permissions
            requestPermissions(PERMISSIONS_REQUIRED, PERMISSIONS_REQUEST_CODE)
            setupCamera()
        } else {
            setupCamera()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        cameraExecutor = Executors.newSingleThreadExecutor()
        FirebaseApp.initializeApp(requireContext())

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCamera()
    }

    @SuppressLint("UnsafeExperimentalUsageError")
    private fun setupCamera(): Unit {
        cameraProviderFuture =
            ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener(Runnable {
            cameraProvider = cameraProviderFuture.get()
            val preview: Preview = Preview.Builder()
                .build()


            val imageAnalysis = ImageAnalysis.Builder()
                .setTargetResolution(Size(1280, 720))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            val converter = YuvToRgbConverter(requireContext())
            try {

                imageAnalysis.setAnalyzer(cameraExecutor, ImageAnalysis.Analyzer { image ->

                    if (!::bitmapBuffer.isInitialized) {
                        Log.d("CAMERAFRAGMENT", "NOT INITIALIZED")
                        val rotationDegrees = image.imageInfo.rotationDegrees
                        bitmapBuffer = Bitmap.createBitmap(
                            image.width, image.height, Bitmap.Config.ARGB_8888
                        )
                    }

                    if (pauseAnalysis) {
                        image.close()
                        return@Analyzer
                    }
                    image.use {
                        converter.yuvToRgb(image.image!!, bitmapBuffer)

                    }

                    detect(bitmapBuffer)


                })
            } catch (e: Exception) {
                Log.d("CAMERAFRAGMENT", "ERROR: ${e.message}")
            }
            var cameraSelector: CameraSelector = CameraSelector.Builder()
                .requireLensFacing(lensFacing)
                .build()
            cameraProvider.unbindAll()

            camera =
                cameraProvider.bindToLifecycle(
                    this as LifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalysis
                )


            preview.setSurfaceProvider(_binding?.previewView?.createSurfaceProvider())


        }, ContextCompat.getMainExecutor(requireContext()))

    }

    private fun detect(bitmap: Bitmap) {
        val image = FirebaseVisionImage.fromBitmap(bitmap)
        detector.detectInImage(image).addOnSuccessListener {
            for (firebaseBarCode in it) {
                CoroutineScope(Dispatchers.Main).launch {
                    withContext(Dispatchers.IO) {
                        setUpCompletionView(
                            BarCode(
                                firebaseBarCode.valueType,
                                firebaseBarCode.displayValue!!, Date()
                            )
                        )
                    }
                }
            }
            animateView(_binding!!.cameraFrameView)
            Log.d("CAMERAFRAGMENT", "${it.toString()}")
        }.addOnFailureListener {
            Log.d("BARCODE", "ERROR ${it.message}")
        }.addOnCompleteListener {
            //setUpCompletionView(it)
            Log.d("BARCODE", "COMPLETE ${it.isSuccessful}")
        }
    }

    private suspend fun setUpCompletionView(barcode: BarCode) {
        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                viewModel.insertBarCode(barcode)
            }
        }
    }

    private fun animateView(view: View) {
        val scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1.5f)
        val scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.5F)
        val animator: ObjectAnimator = ObjectAnimator.ofPropertyValuesHolder(view, scaleX, scaleY)
        animator.repeatCount = 1
        animator.repeatMode = ObjectAnimator.REVERSE

        animator.start()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (PackageManager.PERMISSION_GRANTED == grantResults.firstOrNull()) {
                // Take the user to the success fragment when permission is granted
                Toast.makeText(context, "Permission request granted", Toast.LENGTH_LONG).show()
                setupCamera()
            } else {
                Toast.makeText(context, "Permission request denied", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        try {
            cameraProvider.unbindAll()
        } catch (e: Exception) {

        }
    }

}