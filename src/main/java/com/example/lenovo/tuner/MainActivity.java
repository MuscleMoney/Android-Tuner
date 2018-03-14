package com.example.lenovo.tuner;

import android.app.Activity;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.media.AudioRecord;
import android.media.AudioFormat;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;


public class MainActivity extends Activity {
    int recBufSize;
    static final int volumeLevel = 40;//DB
    static final int frequency = 8000;//Hz
    static final int channelConfiguration = AudioFormat.CHANNEL_IN_MONO;
    static final int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    Button btnStart, btnStop, btnExit;
    boolean isTuning = false;
    AudioRecord audioRecord;
    TextView freqTextView, volumeTextView, toneTextView, toneMsgView;
    Handler mHandler = null;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("校音器");
        //recBufSize = AudioRecord.getMinBufferSize(frequency,channelConfiguration,audioEncoding);
        //此处有优化
        recBufSize = 2048;
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, frequency,
                channelConfiguration, audioEncoding, recBufSize);
        btnStart = (Button) this.findViewById(R.id.btnStart);
        btnStart.setOnClickListener(new ClickEvent());
        btnStop = (Button) this.findViewById(R.id.btnStop);
        btnStop.setOnClickListener(new ClickEvent());
        btnExit = (Button) this.findViewById(R.id.btnExit);
        btnExit.setOnClickListener(new ClickEvent());
        freqTextView = (TextView) this.findViewById(R.id.textView1);
        volumeTextView = (TextView) this.findViewById(R.id.textView2);
        toneTextView = (TextView) this.findViewById(R.id.textView3);
        toneMsgView = (TextView) this.findViewById(R.id.textView4);
        final ToneMessage toneMessage = new ToneMessage();
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                String s1 = String.format("%.2f", msg.getData().getDouble("freq"));
                String s2 = String.format("%.2f", msg.getData().getDouble("volume"));
                freqTextView.setText("Freq:" + s1 + "Hz");
                volumeTextView.setText("VolumeLevel:" + s2 + "Db");
                toneMessage.setTone(msg.getData().getDouble("freq"));
                if(msg.getData().getDouble("freq")!=0.0){
                    toneTextView.setText("Tone:" + toneMessage.getToneName());
                    toneMsgView.setText("Message:" + toneMessage.getToneMsg());
                }
                else{
                    toneTextView.setText("Tone:" );
                    toneMsgView.setText("Message:" );
                }

            }
        };

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Process.killProcess(Process.myPid());
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Main Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }

    class ClickEvent implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (v == btnStart) {
                isTuning = true;
                Toast.makeText(MainActivity.this, "Tuning Started", Toast.LENGTH_SHORT).show();
                new RecordPlayThread().start();
            } else if (v == btnStop) {
                Toast.makeText(MainActivity.this, "Tuning Stopped", Toast.LENGTH_SHORT).show();
                isTuning = false;

            } else if (v == btnExit) {
                isTuning = false;
                MainActivity.this.finish();
            }
        }
    }


    class ToneMessage {
        int tone=-1;
        String name="";
        String msg="";

        final double[] toneFreq = {
                65.41, 69.30, 73.42, 77.78, 82.41, 87.31, 92.5, 98.00, 103.8, 110.0, 116.5, 123.5,
                130.8, 138.6, 146.8, 155.6, 164.8, 174.6, 185.0, 196.0, 207.7, 220.0, 233.1, 246.9,
                261.6, 277.2, 293.7, 311.1, 329.6, 349.2, 370.0, 392.0, 415.3, 440.0, 466.2, 493.9,
                523.3, 554.4, 587.3, 622.3, 659.3, 698.5, 740.0, 784.0, 830.6, 880.0, 932.3, 987.8,
                1047, 1109, 1175, 1245, 1319, 1397, 1480, 1568, 1661, 1760, 1865, 1976,
                2093, 2217, 2349, 2489, 2637, 2794, 2960, 3136, 3322, 3520, 3729, 3951
        };
        final String[] toneName = {
                "C2", "C#2", "D2", "D#2", "E2", "F2", "F#2", "G2", "G#2", "A2", "A#2", "B2",
                "C3", "C#3", "D3", "D#3", "E3", "F3", "F#3", "G3", "G#3", "A3", "A#3", "B3",
                "C4", "C#4", "D4", "D#4", "E4", "F4", "F#4", "G4", "G#4", "A4", "A#4", "B4",
                "C5", "C#5", "D5", "D#5", "E5", "F5", "F#5", "G5", "G#5", "A5", "A#5", "B5",
                "C6", "C#6", "D6", "D#6", "E6", "F6", "F#6", "G6", "G#6", "A6", "A#6", "B7",
                "C7", "C#7", "D7", "D#7", "E7", "F7", "F#7", "G7", "G#7", "A7", "A#7", "B7",
        };

        public void setTone(double freq) {
            int i = 0;
            for (i = 0; i < toneFreq.length; i++) {
                if (freq >= toneFreq[i] * 0.95 && freq <= toneFreq[i] * 1.05) {
                    tone = i;
                    name = toneName[i];
                    if (freq > toneFreq[i] * 1.005){
                        String s = String.format("%.2f",(freq - toneFreq[i]));
                        msg = "+" + s;
                    }

                    else if (freq < toneFreq[i] * 0.995){
                        String s = String.format("%.2f",(toneFreq[i] - freq));
                        msg = "-" + s;
                    }
                    else if(tone==-1)
                        msg = "";
                    else
                        msg = "perfect";
                }
            }
        }
        public String getToneName() {
            return name;
        }

        public String getToneMsg() {
            return msg;
        }
    }

    class FlowLineBuffer {
        short[][] flowLineBuffer;
        int life, bufSize;

        public FlowLineBuffer(int BufSize, int Life) {
            flowLineBuffer = new short[Life][BufSize];
            life = Life;
            bufSize = BufSize;
        }

        public void Push(short[] tmp) {
            System.arraycopy(tmp, 0, flowLineBuffer[life - 1], 0, bufSize);
        }

        public void Update() {
            int i;
            for (i = 0; i < life - 1; i++) {
                System.arraycopy(flowLineBuffer[i + 1], 0, flowLineBuffer[i], 0, bufSize);
            }
            flowLineBuffer[life - 1] = new short[bufSize];
        }

        public short[] getBuffer() {
            short[] result;
            result = new short[bufSize * life];
            for (int i = 0; i < life; i++) {
                System.arraycopy(flowLineBuffer[i], 0, result, i * bufSize, bufSize);
            }
            return result;
        }
    }

    class RecordPlayThread extends Thread {
        @Override
        public void run() {

            try {
                short[] buffer = new short[recBufSize];
                //FlowLineBuffer FLB = new FlowLineBuffer(recBufSize,2);
                audioRecord.startRecording();
                while (isTuning) {
                    int buffReadResult = audioRecord.read(buffer, 0, recBufSize);
                    //FLB.Push(buffer);
                    //低分贝噪音隔离
                    long v = 0;
                    for (int i = 0; i < buffer.length; i++) {
                        v += buffer[i] * buffer[i];
                    }
                    double mean = v / (double) buffReadResult;
                    double volume = 10 * Math.log10(mean);
                    //平均音量
                    /*sumvolume+=volume;
                    cnt++;
                    if(cnt==5){
                        String s= String.format("%.2f" , sumvolume/5.0);
                        Log.d("DB", "平均分贝值:" + s);
                        cnt=0;
                        sumvolume=0;
                    }*/
                    //Log.d("DB", "分贝值:" + volume);
                    double freq = FFT.GetFrequency(buffer);
                    //FLB.Update();
                    Bundle b = new Bundle();
                    Message msg = new Message();
                    if (volume >= volumeLevel) {
                        b.putDouble("freq", freq);
                        b.putDouble("volume", volume);
                    } else {
                        b.putDouble("freq", 0.0);
                        b.putDouble("volume", 0.0);
                    }
                    msg.setData(b);
                    msg.setTarget(mHandler);
                    msg.sendToTarget();
                    Log.i("samplerate", "samplerate:" + audioRecord.getSampleRate());
                }
                audioRecord.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
