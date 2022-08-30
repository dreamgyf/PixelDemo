package com.dreamgyf.pixeldemo

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import android.widget.SeekBar
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.Integer.min

/**
 * 图片像素化
 */
@OptIn(DelicateCoroutinesApi::class)
class PixelActivity : AppCompatActivity() {

    private val mainHandler = Handler(Looper.getMainLooper())

    private lateinit var iv: ImageView

    private lateinit var seekBar: SeekBar

    private lateinit var pixelateBitmaps: Array<Bitmap?>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pixel)

        iv = findViewById(R.id.iv)
        seekBar = findViewById(R.id.seek_bar)

        //读取图片
        val path = intent.getStringExtra(KEY_PATH)
        val bitmap = BitmapFactory.decodeFile(path)

        //限制最大像素化程度值
        val maxScale = bitmap.width / 8
        //限制最大调节级数
        val maxProgress = min(maxScale, MAX_LEVEL_COUNT)
        //通过倍数计算每一级对应的像素化程度值
        val multiply = maxScale.toFloat() / maxProgress

        //使用数组存储像素化后的Bitmap
        pixelateBitmaps = Array(maxProgress + 1) {
            null
        }
        //0对应着原图
        pixelateBitmaps[0] = bitmap
        //后台线程计算每一级像素化后的Bitmap
        GlobalScope.launch {
            for (scale in 1..maxProgress) {
                //有限计算当前选择级数的像素图片
                val currentScale = seekBar.progress
                if (pixelateBitmaps[currentScale] == null) {
                    pixelateBitmaps[currentScale] =
                        pixelateBitmap(bitmap, (currentScale * multiply).toInt())
                    //通知某一级像素图片计算完成
                    notifyPixelateComplete(currentScale)
                }
                //顺序计算所有级数的像素图片
                if (pixelateBitmaps[scale] == null) {
                    pixelateBitmaps[scale] = pixelateBitmap(bitmap, (scale * multiply).toInt())
                    //通知某一级像素图片计算完成
                    notifyPixelateComplete(scale)
                }
            }
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                //从像素化图片数组缓存中直接获取像素化后的图片
                //若有，则直接设置
                val pixelateBitmap = pixelateBitmaps[progress] ?: return
                iv.setImageBitmap(pixelateBitmap)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {

            }
        })

        seekBar.max = maxProgress
        seekBar.progress = 8
    }

    /**
     * 通知某一级像素图片计算完成
     */
    private fun notifyPixelateComplete(scale: Int) {
        //如果计算好的像素图片的级数刚好为当前选择的级数
        //将其设置为当前显示的图片
        if (seekBar.progress == scale) {
            mainHandler.post {
                iv.setImageBitmap(pixelateBitmaps[scale])
            }
        }
    }

    /**
     * 图片像素化
     * 以 scale*scale 个像素为一组，取每组中间像素的颜色，将此组中所有的像素都设置成这个颜色，以达到像素化效果
     * @return 像素化后的图片
     */
    private fun pixelateBitmap(bitmap: Bitmap, scale: Int): Bitmap {
        val pixelateBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, bitmap.config)
        for (row in 0 until bitmap.width step scale) {
            for (col in 0 until bitmap.height step scale) {
                val rowEnd = min(row + scale, bitmap.width)
                val colEnd = min(col + scale, bitmap.height)
                val pixelateColor =
                    bitmap.getPixel(row + (rowEnd - row) / 2, col + (colEnd - col) / 2)
                for (r in row until rowEnd) {
                    for (c in col until colEnd) {
                        pixelateBitmap.setPixel(r, c, pixelateColor)
                    }
                }
            }
        }
        return pixelateBitmap
    }

    companion object {

        private const val MAX_LEVEL_COUNT = 50

        private const val KEY_PATH = "key_path"

        fun launch(context: Context, path: String) {
            val intent = Intent(context, PixelActivity::class.java)
            intent.putExtra(KEY_PATH, path)
            context.startActivity(intent)
        }
    }
}