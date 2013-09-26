package com.rb.server;

import android.content.Context;
import android.content.Intent;

/**
 * Helper class providing methods and constants common to other classes in the
 * app.
 */
public final class CommonUtilities {

	/**
	 * Base URL of the Demo Server (such as http://my_host:8080/api)
	 */
	static final String SERVER_URL = "http://detectorapi.appspot.com";

	/**
	 * Google API project id registered to use GCM.
	 */
	static final String SENDER_ID = "979671037382";

	/**
	 * Tag used on log messages.
	 */
	static final String TAG = "GCM";

	/**
	 * Intent used to display a message in the screen.
	 */
	static final String DISPLAY_MESSAGE_ACTION = "com.rb.server.DISPLAY_MESSAGE";

	/**
	 * Intent's extra that contains the message to be displayed.
	 */
	static final String EXTRA_MESSAGE = "message";
	
	static final String IN_REGISTRATION_ID = "inregid";
	

	/**
	 * Notifies UI to display a message.
	 * <p>
	 * This method is defined in the common helper because it's used both by the
	 * UI and the background service.
	 * 
	 * @param context
	 *            application's context.
	 * @param message
	 *            message to be displayed.
	 */
	static void displayMessage(Context context, String message, String inRegistrationId) {
		Intent intent = new Intent(DISPLAY_MESSAGE_ACTION);
		intent.putExtra(EXTRA_MESSAGE, message);
		if (inRegistrationId != null)
			intent.putExtra(IN_REGISTRATION_ID, inRegistrationId);
		context.sendBroadcast(intent);
	}
	
	static void displayMessage(Context context, String message){
		displayMessage(context, message, null);
	}
}
