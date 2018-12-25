package com.example.jin.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends ToolbarActivity {
    ListView listview = null ;
    EditText ID;
    String formedID;
    EditText PW;
    Button loginButton;
    RequestQueue mQueue;
    SharedPreferences pref;
    String str; //토큰을 위한 스트링
    String name; //로그인한 회원 이름을 위한 스트링
    String deviceToken; //파이어베이스 토큰
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mQueue=Volley.newRequestQueue(this);
        pref=getSharedPreferences("pref", MODE_PRIVATE);
        ID=findViewById(R.id.login_ID);
        PW=findViewById(R.id.SPPASSWORD_t);
        listview = (ListView) findViewById(R.id.drawer_menulist) ;
        listview.setAdapter(super.MENU) ;
        listview.setOnItemClickListener(new ListView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View v, int position, long id) {
            selectDrawable((String)adapterView.getItemAtPosition(position));
        }
     });
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                deviceToken= instanceIdResult.getToken();
                // Do whatever you want with your token now
                // i.e. store it on SharedPreferences or DB
                // or directly send it to server
            }
        });
        loginButton= findViewById(R.id.login_btn);
        loginButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                formedID=ID.getText().toString();
                if(formedID.charAt(3)!='-'){
                    formedID=formedID.substring(0,3)+'-'+formedID.substring(3,formedID.length());
                }
                if(formedID.charAt(formedID.length()-5)!='-'){
                    formedID=formedID.substring(0,formedID.length()-4)+'-'+formedID.substring(formedID.length()-4,formedID.length());
                }
                login();
            }
        });
    }
    void login(){
        String url="http://54.180.90.90:3000/auth/login/customer";
        StringRequest request=new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //Toast.makeText(getApplicationContext(), response, Toast.LENGTH_SHORT).show();
                        //토큰 저장
                        //Toast.makeText(getApplicationContext(), deviceToken, Toast.LENGTH_SHORT).show();
                        SharedPreferences.Editor editor = pref.edit();
                        JSONObject obj=null;
                        try{
                            obj=new JSONObject(response);
                            str=obj.getString("token");
                            name=obj.getString("name");
                            Toast.makeText(getApplicationContext(),name+"님 안녕하세요.",Toast.LENGTH_SHORT).show();
                            editor.putString("token",str);
                            editor.putString("cid",formedID);
                            editor.putString("cname",name);
                            editor.putString("type","customer");
                            editor.commit();
                            //로그인 성공 시 홈 페이지로 이동
                            Intent intent;
                            intent= new Intent(
                                    getApplicationContext(),
                                    MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }catch(JSONException e){
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        supplierLogin();
                    }
                })
        {
            @Override
            protected Map<String,String> getParams(){
                Map<String, String> params = new HashMap<String, String>();
                params.put("cid",formedID);
                params.put("passwd",PW.getText().toString());
                params.put("token",deviceToken);
                return params;
            }
        };
        mQueue.add(request);
    }
    void supplierLogin(){
        String url="http://54.180.90.90:3000/auth/login/supplier";
        StringRequest request=new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //Toast.makeText(getApplicationContext(), response, Toast.LENGTH_SHORT).show();
                        //토큰 저장
                        SharedPreferences.Editor editor = pref.edit();
                        JSONObject obj=null;
                        try{
                            obj=new JSONObject(response);
                            str=obj.getString("token");
                            name=obj.getString("name");
                            Toast.makeText(getApplicationContext(),name+"님 안녕하세요.",Toast.LENGTH_SHORT).show();
                            editor.putString("token",str);
                            editor.putString("cid",formedID);
                            editor.putString("cname",name);
                            editor.putString("type","supplier");
                            editor.commit();
                            //로그인 성공 시 홈 페이지로 이동
                            Intent intent;
                            intent= new Intent(
                                    getApplicationContext(),
                                    MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }catch(JSONException e){
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
            protected Map<String,String> getParams(){
                Map<String, String> params = new HashMap<String, String>();

                params.put("sid",formedID);
                params.put("passwd",PW.getText().toString());
                params.put("token",deviceToken);
                return params;
            }
        };
        mQueue.add(request);
    }
}
