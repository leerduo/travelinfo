package com.ustc.dystu.dandelion.utils.blow;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;

import com.ustc.dystu.dandelion.BlowActivity;
import com.ustc.dystu.dandelion.utils.Logger;

public class RecordThread extends Thread {
	private AudioRecord ar;
	private int bs = 100;
	private static int SAMPLE_RATE_IN_HZ = 8000;
	private int number = 1;
	private int tal = 1;
	private Handler handler;
	private long currenttime;
	private long endtime;
	private long time = 1;

	// 到达该值之后 触发事件
	private static int BLOW_ACTIVI = 3000;

	public RecordThread(Handler myHandler) {
		super();
		bs = AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ,
				AudioFormat.CHANNEL_CONFIGURATION_MONO,
				AudioFormat.ENCODING_PCM_16BIT);
		ar = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE_IN_HZ,
				AudioFormat.CHANNEL_CONFIGURATION_MONO,
				AudioFormat.ENCODING_PCM_16BIT, bs);
		handler = myHandler;
	}

	@Override
	public void run() {
		try {
			ar.startRecording();
			Parameter.isblow = true;
			// 用于读取的 buffer
			byte[] buffer = new byte[bs];
			long startTime = System.currentTimeMillis();
			long timeDelta = 0;
			while (Parameter.isblow) {
				number++;
				// Logger.d("Test", "number-->" + number);
				sleep(8);
				currenttime = System.currentTimeMillis();
				int r = ar.read(buffer, 0, bs) + 1;
				int v = 0;
				for (int i = 0; i < buffer.length; i++) {
					v += (buffer[i] * buffer[i]);
				}
				int value = Integer.valueOf(v / (int) r);
				tal = tal + value;
				endtime = System.currentTimeMillis();
				time = time + (endtime - currenttime);

				timeDelta = endtime - startTime;

				if (timeDelta > 3000) {
					BLOW_ACTIVI = 2500;
				} else if (timeDelta > 6000) {
					BLOW_ACTIVI = 2000;
				} else if (timeDelta > 8000) {
					BLOW_ACTIVI = 1500;
				} else if (timeDelta > 10000) {
					BLOW_ACTIVI = 500;
				}
				Logger.d("Test", "BLOW_ACTIVI-->" + BLOW_ACTIVI);

				if (time >= 500 || number > 5) {

					int total = tal / number;
					Logger.d("Test", "total-->" + total);
					Logger.d("Test", "timeDelta-->" + timeDelta);
					if (total > BLOW_ACTIVI) {
						// 发送消息通知到界面 触发动画

						// 利用传入的handler 给界面发送通知
						handler.sendEmptyMessage(BlowActivity.BLOW_FINISHED); // 改变i的值后，发送一个空message到主线程

						number = 1;
						tal = 1;
						time = 1;
						BLOW_ACTIVI = 3000;
					}
				}

			}
			ar.stop();
			ar.release();
			bs = 100;
			BLOW_ACTIVI = 3000;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}