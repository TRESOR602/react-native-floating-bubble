package com.pelumi_coder.floatingbubble;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.*;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.txusballesteros.bubbles.*;

private boolean isHostResumed = false;
public class RNFloatingBubbleModule
  extends ReactContextBaseJavaModule
  implements LifecycleEventListener {

  private BubblesManager bubblesManager;
  private BubbleLayout bubbleView;
  private final ReactApplicationContext reactContext;

  public RNFloatingBubbleModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
    reactContext.addLifecycleEventListener(this);
  }

  @NonNull
  @Override
  public String getName() {
    return "RNFloatingBubble";
  }

  // ---------- PUBLIC API ----------

  @ReactMethod
  public void initialize(final Promise promise) {
    Activity activity = getCurrentActivity();

    if (activity == null) {
      promise.reject("NO_ACTIVITY", "Activity not ready");
      return;
    }

    try {
      initializeBubblesManager(activity);
      promise.resolve(null);
    } catch (Exception e) {
      promise.reject("INIT_ERROR", e);
    }
  }

  @ReactMethod
public void showFloatingBubble(int x, int y, final Promise promise) {

  if (!isHostResumed) {
    promise.reject("APP_NOT_ACTIVE", "App is not in foreground");
    return;
  }

  Activity activity = getCurrentActivity();
  if (activity == null || bubblesManager == null) {
    promise.reject("NOT_READY", "Activity or manager not ready");
    return;
  }

  try {
    addNewBubble(activity, x, y);
    promise.resolve(null);
  } catch (Exception e) {
    promise.reject("SHOW_ERROR", e);
  }
}

  @ReactMethod
  public void hideFloatingBubble(final Promise promise) {
    try {
      removeBubble();
      promise.resolve(null);
    } catch (Exception e) {/home/tresor/react-native-floating-bubble/library/android/src/main/java/com/reactlibrary/RNFloatingBubbleModule.java
      promise.reject("HIDE_ERROR", e);
    }
  }

  @ReactMethod
  public void requestPermission(final Promise promise) {
    if (hasPermission()) {
      promise.resolve(true);
      return;
    }

    Activity activity = getCurrentActivity();
    if (activity == null) {
      promise.reject("NO_ACTIVITY", "Activity not ready");
      return;
    }

    Intent intent = new Intent(
      Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
      Uri.parse("package:" + reactContext.getPackageName())
    );
    activity.startActivity(intent);
    promise.resolve(false);
  }

  @ReactMethod
  public void checkPermission(final Promise promise) {
    promise.resolve(hasPermission());
  }

  @ReactMethod
  public void reopenApp() {
    Intent launchIntent = reactContext
      .getPackageManager()
      .getLaunchIntentForPackage(reactContext.getPackageName());

    if (launchIntent != null) {
      launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      reactContext.startActivity(launchIntent);
    }
  }

  // ---------- INTERNAL ----------

  private void initializeBubblesManager(Activity activity) {
    if (bubblesManager != null) return;

    bubblesManager = new BubblesManager.Builder(activity)
      .setTrashLayout(R.layout.bubble_trash_layout)
      .setInitializationCallback(() -> {})
      .build();

    bubblesManager.initialize();
  }

  private void addNewBubble(Activity activity, int x, int y) {
    removeBubble();

    bubbleView = (BubbleLayout) LayoutInflater
      .from(activity)
      .inflate(R.layout.bubble_layout, null);

    bubbleView.setShouldStickToWall(true);

    bubbleView.setOnBubbleRemoveListener(bubble -> {
      bubbleView = null;
      sendEvent("floating-bubble-remove");
    });

    bubbleView.setOnBubbleClickListener(bubble ->
      sendEvent("floating-bubble-press")
    );

    bubblesManager.addBubble(bubbleView, x, y);
  }

  private void removeBubble() {
    if (bubbleView != null && bubblesManager != null) {
      try {
        bubblesManager.removeBubble(bubbleView);
        bubbleView = null;
      } catch (Exception ignored) {}
    }
  }

  private boolean hasPermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      return Settings.canDrawOverlays(reactContext);
    }
    return true;
  }

  private void sendEvent(String eventName) {
    reactContext
      .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
      .emit(eventName, null);
  }

  @Override
  public void onHostResume() {
    isHostResumed = true;
  }

  @Override
  public void onHostPause() {
    isHostResumed = false;
    removeBubble(); // TRÈS IMPORTANT
  }

  @Override
  public void onHostDestroy() {
    isHostResumed = false;
    removeBubble();
  }

}
