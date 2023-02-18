package com.mithronn.rnrealtimeaudiostream;

import java.lang.Math;

public class RealDoubleFFT {
    private int ndim;
    private double normFactor;
    private double[] wavetable;
    private double[] ch;
    private static final int[] ntryh = { 4, 2, 3, 5 };

    public RealDoubleFFT(int ndim) {
        this.ndim = ndim;
        this.normFactor = ndim;
        if (wavetable == null || wavetable.length != 2 * ndim + 15) {
            wavetable = new double[2 * ndim + 15];
        }
        rffti(ndim, wavetable);
        ch = new double[ndim];
    }

    public void ft(double[] x) {
        if (x.length != ndim)
            throw new IllegalArgumentException("The length of data can not match that of the wavetable");
        rfftf(ndim, x, wavetable, ch);
    }

    private static void radf2(
            int ido, int l1, double[] cc, double[] ch,
            double[] wtable, int offset) {
        int i, k, ic, iw1;
        double tr2, ti2;
        iw1 = offset;

        k = 0;
        while (k < l1) {
            ch[2 * k * ido] = cc[k * ido] + cc[(k + l1) * ido];
            ch[(2 * k + 1) * ido + ido - 1] = cc[k * ido] - cc[(k + l1) * ido];
            k++;
        }
        if (ido < 2)
            return;
        if (ido != 2) {
            k = 0;
            while (k < l1) {
                i = 2;
                while (i < ido) {
                    ic = ido - i;
                    tr2 = wtable[i - 2 + iw1] * cc[i - 1 + (k + l1) * ido]
                            + wtable[i - 1 + iw1] * cc[i + (k + l1) * ido];
                    ti2 = wtable[i - 2 + iw1] * cc[i + (k + l1) * ido]
                            - wtable[i - 1 + iw1] * cc[i - 1 + (k + l1) * ido];
                    ch[i + 2 * k * ido] = cc[i + k * ido] + ti2;
                    ch[ic + (2 * k + 1) * ido] = ti2 - cc[i + k * ido];
                    ch[i - 1 + 2 * k * ido] = cc[i - 1 + k * ido] + tr2;
                    ch[ic - 1 + (2 * k + 1) * ido] = cc[i - 1 + k * ido] - tr2;
                    i += 2;
                }
                k++;
            }
            if (ido % 2 == 1)
                return;
        }
        k = 0;
        while (k < l1) {
            ch[(2 * k + 1) * ido] = -cc[ido - 1 + (k + l1) * ido];
            ch[ido - 1 + 2 * k * ido] = cc[ido - 1 + k * ido];
            k++;
        }
    }

    private static void radf3(int ido, int l1, double[] cc, double[] ch, double[] wtable, int offset) {
        double taur = -0.5;
        double taui = 0.866025403784439;
        int i;
        int k = 0;
        int ic;
        double ci2;
        double di2;
        double di3;
        double cr2;
        double dr2;
        double dr3;
        double ti2;
        double ti3;
        double tr2;
        double tr3;
        int iw1 = offset;
        int iw2 = iw1 + ido;

        while (k < l1) {
            cr2 = cc[(k + l1) * ido] + cc[(k + 2 * l1) * ido];
            ch[3 * k * ido] = cc[k * ido] + cr2;
            ch[(3 * k + 2) * ido] = taui * (cc[(k + l1 * 2) * ido] - cc[(k + l1) * ido]);
            ch[ido - 1 + (3 * k + 1) * ido] = cc[k * ido] + taur * cr2;
            k++;
        }
        if (ido == 1)
            return;
        k = 0;
        while (k < l1) {
            i = 2;
            while (i < ido) {
                ic = ido - i;
                dr2 = wtable[i - 2 + iw1] * cc[i - 1 + (k + l1) * ido] + wtable[i - 1 + iw1] * cc[i + (k + l1) * ido];
                di2 = wtable[i - 2 + iw1] * cc[i + (k + l1) * ido] - wtable[i - 1 + iw1] * cc[i - 1 + (k + l1) * ido];
                dr3 = wtable[i - 2 + iw2] * cc[i - 1 + (k + l1 * 2) * ido]
                        + wtable[i - 1 + iw2] * cc[i + (k + l1 * 2) * ido];
                di3 = wtable[i - 2 + iw2] * cc[i + (k + l1 * 2) * ido]
                        - wtable[i - 1 + iw2] * cc[i - 1 + (k + l1 * 2) * ido];
                cr2 = dr2 + dr3;
                ci2 = di2 + di3;
                ch[i - 1 + 3 * k * ido] = cc[i - 1 + k * ido] + cr2;
                ch[i + 3 * k * ido] = cc[i + k * ido] + ci2;
                tr2 = cc[i - 1 + k * ido] + taur * cr2;
                ti2 = cc[i + k * ido] + taur * ci2;
                tr3 = taui * (di2 - di3);
                ti3 = taui * (dr3 - dr2);
                ch[i - 1 + (3 * k + 2) * ido] = tr2 + tr3;
                ch[ic - 1 + (3 * k + 1) * ido] = tr2 - tr3;
                ch[i + (3 * k + 2) * ido] = ti2 + ti3;
                ch[ic + (3 * k + 1) * ido] = ti3 - ti2;
                i += 2;
            }
            k++;
        }
    }

    private static void radf4(int ido, int l1, double[] cc, double[] ch, double[] wtable, int offset) {
        double hsqt2 = 0.7071067811865475;
        int i, k, ic;
        double ci2, ci3, ci4, cr2, cr3, cr4, ti1, ti2, ti3, ti4, tr1, tr2, tr3, tr4;
        int iw1 = offset;
        int iw2 = offset + ido;
        int iw3 = iw2 + ido;
        k = 0;
        while (k < l1) {
            tr1 = cc[(k + l1) * ido] + cc[(k + 3 * l1) * ido];
            tr2 = cc[k * ido] + cc[(k + 2 * l1) * ido];
            ch[4 * k * ido] = tr1 + tr2;
            ch[ido - 1 + (4 * k + 3) * ido] = tr2 - tr1;
            ch[ido - 1 + (4 * k + 1) * ido] = cc[k * ido] - cc[(k + 2 * l1) * ido];
            ch[(4 * k + 2) * ido] = cc[(k + 3 * l1) * ido] - cc[(k + l1) * ido];
            k++;
        }
        if (ido < 2) {
            return;
        }
        if (ido != 2) {
            k = 0;
            while (k < l1) {
                i = 2;
                while (i < ido) {
                    ic = ido - i;
                    cr2 = wtable[i - 2 + iw1] * cc[i - 1 + (k + l1) * ido]
                            + wtable[i - 1 + iw1] * cc[i + (k + l1) * ido];
                    ci2 = wtable[i - 2 + iw1] * cc[i + (k + l1) * ido]
                            - wtable[i - 1 + iw1] * cc[i - 1 + (k + l1) * ido];
                    cr3 = wtable[i - 2 + iw2] * cc[i - 1 + (k + 2 * l1) * ido]
                            + wtable[i - 1 + iw2] * cc[i + (k + 2 * l1) * ido];
                    ci3 = wtable[i - 2 + iw2] * cc[i + (k + 2 * l1) * ido]
                            - wtable[i - 1 + iw2] * cc[i - 1 + (k + 2 * l1) * ido];
                    cr4 = wtable[i - 2 + iw3] * cc[i - 1 + (k + 3 * l1) * ido]
                            + wtable[i - 1 + iw3] * cc[i + (k + 3 * l1) * ido];
                    ci4 = wtable[i - 2 + iw3] * cc[i + (k + 3 * l1) * ido]
                            - wtable[i - 1 + iw3] * cc[i - 1 + (k + 3 * l1) * ido];
                    tr1 = cr2 + cr4;
                    tr4 = cr4 - cr2;
                    ti1 = ci2 + ci4;
                    ti4 = ci2 - ci4;
                    ti2 = cc[i + k * ido] + ci3;
                    ti3 = cc[i + k * ido] - ci3;
                    tr2 = cc[i - 1 + k * ido] + cr3;
                    tr3 = cc[i - 1 + k * ido] - cr3;
                    ch[i - 1 + 4 * k * ido] = tr1 + tr2;
                    ch[ic - 1 + (4 * k + 3) * ido] = tr2 - tr1;
                    ch[i + 4 * k * ido] = ti1 + ti2;
                    ch[ic + (4 * k + 3) * ido] = ti1 - ti2;
                    ch[i - 1 + (4 * k + 2) * ido] = ti4 + tr3;
                    ch[ic - 1 + (4 * k + 1) * ido] = tr3 - ti4;
                    ch[i + (4 * k + 2) * ido] = tr4 + ti3;
                    ch[ic + (4 * k + 1) * ido] = tr4 - ti3;
                    i += 2;
                }
                k++;
            }
            if (ido % 2 == 1)
                return;
        }
        k = 0;
        while (k < l1) {
            ti1 = -hsqt2 * (cc[ido - 1 + (k + l1) * ido] + cc[ido - 1 + (k + 3 * l1) * ido]);
            tr1 = hsqt2 * (cc[ido - 1 + (k + l1) * ido] - cc[ido - 1 + (k + 3 * l1) * ido]);
            ch[ido - 1 + 4 * k * ido] = tr1 + cc[ido - 1 + k * ido];
            ch[ido - 1 + (4 * k + 2) * ido] = cc[ido - 1 + k * ido] - tr1;
            ch[(4 * k + 1) * ido] = ti1 - cc[ido - 1 + (k + 2 * l1) * ido];
            ch[(4 * k + 3) * ido] = ti1 + cc[ido - 1 + (k + 2 * l1) * ido];
            k++;
        }
    }

    private static void radf5(int ido, int l1, double[] cc, double[] ch, double[] wtable, int offset) {
        double tr11 = 0.309016994374947;
        double ti11 = 0.951056516295154;
        double tr12 = -0.809016994374947;
        double ti12 = 0.587785252292473;
        int i;
        int k = 0;
        int ic;
        double ci2;
        double di2;
        double ci4;
        double ci5;
        double di3;
        double di4;
        double di5;
        double ci3;
        double cr2;
        double cr3;
        double dr2;
        double dr3;
        double dr4;
        double dr5;
        double cr5;
        double cr4;
        double ti2;
        double ti3;
        double ti5;
        double ti4;
        double tr2;
        double tr3;
        double tr4;
        double tr5;
        int iw1 = offset;
        int iw2;
        int iw3;
        int iw4;
        iw2 = iw1 + ido;
        iw3 = iw2 + ido;
        iw4 = iw3 + ido;

        while (k < l1) {
            cr2 = cc[(k + 4 * l1) * ido] + cc[(k + l1) * ido];
            ci5 = cc[(k + 4 * l1) * ido] - cc[(k + l1) * ido];
            cr3 = cc[(k + 3 * l1) * ido] + cc[(k + 2 * l1) * ido];
            ci4 = cc[(k + 3 * l1) * ido] - cc[(k + 2 * l1) * ido];
            ch[5 * k * ido] = cc[k * ido] + cr2 + cr3;
            ch[ido - 1 + (5 * k + 1) * ido] = cc[k * ido] + tr11 * cr2 + tr12 * cr3;
            ch[(5 * k + 2) * ido] = ti11 * ci5 + ti12 * ci4;
            ch[ido - 1 + (5 * k + 3) * ido] = cc[k * ido] + tr12 * cr2 + tr11 * cr3;
            ch[(5 * k + 4) * ido] = ti12 * ci5 - ti11 * ci4;
            k++;
        }
        if (ido == 1)
            return;
        k = 0;
        while (k < l1) {
            i = 2;
            while (i < ido) {
                ic = ido - i;
                dr2 = wtable[i - 2 + iw1] * cc[i - 1 + (k + l1) * ido] + wtable[i - 1 + iw1] * cc[i + (k + l1) * ido];
                di2 = wtable[i - 2 + iw1] * cc[i + (k + l1) * ido] - wtable[i - 1 + iw1] * cc[i - 1 + (k + l1) * ido];
                dr3 = wtable[i - 2 + iw2] * cc[i - 1 + (k + 2 * l1) * ido]
                        + wtable[i - 1 + iw2] * cc[i + (k + 2 * l1) * ido];
                di3 = wtable[i - 2 + iw2] * cc[i + (k + 2 * l1) * ido]
                        - wtable[i - 1 + iw2] * cc[i - 1 + (k + 2 * l1) * ido];
                dr4 = wtable[i - 2 + iw3] * cc[i - 1 + (k + 3 * l1) * ido]
                        + wtable[i - 1 + iw3] * cc[i + (k + 3 * l1) * ido];
                di4 = wtable[i - 2 + iw3] * cc[i + (k + 3 * l1) * ido]
                        - wtable[i - 1 + iw3] * cc[i - 1 + (k + 3 * l1) * ido];
                dr5 = wtable[i - 2 + iw4] * cc[i - 1 + (k + 4 * l1) * ido]
                        + wtable[i - 1 + iw4] * cc[i + (k + 4 * l1) * ido];
                di5 = wtable[i - 2 + iw4] * cc[i + (k + 4 * l1) * ido]
                        - wtable[i - 1 + iw4] * cc[i - 1 + (k + 4 * l1) * ido];
                cr2 = dr2 + dr5;
                ci5 = dr5 - dr2;
                cr5 = di2 - di5;
                ci2 = di2 + di5;
                cr3 = dr3 + dr4;
                ci4 = dr4 - dr3;
                cr4 = di3 - di4;
                ci3 = di3 + di4;
                ch[i - 1 + 5 * k * ido] = cc[i - 1 + k * ido] + cr2 + cr3;
                ch[i + 5 * k * ido] = cc[i + k * ido] + ci2 + ci3;
                tr2 = cc[i - 1 + k * ido] + tr11 * cr2 + tr12 * cr3;
                ti2 = cc[i + k * ido] + tr11 * ci2 + tr12 * ci3;
                tr3 = cc[i - 1 + k * ido] + tr12 * cr2 + tr11 * cr3;
                ti3 = cc[i + k * ido] + tr12 * ci2 + tr11 * ci3;
                tr5 = ti11 * cr5 + ti12 * cr4;
                ti5 = ti11 * ci5 + ti12 * ci4;
                tr4 = ti12 * cr5 - ti11 * cr4;
                ti4 = ti12 * ci5 - ti11 * ci4;
                ch[i - 1 + (5 * k + 2) * ido] = tr2 + tr5;
                ch[ic - 1 + (5 * k + 1) * ido] = tr2 - tr5;
                ch[i + (5 * k + 2) * ido] = ti2 + ti5;
                ch[ic + (5 * k + 1) * ido] = ti5 - ti2;
                ch[i - 1 + (5 * k + 4) * ido] = tr3 + tr4;
                ch[ic - 1 + (5 * k + 3) * ido] = tr3 - tr4;
                ch[i + (5 * k + 4) * ido] = ti3 + ti4;
                ch[ic + (5 * k + 3) * ido] = ti4 - ti3;
                i += 2;
            }
            ++k;
        }
    }

    private void radfg(int ido, int ip, int l1, int idl1, double[] cc, double[] c1, double[] c2, double[] ch,
            double[] ch2, double[] wtable, int offset) {
        double twopi = 2.0 * Math.PI;
        int idij;
        int ipph = (ip + 1) / 2;
        int i, j, k, l = 1;
        int j2;
        int ic;
        int jc;
        int lc;
        int ik;
        int is;
        int nbd = (ido - 1) / 2;
        double dc2;
        double ai1;
        double ai2;
        double ar1;
        double ar2;
        double ds2;
        double dcp;
        double arg;
        double dsp;
        double ar1h;
        double ar2h;

        RealDoubleFFT $this$run;
        boolean var51;

        arg = twopi / ip;
        dcp = Math.cos(arg);
        dsp = Math.sin(arg);

        if (ido != 1) {
            ik = 0;
            while (ik < idl1) {
                ch2[ik] = c2[ik];
                ik++;
            }
            for (j = 1; j < ip; ++j) {
                $this$run = (RealDoubleFFT) this;
                var51 = false;

                for (k = 0; k < l1; ++k) {
                    ch[(k + j * l1) * ido] = c1[(k + j * l1) * ido];
                }

            }
            if (nbd <= l1) {
                is = -ido;
                j = 1;
                while (j < ip) {
                    is += ido;
                    idij = is - 1;
                    i = 2;
                    while (i < ido) {
                        idij += 2;
                        k = 0;
                        while (k < l1) {
                            ch[i - 1 + (k + j * l1) * ido] = wtable[idij - 1 + offset] * c1[i - 1 + (k + j * l1) * ido]
                                    + wtable[idij + offset] * c1[i + (k + j * l1) * ido];
                            ch[i + (k + j * l1) * ido] = wtable[idij - 1 + offset] * c1[i + (k + j * l1) * ido]
                                    - wtable[idij + offset] * c1[i - 1 + (k + j * l1) * ido];
                            k++;
                        }
                        i += 2;
                    }
                    j++;
                }
            } else {
                is = -ido;
                j = 1;
                while (j < ip) {
                    is += ido;
                    k = 0;
                    while (k < l1) {
                        idij = is - 1;
                        i = 2;
                        while (i < ido) {
                            idij += 2;
                            ch[i - 1 + (k + j * l1) * ido] = wtable[idij - 1 + offset] * c1[i - 1 + (k + j * l1) * ido]
                                    + wtable[idij + offset] * c1[i + (k + j * l1) * ido];
                            ch[i + (k + j * l1) * ido] = wtable[idij - 1 + offset] * c1[i + (k + j * l1) * ido]
                                    - wtable[idij + offset] * c1[i - 1 + (k + j * l1) * ido];
                            i += 2;
                        }
                        k++;
                    }
                    j++;
                }
            }
            if (nbd >= l1) {
                j = 1;
                while (j < ipph) {
                    jc = ip - j;
                    k = 0;
                    while (k < l1) {
                        i = 2;
                        while (i < ido) {
                            c1[i - 1 + (k + j * l1) * ido] = ch[i - 1 + (k + j * l1) * ido]
                                    + ch[i - 1 + (k + jc * l1) * ido];
                            c1[i - 1 + (k + jc * l1) * ido] = ch[i + (k + j * l1) * ido] - ch[i + (k + jc * l1) * ido];
                            c1[i + (k + j * l1) * ido] = ch[i + (k + j * l1) * ido] + ch[i + (k + jc * l1) * ido];
                            c1[i + (k + jc * l1) * ido] = ch[i - 1 + (k + jc * l1) * ido]
                                    - ch[i - 1 + (k + j * l1) * ido];
                            i += 2;
                        }
                        k++;
                    }
                    j++;
                }
            } else {
                j = 1;
                while (j < ipph) {
                    jc = ip - j;
                    i = 2;
                    while (i < ido) {
                        k = 0;
                        while (k < l1) {
                            c1[i - 1 + (k + j * l1) * ido] = ch[i - 1 + (k + j * l1) * ido]
                                    + ch[i - 1 + (k + jc * l1) * ido];
                            c1[i - 1 + (k + jc * l1) * ido] = ch[i + (k + j * l1) * ido] - ch[i + (k + jc * l1) * ido];
                            c1[i + (k + j * l1) * ido] = ch[i + (k + j * l1) * ido] + ch[i + (k + jc * l1) * ido];
                            c1[i + (k + jc * l1) * ido] = ch[i - 1 + (k + jc * l1) * ido]
                                    - ch[i - 1 + (k + j * l1) * ido];
                            k++;
                        }
                        i += 2;
                    }
                    j++;
                }
            }
        } else {
            ik = 0;
            while (ik < idl1) {
                c2[ik] = ch2[ik];
                ik++;
            }
        }
        j = 1;
        while (j < ipph) {
            jc = ip - j;
            k = 0;
            while (k < l1) {
                c1[(k + j * l1) * ido] = ch[(k + j * l1) * ido] + ch[(k + jc * l1) * ido];
                c1[(k + jc * l1) * ido] = ch[(k + jc * l1) * ido] - ch[(k + j * l1) * ido];
                k++;
            }
            j++;
        }

        ar1 = 1.0;
        ai1 = 0.0;
        while (l < ipph) {
            lc = ip - l;
            ar1h = dcp * ar1 - dsp * ai1;
            ai1 = dcp * ai1 + dsp * ar1;
            ar1 = ar1h;
            ik = 0;
            while (ik < idl1) {
                ch2[ik + l * idl1] = c2[ik] + ar1 * c2[ik + idl1];
                ch2[ik + lc * idl1] = ai1 * c2[ik + (ip - 1) * idl1];
                ik++;
            }
            dc2 = ar1;
            ds2 = ai1;
            ar2 = ar1;
            ai2 = ai1;
            j = 2;
            while (j < ipph) {
                jc = ip - j;
                ar2h = dc2 * ar2 - ds2 * ai2;
                ai2 = dc2 * ai2 + ds2 * ar2;
                ar2 = ar2h;
                ik = 0;
                while (ik < idl1) {
                    ch2[ik + l * idl1] += ar2 * c2[ik + j * idl1];
                    ch2[ik + lc * idl1] += ai2 * c2[ik + jc * idl1];
                    ik++;
                }
                j++;
            }
            l++;
        }
        for (j = 1; j < ipph; ++j) {
            $this$run = (RealDoubleFFT) this;
            var51 = false;

            for (ik = 0; ik < idl1; ++ik) {
                ch2[ik] += c2[ik + j * idl1];
            }
        }

        if (ido >= l1) {
            k = 0;
            while (k < l1) {
                i = 0;
                while (i < ido) {
                    cc[i + k * ip * ido] = ch[i + k * ido];
                    i++;
                }
                k++;
            }
        } else {
            i = 0;
            while (i < ido) {
                k = 0;
                while (k < l1) {
                    cc[i + k * ip * ido] = ch[i + k * ido];
                    k++;
                }
                i++;
            }
        }
        j = 1;
        while (j < ipph) {
            jc = ip - j;
            j2 = 2 * j;
            k = 0;
            while (k < l1) {
                cc[ido - 1 + (j2 - 1 + k * ip) * ido] = ch[(k + j * l1) * ido];
                cc[(j2 + k * ip) * ido] = ch[(k + jc * l1) * ido];
                k++;
            }
            j++;
        }
        if (ido == 1)
            return;
        if (nbd >= l1) {
            j = 1;
            while (j < ipph) {
                jc = ip - j;
                j2 = 2 * j;
                k = 0;
                while (k < l1) {
                    i = 2;
                    while (i < ido) {
                        ic = ido - i;
                        cc[i - 1 + (j2 + k * ip) * ido] = ch[i - 1 + (k + j * l1) * ido]
                                + ch[i - 1 + (k + jc * l1) * ido];
                        cc[ic - 1 + (j2 - 1 + k * ip) * ido] = ch[i - 1 + (k + j * l1) * ido]
                                - ch[i - 1 + (k + jc * l1) * ido];
                        cc[i + (j2 + k * ip) * ido] = ch[i + (k + j * l1) * ido] + ch[i + (k + jc * l1) * ido];
                        cc[ic + (j2 - 1 + k * ip) * ido] = ch[i + (k + jc * l1) * ido] - ch[i + (k + j * l1) * ido];
                        i += 2;
                    }
                    k++;
                }
                j++;
            }
        } else {
            j = 1;
            while (j < ipph) {
                jc = ip - j;
                j2 = 2 * j;
                i = 2;
                while (i < ido) {
                    ic = ido - i;
                    k = 0;
                    while (k < l1) {
                        cc[i - 1 + (j2 + k * ip) * ido] = ch[i - 1 + (k + j * l1) * ido]
                                + ch[i - 1 + (k + jc * l1) * ido];
                        cc[ic - 1 + (j2 - 1 + k * ip) * ido] = ch[i - 1 + (k + j * l1) * ido]
                                - ch[i - 1 + (k + jc * l1) * ido];
                        cc[i + (j2 + k * ip) * ido] = ch[i + (k + j * l1) * ido] + ch[i + (k + jc * l1) * ido];
                        cc[ic + (j2 - 1 + k * ip) * ido] = ch[i + (k + jc * l1) * ido] - ch[i + (k + j * l1) * ido];
                        k++;
                    }
                    i += 2;
                }
                j++;
            }
        }
    }

    public void rfftf1(int n, double[] c, double[] wtable, int offset, double[] ch) {
        int i = 0;
        int k1 = 1;
        int l1;
        int l2;
        int na;
        int kh;
        int nf;
        int ip;
        int iw = n - 1 + n + offset;
        int ido;
        int idl1;

        System.arraycopy(wtable, offset, ch, 0, n);

        nf = (int) wtable[1 + 2 * n + offset];
        na = 1;
        l2 = n;
        while (k1 <= nf) {
            kh = nf - k1;
            ip = (int) wtable[kh + 2 + 2 * n + offset];
            l1 = l2 / ip;
            ido = n / l2;
            idl1 = ido * l1;
            iw -= (ip - 1) * ido;
            na = 1 - na;
            if (ip == 4) {
                if (na == 0) {
                    radf4(ido, l1, c, ch, wtable, iw);
                } else {
                    radf4(ido, l1, ch, c, wtable, iw);
                }
            } else if (ip == 2) {
                if (na == 0) {
                    radf2(ido, l1, c, ch, wtable, iw);
                } else {
                    radf2(ido, l1, ch, c, wtable, iw);
                }
            } else if (ip == 3) {
                if (na == 0) {
                    radf3(ido, l1, c, ch, wtable, iw);
                } else {
                    radf3(ido, l1, ch, c, wtable, iw);
                }
            } else if (ip == 5) {
                if (na == 0) {
                    radf5(ido, l1, c, ch, wtable, iw);
                } else {
                    radf5(ido, l1, ch, c, wtable, iw);
                }
            } else {
                if (ido == 1) {
                    na = 1 - na;
                }

                byte var10000;
                if (na == 0) {
                    radfg(ido, ip, l1, idl1, c, c, c, ch, ch, wtable, iw);
                    var10000 = 1;
                } else {
                    radfg(ido, ip, l1, idl1, ch, ch, ch, c, c, wtable, iw);
                    var10000 = 0;
                }

                na = var10000;
            }
            l2 = l1;
            ++k1;
        }
        if (na == 1)
            return;
        while (i < n) {
            c[i] = ch[i];
            i++;
        }
    }

    private void rfftf(int n, double[] r, double[] wtable, double[] ch) {
        if (n == 1)
            return;
        rfftf1(n, r, wtable, 0, ch);
    }

    private void rffti1(int n, double[] wtable, int offset) {
        double twopi = 2.0 * Math.PI;
        double argh;
        int ntry = 0;
        int i, j, k1 = 1, l1, l2, ib, ld, ii, nf = 0, ip, nl, is = 0, nq, nr;
        double fi, argld, arg;
        int ido, ipm, nfm1;

        nl = n;
        j = 0;
        factorize_loop: while (true) {
            ++j;
            if (j <= 4)
                ntry = ntryh[j - 1];
            else
                ntry += 2;
            do {
                nq = nl / ntry;
                nr = nl - ntry * nq;
                if (nr != 0)
                    continue factorize_loop;
                ++nf;
                wtable[nf + 1 + 2 * n + offset] = (double) ntry;

                nl = nq;
                if (ntry == 2 && nf != 1) {
                    i = 2;
                    while (i <= nf) {
                        ib = nf - i + 2;
                        wtable[ib + 1 + 2 * n + offset] = wtable[ib + 2 * n + offset];
                        i++;
                    }
                    wtable[2 + 2 * n + offset] = 2.0;
                }
            } while (nl != 1);
            break factorize_loop;
        }
        wtable[0 + 2 * n + offset] = (double) n;
        wtable[1 + 2 * n + offset] = (double) nf;
        argh = twopi / n;
        nfm1 = nf - 1;
        l1 = 1;
        if (nfm1 == 0)
            return;
        while (k1 <= nfm1) {
            ip = (int) wtable[k1 + 1 + 2 * n + offset];
            ld = 0;
            l2 = l1 * ip;
            ido = n / l2;
            ipm = ip - 1;
            j = 1;
            while (j <= ipm) {
                ld += l1;
                i = is;
                argld = ld * argh;

                fi = 0.0;
                ii = 3;
                while (ii <= ido) {
                    i += 2;
                    fi += 1.0;
                    arg = fi * argld;
                    wtable[i - 2 + n + offset] = Math.cos(arg);
                    wtable[i - 1 + n + offset] = Math.sin(arg);
                    ii += 2;
                }
                is += ido;
                ++j;
            }
            l1 = l2;
            k1++;
        }
    }

    private void rffti(int n, double[] wtable) {
        if (n == 1)
            return;
        rffti1(n, wtable, 0);
    }
}
