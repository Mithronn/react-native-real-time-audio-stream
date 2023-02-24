package com.mithronn.rnrealtimeaudiostream;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder.AudioSource;
import android.util.Base64;
import android.util.Log;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;

import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.lang.Math;

// import opus-jni
import net.labymod.opus.OpusCodec;
import net.labymod.opus.OpusCodecOptions;

public class RNRealTimeAudioStreamModule extends ReactContextBaseJavaModule {

    private final ReactApplicationContext reactContext;
    private DeviceEventManagerModule.RCTDeviceEventEmitter eventEmitter;

    private int sampleRateInHz;
    private int channelConfig;
    private int audioFormat;
    private int audioSource;

    private AudioRecord recorder;
    private int bufferSize;
    private boolean isRecording;
    private boolean isPaused;

  public RNRealTimeAudioStreamModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
  }

  @Override
  public String getName() {
    return "RNRealTimeAudioStream";
  }

  @ReactMethod
  public void init(ReadableMap options) {
      sampleRateInHz = 44100;
      if (options.hasKey("sampleRate")) {
          sampleRateInHz = options.getInt("sampleRate");
      }

      channelConfig = AudioFormat.CHANNEL_IN_MONO;
      if (options.hasKey("channels")) {
          if (options.getInt("channels") == 2) {
              channelConfig = AudioFormat.CHANNEL_IN_STEREO;
          }
      }

      audioFormat = AudioFormat.ENCODING_PCM_16BIT;
      if (options.hasKey("bitsPerSample")) {
          if (options.getInt("bitsPerSample") == 8) {
              audioFormat = AudioFormat.ENCODING_PCM_8BIT;
          }
      }

      audioSource = AudioSource.VOICE_RECOGNITION;
      if (options.hasKey("audioSource")) {
          audioSource = options.getInt("audioSource");
      }

      isRecording = false;
      eventEmitter = reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class);

      bufferSize = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);

      if (options.hasKey("bufferSize")) {
          bufferSize = Math.max(bufferSize, options.getInt("bufferSize"));
      }

      int recordingBufferSize = bufferSize * 3;
      recorder = new AudioRecord(audioSource, sampleRateInHz, channelConfig, audioFormat, recordingBufferSize);
  }

  @ReactMethod
  public void start() {
      isRecording = true;
      isPaused = false;
      recorder.startRecording();

      Thread recordingThread = new Thread(new Runnable() {
          public void run() {
              try {
                  int bytesRead;
                  int count = 0;
                  String base64Data;
                  String base64OpusData;
                  int amplitude;
                  double frequency;
                  double decibel;
                  WritableMap body;
                  byte[] buffer = new byte[bufferSize];
                  AudioCalculator audioCalculator = new AudioCalculator();
                  // Opus codec
                  OpusCodec codec = OpusCodec.newBuilder().withSampleRate(sampleRateInHz).withChannels(channelConfig == AudioFormat.CHANNEL_IN_MONO ? 1 : 2).build();

                  while (isRecording) {
                    if(!isPaused) {
                        bytesRead = recorder.read(buffer, 0, buffer.length);

                        // skip first 2 buffers to eliminate "click sound"
                        if (bytesRead > 0 && ++count > 2) {
                            // Create new map
                            body = Arguments.createMap();

                            base64Data = Base64.encodeToString(buffer, Base64.NO_WRAP);
                            // Pass base64-encoded opus encoded data
                            base64OpusData = Base64.encodeToString(codec.encodeFrame(buffer), Base64.NO_WRAP);

                            audioCalculator = new AudioCalculator(buffer);
                            amplitude = audioCalculator.getAmplitude(buffer);
                            frequency = audioCalculator.getFrequency();
                            // decibel = audioCalculator.getDecibel();
                            
                            // Assign base64Data
                            body.putString("raw_data",base64Data);
                            body.putString("opus_data",base64OpusData);
                            // Assign datas to body
                            body.putInt("amplitude",amplitude);
                            body.putDouble("frequency",frequency);
                            body.putDouble("decibel",(double) (20 * Math.log(((double) amplitude) / 32767d)));
  
                            eventEmitter.emit("data", body);
                        }
                    }
                  }
                  recorder.stop();
              } catch (Exception e) {
                  e.printStackTrace();
              }
          }
      });

      recordingThread.start();
  }

  @ReactMethod
  public void stop(Promise promise) {
      isRecording = false;
  }

  @ReactMethod
  public void pause(Promise promise) {
    isPaused = true;
  }

  @ReactMethod
  public void resume(Promise promise) {
    isPaused = false;
  }
}