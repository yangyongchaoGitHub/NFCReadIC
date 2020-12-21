package com.dataexpo.nfcsample;

import android.app.PendingIntent;
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
public class TestActivity extends BascActivity {
    private final String ACTION_NAME = "android.nfc.action.TECH_DISCOVERED";
    NfcAdapter nfcAdapter;
    private PendingIntent mPendingIntent;
    private IntentFilter[] mIntentFilters;
    private String[][] mTechLists;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 获取默认的NFC控制器
        nfcAdapter = NfcAdapter.getDefaultAdapter (this);
        if (nfcAdapter == null) {
            Toast.makeText(this, "设备不支持NFC！", Toast.LENGTH_LONG).show();
            finish ();
            return;
        }
        if (!nfcAdapter.isEnabled ()) {
            Toast.makeText(this, "请在系统设置中先启用NFC功能！", Toast.LENGTH_LONG).show();
            finish ();
            return;
        }

        initNFC();

    }

    private void initNFC(){
        //绑定Intent
        Intent mIntent = new Intent (ACTION_NAME);
        mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        IntentFilter techFilter = new IntentFilter (nfcAdapter.ACTION_TECH_DISCOVERED);
        try {
            techFilter.addDataType ("text/plain");
            mIntentFilters = new IntentFilter[]{techFilter};
        } catch (IntentFilter.MalformedMimeTypeException e) {
            e.printStackTrace();
        }

        mTechLists  = new String[][] {
                new String[] {"android.nfc.tech.NfcA"},
                new String[]{"android.nfc.tech.NfcB"},
                new String[]{"android.nfc.tech.NfcF"},
                new String[]{"android.nfc.tech.NfcV"},
                new String[]{"android.nfc.tech.Ndef"},
                new String[]{"android.nfc.tech.NdefFormatable"},
                new String[]{"android.nfc.tech.IsoDep"},
                new String[]{"android.nfc.tech.MifareClassic"},
                new String[]{"android.nfc.tech.MifareUltralight"}
        };
    }

    @Override
    protected void onNewIntent(Intent intent){
        super.onNewIntent (intent);
        // 得到是否检测到ACTION_TECH_DISCOVERED触发
        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals (intent.getAction ())) {
            // 处理该intent
            processIntent(intent);
        }
    }

    @Override
    protected void onResume(){
        super.onResume ();
        nfcAdapter.enableForegroundDispatch (this, mPendingIntent, mIntentFilters, mTechLists);
    }

    @Override
    protected void onPause(){
        super.onPause ();
        //注销注册
        nfcAdapter.disableForegroundDispatch(this);
    }

    private String bytesToHexString(byte[] src){
        StringBuilder stringBuilder = new StringBuilder ("0x");
        if (src == null || src.length <= 0) { return null; }
        char[] buffer = new char[2];
        for ( int i = 0 ; i < src.length ; i++ ) {
            buffer[0] = Character.forDigit ((src[i] >>> 4) & 0x0F, 16);
            buffer[1] = Character.forDigit (src[i] & 0x0F, 16);
            //System.out.println (buffer);
            stringBuilder.append (buffer);
        }
        return stringBuilder.toString();
    }

    private String bytesToHexStringNo0x(byte[] src){
        StringBuilder stringBuilder = new StringBuilder ();
        if (src == null || src.length <= 0) { return null; }
        char[] buffer = new char[2];
        for ( int i = 0 ; i < src.length ; i++ ) {
            buffer[0] = Character.forDigit ((src[i] >>> 4) & 0x0F, 16);
            buffer[1] = Character.forDigit (src[i] & 0x0F, 16);
            System.out.println (buffer[0] + " i" + i + " " +buffer[1]);
            stringBuilder.append (buffer);
        }
        return stringBuilder.toString();
    }

    private void processIntent(Intent intent){
        // 取出封装在intent中的TAG
        Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
//        for ( String tech : tagFromIntent.getTechList () ) {
//            System.out.println (tech);
//        }
        boolean auth = false;
        // 读取TAG
        MifareClassic mfc = MifareClassic.get(tagFromIntent);
        //String metaInfo = "本标签的UID为" + Coverter.getUid (intent) + "\n";
        String CardId =bytesToHexStringNo0x(tagFromIntent.getId());
        Log.i("0000000000 卡号是：--", CardId + "");

        Log.i("什么是F", 0xff + "");

        long cardId = 0l;
        byte[] id16 = tagFromIntent.getId();
        Log.i("0: ", id16[0] + " " + (id16[0] << 24));
        Log.i("id16 ", id16.length + "");
        Log.i("1: ", id16[1] + "" + (id16[1] << 16));
        cardId = cardId | ((id16[0] << 24));
        cardId = cardId | ((id16[1] << 16) & 0x00ff0000);
        cardId = cardId | ((id16[2] << 8) & 0x0000ff00);
        cardId = cardId | ((id16[3]) & 0xff);

        Log.i("0000000000 卡号是", CardId + " >> 十进制 " + cardId);
        cardId = Long.parseLong(CardId, 16);
        Log.i("111 卡号是", CardId + " >> 十进制 " + cardId);
        String metaInfo = "本标签的UID为" + CardId + "\n";

        if (mfc != null) {
            try {
                // Enable I/O operations to the tag from this TagTechnology
                // object.
                mfc.connect();
                int type = mfc.getType ();// 获取TAG的类型
                int sectorCount = mfc.getSectorCount();// 获取TAG中包含的扇区数
                String typeS = "";
                switch (type) {
                    case MifareClassic.TYPE_CLASSIC:
                        typeS = "TYPE_CLASSIC";
                        break;
                    case MifareClassic.TYPE_PLUS:
                        typeS = "TYPE_PLUS";
                        break;
                    case MifareClassic.TYPE_PRO:
                        typeS = "TYPE_PRO";
                        break;
                    case MifareClassic.TYPE_UNKNOWN:
                        typeS = "TYPE_UNKNOWN";
                        break;
                }

                Log.i("==========", "卡片类型：" + typeS + "\n共" + sectorCount + "个扇区\n共" + mfc.getBlockCount () + "个块\n存储空间: " + mfc.getSize () + "B\n");

                metaInfo += "卡片类型：" + typeS + "\n共" + sectorCount + "个扇区\n共" + mfc.getBlockCount () + "个块\n存储空间: " + mfc.getSize () + "B\n";
//                for ( int j = 0 ; j < sectorCount ; j++ ) {
//                    // Authenticate a sector with key A.
//                    auth = mfc.authenticateSectorWithKeyA (j, MifareClassic.KEY_DEFAULT);
//                    int bCount;
//                    int bIndex;
//                    if (auth) {
//                        metaInfo += "Sector " + j + ":验证成功\n";
//                        // 读取扇区中的块
//                        bCount = mfc.getBlockCountInSector (j);
//                        bIndex = mfc.sectorToBlock (j);
//                        for ( int i = 0 ; i < bCount ; i++ ) {
//                            byte[] data = mfc.readBlock (bIndex);
//                            metaInfo += "Block " + bIndex + " : " + bytesToHexString(data) + "\n";
//                            bIndex++;
//                        }
//                    } else {
//                        metaInfo += "Sector " + j + ":验证失败\n";
//                    }
//                }
            } catch (Exception e) {
                e.printStackTrace ();
            }
        }
        Log.i("--------------", metaInfo);
    }


}
