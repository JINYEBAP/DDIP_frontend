package com.example.jin.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ReuploadActivity extends AppCompatActivity {
    RequestQueue mQueue;
    SharedPreferences pref;
    String token;

    Button cancelButton;
    Button reuploadButton;
    EditText price;
    EditText count;
    EditText startTime;
    EditText endTime;

    //시간
    String sTime;
    Spinner HourSpinner;
    ArrayList<String> HourList;
    ArrayAdapter<String> HourAdapt;
    //분
    Spinner MinuteSpinner;
    ArrayList<String> MinuteList;
    ArrayAdapter<String> MinuteAdapt;

    long iid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //타이틀바 없애기
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_reupload);


        mQueue=Volley.newRequestQueue(this);
        pref=getSharedPreferences("pref", MODE_PRIVATE);
        token=pref.getString("token", null);
        //iid받기
        Intent intent = getIntent();
        iid = intent.getLongExtra("iid",-1);
        //판매 시작 시간 현재 시간으로 설정
        startTime = findViewById(R.id.StartTime);
        endTime = findViewById(R.id.EndTime);
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
        sTime = sdf.format(cal.getTime()); //현재 시간
        startTime.setText(sTime);
        //시간 스피너
        HourSpinner=findViewById(R.id.saleHourTimeSpinner);
        HourList=new ArrayList<String>();
        HourAdapt=new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_spinner_dropdown_item, HourList);
        HourAdapt.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        for(int i=0;i<10;i++){
            HourList.add(String.valueOf(i)+" 시간");
        }
        HourSpinner.setAdapter(HourAdapt);
        HourSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setSaleTime();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        MinuteSpinner=findViewById(R.id.saleMinuteTimeSpinner);
        MinuteList=new ArrayList<String>();
        MinuteAdapt=new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_spinner_dropdown_item, MinuteList);
        MinuteAdapt.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        for(int i=0;i<6;i++){
            MinuteList.add(String.valueOf(i*10)+" 분");
        }
        MinuteSpinner.setAdapter(MinuteAdapt);
        MinuteSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setSaleTime();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        //뷰연결 및 리스너 등록
        price=findViewById(R.id.Price);
        count=findViewById(R.id.Count);
        cancelButton=findViewById(R.id.cancel_btn);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReuploadActivity.super.onBackPressed();
            }
        });
        reuploadButton=findViewById(R.id.reupload_btn);
        reuploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reuploadItem();
            }
        });
    }
    void setSaleTime(){
        String h=HourSpinner.getSelectedItem().toString();
        h=h.substring(0,h.length()-3);
        String m=MinuteSpinner.getSelectedItem().toString();
        m=m.substring(0,m.length()-2);
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
        try {
            Date now=sdf.parse(sTime);
            cal.setTime(now);
            cal.add(Calendar.HOUR_OF_DAY, Integer.parseInt(h));
            cal.add(Calendar.MINUTE, Integer.parseInt(m));
            String eTime = sdf.format(cal.getTime()); //종료 시간

            endTime.setText(eTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
    void reuploadItem(){

        String url="http://54.180.90.90:3000/api/item/update";
        StringRequest request=new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                         JSONObject msg=null;
                        try {
                            msg=new JSONObject(response);
                            if(msg.getString("success").equals("true")){
                                Toast.makeText(getApplicationContext(),"재등록 완료",Toast.LENGTH_SHORT).show();
                                ReuploadActivity.super.onBackPressed();
                                Intent intent;
                                intent = new Intent(
                                        getApplicationContext(),
                                        UploadItemActivity.class);
                                startActivity(intent);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_SHORT).show();
                    }
                })
        {
            @Override
            protected Map<String,String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("sale_price",price.getText().toString());
                params.put("count",count.getText().toString());
                params.put("start_time",startTime.getText().toString());
                params.put("end_time",endTime.getText().toString());
                params.put("iid",String.valueOf(iid));
                return params;
            }
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization","Bearer "+token);
                return headers;
            }
        };
        mQueue.add(request);
    }
}
