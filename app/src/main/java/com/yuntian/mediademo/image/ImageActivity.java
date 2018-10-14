package com.yuntian.mediademo.image;

import android.Manifest;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.ImageView;

import com.mylhyl.acp.Acp;
import com.mylhyl.acp.AcpListener;
import com.mylhyl.acp.AcpOptions;
import com.yuntian.mediademo.R;

import java.io.File;
import java.util.List;


public class ImageActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
    ImageView iv01;
    CustomImageView iv02;
    SurfaceView surfaceView ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        //sdcard/Android/data/com.yuntian.mediademo/files/image/u=2891948847,1197284497&fm=26&gp=0.jpg
        iv01 = findViewById(R.id.iv_01);
        iv02 = findViewById(R.id.iv_02);
        surfaceView = findViewById(R.id.iv_03);
        right();

        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if (holder == null) {
                    return;
                }

                Paint paint = new Paint();
                Bitmap bitmap = BitmapFactory.decodeFile(getExternalFilesDir("image").getPath() + File.separator + "u=2891948847,1197284497&fm=26&gp=0.jpg");
                Canvas canvas = holder.lockCanvas();  // 先锁定当前surfaceView的画布
                canvas.drawBitmap(bitmap, 0, 0, paint); //执行绘制操作
                holder.unlockCanvasAndPost(canvas); // 解除锁定并显示在界面上
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });
    }

    public void right() {
        Acp.getInstance(this).request(new AcpOptions.Builder()
                        .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        /*以下为自定义提示语、按钮文字
                        .setDeniedMessage()
                        .setDeniedCloseBtn()
                        .setDeniedSettingBtn()
                        .setRationalMessage()
                        .setRationalBtn()*/
                        .build(),
                new AcpListener() {
                    @Override
                    public void onGranted() {
                        try {
                            Bitmap bitmap = BitmapFactory.decodeFile(getExternalFilesDir("image").getPath() + File.separator + "u=2891948847,1197284497&fm=26&gp=0.jpg");
                            iv01.setImageBitmap(bitmap);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void onDenied(List<String> permissions) {
                    }
                });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        surfaceView.getHolder().removeCallback(null);
    }
}
