package com.dataexpo.nfcsample;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.dataexpo.nfcsample.utils.BascActivity;
import com.dataexpo.nfcsample.utils.NfcUtils;

/**
 * 读的是IC卡
 */
public class MainActivity extends BascActivity {
    private static final String TAG = MainActivity.class.getName();
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;

        NfcUtils.NfcCheck(mContext);
    }

    @Override
    protected void onNewIntent(Intent intent){
        super.onNewIntent (intent);
        // 得到是否检测到ACTION_TECH_DISCOVERED触发
        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals (intent.getAction ())) {
            // 处理该intent
            String cardId = NfcUtils.getCardId(intent);
            Log.i(TAG, " cardId is " + cardId);
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        NfcUtils.enable(this);
    }

    @Override
    protected void onPause(){
        super.onPause ();
        //注销注册
        NfcUtils.disable(this);
    }






}
