package com.yuntian.mediademo.audio;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.PathUtils;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * - @Description:  音频采集
 * - @Author:  yuntian
 * - @Time:  18-9-20 下午10:48
 */
public class AudioRecordManager {

    public static final String TAG = "AudioRecordManager";
    private AudioRecord mRecorder;
    private Thread recordThread;


    // 采样率 : 44100是目前的标准，但是某些设备仍然支持22050，16000，11025 指定采样率 （MediaRecoder 的采样率通常是8000Hz AAC的通常是44100Hz。
    // 采样频率一般共分为22.05KHz、44.1KHz、48KHz三个等级
    public final static int AUDIO_SAMPLE_RATE = 16000;

    private boolean isStartThread = false; //录音线程开启

    private boolean isRecording = false;

    private int bufferSize;  //计算的音频缓冲数据大小


    private DataOutputStream dos;
    private String filePath;

    //计时
    private Handler handlerTimer = new Handler();
    private long currentTime = 0;
    private boolean isTimeStart = true;


    private static final int MAXRECORDTIME = 1 * 60; //30分钟最大录制


    //采样率，注意，目前44100Hz是唯一可以保证兼容所有Android手机的采样率。

    public AudioRecordManager() {
        //采样频率,通道,位宽 ，一帧“音频帧”（Frame）的大小: int size = 采样率x 采样时间 x 通道数 x 位宽
        bufferSize = AudioRecord.getMinBufferSize(AUDIO_SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        //声源,采样频率,通道,位宽
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
     */
    public void startRecord() {
        isRecording = true;
        if (TextUtils.isEmpty(this.filePath)) {
            this.filePath = getPCMRecordFileName();

            try {
                File file = new File(filePath);
                if (file.exists()) {
                    file.delete();
                }
                file.createNewFile();
                dos = new DataOutputStream(new FileOutputStream(file, true));
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.e("AudioRecorder", "有新的录音任务");
        }
        if (mRecorder.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) { //正在录音
            Log.e("AudioRecorder", "正在录音中");
            return;
        }
        try {
            startThread();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void reStartRecord() {
        isRecording = true;
        FileUtils.deleteDir(this.filePath);
        currentTime = 0;
    }


    /**
     * 获取pcm格式文件名字
     *
     * @return
     */
    public String getPCMRecordFileName() {
        return PathUtils.getExternalAppAlarmsPath() + File.separator + getCurrentTimeStr() + ".pcm";
    }

    private static SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");

    public static String getCurrentTimeStr() {
        long starttime = System.currentTimeMillis();
        String datetime = df.format(new Date(starttime));
        return datetime;
    }


    private void startThread() {
        destroyThread();

        isStartThread = true;
        if (recordThread == null) {
            recordThread = new Thread(recordRunnable);
            recordThread.start();
        }
    }


    private void destroyThread() {
        try {
            isStartThread = false;
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

    private boolean isDeletePcm = true; //录制完是否删除pcm文件


    public void setDeletePcm(boolean deletePcm) {
        isDeletePcm = deletePcm;
    }


    public void onDestroy() {
        try {
            destroyThread();
            if (mRecorder != null) {
                mRecorder.release();
            }
            handlerTimer.removeCallbacksAndMessages(null);

            if (isDeletePcm) {
                FileUtils.deleteFile(filePath);
            }

            isRecording = false;
            isTimeStart = false;
            currentTime = 0;
            filePath = "";
            if (dos != null) {
                dos.flush();
                dos.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    Runnable recordRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
                byte[] tempBuffer = new byte[bufferSize]; //音频数据数组
                mRecorder.startRecording();

                //writeToFileHead();
                while (isStartThread) { //从音频录制缓冲去读取数据到文件

                    Log.e("AudioRecorder", "录音线程运行中");

                    if (null != mRecorder && isRecording) {
                        if (isTimeStart) {
                            isTimeStart = false;
                            handlerTimer.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    isTimeStart = true;
                                    if (isRecording) {
                                        currentTime = currentTime + 1;
                                        if (timeInterface != null) {
                                            timeInterface.showTime(currentTime);
                                        }

                                        if (currentTime >= MAXRECORDTIME) {
                                            currentTime = MAXRECORDTIME;
                                            isTimeStart = false;
                                            isRecording = false;
                                            Log.e("AudioRecorder", "超过最大录制时间:" + MAXRECORDTIME + "秒");
                                            //saveRecord();
                                        }
                                    }
                                }
                            }, 1000);
                        }

                        //核心方法，读取音频硬件到缓冲区的音频数据  tempBuffer存放读取的音频数据
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
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    };


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


    private boolean isSave = true;


    public void saveRecord() {
        makePCMFileToWAVFile();
    }


    /**
     * 将单个pcm文件转化为wav文件
     */
    private void makePCMFileToWAVFile() {
        if (TextUtils.isEmpty(filePath)) {
            Log.e("AudioRecorder", "文件不存在");
            return;
        }
        File file = new File(filePath);
        if (!file.exists()) {
            Log.e("AudioRecorder", "文件不存在");
            return;
        }
        if (isSave) {
            isSave = false;
            isRecording = false;

            new Thread(() -> {
                if (PcmToWavUtil.makePCMFileToWAVFile(filePath, getWavFilePath(), true)) {
                    Log.e("AudioRecorder", "保存成功");
                    //操作成功
                    isSave = true;

                    filePath = "";
                    currentTime = 0;

                    handlerTimer.post(new Runnable() {
                        @Override
                        public void run() {
                            if (timeInterface != null) {
                                timeInterface.showTime(currentTime);
                            }
                        }
                    });
                } else {
                    //操作失败
                    Log.e("AudioRecorder", "makePCMFileToWAVFile fail");
                }
            }).start();
        }
    }


    public String getWavFilePath() {
        if (!TextUtils.isEmpty(filePath)) {
            return filePath.replace("pcm", "wav");
        }
        return "";
    }


}
