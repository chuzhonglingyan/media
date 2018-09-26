package com.yuntian.mediademo.audio;

import android.media.AudioRecord;
import android.os.Handler;
import android.util.Log;

import java.nio.ByteBuffer;


/**
 * - @Description:  音频采集
 * - @Author:  yuntian
 * - @Time:  18-9-20 下午9:45
 */
public class TestAudioRecord {


    //一帧“音频帧”（Frame）的大小: int size = 采样率 x 位宽 x 采样时间 x 通道数
    //采样时间一般取 2.5ms~120ms 之间，由厂商或者具体的应用决定,每一帧的采样时间取得越短，产生的延时就应该会越小，当然，碎片化的数据也就会越多。

    // audioSource: 该参数指的是音频采集的输入源，可选的值以常量的形式定义在 MediaRecorder.AudioSource 类中，
    // 常用的值包括：DEFAULT（默认），VOICE_RECOGNITION（用于语音识别，等同于DEFAULT），MIC（由手机麦克风输入）
    // ，VOICE_COMMUNICATION（用于VoIP应用）等等。


    //--- sampleRateInHz
    //  采样率，注意，目前44100Hz是唯一可以保证兼容所有Android手机的采样率。


    //channelConfig
    // 通道数的配置，可选的值以常量的形式定义在 AudioFormat 类中，常用的是 CHANNEL_IN_MONO（单通道），CHANNEL_IN_STEREO（双通道）


    //audioFormat
    //这个参数是用来配置“数据位宽”的，可选的值也是以常量的形式定义在 AudioFormat 类中，常用的是 ENCODING_PCM_16BIT（16bit），ENCODING_PCM_8BIT（8bit），注意，前者是可以保证兼容所有Android手机的。


    private AudioRecord audioRecord;


    /**
     * @param audioSource    音频的来源,一一般来源于麦克风
     * @param sampleRateInHz 采样率：音频的采样频率，每秒钟能够采样的次数，采样率越高，音质越高
     * @param channelConfig  声道设置：android支持双声道立体声和单声道。MONO单声道，STEREO立体
     * @param audioFormat    android支持的采样大小16bit 或者8bit ; 编码制式和采样大小：采集来的数据当然使用PCM编码(脉冲代码调制编码，即PCM编码。PCM通过抽样、量化、编码三个步骤将连续变化的模拟信号转换为数字编码。)
     */
    public void initAudioRecord(int audioSource, int sampleRateInHz, int channelConfig, int audioFormat) {

        int minxBufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat); // 采集数据需要的缓冲区的大小

        //AudioRecord会初始化，并和音频缓冲区连接,指定缓冲区大小,采集数据应该小于缓冲区的大小
        audioRecord = new AudioRecord(audioSource, sampleRateInHz, channelConfig, audioFormat, minxBufferSizeInBytes);

    }


    public void startRecord() {
        if (audioRecord.getState() == AudioRecord.RECORDSTATE_RECORDING) {
            return;
        }
        audioRecord.startRecording();
    }

    public void stopRecord() {
        audioRecord.stop();
    }

    /**
     * 用完要释放资源
     */
    public void release() {
        audioRecord.release();
    }

    /**
     * 从音频硬件录制缓冲区读取数据
     *
     * @param audioData     byte[]写入的音频录制数据
     * @param offsetInBytes 目标数组 audioData 的起始偏移量
     * @param sizeInBytes   请求读取的数据大小
     * @return 返回读取数据的状态
     */
    public int read(byte[] audioData, int offsetInBytes, int sizeInBytes) {
        return audioRecord.read(audioData, offsetInBytes, sizeInBytes);
    }

    /**
     * 从音频硬件录制缓冲区读取数据
     *
     * @param audioData     short[]写入的音频录制数据
     * @param offsetInBytes 目标数组 audioData 的起始偏移量
     * @param sizeInShorts  请求读取的数据大小
     * @return 返回读取数据的状态
     */
    public int read(short[] audioData, int offsetInBytes, int sizeInShorts) {
        return audioRecord.read(audioData, offsetInBytes, sizeInShorts);
    }


    /**
     * 从音频硬件录制缓冲区读取数据
     *
     * @param audioBuffer 存储写入音频录制数据的缓冲区
     * @param sizeInBytes 请求的最大字节数。
     * @return
     */
    public int read(ByteBuffer audioBuffer, int sizeInBytes) {
        return audioRecord.read(audioBuffer, sizeInBytes);
    }


    public void setUpdate() {
        audioRecord.setRecordPositionUpdateListener(new AudioRecord.OnRecordPositionUpdateListener() {
            @Override
            public void onMarkerReached(AudioRecord recorder) {

            }

            @Override
            public void onPeriodicNotification(AudioRecord recorder) {

            }
        });
    }


    /**
     * 当之前设置的标志已经成立，或者周期录制位置更新时，设置处理监听者
     *
     * @param handler 用来接收事件通知消息。
     */
    public void setUpdate(Handler handler) {
        audioRecord.setRecordPositionUpdateListener(new AudioRecord.OnRecordPositionUpdateListener() {
            @Override
            public void onMarkerReached(AudioRecord recorder) {

                Log.d("audioRecord", "onMarkerReached:" + recorder.getNotificationMarkerPosition());

            }

            @Override
            public void onPeriodicNotification(AudioRecord recorder) {
                Log.d("audioRecord", "onPeriodicNotification:" + recorder.getPositionNotificationPeriod());

            }
        }, handler);
    }
}
