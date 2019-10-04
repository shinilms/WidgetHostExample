package com.lgfischer.widgethost;

import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * This activity serves as an example of how to search, add and remove widgets
 * from a window.
 *
 * @author Leonardo Garcia Fischer (http://coderender.blogspot.com/)
 *
 */
public class WidgetHostActivity extends AppCompatActivity {
	private static final String TAG = "WidgetHostActivity";
	private static final int APPWIDGET_HOST_ID = 100;
	private static final int REQUEST_PICK_APPWIDGET = 1;
	private static final int REQUEST_CREATE_APPWIDGET = 2;

	private AppWidgetManager mAppWidgetManager;
	private AppWidgetHost mAppWidgetHost;

	private ViewGroup mainLayout;

	/**
	 * Called on the creation of the activity.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_widget_host);

		mainLayout = findViewById(R.id.main_layout);

		mAppWidgetManager = AppWidgetManager.getInstance(this);
		mAppWidgetHost = new AppWidgetHost(this, APPWIDGET_HOST_ID);
	}

	/**
	 * Launches the menu to select the widget. The selected widget will be on
	 * the result of the activity.
	 */
	void selectWidget() {
		int appWidgetId = this.mAppWidgetHost.allocateAppWidgetId();
		Intent pickIntent = new Intent(AppWidgetManager.ACTION_APPWIDGET_PICK);
		pickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
		startActivityForResult(pickIntent, REQUEST_PICK_APPWIDGET);
	}

	/**
	 * If the user has selected an widget, the result will be in the 'data' when
	 * this function is called.
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			if (requestCode == REQUEST_PICK_APPWIDGET) {
				if (data != null) {
					configureWidget(data);
				}
			} else if (requestCode == REQUEST_CREATE_APPWIDGET) {
				if (data != null) {
					createWidget(data);
				}
			}
		} else if (resultCode == RESULT_CANCELED && data != null) {
			int appWidgetId = data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
			if (appWidgetId != -1) {
				mAppWidgetHost.deleteAppWidgetId(appWidgetId);
			}
		}
	}

	/**
	 * Checks if the widget needs any configuration. If it needs, launches the
	 * configuration activity.
	 */
	private void configureWidget(Intent data) {
		Bundle extras = data.getExtras();
		int appWidgetId = 0;
		if (extras != null) {
			appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
		}
		AppWidgetProviderInfo appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(appWidgetId);
		if (appWidgetInfo.configure != null) {
			Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
			intent.setComponent(appWidgetInfo.configure);
			intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
			startActivityForResult(intent, REQUEST_CREATE_APPWIDGET);
		} else {
			createWidget(data);
		}
	}

	/**
	 * Creates the widget and adds to our view layout.
	 */
	public void createWidget(Intent data) {
		Bundle extras = data.getExtras();
		int appWidgetId = 0;
		if (extras != null) {
			appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
		}
		AppWidgetProviderInfo appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(appWidgetId);

		AppWidgetHostView hostView = mAppWidgetHost.createView(this, appWidgetId, appWidgetInfo);
		hostView.setAppWidget(appWidgetId, appWidgetInfo);
		mainLayout.addView(hostView);

		Log.i(TAG, "The widget size is: " + appWidgetInfo.minWidth + "*" + appWidgetInfo.minHeight);
	}

	/**
	 * Registers the AppWidgetHost to listen for updates to any widgets this app
	 * has.
	 */
	@Override
	protected void onStart() {
		super.onStart();
		mAppWidgetHost.startListening();
	}

	/**
	 * Stop listen for updates for our widgets (saving battery).
	 */
	@Override
	protected void onStop() {
		mAppWidgetHost.stopListening();
		super.onStop();
	}

	/**
	 * Removes the widget displayed by this AppWidgetHostView.
	 */
	public void removeWidget(AppWidgetHostView hostView) {
		mAppWidgetHost.deleteAppWidgetId(hostView.getAppWidgetId());
		mainLayout.removeView(hostView);
	}

	/**
	 * Handles the menu.
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.i(TAG, "Menu selected: " + item.getTitle() + " / " + item.getItemId() + " / " + R.id.addWidget);
		switch (item.getItemId()) {
			case R.id.addWidget:
				selectWidget();
				return true;
			case R.id.removeWidget:
				removeWidgetMenuSelected();
				return false;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Handle the 'Remove Widget' menu.
	 */
	public void removeWidgetMenuSelected() {
		int childCount = mainLayout.getChildCount();
		if (childCount > 1) {
			View view = mainLayout.getChildAt(childCount - 1);
			if (view instanceof AppWidgetHostView) {
				removeWidget((AppWidgetHostView) view);
				Toast.makeText(this, R.string.widget_removed_popup, Toast.LENGTH_SHORT).show();
				return;
			}
		}
		Toast.makeText(this, R.string.no_widgets_popup, Toast.LENGTH_SHORT).show();
	}

	/**
	 * Creates the menu with options to add and remove widgets.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.widget_menu, menu);
		return true;
	}
}