package com.example.jin.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import android.widget.SearchView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ToolbarActivity extends AppCompatActivity {

        String[] items = {"로그인", "회원가입", "주문 내역", "설정", "고객센터"};
        ArrayList<String> menuList = new ArrayList<String>();
        ArrayAdapter<String> MENU;
        SharedPreferences pref;
        String token;
        RequestQueue mQueue;
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_toolbar);
            //폰트 정의
            //메뉴 리스트 추가
            //로그인 상태
            mQueue=Volley.newRequestQueue(this);
            pref = getSharedPreferences("pref", MODE_PRIVATE);
            token = pref.getString("token", null);
            String ID = pref.getString("cid", null); //현재 로그인한 사용자의 id
            String TYPE= pref.getString("type",null); //현재 로그인한 사용자의 타입
            String NAME=pref.getString("cname",null); //현재 로그인한 사용자의 이름
            menuList.clear();
            //비회원일 경우
            if (token == null) {
                menuList.add("로그인");
                menuList.add("회원가입");
                menuList.add("설정");
                menuList.add("고객센터");
            }
            else if(TYPE.equals("supplier")){
                menuList.add("판매 등록/관리");
                menuList.add("주문 접수 내역");
                menuList.add("로그아웃");
            }
            //관리자일 경우
            else if (ID != null && ID.equals("999-9999-9999")) {
                menuList.clear();
                menuList.add("카테고리 추가");
                menuList.add("FAQ 관리");
                menuList.add("업주 등록");
                menuList.add("로그아웃");
            } else {
                menuList.clear();
                menuList.add("마이페이지");
                menuList.add("장바구니");
                menuList.add("삽니다 등록/관리");
                //menuList.add("공지/이벤트");
                menuList.add("설정");
                menuList.add("고객센터");
                menuList.add("로그아웃");
            }
            MENU = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, menuList);
        }

        @Override
        public void setContentView(int layoutResID) {
            LinearLayout fullView = (LinearLayout) getLayoutInflater().inflate(R.layout.activity_toolbar, null);
            FrameLayout activityContainer = (FrameLayout) fullView.findViewById(R.id.activity_content);
            getLayoutInflater().inflate(layoutResID, activityContainer, true);
            super.setContentView(fullView);

            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            //툴바 사용여부 결정(기본적으로 사용)
            if (useToolbar()) {
                setSupportActionBar(toolbar);
                //기존 title 제거
                getSupportActionBar().setDisplayShowTitleEnabled(false);
                //홈으로 가는 버튼
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_home);
            } else {
                toolbar.setVisibility(View.GONE);
            }


        }
        protected boolean useToolbar() {
            return true;
        }

        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            MenuInflater inflater=getMenuInflater();
            inflater.inflate(R.menu.menu, menu);
            return true;
        }

        //추가된 소스, ToolBar에 추가된 항목의 select 이벤트를 처리하는 함수
        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            //return super.onOptionsItemSelected(item);
            Intent intent;
            switch (item.getItemId()) {
                case R.id.action_menu:
                    // User chose the "Settings" item, show the app settings UI...
                    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer);
                    if (drawer.isDrawerOpen(Gravity.RIGHT)) {
                        drawer.closeDrawer(Gravity.RIGHT);
                    } else {
                        drawer.openDrawer(Gravity.RIGHT);
                    }


                    return true;
                case R.id.action_search:
                    intent = new Intent(
                            getApplicationContext(),
                            SearchActivity.class);
                    startActivity(intent);
                    return true;
                //홈버튼
                case (16908332):
                    intent = new Intent(
                            getApplicationContext(),
                            MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    return true;
                default:
                    // If we got here, the user's action was not recognized.
                    // Invoke the superclass to handle it.
                    Toast.makeText(getApplicationContext(), String.valueOf(item.getItemId()), Toast.LENGTH_LONG).show();
                    return super.onOptionsItemSelected(item);

            }
        }

        void selectDrawable(String menuItem) {
            Intent intent;
            switch (menuItem) {
                case "로그인":
                    intent = new Intent(
                            getApplicationContext(),
                            LoginActivity.class);
                    startActivity(intent);
                    break;
                case "로그아웃":
                    SharedPreferences.Editor editor = pref.edit();
                    editor.remove("token");
                    editor.remove("cid");
                    editor.remove("type");
                    editor.commit();
                    intent = new Intent(
                            getApplicationContext(),
                            MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    break;
                case "회원가입":
                    intent = new Intent(
                            getApplicationContext(),
                            SignUpActivity.class);
                    startActivity(intent);
                    break;
                case "마이페이지":
                    intent = new Intent(
                            getApplicationContext(),
                            MypageActivity.class);
                    startActivity(intent);
                    break;
                case "주문 내역":
                    intent = new Intent(
                            getApplicationContext(),
                            OrderListActivity.class);
                    startActivity(intent);
                    break;
                case "삽니다 등록/관리":
                    intent = new Intent(
                            getApplicationContext(),
                            WantToBuyActivity.class);
                    startActivity(intent);
                    break;
                case "판매 등록/관리":
                    intent = new Intent(
                            getApplicationContext(),
                            UploadItemActivity.class);
                    startActivity(intent);
                    break;
                case "주문 접수 내역":
                    intent = new Intent(
                            getApplicationContext(),
                            TakeOrderActivity.class);
                    startActivity(intent);
                    break;
                case "설정":

                    break;
                case "고객센터":
                    break;
                case "장바구니":
                    intent = new Intent(
                            getApplicationContext(),
                            CustomCartActivity.class);
                    startActivity(intent);
                    break;
                case "카테고리 추가":
                    intent = new Intent(
                            getApplicationContext(),
                            AddCategoryActivity.class);
                    startActivity(intent);
                    break;
                case "FAQ 관리":
                    intent = new Intent(
                            getApplicationContext(),
                            ManageFAQActivity.class);
                    startActivity(intent);
                    break;
                case "업주 등록":
                    intent = new Intent(
                            getApplicationContext(),
                            SupplierSignUpActivity.class);
                    startActivity(intent);
                    break;
                case "임시메뉴":
                    intent = new Intent(
                            getApplicationContext(),
                            ItemInformActivity.class);
                    startActivity(intent);
                    break;
            }
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer);
            drawer.closeDrawer(Gravity.RIGHT);
        }




    //카트 중복 상품에 대하여 수량을 바꾸어주는 함수
        long update_iid;
        int update_amount;
        void updateCart(long iid,int amount){
            update_iid=iid;
            update_amount=amount;
            String url="http://54.180.90.90:3000/api/shopping_cart/update";
            StringRequest request=new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {

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
                    params.put("iid",String.valueOf(update_iid));
                    params.put("amount",String.valueOf(update_amount));
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
        //카테고리 아이디->카테고리 이름
        String cate_name;
        void cate_idTocate_name(final int cate_id){
            String url="http://54.180.90.90:3000/api/category";
            final JsonArrayRequest request=new JsonArrayRequest(Request.Method.GET, url, null,
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            try {
                                for(int i=0;i<response.length();i++){
                                    JSONObject result=response.getJSONObject(i);
                                    int ID=result.getInt("ID");
                                    if(cate_id==ID){
                                        cate_name=result.getString("name");
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                }
            });
            mQueue.add(request);
        }
        String addComma(String Price){
            // 300,000
            for(int i=Price.length()-1,j=1;i>0;i--,j++){
                if(j%3==0){
                    Price=Price.substring(0,i)+","+Price.substring(i,Price.length());
                }
            }
            return Price;
        }
}
