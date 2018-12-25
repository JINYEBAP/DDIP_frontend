package com.example.jin.myapplication;

import android.content.Intent;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends ToolbarActivity {
    ListView listview = null ;
    EditText ID;
    EditText PW;
    EditText NAME;
    EditText AD;
    Button SignUpButton;
    RequestQueue mQueue;
    String str; //회원가입 성공 여부를 위한 스트링
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        listview = (ListView) findViewById(R.id.drawer_menulist) ;
        listview.setAdapter(super.MENU) ;
        listview.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View v, int position, long id) {
                selectDrawable((String)adapterView.getItemAtPosition(position));
            }
        });
        mQueue=Volley.newRequestQueue(this);
        ID=findViewById(R.id.signup_ID);
        PW=findViewById(R.id.PASSWORD);
        NAME=findViewById(R.id.signup_NAME);
        AD=findViewById(R.id.signup_AR);
        SignUpButton=findViewById(R.id.signup_btn);
        SignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SignUp();
            }
        });
    }
    void SignUp(){
        String url="http://54.180.90.90:3000/api/sign_up/customer";
        StringRequest request=new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //Toast.makeText(getApplicationContext(), response, Toast.LENGTH_SHORT).show();
                        JSONObject obj=null;

                        try{
                            obj=new JSONObject(response);
                            str=obj.getString("success");
                        }catch(JSONException e){
                            e.printStackTrace();
                        }
                        if(str.equals("true")){
                            //회원가입 성공 시 로그인 페이지로 이동
                            Toast.makeText(getApplicationContext(), "회원가입이 완료되었습니다. 로그인 해주세요.", Toast.LENGTH_SHORT).show();
                            Intent intent;
                            intent= new Intent(
                                    getApplicationContext(),
                                    LoginActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
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
                String cid=ID.getText().toString();
                if(cid.charAt(3)!='-'){
                    cid=cid.substring(0,3)+'-'+cid.substring(3,cid.length());
                }
                if(cid.charAt(cid.length()-5)!='-'){
                    cid=cid.substring(0,cid.length()-4)+'-'+cid.substring(cid.length()-4,cid.length());
                }
                params.put("cid",cid);
                params.put("passwd",PW.getText().toString());
                params.put("name",NAME.getText().toString());
                params.put("address",AD.getText().toString());
                params.put("latitude","1.1");
                params.put("longitude","1.1");
                return params;
            }
        };
        mQueue.add(request);
    }
}
