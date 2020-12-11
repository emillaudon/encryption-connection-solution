package com.example.myapplication

import android.content.Context
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import io.reactivex.Observable
import io.reactivex.Observable.create
import io.reactivex.internal.operators.observable.ObservablePublish.create
import io.reactivex.internal.operators.observable.ObservableReplay.create
import io.reactivex.rxkotlin.Observables
import org.w3c.dom.Text
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.lang.Exception
import java.net.URI.create
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets.UTF_8
import java.security.NoSuchAlgorithmException
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec


class MainActivity : AppCompatActivity() {
    private val ALGORITHM = "AES"
    private var key: SecretKey? = null

    private var salt = "A8768CC5BEAA6093"

    private val TEMP_IMAGE_TAG = "temp_"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        println(isNetworkConnected())
        key = getKey()

        var textView = findViewById<TextView>(R.id.encryptionText)
        var button = findViewById<Button>(R.id.button)

        var l = encryptBirgit(textView)

        button.setOnClickListener {
            decryptBirgit(l)
            textView.setText("DECRYPTED!!!!")
        }
    }

    private fun getKey(): SecretKey? {
        var secretKey: SecretKey? = null

        try {
            secretKey = SecretKeySpec(salt.toByteArray(), ALGORITHM)
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
        println("key" + secretKey)
        return secretKey
    }

    private fun encryptBirgit(textView: TextView) : ByteArray {
        val birgitImagePathUri  = Uri.parse("file:///android_asset/birgit.png")

        birgitImagePathUri?.let {
            try {
                val fis = assets.open("img/birgit.png")
                    .readBytes()
                val aes = Cipher.getInstance(ALGORITHM)
                println(key)
                aes.init(Cipher.ENCRYPT_MODE, key)
                var l = aes.doFinal(fis)

                textView.setText("Image encrypted")
                return l
            } catch (e: NoSuchAlgorithmException) {
                e.printStackTrace()
            }
        }
        return ByteArray(0)
    }

    private fun decryptBirgit(birgitEncrypted: ByteArray) {
        try {
            val aes = Cipher.getInstance(ALGORITHM)
            aes.init(Cipher.DECRYPT_MODE, key)
            val decrypted = aes.doFinal(birgitEncrypted)
            createImage(decrypted)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
    private fun createImage(image: ByteArray) {
        var img = BitmapFactory.decodeByteArray(image, 0, image.size)
        var imageView = findViewById<ImageView>(R.id.birgit)
        imageView.setImageBitmap(img)
    }


    private fun isNetworkConnected(): String {
        var result = "false"
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager //1
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            val networkCapabilities = connectivityManager.activeNetwork ?: return "false"
            val actNw = connectivityManager.getNetworkCapabilities(networkCapabilities)
                    ?: return "false"
            result = when {
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "wifi"
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "cellular"
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "ethernet"
                else -> "false"
            }
        } else {
            connectivityManager.run {
                connectivityManager.activeNetworkInfo.run {
                    result = when (type) {
                        ConnectivityManager.TYPE_WIFI -> "wifi"
                        ConnectivityManager.TYPE_MOBILE -> "cellular"
                        ConnectivityManager.TYPE_ETHERNET -> "ethernet"
                        else -> "false"
                    }
                }
            }
        }

        return result
    }
}
