package com.yuntian.mediademo.media;

import android.Manifest;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Build;
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

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

/**
 * - @Description:  使用 Camera API 采集视频数据并保存到文件，
 * 分别使用 SurfaceView、TextureView 来预览 Camera 数据，取到 NV21 的数据回调。
 * - @Author:  yuntian
 * - @Time:  18-10-12 下午10:18
 */
public class MediaActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.

    private MediaExtractor mMediaExtractor; //音频分离器
    private MediaMuxer mMediaMuxer;//音频合成器


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        right();
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
                        startTask();
                    }

                    @Override
                    public void onDenied(List<String> permissions) {
                    }
                });
    }


    public void startTask() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    process();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * 分离出视频
     */
    private void process() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            mMediaExtractor = new MediaExtractor();
            try {

                ///sdcard/Android/data/com.yuntian.mediademo/files/media/videolib_repo%2F1604%2F28%2FfVobI0704%2FSD%2FfVobI0704-mobile.mp4
                mMediaExtractor.setDataSource(getExternalFilesDir("media").getPath() + File.separator + "SampleVideo_1280x720_2mb.mp4");

                int mVideoTrackIndex = -1;
                int framerate = 0;
                for (int i = 0; i < mMediaExtractor.getTrackCount(); i++) { //得到源文件通道数
                    MediaFormat format = mMediaExtractor.getTrackFormat(i); //获取指定（index）的通道格式
                    String mime = format.getString(MediaFormat.KEY_MIME); //获取文件的类型
                    if (!mime.startsWith("video/")) { //分离出视频
                        continue;
                    }

                    framerate = format.getInteger(MediaFormat.KEY_FRAME_RATE);
                    mMediaExtractor.selectTrack(i);
                    //path:输出文件的名称  format:输出文件的格式；当前只支持MP4格式；
                    mMediaMuxer = new MediaMuxer(getExternalFilesDir("media").getPath() + File.separator + "ouput.mp4", MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
                    //添加通道；我们更多的是使用MediaCodec.getOutpurForma()或Extractor.getTrackFormat(int index)来获取MediaFormat;也可以自己创建；
                    mVideoTrackIndex = mMediaMuxer.addTrack(format);
                    mMediaMuxer.start(); //开始合成文件
                }

                if (mMediaMuxer == null) {
                    return;
                }

                MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
                info.presentationTimeUs = 0;
                ByteBuffer buffer = ByteBuffer.allocate(500 * 1024);
                int sampleSize = 0;

                //把指定通道中的数据按偏移量读取到ByteBuffer中；
                while ((sampleSize = mMediaExtractor.readSampleData(buffer, 0)) > 0) {

                    info.offset = 0;
                    info.size = sampleSize;
                    info.flags = MediaCodec.BUFFER_FLAG_SYNC_FRAME;
                    info.presentationTimeUs += 1000 * 1000 / framerate;


                    mMediaMuxer.writeSampleData(mVideoTrackIndex, buffer, info); //把ByteBuffer中的数据写入到在构造器设置的文件中
                    mMediaExtractor.advance();//读取下一帧数据
                }

                mMediaExtractor.release();

                mMediaMuxer.stop();//停止合成文件
                mMediaMuxer.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
