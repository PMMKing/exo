package com.fftpack;

import android.util.Log;

import java.util.Random;

/**
 * Created by chenxi.cui on 2015/7/7.
 */
public class FFTModem {
	private final int aSamplingRate = 44100;
	private final int aBufferSize = 2048;
//	private final int aBufferSize = 3584;
	private final int aDataWidth = 64;
//	private final int aDataWidth = 128;
	private final int aDataSpacing = 2;
//	private final double aFrequencyMin = 16000.0;
	private final double aFrequencyMin = 12000.0;

	private ModemInfoBase ModemParameters = new ModemInfoBase();
	private byte[] dataIndicaion;

	public int getIndexf_min() // index for bit 0
	{
		return ModemParameters.getIndexFrequencyMin();
	}

	public int getIndexf_max() // index for bit 127
	{
		return ModemParameters.getIndexFrequencyMax();
	}

	public int getIndexf_inc() // bit spacing in FFT domain
	{
		return ModemParameters.getPhyDataSpacing();
	}

	public final double threshold_SNR =100;
//	RealDoubleFFT  doubleFFT ;
	public int demodulate(double[] dataIn) {

		if (dataIn.length == ModemParameters.getPhyBufferSize()) {

			// convert audio data to frequency domain
//			if(doubleFFT== null){
//				doubleFFT = new RealDoubleFFT(dataIn.length);
//			}
//			doubleFFT.bt(dataIn);
			// calculate SNR
			double eSignalBand = 0.0;
			double eNoiseBand = 0.0;
			for (int i = getIndexf_min(); i <= getIndexf_max(); i += getIndexf_inc()) {
				eSignalBand += dataIn[i * 2 + 0] * dataIn[i * 2 + 0]
						+ dataIn[i * 2 + 1] * dataIn[i * 2 + 1];
				eNoiseBand += dataIn[i * 2 + 2] * dataIn[i * 2 + 2]
						+ dataIn[i * 2 + 3] * dataIn[i * 2 + 3];
			}

			if (eSignalBand / eNoiseBand < threshold_SNR ||eNoiseBand==0 ||eSignalBand ==0) {
				// low SNR, assumed no signal
				// messageIndication = "low SNR";
				Log.v("ss","low SNR" +eSignalBand / eNoiseBand);
				return 1;
			} else {
				// good SNR, assume signal present
				int kBit = 0;
				int kByte = 0;
				byte tmpB = 0x00;
				dataIndicaion = new byte[ModemParameters.getPhyDataWidth() / 8];
				for (int i = getIndexf_min(); i <= getIndexf_max(); i += getIndexf_inc()) {
					double eSignal = dataIn[i * 2 + 0] * dataIn[i * 2 + 0]
							+ dataIn[i * 2 + 1] * dataIn[i * 2 + 1];
					// double eNoise = dataIn[i * 2 + 2] * dataIn[i * 2 + 2] +
					// dataIn[i * 2 + 3] * dataIn[i * 2 + 3];
					// eNoise += dataIn[i * 2 - 2] * dataIn[i * 2 - 2] +
					// dataIn[i * 2 - 1] * dataIn[i * 2 - 1];
//threshold_SNR  100
					if (eSignal > eNoiseBand * threshold_SNR
							/ ModemParameters.getPhyDataWidth()) { //64
						tmpB >>= 1;
						tmpB += 0x80;
						kBit++;
					} else {
						tmpB >>= 1;
						kBit++;
					}

					if (kBit == 8) {
						dataIndicaion[kByte] = tmpB;
						kByte++;
						kBit = 0;
					}

				}
				Log.v("ss","data present and decoded"+tmpB);
				return 0;
			}
		} else {
			return -1;
		}
	}

	public int modulate(byte[] dataIn, double[] dataOut) {

		// dataOut is to be sent twice consecutively, so the receiver will
		// capture a full buffer of data without the need to synchronize
		dataOut = new double[ModemParameters.getPhyBufferSize()];

		// check dataIn length
		if (dataIn.length * 8 != ModemParameters.getPhyDataWidth()) {
			Log.v("ss","payload length is incorrect" );
			return -1;
		}

		// check dataIn contents
		int sum = 0;
		for (int i = 0; i < dataIn.length; i++)
			sum += dataIn[i];
		if (sum == 0) {
			Log.v("ss","payload is all zero" );
			return -2;
		}

		int kBit = 0;
		int kByte = 0;
		byte tmpByte = dataIn[0];
		int totalOnes = 0;
		Random phase = new Random();
		for (int i = getIndexf_min(); i <= getIndexf_max(); i += getIndexf_inc()) {

			double phaseCurrent = phase.nextDouble();
			if ((tmpByte & 0x01) == 1) {
				double[] t = sincos(4.0 * phaseCurrent);
				dataOut[2 * i] = t[0];
				dataOut[2 * i + 1] = t[1];
				totalOnes++;
			}

			tmpByte >>= 1;
			kBit++;
			if (kBit == 8) {
				kBit = 0;
				kByte++;
				if (i != getIndexf_max())
					tmpByte = dataIn[kByte];
			}

		}
		new RealDoubleFFT(dataOut.length).ft(dataOut);

		for (int i = 0; i < dataOut.length; i++)
			dataOut[i] = dataOut[i] / totalOnes;

		return 0;
	}

	// get sin and sincos[1] from look-up table
	// there is no restriction on x (-inf -+inf)
	// outputs represent sin(x*pi/2) and sincos[1] (x*pi/2), respectively
	//
	// XD, Touch Air
	// 2015.3.2
	//
	public static double[] sincos(double x) {
		double[] sincos = new double[2];
		int length = sincosTable.length - 1;
		// int k;
		// int tmp1 = Math.DivRem((int)(Math.abs(x) * length), length, k); // k
		// ranges from 0 - 128
		// int kq;
		// int tmp2 = Math.DivRem(tmp1, 4, kq); // kq ranged from 0 - 3
		int tmp1 = (int) (Math.abs(x) * length) / length;
		int k = (int) (Math.abs(x) % length) / length;
		int tmp2 = tmp1 / 4;
		int kq = tmp1 % 4;
		switch (kq) {
		case 0: // quadrant 1
		{
			sincos[0] = sincosTable[k];
			sincos[1] = sincosTable[length - k];
			break;
		}

		case 1: // quadrant 2
		{
			sincos[0] = sincosTable[length - k];
			sincos[1] = -sincosTable[k];
			break;
		}

		case 2: // quadrant 3
		{
			sincos[0] = -sincosTable[k];
			sincos[1] = -sincosTable[length - k];
			break;
		}
		case 3: // quadrant 4
		{
			sincos[0] = -sincosTable[length - k];
			sincos[1] = sincosTable[k];
			break;
		}
		default:
			sincos[0] = -100.0;
			sincos[1] = -100.0;
			break;
		}
		if (x < 0)
			sincos[0] = -sincos[0];

		return sincos;
	}

	private static double[] sincosTable = { 0, 0.012272, 0.024541, 0.036807,
			0.049068, 0.061321, 0.073565, 0.085797, 0.098017, 0.11022, 0.12241,
			0.13458, 0.14673, 0.15886, 0.17096, 0.18304, 0.19509, 0.20711,
			0.2191, 0.23106, 0.24298, 0.25487, 0.26671, 0.27852, 0.29028,
			0.30201, 0.31368, 0.32531, 0.33689, 0.34842, 0.3599, 0.37132,
			0.38268, 0.39399, 0.40524, 0.41643, 0.42756, 0.43862, 0.44961,
			0.46054, 0.4714, 0.48218, 0.4929, 0.50354, 0.5141, 0.52459, 0.535,
			0.54532, 0.55557, 0.56573, 0.57581, 0.5858, 0.5957, 0.60551,
			0.61523, 0.62486, 0.63439, 0.64383, 0.65317, 0.66242, 0.67156,
			0.6806, 0.68954, 0.69838, 0.70711, 0.71573, 0.72425, 0.73265,
			0.74095, 0.74914, 0.75721, 0.76517, 0.77301, 0.78074, 0.78835,
			0.79584, 0.80321, 0.81046, 0.81758, 0.82459, 0.83147, 0.83822,
			0.84485, 0.85136, 0.85773, 0.86397, 0.87009, 0.87607, 0.88192,
			0.88764, 0.89322, 0.89867, 0.90399, 0.90917, 0.91421, 0.91911,
			0.92388, 0.92851, 0.93299, 0.93734, 0.94154, 0.94561, 0.94953,
			0.95331, 0.95694, 0.96043, 0.96378, 0.96698, 0.97003, 0.97294,
			0.9757, 0.97832, 0.98079, 0.98311, 0.98528, 0.9873, 0.98918,
			0.9909, 0.99248, 0.99391, 0.99518, 0.99631, 0.99729, 0.99812,
			0.9988, 0.99932, 0.9997, 0.99992, 1 };

	private class ModemInfoBase {

		// parameters whose value can be assigned
		public int getPhySamplingRate() {
			// audio system sampling rate
			return aSamplingRate;
		}

		public int getPhyBufferSize()

		{
			// audio buffer size
			return aBufferSize;
		}

		public int getPhyDataWidth()

		{
			// bit-data frame width (number of bits per frame)
			return aDataWidth;
		}

		public int getPhyDataSpacing()

		{
			// spacing of bits in terms of FFT frequency spacing
			return aDataSpacing;
		}

		public double getPhyFrequencyMin()

		{
			// lower bond of frequency band
			return getIndexFrequencyMin() * aBufferSize
					/ (double) aSamplingRate;
		}

		public double getPhyFrequencyMax()

		{
			// upper bond of frequency band
			return getIndexFrequencyMax() * aBufferSize
					/ (double) aSamplingRate;
		}

		public int getIndexFrequencyMin()

		{
			// index for lower bond of frequency band
			return (int) (aFrequencyMin / aSamplingRate * aBufferSize);
		}

		public int getIndexFrequencyMax()

		{
			// index for upper bond of frequency band
			return (int) (aFrequencyMin / aSamplingRate * aBufferSize)
					+ (aDataWidth - 1) * aDataSpacing;
		}

		// private values, don't bother

		// default constructor
		public ModemInfoBase() {
			// nothing to do
		}

	}
}
