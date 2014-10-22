package dmart.app_kernel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.support.v7.app.ActionBarActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;


public class Seller extends ActionBarActivity implements Handler.Callback {
	private String sellerTAG = "Seller_Kernel";
	
	private ArrayList<String> ipList = null;
	private ArrayList<Double> accountList = null;
	private ArrayList<String> ipWithAccountList = null;
	private ArrayAdapter<String> listAdapter = null;
	private ListView listview = null;
	
	private Handler mHandler;

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
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_seller);
		
		ipList = new ArrayList<String>();
		accountList = new ArrayList<Double>();
		ipWithAccountList = new ArrayList<String>();
		
		listAdapter = new ArrayAdapter<String>(this,
					android.R.layout.simple_expandable_list_item_1, ipWithAccountList);
		listview = (ListView)findViewById(R.id.list_ip_seller);
		listview.setAdapter(listAdapter);
		listviewOnItemLongClick();
		
		if (mHandler == null) {mHandler = new Handler(this);}
	}
	
	private void listviewOnItemLongClick() {
		listview.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
			@Override
			public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
				menu.add(Menu.NONE, 1, 1, "Delete this Buyer");
				menu.add(Menu.NONE, 2, 2, "Cancel");
			}
			
		});
	}
	
	public boolean onContextItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case 1:
			AdapterView.AdapterContextMenuInfo menuInfo;
			menuInfo = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
			int pos = menuInfo.position;
			String ip = ipList.get(pos);
			Log.d(sellerTAG, "Seller remove Buyer (" + ipList.get(pos) + ")");
			// first remove the buyer from whitelist
			try {
				Runtime mRuntime = Runtime.getRuntime();
        		Process mProcess = mRuntime.exec("su -c iptables -D FORWARD -s " + ip + " -j ACCEPT");
        		mProcess = mRuntime.exec("su -c iptables -D FORWARD -s " + ip + "/24 -j DROP");
        		mProcess = mRuntime.exec("su -c iptables -t nat -D POSTROUTING -s " + ip + " -o wlan0 -j MASQUERADE");
        		Toast.makeText(this, "Dis-Connect with Buyer " + ip, Toast.LENGTH_SHORT).show();
			} catch (IOException e) {
				Log.e(sellerTAG, "Seller Delete Buyer Failed: " + e.toString());
			}
			// then remove the buyer item from ListView
			ipList.remove(pos); accountList.remove(pos);
			ipWithAccountList.remove(pos);
			listAdapter.notifyDataSetInvalidated();
        	break;
		case 2:
			break;
		}
		return true;
	}
	
	public void onClick1(View view) {
		new Thread(new Runnable() {
            public void run() {
            	Runtime mRuntime = Runtime.getRuntime();
            	Process mProcess;
            	StringBuffer arpResBuff = null;
            	StringBuffer accountResBuff = null;
            	String ip="", dev="";
            	// loop until get p2p-p2p0 interface address
            	Log.d(sellerTAG, "Begin to Fetch Wifi Direct Client IP");

            	// find connected Buyer's IP
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
            	
            	// collect accounting information
            	try {
        			Process nProcess = mRuntime.exec("su -c iptables -L FORWARD -n -v -x");
        			BufferedReader accountReader = new BufferedReader(new InputStreamReader(nProcess.getInputStream()));
        			accountResBuff = new StringBuffer();
        			char[] mBuff = new char[1024];
        			int length = 0;
        			while((length=accountReader.read(mBuff)) != -1) {
        				accountResBuff.append(mBuff, 0, length);
        			}
        			accountReader.close();
        		} catch (IOException e) {
        			Log.e(sellerTAG, "SELLER EXCECUTE ARP FAILED: " + e.toString());
        		}
        		
            	String[] arpItems = arpResBuff.toString().split("\n");
            	String[] accountItems = accountResBuff.toString().split("\n");
    			for(int i=0;i<arpItems.length;++i) {
    				if(arpItems[i].contains("p2p-p2p0")) {
    					int devIndex = arpItems[i].indexOf("p2p-p2p");
    					dev = arpItems[i].substring(devIndex);
    					
    					int gwIndex1 = arpItems[i].indexOf("(");
    					int gwIndex2 = arpItems[i].indexOf(")");
    					ip = arpItems[i].substring(gwIndex1+1, gwIndex2);
    					
    					if(ipList.contains(ip)) {
    						// an existed Buyer, update the traffic account
    						Log.d(sellerTAG, "SELLER UPDATE BUYER ACCOUNT: " + ip);
    						
    						for(int j=0;j<accountItems.length;++j) {
    							if(accountItems[j].contains(ip)) {
    								String[] items = accountItems[j].split(" ");
    								int count = 0;
    								for(int k=0;k<items.length;++k) {
    									if(items[k].length() == 0) {continue;}
    									else {
    										count += 1;
    										if(count == 2) {
    											Log.d(sellerTAG, "Bytes: " + items[k]);
    											int ipIndex = ipList.indexOf(ip);
    											accountList.set(ipIndex, Double.parseDouble(items[k]));
    											ipWithAccountList.set(ipIndex, ip + "\t " + accountList.get(ipIndex) + " Bytes");
    											break;
    										}
    									}
    								}
    								break;
								}
    						}
    					} else {
    						// a new Buyer is connected
    						ipList.add(ip);
    						accountList.add(0.0);
    						ipWithAccountList.add(ip + "\t 0.0 Bytes");
    						Log.d(sellerTAG, "SELLER GET NEW BUYER IP: " + ip);
    						try {
    		            		// enable FORWARDING
    		            		mProcess = mRuntime.exec("su -c sysctl -w net.ipv4.ip_forward=1");
    		            		// block all IP: su -c iptables -A FORWARD -s 192.168.49.1/24 -j DROP
    							mProcess = mRuntime.exec("su -c iptables -A FORWARD -s " + ip + "/24 -j DROP");
    							// add whitelist: su -c iptables -I FORWARD -s IP -j ACCEPT
    							mProcess = mRuntime.exec("su -c iptables -I FORWARD -s " + ip + " -j ACCEPT");
    							// add NAT rule: su -c iptables -t nat -I POSTROUTING -s 192.168.49.1/24 -o wlan0 -j MASQUERADE
    		                	mProcess = mRuntime.exec("su -c iptables -t nat -I POSTROUTING -s " + ip + " -o wlan0 -j MASQUERADE");
    						} catch (IOException e) {
    							Log.e(sellerTAG, "SELLER ADD BUYER: " + e.toString());
    						}
    					}
    				}
    			}
    			
                Message msg = new Message();
                Bundle b = new Bundle();
                b.putString("message", "update");
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
    		listAdapter.notifyDataSetChanged();
    		Toast.makeText(this, "Buyer Accounts have been Updated!", Toast.LENGTH_LONG).show();
        }
        return true;
    }
}
