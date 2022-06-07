package com.appsol.advancedvoicechangeapp.utils;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Build;
import android.util.Log;

public class AudioPlayThread extends Thread {

    private boolean isRunning;
    public int sr = 44100;

    public short[] audioData;
    public int bufferSizeInBytes;
    AudioTrack audioTrack;

    @Override
    public void run() {
        super.run();
        try {
            isRunning = true;

            audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sr,
                    AudioFormat.CHANNEL_CONFIGURATION_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, bufferSizeInBytes,
                    AudioTrack.MODE_STREAM);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Log.d("MY_AUDIO_DATA", String.valueOf(audioTrack.getBufferSizeInFrames()));
            }
            audioTrack.play();
            {
                audioTrack.write(audioData, 0, bufferSizeInBytes);

            }

        } catch (Exception e) {
            Log.d("TAG_C", "run: " + e.getLocalizedMessage());
        }
    }


    public boolean isRunning() {
        return isRunning;
    }

    public void stopTune() {
        isRunning = false;
        try {
            if (audioTrack != null) {
                audioTrack.flush();
                audioTrack.stop();
                audioTrack.release();
            }
            this.join();
            this.interrupt();

        } catch (InterruptedException | IllegalStateException e) {
            e.printStackTrace();
        }
    }
}
