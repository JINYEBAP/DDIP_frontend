package com.example.jin.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.location.Address;
import android.location.Geocoder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
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

public class ItemInformActivity extends ToolbarActivity {
    ListView listview;
    Long iid;
    Button wishButton;
    Spinner amountSpinner;
    ArrayList<String> amountList;
    ArrayAdapter<String> amountAdapt;
    Button cartButton;
    Button orderButton;
    Button goReviewButton;
    Button reviewButton;
    Button goSPinformButton;
    RequestQueue mQueue;
    SharedPreferences pref;
    String token;
    String TYPE; //업주인지 구분
    String rName; //리뷰 목록에 넘겨주기 위한 String
    String iName; //리뷰 목록에 넘겨주기 위한 String
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_inform);
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
        iid = intent.getLongExtra("iid",-1);
        //수량
        amountSpinner=findViewById(R.id.amountSpinner);
        amountList=new ArrayList<String>();
        amountAdapt=  new ArrayAdapter<String>(getApplicationContext(),  android.R.layout.simple_spinner_dropdown_item, amountList);
        amountAdapt.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item);

        //아이템 정보 로드
        loadItemInform();
        //가게 정보 로드
        loadSupplierInform();

        //각종 버튼 리스너들
        wishButton=findViewById(R.id.wish_btn);
        wishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TYPE==null || !TYPE.equals("customer")){
                    Toast.makeText(getApplicationContext(),"회원만 가능한 서비스 입니다.",Toast.LENGTH_SHORT).show();
                    return;
                }
                inputWishList();
            }
        });
        cartButton=findViewById(R.id.cart_btn);
        cartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TYPE==null || !TYPE.equals("customer")){
                    Toast.makeText(getApplicationContext(),"회원만 가능한 서비스 입니다.",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(amountSpinner.getSelectedItem().toString().equals("0")){
                    Toast.makeText(getApplicationContext(),"상품이 모두 판매되었습니다.",Toast.LENGTH_SHORT).show();
                    return;
                }
                inputCart();
            }
        });
        orderButton=findViewById(R.id.order_btn);
        orderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TYPE==null || !TYPE.equals("customer")){
                    Toast.makeText(getApplicationContext(),"회원만 가능한 서비스 입니다.",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(amountSpinner.getSelectedItem().toString().equals("0")){
                    Toast.makeText(getApplicationContext(),"상품이 모두 판매되었습니다.",Toast.LENGTH_SHORT).show();
                    return;
                }
                itemOrder();
            }
        });
        goReviewButton=findViewById(R.id.goReveiw_btn);
        goReviewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;
                intent = new Intent(
                        getApplicationContext(),
                        ReviewListActivity.class);
                intent.putExtra("iid",iid);
                intent.putExtra("rname",rName);
                intent.putExtra("iname",iName);
                startActivity(intent);
            }
        });
        reviewButton=findViewById(R.id.review_btn);
        reviewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TYPE==null || !TYPE.equals("customer")){
                    Toast.makeText(getApplicationContext(),"회원만 가능한 서비스 입니다.",Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent;
                intent = new Intent(
                        getApplicationContext(),
                        WriteReviewActivity.class);
                intent.putExtra("iid",iid);
                startActivity(intent);
            }
        });
        goSPinformButton=findViewById(R.id.goSupplierInform_btn);
    }
    void loadCategory(final int categoryId){
        String url="http://54.180.90.90:3000/api/category";
        final JsonArrayRequest request=new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            for(int i=0;i<response.length();i++){
                                JSONObject result=response.getJSONObject(i);
                                if(categoryId==result.getInt("ID")){
                                    TextView cateName=findViewById(R.id.categoryName);
                                    cateName.setText(result.getString("name"));
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
    void loadItemInform(){
        String url="http://54.180.90.90:3000/api/item/detail/"+String.valueOf(iid);
        //상품 정보
        final StringRequest request=new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        JSONObject result=null;
                        try {
                            result=new JSONObject(response);
                            loadCategory(result.getInt("categoryId"));
                            String iname=result.getString("itemName");
                            iName=iname;
                            int rawPrice=result.getInt("rawPrice");
                            int price=result.getInt("salePrice");
                            double sale=(rawPrice-price)/(double)rawPrice*100;
                            int amount=result.getInt("itemCount");
                            String start_time=result.getString("startTime"); //판매 시작 시간
                            if(!start_time.equals("null")){
                                start_time=start_time.substring(0,start_time.lastIndexOf("T"))+"\n"+start_time.substring(start_time.lastIndexOf("T")+1,start_time.lastIndexOf("."));
                            }
                            String end_time=result.getString("endTime"); //판매 종료 시간
                            if(!end_time.equals("null")){
                                end_time=end_time.substring(0,end_time.lastIndexOf("T"))+"\n"+end_time.substring(end_time.lastIndexOf("T")+1,end_time.lastIndexOf("."));
                            }
                            ImageView IImage=findViewById(R.id.imageView3);
                            if(amount==0){
                                IImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_soldout));
                            }
                            else {
                                final String imageData = result.getString("imagePath");
                                byte[] imageBytes = Base64.decode(imageData, Base64.DEFAULT);
                                Bitmap decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                                IImage.setImageBitmap(decodedImage);
                            }
                            amountList.clear();
                            for(int i=0;i<amount;i++){
                                amountList.add(String.valueOf(i+1));
                            }
                            if(amount==0){amountList.add(String.valueOf(0));}
                            amountSpinner.setAdapter(amountAdapt);

                            //아이템 정보 세팅
                            TextView ItemName=findViewById(R.id.ItemTitle);
                            ItemName.setText(iname); //상품 이름
                            TextView ItemRawCost=findViewById(R.id.itemRawCost);
                            ItemRawCost.setText(String.valueOf(rawPrice)); //상품 원가
                            ItemRawCost.setPaintFlags(ItemRawCost.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                            TextView ItemPrice=findViewById(R.id.itemCost);
                            ItemPrice.setText(String.valueOf(price)+"원"); //상품 가격
                            TextView ItemCount=findViewById(R.id.itemCount);
                            ItemCount.setText(String.valueOf(amount)); //상품 수량
                            TextView ItemSale=findViewById(R.id.percent);
                            TextView startTime=findViewById(R.id.startTime); //판매 시작 시간
                            startTime.setText(start_time);
                            TextView endTime=findViewById(R.id.endTime); //판매 종료 시간
                            endTime.setText(end_time);
                            //소수점 아래 버리기
                            String salePercent=String.valueOf(sale);
                            ItemSale.setText(salePercent.substring(0,salePercent.lastIndexOf("."))+"%"); //상품 할인율

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
    void loadSupplierInform(){
        String url="http://54.180.90.90:3000/api/supplier/detail/item/"+String.valueOf(iid);
        //가게 정보
        final StringRequest request=new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        JSONArray arr=null;
                        try {
                            arr=new JSONArray(response);
                            JSONObject result=arr.getJSONObject(0);
                            //가게 정보 리스너 등록 (sid 전달)
                            final String sid=result.getString("ID");
                            goSPinformButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent;
                                    intent = new Intent(
                                            getApplicationContext(),
                                            SupplierInformActivity.class);
                                    intent.putExtra("iid",iid);
                                    intent.putExtra("sid",sid);
                                    startActivity(intent);
                                }
                            });
                            String rname=result.getString("rname");
                            rName=rname;
                            //double latitude=result.getDouble("latitude");
                            //double longitude=result.getDouble("longitude");
                            String address=result.getString("address");
                            TextView SupplierName=findViewById(R.id.supplierName);
                            SupplierName.setText(rname);
                            TextView SupplierAddress=findViewById(R.id.supplierLocation);
                            //String address=getAddressListUsingGeolocation(new GeoLocation(latitude,longitude));
                            SupplierAddress.setText(address);
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
    void inputWishList(){
        String url="http://54.180.90.90:3000/api/wishlist";
        StringRequest request=new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        JSONObject msg=null;
                        try {
                            msg=new JSONObject(response);
                            if(msg.getString("message").equals("duplicate")){
                                deleteWishList();
                            }
                            if(msg.getString("message").equals("success")){
                                Toast.makeText(getApplicationContext(), "찜목록에 추가하였습니다.", Toast.LENGTH_SHORT).show();
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
                params.put("iid",String.valueOf(iid));
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
    void deleteWishList(){
        String url="http://54.180.90.90:3000/api/wishlist/delete";
        StringRequest request=new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(getApplicationContext(), "찜목록에서 삭제합니다.", Toast.LENGTH_SHORT).show();
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
                params.put("iid",String.valueOf(iid));
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
    void inputCart(){
        String url="http://54.180.90.90:3000/api/shopping_cart";
        StringRequest request=new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONObject msg=null;
                        try {
                            msg=new JSONObject(response);
                            if(msg.getString("message").equals("duplicate")){
                                Toast.makeText(getApplicationContext(), "이미 장바구니에 있는 상품입니다. 수량을 변경합니다.", Toast.LENGTH_SHORT).show();
                                ItemInformActivity.super.updateCart(iid, Integer.parseInt(amountSpinner.getSelectedItem().toString()));
                            }
                            if(msg.getString("message").equals("success")){
                                Toast.makeText(getApplicationContext(), "장바구니에 담았습니다.", Toast.LENGTH_SHORT).show();
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
                params.put("iid",String.valueOf(iid));
                params.put("amount",amountSpinner.getSelectedItem().toString());
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
    void itemOrder(){
        String url="http://54.180.90.90:3000/api/order";
        StringRequest request=new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(getApplicationContext(), "주문이 완료되었습니다.", Toast.LENGTH_SHORT).show();
                        loadItemInform();
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
                params.put("payment","cash");
                params.put("iid",String.valueOf(iid));
                params.put("amount",amountSpinner.getSelectedItem().toString());
                params.put("time","10");
                params.put("length",String.valueOf(1));
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
