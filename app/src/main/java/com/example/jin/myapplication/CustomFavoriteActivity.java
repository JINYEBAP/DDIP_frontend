package com.example.jin.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
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
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CustomFavoriteActivity extends ToolbarActivity {
    ListView listview;
    RequestQueue mQueue;
    SharedPreferences pref;
    String token;

    JSONArray FavoriteListJSON;
    LinearLayout FavoriteLists; //모든 가게 항목
    ArrayList<LinearLayout> FavoriteList; //개별 가게 항목

    int delete_index=-1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_favorite);
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


        FavoriteLists=findViewById(R.id.FavoriteList);
        FavoriteList=new ArrayList<LinearLayout>();

        loadFavoriteList();
    }
    // 뒤로가기로 찜목록으로 왔을 시 새로고침
    @Override
    public void onRestart() {
        super.onRestart();
        //When BACK BUTTON is pressed, the activity on the stack is restarted
        //Do what you want on the refresh procedure here
        loadFavoriteList();
    }
    void LayoutInit(){
       FavoriteLists.removeAllViewsInLayout();
       FavoriteList.clear();
    }
    void loadFavoriteList(){
        String url="http://54.180.90.90:3000/api/favorites/history";
        final JsonArrayRequest request=new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        FavoriteListJSON=response;
                        //레이아웃 초기화
                        if(delete_index==-1){
                            //처음 즐겨찾기 로드할 때: 레이아웃 초기화 및 세팅
                            LayoutInit();
                            setFavoriteLayout();
                        }
                        else{
                            //삭제 후 즐겨찾기 로드할 때: 삭제 변수 초기화 및 레이아웃 숨김 해제
                            FavoriteLists.setVisibility(View.VISIBLE);
                            delete_index=-1;
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
    void setFavoriteLayout(){
        try {
            //장바구니 항목 개수에 따라 추가
            for(int i=0;i<FavoriteListJSON.length();i++){
                //수평 레이아웃 생성
                final LinearLayout l = new LinearLayout(getApplicationContext());
                l.setOrientation(LinearLayout.HORIZONTAL);
                //json 오브젝트 받아오기
                JSONObject result=FavoriteListJSON.getJSONObject(i);
                String rname=result.getString("restaurant_name"); //가게 이름
                String address=result.getString("address"); //가게 주소
                final String sid=result.getString("supplier_ID");


                //width 세팅
                LinearLayout.LayoutParams nameWidth = new LinearLayout.LayoutParams(findViewById(R.id.rName_t_wrapper).getWidth(),
                        LinearLayout.LayoutParams.MATCH_PARENT);
                LinearLayout.LayoutParams addressWidth = new LinearLayout.LayoutParams(findViewById(R.id.rAddress_t_wrapper).getWidth(),
                        LinearLayout.LayoutParams.MATCH_PARENT);
                LinearLayout.LayoutParams deleteWidth = new LinearLayout.LayoutParams(findViewById(R.id.rDelete_t_wrapper).getWidth(),
                        LinearLayout.LayoutParams.MATCH_PARENT);

                //TextView 생성
                //  가게 이름
                TextView rName_t=new TextView(getApplicationContext());
                LinearLayout rNameWrapper=new LinearLayout(getApplicationContext());
                rNameWrapper.setLayoutParams(nameWidth);
                rNameWrapper.setGravity(Gravity.CENTER);
                rName_t.setText(rname);
                rName_t.setTextSize(20);
                rNameWrapper.addView(rName_t);

                //  가게 주소
                TextView address_t=new TextView(getApplicationContext());
                LinearLayout addressWrapper=new LinearLayout(getApplicationContext());
                addressWrapper.setLayoutParams(addressWidth);
                addressWrapper.setGravity(Gravity.CENTER);
                address_t.setText(String.valueOf(address));
                address_t.setTextSize(20);
                addressWrapper.addView(address_t);


                //삭제 텍스트 생성
                TextView deleteItem=new TextView(getApplicationContext());
                LinearLayout deleteItemWrapper=new LinearLayout(getApplicationContext());
                deleteItemWrapper.setLayoutParams(deleteWidth);
                deleteItemWrapper.setGravity(Gravity.CENTER);
                deleteItem.setText(" X ");
                deleteItemWrapper.addView(deleteItem);

                //수평 레이아웃에 추가
                l.addView(rNameWrapper);
                l.addView(addressWrapper);
                l.addView(deleteItemWrapper);
                l.setBackgroundResource(R.drawable.underline);
                //즐겨찾기 각 가게항목 ArrayList에 추가
                FavoriteList.add(l);
                //가게 상세로 이동
                l.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent;
                        intent = new Intent(
                                getApplicationContext(),
                                SupplierInformActivity.class);
                        intent.putExtra("sid",sid);
                        startActivity(intent);
                    }
                });


                //즐겨찾기 내 아이템 삭제 기능
                deleteItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int index=FavoriteList.indexOf(l);
                        Toast.makeText(getApplicationContext(),String.valueOf(index),Toast.LENGTH_SHORT).show();
                        deleteItem(index,sid);
                        //삭제되는 동안 레이아웃을 잠시 숨김
                        FavoriteLists.setVisibility(View.INVISIBLE);
                    }
                });
            }
            //즐겨찾기 가게들 모두 추가
            for(int i=0;i<FavoriteList.size();i++) {
                FavoriteLists.addView(FavoriteList.get(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    void deleteRefreshWishList(){
       FavoriteLists.removeAllViewsInLayout();
        for(int i=0;i<FavoriteList.size();i++) {
            if(i==delete_index){
                continue;
            }
            FavoriteLists.addView(FavoriteList.get(i));
        }
    }
    void deleteItem(int index, final String sid){
        delete_index=index;
        String url="http://54.180.90.90:3000/api/favorites/delete";
        StringRequest request=new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(getApplicationContext(), response, Toast.LENGTH_SHORT).show();
                        deleteRefreshWishList(); //카트 새로고침
                        //레이아웃 삭제
                        FavoriteList.remove(delete_index);
                        loadFavoriteList(); //JSON 다시 받아오기
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
                params.put("sid", sid);
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
