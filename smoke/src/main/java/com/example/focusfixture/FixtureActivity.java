package com.example.focusfixture;
public class FixtureActivity extends android.app.Activity {
 @Override public void onCreate(android.os.Bundle b){super.onCreate(b);android.widget.TextView t=new android.widget.TextView(this);t.setText("FocusLock test app — access allowed");t.setTextSize(24);t.setGravity(android.view.Gravity.CENTER);setContentView(t);}
}
