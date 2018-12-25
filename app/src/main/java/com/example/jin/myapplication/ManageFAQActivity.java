package com.example.jin.myapplication;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class ManageFAQActivity extends ToolbarActivity {
    ListView listview;
    Button FAQenrollButton;
    EditText question;
    EditText answer;
    RequestQueue mQueue;
    SharedPreferences pref;
    String token;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_faq);
        mQueue=Volley.newRequestQueue(this);
        pref=getSharedPreferences("pref", MODE_PRIVATE);
        token=pref.getString("token",null);
        listview = (ListView) findViewById(R.id.drawer_menulist) ;
        listview.setAdapter(super.MENU) ;
        listview.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View v, int position, long id) {
                selectDrawable((String)adapterView.getItemAtPosition(position));
            }
        });
        //ID로 뷰 객체 불러오기
        FAQenrollButton=findViewById(R.id.FAQenrolll_btn);
        question=findViewById(R.id.FAQquestion);
        answer=findViewById(R.id.FAQanswer);
        FAQenrollButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                enrollFAQ();
            }
        });
    }
    void enrollFAQ(){
        String url="http:54.180.90.90:3000/api/faq";
        StringRequest request=new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(getApplicationContext(), question.getText().toString()+answer.getText().toString()+response, Toast.LENGTH_SHORT).show();
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
                params.put("question",question.getText().toString());
                params.put("answer",answer.getText().toString());
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
