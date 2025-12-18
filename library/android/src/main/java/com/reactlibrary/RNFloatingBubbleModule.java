package com.pelumi_coder.floatingbubble;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.*;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.txusballesteros.bubbles.*;

public class RNFloatingBubbleModule extends ReactContextBaseJavaModule
  implements LifecycleEventListener {

  private BubblesManager bubblesManager;
  private BubbleLayout bubbleView;
  private final ReactApplicationContext reactContext;
  private boolean isHostResumed = false;

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

  // ================= PUBLIC API =================

  @ReactMethod
  public void initialize(Promise promise) {
    Activity activity = getCurrentActivity();
    if (activity == null) {
      promise.reject("NO_ACTIVITY", "Activity not ready");
      return;
    }

    if (bubblesManager != null) {
      promise.resolve(null);
      return;
    }

    bubblesManager = new BubblesManager.Builder(activity)
      .setTrashLayout(R.layout.bubble_trash_layout)
      .setInitializationCallback(() -> {})
      .build();

    bubblesManager.initialize();
    promise.resolve(null);
  }

  @ReactMethod
  public void showFloatingBubble(int x, int y, Promise promise) {
    if (!isHostResumed) {
      promise.reject("APP_NOT_FOREGROUND", "App is not in foreground");
      return;
    }

    Activity activity = getCurrentActivity();
    if (activity == null || bubblesManager == null) {
      promise.reject("NOT_READY", "Activity or manager not ready");
      return;
    }

    removeBubble();

    bubbleView = (BubbleLayout) LayoutInflater
      .from(activity)
      .inflate(R.layout.bubble_layout, null);

    bubbleView.setShouldStickToWall(true);

    bubbleView.setOnBubbleClickListener(bubble ->
      sendEvent("floating-bubble-press")
    );

    bubbleView.setOnBubbleRemoveListener(bubble ->
      sendEvent("floating-bubble-remove")
    );

    bubblesManager.addBubble(bubbleView, x, y);
    promise.resolve(null);
  }

  @ReactMethod
  public void hideFloatingBubble(Promise promise) {
    removeBubble();
    promise.resolve(null);
  }

  @ReactMethod
  public void requestPermission(Promise promise) {
    if (hasPermission()) {
      promise.resolve(true);
      return;
    }

    Activity activity = getCurrentActivity();
    if (activity == null) {
      promise.reject("NO_ACTIVITY");
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
  public void checkPermission(Promise promise) {
    promise.resolve(hasPermission());
  }

  // ================= LIFECYCLE =================

  @Override
  public void onHostResume() {
    isHostResumed = true;
  }

  @Override
  public void onHostPause() {
    isHostResumed = false;
    removeBubble(); // ⚠️ CRUCIAL
  }

  @Override
  public void onHostDestroy() {
    isHostResumed = false;
    removeBubble();
  }

  // ================= INTERNAL =================

  private void removeBubble() {
    if (bubbleView != null && bubblesManager != null) {
      try {
        bubblesManager.removeBubble(bubbleView);
      } catch (Exception ignored) {}
      bubbleView = null;
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
}
