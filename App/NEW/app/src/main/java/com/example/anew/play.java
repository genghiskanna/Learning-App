package com.example.anew;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import android.widget.ImageButton;
import android.widget.Button;

import java.util.Arrays;
import java.util.Random;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import org.tensorflow.lite.Interpreter;

import java.nio.MappedByteBuffer;

public class play extends AppCompatActivity {
    TextView ans;
    ImageView i6;
    MediaPlayer m2;
    Interpreter soundLite;
    String modelFile="sound.tflite";
    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        String sessionId= getIntent().getStringExtra("EXTRA_SESSION_ID");
        i6 = findViewById(R.id.imageView6);
        ImageButton play = findViewById(R.id.play);
        ImageButton pause = findViewById(R.id.pause);
        ans = (TextView) findViewById(R.id.answer);
        Button startRecording = findViewById(R.id.recordStart);
        Button stopRecording = findViewById(R.id.recordStop);
        ImageView i7 = findViewById(R.id.imageView7);
        final VideoView v1 = findViewById(R.id.videoView);
        MediaController m1 = new MediaController(this);
        m2 = new MediaPlayer();


        m1.setAnchorView(v1);
        Uri uri = null;
//        play.setBackgroundResource(R.drawable.play);
//        pause.setBackgroundResource(R.drawable.pause);
        switch(sessionId) {
            case "ta":
                uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.taaa);
                i7.setBackgroundResource(R.drawable.ta);
                break;
            case "pa":
                uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.paaa);
                i7.setBackgroundResource(R.drawable.pa);
                break;
            case "ma":
                uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.maaa);
                i7.setBackgroundResource(R.drawable.ma);
                break;
            case "ya":
                uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.yaaa);
                i7.setBackgroundResource(R.drawable.ya);
                break;
        }

        v1.setMediaController(m1);
        v1.setVideoURI(uri);
        v1.requestFocus();
        v1.start();

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                v1.start();
            }
        });

        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                v1.pause();
            }
        });

        i6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(play.this, home.class));
                overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
            }
        });

        startRecording.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ans.setText("Recording Started");
                recordVoice();
            }
        });

        stopRecording.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ans.setText("Recording Stopped");
                stopRecord();
                // Conversion of 3GP to WAV
                String exeQuery = Environment.getExternalStorageDirectory().getAbsolutePath() + "/ffmpeg" + " -i "+AudioSavePathInDevice+" "+AudioSavePathInDeviceWav;

//                try {
//                    ans.setText("Converting from 3GP to WAV");
//                    //Runtime.getRuntime().exec(exeQuery);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
                ans.setText("Conversion Successful");
                System.out.println("Converted to WAV successfully");
                getData();
            }
        });

    }

    private MappedByteBuffer loadModelFile(Activity activity,String MODEL_FILE) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(MODEL_FILE);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }



    private float minSize;
    File audiofile;
    Random random ;
    private MediaRecorder recorder = null;
    private void recordVoice(){
        try {
            AudioSavePathInDevice =
                    Environment.getExternalStorageDirectory().getAbsolutePath() + "/AudioRecording.3gp";
            AudioSavePathInDeviceWav = Environment.getExternalStorageDirectory().getAbsolutePath() + "/temp.wav";
            AudioSavePathInDeviceWavTemp = Environment.getExternalStorageDirectory().getAbsolutePath() + "/ta_sample_2.wav";
            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            recorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
            recorder.setOutputFile(AudioSavePathInDevice);
            recorder.prepare();
            recorder.start();
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    private void stopRecord(){
        try {
            recorder.stop();
            recorder.release();
        } catch (Exception e){
            e.printStackTrace();
        }
    }



    String AudioSavePathInDevice = null;
    String AudioSavePathInDeviceWav = null;
    String AudioSavePathInDeviceWavTemp = null;


    private void getData(){
        MyMFCC mfcc = new MyMFCC();
        WavFile wavFile = null;
        try {
            wavFile = WavFile.openWavFile(new File(AudioSavePathInDeviceWav));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (WavFileException e) {
            e.printStackTrace();
        }
        long le = wavFile.getNumFrames();
        double[] buffer = new double[(int)le*2];
        try {
            wavFile.readFrames(buffer,(int)le-1);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (WavFileException e) {
            e.printStackTrace();
        }
        float[] floatArray = new float[buffer.length];
        for (int i = 0 ; i < buffer.length; i++)
        {
            floatArray[i] = (float) buffer[i];
        }
        ans.setText("Extracting MFCC from WAV file");
        float[] mfccs;
        mfccs = mfcc.getMFCC(floatArray,wavFile.getSampleRate(),40);





        // TFLITE
        try {
            ans.setText("sending the MFCC to the TFLite model");
            soundLite=new Interpreter(loadModelFile(this,modelFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
        float [][] out = new float[][]{{0,0,0,0}};
        float [][] in1 = new float[][]{{(float) -19.33045, (float) 1.200156, (float) -2.1030753, (float) 0.19752105, (float) 0.56691295, (float) -0.53265303, (float) -0.3244097, (float) -0.6539764, (float) -0.32931086, (float) 0.23495175, (float) -0.24086043, (float) -0.037566353, (float) 0.36181945, (float) -0.02422667, (float) -0.36981025, (float) -0.33596578, (float) -0.44130838, (float) -0.45967752, (float) -0.39285657, (float) -0.13014504, (float) 0.03685166, (float) -0.026347598, (float) 0.02108531, (float) -0.07937952, (float) -0.006444895, (float) 0.10084602, (float) 0.02320764, (float) -0.108476564, (float) -0.031362865, (float) -0.06586788, (float) -0.021733157, (float) 0.1933272, (float) -0.010134807, (float) 0.04359253, (float) 0.22502007, (float) 0.061905105, (float) 0.009524126, (float) 0.075185165, (float) 0.08022576, (float) -0.007984205,
        }};

        printArray("input",in1[0]);
        soundLite.run(in1,out);

        printArray("out",out[0]);
        ans.setText(Arrays.toString(out[0]));

    }

    public static void printArray(String name, float[] array) {
        System.out.println(name + " [" + array.length + "]");
        for (float f : array) {
            System.out.print(f + ", ");
        }
        System.out.println('\n');
    }



}
