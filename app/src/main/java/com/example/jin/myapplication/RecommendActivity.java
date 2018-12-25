package com.example.jin.myapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RecommendActivity extends ToolbarActivity {
    RequestQueue mQueue;
    EditText RecommendItem_id;
    Button RecommendItem_id_summit;
    String cid;
    String str;
    TextView RecommendItem_id_result;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommend);
        mQueue=Volley.newRequestQueue(this);
        RecommendItem_id=(EditText) findViewById(R.id.recommendItem_id);
        RecommendItem_id_summit=(Button) findViewById(R.id.recommend_id_summit);
        RecommendItem_id_summit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cid=RecommendItem_id.getText().toString();
                jsonParse();
            }
        });

    }
    void jsonParse(){
        String url="http://54.180.90.90:3000/api/alarm";
        StringRequest request=new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(getApplicationContext(), response, Toast.LENGTH_SHORT).show();
                        JSONObject obj=null;
                        try{
                            obj=new JSONObject(response);
                            str=obj.getString("name");
                            RecommendItem_id_result=(TextView) findViewById(R.id.recommend_id_result);
                            RecommendItem_id_result.setText(str);
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
                params.put("cid", cid);
                return params;
            }
        };
        mQueue.add(request);
    }
}
