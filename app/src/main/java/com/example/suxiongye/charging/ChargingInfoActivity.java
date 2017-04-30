package com.example.suxiongye.charging;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.suxiongye.bean.Charging;
import com.example.suxiongye.util.ChargingData;

import java.util.ArrayList;

public class ChargingInfoActivity extends AppCompatActivity implements View.OnClickListener {

    private Context mContext;
    private ChargingData chargingData;
    private TextView tv_title_right, tv_name, tv_used, tv_status;
    private Button button;
    private ImageView iv_back;

    private ScrollView sv;
    private Charging charging;

    public static final int SHOW_RESPONSE = 0;
    public static final int SHOW_CHARGING = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        mContext = this;
        chargingData = new ChargingData(handler);
        initView();
        setText();
    }

    private void initView() {
        tv_name = (TextView) findViewById(R.id.tv_name);
        tv_used = (TextView) findViewById(R.id.tv_used);
        tv_status = (TextView) findViewById(R.id.tv_status);

        iv_back = (ImageView) findViewById(R.id.iv_back);
        iv_back.setOnClickListener(this);
        iv_back.setVisibility(View.VISIBLE);

        tv_title_right = (TextView) findViewById(R.id.tv_title_button);
        tv_title_right.setText("导航 >");
        tv_title_right.setOnClickListener(this);
        tv_title_right.setVisibility(View.VISIBLE);

        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(this);
        sv = (ScrollView) findViewById(R.id.sv);

    }

    private void setText() {
        charging = getIntent().getParcelableExtra("c");
        updateText();
        sv.smoothScrollTo(0, 0);
    }

    /**
     * 更新状态
     */
    private void updateText() {
        tv_name.setText(charging.getName());
        if (charging.getUsed().equals("used"))
            tv_used.setText("使用中");
        else tv_used.setText("未使用");
        if (charging.getStatus().equals("normal")) tv_status.setText("可正常使用");
        else tv_status.setText("故障中");
        if (charging.getStatus().equals("normal")) {
            button.setEnabled(true);
            if (charging.getUsed().equals("used")) {
                button.setText("停止");
            } else {
                button.setText("开启");
            }
        } else button.setEnabled(false);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //返回
            case R.id.iv_back:
                finish();
                break;
            //导航
            case R.id.tv_title_button:
                break;
            case R.id.button:
                if (button.getText().equals("开启")) {
                    openCharging(charging.getId());
                } else {
                    closeCharging(charging.getId());
                }
                isOpenCharging(charging.getId());
                updateText();
                break;
            default:
                break;
        }
    }

    private void openCharging(final int id) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                chargingData.openCharging(id);
            }
        }).start();
    }

    private void closeCharging(final int id) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                chargingData.closeCharging(id);
            }
        }).start();
    }

    private void isOpenCharging(final int id) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                chargingData.isOpenCharging(id);
            }
        }).start();
    }


    //获取所有充电桩
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                //获取请求结果
                case SHOW_RESPONSE:
                    String result = (String) msg.obj;
                    if (result.equals("used")) {
                        charging.setUsed("used");
                    } else if (result.equals("unused")) {
                        charging.setUsed("unused");
                    }
                    updateText();
                    break;

                default:
                    break;
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        updateText();
    }
}
