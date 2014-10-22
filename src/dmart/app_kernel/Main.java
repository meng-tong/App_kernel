package dmart.app_kernel;

import dmart.app_kernel.Buyer;
import dmart.app_kernel.Main;
import dmart.app_kernel.Seller;
import android.support.v7.app.ActionBarActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class Main extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return true;
	}
	
	public void onClick1(View view) {
		final Intent intent = new Intent(Main.this, Seller.class);
		new Thread(new Runnable() {
            public void run() {
            	startActivity(intent);
        		finish();
            }
        }).start();
	}
	
	public void onClick2(View view) {
		final Intent intent = new Intent(Main.this, Buyer.class);
		new Thread(new Runnable() {
            public void run() {
        		startActivity(intent);
        		finish();
            }
        }).start();
	}
}
