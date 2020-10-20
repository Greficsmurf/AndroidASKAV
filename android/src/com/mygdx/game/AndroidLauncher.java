package com.mygdx.game;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;



public class AndroidLauncher extends AndroidApplication implements app.androidIntent {
	public Float data = 0f;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		app App = new app();
		App.setCallback(this);
		initialize(App, config);
	}
	@Override
	public void start() {
        startService(new Intent(getBaseContext(), NotificationService.class));
	}
	@Override
	public void modeSwitch(){
		NotificationService.isChecked = !NotificationService.isChecked;
		if(NotificationService.isChecked)
			runOnUiThread(new Runnable() {
				public void run() {
					Toast.makeText(AndroidLauncher.this, "Сигнализация включена", Toast.LENGTH_SHORT).show();
				}
			});
		else if(!NotificationService.isChecked){
			runOnUiThread(new Runnable() {
				public void run() {
					Toast.makeText(AndroidLauncher.this, "Сигнализация выключена", Toast.LENGTH_SHORT).show();

				}
			});
		}


	}

}
