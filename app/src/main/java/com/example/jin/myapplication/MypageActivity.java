package com.example.jin.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
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

public class MypageActivity extends ToolbarActivity {
    LinearLayout MyPageOrderList;
    LinearLayout MyReviewList;
    ArrayList<LinearLayout> MyPageOrderItem;
    ArrayList<LinearLayout> MyReview;
    TextView CustomerName;
    //ArrayList<LinearLayout> MyPageOrder=new ArrayList<LinearLayout>();
    ListView listview = null ;
    RequestQueue mQueue;
    SharedPreferences pref;
    String token;
    String name;
    String cid;
    TextView t;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mypage);
        MyPageOrderList=findViewById(R.id.mpOrderList);
        MyReviewList=findViewById(R.id.myReviewList);
        MyPageOrderItem=new ArrayList<LinearLayout>();
        MyReview=new ArrayList<LinearLayout>();
        mQueue=Volley.newRequestQueue(this);
        pref=getSharedPreferences("pref", MODE_PRIVATE);
        token=pref.getString("token", null);
        name=pref.getString("cname",null);
        cid=pref.getString("cid",null);
        listview = (ListView) findViewById(R.id.drawer_menulist) ;
        listview.setAdapter(super.MENU) ;
        listview.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View v, int position, long id) {
                selectDrawable((String)adapterView.getItemAtPosition(position));
            }
        });

        loadRecentOrderList();
        loadMyReviewList();


        //화면 이동
        LinearLayout goOrderList=findViewById(R.id.goOrderList);
        goOrderList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;
                intent = new Intent(
                        getApplicationContext(),
                        OrderListActivity.class);
                startActivity(intent);
            }
        });

        LinearLayout goWishList=findViewById(R.id.goWishList);
        goWishList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;
                intent = new Intent(
                        getApplicationContext(),
                        CustomWishListActivity.class);
                startActivity(intent);
            }
        });

        LinearLayout goFavoriteList=findViewById(R.id.goFavorite);
        goFavoriteList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;
                intent = new Intent(
                        getApplicationContext(),
                        CustomFavoriteActivity.class);
                startActivity(intent);
            }
        });
    }
    //최근 주문 내역
    void loadRecentOrderList(){
        String url="http://54.180.90.90:3000/api/order_history/customer";
        final JsonArrayRequest request=new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            long gid=0; //주문 그룹 번호
                            int i;
                            //최근 주문 받아오기
                            for(i=0;i<response.length();i++) {
                                //json 받아오기
                                JSONObject result = response.getJSONObject(i);
                                String orderDate = result.getString("order_date");
                                orderDate = orderDate.substring(0, orderDate.lastIndexOf("T"));
                                String itemName = result.getString("name");
                                //수평 레이아웃 생성
                                LinearLayout l = new LinearLayout(getApplicationContext());
                                l.setOrientation(LinearLayout.HORIZONTAL);
                                if(i==0){
                                    gid=result.getLong("gid");
                                }
                                else{
                                    if(gid!=result.getLong("gid")){
                                        break;
                                    }
                                }
                                //width 세팅
                                LinearLayout.LayoutParams ordernumberWidth = new LinearLayout.LayoutParams(findViewById(R.id.OrderNumberWrapper).getWidth(),
                                        LinearLayout.LayoutParams.MATCH_PARENT);
                                LinearLayout.LayoutParams orderdateWidth = new LinearLayout.LayoutParams(findViewById(R.id.OrderDateWrapper).getWidth(),
                                        LinearLayout.LayoutParams.MATCH_PARENT);
                                LinearLayout.LayoutParams nameWidth = new LinearLayout.LayoutParams(findViewById(R.id.OrderNameWrapper).getWidth(),
                                        LinearLayout.LayoutParams.MATCH_PARENT);
                                //  주문 번호
                                TextView OrderNumber_t=new TextView(getApplicationContext());
                                LinearLayout OrderNumberWrapper=new LinearLayout(getApplicationContext());
                                OrderNumberWrapper.setLayoutParams(ordernumberWidth);
                                OrderNumberWrapper.setGravity(Gravity.CENTER);
                                if(i==0){OrderNumber_t.setText(String.valueOf(gid));}
                                OrderNumberWrapper.addView(OrderNumber_t);
                                //  주문 날짜
                                TextView OrderDate_t=new TextView(getApplicationContext());
                                LinearLayout OrderDateWrapper=new LinearLayout(getApplicationContext());
                                OrderDateWrapper.setLayoutParams(orderdateWidth);
                                OrderDateWrapper.setGravity(Gravity.CENTER);
                                OrderDate_t.setText(orderDate);
                                OrderDateWrapper.addView(OrderDate_t);
                                //  상품 이름
                                LinearLayout INameWrapper=new LinearLayout(getApplicationContext());
                                INameWrapper.setLayoutParams(nameWidth);
                                INameWrapper.setGravity(Gravity.LEFT);
                                TextView IName_t=new TextView(getApplicationContext());
                                IName_t.setText(itemName);
                                IName_t.setTextColor(Color.BLACK);
                                INameWrapper.addView(IName_t);


                                //수평 레이아웃에 추가
                                l.addView(OrderNumberWrapper);
                                l.addView(OrderDateWrapper);
                                l.addView(INameWrapper);
                                MyPageOrderItem.add(l);
                            }

                            //최근 주문 Item들 모두 추가
                            for(i=0;i<MyPageOrderItem.size();i++) {
                                MyPageOrderList.addView(MyPageOrderItem.get(i));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
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
    void loadMyReviewList(){
        String url="http://54.180.90.90:3000/api/review/customer/"+cid;
        final JsonArrayRequest request=new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            int i;
                            //최근 주문 받아오기
                            for(i=0;i<response.length();i++) {
                                //json 받아오기
                                JSONObject result = response.getJSONObject(i);
                                long itemid=result.getLong("item_id");
                                String itemName=result.getString("itemName");
                                String score=result.getString("score");
                                String text=result.getString("text");
                                String date = result.getString("date");
                                date = date.substring(0, date.lastIndexOf("T"));

                                //수평 레이아웃 생성
                                LinearLayout l = new LinearLayout(getApplicationContext());
                                l.setOrientation(LinearLayout.HORIZONTAL);

                                //width 세팅
                                LinearLayout.LayoutParams nameWidth = new LinearLayout.LayoutParams(findViewById(R.id.RItemNameWrapper).getWidth(),
                                        LinearLayout.LayoutParams.MATCH_PARENT);
                                LinearLayout.LayoutParams scoreWidth = new LinearLayout.LayoutParams(findViewById(R.id.RItemScoreWrapper).getWidth(),
                                        LinearLayout.LayoutParams.MATCH_PARENT);
                                LinearLayout.LayoutParams textWidth = new LinearLayout.LayoutParams(findViewById(R.id.RItemTextWrapper).getWidth(),
                                        LinearLayout.LayoutParams.MATCH_PARENT);
                                LinearLayout.LayoutParams dateWidth = new LinearLayout.LayoutParams(findViewById(R.id.RDateWrapper).getWidth(),
                                        LinearLayout.LayoutParams.MATCH_PARENT);
                                //  상품 이름
                                final TextView Name_t=new TextView(getApplicationContext());
                                LinearLayout NameWrapper=new LinearLayout(getApplicationContext());
                                NameWrapper.setLayoutParams(nameWidth);
                                Name_t.setText(itemName);
                                Name_t.setTextColor(Color.BLACK);
                                NameWrapper.setGravity(Gravity.CENTER);

                                NameWrapper.addView(Name_t);
                                //  상품 평점
                                TextView Score_t=new TextView(getApplicationContext());
                                LinearLayout ScoreWrapper=new LinearLayout(getApplicationContext());
                                ScoreWrapper.setLayoutParams(scoreWidth);
                                ScoreWrapper.setGravity(Gravity.CENTER);
                                String star="";
                                for(int j=0;j<Integer.parseInt(score);j++){
                                    star+="★";
                                }
                                Score_t.setText(star);
                                Score_t.setTextColor(Color.rgb(255,152,0));
                                ScoreWrapper.addView(Score_t);
                                //  리뷰 내용
                                LinearLayout TextWrapper=new LinearLayout(getApplicationContext());
                                TextWrapper.setLayoutParams(textWidth);
                                TextWrapper.setGravity(Gravity.LEFT);
                                TextView Text_t=new TextView(getApplicationContext());
                                Text_t.setText(text);
                                Text_t.setTextColor(Color.BLACK);
                                TextWrapper.addView(Text_t);
                                //  작성 날짜
                                LinearLayout DateWrapper=new LinearLayout(getApplicationContext());
                                DateWrapper.setLayoutParams(nameWidth);
                                DateWrapper.setGravity(Gravity.CENTER);
                                TextView Date_t=new TextView(getApplicationContext());
                                Date_t.setText(date);
                                Date_t.setTextColor(Color.BLACK);
                                DateWrapper.addView(Date_t);


                                //수평 레이아웃에 추가
                                l.addView(NameWrapper);
                                l.addView(ScoreWrapper);
                                l.addView(TextWrapper);
                                l.addView(DateWrapper);
                                l.setBackgroundResource(R.drawable.underline);
                                MyReview.add(l);
                            }

                            //최근 주문 Item들 모두 추가
                            for(i=0;i<MyReview.size();i++) {
                                MyReviewList.addView(MyReview.get(i));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
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
}
