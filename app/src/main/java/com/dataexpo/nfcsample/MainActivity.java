package com.dataexpo.nfcsample;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.dataexpo.nfcsample.utils.BascActivity;
import com.dataexpo.nfcsample.utils.NfcUtils;
import com.dataexpo.nfcsample.utils.Utils;
import com.dataexpo.nfcsample.utils.net.HttpCallback;
import com.dataexpo.nfcsample.utils.net.HttpService;
import com.dataexpo.nfcsample.utils.net.URLs;
import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.google.gson.Gson;

import java.util.Date;
import java.util.HashMap;

import okhttp3.Call;

import static com.dataexpo.nfcsample.utils.Utils.FORMAT_YMD_HMS;

/**
 * 读的是IC卡
 */
public class MainActivity extends BascActivity {
    private static final String TAG = MainActivity.class.getName();
    private Context mContext;

    private TextView tv_card_id;
    private TextView tv_time;
    private CircularProgressView progressView;
    private ConstraintLayout main_root;

    private final int STATUS_INIT = 1;
    private final int STATUS_CHECK_CARD_EXIST = 2;
    private final int STATUS_CHECK_IMAGE = 3;
    private final int STATUS_SHOWING = 4;

    private int mStatus = STATUS_INIT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;

        NfcUtils.NfcCheck(mContext);
        initView();
    }

    private void initView() {
        tv_card_id = findViewById(R.id.main_tv_cardid);
        tv_time = findViewById(R.id.main_tv_time);
        progressView = (CircularProgressView) findViewById(R.id.progress_view);
        main_root = findViewById(R.id.main_root);
    }

    @Override
    protected void onNewIntent(Intent intent){
        super.onNewIntent (intent);
        // 得到是否检测到ACTION_TECH_DISCOVERED触发
        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals (intent.getAction ())) {
            // 处理该intent
            String cardId = NfcUtils.getCardId(intent);
            Log.i(TAG, " cardId is " + cardId);
            tv_card_id.setText(cardId);

            tv_time.setText(Utils.formatTime(new Date().getTime(), FORMAT_YMD_HMS));
            progressView.setVisibility(View.VISIBLE);
            if (mStatus == STATUS_INIT || mStatus == STATUS_SHOWING) {
                checkCard(cardId);
            }
        }
    }

    private void checkCard(String cardId) {
        final HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("cardId", cardId);

        HttpService.getWithParams(mContext, URLs.checkCard, hashMap, new HttpCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext, "网络异常，请重新验证", Toast.LENGTH_SHORT).show();
                    }
                });
                Log.i(TAG, e.getMessage());

                progressView.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onResponse(String response, int id) {
//                final MsgBean msgBean = new Gson().fromJson(response, MsgBean.class);
                Log.i(TAG, "online check expoid response: " + response);

                //回来有卡号是否存在的标志，
                //1卡不存在，直接退出流程 status = init
                //2卡存在 查看卡审核状态，
                //2.1审核未通过, 提示  退出 status = init
                //2.2 审核通过 查看使用次数
                //3.1 使用次数已经到达最大次数 提示 退出  status = init
                //3.2 使用次数在最大使用次数之内

                //去更新使用次数

//
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        if (msgBean.code == 200) {
//                            Intent intent = new Intent();
//
//                            //跳转到输入手机或者二维码界面
//                            intent.putExtra("Expo_id", expo_id);
//                            intent.putExtra("Add", address);
//                            intent.setClass(mContext, InputLoginActivity.class);
//                            if (null != msgBean.msg) {
//                                Toast.makeText(mContext, msgBean.msg, Toast.LENGTH_LONG).show();
//                            }
//
//                            startActivity(intent);
//
//                        } else {
//                            //展会不存在时清除输入的展会id和门禁地址
//                            et_address.setText("");
//                            et_expo_id.setText("");
//                            address = "";
//                            expo_id = "";
//                            Toast.makeText(mContext, "展会ID不存在，请输入正确的展会ID", Toast.LENGTH_SHORT).show();
//                            et_expo_id.requestFocus();
//                        }
//                    }
//                });
                progressView.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void addCount(String cardId) {
        final HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("cardId", cardId);

        HttpService.getWithParams(mContext, URLs.addCount, hashMap, new HttpCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext, "网络异常，请重新验证", Toast.LENGTH_SHORT).show();
                    }
                });
                Log.i(TAG, e.getMessage());

                progressView.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onResponse(String response, int id) {
                Log.i(TAG, "online check expoid response: " + response);

                //添加使用次数成功
                //progressView.setVisibility(View.INVISIBLE);
                //显示拿到的数据， 进行下一个步骤
                //显示图像
                showHead("");
            }
        });
    }

    private void showHead(String cardId) {
        //如果人像不存在
        //获取人像
        final HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("cardId", cardId);

        HttpService.getWithParams(mContext, URLs.getHead, hashMap, new HttpCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(mContext, "网络异常，请重新验证", Toast.LENGTH_SHORT).show();
                    }
                });
                Log.i(TAG, e.getMessage());

                progressView.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onResponse(String response, int id) {
                Log.i(TAG, "online check expoid response: " + response);

                //显示图像， 并且保存到本地
                //Bitmap bitmap = getLoacalBitmap("/sdcard/tubiao.jpg"); //从本地取图片(在cdcard中获取)  //
                //image1 .setImageBitmap(bitmap); //设置Bitmap
            }
        });
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
