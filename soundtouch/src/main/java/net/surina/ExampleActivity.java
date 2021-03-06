/////////////////////////////////////////////////////////////////////////////
///
/// Example Android Application/Activity that allows processing WAV 
/// audio files with SoundTouch library
///
/// Copyright (c) Olli Parviainen
///
////////////////////////////////////////////////////////////////////////////////
//
// $Id: SoundTouch.java 210 2015-05-14 20:03:56Z oparviai $
//
////////////////////////////////////////////////////////////////////////////////
package net.surina;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import net.surina.soundtouch.OnProgressChangedListener;
import net.surina.soundtouch.SoundStreamAudioPlayer;
import net.surina.soundtouch.SoundStreamFileWriter;
import net.surina.soundtouchexample.R;

import java.io.IOException;


public class ExampleActivity extends Activity implements OnClickListener {

    private static final String TAG = ExampleActivity.class.getSimpleName();

    TextView textViewConsole = null;
    EditText editSourceFile = null;
    EditText editOutputFile = null;
    EditText editTempo = null;
    EditText editPitch = null;
    CheckBox checkBoxPlay = null;
    Button playBtn;

    StringBuilder consoleText = new StringBuilder();
    String kept;
    SoundStreamAudioPlayer player = null;

    /// Called when the activity is created
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_example);

        textViewConsole = (TextView) findViewById(R.id.textViewResult);
        editSourceFile = (EditText) findViewById(R.id.editTextSrcFileName);
        editOutputFile = (EditText) findViewById(R.id.editTextOutFileName);

        String mBaseFolderPath = getExternalFilesDir(null).getAbsolutePath();
        kept = mBaseFolderPath.substring(0, mBaseFolderPath.indexOf("/Android"));

        editSourceFile.setText(kept + "/file.wav");
        editOutputFile.setText(kept + "/file_out.wav");
        editTempo = (EditText) findViewById(R.id.editTextTempo);
        editPitch = (EditText) findViewById(R.id.editTextPitch);

        playBtn = (Button) findViewById(R.id.playBtn);
        playBtn.setOnClickListener(this);

        Button buttonFileSrc = (Button) findViewById(R.id.buttonSelectSrcFile);
        Button buttonFileOutput = (Button) findViewById(R.id.buttonSelectOutFile);
        Button buttonProcess = (Button) findViewById(R.id.buttonProcess);
        buttonFileSrc.setOnClickListener(this);
        buttonFileOutput.setOnClickListener(this);
        buttonProcess.setOnClickListener(this);

        checkBoxPlay = (CheckBox) findViewById(R.id.checkBoxPlay);

        // Check soundtouch library presence & version
        checkLibVersion();
    }


    /// Function to append status text onto "console box" on the Activity
    public void appendToConsole(final String text) {
        // run on UI thread to avoid conflicts
        runOnUiThread(new Runnable() {
            public void run() {
                consoleText.append(text);
                consoleText.append("\n");
                textViewConsole.setText(consoleText);
            }
        });
    }


    /// print SoundTouch native library version onto console
    protected void checkLibVersion() {
//        String ver = SoundTouch();
//        appendToConsole("SoundTouch native library version = " + ver);
    }


    /// Button click handler
    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View arg0) {
        switch (arg0.getId()) {
//            case R.id.buttonSelectSrcFile:
//            case R.id.buttonSelectOutFile:
                // one of the file select buttons clicked ... we've not just implemented them ;-)
//                Toast.makeText(this, "File selector not implemented, sorry! Enter the file path manually ;-)", Toast.LENGTH_LONG).show();
//                break;

//            case R.id.buttonProcess:
                // button "process" pushed
//                try {
//                    process();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }

//                break;
//            case R.id.playBtn:
//                if (new File(kept + "/file.wav").exists())
//                    playWavFile(kept + "/file.wav");
//                try {
//                    mp3ToWav(new File(kept + "/my_file.mp3"));
//                } catch (UnsupportedAudioFileException | IOException e) {
//                    Log.d("TAG_ERROR", "onClick: " + e.getMessage());
//                    e.printStackTrace();
//                }
//                if (player != null && !player.isPaused())
//                    player.stop();
//                else
//                    playSound();
//                break;
        }

    }


    private void playSound() {

        try {
            float tempo = 0.01f * Float.parseFloat(editTempo.getText().toString());
            float pitch = Float.parseFloat(editPitch.getText().toString());
            player = new SoundStreamAudioPlayer(0, editSourceFile.getText().toString(), tempo, pitch);
        } catch (IOException e) {
            e.printStackTrace();
        }
        new Thread(player).start();
        if (player != null) {
            player.start();
        }
    }

/// Play audio file
//    protected void playWavFile(String fileName) {
//        File file2play = new File(fileName);
//        Intent i = new Intent();
//        i.setAction(android.content.Intent.ACTION_VIEW);
//        i.setDataAndType(Uri.fromFile(file2play), "audio/wav");
//        startActivity(i);
//    }


    /// Helper class that will execute the SoundTouch processing. As the processing may take
/// some time, run it in background thread to avoid hanging of the UI.
//    protected class ProcessTask extends AsyncTask<ProcessTask.Parameters, Integer, Long> {
//        /// Helper class to store the SoundTouch file processing parameters
//        public final class Parameters {
//            String inFileName;
//            String outFileName;
//            float tempo;
//            float pitch;
//        }
//
//
//        /// Function that does the SoundTouch processing
//        public final long doSoundTouchProcessing(Parameters params) {
////            SoundTouch st = new SoundTouch();
////            st.setTempo(params.tempo);
////            st.setPitchSemiTones(params.pitch);
////            Log.i("SoundTouch", "process file " + params.inFileName);
////            long startTime = System.currentTimeMillis();
////            int res = st.processFile(params.inFileName, params.outFileName);
////            long endTime = System.currentTimeMillis();
////            float duration = (endTime - startTime) * 0.001f;
////
////            Log.i("SoundTouch", "process file done, duration = " + duration);
////            appendToConsole("Processing done, duration " + duration + " sec.");
////            if (res != 0) {
////                String err = SoundTouch.getErrorString();
////                appendToConsole("Failure: " + err);
////                return -1L;
////            }
////
////            // Play file if so is desirable
////            if (checkBoxPlay.isChecked()) {
////                playWavFile(params.outFileName);
////            }
////            return 0L;
////        }
////
////
////        /// Overloaded function that get called by the system to perform the background processing
////        @Override
////        protected Long doInBackground(Parameters... aparams) {
////            return doSoundTouchProcessing(aparams[0]);
////        }
//
//    }
//
//
//    /// process a file with SoundTouch. Do the processing using a background processing
//    /// task to avoid hanging of the UI
    protected void process() throws IOException {
//        params.inFileName = editSourceFile.getText().toString();
//        params.outFileName = editOutputFile.getText().toString();
//        params.tempo = 0.01f * Float.parseFloat(editTempo.getText().toString());
//        params.pitch = Float.parseFloat(editPitch.getText().toString());
//
        SoundStreamFileWriter writer = new SoundStreamFileWriter(0, editSourceFile.getText().toString(), editOutputFile.getText().toString(), 0.01f * Float.parseFloat(editTempo.getText().toString()),
                Float.parseFloat(editPitch.getText().toString()));
        new Thread(writer).start();
        writer.setOnProgressChangedListener(new OnProgressChangedListener() {
            @Override
            public void onProgressChanged(int track, double currentPercentage, long position) {
                Log.d("TAG_SOUND", "onProgressChanged: " + currentPercentage);
            }

            @Override
            public void onTrackEnd(int track) {

            }

            @Override
            public void onExceptionThrown(String string) {

            }
        });
        writer.start();
//        try {
//            ProcessTask task = new ProcessTask();
//            ProcessTask.Parameters params = task.new Parameters();
//            // parse processing parameters
//            params.inFileName = editSourceFile.getText().toString();
//            params.outFileName = editOutputFile.getText().toString();
//            params.tempo = 0.01f * Float.parseFloat(editTempo.getText().toString());
//            params.pitch = Float.parseFloat(editPitch.getText().toString());
//
//            // update UI about status
//            appendToConsole("Process audio file :" + params.inFileName + " => " + params.outFileName);
//            appendToConsole("Tempo = " + params.tempo);
//            appendToConsole("Pitch adjust = " + params.pitch);
//
//            Toast.makeText(this, "Starting to process file " + params.inFileName + "...", Toast.LENGTH_SHORT).show();
//
//            // start SoundTouch processing in a background thread
//            task.execute(params);
////			task.doSoundTouchProcessing(params);	// this would run processing in main thread
//
//        } catch (Exception exp) {
//            exp.printStackTrace();
//        }
    }

//
//    public void mp3ToWav(File mp3Data) throws UnsupportedAudioFileException, IOException {
//        // open stream
//
//        final InputStream in = new FileInputStream(mp3Data);
//        Log.d("TAG_ERROR", "mp3ToWav: " + mp3Data.getAbsolutePath());
//        AudioInputStream mp3Stream = AudioSystem.getAudioInputStream(new BufferedInputStream(new FileInputStream(mp3Data)));
//        AudioFormat sourceFormat = mp3Stream.getFormat();
//        // create audio format object for the desired stream/audio format
//        // this is *not* the same as the file format (wav)
//        AudioFormat convertFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
//                sourceFormat.getSampleRate(), 16,
//                sourceFormat.getChannels(),
//                sourceFormat.getChannels() * 2,
//                sourceFormat.getSampleRate(),
//                false);
//        // create stream that delivers the desired format
//        AudioInputStream converted = AudioSystem.getAudioInputStream(convertFormat, mp3Stream);
//        // write stream into a file with file format wav
//        AudioSystem.write(converted, AudioFileFormat.Type.WAVE, new File(kept + "/out.wav"));
//    }
}
