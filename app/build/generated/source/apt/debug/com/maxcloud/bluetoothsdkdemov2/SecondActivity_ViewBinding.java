// Generated code from Butter Knife. Do not modify!
package com.maxcloud.bluetoothsdkdemov2;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import java.lang.IllegalStateException;
import java.lang.Override;

public class SecondActivity_ViewBinding implements Unbinder {
  private SecondActivity target;

  @UiThread
  public SecondActivity_ViewBinding(SecondActivity target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  public SecondActivity_ViewBinding(SecondActivity target, View source) {
    this.target = target;

    target.mDeviceList = Utils.findRequiredViewAsType(source, R.id.mDeviceList, "field 'mDeviceList'", ListView.class);
    target.mScanBtn = Utils.findRequiredViewAsType(source, R.id.mScanBtn, "field 'mScanBtn'", Button.class);
    target.mOpenBtn = Utils.findRequiredViewAsType(source, R.id.mOpenBtn, "field 'mOpenBtn'", Button.class);
    target.mProgressBar = Utils.findRequiredViewAsType(source, R.id.mProgress, "field 'mProgressBar'", ProgressBar.class);
    target.mRecord = Utils.findRequiredViewAsType(source, R.id.mRecord, "field 'mRecord'", TextView.class);
    target.mScrollView = Utils.findRequiredViewAsType(source, R.id.mScroll, "field 'mScrollView'", ScrollView.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    SecondActivity target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.mDeviceList = null;
    target.mScanBtn = null;
    target.mOpenBtn = null;
    target.mProgressBar = null;
    target.mRecord = null;
    target.mScrollView = null;
  }
}
