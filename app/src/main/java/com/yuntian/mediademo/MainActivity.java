package com.yuntian.mediademo;

import android.Manifest;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.PathUtils;
import com.mylhyl.acp.Acp;
import com.mylhyl.acp.AcpListener;
import com.mylhyl.acp.AcpOptions;
import com.yuntian.mediademo.audio.AudioRecordManager;
import com.yuntian.mediademo.audio.AudioTrackUtil;
import com.yuntian.mediademo.util.FileUtil;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    private TextView tvShowTime;
    private TextView tvPlayTime;
    private AudioRecordManager audioRecordManager;
    private AudioTrackUtil audioTrackUtil;

    private static SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");

    public static String getCurrentTimeStr() {
        long starttime = System.currentTimeMillis();
        String datetime = df.format(new Date(starttime));
        return datetime;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Example of a call to a native method
        TextView tv = findViewById(R.id.sample_text);
        TextView tvAudioRecord = findViewById(R.id.tv_audio_record);
        TextView tvStopAudioRecord = findViewById(R.id.tv_stop_audio_record);
        TextView tv_save = findViewById(R.id.tv_save);


        TextView tvAudioPlay = findViewById(R.id.tv_audio_play);
        TextView tvStopAudioplay = findViewById(R.id.tv_stop_audio_play);
         tvPlayTime = findViewById(R.id.tv_play_time);


        tvShowTime = findViewById(R.id.tv_record_time);

        right();


        tvAudioRecord.setOnClickListener((v) -> {
            if (audioRecordManager != null) {
                audioRecordManager.startRecord();
                Toast.makeText(getApplicationContext(), "录音开始", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "录音还未初始化", Toast.LENGTH_SHORT).show();
            }
        });

        tvStopAudioRecord.setOnClickListener((v) -> {
            if (audioRecordManager != null) {
                audioRecordManager.pause();
            }
        });
        tv_save.setOnClickListener((v) -> {
            if (audioRecordManager != null) {
                audioRecordManager.saveRecord();
            }
        });

        tvAudioPlay.setOnClickListener((v) -> {
            if (audioTrackUtil != null) {
                audioTrackUtil.play(PathUtils.getExternalAppAlarmsPath() + File.separator +"20180928235408.pcm");
            }
        });


        tvStopAudioplay.setOnClickListener((v) -> {
            if (audioRecordManager != null) {
                audioTrackUtil.pause();
            }
        });

        tv.setText(stringFromJNI());
    }


    public void right() {
        Acp.getInstance(this).request(new AcpOptions.Builder()
                        .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE
                                , Manifest.permission.READ_PHONE_STATE
                                , Manifest.permission.RECORD_AUDIO)
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
                        audioRecordManager = AudioRecordManager.newInstance();
                        audioTrackUtil =new AudioTrackUtil();

                        if (audioRecordManager != null) {
                            audioRecordManager.setTimeInterface(new AudioRecordManager.TimeInterface() {
                                @Override
                                public void showTime(long time) {
                                    String hh = new DecimalFormat("00").format(time / 3600);
                                    String mm = new DecimalFormat("00").format(time % 3600 / 60);
                                    String ss = new DecimalFormat("00").format(time % 60);
                                    tvShowTime.setText(hh + ":" + mm + ":" + ss);
                                }
                            });

                            audioTrackUtil.setTimeInterface(new AudioRecordManager.TimeInterface() {
                                @Override
                                public void showTime(long time) {
                                    String hh = new DecimalFormat("00").format(time / 3600);
                                    String mm = new DecimalFormat("00").format(time % 3600 / 60);
                                    String ss = new DecimalFormat("00").format(time % 60);
                                    tvPlayTime.setText(hh + ":" + mm + ":" + ss);
                                }
                            });
                        }
                    }

                    @Override
                    public void onDenied(List<String> permissions) {
                    }
                });
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (audioRecordManager!=null){
            audioRecordManager.onDestroy();

        }
        if (audioTrackUtil!=null){
            audioTrackUtil.onDestory();

        }
    }


}
