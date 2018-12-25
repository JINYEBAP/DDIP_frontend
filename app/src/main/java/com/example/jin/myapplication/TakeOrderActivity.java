package com.example.jin.myapplication;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
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

public class TakeOrderActivity extends ToolbarActivity {
    ListView listview = null ;
    RequestQueue mQueue;
    SharedPreferences pref;
    String token;

    JSONArray TakeOrderJSON;
    LinearLayout TakeOrder;
    ArrayList<LinearLayout> CustomOrder;
    TextView rName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_order);
        mQueue=Volley.newRequestQueue(this);
        pref=getSharedPreferences("pref", MODE_PRIVATE);
        token=pref.getString("token", null);
        String rname=pref.getString("cname",null);
        listview = (ListView) findViewById(R.id.drawer_menulist) ;
        listview.setAdapter(super.MENU) ;
        listview.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View v, int position, long id) {
                selectDrawable((String)adapterView.getItemAtPosition(position));
            }
        });
        //레이아웃 연결
        TakeOrder=findViewById(R.id.TakeOrder);
        CustomOrder=new ArrayList<LinearLayout>();
        rName=findViewById(R.id.rName);
        rName.setText(rname);
        loadTakeOrder();
    }
    void loadTakeOrder(){
        String url="http://54.180.90.90:3000/api/order_history/supplier";
        final JsonArrayRequest request=new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        //Toast.makeText(getApplicationContext(),response.toString(),Toast.LENGTH_SHORT).show();
                        TakeOrder.removeAllViewsInLayout();
                        CustomOrder.clear();
                        TakeOrderJSON=response;
                        try {
                            //주문내역 항목 개수에 따라 추가
                            for(int i=0;i<response.length();i++){
                                //수평 레이아웃 생성
                                LinearLayout l = new LinearLayout(getApplicationContext());
                                l.setOrientation(LinearLayout.VERTICAL);

                                LinearLayout ll1 = new LinearLayout(getApplicationContext());
                                ll1.setOrientation(LinearLayout.HORIZONTAL);
                                LinearLayout ll2 = new LinearLayout(getApplicationContext());
                                ll2.setOrientation(LinearLayout.HORIZONTAL);
                                LinearLayout ll3 = new LinearLayout(getApplicationContext());
                                ll3.setOrientation(LinearLayout.HORIZONTAL);
                                //json 오브젝트 받아오기
                                JSONObject result=response.getJSONObject(i);
                                final Long oid=result.getLong("oid");
                                Long group=result.getLong("gid"); //그룹 넘버
                                String order_date=result.getString("order_date"); //주문 날짜
                                order_date=order_date.substring(0,order_date.lastIndexOf("T"))+" "+order_date.substring(order_date.lastIndexOf("T")+1,order_date.lastIndexOf("."));
                                String cid=result.getString("cid"); //고객 아이디(연락처)
                                String iname=result.getString("name"); //아이템 이름
                                int count=result.getInt("amount"); //아이템 수량
                                String order_state=result.getString("order_state"); //주문 상태

                                //width 세팅
                                LinearLayout.LayoutParams groupnumWidth = new LinearLayout.LayoutParams(TakeOrder.getWidth()/2,
                                        LinearLayout.LayoutParams.MATCH_PARENT);
                                LinearLayout.LayoutParams orderdateWidth = new LinearLayout.LayoutParams(TakeOrder.getWidth()/2,
                                        LinearLayout.LayoutParams.MATCH_PARENT);
                                LinearLayout.LayoutParams cidWidth = new LinearLayout.LayoutParams(TakeOrder.getWidth()/2,
                                        LinearLayout.LayoutParams.MATCH_PARENT);
                                LinearLayout.LayoutParams inameWidth = new LinearLayout.LayoutParams(TakeOrder.getWidth()/2,
                                        LinearLayout.LayoutParams.MATCH_PARENT);
                                LinearLayout.LayoutParams countWidth = new LinearLayout.LayoutParams(TakeOrder.getWidth()/2,
                                        LinearLayout.LayoutParams.MATCH_PARENT);
                                LinearLayout.LayoutParams buttonWidth = new LinearLayout.LayoutParams(TakeOrder.getWidth()/2,
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
                                OrderDate_t.setTextSize(15);
                                OrderDate_t.setTextColor(Color.BLACK);
                                OrderDate_t.setText(order_date);
                                OrderDateWrapper.addView(OrderDate_t);

                                //  고객 아이디
                                TextView Cid_t=new TextView(getApplicationContext());
                                LinearLayout CidWrapper=new LinearLayout(getApplicationContext());
                                CidWrapper.setLayoutParams(cidWidth);
                                CidWrapper.setGravity(Gravity.CENTER);
                                Cid_t.setTextSize(20);
                                Cid_t.setTextColor(Color.BLACK);
                                Cid_t.setText(cid);
                                CidWrapper.addView(Cid_t);

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
                                countItem.setText(String.valueOf(count)+" 개");
                                countItemWrapper.addView(countItem);

                                //버튼 생성
                                Button stateUpdate=new Button(getApplicationContext());
                                LinearLayout stateUpdateWrapper=new LinearLayout(getApplicationContext());
                                stateUpdateWrapper.setLayoutParams(buttonWidth);
                                stateUpdateWrapper.setGravity(Gravity.CENTER);
                                if(order_state.equals("waiting")){
                                    stateUpdate.setText("waiting->cooking");
                                    stateUpdate.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            updateState(oid,"cooking");
                                        }
                                    });
                                }
                                else if(order_state.equals("cooking")){
                                    stateUpdate.setText("cooking->complement");
                                    stateUpdate.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            updateState(oid,"complement");
                                        }
                                    });
                                }
                                else{
                                    stateUpdate.setText("complement");
                                    stateUpdate.setEnabled(false);
                                }
                                stateUpdateWrapper.addView(stateUpdate);

                                //수평 레이아웃에 추가
                                ll1.addView(GroupNumWrapper);
                                ll1.addView(OrderDateWrapper);
                                ll2.addView(INameWrapper);
                                ll2.addView(countItemWrapper);
                                ll3.addView(CidWrapper);
                                ll3.addView(stateUpdateWrapper);
                                l.addView(ll1);
                                l.addView(ll2);
                                l.addView(ll3);
                                l.setBackgroundResource(R.drawable.underline);

                                //주문 아이템 ArrayList에 추가
                                CustomOrder.add(l);
                            }
                            //장바구니 내의 Item들 모두 추가
                            for(int i=0;i<CustomOrder.size();i++) {
                                TakeOrder.addView(CustomOrder.get(i));
                            }
                            TakeOrder.setVisibility(View.VISIBLE);
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
    void updateState(final long oid,final String state){
        String url="http://54.180.90.90:3000/api/order/state/update";
        StringRequest request=new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONObject msg=null;
                        try {
                            msg=new JSONObject(response);
                            if(msg.getString("success").equals("true")){
                                Toast.makeText(getApplicationContext(), "상태 변경 완료", Toast.LENGTH_SHORT).show();
                                TakeOrder.setVisibility(View.INVISIBLE);
                                loadTakeOrder();//새로 고침
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
                params.put("oid",String.valueOf(oid));
                params.put("state",state);
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
