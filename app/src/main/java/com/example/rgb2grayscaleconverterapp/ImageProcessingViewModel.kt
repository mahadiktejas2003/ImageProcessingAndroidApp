//import android.graphics.Bitmap
// ImageProcessingViewModel.kt
// ImageProcessingViewModel.kt

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.ByteArrayOutputStream
import java.io.IOException

class ImageProcessingViewModel(application: Application) : AndroidViewModel(application) {
    private val _inputImage = MutableLiveData<Bitmap>()
    val inputImage: LiveData<Bitmap> = _inputImage

    private val _outputImage = MutableLiveData<Bitmap>()
    val outputImage: LiveData<Bitmap> = _outputImage

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val client = OkHttpClient()

    fun setInputImage(bitmap: Bitmap) {
        _inputImage.value = bitmap
    }

    // Updated function to accept only Uri
    fun uploadImage(imageUri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>().applicationContext
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
            val requestBody = RequestBody.create("image/png".toMediaTypeOrNull(), byteArrayOutputStream.toByteArray())

            val request = Request.Builder()
                .url("Enter Invoke URL to the API")
                .post(requestBody)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    _error.postValue("Failed to upload image: ${e.message}")
                }

                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        _error.postValue("Unexpected code $response")
                        return
                    }
                    response.body?.byteStream()?.let { stream ->
                        val outputBitmap = BitmapFactory.decodeStream(stream)
                        _outputImage.postValue(outputBitmap)
                    } ?: _error.postValue("Error: No response body")
                }
            })
        }
    }
}





//import android.graphics.BitmapFactory
//import android.util.Base64
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.example.rgb2grayscaleconverterapp.ImageProcessingApi
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.launch
//import okhttp3.MediaType.Companion.toMediaType
//import okhttp3.RequestBody
//import okhttp3.RequestBody.Companion.toRequestBody
//import retrofit2.HttpException
//import retrofit2.Retrofit
//import retrofit2.converter.gson.GsonConverterFactory
//import retrofit2.http.Body
//import retrofit2.http.POST
//import java.io.ByteArrayOutputStream
//import java.io.IOException
//
//class ImageProcessingViewModel : ViewModel() {
//    private val _grayImageBitmap = MutableStateFlow<Bitmap?>(null)
//    val grayImageBitmap: StateFlow<Bitmap?> = _grayImageBitmap
//
//    private val _showError = MutableStateFlow(false)
//    val showError: StateFlow<Boolean> = _showError
//
//    private val retrofit = Retrofit.Builder()
//        .baseUrl("https://3ow99rdgna.execute-api.ap-south-1.amazonaws.com")
//        .addConverterFactory(GsonConverterFactory.create())
//        .build()
//
//    private val api = retrofit.create(ImageProcessingApi::class.java)
//
//    fun convertImageToGrayscale(bitmap: Bitmap) {
//        viewModelScope.launch {
//            var retryCount = 0
//            val maxRetries = 3
//
//            while (retryCount < maxRetries) {
//                try {
//                    val outputStream = ByteArrayOutputStream()
//                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
//                    val imageBytes = outputStream.toByteArray()
//
//                    val response = api.convertToGrayscale(
//                        RequestBody.create("application/json".toMediaType(),
//                            "{\"body\":\"" + Base64.encodeToString(imageBytes, Base64.NO_WRAP) + "\"}"
//                        )
//                    )
//
//                    println("API Response Code: ${response.code()}") // Debug log
//
//                    if (response.isSuccessful) {
//                        val responseBody = response.body()
//                        val base64GrayImage = responseBody?.string()
//                        val grayBitmap = base64ToBitmap(base64GrayImage)
//                        _grayImageBitmap.value = grayBitmap
//                        _showError.value = false // Reset error state
//                        break // Exit the loop if the API call is successful
//                    } else {
//                        // Handle API errors gracefully
//                        handleApiError(response.code())
//                    }
//                } catch (e: HttpException) {
//                    println("HTTP Exception: ${e.message()}")
//                    // Handle HTTP exceptions gracefully
//                    handleRetry(e)
//                } catch (e: IOException) {
//                    println("IO Exception: ${e.message}")
//                    // Handle I/O exceptions gracefully
//                    handleRetry(e)
//                } catch (e: Exception) {
//                    println("Exception: ${e.message}")
//                    // Handle other exceptions gracefully
//                    handleRetry(e)
//                }
//
//                retryCount++
//            }
//
//            // If all retries failed, update UI with an error message
//            if (retryCount >= maxRetries) {
//                println("API request failed after all retries.")
//                _showError.value = true // Update UI with error message
//            }
//        }
//    }
//
//    private fun handleApiError(errorCode: Int) {
//        when (errorCode) {
//            502 -> println("API Error: 502 Bad Gateway")
//            else -> println("API Error: $errorCode")
//        }
//        _showError.value = true
//    }
//
//    private fun handleRetry(exception: Exception) {
//        println("Exception: ${exception.message}")
//        // Handle retry logic
//        _showError.value = true
//    }
//
//    fun retryConversion(bitmap: Bitmap?) {
//        bitmap?.let { convertImageToGrayscale(it) }
//    }
//
//    private fun base64ToBitmap(base64Image: String?): Bitmap? {
//        return try {
//            val decodedString = Base64.decode(base64Image, Base64.NO_WRAP)
//            BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
//        } catch (e: Exception) {
//            println("Exception while decoding base64: ${e.message}")
//            null
//        }
//    }
//}
