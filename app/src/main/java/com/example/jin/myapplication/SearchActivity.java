package com.example.jin.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
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
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SearchActivity extends ToolbarActivity {
    ListView listview;
    EditText SearchBar;
    Button deleteButton;
    Button searchButton;
    LinearLayout SearchResultList;
    ArrayList<LinearLayout> SearchResultItem;
    RequestQueue mQueue;
    SharedPreferences pref;
    String token;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
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
        //검색 결과
        SearchResultList=findViewById(R.id.SearchResultList);
        SearchResultItem=new ArrayList<LinearLayout>();
        //검색 버튼
        searchButton=findViewById(R.id.btn_search);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SearchResultList.removeAllViewsInLayout();
                SearchResultItem.clear();
                loadSearchResultList();
            }
        });
        //검색바
        SearchBar=findViewById(R.id.SearchBar);
        deleteButton=findViewById(R.id.btn_clear);
        SearchBar.addTextChangedListener(textWatcher());
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SearchBar.setText("");
            }
        });



    }
    // 검색바 X표시
    private TextWatcher textWatcher() {
        return new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!SearchBar.getText().toString().equals("")) {
                    deleteButton.setVisibility(View.VISIBLE);
                } else { //not include text
                    deleteButton.setVisibility(View.GONE);
                }
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {            }
            @Override
            public void afterTextChanged(Editable s) {            }
        };
    }
    void loadSearchResultList(){
        String url="http://54.180.90.90:3000/api/item/search";
        StringRequest request=new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONArray arr=null;
                        try {
                            arr=new JSONArray(response);
                            //검색어에 해당하는 항목 개수에 따라 추가
                            for(int i=0;i<arr.length();i++) {
                                //json 오브젝트 받아오기
                                JSONObject result = arr.getJSONObject(i);
                                final String rname=result.getString("restaurant_name"); //가게 이름
                                String iname = result.getString("itemName"); //상품 이름
                                final int rawPrice = result.getInt("rawPrice"); //원가
                                final int price = result.getInt("salePrice"); //상품 가격
                                double sale=(rawPrice-price)/(double)rawPrice*100;
                                final Long iid = result.getLong("iid"); //상품 아이디
                                String icategoryId = result.getString("categoryId"); //카테고리 아이디

                                //수평 레이아웃 생성
                                final LinearLayout l = new LinearLayout(getApplicationContext());
                                l.setOrientation(LinearLayout.HORIZONTAL);


                                //이미지뷰 레이아웃
                                final LinearLayout ImageViewWrapper = new LinearLayout(getApplicationContext());
                                ImageViewWrapper.setOrientation(LinearLayout.VERTICAL);
                                //가게 이름&상품 정보 레이아웃
                                final LinearLayout NameWrapper = new LinearLayout(getApplicationContext());
                                NameWrapper.setOrientation(LinearLayout.VERTICAL);
                                //NameWrapper.setLayoutParams(new LinearLayout.LayoutParams(SortedItemList.getWidth()/3,ViewGroup.LayoutParams.MATCH_PARENT));
                                //가격 레이아웃
                                final LinearLayout ItemInformWrapper = new LinearLayout(getApplicationContext());
                                ItemInformWrapper.setOrientation(LinearLayout.VERTICAL);

                                //ImageView 생성
                                final ImageView IImage = new ImageView(getApplicationContext());
                                IImage.setLayoutParams(new LinearLayout.LayoutParams(SearchResultList.getWidth()/3,SearchResultList.getWidth()/3));
                                IImage.setPadding(30,30,30,30);
                                final String imageData=result.getString("imagePath");
                                byte[] imageBytes = Base64.decode(imageData, Base64.DEFAULT);
                                Bitmap decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                                IImage.setImageBitmap(decodedImage);

                                //TextView 생성
                                //  가게이름
                                LinearLayout RNameWrapper = new LinearLayout(getApplicationContext());
                                TextView RName_t = new TextView(getApplicationContext());
                                RName_t.setText(rname);
                                RName_t.setTextSize(20);
                                RNameWrapper.addView(RName_t);
                                //  상품 이름
                                LinearLayout INameWrapper = new LinearLayout(getApplicationContext());
                                TextView IName_t = new TextView(getApplicationContext());
                                if(iname.length()>=8){
                                    iname=iname.substring(0,9)+"...";
                                }
                                IName_t.setText(iname);
                                IName_t.setTextSize(25);

                                IName_t.setTextColor(Color.BLACK);
                                INameWrapper.addView(IName_t);
                                //  상품 평점
                                LinearLayout IScoreWrapper = new LinearLayout(getApplicationContext());
                                final TextView IScore_t = new TextView(getApplicationContext());
                                setReviewScore(iid, IScore_t);
                                IScore_t.setText("★");
                                IScore_t.setTextSize(25);
                                IScore_t.setTextColor(Color.rgb(255,152,0));
                                IScoreWrapper.addView(IScore_t);

                                // 상품 가격
                                LinearLayout SaleWrapper = new LinearLayout(getApplicationContext());
                                TextView sale_t = new TextView(getApplicationContext());
                                SaleWrapper.setGravity(Gravity.RIGHT);
                                sale_t.setGravity(Gravity.CENTER_VERTICAL);
                                String salePercent=String.valueOf(sale);
                                sale_t.setText(String.valueOf(salePercent.substring(0,salePercent.lastIndexOf("."))+"%"));
                                sale_t.setTextColor(Color.RED);
                                sale_t.setTextSize(30);
                                SaleWrapper.addView(sale_t);
                                LinearLayout RPriceWrapper = new LinearLayout(getApplicationContext());
                                TextView RPrice_t = new TextView(getApplicationContext());
                                RPriceWrapper.setGravity(Gravity.RIGHT);
                                RPrice_t.setGravity(Gravity.CENTER_VERTICAL);
                                RPrice_t.setText(SearchActivity.super.addComma(String.valueOf(rawPrice)));
                                RPrice_t.setTextSize(15);
                                RPrice_t.setPaintFlags(RPrice_t.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                                RPriceWrapper.addView(RPrice_t);
                                LinearLayout IPriceWrapper = new LinearLayout(getApplicationContext());
                                TextView IPrice_t = new TextView(getApplicationContext());
                                IPriceWrapper.setGravity(Gravity.RIGHT);
                                IPrice_t.setGravity(Gravity.CENTER_VERTICAL);
                                IPrice_t.setText(SearchActivity.super.addComma(String.valueOf(price)));
                                IPrice_t.setTextSize(30);
                                IPrice_t.setTextColor(Color.BLACK);
                                IPriceWrapper.addView(IPrice_t);

                                //수평 레이아웃에 추가
                                ImageViewWrapper.addView(IImage);
                                NameWrapper.addView(RNameWrapper);
                                NameWrapper.addView(INameWrapper);
                                NameWrapper.addView(IScoreWrapper);
                                ItemInformWrapper.addView(SaleWrapper);
                                ItemInformWrapper.addView(IPriceWrapper);
                                ItemInformWrapper.addView(RPriceWrapper);


                                l.addView(ImageViewWrapper);
                                l.addView(NameWrapper);
                                l.addView(ItemInformWrapper);
                                l.setBackgroundResource(R.drawable.round_shadow);
                                /*변경하고 싶은 레이아웃의 파라미터 값을 가져 옴*/
                                LinearLayout.LayoutParams plControl = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
                                /*해당 margin값 변경*/
                                plControl.setMargins(20,20,20,20);
                                /*변경된 값의 파라미터를 해당 레이아웃 파라미터 값에 셋팅*/
                                l.setLayoutParams(plControl);

                                //레이아웃 사이즈 설정
                                LinearLayout.LayoutParams lparam = new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                                ItemInformWrapper.setLayoutParams(lparam);

                                //정렬 및 패딩 설정
                                NameWrapper.setVerticalGravity(Gravity.CENTER_VERTICAL);
                                NameWrapper.setPadding(30,30,10,30);
                                ItemInformWrapper.setVerticalGravity(Gravity.CENTER_VERTICAL);
                                ItemInformWrapper.setPadding(30,0,30,0);

                                //아이템 상세 정보 페이지 이동 리스너 설정
                                l.setOnClickListener(new View.OnClickListener() {
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

                                //주문 아이템 ArrayList에 추가
                                SearchResultItem.add(l);
                            }
                            //장바구니 내의 Item들 모두 추가
                            for(int i=0;i<SearchResultItem.size();i++) {
                                SearchResultList.addView(SearchResultItem.get(i));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener()
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
                params.put("name",SearchBar.getText().toString());
                /*params.put("iid",SearchBar.getText().toString());
                params.put("sid",SearchBar.getText().toString());*/
                return params;
            }
        };
        mQueue.add(request);
    }
    void setReviewScore(final long iid,final TextView avgScore){
        String url="http://54.180.90.90:3000/api/review/"+iid;
        final JsonArrayRequest request=new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        //Toast.makeText(getApplicationContext(), response.toString(), Toast.LENGTH_SHORT).show();
                        int sum=0;
                        float avg=0;
                        try {
                            //장바구니 항목 개수에 따라 추가
                            for(int i=0;i<response.length();i++) {
                                //json 오브젝트 받아오기
                                JSONObject result = response.getJSONObject(i);
                                int score = result.getInt("score"); // 평점
                                sum += score;
                            }
                            avg=sum/(float)response.length();
                            avg=Math.round(avg*100)/(float)100;
                            avgScore.setText("★"+String.valueOf(avg));

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
