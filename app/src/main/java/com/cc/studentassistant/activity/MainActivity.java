package com.cc.studentassistant.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cc.studentassistant.R;
import com.cc.studentassistant.http.HttpUtil;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.util.EntityUtils;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView login_iv;
    private TextView name_tv;
    private LinearLayout queryResults_LinearLayout;
    private Button queryResults_btn;

    private HttpUtil httpUtil;

    private boolean login_flag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 隐藏标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        // 初始化控件
        initWidget();
        // 初始化网络
        initHttp();
    }

    // 初始化控件
    private void initWidget() {
        login_iv = (ImageView) findViewById(R.id.iv_MainActivity_login);
        name_tv = (TextView) findViewById(R.id.tv_MainActivity_name);
        queryResults_LinearLayout = (LinearLayout) findViewById(R.id.linearLayout_MainActivity_QueryResults);
        queryResults_btn = (Button) findViewById(R.id.btn_MainActivity_QueryResults);

        login_iv.setOnClickListener(this);
        queryResults_LinearLayout.setOnClickListener(this);
        queryResults_btn.setOnClickListener(this);
    }

    // 初始化网络
    private void initHttp() {
        httpUtil = new HttpUtil();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_MainActivity_login: {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("HttpUtil", httpUtil);
                intent.putExtras(bundle);
                startActivityForResult(intent, 1);
            }
            break;
            case R.id.linearLayout_MainActivity_QueryResults: {
                // TODO LinearLayout 无法响应单击事件？
                Intent intent = new Intent(MainActivity.this, ScoreActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("HttpUtil", httpUtil);
                intent.putExtras(bundle);
                startActivityForResult(intent, 2);
            }
            break;
            case R.id.btn_MainActivity_QueryResults: {
                Intent intent = new Intent(MainActivity.this, ScoreActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("HttpUtil", httpUtil);
                intent.putExtras(bundle);
                startActivityForResult(intent, 2);
            }
            break;

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1: // 登陆界面返回
                httpUtil = (HttpUtil) data.getSerializableExtra("HttpUtil");
                if (resultCode == RESULT_OK) {
                    login_flag = true;
                    Log.d("MainActivity", "学生姓名：" + data.getStringExtra("name"));
                    Log.d("MainActivity", "Location：" + httpUtil.getLocation());

                    String name = "（" + data.getStringExtra("name") + "）";
                    name_tv.setText(name);
                    Toast.makeText(getApplicationContext(), "登陆成功", Toast.LENGTH_SHORT).show();

                } else {
                    // TODO 细节处理
                    Toast.makeText(getApplicationContext(), "登陆失败", Toast.LENGTH_SHORT).show();
                }
                break;
            case 2: // 成绩查询界面返回
                // TODO
                break;
        }
    }
}
