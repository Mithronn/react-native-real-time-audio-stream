package com.mithronn.rnrealtimeaudiostream;

import java.lang.Math;

public class AudioCalculator {

    private byte[] bytes;
    private int[] amplitudes;
    private double[] decibels;
    private double frequency;
    private int amplitude;
    private double decibel;

    private int amplitudeLevels(byte[] bytes) {
        int[] amplitudes = getAmplitudes(bytes);
        int major = 0;
        int minor = 0;
        for (int i : amplitudes) {
            if (i > major)
                major = i;
            if (i < minor)
                minor = i;
        }
        return Math.max(major, minor * -1);
    }

    public AudioCalculator() {
        this.bytes = null;
        this.amplitudes = null;
        this.decibels = null;
        this.frequency = 0.0;
        this.amplitude = 0;
        this.decibel = 0.0;
    }

    public AudioCalculator(byte[] bytes) {
        this.bytes = bytes;
        this.amplitudes = null;
        this.decibels = null;
        this.frequency = 0.0;
        this.amplitude = 0;
        this.decibel = 0.0;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
        amplitudes = null;
        decibels = null;
        frequency = 0.0;
        amplitude = 0;
        decibel = 0.0;
    }

    public int[] getAmplitudes(byte[] bytes) {
        return getAmplitudesFromBytes(bytes);
    }

    public double[] getDecibels() {
        if (amplitudes == null) {
            amplitudes = getAmplitudesFromBytes(bytes);
        }
        if (decibels == null) {
            decibels = new double[amplitudes.length];
            for (int i = 0; i < amplitudes.length; i++) {
                decibels[i] = resizeNumber(getRealDecibel(amplitudes[i]));
            }
        }
        return decibels;
    }

    public int getAmplitude(byte[] bytes) {
        return amplitudeLevels(bytes);
    }

    public double getDecibel() {
        if (decibel == 0.0)
            decibel = resizeNumber(getRealDecibel(amplitude));
        return decibel;
    }

    public double getFrequency() {
        if (frequency == 0.0)
            frequency = retrieveFrequency();
        return frequency;
    }

    private double retrieveFrequency() {
        int length = bytes.length / 2;
        int sampleSize = 8192;
        while (sampleSize > length)
            sampleSize = sampleSize >> 1;

        FrequencyCalculator frequencyCalculator = new FrequencyCalculator(sampleSize);
        frequencyCalculator.feedData(bytes, length);

        return resizeNumber(frequencyCalculator.getFreq());
    }

    private double getRealDecibel(int amplitude) {
        if (amplitude < 0)
            amplitude *= -1;
        double amp = (double) amplitude / 32767.0 * 100.0;
        if (amp == 0.0) {
            amp = 1.0;
        }
        double decibel = Math.sqrt(100.0 / amp);
        decibel *= decibel;
        if (decibel > 100.0) {
            decibel = 100.0;
        }
        return (-1.0 * decibel + 1.0) / Math.PI;
    }

    public double resizeNumber(double value) {
        int temp = (int) (value * 10.0);
        return temp / 10.0;
    }

    private int[] getAmplitudesFromBytes(byte[] bytes) {
        int[] amps = new int[bytes.length / 2];
        int i = 0;
        while (i < bytes.length) {
            short buff = (short) bytes[i + 1];
            short buff2 = (short) bytes[i];

            buff = (short) ((buff & 0xFF) << 8);
            buff2 = (short) (buff2 & 0xFF);

            int res = buff | buff2;
            amps[i == 0 ? 0 : i / 2] = res;
            i += 2;
        }
        return amps;
    }
}