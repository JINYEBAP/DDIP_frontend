package com.example.jin.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
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

import java.util.HashMap;
import java.util.Map;

public class WriteReviewActivity extends ToolbarActivity {
    ListView listview;
    RequestQueue mQueue;
    SharedPreferences pref;
    String token;
    long iid;
    TextView score;
    EditText text;
    Button reviewWriteButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_review);
        mQueue=Volley.newRequestQueue(this);
        pref=getSharedPreferences("pref", MODE_PRIVATE);
        token=pref.getString("token",null);
        listview = (ListView) findViewById(R.id.drawer_menulist) ;
        listview.setAdapter(super.MENU) ;
        listview.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View v, int position, long id) {
                selectDrawable((String) adapterView.getItemAtPosition(position));
            }
        });
        Intent intent = getIntent();
        iid = intent.getLongExtra("iid",-1);
        score=findViewById(R.id.review_score);
        text=findViewById(R.id.review_text);
        RatingBar starScore =(RatingBar)findViewById(R.id.star_score);
        starScore.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating,
                                        boolean fromUser) {
                int star=(int)rating;
                score.setText(String.valueOf(star));
            }
        });

        reviewWriteButton=findViewById(R.id.write_btn);
        reviewWriteButton.setOnClickListener(v -> writeReview());

    }
    void writeReview() {
        String url = "http://54.180.90.90:3000/api/review";
        StringRequest request = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONObject msg=null;
                        try {
                            msg=new JSONObject(response);
                            if(msg.getString("success").equals("true")){
                                Toast.makeText(getApplicationContext(), "리뷰를 작성하였습니다.", Toast.LENGTH_SHORT).show();
                                WriteReviewActivity.super.onBackPressed();
                                /*Intent intent;
                                intent = new Intent(
                                        getApplicationContext(),
                                        ItemInformActivity.class);
                                intent.putExtra("iid",iid);
                                startActivity(intent);*/
                            }
                            else{
                                Toast.makeText(getApplicationContext(),"리뷰를 이미 작성하였거나 작성할 수 없습니다.", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_SHORT).show();
                    }
                }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("iid", String.valueOf(iid));
                params.put("score",score.getText().toString());
                params.put("text",text.getText().toString());
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };
        mQueue.add(request);
    }
}
