package com.dreamgyf.pixeldemo

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * 选择图片
 */
class MainActivity : AppCompatActivity() {

    private var path: String? = null

    private val chooseImage =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                val uri = it.data?.data ?: return@registerForActivityResult
                val cursor = contentResolver.query(
                    uri,
                    arrayOf(MediaStore.Images.Media.DATA),
                    null,
                    null,
                    null
                ) ?: return@registerForActivityResult
                cursor.moveToFirst()
                val index = cursor.getColumnIndex(MediaStore.Images.Media.DATA)
                val path = cursor.getString(index)

                val permission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                if (permission == PackageManager.PERMISSION_GRANTED) {
                    PixelActivity.launch(this, path)
                } else {
                    this.path = path
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 2233)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btn_choose_pic).setOnClickListener {
            val intent = Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            )
            chooseImage.launch(intent)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 2233) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                path?.let { PixelActivity.launch(this, it) }
            } else {
                Toast.makeText(this, "请求权限失败，请自行去设置打开权限", Toast.LENGTH_LONG).show()
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }
}