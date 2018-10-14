package com.yuntian.mediademo.camera;

import android.Manifest;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;

import com.blankj.utilcode.util.LogUtils;
import com.mylhyl.acp.Acp;
import com.mylhyl.acp.AcpListener;
import com.mylhyl.acp.AcpOptions;
import com.yuntian.mediademo.R;

import java.util.List;

/**
 * - @Description:  使用 Camera API 采集视频数据并保存到文件，
 * 分别使用 SurfaceView、TextureView 来预览 Camera 数据，取到 NV21 的数据回调。
 * - @Author:  yuntian
 * - @Time:  18-10-12 下午10:18
 */
public class CameraActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.


    Camera camera; //采集图像视频
    SurfaceView surfaceView; //渲染视图
    TextureView textureView; //渲染视图

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        surfaceView = findViewById(R.id.surfaceView);
        textureView = findViewById(R.id.textureView);
        right();
    }

    public void right() {
        Acp.getInstance(this).request(new AcpOptions.Builder()
                        .setPermissions(Manifest.permission.CAMERA)
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
                        // 打开摄像头并将展示方向旋转90度
                        camera = Camera.open();
                        camera.setDisplayOrientation(90);
                        //Android 中Google支持的 Camera Preview Callback的YUV常用格式有两种：一个是NV21，一个是YV12。
                        // Android一般默认使用YCbCr_420_SP的格式（NV21）。
                        Camera.Parameters parameters = camera.getParameters();
                        parameters.setPreviewFormat(ImageFormat.NV21);
                        camera.setParameters(parameters);
                        camera.setPreviewCallback(new Camera.PreviewCallback() {
                            @Override
                            public void onPreviewFrame(byte[] bytes, Camera camera) {
                                LogUtils.d("采集数据了");
                            }
                        });

                       // initTextureView();
                        initSufaceView();
                    }

                    @Override
                    public void onDenied(List<String> permissions) {
                    }
                });
    }



    public  void  initSufaceView(){
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if (holder == null || camera == null) {
                    return;
                }
                try {
                    camera.setPreviewDisplay(holder);
                    camera.startPreview();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                camera.release();
            }
        });

    }
    public  void  initTextureView(){

     textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
         @Override
         public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
             try {
                 camera.setPreviewTexture(surface);
                 camera.startPreview();
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }

         @Override
         public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

         }

         @Override
         public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
             camera.release();
             return false;
         }

         @Override
         public void onSurfaceTextureUpdated(SurfaceTexture surface) {

         }
     });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        surfaceView.getHolder().removeCallback(null);
    }
}
