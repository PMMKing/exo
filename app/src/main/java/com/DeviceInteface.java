package com;

import android.util.Log;

import com.haolb.client.domain.LockInfoResult.LockInfo;
import com.haolb.client.manager.OpenDoorManager;
import com.haolb.client.utils.QArrays;

import java.util.List;

public class DeviceInteface {


	public DeviceInteface() {

	}


    public LockInfo check(int[] x1buffer_CH0) {
		List<LockInfo> all = OpenDoorManager.getAll();
		if (QArrays.isEmpty(all)){
			return null ;
		}
		for (LockInfo lockInfo :all){
            boolean ok = check(x1buffer_CH0, lockInfo.gateid);
//            boolean ok = check(x1buffer_CH0, 0xc0a80002L);
			if (ok) {
				return lockInfo;
			}
		}
		return null;
	}
	
	private static void dumpBuffer(long [] in){
		 if (in != null) {
			 String s = "";
			for (int i = 0; i < in.length; i++) {
				s += Math.abs(in[i]);
				if (i != in.length - 1) {
					s += '\t';
				}
			}
			System.out.println(s);
		}
	}

	private static void dumpBuffer(int[] in) {
		if (in != null) {
			String s = "";
			for (int i = 834; i < 834 + 64; i++) {
				s += Math.abs(in[i]);
				if (i != in.length - 1) {
					s += '\t';
				}
			}
			System.out.println(s);
		}
	}

	private static void dumpBuffer0(int[] in) {
		if (in != null) {
			String s = "";
			for (int i = 0; i < in.length; i++) {
				s += Math.abs(in[i]);
				if (i != in.length - 1) {
					s += '\t';
				}
			}
			System.out.println(s);
		}
	}

	private static void dump2Buffer(int[] in, int[] in1) {
		if (in != null && in1 != null) {
			String s = "";
			for (int i = 0; i < in.length; i++) {
				s += Math.abs(in[i]);
				if (i != in.length - 1) {
					s += '\t';
				}
			}
			String s1 = "";
			for (int i = 0; i < in.length; i++) {
				s1 += Math.abs(in1[i]);
				if (i != in1.length - 1) {
					s1 += '\t';
				}
			}
			System.out.println(s);
			System.out.println('\n');
			System.out.println(s1);
		}
	}

	long demodulate = 0;
	long eSignalBandAvg = 0;
	long eNoiseBandAvg = 0;
	long destSNR = 0;
	int fft_freq_width = 64;
	int fft_freq_min = 0;
	int fft_freq_max = 0;
	int indexf_min = fft_freq_min = 18000 * 2048 / 44100 - 1; // LOCK=743
	int indexf_max = fft_freq_max = fft_freq_min + fft_freq_width;

	private boolean check(int[] x1buffer_CH0, long sourceKeyID) {
		final int sourceSNR = 300;
		final int indexf_inc = 2;
		final int threshod_SNR = sourceSNR;
		long signal_check_bit = 0x00000001l;
		long eSignalAvg = 0l;
		long eSignal = 0l;
		long eSignalBand = 0l;
		long eNoiseBand = 0l;
		long detectSNR = 0l;
		long detectKeyID = 0l;
		int index_div = 0;

		// step1: calculate signals and noises sum
		for (int i = indexf_min; i < indexf_max; i += indexf_inc) {
			long a = (long) Math.abs(x1buffer_CH0[i]);
			long b = (long) Math.abs(x1buffer_CH0[i + 1]);

			eNoiseBand += b * b;
			if ((signal_check_bit & sourceKeyID) !=0 ) {
				eSignalBand += a * a;
				index_div += 1;
			} else {
				eNoiseBand += a * a;
			}
			signal_check_bit <<= 1;
		}

		// step2: calculate SNR
		eNoiseBand /= (fft_freq_width - index_div);
		eSignalBand /= index_div;

		eSignalBand >>= 9;
		eNoiseBand >>= 11;
		detectSNR = eSignalBand / (eNoiseBand + 1);

		// trace info...
		eSignalBandAvg = eSignalBand;
		eNoiseBandAvg = eNoiseBand;
		destSNR = detectSNR;
		// destKeyID = 0;
		int[] buffer_noise = new int[64];
		int[] buffer_snr = new int[64];
		int[] buffer_TmpX = new int[64];
		int[] buffer_avg = new int[64];
		for (int i = 0; i < 64; i++) {
			buffer_noise[i] = (int) eNoiseBandAvg;
		}
		for (int i = 0; i < 64; i++) {
			buffer_snr[i] = (int) destSNR;
		}

		eSignalAvg = (eSignalBand >> 8) + 1;
		for (int i = 0; i < 64; i++) {
			buffer_avg[i] = (int) eSignalAvg;
		}

		// step3: get key ID
		if (detectSNR > threshod_SNR) {
			// good SMR, assume signal present
			int freq_detect_flag = 0;
			long freq_check_bit = 0x00000001l;

			for (int i = indexf_min; i < indexf_max; i += indexf_inc) {
				long a = (long) Math.abs(x1buffer_CH0[i]);
				// long b = (long) Math.abs(x1buffer_CH0[i + 1]);
				eSignal = a * a;
				eSignal >>= 12;

				buffer_TmpX[i - indexf_min] = (int) eSignal;

				if ((freq_check_bit & sourceKeyID) != 0) {
					if (eSignal >= eSignalAvg) {
						freq_detect_flag = 1;
					} else {
						freq_detect_flag = 0;
					}
				}
				freq_check_bit <<= 1;

				detectKeyID >>= 1;
				if (freq_detect_flag == 1) {
					detectKeyID |= 0x80000000l; // write an one to detectKeyID
					freq_detect_flag = 0;
				}
			}

			// step4: turn-on this lock
			if (detectKeyID == sourceKeyID) {
				Log.v("destSNR", String.valueOf(destSNR));
				Log.v("eNoiseBandAvg", String.valueOf(eNoiseBandAvg));
				Log.v("eSignalAvg", String.valueOf(eSignalAvg));
				Log.v("buffer_TmpX", String.valueOf(buffer_TmpX[0]));
				dump2Buffer(buffer_TmpX, buffer_avg);
				return true;
			}
		}

		// frameCnt++;
		// if ((frameCnt % frameCntFixed) == 0) {
		// Log.v("x1buffer_CH0", String.valueOf(x1buffer_CH0[0]));
		// dumpBuffer(x1buffer_CH0);
		// Log.v("buffer_noise", String.valueOf(buffer_noise[0]));
		// dumpBuffer0(buffer_noise);
		// Log.v("buffer_snr", String.valueOf(buffer_snr[0]));
		// dumpBuffer0(buffer_snr);
		// Log.v("buffer_TmpX", String.valueOf(buffer_TmpX[0]));
		// dumpBuffer0(buffer_TmpX);
		// Log.v("buffer_avg", String.valueOf(buffer_avg[0]));
		// dumpBuffer0(buffer_avg);
		// // Log.v("destSNR", String.valueOf(destSNR));
		// // Log.v("eNoiseBandAvg", String.valueOf(eNoiseBandAvg));
		// // Log.v("eSignalBandAvg", String.valueOf(eSignalBandAvg));
		// }

		return false;
	}
	
}
