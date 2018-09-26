package com.yuntian.mediademo.audio;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * - @Description:  音频采集
 * - @Author:  yuntian
 * - @Time:  18-9-20 下午10:48
 */
public class AudioRecordManager {

    public static final String TAG = "AudioRecordManager";
    private AudioRecord mRecorder;
    private DataOutputStream dos;
    private Thread recordThread;

    // 采样率 // 44100是目前的标准，但是某些设备仍然支持22050，16000，11025
    // 采样频率一般共分为22.05KHz、44.1KHz、48KHz三个等级
    private final static int AUDIO_SAMPLE_RATE = 16000;


    private boolean isStart = false;
    private boolean isRecording = false;

    private int bufferSize;  //计算的音频缓冲数据大小
    private String path;


    //采样率，注意，目前44100Hz是唯一可以保证兼容所有Android手机的采样率。

    public AudioRecordManager() {

        //采样频率,通道,位宽
        bufferSize = AudioRecord.getMinBufferSize(AUDIO_SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        mRecorder = new AudioRecord(MediaRecorder.AudioSource.MIC, AUDIO_SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,
                bufferSize * 2);

        setRecordPositionUpdateListener();
    }

    public static AudioRecordManager newInstance() {
        AudioRecordManager audioRecordManager = new AudioRecordManager();

        if (audioRecordManager.mRecorder.getState() != AudioRecord.STATE_INITIALIZED) {
            Log.e(TAG, "mRecorder初始化失败，请检查是否开启音频权限");
        }
        return audioRecordManager;
    }


    /**
     * 启动录音写入到某个文件
     *
     * @param path
     */
    public void startRecord(String path) {
        isRecording = true;
        if (mRecorder.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) { //正在录音
            return;
        }

        this.path = path;
        try {
            startThread();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 重新启动录音
     *
     * @param path
     */
    public void reStartRecord(String path) {
        this.path = path;
        try {
            stopRecord();
            startThread();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void startThread() {
        destroyThread();
        isStart = true;
        if (recordThread == null) {
            recordThread = new Thread(recordRunnable);
            recordThread.start();
        }
    }


    private void destroyThread() {
        try {
            isStart = false;
            if (null != recordThread && Thread.State.RUNNABLE == recordThread.getState()) {
                try {
                    Thread.sleep(500);
                    recordThread.interrupt();
                } catch (Exception e) {
                    recordThread = null;
                }
            }
            recordThread = null;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            recordThread = null;
        }
    }


    /**
     * 暂停 Pause
     */
    public void pause() {
        isRecording = false;
    }


    /**
     * 结束录音
     */
    public void stopRecord() {
        try {
            destroyThread();
            if (mRecorder != null) {
                if (mRecorder != null) {
                    mRecorder.release();
                }
                handlerTimer.removeCallbacksAndMessages(null);
                isTimeStart = false;
                currentTime = 0;

                if (isDeletePcm && !TextUtils.isEmpty(path)) {
                    File file = new File(path);
                    if (file.exists()) {
                        file.delete();
                    }
                }

            }
            if (dos != null) {
                dos.flush();
                dos.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    boolean isDeletePcm = true;

    Runnable recordRunnable = new Runnable() {
        @Override
        public void run() {
            try {

                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
                File file = new File(path);
                if (file.exists()) {
                    file.delete();
                }
                file.createNewFile();
                dos = new DataOutputStream(new FileOutputStream(file, true));

                byte[] tempBuffer = new byte[bufferSize]; //音频数据数组

                mRecorder.startRecording();


                //writeToFileHead();
                while (isStart) { //从音频录制缓冲去读取数据到文件
                    if (null != mRecorder && isRecording) {
                        if (isTimeStart) {
                            isTimeStart = false;
                            handlerTimer.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    currentTime = currentTime + 1;
                                    if (timeInterface != null) {
                                        timeInterface.showTime(currentTime);
                                    }
                                    isTimeStart = true;
                                }
                            }, 1000);
                        }

                        int bytesRecord = mRecorder.read(tempBuffer, 0, bufferSize);
                        if (bytesRecord == AudioRecord.ERROR_INVALID_OPERATION || bytesRecord == AudioRecord.ERROR_BAD_VALUE) {
                            continue;
                        }
                        if (bytesRecord == 0 || bytesRecord == -1) {
                            break;
                        }

                        //在此可以对录制音频的数据进行二次处理 比如变声，压缩，降噪，增益等操作
                        //我们这里直接将pcm音频原数据写入文件 这里可以直接发送至服务器 对方采用AudioTrack进行播放原数据
                        dos.write(tempBuffer, 0, bytesRecord);

                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    };


    private Handler handlerTimer = new Handler();


    private long currentTime = 0;

    private boolean isTimeStart = true;


    public void setRecordPositionUpdateListener() {
        mRecorder.setRecordPositionUpdateListener(new AudioRecord.OnRecordPositionUpdateListener() {
            @Override
            public void onMarkerReached(AudioRecord recorder) {

                Log.d("audioRecord", "onMarkerReached:" + recorder.getNotificationMarkerPosition());

            }

            @Override
            public void onPeriodicNotification(AudioRecord recorder) {
                Log.d("audioRecord", "onPeriodicNotification:" + recorder.getPositionNotificationPeriod());

            }
        });
    }


    private TimeInterface timeInterface;


    public void setTimeInterface(TimeInterface timeInterface) {
        this.timeInterface = timeInterface;
    }

    public interface TimeInterface {

        void showTime(long time);
    }


    boolean isSave = true;

    /**
     * 将单个pcm文件转化为wav文件
     */
    public void makePCMFileToWAVFile() {

        if (isRecording) {
            Log.e("AudioRecorder", "正在录制中");
            return;
        }
        File file = new File(path);
        if (!file.exists()) {
            Log.e("AudioRecorder", "文件不存在");
            return;
        }
        if (isSave) {
            isSave = false;
            new Thread(() -> {
                if (PcmToWavUtil.makePCMFileToWAVFile(path, path + ".wav", false)) {
                    Log.e("AudioRecorder", "保存成功");
                    //操作成功
                } else {
                    //操作失败
                    Log.e("AudioRecorder", "makePCMFileToWAVFile fail");
                }
                isSave = true;
            }).start();
        }
    }


}
