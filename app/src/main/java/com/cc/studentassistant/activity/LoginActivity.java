package com.cc.studentassistant.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.cc.studentassistant.R;
import com.cc.studentassistant.http.HttpUtil;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int UPDATE_IMAGE = 0;
    private static final int LOGIN_SUCCESS = 1;


    private static final String login_url1 = "http://jwgl.gdut.edu.cn";
    private static final String login_url2 = "http://jwgl.gdut.edu.cn/default2.aspx";

    private boolean login_flag = false;

    private HttpUtil httpUtil;
    private String name = "未登录";

    private ImageView back_iv;
    private EditText studentID_edtTxt;
    private EditText password_edtTxt;
    private EditText verificationCode_edtTxt;
    private ImageView verificationCode_iv;
    private Button login_btn;

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch(msg.what){
                case UPDATE_IMAGE:
                    // 更新验证码
                    verificationCode_iv.setImageBitmap((Bitmap)msg.obj);
                    break;
                case LOGIN_SUCCESS:
                    backToMainActivity();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);

        Intent intent = this.getIntent();
        httpUtil = (HttpUtil)intent.getSerializableExtra("HttpUtil");

        initWidget();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 请求首页和验证码信息
        sendRequestForHomePage();
    }

    private void initWidget(){
        back_iv = (ImageView) findViewById(R.id.iv_LoginActivity_back);
        studentID_edtTxt = (EditText) findViewById(R.id.edtTxt_LoginActivity_StudentID);
        password_edtTxt = (EditText) findViewById(R.id.edtTxt_LoginActivity_Password);
        verificationCode_edtTxt = (EditText) findViewById(R.id.edtTxt_LoginActivity_VerificationCode);
        verificationCode_iv = (ImageView) findViewById(R.id.iv_LoginActivity_VerificationCode);
        login_btn = (Button)findViewById(R.id.btn_LoginActivity_login);

        back_iv.setOnClickListener(this);
        verificationCode_iv.setOnClickListener(this);
        login_btn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.iv_LoginActivity_back: {
                backToMainActivity();
            }
            break;
            case R.id.iv_LoginActivity_VerificationCode:{
                sendRequestForVerificationCode();
            }
            break;
            case  R.id.btn_LoginActivity_login: {
                sendRequestForLogin();
                // TODO UI弹出一个加载窗口
                // 等待登陆完成执行finish()操作
//                while (!login_flag) {
//                    // TODO 考虑是否存在卡死的情况
//                }
//                backToMainActivity();
            }
            break;

        }
    }

    // 1. 发送请求首页
    private void sendRequestForHomePage(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 请求首页数据
                HttpGet getHomePage = new HttpGet(login_url1);
                try{
                    HttpResponse httpResponse = httpUtil.getHttpClient().execute(getHomePage);
                    if(httpResponse.getStatusLine().getStatusCode() == 200){
                        HttpEntity entity = httpResponse.getEntity();
                        String responseHtml = EntityUtils.toString(entity);
                        httpUtil.setValue(responseHtml.split("<input type=\"hidden\" name=\"__VIEWSTATE\" value=\"")[1].split("\" />")[0]);
                    }
                }catch(IOException e){
                    // TODO 需弹出警告
                    e.printStackTrace();
                }
                Log.d("LoginActivity", "请求首页数据完成");
                // 请求验证码数据
                HttpGet getVerificationCode = new HttpGet("http://jwgl.gdut.edu.cn/CheckCode.aspx");
                try{
                    HttpResponse verificationCodeResponse = httpUtil.getHttpClient().execute(getVerificationCode);
                    HttpEntity entity = verificationCodeResponse.getEntity();
                    // 获取输入流
                    InputStream in = entity.getContent();
                    Bitmap bitmap = BitmapFactory.decodeStream(in);
                    in.close();
                    Message message = new Message();
                    message.what = UPDATE_IMAGE;
                    message.obj = bitmap;
                    handler.sendMessage(message);
                }catch (IOException e){
                    // TODO 需弹出警告
                    e.printStackTrace();
                }
                Log.d("LoginActivity", "请求验证码完成");
            }
        }).start();
    }

    // 1.2 重新请求验证码
    private void sendRequestForVerificationCode(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 请求验证码数据
                HttpGet getVerificationCode = new HttpGet("http://jwgl.gdut.edu.cn/CheckCode.aspx");
                try{
                    HttpResponse verificationCodeResponse = httpUtil.getHttpClient().execute(getVerificationCode);
                    HttpEntity entity = verificationCodeResponse.getEntity();
                    // 获取输入流
                    InputStream in = entity.getContent();
                    Bitmap bitmap = BitmapFactory.decodeStream(in);
                    in.close();
                    Message message = new Message();
                    message.what = UPDATE_IMAGE;
                    message.obj = bitmap;
                    handler.sendMessage(message);
                }catch (IOException e){
                    // TODO 需弹出警告
                    e.printStackTrace();
                }
                Log.d("LoginActivity", "请求验证码数据完成");
            }
        }).start();
    }

    // 2. 请求登陆
    private void sendRequestForLogin(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 请求登陆
                try {
                    HttpPost httpPost = new HttpPost(login_url2);
                    // 禁止重定向，由于刚刚Post的状态值是重定向，所以我们要去禁止它，不然网页会乱飞。
                    httpPost.getParams().setParameter(ClientPNames.HANDLE_REDIRECTS, false);
                    // 设置头部信息
                    httpPost.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:30.0) Gecko/20100101 Firefox/30.0");
                    httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");

                    {
                        Log.d("LoginActivity", "学号：" + studentID_edtTxt.getText().toString());
                        Log.d("LoginActivity", "密码：" + password_edtTxt.getText().toString());
                        Log.d("LoginActivity", "验证码：" + verificationCode_edtTxt.getText().toString());
                    }
                    List<NameValuePair> params = new ArrayList<NameValuePair>();
                    params.add(new BasicNameValuePair("__VIEWSTATE", httpUtil.getValue()));
                    params.add(new BasicNameValuePair("txtUserName", studentID_edtTxt.getText().toString()));
                    params.add(new BasicNameValuePair("TextBox2", password_edtTxt.getText().toString()));
                    params.add(new BasicNameValuePair("txtSecretCode", verificationCode_edtTxt.getText().toString()));
                    params.add(new BasicNameValuePair("RadioButtonList1", "%D1%A7%C9%FA"));
                    params.add(new BasicNameValuePair("Button1", ""));
                    params.add(new BasicNameValuePair("lbLanguage", ""));
                    params.add(new BasicNameValuePair("hidPdrs", ""));
                    params.add(new BasicNameValuePair("hidsc", ""));
                    UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, "GBK");
                    httpPost.setEntity(entity);
                    HttpResponse httpResponse = httpUtil.getHttpClient().execute(httpPost);

                    if (httpResponse.getStatusLine().getStatusCode() == 302
                            || httpResponse.getStatusLine().getStatusCode() == 301) {

                        List<Cookie> cookies = ((AbstractHttpClient) httpUtil.getHttpClient()).getCookieStore().getCookies();
                        if (cookies != null) {
                            String tmpcookies = "";
                            for (Cookie ck : cookies) {
                                tmpcookies += ck.getName() + "=" + ck.getValue() + ";" + "domain=" + ck.getDomain() + ";" + "path=" + ck.getPath();
                            }
                            httpUtil.setCookie(tmpcookies);
                        }
                        httpUtil.setLocation(httpResponse.getFirstHeader("Location").getValue());
                        httpUtil.setStudentID(httpResponse.getFirstHeader("Location").getValue().split("xh=")[1]);
                        Log.d("LoginActivity", "登陆成功\nLocation: " + httpUtil.getLocation()
                                +"\nStudentID: " + httpUtil.getStudentID()
                                +"\nCookie: " + httpUtil.getCookie());
                    } else {
                        // TODO 分析登陆失败原因，弹出相应提示
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                // 请求登陆后页面
                HttpGet getLoginLater = new HttpGet(login_url1 + httpUtil.getLocation());
                getLoginLater.getParams().setParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS,false);
                getLoginLater.setHeader("Referer", "http://jwgl.gdut.edu.cn/");
                getLoginLater.addHeader("Cookie", httpUtil.getCookie());
                String responseHtml;
                try {
                    HttpResponse httpResponse = httpUtil.getHttpClient().execute(getLoginLater);

                    if (httpResponse.getStatusLine().getStatusCode() == 200) {
                        HttpEntity httpEntity = httpResponse.getEntity();
                        responseHtml = EntityUtils.toString(httpEntity, "utf-8");
                        // 解析出学生姓名
                        Document doc = Jsoup.parse(responseHtml);
                        Elements elements = doc.select("div.info span#xhxm");
                        name = elements.get(0).text();
                        Log.d("LoginActivity", "得到登陆后页面。");
                        Log.d("LoginActivity", "学生姓名：" + name);
                        // TODO 使用死循环进行控制，导致主线程阻塞
                        login_flag = true;
                        Message message = new Message();
                        message.what = LOGIN_SUCCESS;
                        handler.sendMessage(message);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        }).start();
    }

    @Override
    public void onBackPressed(){
        backToMainActivity();
    }

    private void backToMainActivity(){

        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putSerializable("HttpUtil", httpUtil);
        intent.putExtras(bundle);
        if(login_flag) {
            intent.putExtra("name", name);
            setResult(RESULT_OK, intent);
        }else {
            setResult(RESULT_CANCELED, intent);
        }
        finish();
    }
}
