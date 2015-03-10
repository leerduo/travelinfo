package com.ustc.dystu.dandelion.utils;

import android.util.Log;

public class Logger {
	
	public static int DEBUG_LEVEL = 6;// ÆÁ±Îlog,ÐÞ¸ÄÎª0

	private static final int VERBOSE = 5;
	private static final int DEBUG = 4;
	private static final int INFO = 3;
	private static final int WARN = 2;
	private static final int ERROR = 1;

	public static int v(String tag, String msg) {
		if (DEBUG_LEVEL > VERBOSE) {
			return Log.v(tag, msg);
		} else {
			return 0;
		}
	}

	public static int v(String tag, String msg, Throwable e) {
		if (DEBUG_LEVEL > VERBOSE) {			return Log.v(tag, msg, e);
		} else {
			return 0;
		}
	}

	public static int d(String tag, String msg) {
		if (DEBUG_LEVEL > DEBUG) {
			return Log.d(tag, msg);
		} else {
			return 0;
		}
	}

	public static int d(String tag, String msg, Throwable e) {
		if (DEBUG_LEVEL > DEBUG) {
			return Log.d(tag, msg, e);
		} else {
			return 0;
		}
	}

	public static int i(String tag, String msg) {
		if (DEBUG_LEVEL > INFO) {
			return Log.i(tag, msg);
		} else {
			return 0;
		}
	}

	public static int i(String tag, String msg, Throwable e) {
		if (DEBUG_LEVEL > INFO) {
			return Log.i(tag, msg, e);
		} else {
			return 0;
		}
	}

	public static int w(String tag, String msg) {
		if (DEBUG_LEVEL > WARN) {
			return Log.w(tag, msg);
		} else {
			return 0;
		}
	}

	public static int w(String tag, String msg, Throwable e) {
		if (DEBUG_LEVEL > WARN) {
			return Log.w(tag, msg, e);
		} else {
			return 0;
		}
	}

	public static int e(String tag, String msg) {
		if (DEBUG_LEVEL > ERROR) {
			return Log.e(tag, msg);
		} else {
			return 0;
		}
	}

	public static int e(String tag, String msg, Throwable e) {
		if (DEBUG_LEVEL > ERROR) {
			return Log.e(tag, msg, e);
		} else {
			return 0;
		}
	}

}
