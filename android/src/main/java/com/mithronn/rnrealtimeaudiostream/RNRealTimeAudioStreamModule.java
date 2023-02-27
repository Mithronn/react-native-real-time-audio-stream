package com.mithronn.rnrealtimeaudiostream;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder.AudioSource;
import android.media.audiofx.NoiseSuppressor;
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
    private NoiseSuppressor noiseSuppressor;
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
        sampleRateInHz = 48000;
        if (options.hasKey("sampleRate")) {
            int option_sample_rate = options.getInt("sampleRate");
            if (
                    option_sample_rate == 8000 ||
                            option_sample_rate == 12000 ||
                            option_sample_rate == 16000 ||
                            option_sample_rate == 24000 ||
                            option_sample_rate == 48000
            ) {
                sampleRateInHz = option_sample_rate;
            }
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

        // Microphone audio source tuned for voice communications such as VoIP.
        // It will for instance take advantage of echo cancellation or automatic gain control if available
        audioSource = AudioSource.VOICE_COMMUNICATION;
        if (options.hasKey("audioSource")) {
            int options_audio_source = options.getInt("audioSource");
            if (options_audio_source >= 0 && options_audio_source <= 10) {
                audioSource = options_audio_source;
            }
        }

        isRecording = false;

        eventEmitter = reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class);

        bufferSize = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);

        recorder = new AudioRecord(audioSource, sampleRateInHz, channelConfig, audioFormat, bufferSize);

        // Initialize noise suppresor

        if (NoiseSuppressor.isAvailable()) {
            try {
                noiseSuppressor = NoiseSuppressor.create(recorder.getAudioSessionId());
                if (noiseSuppressor != null) {
                    noiseSuppressor.setEnabled(true);
                }
            } catch (Exception e) {
                eventEmitter.emit("error", e.getMessage());
                e.printStackTrace();
            }
        }
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
                    String base64OpusData;
                    int amplitude;
                    double frequency;
                    WritableMap body;
                    int DEF_FRAME_SIZE = getDefaultFrameSize(sampleRateInHz);
                    AudioCalculator audioCalculator = new AudioCalculator();
                    // Opus codec
                    OpusCodec codec = OpusCodec.newBuilder()
                            .withSampleRate(sampleRateInHz)
                            .withChannels(channelConfig == AudioFormat.CHANNEL_IN_MONO ? 1 : 2)
                            .withFrameSize(DEF_FRAME_SIZE)
                            .build();
                    // byte[] buffer = new byte[bufferSize];
                    // "CHUNK_SIZE = DEF_FRAME_SIZE * CHANNELS * 2" it's formula from opus.h "frame_size*channels*sizeof(opus_int16)"
                    int CHUNK_SIZE = DEF_FRAME_SIZE * (channelConfig == AudioFormat.CHANNEL_IN_MONO ? 1 : 2) * 2;
                    byte[] buffer = new byte[CHUNK_SIZE];

                    while (isRecording) {
                        if (!isPaused) {
                            bytesRead = recorder.read(buffer, 0, buffer.length);

                            // skip first 2 buffers to eliminate "click sound"
                            if (bytesRead > 0 && ++count > 2) {
                                // Create new map
                                body = Arguments.createMap();

                                // Pass base64-encoded opus encoded data
                                base64OpusData = Base64.encodeToString(codec.encodeFrame(buffer), Base64.NO_WRAP);

                                audioCalculator = new AudioCalculator(buffer);
                                amplitude = audioCalculator.getAmplitude(buffer);
                                frequency = audioCalculator.getFrequency();
                                // decibel = audioCalculator.getDecibel();

                                // Assign base64Data
                                body.putString("opus_data", base64OpusData);
                                // Assign datas to body
                                body.putInt("amplitude", amplitude);
                                body.putDouble("frequency", frequency);
                                body.putDouble("decibel", (double) (20 * Math.log(((double) amplitude) / 32767d)));

                                eventEmitter.emit("data", body);
                            }
                        }
                    }

                    if (recorder != null) {
                        recorder.stop();
                        recorder.release();
                        recorder = null;
                    }
                    if (noiseSuppressor != null) {
                        noiseSuppressor.release();
                        noiseSuppressor = null;
                    }
                } catch (Exception e) {
                    if (recorder != null) {
                        recorder.stop();
                        recorder.release();
                        recorder = null;
                    }
                    if (noiseSuppressor != null) {
                        noiseSuppressor.release();
                        noiseSuppressor = null;
                    }
                    eventEmitter.emit("error", e.getMessage());

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

    @ReactMethod
    public void toggleNoiseSuppression(boolean state) {
        if (noiseSuppressor != null) {
            noiseSuppressor.setEnabled(state);
        }
    }

    private int getDefaultFrameSize(int sampleRate) {
        switch (sampleRate) {
            case 8000:
                return 160;
            case 12000:
                return 160;
            case 16000:
                return 320;
            case 24000:
                return 640;
            case 48000:
                return 960;
            default:
                throw new IllegalArgumentException("Unknown sample rate!");
        }
    }
}