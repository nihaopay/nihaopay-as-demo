package com.nihaopay.sdk.pay;

import android.util.Log;

/**
 * Log统一管理类
 * 
 * 
 * 
 */
public class Logger
{

	private Logger()
	{
		/* cannot be instantiated */
		throw new UnsupportedOperationException("cannot be instantiated");
	}

	public static boolean isDebug = true;// 是否需要打印bug，可以在application的onCreate函数里面初始化
	private static final String TAG = "nihaopay";

	// 下面四个是默认tag的函数
	public static void info(String msg)
	{
		Log.i(TAG, msg);
	}

	public static void debug(String msg)
	{
		if (isDebug)
			Log.d(TAG, msg);
	}

	public static void error(String msg)
	{
		Log.e(TAG, msg);
	}

	public static void v(String msg)
	{
		if (isDebug)
			Log.v(TAG, msg);
	}

	// 下面是传入自定义tag的函数
	public static void i(String tag, String msg)
	{
		if (isDebug)
			Log.i(tag, msg);
	}

	public static void d(String tag, String msg)
	{
		if (isDebug)
			Log.i(tag, msg);
	}

	public static void e(String tag, String msg)
	{
		if (isDebug)
			Log.i(tag, msg);
	}

	public static void v(String tag, String msg)
	{
		if (isDebug)
			Log.i(tag, msg);
	}
}