package dmart.app_kernel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import android.support.v7.app.ActionBarActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


public class Buyer extends ActionBarActivity {
	private String buyerTAG = "Buyer";
	
	private TextView textview = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_buyer);
		
		textview = (TextView)findViewById(R.id.text_ip);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, 1, 1, "Back");
		menu.add(Menu.NONE, 2, 2, "Quit");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		switch(id) {
			case 1:
				final Intent intent = new Intent(Buyer.this, Main.class);
				new Thread(new Runnable() {
		            public void run() {
		            	startActivity(intent);
		        		finish();
		            }
		        }).start();
				break;
			case 2:
				//TODO: add Quit operations
				break;
		}
		return true;
	}
	
	@Override
	public boolean onKeyDown(int KeyCode, KeyEvent event) {
		switch(KeyCode) {
		case KeyEvent.KEYCODE_BACK:
			moveTaskToBack(true);
		case KeyEvent.KEYCODE_HOME:
			moveTaskToBack(true);
		case KeyEvent.KEYCODE_MENU:
			break;
		}
		return super.onKeyDown(KeyCode, event);
	}
	
	public void onClick1(View view) {
		Runtime mRuntime = Runtime.getRuntime();
		String gw = "";
		String dev = "";
		boolean p2pFlag = false;
		
		Process mProcess;
		try {
			mProcess = mRuntime.exec("arp");
			BufferedReader arpReader = new BufferedReader(new InputStreamReader(mProcess.getInputStream()));
			StringBuffer arpResBuff = new StringBuffer();
			char[] mBuff = new char[1024];
			int length = 0;
			while((length=arpReader.read(mBuff)) != -1) {
				arpResBuff.append(mBuff, 0, length);
			}
			arpReader.close();
			
			String[] arpItems = arpResBuff.toString().split("\n");
			for(int i=0;i<arpItems.length;++i) {
				if(arpItems[i].contains("p2p-p2p0")) {
					int devIndex = arpItems[i].indexOf("p2p-p2p");
					dev = arpItems[i].substring(devIndex);
					
					int gwIndex1 = arpItems[i].indexOf("(");
					int gwIndex2 = arpItems[i].indexOf(")");
					gw = arpItems[i].substring(gwIndex1+1, gwIndex2);
					p2pFlag = true;
					break;
				}
			}
		} catch (IOException e) {
			Log.e(buyerTAG, "EXECUTE ARP FAILED: " + e.toString());
		}
		
		if(p2pFlag) {
			Toast.makeText(this, gw + " at " + dev, Toast.LENGTH_LONG).show();
			
			//su -c route add default gw GW dev DEV
			String routeCmd = "su -c route add default gw " + gw + " dev " + dev;
			try {
				mProcess = mRuntime.exec(routeCmd);
			} catch (IOException e) {
				Log.e(buyerTAG, "ADD DEFAULT ROUTE FAILED: " + e.toString());
			}
			
			Toast.makeText(this, "Add default route Succeed", Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(this, "NO WIFI DIRECT CONNECTION!", Toast.LENGTH_LONG).show();
		}
	}
}
