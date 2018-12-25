package com.example.jin.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.graphics.Color.rgb;

public class CategoryActivity extends ToolbarActivity {
    ListView listview;
    SharedPreferences pref;
    LinearLayout SelectedCategoryItemList;
    ArrayList<LinearLayout> SelectedCategoryItem;
    String cateid;

    //카테고리
    ArrayList<String> categoryIDList;
    ArrayList<String> categoryList;
    String category;
    private TextView text1;
    private TextView text2;
    private TextView text3;
    private TextView text4;
    private TextView text5;
    private TextView text6;
    private TextView text7;
    private TextView text8;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);
        listview = (ListView) findViewById(R.id.drawer_menulist) ;
        listview.setAdapter(super.MENU) ;
        listview.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View v, int position, long id) {
                selectDrawable((String)adapterView.getItemAtPosition(position));
            }
        });
        Intent intent = getIntent();
        cateid = intent.getStringExtra("cateid");
        SelectedCategoryItemList=findViewById(R.id.SelectedCategoryItemList);
        SelectedCategoryItem=new ArrayList<LinearLayout>();
        //카테고리&카테고리 아이디 리스트 초기화
        categoryIDList=new ArrayList<String>();
        categoryList=new ArrayList<String>();
        //텍스트뷰 연결결
        text1 = (TextView)findViewById(R.id.all_t);
        text1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                category=text1.getText().toString();
                loadCategory();
            }
        });
        text2 = (TextView)findViewById(R.id.fastfood_t);
        text2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                category=text2.getText().toString();
                loadCategory();
            }
        });
        text3 = (TextView)findViewById(R.id.meat_t);
        text3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                category=text3.getText().toString();
                loadCategory();
            }
        });
        text4 = (TextView)findViewById(R.id.japan_t);
        text4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                category=text4.getText().toString();
                loadCategory();
            }
        });
        text5 = (TextView)findViewById(R.id.china_t);
        text5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                category=text5.getText().toString();
                loadCategory();
            }
        });
        text6 = (TextView)findViewById(R.id.cafe_t);
        text6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                category=text6.getText().toString();
                loadCategory();
            }
        });
        text7 = (TextView)findViewById(R.id.bunsik_t);
        text7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                category=text7.getText().toString();
                loadCategory();
            }
        });
        text8 = (TextView)findViewById(R.id.etc_t);
        text8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                category=text8.getText().toString();
                loadCategory();
            }
        });
        getItemList();
    }
    void loadCategory(){
        String url="http://54.180.90.90:3000/api/category";
        final JsonArrayRequest request=new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            for(int i=0;i<response.length();i++){
                                JSONObject result=response.getJSONObject(i);
                                categoryIDList.add(String.valueOf(result.getInt("ID")));
                                String name=result.getString("name");
                                categoryList.add(name);
                            }
                            CategorySearch();
                            //findCategoryID();
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
    void CategorySearch() {
        //선택된 카테고리 ID 찾기
        for (int i = 0; i < categoryList.size(); i++) {
            if (categoryList.get(i).equals(category)) {
                cateid=categoryIDList.get(i);
                getItemList();
                return;
            }
        }
    }
    void itemListInit(){
        SelectedCategoryItemList.removeAllViewsInLayout();
        SelectedCategoryItem.clear();
    }
   //해당 카테고리의 item 로드
    void getItemList(){
        String url="http://54.180.90.90:3000/api/item/search";
        StringRequest request=new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONArray arr=null;
                        try {
                            itemListInit();
                            arr=new JSONArray(response);
                            for(int i=0;i<arr.length();i++){
                                //수평 레이아웃 생성
                                final LinearLayout l = new LinearLayout(getApplicationContext());
                                l.setOrientation(LinearLayout.HORIZONTAL);
                                //이미지뷰에 레이아웃
                                final LinearLayout ImageViewWrapper = new LinearLayout(getApplicationContext());
                                ImageViewWrapper.setOrientation(LinearLayout.VERTICAL);
                                //상품 이름, 가격 레이아웃
                                final LinearLayout ItemInformWrapper = new LinearLayout(getApplicationContext());
                                ItemInformWrapper.setOrientation(LinearLayout.VERTICAL);

                                //json 오브젝트 받아오기
                                JSONObject result = arr.getJSONObject(i);
                                final String iname = result.getString("itemName"); //상품 이름
                                final int price = result.getInt("salePrice"); //상품 가격
                                final Long itemid=result.getLong("iid"); //상품 아이디

                                //ImageView 생성
                                final ImageView IImage = new ImageView(getApplicationContext());
                                Picasso.get().load("http://54.180.90.90:3000/images/item/999-9999-99991542269644542%EB%B9%A0%EC%97%90%EC%95%BC_%EA%B5%AC%EA%B8%80_%EB%B9%84%EC%A0%84.png").resize(200, 200).into(
                                        new Target(){
                                            //image가 로드된 후에 레이아웃 배치
                                            @Override
                                            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                                IImage.setBackground(new BitmapDrawable(getApplicationContext().getResources(), bitmap));
                                            }
                                            @Override
                                            public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                                                IImage.setImageDrawable(errorDrawable);
                                            }
                                            @Override
                                            public void onPrepareLoad(final Drawable placeHolderDrawable) {
                                                IImage.setImageDrawable(placeHolderDrawable);
                                            }
                                        });
                                //TextView 생성
                                //  상품 이름
                                LinearLayout INameWrapper = new LinearLayout(getApplicationContext());
                                TextView IName_t = new TextView(getApplicationContext());
                                IName_t.setGravity(Gravity.CENTER_VERTICAL);
                                IName_t.setText(iname);
                                IName_t.setTextSize(30);
                                IName_t.setTextColor(Color.BLACK);
                                INameWrapper.addView(IName_t);

                                // 상품 가격
                                LinearLayout IPriceWrapper = new LinearLayout(getApplicationContext());
                                TextView IPrice_t = new TextView(getApplicationContext());
                                IPrice_t.setGravity(Gravity.CENTER_VERTICAL);
                                IPrice_t.setText(String.valueOf(price));
                                IPrice_t.setTextSize(20);
                                IPriceWrapper.addView(IPrice_t);

                                //수평 레이아웃에 추가
                                ImageViewWrapper.addView(IImage);
                                ItemInformWrapper.addView(INameWrapper);
                                ItemInformWrapper.addView(IPriceWrapper);
                                l.addView(ImageViewWrapper);
                                l.addView(ItemInformWrapper);
                                l.setBackgroundResource(R.drawable.underline);

                                //레이아웃 사이즈 설정
                                LinearLayout.LayoutParams lparam = new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                                ItemInformWrapper.setLayoutParams(lparam);

                                //정렬 및 패딩 설정
                                ItemInformWrapper.setVerticalGravity(Gravity.CENTER_VERTICAL);
                                ItemInformWrapper.setPadding(30,0,0,30);

                                //아이템 상세 정보 페이지 이동 리스너 설정
                                l.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Intent intent;
                                        intent = new Intent(
                                                getApplicationContext(),
                                                ItemInformActivity.class);
                                        intent.putExtra("iid",itemid);
                                        startActivity(intent);
                                    }
                                });

                                //상품 ArrayList에 추가
                                SelectedCategoryItem.add(l);
                            }
                            //상품들 모두 레이아웃에 추가
                            for(int i=0;i<SelectedCategoryItem.size();i++) {
                                SelectedCategoryItemList.addView(SelectedCategoryItem.get(i));
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
                params.put("cateid",cateid);
                return params;
            }
        };
        mQueue.add(request);
    }
}
