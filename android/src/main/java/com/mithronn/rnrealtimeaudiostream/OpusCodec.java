package com.mithronn.rnrealtimeaudiostream;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.NoSuchElementException;


public class OpusCodec {

    static {
        System.loadLibrary("opus_jni");
    }

    private final OpusCodecOptions opusOptions;
    private boolean encoderInitialized = false;
    private boolean decoderInitialized = false;
    private long encoderState;
    private long decoderState;

    private OpusCodec(OpusCodecOptions opusOptions) {
        this.opusOptions = opusOptions;
    }

    public int getFrameSize() {
        return this.opusOptions.getFrameSize();
    }

    public int getSampleRate() {
        return this.opusOptions.getSampleRate();
    }

    public int getChannels() {
        return this.opusOptions.getChannels();
    }

    public int getBitrate() {
        return this.opusOptions.getBitrate();
    }

    public int getMaxFrameSize() {
        return this.opusOptions.getMaxFrameSize();
    }

    public int getMaxPacketSize() {
        return this.opusOptions.getMaxPacketSize();
    }


    public static OpusCodec createDefault() {
        return newBuilder().build();
    }

    public static OpusCodec createByOptions(OpusCodecOptions opusCodecOptions) {
        return new OpusCodec(opusCodecOptions);
    }

    public static Builder newBuilder() {
        return new Builder();
    }


    /**
     * Encodes a chunk of raw PCM data.
     *
     * @param bytes data to encode. Must have a length of CHANNELS * FRAMESIZE * 2.
     * @return encoded data
     * <p>
     * throws {@link IllegalArgumentException} if bytes has an invalid length
     */
    public byte[] encodeFrame(byte[] bytes) {
        return this.encodeFrame(bytes, 0, bytes.length);
    }

    /**
     * Encodes a chunk of raw PCM data.
     *
     * @param bytes data to encode. Must have a length of CHANNELS * FRAMESIZE * 2.
     * @return encoded data
     * <p>
     * throws {@link IllegalArgumentException} if length is invalid
     */
    public byte[] encodeFrame(byte[] bytes, int offset, int length) {
        if (length != this.getChannels() * this.getFrameSize() * 2)
            throw new IllegalArgumentException(String.format("data length must be == CHANNELS * FRAMESIZE * 2 (%d bytes) but is %d bytes", this.getChannels() * this.getFrameSize() * 2, bytes.length));
        this.ensureEncoderExistence();
        return this.encodeFrame(this.encoderState, bytes, offset, length);
    }

    private native byte[] encodeFrame(long encoder, byte[] in, int offset, int length);

    /**
     * Decodes a chunk of opus encoded pcm data.
     *
     * @param bytes data to decode. Length may vary because the less complex the encoded pcm data is, the compressed data size is smaller.
     * @return encoded data.
     */
    public byte[] decodeFrame(byte[] bytes) {
        this.ensureDecoderExistence();
        return this.decodeFrame(this.decoderState, bytes);
    }

    private native byte[] decodeFrame(long decoder, byte[] out);


    private void ensureEncoderExistence() {
        if (this.encoderInitialized) return;
        this.encoderState = this.createEncoder(this.opusOptions);
        this.encoderInitialized = true;
    }

    private native long createEncoder(OpusCodecOptions opts);


    private void ensureDecoderExistence() {
        if (this.decoderInitialized) return;
        this.decoderState = this.createDecoder(this.opusOptions);
        this.decoderInitialized = true;
    }

    private native long createDecoder(OpusCodecOptions opts);

    /**
     * destroys Opus encoder and decoder
     */
    public void destroy() {
        if (this.encoderInitialized) this.destroyEncoder(this.encoderState);
        if (this.decoderInitialized) this.destroyDecoder(this.decoderState);

        this.encoderInitialized = false;
        this.decoderInitialized = false;
    }

    private native void destroyEncoder(long encoder);

    private native void destroyDecoder(long decoder);

    /**
     * Default settings should be good to use for most cases.
     */
    public static class Builder {

        private int frameSize = 960;
        private int sampleRate = 48000;
        private int channels = 1;
        private int bitrate = 64000;
        private int maxFrameSize = 6 * 960;
        private int maxPacketSize = 3 * 1276;

        private Builder() {
        }

        public int getFrameSize() {
            return this.frameSize;
        }

        public Builder withFrameSize(int frameSize) {
            this.frameSize = frameSize;
            return this;
        }

        public int getSampleRate() {
            return this.sampleRate;
        }

        /**
         * @param sampleRate The sample rate to use in the codec instance.
         *                   8, 12, 16, 24 and 48khz are supported.
         * @return this
         */
        public Builder withSampleRate(int sampleRate) {
            this.sampleRate = sampleRate;
            return this;
        }

        public int getChannels() {
            return this.channels;
        }

        public Builder withChannels(int channels) {
            this.channels = channels;
            return this;
        }

        public int getBitrate() {
            return this.bitrate;
        }

        public Builder withBitrate(int bitrate) {
            this.bitrate = bitrate;
            return this;
        }

        public int getMaxFrameSize() {
            return this.maxFrameSize;
        }

        public Builder withMaxFrameSize(int maxFrameSize) {
            this.maxFrameSize = maxFrameSize;
            return this;
        }

        public int getMaxPacketSize() {
            return this.maxPacketSize;
        }

        public Builder withMaxPacketSize(int maxPacketSize) {
            this.maxPacketSize = maxPacketSize;
            return this;
        }

        public OpusCodec build() {
            return new OpusCodec(OpusCodecOptions.of(this.frameSize, this.sampleRate, this.channels, this.bitrate, this.maxFrameSize, this.maxPacketSize));
        }
    }
}