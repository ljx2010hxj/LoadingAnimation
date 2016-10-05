package com.ljx2010hxj.loadinganim;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;


public class LoadingAnimMainActivity extends Activity implements OnClickListener{
	private static final String TAG = "LoadingAnimMainActivity";
	private ImageView loadingImg;
	private TextView loadingTxt;
	private WifiManager wm;
	private ConnectivityManager cm;
	private ObjectAnimator anima;
	private static final int UPDATE_WIFI_MSG = 1;
	private WifiStateChangedRev mWifiStatechangedRev;
	

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        
        loadingImg = (ImageView)findViewById(R.id.img);
        if(getWifiOpenState()){
        	loadingImg.setImageResource(R.drawable.ic_ts_wifi_on);
        }else {
        	loadingImg.setImageResource(R.drawable.ic_ts_wifi_off);
        }
        loadingImg.setOnClickListener(this);
        
        mWifiStatechangedRev = new WifiStateChangedRev();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        registerReceiver(mWifiStatechangedRev, intentFilter);
    }
    
    private void startAnim(ImageView image){
    	anima = ObjectAnimator.ofFloat(image, "rotation", 0f, 360f);
    	LinearInterpolator interpolator = new LinearInterpolator();
    	anima.setInterpolator(interpolator);
    	anima.setDuration(1000);//1500
    	anima.setRepeatMode(Animation.RESTART);
    	anima.setRepeatCount(Animation.INFINITE);
    	anima.start();
    }
    
    private void stopAnim(){
    	if(anima != null && (anima.isRunning())){
    		anima.cancel();
    		anima = null;
    	}
    }
    
    public void setWifiEnabled(final boolean enabled) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... args) {
            	wm.setWifiEnabled(enabled);
                return null;
            }

        }.execute();
    }
    
    private boolean getWifiOpenState(){
    	return wm.isWifiEnabled();
    }
    
    private boolean getWifiConnectState(){
    	State wifiState = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
    	if(wifiState == State.CONNECTED){
    		return true;
    	}
    	return false;
    }
    
    private Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch(msg.what){
			case UPDATE_WIFI_MSG:
				if(getWifiOpenState()){
					if(anima != null && anima.isRunning()){
						stopAnim();
					}
					loadingImg.setImageResource(R.drawable.ic_ts_wifi_on);
				}else {
					loadingImg.setImageResource(R.drawable.loading);
					startAnim(loadingImg);
				}
				break;
			}
		}
    	
    };

	@Override
	public void onClick(View v) {
       int id = v.getId();
       switch(id){
       case R.id.img:
    	   if(getWifiOpenState()){
				setWifiEnabled(false);
			}else {
				setWifiEnabled(true);
			}
    	   break;
           default:
        	   Log.d(TAG, "can not find id");
    		   
       }
    	   
	}

	@Override
	protected void onStop() {
		super.onStop();
		if(anima != null && anima.isRunning()){
			stopAnim();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mWifiStatechangedRev);
	}
	
	private class WifiStateChangedRev extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if(WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)){
			  int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
			  switch(wifiState){
			  case WifiManager.WIFI_STATE_DISABLED:
			  case WifiManager.WIFI_STATE_DISABLING:
				  if(anima != null && anima.isRunning()){
					  stopAnim();
				  }
				  loadingImg.setImageResource(R.drawable.ic_ts_wifi_off);
			      break;
			  case WifiManager.WIFI_STATE_ENABLED:
				  if(anima != null && anima.isRunning()){
					  stopAnim();
				  }
				  //Reset ImageView rotation.
				  loadingImg.setRotation(0.0f);
				  loadingImg.setImageResource(R.drawable.ic_ts_wifi_on);
				  break;
			  case WifiManager.WIFI_STATE_ENABLING:
				  loadingImg.setImageResource(R.drawable.loading);
				  startAnim(loadingImg);
				  break;
			  }
			}
		}
		
	}

}
