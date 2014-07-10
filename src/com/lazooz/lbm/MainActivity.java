package com.lazooz.lbm;




import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.lazooz.lbm.businessClasses.ServerData;
import com.lazooz.lbm.communications.ServerCom;
import com.lazooz.lbm.preference.MySharedPreferences;
import com.lazooz.lbm.utils.Utils;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends MyActionBarActivity {
	
	private Timer ShortPeriodTimer;
	private TextView mDistanceTV;
	private TextView mZoozBalTV;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//super.onCreate(savedInstanceState);
		super.onCreate(savedInstanceState, R.layout.activity_main);
		//setContentView(R.layout.activity_main);

		
		startService(new Intent(this, LbmService.class));
		
		mDistanceTV = (TextView)findViewById(R.id.main_distance_tv);
		mZoozBalTV = (TextView)findViewById(R.id.main_zoz_balance_tv);
		
		
		 final Handler guiHandler = new Handler();
		 final Runnable guiRunnable = new Runnable() {
		      public void run() {
		         UpdateGUI();
		      }
		   };
		
		
		ShortPeriodTimer = new Timer();
		TimerTask twoSecondsTimerTask = new TimerTask() {
				@Override
				public void run() {
					guiHandler.post(guiRunnable);				
				}
			};
		ShortPeriodTimer.scheduleAtFixedRate(twoSecondsTimerTask, 0, 10*1000);

		
		
	}

	protected void UpdateGUI() {
		ServerData sd = MySharedPreferences.getInstance().getServerData(this);
		mDistanceTV.setText(sd.getDistance());
		mZoozBalTV.setText(sd.getZoozBalance());
	
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		    if (requestCode == 1) {
		        if(resultCode == RESULT_OK){
		        	String fromActivity = data.getStringExtra("ACTIVITY");
		        	String s = MySharedPreferences.getInstance().getRecommendUserList(this).toString();
		            Log.e("aaa", s);
		            sendFriendRecommendToServerAsync(s);
		        }
		        if (resultCode == RESULT_CANCELED) {
		            //Write your code if there's no result
		        }
		    }
	}

	
	
	
	
	
	
	private void sendFriendRecommendToServerAsync(String data){
		FriendRecommendToServer friendRecommendToServer = new FriendRecommendToServer();
		friendRecommendToServer.execute(data);
	}
	
	private class FriendRecommendToServer extends AsyncTask<String, Void, String> {


		@Override
		protected String doInBackground(String... params) {
			
          	ServerCom bServerCom = new ServerCom(MainActivity.this);
        	
              
        	JSONObject jsonReturnObj=null;
			try {
				MySharedPreferences msp = MySharedPreferences.getInstance();
				
				JSONArray dataList = msp.getRecommendUserList(MainActivity.this);
				
				bServerCom.setFriendRecommend(msp.getUserId(MainActivity.this), msp.getUserSecret(MainActivity.this), dataList.toString());
				jsonReturnObj = bServerCom.getReturnObject();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
        	
        	String serverMessage = "";
	
			try {
				if (jsonReturnObj == null)
					serverMessage = "ConnectionError";
				else {
					serverMessage = jsonReturnObj.getString("message");
					if (serverMessage.equals("success")){
						
					}
				}
			} 
			catch (JSONException e) {
				e.printStackTrace();
				serverMessage = "GeneralError";
			}
			
			
			return serverMessage;
		}
		
		@Override
		protected void onPostExecute(String result) {
			
			if (result.equals("success")){
				Toast.makeText(MainActivity.this, "Friend List Sent", Toast.LENGTH_LONG).show();
			}
		}
			
		
		@Override
		protected void onPreExecute() {
			
		}
	}
	
}
