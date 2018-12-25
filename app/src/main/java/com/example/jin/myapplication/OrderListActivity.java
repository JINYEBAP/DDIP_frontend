package com.example.jin.myapplication;

import android.content.SharedPreferences;
import android.graphics.Color;
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
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class OrderListActivity extends ToolbarActivity {
    LinearLayout OrderList;
    ListView listview = null ;
    LinearLayout CustomOrderList; //모든 상품 항목
    ArrayList<LinearLayout> CustomOrderItem; //개별 상품 항목
    JSONArray CustomOrderJSON;
    RequestQueue mQueue;
    SharedPreferences pref;
    String token;
    String name;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_list);
        OrderList=findViewById(R.id.OrderList);
        mQueue=Volley.newRequestQueue(this);
        pref=getSharedPreferences("pref", MODE_PRIVATE);
        token=pref.getString("token", null);
        listview = (ListView) findViewById(R.id.drawer_menulist) ;
        listview.setAdapter(super.MENU) ;
        listview.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View v, int position, long id) {
                selectDrawable((String)adapterView.getItemAtPosition(position));
            }
        });
        CustomOrderList=findViewById(R.id.OrderList);
        CustomOrderItem=new ArrayList<LinearLayout>();
        loadOrderList();
    }

    void loadOrderList(){
        String url="http://54.180.90.90:3000/api/order_history/customer";
        final JsonArrayRequest request=new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        CustomOrderJSON=response;
                        try {
                            //주문내역 항목 개수에 따라 추가
                            for(int i=0;i<response.length();i++){
                                //수평 레이아웃 생성
                                LinearLayout l = new LinearLayout(getApplicationContext());
                                l.setOrientation(LinearLayout.HORIZONTAL);
                                //json 오브젝트 받아오기
                                JSONObject result=response.getJSONObject(i);
                                Long group=result.getLong("gid"); //그룹 넘버
                                String order_date_total=result.getString("order_date"); //주문 날짜
                                String order_date=order_date_total.substring(0,order_date_total.lastIndexOf("T"));
                                String iname=result.getString("name"); //아이템 이름
                                int count=result.getInt("amount"); //아이템 수량
                                String order_state=result.getString("order_state"); //주문 상태

                                //width 세팅
                                LinearLayout.LayoutParams groupnumWidth = new LinearLayout.LayoutParams(findViewById(R.id.orderNum_t_wrapper).getWidth(),
                                        LinearLayout.LayoutParams.MATCH_PARENT);
                                LinearLayout.LayoutParams orderdateWidth = new LinearLayout.LayoutParams(findViewById(R.id.OrderDate_t_wrapper).getWidth(),
                                        LinearLayout.LayoutParams.MATCH_PARENT);
                                LinearLayout.LayoutParams inameWidth = new LinearLayout.LayoutParams(findViewById(R.id.orderItemName_t_wrapper).getWidth(),
                                        LinearLayout.LayoutParams.MATCH_PARENT);
                                LinearLayout.LayoutParams countWidth = new LinearLayout.LayoutParams(findViewById(R.id.orderItemCount_t_wrapper).getWidth(),
                                        LinearLayout.LayoutParams.MATCH_PARENT);
                                LinearLayout.LayoutParams orderstateWidth = new LinearLayout.LayoutParams(findViewById(R.id.orderState_t_wrapper).getWidth(),
                                        LinearLayout.LayoutParams.MATCH_PARENT);


                                //TextView 생성
                                //  그룹 넘버
                                TextView GroupNum_t=new TextView(getApplicationContext());
                                LinearLayout GroupNumWrapper=new LinearLayout(getApplicationContext());
                                GroupNumWrapper.setLayoutParams(groupnumWidth);
                                GroupNumWrapper.setGravity(Gravity.CENTER);
                                GroupNum_t.setText(String.valueOf(group));
                                GroupNum_t.setTextSize(15);
                                GroupNumWrapper.addView(GroupNum_t);

                                //  주문 날짜
                                TextView OrderDate_t=new TextView(getApplicationContext());
                                LinearLayout OrderDateWrapper=new LinearLayout(getApplicationContext());
                                OrderDateWrapper.setLayoutParams(orderdateWidth);
                                OrderDateWrapper.setGravity(Gravity.CENTER);
                                OrderDate_t.setTextSize(20);
                                OrderDate_t.setText(order_date);
                                OrderDateWrapper.addView(OrderDate_t);

                                //  상품 이름
                                LinearLayout INameWrapper=new LinearLayout(getApplicationContext());
                                INameWrapper.setLayoutParams(inameWidth);
                                INameWrapper.setGravity(Gravity.CENTER);
                                TextView IName_t=new TextView(getApplicationContext());
                                IName_t.setText(iname);
                                IName_t.setTextSize(20);
                                INameWrapper.addView(IName_t);

                                //수량 텍스트 생성
                                TextView countItem=new TextView(getApplicationContext());
                                LinearLayout countItemWrapper=new LinearLayout(getApplicationContext());
                                countItemWrapper.setLayoutParams(countWidth);
                                countItemWrapper.setGravity(Gravity.CENTER);
                                countItem.setTextSize(20);
                                countItem.setText(String.valueOf(count));
                                countItemWrapper.addView(countItem);

                                // 주문 상태
                                LinearLayout OrderStateWrapper=new LinearLayout(getApplicationContext());
                                OrderStateWrapper.setLayoutParams(orderstateWidth);
                                OrderStateWrapper.setGravity(Gravity.CENTER);
                                TextView OrderState_t=new TextView(getApplicationContext());
                                OrderState_t.setTextSize(20);
                                OrderState_t.setText(order_state);
                                OrderStateWrapper.addView(OrderState_t);



                                //수평 레이아웃에 추가
                                l.addView(GroupNumWrapper);
                                l.addView(OrderDateWrapper);
                                l.addView(INameWrapper);
                                l.addView(countItemWrapper);
                                l.addView(OrderStateWrapper);
                                l.setBackgroundResource(R.drawable.underline);

                                //주문 아이템 ArrayList에 추가
                                CustomOrderItem.add(l);
                            }
                            //장바구니 내의 Item들 모두 추가
                            for(int i=0;i<CustomOrderItem.size();i++) {
                                CustomOrderList.addView(CustomOrderItem.get(i));
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
