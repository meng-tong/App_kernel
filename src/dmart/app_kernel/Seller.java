package dmart.app_kernel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import android.support.v7.app.ActionBarActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


public class Seller extends ActionBarActivity implements Handler.Callback {
	private String sellerTAG = "Seller_Kernel";
	
	private TextView textview = null;
	
	private Handler mHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_seller);
		
		textview = (TextView)findViewById(R.id.text_ip_seller);
		if (mHandler == null) {mHandler = new Handler(this);}
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
			final Intent intent = new Intent(Seller.this, Main.class);
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
		new Thread(new Runnable() {
            public void run() {
            	Runtime mRuntime = Runtime.getRuntime();
            	Process mProcess;
            	StringBuffer arpResBuff = null;
            	String ip="", dev="";
            	// loop until get p2p-p2p0 interface address
            	Log.d(sellerTAG, "Begin to Fetch Wifi Direct Client IP");
            	while(true) {
            		boolean p2pFlag = false;
	        		try {
	        			mProcess = mRuntime.exec("arp -n");
	        			BufferedReader arpReader = new BufferedReader(new InputStreamReader(mProcess.getInputStream()));
	        			arpResBuff = new StringBuffer();
	        			char[] mBuff = new char[1024];
	        			int length = 0;
	        			while((length=arpReader.read(mBuff)) != -1) {
	        				arpResBuff.append(mBuff, 0, length);
	        			}
	        			arpReader.close();
	        		} catch (IOException e) {
	        			Log.e(sellerTAG, "SELLER EXCECUTE ARP FAILED: " + e.toString());
	        		}
	        		
        			String[] arpItems = arpResBuff.toString().split("\n");
        			for(int i=0;i<arpItems.length;++i) {
        				if(arpItems[i].contains("p2p-p2p0")) {
        					int devIndex = arpItems[i].indexOf("p2p-p2p");
        					dev = arpItems[i].substring(devIndex);
        					
        					int gwIndex1 = arpItems[i].indexOf("(");
        					int gwIndex2 = arpItems[i].indexOf(")");
        					ip = arpItems[i].substring(gwIndex1+1, gwIndex2);
        					p2pFlag = true;
        					break;
        				}
        			}
        			if(p2pFlag) {break;}
            	}
            	Log.d(sellerTAG, "SELLER GET BUYER IP: " + ip);
            	Message msg = new Message();
                Bundle b = new Bundle();
                b.putString("message", ip);
                msg.setData(b);
                mHandler.sendMessage(msg);
            	
            	// block all the address and add the only whitelist
            	try {
            		// enable FORWARDING
            		mProcess = mRuntime.exec("su -c sysctl -w net.ipv4.ip_forward=1");
            		// su -c iptables -A FORWARD -s 192.168.49.1/24 -j DROP
					mProcess = mRuntime.exec("su -c iptables -A FORWARD -s " + ip + "/24 -j DROP");
					// su -c iptables -I FORWARD -s IP -j ACCEPT
					mProcess = mRuntime.exec("su -c iptables -I FORWARD -s " + ip + " -j ACCEPT");
				} catch (IOException e) {
					Log.e(sellerTAG, "SELLER (UN)BLOCK FAILED: " + e.toString());
				}
            	Log.d(sellerTAG, "SELLER UNBLOCK BUYER IP: " + ip);
            	msg = new Message();
                b = new Bundle();
                b.putString("message", ip+" unblocked");
                msg.setData(b);
                mHandler.sendMessage(msg);
                
                // add the NAT rules to relay packets from Buyer
                try {
                	// su -c iptables -t nat -I POSTROUTING -s 192.168.49.1/24 -o wlan0 -j MASQUERADE
                	mProcess = mRuntime.exec("su -c iptables -t nat -I POSTROUTING -s " + ip + "/24 -o wlan0 -j MASQUERADE");
                } catch (IOException e) {
                	Log.e(sellerTAG, "SELLER ADD NAT RULES FAILED: " + e.toString());
                }
                Log.d(sellerTAG, "SELLER SNAT BUYER IP: " + ip);
            	msg = new Message();
                b = new Bundle();
                b.putString("message", ip+" has source NAT");
                msg.setData(b);
                mHandler.sendMessage(msg);
            }
        }).start();
	}
	
	@Override
    public boolean handleMessage(Message message) {
    	if (message != null) {
    		Bundle b = message.getData();
    		String msg = b.getString("message");
    		textview.setText(msg);
        }
        return true;
    }
}
