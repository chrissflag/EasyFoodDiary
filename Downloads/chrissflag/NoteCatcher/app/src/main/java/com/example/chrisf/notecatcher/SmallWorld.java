package com.example.chrisf.notecatcher;

import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.FileReader;
import java.util.ArrayList;

public class SmallWorld extends AppCompatActivity {

    final float noteA=440.0f;
    final float noteB=493.9f;
    final float noteD=293.7f;
    final float noteE=329.6f;
    final float noteE5=659.3f;
    final float noteF=349.2f;
    final float noteG=392.0f;
    final float noteD5=587.3f;
    final float noteC5=523.3f;
    final String[] notesList = {"E4", "F4", "G4", "E5", "C5", "D5", "B4", "D4", "A4", "G4"};
    ArrayList<Float> smallWorldSong;
    private static final int RECORDER_SAMPLERATE = 8000;
    private final int duration = 1;
    private final int numSamples=duration * RECORDER_SAMPLERATE;
    private double sample[]=new double[numSamples];
    private final byte generatedSnd[] = new byte[2*numSamples];


    private AudioTrack audioTrack;
    //boolean canPlay=true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_small_world);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 8000, AudioFormat.CHANNEL_OUT_MONO,AudioFormat.ENCODING_PCM_16BIT, duration*RECORDER_SAMPLERATE,
                AudioTrack.MODE_STREAM);
        setSupportActionBar(toolbar);
        smallWorldSong=new ArrayList<Float>();
        createSmallWorld();

        final boolean readyToPlay = compareNset();

        ImageButton play = (ImageButton) findViewById(R.id.playAnother);
        play.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                if(readyToPlay){
                    playSound();
                    genTone(smallWorldSong);
                }
                else{
                    Toast.makeText(getApplicationContext(), "You haven't collected them all yet, keep working!",
                            Toast.LENGTH_LONG).show();
                }
            }
        });

    }
    public void genTone(ArrayList<Float> pitchList){
        for (float pitch: pitchList){
            for (int i = 0; i < numSamples; ++i) {
                sample[i] = Math.sin(2 * Math.PI * i / (RECORDER_SAMPLERATE/pitch));
            }
            int idx = 0;
            for (double dVal : sample) {
                short val = (short) (dVal * 32767);
                generatedSnd[idx++] = (byte) (val & 0x00ff);
                generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
            }
            audioTrack.write(generatedSnd, 0, numSamples);
        }
    }

    private boolean compareNset(){
        //compare what we caught with what we need
        //check the corresponding boxes
        //and decided if good to go!
        boolean go = true;
        String whatWeHave = readFile();

        for(String note : notesList){
            String noteN = note.replace('#', 's');
            //int id = getResources().getIdentifier("note_"+noteN, "id", this.getPackageName());
            CheckBox box = (CheckBox) findViewById(getResources().getIdentifier("note_"+noteN, "id", this.getPackageName()));
            if(whatWeHave.contains(note)){
                box.setChecked(true);
            }
            else {
                box.setChecked(false);
                go = false;
            }
        }
        return go;
    }

    public String readFile() {
        char buf[] = new char[512];
        FileReader rdr;
        String contents = "";
        try {
            rdr = new FileReader("/"+ Environment.getExternalStorageDirectory().getPath()+"/1.txt");
            int s = rdr.read(buf);
            for (int k = 0; k < s; k++) {
                contents += buf[k];
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return contents;
    }

    void playSound(){
        audioTrack.play();
    }


    public void createSmallWorld(){
        smallWorldSong.add(noteE);
        smallWorldSong.add(noteF);
        smallWorldSong.add(noteG);
        smallWorldSong.add(noteE5);
        smallWorldSong.add(noteC5);
        smallWorldSong.add(noteD5);

        smallWorldSong.add(noteC5);
        smallWorldSong.add(noteC5);
        smallWorldSong.add(noteB);
        smallWorldSong.add(noteB);
        smallWorldSong.add(noteD);
        smallWorldSong.add(noteE);

        smallWorldSong.add(noteF);
        smallWorldSong.add(noteD5);
        smallWorldSong.add(noteB);
        smallWorldSong.add(noteC5);
        smallWorldSong.add(noteB);
        smallWorldSong.add(noteA);

        smallWorldSong.add(noteG);
        smallWorldSong.add(noteG);

    }
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_back:
                Intent i = new Intent(SmallWorld.this, MainActivity.class);
                startActivity(i);
                return true;
            case R.id.action_backTune:
                Intent g = new Intent(SmallWorld.this, ListSong.class);
                startActivity(g);
                return true;
            case R.id.action_backRecord:
                Intent m = new Intent(SmallWorld.this, Record.class);
                startActivity(m);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
