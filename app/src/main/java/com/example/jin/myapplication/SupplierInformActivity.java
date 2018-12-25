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
import android.util.Base64;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
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
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SupplierInformActivity extends ToolbarActivity {
    ListView listview;
    RequestQueue mQueue;
    SharedPreferences pref;
    String token;
    long iid;
    String sid;
    String TYPE;
    LinearLayout SupplierItemList;
    ArrayList<LinearLayout> SupplierItem;
    Button favoriteButton;

    Button searchMapButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_supplier_inform);
        mQueue=Volley.newRequestQueue(this);
        pref=getSharedPreferences("pref", MODE_PRIVATE);
        token=pref.getString("token",null);
        TYPE=pref.getString("type",null);
        listview = (ListView) findViewById(R.id.drawer_menulist) ;
        listview.setAdapter(super.MENU) ;
        listview.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View v, int position, long id) {
                selectDrawable((String)adapterView.getItemAtPosition(position));
            }
        });
        Intent intent = getIntent();
        iid= intent.getLongExtra("iid",-1);
        sid = intent.getStringExtra("sid");
        //Toast.makeText(getApplicationContext(),sid,Toast.LENGTH_LONG).show();
        getItemList();
        SupplierItemList=findViewById(R.id.SupplierItemList);
        SupplierItem=new ArrayList<LinearLayout>();
        //버튼 리스너 설정
        favoriteButton=findViewById(R.id.favorite_btn);
        favoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TYPE==null || !TYPE.equals("customer")){
                    Toast.makeText(getApplicationContext(),"회원만 가능한 서비스 입니다.",Toast.LENGTH_SHORT).show();
                    return;
                }
                inputFavorite();
            }
        });
        searchMapButton=findViewById(R.id.searchMap_btn);
    }
    void getRname(){
        String url="http://54.180.90.90:3000/api/supplier/detail/item/"+String.valueOf(iid);
        //가게 정보
        final StringRequest request=new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //Toast.makeText(getApplicationContext(),response,Toast.LENGTH_LONG).show();
                        JSONArray arr=null;
                        try {
                            arr=new JSONArray(response);
                            JSONObject result=arr.getJSONObject(0);
                            final String rname=result.getString("rname");
                            final double latitude=result.getDouble("latitude");
                            final double longitude=result.getDouble("longitude");
                            final String address=result.getString("address");
                            TextView SupplierName=findViewById(R.id.supplierNameInform);
                            SupplierName.setText(rname);
                            TextView SupplierAddress=findViewById(R.id.supplierLocationInform);
                            //String address=getAddressListUsingGeolocation(new GeoLocation(latitude,longitude));
                            SupplierAddress.setText(address);
                            searchMapButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent;
                                    intent = new Intent(
                                            getApplicationContext(),
                                            SupplierInformMapActivity.class);
                                    intent.putExtra("rname",rname);
                                    intent.putExtra("address",address);
                                    intent.putExtra("longitude",longitude);
                                    intent.putExtra("latitude",latitude);
                                    startActivity(intent);
                                }
                            });
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
                        //Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_SHORT).show();
                    }
                });
        mQueue.add(request);
    }
    void getItemList(){
        String url="http://54.180.90.90:3000/api/item/search";
        StringRequest request=new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //Toast.makeText(getApplicationContext(),response,Toast.LENGTH_LONG).show();
                        JSONArray arr=null;
                        try {
                            arr=new JSONArray(response);

                            for(int i=0;i<arr.length();i++) {
                                //json 오브젝트 받아오기
                                JSONObject result = arr.getJSONObject(i);
                                final String rname=result.getString("restaurant_name"); //가게 이름
                                String iname = result.getString("itemName"); //상품 이름
                                final int rawPrice = result.getInt("rawPrice"); //원가
                                final int price = result.getInt("salePrice"); //상품 가격
                                double sale=(rawPrice-price)/(double)rawPrice*100;
                                final Long itemid = result.getLong("iid"); //상품 아이디
                                String icategoryId = result.getString("categoryId"); //카테고리 아이디
                                int count=result.getInt("itemCount");
                                iid=itemid;
                                getRname();

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
                                IImage.setLayoutParams(new LinearLayout.LayoutParams(SupplierItemList.getWidth()/3,SupplierItemList.getWidth()/3));
                                IImage.setPadding(30,30,30,30);
                                if(count==0){
                                    IImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_soldout));
                                }
                                else {
                                    final String imageData = result.getString("imagePath");
                                    byte[] imageBytes = Base64.decode(imageData, Base64.DEFAULT);
                                    Bitmap decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                                    IImage.setImageBitmap(decodedImage);
                                }
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
                                    IName_t.setTextSize(20);
                                    iname=iname.substring(0,6)+"...";
                                }
                                else{
                                    IName_t.setTextSize(25);
                                }
                                IName_t.setText(iname);

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
                                RPrice_t.setText(SupplierInformActivity.super.addComma(String.valueOf(rawPrice)));
                                RPrice_t.setTextSize(15);
                                RPrice_t.setPaintFlags(RPrice_t.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                                RPriceWrapper.addView(RPrice_t);
                                LinearLayout IPriceWrapper = new LinearLayout(getApplicationContext());
                                TextView IPrice_t = new TextView(getApplicationContext());
                                IPriceWrapper.setGravity(Gravity.RIGHT);
                                IPrice_t.setGravity(Gravity.CENTER_VERTICAL);
                                IPrice_t.setText(SupplierInformActivity.super.addComma(String.valueOf(price)));
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
                                        intent.putExtra("iid",itemid);
                                        startActivity(intent);
                                    }
                                });
                                //상품 ArrayList에 추가
                                SupplierItem.add(l);
                            }
                            //상품들 모두 레이아웃에 추가
                            for(int i=0;i<SupplierItem.size();i++) {
                                SupplierItemList.addView(SupplierItem.get(i));
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
                params.put("sid",sid);
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
    void inputFavorite() {
        String url="http://54.180.90.90:3000/api/favorites";
        StringRequest request=new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //Toast.makeText(getApplicationContext(), response, Toast.LENGTH_SHORT).show();
                        JSONObject msg=null;
                        try {
                            msg=new JSONObject(response);
                            if(msg.getString("message").equals("duplicate")){
                                Toast.makeText(getApplicationContext(), "즐겨찾기에서 삭제합니다.", Toast.LENGTH_SHORT).show();
                                deleteItem();
                            }
                            if(msg.getString("message").equals("success")){
                                Toast.makeText(getApplicationContext(), "즐겨찾기에 추가했습니다.", Toast.LENGTH_SHORT).show();
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
                params.put("sid",sid);
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
    void deleteItem(){
        String url="http://54.180.90.90:3000/api/favorites/delete";
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
