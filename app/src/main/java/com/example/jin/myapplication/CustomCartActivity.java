package com.example.jin.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
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
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CustomCartActivity extends ToolbarActivity{
    ListView listview;
    LinearLayout CustomCartList; //모든 상품 항목
    ArrayList<LinearLayout> CustomCartItem; //개별 상품 항목
    ArrayList<CheckBox> CheckboxList; //체크박스들 리스트
    JSONArray CustomCartJSON;
    int totalPrice; //총금액
    int totalCount; //총수량
    final int CHECK_SIGNAL=1;
    final int DELETE_SIGNAL=2;
    TextView cartSelectText;
    Button cartOrderButton;
    RequestQueue mQueue;
    SharedPreferences pref;
    String token;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_cart);
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
        cartSelectText=findViewById(R.id.cartSelect_t);
        CustomCartList=findViewById(R.id.CustomCartList);

        CustomCartItem=new ArrayList<LinearLayout>();
        CheckboxList=new ArrayList<CheckBox>();
        totalPrice=0;
        totalCount=0;

        loadCart();

        cartOrderButton=findViewById(R.id.cartOrder_btn);
        //주문 버튼 리스너 설정
        cartOrderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkCartList();
            }
        });
    }
    // 뒤로가기로 장바구니로 왔을 시 새로고침
    @Override
    public void onRestart() {
        super.onRestart();
        //When BACK BUTTON is pressed, the activity on the stack is restarted
        //Do what you want on the refresh procedure here
        loadCart();
    }
    void LayoutInit(){
        CustomCartList.removeAllViewsInLayout();
        CustomCartItem.clear();
        CheckboxList.clear();
        totalPrice=0;
        totalCount=0;
    }
    void loadCart(){
        String url="http://54.180.90.90:3000/api/shopping_cart_history";
        final JsonArrayRequest request=new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        //Toast.makeText(getApplicationContext(),response.toString(),Toast.LENGTH_SHORT).show();
                        //레이아웃 초기화
                        CustomCartJSON=response;

                        if(delete_index==-1){
                            //처음 카트 로드할 때: 레이아웃 초기화 및 세팅
                            LayoutInit();
                            setCartLayout();
                        }
                        else{
                            //삭제 후 카트 로드할 때: 삭제 변수 초기화 및 레이아웃 숨김 해제
                            CustomCartList.setVisibility(View.VISIBLE);
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
    void setCartLayout(){
        try {
            //장바구니 항목 개수에 따라 추가
            for(int i=0;i<CustomCartJSON.length();i++){
                //수평 레이아웃 생성
                final LinearLayout l = new LinearLayout(getApplicationContext());
                l.setOrientation(LinearLayout.HORIZONTAL);
                //json 오브젝트 받아오기
                JSONObject result=CustomCartJSON.getJSONObject(i);
                String iname=result.getString("name"); //아이템 이름
                int Amount=result.getInt("Amount"); //아이템 수량
                int price=result.getInt("sale_price"); //아이템 가격
                final Long iid=result.getLong("ItemID");//아이템 아이디
                totalPrice+=price*Amount; //총 금액에 더함
                totalCount+=Amount; //총 수량에 더함

                //width 세팅
                LinearLayout.LayoutParams selectWidth = new LinearLayout.LayoutParams(findViewById(R.id.cartSelect_t_wrapper).getWidth(),
                        LinearLayout.LayoutParams.MATCH_PARENT);
                LinearLayout.LayoutParams nameWidth = new LinearLayout.LayoutParams(findViewById(R.id.cartItemName_t_wrapper).getWidth(),
                        LinearLayout.LayoutParams.MATCH_PARENT);
                LinearLayout.LayoutParams costWidth = new LinearLayout.LayoutParams(findViewById(R.id.cartCost_t_wrapper).getWidth(),
                        LinearLayout.LayoutParams.MATCH_PARENT);
                LinearLayout.LayoutParams amountWidth = new LinearLayout.LayoutParams(findViewById(R.id.cartAmount_t_wrapper).getWidth(),
                        LinearLayout.LayoutParams.MATCH_PARENT);
                LinearLayout.LayoutParams deleteWidth = new LinearLayout.LayoutParams(findViewById(R.id.cartDelete_t_wrapper).getWidth(),
                        LinearLayout.LayoutParams.MATCH_PARENT);

                //TextView 생성
                //  상품 이름
                TextView ItemName_t=new TextView(getApplicationContext());
                ItemName_t.setTextSize(20);
                LinearLayout ItemNameWrapper=new LinearLayout(getApplicationContext());
                ItemNameWrapper.setLayoutParams(nameWidth);
                ItemNameWrapper.setGravity(Gravity.CENTER);
                ItemName_t.setText(iname);
                ItemNameWrapper.addView(ItemName_t);

                //  상품 가격
                TextView ItemCost_t=new TextView(getApplicationContext());
                ItemCost_t.setTextSize(20);
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
                Amount_t.setTextSize(20);
                Amount_t.setText(String.valueOf(Amount));
                ItemAmountWrapper.addView(Amount_t);

                //선택 체크 박스 생성
                final CheckBox selectItem=new CheckBox(getApplicationContext());
                CheckboxList.add(selectItem);
                selectItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //체크박스가 체크 또는 체크해제 되었을 때 총 금액과 총 수량 변동
                        int index=CheckboxList.indexOf(selectItem);
                        changeTotalPrice(index,CHECK_SIGNAL);
                    }
                });
                LinearLayout checkboxWrapper=new LinearLayout(getApplicationContext());
                checkboxWrapper.setLayoutParams(selectWidth);
                checkboxWrapper.setGravity(Gravity.CENTER);
                selectItem.setChecked(true);
                checkboxWrapper.addView(selectItem);

                //삭제 텍스트 생성
                TextView deleteItem=new TextView(getApplicationContext());
                LinearLayout deleteItemWrapper=new LinearLayout(getApplicationContext());
                deleteItemWrapper.setLayoutParams(deleteWidth);
                deleteItemWrapper.setGravity(Gravity.CENTER);
                deleteItem.setText(" X ");
                deleteItem.setTextSize(20);
                deleteItemWrapper.addView(deleteItem);

                //수평 레이아웃에 추가
                l.addView(checkboxWrapper);
                l.addView(ItemNameWrapper);
                l.addView(ItemCostWrapper);
                l.addView(ItemAmountWrapper);
                l.addView(deleteItemWrapper);
                l.setBackgroundResource(R.drawable.underline);
                //장바구니 아이템 ArrayList에 추가
                CustomCartItem.add(l);


                //장바구니 내 아이템 삭제 기능
                deleteItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int index=CustomCartItem.indexOf(l);
                        deleteItem(index,iid);
                        //삭제되는 동안 레이아웃을 잠시 숨김
                        CustomCartList.setVisibility(View.INVISIBLE);
                    }
                });
            }
            //장바구니 내의 Item들 모두 추가
            for(int i=0;i<CustomCartItem.size();i++) {
                CustomCartList.addView(CustomCartItem.get(i));
            }
            setTotalPrice();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    void cartOrder(final String iid_str,final String amount_str,final String time_str,final int length){
        String url="http://54.180.90.90:3000/api/order";
        StringRequest request=new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if(totalCount>0) {
                            Toast.makeText(getApplicationContext(), "주문이 완료되었습니다.", Toast.LENGTH_SHORT).show();
                            Intent intent;
                            intent = new Intent(
                                    getApplicationContext(),
                                    OrderListActivity.class);
                            startActivity(intent);
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
                params.put("payment","cash");
                params.put("iid",iid_str);
                params.put("amount",amount_str);
                params.put("time",time_str);
                params.put("length",String.valueOf(length));
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
    void changeTotalPrice(int index,int signal){
        //CheckBox의 Onclick리스너 함수
        //signal 1: 체크에 따른 총 금액 및 수량 변화
        //signal 2: 삭제에 의한 총 금액 및 수량 변화
        int indexPrice;
        try {
            indexPrice=CustomCartJSON.getJSONObject(index).getInt("sale_price");
        } catch (JSONException e) {
            e.printStackTrace();
            indexPrice=0;
        }
        int indexAmount;
        try {
            indexAmount=CustomCartJSON.getJSONObject(index).getInt("Amount");
        } catch (JSONException e) {
            e.printStackTrace();
            indexAmount=0;
        }
        if(signal==DELETE_SIGNAL){
            if(CheckboxList.get(index).isChecked()){
                totalPrice-=indexPrice*indexAmount;
                totalCount-=indexAmount;
            }
        }
        else {
            //합계 수량과 금액을 변경
            if (CheckboxList.get(index).isChecked()) {
                totalPrice += indexPrice * indexAmount;
                totalCount += indexAmount;
            } else {
                totalPrice -= indexPrice * indexAmount;
                totalCount -= indexAmount;
            }
        }
        setTotalPrice();
    }
    void setTotalPrice(){
        //인자의 값을 합계TextView의 값에 세팅
        TextView TotalPrice=findViewById(R.id.totalCost_t);
        TextView TotalCount=findViewById(R.id.totalAmount_t);
        TotalPrice.setText("합계 금액: "+String.valueOf(totalPrice));
        TotalCount.setText("합계 수량: "+String.valueOf(totalCount));

    }
    Long delete_id;
    int delete_index=-1;
    void deleteRefreshCart(){
        CustomCartList.removeAllViewsInLayout();
        for(int i=0;i<CustomCartItem.size();i++) {
            if(i==delete_index){
                continue;
            }
            CustomCartList.addView(CustomCartItem.get(i));
        }
        changeTotalPrice(delete_index,DELETE_SIGNAL);
    }
    void deleteItem(int index, Long iid){
        delete_id=iid;
        delete_index=index;
        String url="http://54.180.90.90:3000/api/shopping_cart/delete";
        StringRequest request=new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //Toast.makeText(getApplicationContext(), response, Toast.LENGTH_SHORT).show();
                        JSONObject msg=null;
                        try {
                            msg=new JSONObject(response);
                            if(msg.getString("success").equals("true")){
                                Toast.makeText(getApplicationContext(), "삭제 완료", Toast.LENGTH_SHORT).show();
                                deleteRefreshCart(); //카트 새로고침
                                //레이아웃 삭제
                                CustomCartItem.remove(delete_index);
                                CheckboxList.remove(delete_index);
                                loadCart(); //JSON 다시 받아오기
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
                params.put("iid", delete_id.toString());
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
    void checkCartList(){
        String iid_str="";
        String amount_str="";
        String time_str="";
        int length=0; //상품 개수
        for(int i=0;i<CustomCartItem.size();i++){
            if(CheckboxList.get(i).isChecked()){
                try {
                    length++;
                    String ordered_iid=String.valueOf(CustomCartJSON.getJSONObject(i).getLong("ItemID"));
                    deleteOrderedItem(ordered_iid);
                    iid_str+=ordered_iid;
                    iid_str+=";";
                    amount_str+=String.valueOf(CustomCartJSON.getJSONObject(i).getInt("Amount"));
                    amount_str+=";";
                    time_str+="10";
                    time_str+=";";
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        cartOrder(iid_str,amount_str,time_str,length);
    }
    //주문한 상품 목록 장바구니 내에서 제거
    void deleteOrderedItem(final String ordered_iid){
        String url="http://54.180.90.90:3000/api/shopping_cart/delete";
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
                params.put("iid", ordered_iid);
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
