package com.dataexpo.nfcsample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;

import com.dataexpo.nfcsample.utils.BascActivity;
import com.dataexpo.nfcsample.utils.NfcUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class MainActivity extends BascActivity {
    private static final String TAG = MainActivity.class.toString();
    NfcAdapter nfcAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new NfcUtils(this);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            Toast.makeText(this, "NFC is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        // Register callback
        nfcAdapter.setNdefPushMessageCallback(new NfcAdapter.CreateNdefMessageCallback() {

            @Override
            public NdefMessage createNdefMessage(NfcEvent event) {
                Log.i(TAG, "111111111111111111111111");
                return null;
            }
        }, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (NfcUtils.mNfcAdapter != null) {
            NfcUtils.mNfcAdapter.enableForegroundDispatch(this, NfcUtils.mPendingIntent, NfcUtils.mIntentFilter, NfcUtils.mTechList);
        }
    }

    //在onNewIntent中处理由NFC设备传递过来的intent
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.e(TAG, "--------------NFC-------------" );
        processIntent(intent);
    }

    //  这块的processIntent() 就是处理卡中数据的方法
    public void processIntent(Intent intent) {
        //Parcelable[] rawmsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        Parcelable[] rawmsgs = intent.getParcelableArrayExtra(NfcAdapter.ACTION_TECH_DISCOVERED);
        Log.i(TAG, rawmsgs + "");
        if (rawmsgs != null) {
            NdefMessage msg = (NdefMessage) rawmsgs[0];
            NdefRecord[] records = msg.getRecords();
            String resultStr = new String(records[0].getPayload());
            // 返回的是NFC检查到卡中的数据
            Log.e(TAG, "processIntent: " + resultStr);
            try {
                // 检测卡的id
                String id = NfcUtils.readNFCId(intent);
                Log.e(TAG, "processIntent--id: " + id);
                // NfcUtils中获取卡中数据的方法
                String result = NfcUtils.readNFCFromTag(intent);
                Log.e(TAG, "processIntent--result: " + result);
                // 往卡中写数据
                //ToastUtils.showLong(getActivity(),result);
                Log.i(TAG, result);
                String data = "this.is.write";
                NfcUtils.writeNFCToTag(data, intent);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (FormatException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (NfcUtils.mNfcAdapter != null) {
            NfcUtils.mNfcAdapter.disableForegroundDispatch(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        NfcUtils.mNfcAdapter = null;
    }
}