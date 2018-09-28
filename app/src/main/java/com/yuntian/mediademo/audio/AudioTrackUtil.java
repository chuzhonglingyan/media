package com.yuntian.mediademo.audio;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.os.Handler;
import android.text.TextUtils;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * https://blog.csdn.net/zyuanyun/article/details/60890534
 */
public class AudioTrackUtil {


    private AudioTrack audioTrack;
    private int sampleRateInHz = AudioRecordManager.AUDIO_SAMPLE_RATE; // 采样率
    private int channelConfiguration = AudioFormat.CHANNEL_OUT_MONO; // 单声道
    private int audioEncoding = AudioFormat.ENCODING_PCM_16BIT; // pcm 16位宽编码
    private int bufferSize;


    private Thread recordThread;

    private String path;
    private boolean isStart;

    private Handler handlerTimer = new Handler();
    private long currentTime = 0;

    private boolean isFinish = false;


    public AudioTrackUtil() {

        bufferSize = AudioTrack.getMinBufferSize(sampleRateInHz, channelConfiguration, audioEncoding);
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRateInHz, channelConfiguration, audioEncoding,
                bufferSize * 2, AudioTrack.MODE_STREAM);
    }


    /**
     * 播放
     *
     * @param path
     */
    public void play(String path) {
        if (TextUtils.isEmpty(path)) {
            return;
        }
        this.path = path;
        if (audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
            return;
        }

        if (isFinish) {
            try {
                File file = new File(path);
                if (!file.exists()) {
                    return;
                }
                dis = new DataInputStream(new FileInputStream(file));
            } catch (Exception e) {
                e.printStackTrace();
            }

            currentTime = 0;
            if (timeInterface != null) {
                timeInterface.showTime(currentTime);
            }
            isFinish = false;
        }
        if (audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PAUSED) {
            audioTrack.play();
            return;
        }

        startThread();
    }


    /**
     * 暂停
     */
    public void pause() {
        if (audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
            audioTrack.pause();
            isFinish = false;
        }
    }


    private DataInputStream dis;
    private boolean isTimeStart = true;


    Runnable recordRunnable = new Runnable() {
        @Override
        public void run() {
            try {


                File file = new File(path);
                if (!file.exists()) {
                    return;
                }
                dis = new DataInputStream(new FileInputStream(file));


                byte[] audiodata = new byte[bufferSize]; //音频数据数组
                audioTrack.play();

                while (isStart) {
                    if (audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {

                        if ((dis.read(audiodata)) != -1) {

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


                            audioTrack.write(audiodata, 0, audiodata.length);
                        } else {
                            audioTrack.pause();
                            isFinish = true;
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };


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
     * 结束播放
     */
    public void onDestory() {
        try {
            destroyThread();
            if (audioTrack != null) {
                audioTrack.release();
                handlerTimer.removeCallbacksAndMessages(null);
                isTimeStart = false;
                currentTime = 0;
            }
            if (dis != null) {
                dis.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private AudioRecordManager.TimeInterface timeInterface;


    public void setTimeInterface(AudioRecordManager.TimeInterface timeInterface) {
        this.timeInterface = timeInterface;
    }


}
