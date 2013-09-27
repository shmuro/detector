package com.rb.server;

import static com.rb.server.CommonUtilities.DISPLAY_MESSAGE_ACTION;
import static com.rb.server.CommonUtilities.EXTRA_MESSAGE;
import static com.rb.server.CommonUtilities.IN_REGISTRATION_ID;
import static com.rb.server.CommonUtilities.SENDER_ID;
import static com.rb.server.CommonUtilities.SERVER_URL;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gcm.GCMRegistrar;

/**
 * Main UI for the demo app.
 */
public class DemoActivity extends Activity implements OnClickListener {

	TextView mDisplay;
	EditText inputMessage;
	Button buttonSend;
	AsyncTask<Void, Void, Void> mRegisterTask;

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.buttonSend:
			final String message = inputMessage.getText().toString();
			if (!TextUtils.isEmpty(message)) {
				inputMessage.getText().clear();
				new Thread(new Runnable() {

					@Override
					public void run() {
						String registrationId = GCMRegistrar
								.getRegistrationId(DemoActivity.this);
						ServerUtilities.sendMessage(message, registrationId);
					}
				}).start();
			} else {
				Toast.makeText(this, "Message is empty", Toast.LENGTH_SHORT)
						.show();
			}
			break;

		default:
			break;
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		checkNotNull(SERVER_URL, "SERVER_URL");
		checkNotNull(SENDER_ID, "695223804692");
		// Make sure the device has the proper dependencies.
		GCMRegistrar.checkDevice(this);
		// Make sure the manifest was properly set - comment out this line
		// while developing the app, then uncomment it when it's ready.
		GCMRegistrar.checkManifest(this);
		setContentView(R.layout.main);
		mDisplay = (TextView) findViewById(R.id.display);
		inputMessage = (EditText) findViewById(R.id.inputMessage);
		buttonSend = (Button) findViewById(R.id.buttonSend);
		buttonSend.setOnClickListener(this);

		registerReceiver(mHandleMessageReceiver, new IntentFilter(
				DISPLAY_MESSAGE_ACTION));
		final String regId = GCMRegistrar.getRegistrationId(this);
		if (regId.equals("")) {
			// Automatically registers application on startup.
			GCMRegistrar.register(this, SENDER_ID);
		} else {
			// Device is already registered on GCM, needs to check if it is
			// registered on our server as well.
			if (GCMRegistrar.isRegisteredOnServer(this)) {
				// Skips registration.
				mDisplay.append(getString(R.string.already_registered) + "\n");
			} else {
				// Try to register again, but not in the UI thread.
				// It's also necessary to cancel the thread onDestroy(),
				// hence the use of AsyncTask instead of a raw thread.
				final Context context = this;
				mRegisterTask = new AsyncTask<Void, Void, Void>() {

					@Override
					protected Void doInBackground(Void... params) {
						boolean registered = ServerUtilities.register(context,
								regId);
						// At this point all attempts to register with the app
						// server failed, so we need to unregister the device
						// from GCM - the app will try to register again when
						// it is restarted. Note that GCM will send an
						// unregistered callback upon completion, but
						// GCMIntentService.onUnregistered() will ignore it.
						if (!registered) {
							GCMRegistrar.unregister(context);
						}
						return null;
					}

					@Override
					protected void onPostExecute(Void result) {
						mRegisterTask = null;
					}

				};
				mRegisterTask.execute(null, null, null);
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.options_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		/*
		 * Typically, an application registers automatically, so options below
		 * are disabled. Uncomment them if you want to manually register or
		 * unregister the device (you will also need to uncomment the equivalent
		 * options on options_menu.xml).
		 */
	  /*case R.id.options_register:
		GCMRegistrar.register(this, SENDER_ID);
			return true;
		case R.id.options_unregister:
			GCMRegistrar.unregister(this);
			return true;*/
		case R.id.options_clear:
			mDisplay.setText(null);
			return true;
		case R.id.options_exit:
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onDestroy() {
		if (mRegisterTask != null) {
			mRegisterTask.cancel(true);
		}
		unregisterReceiver(mHandleMessageReceiver);
		GCMRegistrar.onDestroy(this);
		super.onDestroy();
	}

	private void checkNotNull(Object reference, String name) {
		if (reference == null) {
			throw new NullPointerException(getString(R.string.error_config,
					name));
		}
	}

	private final BroadcastReceiver mHandleMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle extras = intent.getExtras();
			String newMessage = extras.getString(EXTRA_MESSAGE);

			String registrationId = GCMRegistrar
					.getRegistrationId(DemoActivity.this);
			String inRegistrationId = extras.getString(IN_REGISTRATION_ID);
			if (!TextUtils.isEmpty(registrationId) && !TextUtils.isEmpty(inRegistrationId)) {
				String user = inRegistrationId.substring(inRegistrationId.length() - 5, inRegistrationId.length()) + ": ";
				if (registrationId.equals(inRegistrationId)) {
					user = "me: ";
				}
				newMessage = user + newMessage;
			}
			mDisplay.append(newMessage + "\n");
		}
	};
}