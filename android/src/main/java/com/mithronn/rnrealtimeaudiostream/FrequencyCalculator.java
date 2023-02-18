package com.mithronn.rnrealtimeaudiostream;

import java.util.Arrays;
import java.lang.Math;

public class FrequencyCalculator {

    private RealDoubleFFT spectrumAmpFFT;
    private double[] spectrumAmpOutCum;
    private double[] spectrumAmpOutTmp;
    private double[] spectrumAmpOut;
    private double[] spectrumAmpOutDB;
    private double[] spectrumAmpIn;
    private double[] spectrumAmpInTmp;
    private double[] wnd;
    private double[][] spectrumAmpOutArray;

    private int fftLen;
    private int spectrumAmpPt;
    private int spectrumAmpOutArrayPt;
    private int nAnalysed;
    private byte[] bytes;

    public double getFreq() {
        if (nAnalysed != 0) {
            int outLen = spectrumAmpOut.length;
            double[] sAOC = spectrumAmpOutCum;
            for (int j = 0; j < outLen; j++) {
                if (sAOC != null) {
                    sAOC[j] /= (double) nAnalysed;
                }
            }
            System.arraycopy(sAOC, 0, spectrumAmpOut, 0, outLen);
            Arrays.fill(sAOC, 0.0);
            nAnalysed = 0;
            for (int i = 0; i < outLen; i++) {
                if (spectrumAmpOutDB != null) {
                    spectrumAmpOutDB[i] = 10.0 * Math.log10(spectrumAmpOut[i]);
                }
            }
        }

        double maxAmpDB = 20 * Math.log10(0.125 / 32768);
        double maxAmpFreq = 0.0;
        for (int i = 1; i < spectrumAmpOutDB.length; i++) {
            if (spectrumAmpOutDB[i] > maxAmpDB) {
                maxAmpDB = spectrumAmpOutDB[i];
                maxAmpFreq = i;
            }
        }
        double sampleRate = 44100;
        maxAmpFreq = maxAmpFreq * sampleRate / fftLen;
        if (sampleRate / fftLen < maxAmpFreq && maxAmpFreq < sampleRate / 2 - sampleRate / fftLen) {
            int id = (int) Math.round((float) maxAmpFreq / sampleRate * fftLen);
            double x1 = spectrumAmpOutDB[id - 1];
            double x2 = spectrumAmpOutDB[id];
            double x3 = spectrumAmpOutDB[id + 1];
            double a = (x3 + x1) / 2 - x2;
            double b = (x3 - x1) / 2;
            if (a < 0) {
                double xPeak = -b / (2 * a);
                if (Math.abs(xPeak) < 1) {
                    maxAmpFreq += xPeak * sampleRate / fftLen;
                }
            }
        }
        return maxAmpFreq;
    }

    public FrequencyCalculator(int fftlen) {
        init(fftlen);
    }

    private void init(int fftlen) {
        fftLen = fftlen;
        spectrumAmpOutCum = new double[fftlen];
        spectrumAmpOutTmp = new double[fftlen];
        spectrumAmpOut = new double[fftlen];
        spectrumAmpOutDB = new double[fftlen];
        spectrumAmpIn = new double[fftlen];
        spectrumAmpInTmp = new double[fftlen];
        spectrumAmpFFT = new RealDoubleFFT(fftlen);
        spectrumAmpOutArray = new double[1][];
        spectrumAmpOutArray[0] = new double[fftlen];

        for (int i = 0; i < spectrumAmpOutArray.length; i++) {
            if (spectrumAmpOutArray[i] != null) {
                for (int j = 0; j < spectrumAmpOutArray[i].length; j++) {
                    spectrumAmpOutArray[i][j] = 0;
                }
            }
        }

        wnd = new double[fftlen];
        for (int i = 0; i < wnd.length; i++) {
            wnd[i] = Math.asin(Math.sin(Math.PI * i / wnd.length)) / Math.PI * 2;
        }
    }

    private short getShortFromBytes(int index) {
        index *= 2;
        short buff = (short) bytes[index + 1];
        short buff2 = (short) bytes[index];

        buff = (short) ((buff & 0xFF) << 8);
        buff2 = (short) (buff2 & 0xFF);

        return (short) (buff | buff2);
    }

    public void feedData(byte[] ds, int dsLen) {
        bytes = ds;
        int dsPt = 0;
        while (dsPt < dsLen) {
            while (spectrumAmpPt < fftLen && dsPt < dsLen) {
                double s = getShortFromBytes(dsPt++) / 32768.0;
                if (spectrumAmpIn != null) {
                    spectrumAmpIn[spectrumAmpPt++] = s;
                }
            }
            if (spectrumAmpPt == fftLen) {
                for (int i = 0; i < fftLen; i++) {
                    if (spectrumAmpInTmp != null && spectrumAmpIn != null && wnd != null) {
                        spectrumAmpInTmp[i] = spectrumAmpIn[i] * wnd[i];
                    }
                }
                if (spectrumAmpFFT != null) {
                    spectrumAmpFFT.ft(spectrumAmpInTmp);
                }
                fftToAmp(spectrumAmpOutTmp, spectrumAmpInTmp);
                if (spectrumAmpOutArray != null && spectrumAmpOutTmp != null) {
                    System.arraycopy(spectrumAmpOutTmp, 0, spectrumAmpOutArray[spectrumAmpOutArrayPt], 0,
                            spectrumAmpOutTmp.length);
                    spectrumAmpOutArrayPt = (spectrumAmpOutArrayPt + 1) % spectrumAmpOutArray.length;
                }
                for (int i = 0; i < fftLen; i++) {
                    if (spectrumAmpOutCum != null && spectrumAmpOutTmp != null) {
                        spectrumAmpOutCum[i] += spectrumAmpOutTmp[i];
                    }
                }
                nAnalysed++;
                int n2 = spectrumAmpIn.length / 2;
                System.arraycopy(spectrumAmpIn, n2, spectrumAmpIn, 0, n2);
                spectrumAmpPt = n2;
            }
        }
    }

    private void fftToAmp(double[] dataOut, double[] data) {
        double scalar = 2.0 * 2.0 / (data.length * data.length);
        dataOut[0] = data[0] * data[0] * scalar / 4.0;
        int j = 1;
        int i = 1;
        while (i < data.length - 1) {
            dataOut[j] = (data[i] * data[i] + data[i + 1] * data[i + 1]) * scalar;
            i += 2;
            j++;
        }
        dataOut[j] = data[data.length - 1] * data[data.length - 1] * scalar / 4.0;
    }
}