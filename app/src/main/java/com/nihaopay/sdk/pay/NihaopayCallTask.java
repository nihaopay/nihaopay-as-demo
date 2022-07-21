package com.nihaopay.sdk.pay;

import android.app.Activity;

import com.alipay.sdk.app.PayTask;

public class NihaopayCallTask {

	private PayTask payTask;
	
	public NihaopayCallTask(Activity fragmentActivity){
		payTask = new PayTask(fragmentActivity);
	}
	
	public String pay(String payInfo){
		
		String result = payTask.pay(payInfo, true);
		return result;
	}
	
}


