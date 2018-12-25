package com.example.jin.myapplication;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Image;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Base64;
import android.util.Log;
import android.util.Xml;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import static android.graphics.Color.rgb;


public class MainActivity extends ToolbarActivity {
    ListView listview = null ;
    Button mapButton;
    Button testButton;
    Bitmap bitmap;
    SharedPreferences pref;

    //아이템 리스트 관련 변수들
    LinearLayout SortedItemList;
    ArrayList<String> itemCategoryIdList;
    ArrayList<LinearLayout> SortedItem;
    Spinner sortSpinner;
    ArrayList<String> sortList;
    ArrayAdapter<String> sortAdapt;

    //위치 허가 관련 상수들
    public final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1;
    public final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 2;

    private Location mLocation; // 내 위치를 받는 Location 변수

    private Geocoder geocoder = new Geocoder(MainActivity.this);

    private Button location_button;
    //카테고리 관련
    private ImageView image1;
    private ImageView image2;
    private ImageView image3;
    private ImageView image4;
    private ImageView image5;
    private ImageView image6;
    private ImageView image7;
    private ImageView image8;

    private TextView text1;
    private TextView text2;
    private TextView text3;
    private TextView text4;
    private TextView text5;
    private TextView text6;
    private TextView text7;
    private TextView text8;

    private String category = "전체";
    ArrayList<String> categoryIDList;
    ArrayList<String> categoryList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences pref= getSharedPreferences("pref", MODE_PRIVATE);
        String token=pref.getString("token",null);
        //---------------------------------------------------------파이어 베이스

        //토큰 출력
        //Toast.makeText(getApplicationContext(), token, Toast.LENGTH_SHORT).show();
        listview = (ListView) findViewById(R.id.drawer_menulist) ;
        listview.setAdapter(super.MENU) ;
        listview.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View v, int position, long id) {
                selectDrawable((String)adapterView.getItemAtPosition(position));
            }
        });

        mapButton=findViewById(R.id.map_btn);
        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MapActivity.class);
                MainActivity.this.startActivity(intent);
            }
        });
        /*
        testButton=findViewById(R.id.test_btn);
        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendImageToServer();
            }
        });*/

        //----------------------아이템 리스트 관련----------------------------
        SortedItemList=findViewById(R.id.SortedItemList);
        itemCategoryIdList=new ArrayList<String>();
        SortedItem=new ArrayList<LinearLayout>();
        sortSpinner=findViewById(R.id.sortSpinner);
        sortList=new ArrayList<String>();
        sortAdapt=  new ArrayAdapter<String>(getApplicationContext(),  android.R.layout.simple_spinner_dropdown_item, sortList);
        sortAdapt.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item);
        sortList.add("최근 등록순");
        sortList.add("조회순");
        sortList.add("가격 낮은순");
        sortSpinner.setAdapter(sortAdapt);
        showItemList(1,"-1"); //초기값: 최근등록순, 전체
        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadCategory();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //
        mLocation = new Location("myLocation");

        text1 = (TextView)findViewById(R.id.all_t);
        text2 = (TextView)findViewById(R.id.fastfood_t);
        text3 = (TextView)findViewById(R.id.meat_t);
        text4 = (TextView)findViewById(R.id.japan_t);
        text5 = (TextView)findViewById(R.id.cafe_t);
        text6 = (TextView)findViewById(R.id.fruit_t);
        text7 = (TextView)findViewById(R.id.dessert_t);
        text8 = (TextView)findViewById(R.id.etc_t);

        image1 = (ImageView)findViewById(R.id.all);
        image2 = (ImageView)findViewById(R.id.fastfood);
        image3 = (ImageView)findViewById(R.id.meat);
        image4 = (ImageView)findViewById(R.id.japan);
        image5 = (ImageView)findViewById(R.id.cafe);
        image6 = (ImageView)findViewById(R.id.fruit);
        image7 = (ImageView)findViewById(R.id.dessert);
        image8 = (ImageView)findViewById(R.id.etc);
        text1.setTextColor(rgb(255,255,255));
        text1.setBackgroundColor(rgb(125,181,124));
        image1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setDefault();
                text1.setTextColor(rgb(255,255,255));
                text1.setBackgroundColor(rgb(125,181,124));
                category = "전체";
                loadCategory();
            }
        });

        image2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setDefault();
                text2.setTextColor(rgb(255,255,255));
                text2.setBackgroundColor(rgb(125,181,124));
                category = "치킨/피자";
                loadCategory();
            }
        });

        image3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setDefault();
                text3.setTextColor(rgb(255,255,255));
                text3.setBackgroundColor(rgb(125,181,124));
                category = "족발/보쌈";
                loadCategory();
            }
        });

        image4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setDefault();
                text4.setTextColor(rgb(255,255,255));
                text4.setBackgroundColor(rgb(125,181,124));
                category = "돈까스/일식";
                loadCategory();
            }
        });
        image5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setDefault();
                text5.setTextColor(rgb(255,255,255));
                text5.setBackgroundColor(rgb(125,181,124));
                category = "카페/음료";
                loadCategory();
            }
        });
        image6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setDefault();
                text6.setTextColor(rgb(255,255,255));
                text6.setBackgroundColor(rgb(125,181,124));
                category = "신선품";
                loadCategory();
            }
        });
        image7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setDefault();
                text7.setTextColor(rgb(255,255,255));
                text7.setBackgroundColor(rgb(125,181,124));
                category = "디저트/빵";
                loadCategory();
            }
        });
        image8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setDefault();
                text8.setTextColor(rgb(255,255,255));
                text8.setBackgroundColor(rgb(125,181,124));
                category = "기타";
                loadCategory();
            }
        });
        //카테고리&카테고리 아이디 리스트 초기화
        categoryIDList=new ArrayList<String>();
        categoryList=new ArrayList<String>();
        //현재 위치======================================================
        location_button = (Button)findViewById(R.id.mylocation);

        location_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                location_button.setText("현재 위치 탐색중...");
            }
        });
        //---------------------------------------------------------------------------------------------------------------// 현재 위치 받아오기
        // Location 제공자에서 정보를 얻어오기(GPS)
        // 1. Location을 사용하기 위한 권한을 얻어와야한다 AndroidManifest.xml
        //     ACCESS_FINE_LOCATION : NETWORK_PROVIDER, GPS_PROVIDER
        //     ACCESS_COARSE_LOCATION : NETWORK_PROVIDER
        // 2. LocationManager 를 통해서 원하는 제공자의 리스너 등록
        // 3. GPS 는 에뮬레이터에서는 기본적으로 동작하지 않는다
        // 4. 실내에서는 GPS_PROVIDER 를 요청해도 응답이 없다.  특별한 처리를 안하면 아무리 시간이 지나도
        //    응답이 없다.
        //    해결방법은
        //     ① 타이머를 설정하여 GPS_PROVIDER 에서 일정시간 응답이 없는 경우 NETWORK_PROVIDER로 전환
        //     ② 혹은, 둘다 한꺼번헤 호출하여 들어오는 값을 사용하는 방식.

        // LocationManager 객체를 얻어온다
        final LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        location_button.setText("현재 위치 탐색중...");

        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, // 등록할 위치제공자
                500, // 통지사이의 최소 시간간격 (miliSecond)
                1, // 통지사이의 최소 변경거리 (m)
                mLocationListener);
        lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, // 등록할 위치제공자
                500, // 통지사이의 최소 시간간격 (miliSecond)
                1, // 통지사이의 최소 변경거리 (m)
                mLocationListener);
        //---------------------------------------------------------------------------------------------------------------//

    }
    //카테고리 관련===================================

    void loadCategory(){
        String url="http://54.180.90.90:3000/api/category";
        final JsonArrayRequest request=new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        categoryIDList.clear();
                        categoryList.clear();
                        try {
                            for(int i=0;i<response.length();i++){
                                JSONObject result=response.getJSONObject(i);

                                int id=result.getInt("ID");
                                String name=result.getString("name");
                                if(name.equals("치킨/피자")||name.equals("족발/보쌈")||name.equals("돈까스/일식")||name.equals("카페/음료")||name.equals("신선품")||name.equals("디저트/빵")) {
                                    categoryIDList.add(String.valueOf(id));
                                    categoryList.add(name);
                                }
                            }
                            //goCategorySearch();
                            findCategoryID();
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
    void goCategorySearch() {
        //선택된 카테고리 ID 찾기
        String cateid="";
        for (int i = 0; i < categoryList.size(); i++) {
            if (categoryList.get(i).equals(category)) {
                Intent intent;
                intent = new Intent(
                        getApplicationContext(),
                        CategoryActivity.class);
                intent.putExtra("cateid",categoryIDList.get(i));
                startActivity(intent);
                break;
            }
        }

    }
    void findCategoryID() {
        //선택된 카테고리 ID 찾기
        String cateid="";

        if(category.equals("전체")){
            cateid="-1";
        }
        else if(category.equals("기타")){
            cateid="-2";
        }
        else{
            int i;
            for (i = 0; i < categoryList.size(); i++) {
                if (categoryList.get(i).equals(category)) {
                    cateid=categoryIDList.get(i);
                    break;
                }
            }
        }
        categorySearch(cateid);
    }
    void categorySearch(String categoryID){
        String sort=sortSpinner.getSelectedItem().toString();
        int sortNum=0;
        if(sort.equals("조회순")){
            sortNum=0;
        }
        if(sort.equals("최근 등록순")){
            sortNum=1;
        }
        if(sort.equals("가격 낮은순")){
            sortNum=2;
        }
        showItemList(sortNum,categoryID);
    }
    //아이템 리스트 관련==============================
    void ItemListInit(){
        SortedItem.clear();
        itemCategoryIdList.clear();
        SortedItemList.removeAllViewsInLayout();
    }
    void showItemList(int sort_num,final String selectedCateID){
        String url="http://54.180.90.90:3000/api/item/list/"+sort_num;
        final JsonArrayRequest request=new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        //Toast.makeText(getApplicationContext(),response.toString(),Toast.LENGTH_SHORT).show();
                        ItemListInit();
                        JSONArray arr=null;
                        try {
                            arr=response;

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
                                int count=result.getInt("itemCount");
                                double avg_score=result.getDouble("avg_review_score");

                                //카테고리가 전체인 경우
                                if (selectedCateID.equals("-1")) {

                                }
                                //카테고리가 기타인 경우
                                else if (selectedCateID.equals("-2")) {
                                    int j;
                                    for (j = 0; j < categoryIDList.size(); j++) {
                                        //특정 카테고리인 경우
                                        if (icategoryId.equals(categoryIDList.get(j))) {
                                            break;
                                        }
                                    }
                                    if (j != categoryIDList.size()) {
                                           continue;
                                    }
                                }
                                //특정 카테고리가 선택된 경우
                                else {
                                    //선택된 카테고리와 아이디가 다를 경우
                                    if (!selectedCateID.equals(icategoryId)) {
                                        continue;
                                    }
                                }
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
                                IImage.setLayoutParams(new LinearLayout.LayoutParams(SortedItemList.getWidth()/3,SortedItemList.getWidth()/3));
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
                                if(iname.length()>=6){
                                    IName_t.setTextSize(20);
                                    iname=iname.substring(0,5)+"...";
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
                                avg_score=Math.round(avg_score*100)/(double)100;
                                IScore_t.setText("★"+String.valueOf(avg_score));
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
                                RPrice_t.setText(MainActivity.super.addComma(String.valueOf(rawPrice)));
                                RPrice_t.setTextSize(15);
                                RPrice_t.setPaintFlags(RPrice_t.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                                RPriceWrapper.addView(RPrice_t);
                                LinearLayout IPriceWrapper = new LinearLayout(getApplicationContext());
                                TextView IPrice_t = new TextView(getApplicationContext());
                                IPriceWrapper.setGravity(Gravity.RIGHT);
                                IPrice_t.setGravity(Gravity.CENTER_VERTICAL);
                                IPrice_t.setText(MainActivity.super.addComma(String.valueOf(price)));
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
                                //정렬된 아이템 ArrayList에 추가
                                SortedItem.add(l);
                            }
                            //Item들 모두 추가
                            for(int i=0;i<SortedItem.size();i++) {
                                SortedItemList.addView(SortedItem.get(i));
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_SHORT).show();
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

    public void setDefault() {

        if (category == "전체") { text1.setBackgroundColor(rgb(255,255,255)); text1.setTextColor(rgb(102,102,102)); }

        else if (category == "치킨/피자") { text2.setBackgroundColor(rgb(255,255,255)); text2.setTextColor(rgb(102,102,102)); }
        else if (category == "족발/보쌈") { text3.setBackgroundColor(rgb(255,255,255)); text3.setTextColor(rgb(102,102,102)); }
        else if (category == "돈까스/일식") { text4.setBackgroundColor(rgb(255,255,255)); text4.setTextColor(rgb(102,102,102)); }
        else if (category == "카페/음료") { text5.setBackgroundColor(rgb(255,255,255)); text5.setTextColor(rgb(102,102,102)); }
        else if (category == "신선품") { text6.setBackgroundColor(rgb(255,255,255)); text6.setTextColor(rgb(102,102,102)); }
        else if (category == "디저트/빵")  { text7.setBackgroundColor(rgb(255,255,255)); text7.setTextColor(rgb(102,102,102)); }
        else if (category == "기타") { text8.setBackgroundColor(rgb(255,255,255)); text8.setTextColor(rgb(102,102,102)); }

    }

    private final LocationListener mLocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            //여기서 위치값이 갱신되면 이벤트가 발생한다.
            //값은 Location 형태로 리턴되며 좌표 출력 방법은 다음과 같다.

            Log.d("test", "onLocationChanged, location:" + location);
            double longitude = location.getLongitude(); //경도
            double latitude = location.getLatitude();   //위도
            double altitude = location.getAltitude();   //고도
            float accuracy = location.getAccuracy();    //정확도
            String provider = location.getProvider();   //위치제공자
            //Gps 위치제공자에 의한 위치변화. 오차범위가 좁다.
            //Network 위치제공자에 의한 위치변화
            //Network 위치는 Gps에 비해 정확도가 많이 떨어진다.

            mLocation.setLongitude(longitude);
            mLocation.setLatitude(latitude);
            location_button.setText(getAddressListUsingGeolocation(new GeoLocation(mLocation.getLatitude(),mLocation.getLongitude())));
        }

        public void onProviderDisabled(String provider) {
            // Disabled시
            Log.d("test", "onProviderDisabled, provider:" + provider);
        }

        public void onProviderEnabled(String provider) {
            // Enabled시
            Log.d("test", "onProviderEnabled, provider:" + provider);
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
            // 변경시
            Log.d("test", "onStatusChanged, provider:" + provider + ", status:" + status + " ,Bundle:" + extras);
        }

    };

    public static class GeoLocation {

        double latitude;
        double longitude;

        public GeoLocation(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }

    public ArrayList<GeoLocation> getGeoLocationListUsingAddress(String address) {
        ArrayList<GeoLocation> resultList = new ArrayList<>();

        try {
            List<Address> list = geocoder.getFromLocationName(address, 10);

            for (Address addr : list) {
                resultList.add(new GeoLocation(addr.getLatitude(), addr.getLongitude()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return resultList;
    }

    public String getAddressListUsingGeolocation(GeoLocation location) {
        ArrayList<String> resultList = new ArrayList<>();
        String address[] = {""};
        String result = "";
        try {
            List<Address> list = geocoder.getFromLocation(location.latitude, location.longitude, 10);

            for (Address addr : list) {
                resultList.add(addr.toString());
            }

            address = list.get(0).getAddressLine(0).split(" ");
        } catch (Exception e) {
            e.printStackTrace();
        }
        result += address[1] + " "+ address[2] + " " + address[3];
        return result;
    }

    /*
    void sendImageToServer(){
        //사진 할당
        bitmap = BitmapFactory.decodeResource(getApplicationContext().getResources(),
                R.drawable.ic_ddiptok);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 25, baos);
        //바이트 어레이로 변경
        byte[] imageBytes = baos.toByteArray();
        //어레이를 문자열로
        final String imageString = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        //어레이 길이
        Toast.makeText(getApplicationContext(),String.valueOf(imageString.length()),Toast.LENGTH_SHORT).show();

        //byteArrayToImage(imageBytes);
    }
*/
    void byteArrayToImage(byte[] bytesArr)
    {
        Bitmap bmp = BitmapFactory.decodeByteArray(bytesArr, 0, bytesArr.length);
        ImageView image = (ImageView) findViewById(R.id.all);
        Toast.makeText(getApplicationContext(),bytesArr+"",Toast.LENGTH_SHORT).show();
        //image.setImageBitmap(Bitmap.createScaledBitmap(bmp, image.getWidth(),image.getHeight(), false));
    }

    //뒤로가기 버튼을 두번 연속으로 눌러야 종료되게끔 하는 메소드
    private long time= 0;
    @Override
    public void onBackPressed(){
        if(System.currentTimeMillis()-time>=2000){
            time=System.currentTimeMillis();
            Toast.makeText(getApplicationContext(),"뒤로 버튼을 한번 더 누르면 종료합니다.",Toast.LENGTH_SHORT).show();
        }else if(System.currentTimeMillis()-time<2000){
            finish();
        }
    }
}

