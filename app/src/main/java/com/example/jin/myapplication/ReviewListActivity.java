package com.example.jin.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ReviewListActivity extends ToolbarActivity {
    ListView listview;
    RequestQueue mQueue;
    SharedPreferences pref;
    String token;
    long iid;
    TextView rName_t;
    TextView iName_t;
    String rName;
    String iName;
    JSONArray ReviewListJSON;
    LinearLayout ReviewLists; //모든 상품 항목
    ArrayList<LinearLayout> ReviewList; //개별 상품 항목
    Button backButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_list);
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
        Intent intent = getIntent();
        iid = intent.getLongExtra("iid",-1);
        rName=intent.getStringExtra("rname");
        iName=intent.getStringExtra("iname");
        rName_t=findViewById(R.id.rname);
        rName_t.setText(rName);
        iName_t=findViewById(R.id.itemname);
        iName_t.setText(iName);
        ReviewLists=findViewById(R.id.ItemReviewList);
        ReviewList=new ArrayList<LinearLayout>();

        loadReviewList();

        backButton=findViewById(R.id.back_btn);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;
                intent = new Intent(
                        getApplicationContext(),
                        ItemInformActivity.class);
                intent.putExtra("iid",iid);
                startActivity(intent);
            }
        });
    }
    void loadReviewList(){
        String url="http://54.180.90.90:3000/api/review/"+iid;
        final JsonArrayRequest request=new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        //Toast.makeText(getApplicationContext(), response.toString(), Toast.LENGTH_SHORT).show();
                        ReviewListJSON=response;
                        //레이아웃 초기화
                        setReviewLayout();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_SHORT).show();
                error.printStackTrace();
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization","Bearer "+token);
                return headers;
            }
        };
        mQueue.add(request);
    }

    void setReviewLayout(){
        int sum=0;
        float avg=0;
        try {
            //장바구니 항목 개수에 따라 추가
            for(int i=0;i<ReviewListJSON.length();i++){
                //수평 레이아웃 생성
                final LinearLayout l = new LinearLayout(getApplicationContext());
                l.setOrientation(LinearLayout.HORIZONTAL);
                //json 오브젝트 받아오기
                JSONObject result=ReviewListJSON.getJSONObject(i);
                int score=result.getInt("score"); // 평점
                sum+=score;
                String text=result.getString("text"); // 내용
                String date=result.getString("date"); // 날짜
                date=date.substring(0,date.lastIndexOf("T"));


                //width 세팅
                LinearLayout.LayoutParams scoreWidth = new LinearLayout.LayoutParams(findViewById(R.id.ReviewScore_t_wrapper).getWidth(),
                        LinearLayout.LayoutParams.MATCH_PARENT);
                LinearLayout.LayoutParams textWidth = new LinearLayout.LayoutParams(findViewById(R.id.ReviewListText_t_wrapper).getWidth(),
                        LinearLayout.LayoutParams.MATCH_PARENT);
                LinearLayout.LayoutParams dateWidth = new LinearLayout.LayoutParams(findViewById(R.id.ReviewListDate_t_wrapper).getWidth(),
                        LinearLayout.LayoutParams.MATCH_PARENT);


                //TextView 생성
                //  점수
                TextView ItemName_t=new TextView(getApplicationContext());
                LinearLayout ItemNameWrapper=new LinearLayout(getApplicationContext());
                ItemNameWrapper.setLayoutParams(scoreWidth);
                ItemNameWrapper.setGravity(Gravity.CENTER);
                ItemName_t.setText(String.valueOf(score));
                ItemNameWrapper.addView(ItemName_t);

                //  내용
                TextView ItemCost_t=new TextView(getApplicationContext());
                LinearLayout ItemCostWrapper=new LinearLayout(getApplicationContext());
                ItemCostWrapper.setLayoutParams(textWidth);
                ItemCostWrapper.setGravity(Gravity.CENTER);
                ItemCost_t.setText(text);
                ItemCostWrapper.addView(ItemCost_t);

                //  날짜
                LinearLayout ItemAmountWrapper=new LinearLayout(getApplicationContext());
                ItemAmountWrapper.setLayoutParams(dateWidth);
                ItemAmountWrapper.setGravity(Gravity.CENTER);
                TextView Amount_t=new TextView(getApplicationContext());
                Amount_t.setText(date);
                ItemAmountWrapper.addView(Amount_t);


                //수평 레이아웃에 추가
                l.addView(ItemNameWrapper);
                l.addView(ItemCostWrapper);
                l.addView(ItemAmountWrapper);

                l.setBackgroundResource(R.drawable.underline);
                //각각 리뷰 ArrayList에 추가
                ReviewList.add(l);

            }
            avg=sum/(float)ReviewListJSON.length();
            avg=Math.round(avg*100)/(float)100;
            TextView avg_score=findViewById(R.id.score_avg);
            avg_score.setText("★"+String.valueOf(avg));
            //리뷰 레이아웃에 추가
            for(int i=0;i<ReviewList.size();i++) {
                ReviewLists.addView(ReviewList.get(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}

