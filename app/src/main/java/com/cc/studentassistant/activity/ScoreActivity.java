package com.cc.studentassistant.activity;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.cc.studentassistant.R;
import com.cc.studentassistant.adapter.StudentScore;
import com.cc.studentassistant.adapter.ScoreAdapter;
import com.cc.studentassistant.http.HttpUtil;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class ScoreActivity extends AppCompatActivity implements View.OnClickListener {

    private HttpUtil httpUtil;
    private static final String login_url1 = "http://jwgl.gdut.edu.cn";
    private static final int QUERY_SUCCESS = 1;
    private static final int QUERY_FALSE = 2;

    private String year_select;
    private String term_select;
    private boolean score_result = false;
    private String scoreHtml;

    private ImageView back_iv;
    private Spinner year_spn;
    private Spinner term_spn;
    private Button term_btn;
    private Button year_btn;
    private Button all_btn;
    private TextView gpa_tv;
    private TextView excellent_tv;
    private ArrayAdapter year_spn_adapter;
    private ArrayAdapter term_spn_adapter;
    private ListView score_lv;

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch(msg.what){
                case QUERY_SUCCESS:
                    gpa_tv.setText("正在计算...");
                    showScore();
                    break;
                case QUERY_FALSE:
                    // TODO 查询失败处理
                    break;

            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_score);

        Intent intent = this.getIntent();
        httpUtil = (HttpUtil) intent.getSerializableExtra("HttpUtil");

        initWidget();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // 跳转到成绩查询页面
        sendRequestForScorePage();
    }

    // 初始化控件
    private void initWidget() {
        back_iv = (ImageView) findViewById(R.id.iv_ScoreActivity_back);
        year_spn = (Spinner) findViewById(R.id.spn_ScoreActivity_year);
        term_spn = (Spinner) findViewById(R.id.spn_ScoreActivity_term);
        term_btn = (Button) findViewById(R.id.btn_ScoreActivity_term);
        year_btn = (Button) findViewById(R.id.btn_ScoreActivity_year);
        all_btn = (Button) findViewById(R.id.btn_ScoreActivity_all);
        gpa_tv = (TextView) findViewById(R.id.tv_ScoreActivity_GPA);
        excellent_tv = (TextView) findViewById(R.id.tv_ScoreActivity_excellent);
        score_lv = (ListView) findViewById(R.id.lv_ScoreActivity_score);
        score_lv.setVisibility(View.INVISIBLE);

        // 适配year_spn
        year_spn_adapter = ArrayAdapter.createFromResource(this, R.array.years, android.R.layout.simple_spinner_item);
        year_spn_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        year_spn.setAdapter(year_spn_adapter);
        year_btn.setVisibility(View.VISIBLE);
        // 适配term_spn
        term_spn_adapter = ArrayAdapter.createFromResource(this, R.array.terms, android.R.layout.simple_spinner_item);
        term_spn_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        term_spn.setAdapter(term_spn_adapter);
        term_spn.setVisibility(View.VISIBLE);

        // 设置spinner监听事件，更新所选择的选项
        year_spn.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                year_select = ScoreActivity.this.getResources().getStringArray(R.array.years)[arg2];
                Log.d("ScoreActivity", "学年：" + year_select);
                //设置显示当前选择的项
                arg0.setVisibility(View.VISIBLE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
        term_spn.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                term_select = ScoreActivity.this.getResources().getStringArray(R.array.terms)[arg2];
                Log.d("ScoreActivity", "学期：" + term_select);
                //设置显示当前选择的项
                arg0.setVisibility(View.VISIBLE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
        back_iv.setOnClickListener(this);
        year_btn.setOnClickListener(this);
        term_btn.setOnClickListener(this);
        all_btn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_ScoreActivity_back:
                backToMainActivity();
                break;
            case R.id.btn_ScoreActivity_term: // 按学期查询
                gpa_tv.setText("正在查询成绩..");
                sendRequestForQueryByTerm();
                break;
            case R.id.btn_ScoreActivity_year: // 按学年查询
                sendRequestForQueryByYear();
                break;
            case R.id.btn_ScoreActivity_all: // 查询全部成绩
                sendRequestForQueryByAll();
                break;
        }
    }

    // 跳转到成绩查询页面
    private void sendRequestForScorePage() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String url = "http://jwgl.gdut.edu.cn/xscj.aspx?xh=" + httpUtil.getStudentID() + "&xm=%D6%A3%B2%D3%B3%CF&gnmkdm=N121605";
                HttpGet getGradesPage = new HttpGet(url);
                getGradesPage.getParams().setParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, false);
                getGradesPage.setHeader("Referer", "http://jwgl.gdut.edu.cn/");
                getGradesPage.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:30.0) Gecko/20100101 Firefox/30.0");
                getGradesPage.addHeader("Location", "/xs_main.aspx?xh=3114006200");
                getGradesPage.addHeader("Cookie", httpUtil.getCookie());

                Log.d("ScoreActivity", "查询成绩地址：" + url);

                try {
                    HttpResponse httpResponse = httpUtil.getHttpClient().execute(getGradesPage);
                    if (httpResponse.getStatusLine().getStatusCode() == 200) {
                        HttpEntity entity = httpResponse.getEntity();
                        String responseHtml = EntityUtils.toString(entity, "utf-8");
                        // 保存动态的value值
                        httpUtil.setValue(responseHtml.split("<input type=\"hidden\" name=\"__VIEWSTATE\" value=\"")[1].split("\" />")[0]);
                        Log.d("ScoreActivity", "已跳转到到成绩查询页面");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    // 按学期查询成绩
    private void sendRequestForQueryByTerm() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 保存成绩查询返回页面，用于解析
                try {
                    String url = "http://jwgl.gdut.edu.cn/xscj.aspx?xh=" + httpUtil.getStudentID() + "&xm=%D6%A3%B2%D3%B3%CF&gnmkdm=N121605";
                    HttpPost httpPost = new HttpPost(url);// 地址刚好与Referer相同
                    // 禁止重定向，由于刚刚Post的状态值是重定向，所以我们要去禁止它，不然网页会乱飞。
                    httpPost.getParams().setParameter(ClientPNames.HANDLE_REDIRECTS, false);
                    // 设置头部信息
                    httpPost.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:30.0) Gecko/20100101 Firefox/30.0");
                    httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
                    httpPost.setHeader("Accept", "text/html, application/xhtml+xml, */*");
                    httpPost.setHeader("Connection", "Keep-Alive");
                    // 此设置不能少
                    httpPost.setHeader("Referer", url);// 地址刚好与目的地址相同
                    httpPost.addHeader("Cookie", httpUtil.getCookie());

                    {
                        Log.d("ScoreActivity", "学号：" + httpUtil.getStudentID());
                        Log.d("ScoreActivity", "学年：" + year_select);
                        Log.d("ScoreActivity", "学期：" + term_select);
                    }
                    List<NameValuePair> params = new ArrayList<NameValuePair>();
                    params.add(new BasicNameValuePair("__VIEWSTATE", httpUtil.getValue()));
                    params.add(new BasicNameValuePair("ddlXN", year_select));
                    params.add(new BasicNameValuePair("ddlXQ", term_select));
                    params.add(new BasicNameValuePair("txtQSCJ", "0"));
                    params.add(new BasicNameValuePair("txtZZCJ", "100"));
                    params.add(new BasicNameValuePair("Button1", "%B0%B4%D1%A7%C6%DA%B2%E9%D1%AF"));
                    UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, "GBK");
                    httpPost.setEntity(entity);
                    HttpResponse httpResponse = httpUtil.getHttpClient().execute(httpPost);

                    //Log.d("ScoreActivity", "状态码" + httpResponse.getStatusLine().getStatusCode());

                    if (httpResponse.getStatusLine().getStatusCode() == 200) {
                        HttpEntity httpEntity = httpResponse.getEntity();
                        String response = EntityUtils.toString(httpEntity, "utf-8");
                        scoreHtml = response;
                        //score_result = true;
                        Log.d("ScoreActivity", "取得成绩单");
                        // 更新主线程UI
                        Message message = new Message();
                        message.what = QUERY_SUCCESS;
                        handler.sendMessage(message);
                        // TODO 存储view
                    } else {
                        // TODO 弹出错误窗口
                        Log.d("ScoreActivity", "状态码" + httpResponse.getStatusLine().getStatusCode());
                    }
                } catch (Exception e) {
                    // TODO 弹出错误窗口
                    e.printStackTrace();
                }

            }
        }).start();
    }

    // 按学年查询成绩
    private void sendRequestForQueryByYear() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 保存成绩查询返回页面，用于解析
                try {
                    String url = "http://jwgl.gdut.edu.cn/xscj.aspx?xh=" + httpUtil.getStudentID() + "&xm=%D6%A3%B2%D3%B3%CF&gnmkdm=N121605";
                    HttpPost httpPost = new HttpPost(url);// 地址刚好与Referer相同
                    // 禁止重定向，由于刚刚Post的状态值是重定向，所以我们要去禁止它，不然网页会乱飞。
                    httpPost.getParams().setParameter(ClientPNames.HANDLE_REDIRECTS, false);
                    // 设置头部信息
                    httpPost.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:30.0) Gecko/20100101 Firefox/30.0");
                    httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
                    httpPost.setHeader("Accept", "text/html, application/xhtml+xml, */*");
                    httpPost.setHeader("Connection", "Keep-Alive");
                    // 此设置不能少
                    httpPost.setHeader("Referer", url);// 地址刚好与目的地址相同
                    httpPost.addHeader("Cookie", httpUtil.getCookie());

                    {
                        Log.d("ScoreActivity", "学号：" + httpUtil.getStudentID());
                        Log.d("ScoreActivity", "学年：" + year_select);
                        //Log.d("ScoreActivity", "学期：" + term_select);
                    }
                    List<NameValuePair> params = new ArrayList<NameValuePair>();
                    params.add(new BasicNameValuePair("__VIEWSTATE", httpUtil.getValue()));
                    params.add(new BasicNameValuePair("ddlXN", year_select));
                    params.add(new BasicNameValuePair("ddlXQ", ""));
                    params.add(new BasicNameValuePair("txtQSCJ", "0"));
                    params.add(new BasicNameValuePair("txtZZCJ", "100"));
                    params.add(new BasicNameValuePair("Button5", "%B0%B4%D1%A7%C4%EA%B2%E9%D1%AF"));
                    UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, "GBK");
                    httpPost.setEntity(entity);
                    HttpResponse httpResponse = httpUtil.getHttpClient().execute(httpPost);

                    //Log.d("ScoreActivity", "状态码" + httpResponse.getStatusLine().getStatusCode());

                    if (httpResponse.getStatusLine().getStatusCode() == 200) {
                        HttpEntity httpEntity = httpResponse.getEntity();
                        String response = EntityUtils.toString(httpEntity, "utf-8");
                        scoreHtml = response;
                        //score_result = true;
                        Log.d("ScoreActivity", "取得成绩单");
                        // 更新主线程UI
                        Message message = new Message();
                        message.what = QUERY_SUCCESS;
                        handler.sendMessage(message);
                    } else {
                        // TODO 弹出错误窗口
                        Log.d("ScoreActivity", "状态码" + httpResponse.getStatusLine().getStatusCode());
                    }
                } catch (Exception e) {
                    // TODO 弹出错误窗口
                    e.printStackTrace();
                }

            }
        }).start();
    }

    // 按学年查询成绩
    private void sendRequestForQueryByAll() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 保存成绩查询返回页面，用于解析
                try {
                    String url = "http://jwgl.gdut.edu.cn/xscj.aspx?xh=" + httpUtil.getStudentID() + "&xm=%D6%A3%B2%D3%B3%CF&gnmkdm=N121605";
                    HttpPost httpPost = new HttpPost(url);// 地址刚好与Referer相同
                    // 禁止重定向，由于刚刚Post的状态值是重定向，所以我们要去禁止它，不然网页会乱飞。
                    httpPost.getParams().setParameter(ClientPNames.HANDLE_REDIRECTS, false);
                    // 设置头部信息
                    httpPost.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:30.0) Gecko/20100101 Firefox/30.0");
                    httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
                    httpPost.setHeader("Accept", "text/html, application/xhtml+xml, */*");
                    httpPost.setHeader("Connection", "Keep-Alive");
                    // 此设置不能少
                    httpPost.setHeader("Referer", url);// 地址刚好与目的地址相同
                    httpPost.addHeader("Cookie", httpUtil.getCookie());

                    {
                        Log.d("ScoreActivity", "学号：" + httpUtil.getStudentID());
                        Log.d("ScoreActivity", "查询全部内容");
                        //Log.d("ScoreActivity", "学期：" + term_select);
                    }
                    List<NameValuePair> params = new ArrayList<NameValuePair>();
                    params.add(new BasicNameValuePair("__VIEWSTATE", httpUtil.getValue()));
                    params.add(new BasicNameValuePair("ddlXN", ""));
                    params.add(new BasicNameValuePair("ddlXQ", ""));
                    params.add(new BasicNameValuePair("txtQSCJ", "0"));
                    params.add(new BasicNameValuePair("txtZZCJ", "100"));
                    params.add(new BasicNameValuePair("Button2", "%D4%DA%D0%A3%D1%A7%CF%B0%B3%C9%BC%A8%B2%E9%D1%AF"));
                    UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, "GBK");
                    httpPost.setEntity(entity);
                    HttpResponse httpResponse = httpUtil.getHttpClient().execute(httpPost);

                    //Log.d("ScoreActivity", "状态码" + httpResponse.getStatusLine().getStatusCode());

                    if (httpResponse.getStatusLine().getStatusCode() == 200) {
                        HttpEntity httpEntity = httpResponse.getEntity();
                        String response = EntityUtils.toString(httpEntity, "utf-8");
                        scoreHtml = response;
                        //score_result = true;
                        Log.d("ScoreActivity", "取得成绩单");
                        // 更新主线程UI
                        Message message = new Message();
                        message.what = QUERY_SUCCESS;
                        handler.sendMessage(message);
                    } else {
                        // TODO 弹出错误窗口
                        Log.d("ScoreActivity", "状态码" + httpResponse.getStatusLine().getStatusCode());
                    }
                } catch (Exception e) {
                    // TODO 弹出错误窗口
                    e.printStackTrace();
                }

            }
        }).start();
    }

    // 显示成绩
    // TODO 最好用多线程
    private void showScore() {
        Log.d("ScoreActivity", "开始解析成绩单");
        // 解析成绩
        Document gradeDoc = Jsoup.parse(scoreHtml);
        Elements rowElements = gradeDoc.select("div.main_box div.mid_box span.formbox table.datelist#DataGrid1 tbody tr");
        // 获取解析的个数
        Log.d("ScoreActivity", "科目总数：" + rowElements.size() + "科");

        // 全部课程学分绩点之和，全部课程学分之和
        float allScore = 0, allCredit = 0;
        // 优秀科目数
        int num = 0;
        List<StudentScore> list = new ArrayList<StudentScore>();
        for (int i = 1; i < rowElements.size(); i++) { //索引0为标题，跳过
            Elements elements = rowElements.get(i).select("td");
            // // 科目，类型，学分，分数
            StudentScore studentScore = new StudentScore(elements.get(1).text(), elements.get(2).text(), elements.get(7).text(), elements.get(3).text());

            // 获取科目学分
            float tempCredit = Float.parseFloat(elements.get(7).text().split("学分")[0]);
            allCredit += tempCredit;
            // 获取科目分数
            float tempScore = 0;
            String strTempScore = elements.get(3).text();
            if (strTempScore.equals("不及格") || strTempScore.equals("及格")
                    || strTempScore.equals("中等") || strTempScore.equals("良好") || strTempScore.equals("优秀")) {
                switch (strTempScore) {
                    case "不及格":
                        tempScore = 0;
                        break;
                    case "及格":
                        tempScore = 65;
                        break;
                    case "中等":
                        tempScore = 75;
                        break;
                    case "良好":
                        tempScore = 85;
                        break;
                    case "优秀":
                        tempScore = 95;
                        break;
                }
            } else {
                tempScore = Float.parseFloat(strTempScore);
            }
            if (tempScore >= 90) {
                num++;
            }
            // 计算全部课程学分绩点之和
            if (tempScore >= 60) {
                allScore += tempCredit * ((tempScore - 50) / 10);
            }

            list.add(studentScore);
        }
        {
            Log.d("ScoreActivity", "成绩分析完毕。");
            Log.d("ScoreActivity", "绩点：" + allScore / allCredit);
            Log.d("ScoreActivity", "优秀科目数：" + num);
        }
        // 显示绩点，只显示绩点小数点后两位
        gpa_tv.setText(new DecimalFormat(".00").format(allScore / allCredit));
        excellent_tv.setText("优秀科目数：" + num);

        ScoreAdapter scoreAdapter = new ScoreAdapter(ScoreActivity.this, R.layout.score_item, list);
        score_lv.setAdapter(scoreAdapter);
        score_lv.setVisibility(View.VISIBLE);
    }

    private void backToMainActivity() {

        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putSerializable("HttpUtil", httpUtil);
        intent.putExtras(bundle);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        backToMainActivity();
    }
}
