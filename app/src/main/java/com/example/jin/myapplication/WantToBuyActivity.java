package com.example.jin.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
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

public class WantToBuyActivity extends ToolbarActivity {
    ListView listview;
    RequestQueue mQueue;
    SharedPreferences pref;
    String token;

    Spinner categorySpinner;
    ArrayList<String> categoryIDList;
    ArrayList<String> categoryList;
    ArrayAdapter<String> categoryAdapt;
    Button WantToBuyEnrollButton;
    EditText min;
    EditText max;

    JSONArray WTBListJSON;
    LinearLayout WTBLists; //모든 삽니다 항목
    ArrayList<LinearLayout> WTBList; //개별 삽니다 항목
    ArrayList<String> cateID; //개별 카테고리 아이디
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_want_to_buy);
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
        //카테고리 로드
        categorySpinner=findViewById(R.id.categorySpinner);
        categoryIDList=new ArrayList<String>();
        categoryList=new ArrayList<String>();
        categoryAdapt=  new ArrayAdapter<String>(getApplicationContext(),  android.R.layout.simple_spinner_dropdown_item, categoryList);
        categoryAdapt.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item);
        loadCategory();

        //EDIT TEXT 뷰 연결
        min=findViewById(R.id.minPrice);
        max=findViewById(R.id.maxPrice);

        WantToBuyEnrollButton=findViewById(R.id.want_to_buy_btn);
        //버튼 리스너
        WantToBuyEnrollButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Integer.parseInt(min.getText().toString())>Integer.parseInt(max.getText().toString())){
                    Toast.makeText(getApplicationContext(),"최소 가격의 값보다 최대 가격을 더 크게 입력하세요.",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(min.getText().toString().equals("") || max.getText().toString().equals("")){
                    Toast.makeText(getApplicationContext(),"최소 가격과 최대 가격을 반드시 입력하세요.",Toast.LENGTH_SHORT).show();
                    return;
                }
                enrollWantToBuy();
            }
        });

        //삽니다 관리 로드
        WTBLists=findViewById(R.id.WantToBuyManage);
        WTBList=new ArrayList<LinearLayout>();
        loadWantToBuy();
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
                            categorySpinner.setAdapter(categoryAdapt);
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
    String CategoryID;
    void enrollWantToBuy(){
        //선택된 카테고리 ID 설정
        for(int i=0;i<categoryList.size();i++){
            if(categorySpinner.getSelectedItem().equals(categoryAdapt.getItem(i))){
                CategoryID=categoryIDList.get(i);
            }
        }
        String url="http://54.180.90.90:3000/api/wtb";
        StringRequest request=new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONObject obj=null;
                        String str="";
                        try{
                            obj=new JSONObject(response);
                            str=obj.getString("success");
                        }catch(JSONException e){
                            e.printStackTrace();
                        }
                        if(str.equals("true")){
                            Toast.makeText(getApplicationContext(), "삽니다 등록 완료", Toast.LENGTH_SHORT).show();
                            loadWantToBuy();

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
                params.put("cateid",CategoryID);
                params.put("min_price",min.getText().toString());
                params.put("max_price",max.getText().toString());
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
    void WTBListInit(){
        WTBList.clear();
        WTBLists.removeAllViewsInLayout();
    }
    void loadWantToBuy(){
        String url="http://54.180.90.90:3000/api/wtb";
        final JsonArrayRequest request=new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        WTBListJSON=response;

                        if(delete_index==-1){
                            //레이아웃 초기화 및 세팅
                            WTBListInit();
                            setWTBLayout();
                        }
                        else{
                            //삭제 후 로드할 때: 삭제 변수 초기화 및 레이아웃 숨김 해제
                            WTBLists.setVisibility(View.VISIBLE);
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
    void setWTBLayout(){
        try {
            for(int i=0;i<WTBListJSON.length();i++){
                //수평 레이아웃 생성
                final LinearLayout l = new LinearLayout(getApplicationContext());
                l.setOrientation(LinearLayout.HORIZONTAL);
                //json 오브젝트 받아오기
                JSONObject result=WTBListJSON.getJSONObject(i);
                String cateName=result.getString("cateName"); // 카테고리 이름
                int minPrice=result.getInt("minPrice"); // 최소 가격
                int maxPrice=result.getInt("maxPrice"); // 최대 가격
                final int cateid=result.getInt("cateID"); //카테고리 아이디

                //width 세팅
                LinearLayout.LayoutParams nameWidth = new LinearLayout.LayoutParams(findViewById(R.id.WTBcategory_t_wrapper).getWidth(),
                        LinearLayout.LayoutParams.MATCH_PARENT);
                LinearLayout.LayoutParams minWidth = new LinearLayout.LayoutParams(findViewById(R.id.WTBminPrice_t_wrapper).getWidth(),
                        LinearLayout.LayoutParams.MATCH_PARENT);
                LinearLayout.LayoutParams maxWidth = new LinearLayout.LayoutParams(findViewById(R.id.WTBmaxPrice_t_wrapper).getWidth(),
                        LinearLayout.LayoutParams.MATCH_PARENT);
                LinearLayout.LayoutParams deleteWidth = new LinearLayout.LayoutParams(findViewById(R.id.WTBdelete_t_wrapper).getWidth(),
                        LinearLayout.LayoutParams.MATCH_PARENT);


                //TextView 생성
                //  카테고리 이름
                TextView cateName_t=new TextView(getApplicationContext());
                LinearLayout cateNameWrapper=new LinearLayout(getApplicationContext());
                cateNameWrapper.setLayoutParams(nameWidth);
                cateNameWrapper.setGravity(Gravity.CENTER);
                cateName_t.setText(cateName);
                cateName_t.setTextSize(20);
                cateNameWrapper.addView(cateName_t);

                // 최저
                TextView minPrice_t=new TextView(getApplicationContext());
                LinearLayout minPriceWrapper=new LinearLayout(getApplicationContext());
                minPriceWrapper.setLayoutParams(minWidth);
                minPriceWrapper.setGravity(Gravity.CENTER);
                minPrice_t.setText(String.valueOf(minPrice));
                minPrice_t.setTextSize(20);
                minPriceWrapper.addView(minPrice_t);

                //  최대
                TextView maxPrice_t=new TextView(getApplicationContext());
                LinearLayout maxPriceWrapper=new LinearLayout(getApplicationContext());
                maxPriceWrapper.setLayoutParams(maxWidth);
                maxPriceWrapper.setGravity(Gravity.CENTER);
                maxPrice_t.setText(String.valueOf(maxPrice));
                maxPrice_t.setTextSize(20);
                maxPriceWrapper.addView(maxPrice_t);

                //삭제
                TextView delete_t=new TextView(getApplicationContext());
                LinearLayout deleteWrapper=new LinearLayout(getApplicationContext());
                deleteWrapper.setLayoutParams(deleteWidth);
                deleteWrapper.setGravity(Gravity.CENTER);
                delete_t.setText(" X ");
                delete_t.setTextSize(20);
                deleteWrapper.addView(delete_t);

                //삭제 리스너
                deleteWrapper.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int index=WTBList.indexOf(l);
                        deleteWantToBuy(index,cateid);
                        //삭제되는 동안 레이아웃을 잠시 숨김
                        WTBLists.setVisibility(View.INVISIBLE);
                    }
                });

                //수평 레이아웃에 추가
                l.addView(cateNameWrapper);
                l.addView(minPriceWrapper);
                l.addView(maxPriceWrapper);
                l.addView(deleteWrapper);

                l.setBackgroundResource(R.drawable.underline);
                //각각 삽니다 ArrayList에 추가
                WTBList.add(l);

            }
            //삽니다 레이아웃에 추가
            for(int i=0;i<WTBList.size();i++) {
                WTBLists.addView(WTBList.get(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    int delete_cate_id;
    int delete_index=-1;
    void deleteRefreshWTB(){
        WTBLists.removeAllViewsInLayout();
        for(int i=0;i<WTBList.size();i++) {
            if(i==delete_index){
                continue;
            }
            WTBLists.addView(WTBList.get(i));
        }
    }
    void deleteWantToBuy(int index, int cateid){
        delete_cate_id=cateid;
        delete_index=index;
        String url="http://54.180.90.90:3000/api/wtb/delete";
        StringRequest request=new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(getApplicationContext(), response, Toast.LENGTH_SHORT).show();
                        deleteRefreshWTB(); //삽니다 새로고침
                        //레이아웃 삭제
                        WTBList.remove(delete_index);
                        loadWantToBuy(); //JSON 다시 받아오기
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
                params.put("cateid", String.valueOf(delete_cate_id));
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
