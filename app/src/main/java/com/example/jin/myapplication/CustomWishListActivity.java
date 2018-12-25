package com.example.jin.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
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

public class CustomWishListActivity extends ToolbarActivity {
    ListView listview;
    RequestQueue mQueue;
    SharedPreferences pref;
    String token;

    JSONArray CustomWishListJSON;
    LinearLayout CustomWishList; //모든 상품 항목
    ArrayList<LinearLayout> CustomWishItem; //개별 상품 항목

    int delete_index=-1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_wish_list);
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


        CustomWishList=findViewById(R.id.CustomWishList);
        CustomWishItem=new ArrayList<LinearLayout>();

        loadWishList();
    }
    // 뒤로가기로 찜목록으로 왔을 시 새로고침
    @Override
    public void onRestart() {
        super.onRestart();
        //When BACK BUTTON is pressed, the activity on the stack is restarted
        //Do what you want on the refresh procedure here
        loadWishList();
    }
    void LayoutInit(){
        CustomWishList.removeAllViewsInLayout();
        CustomWishItem.clear();
    }
    void loadWishList(){
        String url="http://54.180.90.90:3000/api/wishlist/history";
        final JsonArrayRequest request=new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        CustomWishListJSON=response;
                        //Toast.makeText(getApplicationContext(),response.toString(),Toast.LENGTH_SHORT).show();
                        //레이아웃 초기화
                        if(delete_index==-1){
                            //처음 카트 로드할 때: 레이아웃 초기화 및 세팅
                            LayoutInit();
                            setWishListLayout();
                        }
                        else{
                            //삭제 후 카트 로드할 때: 삭제 변수 초기화 및 레이아웃 숨김 해제
                            CustomWishList.setVisibility(View.VISIBLE);
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
    void setWishListLayout(){
        try {
            //장바구니 항목 개수에 따라 추가
            for(int i=0;i<CustomWishListJSON.length();i++){
                //수평 레이아웃 생성
                final LinearLayout l = new LinearLayout(getApplicationContext());
                l.setOrientation(LinearLayout.HORIZONTAL);
                //json 오브젝트 받아오기
                JSONObject result=CustomWishListJSON.getJSONObject(i);
                String iname=result.getString("name"); //아이템 이름
                int price=result.getInt("sale_price"); //아이템 가격
                int Amount=result.getInt("item_count"); //아이템 수량
                String start_time=result.getString("start_time"); //판매 시작 시간
                if(!start_time.equals("null")){
                    start_time=start_time.substring(0,start_time.lastIndexOf("T"))+"\n"+start_time.substring(start_time.lastIndexOf("T")+1,start_time.lastIndexOf("."));
                }
                String end_time=result.getString("end_time"); //판매 종료 시간
                if(!end_time.equals("null")){
                    end_time=end_time.substring(0,end_time.lastIndexOf("T"))+"\n"+end_time.substring(end_time.lastIndexOf("T")+1,end_time.lastIndexOf("."));
                }
                final Long iid=result.getLong("item_id");//아이템 아이디

                //width 세팅
                LinearLayout.LayoutParams nameWidth = new LinearLayout.LayoutParams(findViewById(R.id.WishListItemName_t_wrapper).getWidth(),
                        LinearLayout.LayoutParams.MATCH_PARENT);
                LinearLayout.LayoutParams costWidth = new LinearLayout.LayoutParams(findViewById(R.id.WishListCost_t_wrapper).getWidth(),
                        LinearLayout.LayoutParams.MATCH_PARENT);
                LinearLayout.LayoutParams amountWidth = new LinearLayout.LayoutParams(findViewById(R.id.WishListAmount_t_wrapper).getWidth(),
                        LinearLayout.LayoutParams.MATCH_PARENT);
                LinearLayout.LayoutParams start_timeWidth = new LinearLayout.LayoutParams(findViewById(R.id.WishListStartTime_t_wrapper).getWidth(),
                        LinearLayout.LayoutParams.MATCH_PARENT);
                LinearLayout.LayoutParams end_timeWidth = new LinearLayout.LayoutParams(findViewById(R.id.WishListEndTime_t_wrapper).getWidth(),
                        LinearLayout.LayoutParams.MATCH_PARENT);
                LinearLayout.LayoutParams deleteWidth = new LinearLayout.LayoutParams(findViewById(R.id.WishListDelete_t_wrapper).getWidth(),
                        LinearLayout.LayoutParams.MATCH_PARENT);

                //TextView 생성
                //  상품 이름
                TextView ItemName_t=new TextView(getApplicationContext());
                LinearLayout ItemNameWrapper=new LinearLayout(getApplicationContext());
                ItemNameWrapper.setLayoutParams(nameWidth);
                ItemNameWrapper.setGravity(Gravity.CENTER);
                ItemName_t.setText(iname);
                ItemNameWrapper.addView(ItemName_t);

                //  상품 가격
                TextView ItemCost_t=new TextView(getApplicationContext());
                LinearLayout ItemCostWrapper=new LinearLayout(getApplicationContext());
                ItemCostWrapper.setLayoutParams(costWidth);
                ItemCostWrapper.setGravity(Gravity.CENTER);
                ItemCost_t.setText(String.valueOf(price));
                ItemCostWrapper.addView(ItemCost_t);

                //  상품 수량
                LinearLayout ItemAmountWrapper=new LinearLayout(getApplicationContext());
                ItemAmountWrapper.setLayoutParams(amountWidth);
                ItemAmountWrapper.setGravity(Gravity.CENTER);
                TextView Amount_t=new TextView(getApplicationContext());
                Amount_t.setText(String.valueOf(Amount));
                ItemAmountWrapper.addView(Amount_t);

                // 시작 시간
                LinearLayout StartTimeWrapper=new LinearLayout(getApplicationContext());
                StartTimeWrapper.setLayoutParams(start_timeWidth);
                StartTimeWrapper.setGravity(Gravity.CENTER);
                TextView StartTime_t=new TextView(getApplicationContext());
                StartTime_t.setText(start_time);
                StartTimeWrapper.addView(StartTime_t);

                // 종료 시간
                LinearLayout EndTimeWrapper=new LinearLayout(getApplicationContext());
                EndTimeWrapper.setLayoutParams(end_timeWidth);
                EndTimeWrapper.setGravity(Gravity.CENTER);
                TextView EndTime_t=new TextView(getApplicationContext());
                EndTime_t.setText(end_time);
                EndTimeWrapper.addView(EndTime_t);

                //삭제 텍스트 생성
                TextView deleteItem=new TextView(getApplicationContext());
                LinearLayout deleteItemWrapper=new LinearLayout(getApplicationContext());
                deleteItemWrapper.setLayoutParams(deleteWidth);
                deleteItemWrapper.setGravity(Gravity.CENTER);
                deleteItem.setText(" X ");
                deleteItemWrapper.addView(deleteItem);

                //수평 레이아웃에 추가
                l.addView(ItemNameWrapper);
                l.addView(ItemCostWrapper);
                l.addView(ItemAmountWrapper);
                l.addView(StartTimeWrapper);
                l.addView(EndTimeWrapper);
                l.addView(deleteItemWrapper);
                l.setBackgroundResource(R.drawable.underline);
                //장바구니 아이템 ArrayList에 추가
                CustomWishItem.add(l);
                //아이템 상세로 이동
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


                //장바구니 내 아이템 삭제 기능
                deleteItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int index=CustomWishItem.indexOf(l);
                        deleteItem(index,iid);
                        //삭제되는 동안 레이아웃을 잠시 숨김
                        CustomWishList.setVisibility(View.INVISIBLE);
                    }
                });
            }
            //장바구니 내의 Item들 모두 추가
            for(int i=0;i<CustomWishItem.size();i++) {
                CustomWishList.addView(CustomWishItem.get(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    void deleteRefreshWishList(){
        CustomWishList.removeAllViewsInLayout();
        for(int i=0;i<CustomWishItem.size();i++) {
            if(i==delete_index){
                continue;
            }
            CustomWishList.addView(CustomWishItem.get(i));
        }
    }
    void deleteItem(int index, final Long iid){
        delete_index=index;
        String url="http://54.180.90.90:3000/api/wishlist/delete";
        StringRequest request=new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONObject msg=null;
                        try {
                            msg=new JSONObject(response);
                            if(msg.getString("success").equals("true")){
                                deleteRefreshWishList(); //카트 새로고침
                                //레이아웃 삭제
                                CustomWishItem.remove(delete_index);
                                loadWishList(); //JSON 다시 받아오기
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
                params.put("iid", iid.toString());
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
