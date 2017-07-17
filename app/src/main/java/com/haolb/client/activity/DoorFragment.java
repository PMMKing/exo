package com.haolb.client.activity;

import android.app.AlertDialog;
import android.app.Service;
import android.content.DialogInterface;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.DeviceInteface;
import com.easemob.Constant;
import com.fftpack.RealDoubleFFT;
import com.haolb.client.R;
import com.haolb.client.app.MainApplication;
import com.haolb.client.domain.LockInfoResult;
import com.haolb.client.domain.param.BaseParam;
import com.haolb.client.manager.OpenDoorManager;
import com.haolb.client.net.NetworkParam;
import com.haolb.client.net.Request;
import com.haolb.client.net.ServiceMap;
import com.haolb.client.utils.QArrays;
import com.haolb.client.utils.inject.From;

import java.util.LinkedList;
import java.util.List;

import static com.haolb.client.domain.LockInfoResult.LockInfo;

/**
 * 小区
 *
 * @author Administrator
 */
public class DoorFragment extends BaseFragment {
    private static final long WAIT_TIME = 3000;
    @From(R.id.ll_auth)
    public LinearLayout imageAuth;
    @From(R.id.tv_gps)
    public TextView tvGps;
    @From(R.id.image_open)
    public ImageView imageOpen;
    @From(R.id.checkbox_auto)
    public CheckBox checkBoxAuto;
    private DeviceInteface deviceInteface;
    private RealDoubleFFT transformer;
    private boolean started = false;
    private HandAudio handAudio;
    private int frequence = 44100; // 录制频率，单位hz.这里的值注意了，写的不好，可能实例化AudioRecord对象的时候，会出错。我开始写成11025就不行。这取决于硬件设备
    private int channelConfig = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    private int blockSize = 2048;
    private int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    private boolean isPress =false;
    private AudioRecord audioRecord;
    private boolean isSendOpen = false;
    private RecordAudio recordAudio;
    int bufferSize = AudioRecord.getMinBufferSize(frequence, channelConfig, audioEncoding);
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return onCreateViewWithTitleBar(inflater, container, R.layout.activity_door);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setTitleBar("好乐帮", false);
//        tvGps.setText("经度："+MainApplication.getInstance().lon
//        +"        纬度:"+MainApplication.getInstance().lan);
        tvGps.setVisibility(View.GONE);
        transformer = new RealDoubleFFT(blockSize);
        deviceInteface = new DeviceInteface();
        final AudioManager audio = (AudioManager) getActivity().getSystemService(Service.AUDIO_SERVICE);
        final int maxVolume = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC)*7/10;
        imageOpen.setOnTouchListener(new View.OnTouchListener() {
            public long clickTime;

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    long curClickTime = SystemClock.elapsedRealtime();
                    if( curClickTime - this.clickTime >= 1000L) {
                        this.clickTime = curClickTime;
                        if (QArrays.isEmpty(OpenDoorManager.getAll())) {
                           showTip("你还没有配钥匙，是否申请授权！");
                           return true;
                        }
                        audio.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, 0);
                        isPress = true;
                        if (started) {
                            return false;
                        }
                        started = true;
                        handAudio = new HandAudio();
                        handAudio.addSuccessHandler(new SuccessHandler() {
                            @Override
                            public void onSuccess(final LockInfo lockInfo) {
                                getContext().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        started = false;
                                        if (isPress) {
                                            isSendOpen = true;
                                            sendOpenKey(lockInfo);
                                        }
                                    }
                                });
                            }
                        });
                        new Thread(handAudio).start();
                        new Thread(new RecordAudio()).start();
                    }
                } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    isPress = false;
                    started = false;
                    if (audioRecord != null) {
                        audioRecord.release();
                        audioRecord = null;
                    }
                }
                return false;
            }
        });
    }

    private void sendOpenKey(LockInfo lockInfo) {
        if (lockInfo == null || TextUtils.isEmpty(lockInfo.gatedata)) {
            showTip("靠近锁继续尝试或钥匙不匹配");
            return;
        }
        String text = lockInfo.gatedata;
        text = text.replace("\r\n", "");
        String[] arr = text.split(",");
//        int startEm = 512 ;
        short[] buffer = new short[arr.length];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = arr[i].trim();
            buffer[i] = Short.parseShort((arr[i]));
        }
        showToast("已发出开锁信号！");
        new Thread(new PlayTask(buffer)).start();
        OpenGateParam param = new OpenGateParam();
        param.id = lockInfo.id;//C0A80003
        Request.startRequest(param, ServiceMap.OPENGATE, mHandler);
    }

    private void showTip(String tip) {
        new AlertDialog.Builder(getActivity()).setTitle("提示")
            .setMessage(tip)
            .setNegativeButton(R.string.sure, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                qStartActivity(AuthListAct.class);
            }
        }).setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        })
                .show();
    }


    @Override
    public void onNetEnd(NetworkParam param) {
        if (param.key == ServiceMap.GET_MESSAGE) {
//            lockInfo = null;
        }
        super.onNetEnd(param);
    }


    public class OpenGateParam extends BaseParam {
        public String id;
    }


    class PlayTask implements Runnable {
        private short[] buffer;

        public PlayTask(short[] buffer) {
            this.buffer = buffer;
        }

        @Override
        public void run() {
            try {
                //定义输入流，将音频写入到AudioTrack类中，实现播放
                //实例AudioTrack
                AudioTrack track = new AudioTrack(AudioManager.STREAM_MUSIC, frequence, channelConfig, audioEncoding, buffer.length, AudioTrack.MODE_STREAM);
                //开始播放
                track.play();
                //这里先发送5秒
                long e =  System.currentTimeMillis() + 5 * 1000;
                long now  = System.currentTimeMillis();
                while (now < e) {
                    now = System.currentTimeMillis();
                    track.write(buffer, 0, buffer.length);
                }
                //播放结束
                track.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public synchronized void recordAudio(){
          if(audioRecord==null){
            audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, frequence, channelConfig,  audioEncoding, bufferSize);
            if(audioRecord!=null){
                audioRecord.startRecording();
            }
            while (started) {
                short[] buffer = new short[blockSize];
                // 将record的数据 读到buffer中，但是我认为叫做write可能会比较合适些。
                if(audioRecord!=null){
                    audioRecord.read(buffer, 0, blockSize);
                    Log.v("fuck", String.valueOf(buffer));
                    handAudio.addData(buffer);
                }
            }
         }
        if(audioRecord!=null){
//          audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
        }
    }


    private class RecordAudio implements Runnable {
        @Override
        public void run() {
            recordAudio();
        }
    }

    private class HandAudio implements Runnable {

        private SuccessHandler successHandler;
        private List<short[]> list = new LinkedList<short[]>();
        private long clickTime;
        public void addData(short[] data) {
            synchronized (list) {
                if (data != null) {
                    list.add(data);
                }
                list.notify();
            }
        }

        public void addSuccessHandler(SuccessHandler successHandler) {
            this.successHandler = successHandler;
        }

        @Override
        public void run() {
            while (started) {
                if (successHandler != null) {
                     clickTime = SystemClock.elapsedRealtime();
                    for(int i =0 ;i< Integer.MAX_VALUE ;i++){
                        long curClickTime = SystemClock.elapsedRealtime();
                        if( curClickTime - this.clickTime >= 5000L) {
                            if(started){
                                successHandler.onSuccess(null);
                            }
                            break;
                        }
                        LockInfo lockInfo = unLock();
                        if(lockInfo != null&&started){
                            successHandler.onSuccess(lockInfo);
                            break;
                        }
                    }
                }
            }
        }

        /**
         * 解锁
         * @return
         */
        private LockInfo unLock() {
            short[] bufferResult = null;
            synchronized (list) {
                if (list.isEmpty()) {
                    try {
                        list.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (!list.isEmpty()) {
                    bufferResult = list.remove(0);
                }
            }
            if (bufferResult != null) {
                Log.v("fuck", String.valueOf(bufferResult));
                final double[] toTransform = new double[blockSize];
                final int[] buffer_CH0 = new int[blockSize / 2];
                for (int i = 0; i < bufferResult.length; i++) {
                    toTransform[i] = (double) bufferResult[i] / Short.MAX_VALUE;
                }
                transformer.ft(toTransform);
                for (int i = 0; i < toTransform.length / 2; i++) {
                    toTransform[i] = toTransform[2 * i + 1];
                    buffer_CH0[i] = (int) (toTransform[i] * Short.MAX_VALUE);
                }
                return deviceInteface.check(buffer_CH0);

            }
            return null ;
        }
    }

    interface SuccessHandler {
        void onSuccess(LockInfo keyId);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (((MainActivity2) getActivity()).isConflict) {
            outState.putBoolean("isConflict", true);
        } else if (((MainActivity2) getActivity()).getCurrentAccountRemoved()) {
            outState.putBoolean(Constant.ACCOUNT_REMOVED, true);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (handAudio != null) {
            started = false;
            handAudio.addData(null);
        }
    }
}
