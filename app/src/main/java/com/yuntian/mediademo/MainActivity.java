package com.yuntian.mediademo;

import android.Manifest;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.mylhyl.acp.Acp;
import com.mylhyl.acp.AcpListener;
import com.mylhyl.acp.AcpOptions;
import com.yuntian.mediademo.audio.AudioRecordManager;

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
    private AudioRecordManager audioRecordManager;

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
        tvShowTime = findViewById(R.id.tv_show_time);

        right();

        String path = getCacheDir().getAbsolutePath() + File.separator + getCurrentTimeStr() + "-audioRecord";
        tvAudioRecord.setOnClickListener((v) -> {
            if (audioRecordManager != null) {
                audioRecordManager.startRecord(path);
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
                audioRecordManager.makePCMFileToWAVFile();
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
        audioRecordManager.stopRecord();
    }


}
