package edu.brandeis.notecatcher;

import edu.brandeis.notecatcher.external.Yin;

import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;


public class MainActivity extends AppCompatActivity {

    private MediaRecorder mRecorder;
    private MediaPlayer mPlayer;
    private String outputFile = null;
    private Button record;
    private Button finish;
    private Button play;
    private Button stop;
    private Button saved;

    //for real-time analysis
    AudioRecord audioRec;
    Thread recThread;
    String pitchList = new String();
    float pitch;

    private static final int RECORDER_SAMPLERATE = 8000;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;

    TextView pitches;

    //for real-time...
    int bufferRec = 1024;
    int bytesPerElement = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Long tsLong = System.currentTimeMillis()/1000;
        String ts = tsLong.toString();
        outputFile = Environment.getExternalStorageDirectory().getAbsolutePath() + "/noteCatcher/"+ts+"Recording.3gpp";
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        mRecorder.setOutputFile(outputFile);

        pitches = (TextView)findViewById(R.id.pitches);
        pitches.setText(String.valueOf(pitch));

        record =(Button) findViewById(R.id.record);
        int bufferSize =AudioRecord.getMinBufferSize(40000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);

        audioRec = new AudioRecord(MediaRecorder.AudioSource.MIC, 40000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);

        record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startRecord(v);



                if (audioRec.getState() == AudioRecord.STATE_INITIALIZED)
                {
                    audioRec.setPositionNotificationPeriod(44100 / 2); // should make sure the buffer is a multiple of this

                    audioRec.setRecordPositionUpdateListener(
                            new AudioRecord.OnRecordPositionUpdateListener()
                            {
                                public void onPeriodicNotification(AudioRecord recorder)
                                {
                                    runOnUiThread(new Runnable()
                                    {
                                        public void run()
                                        {
                                            String lout = String.format("%f Hz", pitch);
                                            pitches.setText(lout);

                                        }
                                    });
                                }
                                public void onMarkerReached(AudioRecord recorder)
                                {
                                    //Log.d(_tag, "onMarkerReached");
                                }
                            }
                    );


                }

                
                /*audioRec = new AudioRecord(MediaRecorder.AudioSource.MIC,
                        RECORDER_SAMPLERATE, RECORDER_CHANNELS,
                        RECORDER_AUDIO_ENCODING, bufferRec*bytesPerElement);
                //audioRec.startRecording();

                if (audioRec.getState() == AudioRecord.STATE_INITIALIZED) {
                    audioRec.setPositionNotificationPeriod(bufferRec);
                    audioRec.setRecordPositionUpdateListener(new AudioRecord.OnRecordPositionUpdateListener() {

                        @Override
                        public void onMarkerReached(AudioRecord recorder) {
                        }

                        @Override
                        public void onPeriodicNotification(AudioRecord recorder) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    pitches.setText(String.valueOf(pitch));
                                }
                            });
                        }
                    });
                    //String res = new String();
                }*/

                Toast.makeText(getApplicationContext(), "Recording..",
                        Toast.LENGTH_SHORT).show();
            }
        });

        finish =(Button) findViewById(R.id.finish);
        finish.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                stopRecord(v);
            }
        });

        play =(Button) findViewById(R.id.play);
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playRecord(v);
            }
        });

        stop =(Button) findViewById(R.id.stop);
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopPlay(v);
            }
        });

        saved =(Button)findViewById(R.id.saved);
        saved.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savedMenu(v);
            }
        });
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        if (audioRec.getState() == AudioRecord.STATE_INITIALIZED)
        {
            audioRec.startRecording();

            Thread lrecorder = new Thread(new Runnable()
            {
                public void run()
                {
                    int lcounter = 0;
                    while (audioRec.getRecordingState() != AudioRecord.RECORDSTATE_STOPPED)
                    {
                        final short[] lshortArray = new short[bufferRec/2];

                        audioRec.read(lshortArray, 0, bufferRec/2);

                        if (lcounter == 0)
                        {
                            Yin lyin = new Yin(audioRec.getSampleRate());
                            double linstantaneousPitch = lyin.getPitch(lshortArray);
                            if (linstantaneousPitch >= 0)
                            {
                                pitch = (float)((2.0 * pitch + linstantaneousPitch) / 3.0);
                            }
                        }

                        lcounter = (lcounter + 1) % 5;
                    }
                }
            });

            lrecorder.start();
        }
    }


   /* @Override
    protected void onResume() {
        super.onResume();
        if (audioRec.getState() == AudioRecord.STATE_INITIALIZED) {
            audioRec.startRecording();
            recThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    //getNotes();
                    int counter = 0;//what for???
                    while (audioRec.getRecordingState() != AudioRecord.RECORDSTATE_STOPPED) {
                        short audioData[] = new short[bufferRec];
                        audioRec.read(audioData, 0, bufferRec);

                        if (counter == 0) {
                            Yin yin = new Yin(audioRec.getSampleRate());
                            double instantaneousPitch = yin.getPitch(audioData);
                            if (instantaneousPitch >= 0)
                                pitch = (float) ((2 * pitch + instantaneousPitch) / 3);
                        }

                        counter = (counter + 1) / 5;
                    }


                    //System.out.println("!!!!!!!!!!"+audioData.toString()+"\n");
                }
            }, "recordingThread");

            recThread.start();
        }
    }*/

    public void startRecord (View v) {
        /*try {
            mRecorder.prepare();
            mRecorder.start();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        record.setEnabled(false);
        finish.setEnabled(true);*/

        audioRec = new AudioRecord(MediaRecorder.AudioSource.MIC,
                RECORDER_SAMPLERATE, RECORDER_CHANNELS,
                RECORDER_AUDIO_ENCODING, bufferRec*bytesPerElement);
        audioRec.startRecording();


        //String res = new String();

        recThread = new Thread(new Runnable() {
            @Override
            public void run() {
                //getNotes();
                int counter = 0;//what for???
                while(audioRec.getRecordingState()!=AudioRecord.RECORDSTATE_STOPPED){
                    short audioData[] = new short[bufferRec];
                    audioRec.read(audioData, 0, bufferRec);

                    if(counter==0){
                        Yin yin = new Yin(audioRec.getSampleRate());
                        double instantaneousPitch = yin.getPitch(audioData);
                        if(instantaneousPitch>=0)
                            pitch = (float)((2*pitch + instantaneousPitch)/3);
                    }

                    counter = (counter+1)/5;
                }


                //System.out.println("!!!!!!!!!!"+audioData.toString()+"\n");
            }
        }, "recordingThread");
        recThread.start();

        Toast.makeText(getApplicationContext(), "Recording..",
                Toast.LENGTH_SHORT).show();
    }

    /*private void getNotes() {
        pitches.setText("recording started!!!");
        short audioData[] = new short[bufferRec];
        audioRec.read(audioData, 0, bufferRec);
        pitches.setText(audioData.toString());
    }*/

    public void stopRecord(View v){
        /*try {
            mRecorder.stop();
            mRecorder.release();
            mRecorder  = null;

            finish.setEnabled(false);
            play.setEnabled(true);


        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (RuntimeException e) {
            e.printStackTrace();
        }*/

        if(audioRec!=null){
            audioRec.stop();
            audioRec.release();
            recThread = null;
        }

        Toast.makeText(getApplicationContext(), "Finishing..",
                Toast.LENGTH_SHORT).show();
    }

    public void playRecord(View view) {
        try{
            mPlayer = new MediaPlayer();
            mPlayer.setDataSource(outputFile);
            mPlayer.prepare();
            mPlayer.start();
            play.setEnabled(false);
            stop.setEnabled(true);

            Toast.makeText(getApplicationContext(), "Playing..",
                    Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void stopPlay(View view) {
        try {
            if (mPlayer != null) {
                mPlayer.stop();
                mPlayer.release();
                mPlayer = null;
                play.setEnabled(true);
                stop.setEnabled(false);
                Toast.makeText(getApplicationContext(), "Stoping..",
                        Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void savedMenu(View view){
        Intent i = new Intent(MainActivity.this, Activity2.class);
        startActivity(i);

    }


}
