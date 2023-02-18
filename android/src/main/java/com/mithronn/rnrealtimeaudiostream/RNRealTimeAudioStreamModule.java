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
                  int amplitude;
                  double frequency;
                  double decibel;
                  WritableMap body;
                  byte[] buffer = new byte[bufferSize];
                  AudioCalculator audioCalculator = new AudioCalculator();

                  while (isRecording) {
                    if(!isPaused) {
                        bytesRead = recorder.read(buffer, 0, buffer.length);

                        // skip first 2 buffers to eliminate "click sound"
                        if (bytesRead > 0 && ++count > 2) {
                          // Create new map
                            body = Arguments.createMap();
                            // amplitude = 0;
                            base64Data = Base64.encodeToString(buffer, Base64.NO_WRAP);
                            audioCalculator = new AudioCalculator(buffer);
                            // // Currently can only calculate mono channel amplitude
                            // if (channelConfig == AudioFormat.CHANNEL_IN_MONO) {
                            //   for (int i = 0; i < buffer.length/2; i++) {
                            //     double y = (buffer[i*2] | buffer[i*2+1] << 8) / 32768.0;
                            //     // depending on your endianness:
                            //     // double y = (audioData[i*2]<<8 | audioData[i*2+1]) / 32768.0
                            //     amplitude += Math.abs(y);
                            //   }
                            //   amplitude = amplitude / buffer.length / 2;
                            // }

                            amplitude = audioCalculator.getAmplitude(buffer);
                            frequency = audioCalculator.getFrequency();
                            decibel = audioCalculator.getDecibel();
                            
                            // Assign base64Data
                            body.putString("data",base64Data);
                            // Assign datas to body
                            body.putInt("amplitude",amplitude);
                            body.putDouble("frequency",frequency);
                            body.putDouble("decibel",decibel);
  
                            // // Assign amplitude decibel_raw_level and decibel_level
                            // if (amplitude == 0) {
                            //   body.putInt("decibel_level", -160);
                            //   body.putInt("decibel_raw_level", 0);
                            // } else {
                            //   body.putInt("decibel_raw_level", amplitude);
                            //   body.putInt("decibel_level", (int) (20 * Math.log(((double) amplitude) / 32767d)));
                            // }
  
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