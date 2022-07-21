package com.nihaopay.sdk.pay.demo;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import com.nihaopay.sdk.R;
import com.nihaopay.sdk.pay.Logger;
import com.nihaopay.sdk.pay.NihaopayResult;
import com.nihaopay.sdk.pay.NihaopayTask;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

public class PayDemoActivity extends FragmentActivity {

	private static final int SDK_PAY_FLAG = 1;

	private static final int SDK_CHECK_FLAG = 2;

	private Handler mHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mHandler = new Handler() {
			public void handleMessage(Message msg) {
				switch (msg.what) {
					case SDK_PAY_FLAG: {
						NihaopayResult payResult = new NihaopayResult((String) msg.obj);

						String resultStatus = payResult.getStatus();

						// 判断resultStatus 为“success”则代表支付成功，具体状态码代表含义可参考接口文档
						if (TextUtils.equals(resultStatus, "success")) {
							Toast.makeText(PayDemoActivity.this, "支付成功",
									Toast.LENGTH_SHORT).show();
						} else {
							// 判断resultStatus 为非“success”则代表可能支付失败
							// “pending”代表支付结果因为支付渠道原因或者系统原因还在等待支付结果确认，最终交易是否成功以服务端异步通知为准（小概率状态）
							if (TextUtils.equals(resultStatus, "pending")) {
								Toast.makeText(PayDemoActivity.this, "支付结果确认中",
										Toast.LENGTH_SHORT).show();

							} else {
								// 其他值就可以判断为支付失败，包括用户主动取消支付，或者系统返回的错误
								Toast.makeText(PayDemoActivity.this, "支付失败",
										Toast.LENGTH_SHORT).show();

							}
						}
						break;
					}
					case SDK_CHECK_FLAG: {
						Toast.makeText(PayDemoActivity.this, "检查结果为：" + msg.obj,
								Toast.LENGTH_SHORT).show();
						break;
					}
					default:
						break;
				}
			};
		};

		setContentView(R.layout.pay_main);
	}

	/**
	 * call alipay sdk pay. 调用SDK支付
	 *
	 */
	public void pay(View v, final String payType) {
		if (TextUtils.isEmpty(Config.TOKEN)) {
			new AlertDialog.Builder(this)
					.setTitle("Warn")
					.setMessage("Need config API TOKEN")
					.setPositiveButton("Confirm",
							new DialogInterface.OnClickListener() {
								public void onClick(
										DialogInterface dialoginterface, int i) {
									//
									finish();
								}
							}).show();
			return;
		}

		final String payInfo = getPayInfo("http://www.nihaopay.com", "1", "USD", payType);

		Runnable payRunnable = new Runnable() {

			@Override
			public void run() {
				System.out.println("send info : "+ payInfo);
				// Create PayTask object
				NihaopayTask nhpTask = new NihaopayTask(PayDemoActivity.this);
				// call payment api, got payment result
				String result = nhpTask.pay(payInfo, Config.TOKEN, "https://api.nihaopay.com/v1.2/transactions/apppay");

				if(payType.equals("alipay")){
					Message msg = new Message();
					msg.what = SDK_CHECK_FLAG;
					msg.obj = result;
					mHandler.sendMessage(msg);
				}

				if(payType.equals("wechatpay")){
					Logger.info("返回API结果：" + result);
				}


			}
		};

		// Must asynchronously invoked
		Thread payThread = new Thread(payRunnable);
		payThread.start();
	}

	public void payAPS() {
		if (TextUtils.isEmpty(Config.TOKEN)) {
			new AlertDialog.Builder(this)
					.setTitle("Warn")
					.setMessage("Need config API TOKEN")
					.setPositiveButton("Confirm",
							new DialogInterface.OnClickListener() {
								public void onClick(
										DialogInterface dialoginterface, int i) {
									//
									finish();
								}
							}).show();
			return;
		}

		final String payInfo = getPayAPSInfo("http://www.nihaopay.com", "1", "USD");

		Runnable payRunnable = new Runnable() {

			@Override
			public void run() {
				System.out.println("send info : "+ payInfo);
				// Create PayTask object
				NihaopayTask nhpTask = new NihaopayTask(PayDemoActivity.this);
				// call payment api, got payment result
				String result = nhpTask.pay(payInfo, Config.TOKEN, "https://api.nihaopay.com/v1.2/transactions/aplus");

				Message msg = new Message();
				msg.what = SDK_CHECK_FLAG;
				msg.obj = result;
				mHandler.sendMessage(msg);

			}
		};

		// Must asynchronously invoked
		Thread payThread = new Thread(payRunnable);
		payThread.start();
	}

//	/**
//	 * check whether the device has authentication alipay account.
//	 * 查询终端设备是否存在支付宝认证账户
//	 *
//	 */
//	public void check(View v) {
//		Runnable checkRunnable = new Runnable() {
//
//			@Override
//			public void run() {
//				// 构造PayTask 对象
//				PayTask payTask = new PayTask(PayDemoActivity.this);
//				// 调用查询接口，获取查询结果
//				boolean isExist = payTask.checkAccountIfExist();
//
//				Message msg = new Message();
//				msg.what = SDK_CHECK_FLAG;
//				msg.obj = isExist;
//				mHandler.sendMessage(msg);
//			}
//		};
//
//		Thread checkThread = new Thread(checkRunnable);
//		checkThread.start();
//
//	}

//	/**
//	 * get the sdk version. 获取SDK版本号
//	 *
//	 */
//	public void getSDKVersion() {
//		PayTask payTask = new PayTask(this);
//		String version = payTask.getVersion();
//		Toast.makeText(this, version, Toast.LENGTH_SHORT).show();
//	}


	public void alipay(View v){
		pay(v, "alipay");
	}

	public void unionpay(View v){
		pay(v, "unionpay");
	}

	public void wechatpay(View v){
		pay(v, "wechatpay");
	}

	public void aps(View v) {
		payAPS();
	}

	/**
	 * Assemble pay information
	 *
	 * ipn_url: your Instant Payment Notification URL
	 * amount : payment amount
	 * currency: USD
	 *
	 */
	public String getPayInfo(String ipn_url,  String amount, String currency, String payType) {

		String orderInfo = "amount="  + amount ;

		orderInfo += "&currency=" +  currency ;

		// merchant order ID
		orderInfo += "&reference=" +  getOutTradeNo() ;

		// IPN url
		orderInfo += "&ipn_url=" +  ipn_url ;

		String note = "it is test";
		orderInfo += "&note=" +  note ;
		String description = "毛线裤两件";
		orderInfo += "&description=" +  description ;

		orderInfo += "&vendor=" +  payType;

		orderInfo += "&ostype=ANDROID";



		return orderInfo;
	}

	public String getPayAPSInfo(String ipn_url,  String amount, String currency) {

		String orderInfo = "amount="  + amount ;

		orderInfo += "&currency=" +  currency ;

		// merchant order ID
		orderInfo += "&reference=" +  getOutTradeNo() ;

		// IPN url
		orderInfo += "&ipn_url=" +  ipn_url ;

		String note = "it is test";
		orderInfo += "&note=" +  note ;
		String description = "毛线裤两件";
		orderInfo += "&description=" +  description ;

		orderInfo += "&terminal=APP";

		orderInfo += "&ostype=ANDROID";

		return orderInfo;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		/*************************************************
		 * 步骤3：处理银联手机支付控件返回的支付结果
		 ************************************************/
		NihaopayResult payResult = new NihaopayResult(requestCode, resultCode, data);

		String resultStatus = payResult.getStatus();

		// 判断resultStatus 为“success”则代表支付成功，具体状态码代表含义可参考接口文档
		if (TextUtils.equals(resultStatus, "success")) {
			Toast.makeText(PayDemoActivity.this, "支付成功",
					Toast.LENGTH_SHORT).show();
		} else {
			// 判断resultStatus 为非“success”则代表可能支付失败
			// “pending”代表支付结果因为支付渠道原因或者系统原因还在等待支付结果确认，最终交易是否成功以服务端异步通知为准（小概率状态）
			if (TextUtils.equals(resultStatus, "pending")) {
				Toast.makeText(PayDemoActivity.this, "支付结果确认中",
						Toast.LENGTH_SHORT).show();

			} else {
				// 其他值就可以判断为支付失败，包括用户主动取消支付，或者系统返回的错误
				Toast.makeText(PayDemoActivity.this, "支付失败",
						Toast.LENGTH_SHORT).show();

			}
		}
	}



	/**
	 * get Merchant Order ID.
	 *
	 */
	public static String getOutTradeNo() {
		SimpleDateFormat format = new SimpleDateFormat("MMddHHmmss",
				Locale.getDefault());
		Date date = new Date();
		String key = format.format(date);

		Random r = new Random();
		key = key + r.nextInt();
		key = key.substring(0, 15);
		return key;
	}


}
