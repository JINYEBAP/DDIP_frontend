package com.example.jin.myapplication;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class GeoLocation {

    double latitude;
    double longitude;

    public GeoLocation(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
public class SupplierSignUpActivity extends ToolbarActivity {
    ListView listview;
    EditText ID;
    EditText PW;
    EditText NAME;
    EditText AD;
    EditText DL;
    double longitude;
    double latitude;
    Button SPSignUpButton;
    RequestQueue mQueue;
    String str; //회원가입 성공 여부를 위한 스트링
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_supplier_sign_up);
        listview = (ListView) findViewById(R.id.drawer_menulist) ;
        listview.setAdapter(super.MENU) ;
        listview.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View v, int position, long id) {
                selectDrawable((String)adapterView.getItemAtPosition(position));
            }
        });
        mQueue=Volley.newRequestQueue(this);
        ID=findViewById(R.id.SPsignup_ID);
        PW=findViewById(R.id.SPPASSWORD);
        NAME=findViewById(R.id.SPsignup_NAME);
        AD=findViewById(R.id.SPsignup_AR);
        DL=findViewById(R.id.SPsignup_DL);
        SPSignUpButton=findViewById(R.id.SPsignup_btn);
        SPSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //입력한 주소를 위도 경도로 볂환
                GeoLocation result=getGeoLocationListUsingAddress(AD.getText().toString()).get(0);
                latitude=result.latitude;
                longitude=result.longitude;
                SignUp();
            }
        });
    }
    private Geocoder geocoder = new Geocoder(SupplierSignUpActivity.this);
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
    public ArrayList<String> getAddressListUsingGeolocation(GeoLocation location) {
        ArrayList<String> resultList = new ArrayList<>();
        try {
            List<Address> list = geocoder.getFromLocation(location.latitude, location.longitude, 10);
            for (Address addr : list) {
                resultList.add(addr.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return resultList;
    }
    void SignUp(){
        String url="http://54.180.90.90:3000/api/sign_up/supplier";
        StringRequest request=new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //Toast.makeText(getApplicationContext(), response, Toast.LENGTH_SHORT).show();
                        //Toast.makeText(getApplicationContext(), String.valueOf(latitude)+" "+String.valueOf(longitude), Toast.LENGTH_SHORT).show();
                        JSONObject obj=null;

                        try{
                            obj=new JSONObject(response);
                            str=obj.getString("success");
                        }catch(JSONException e){
                            e.printStackTrace();
                        }
                        if(str.equals("true")){
                            //회원가입 성공 시 홈 페이지로 이동
                            Intent intent;
                            intent= new Intent(
                                    getApplicationContext(),
                                    MainActivity.class);
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
            protected Map<String,String> getParams(){
                Map<String, String> params = new HashMap<String, String>();
                String sid=ID.getText().toString();
                if(sid.charAt(3)!='-'){
                    sid=sid.substring(0,3)+'-'+sid.substring(3,sid.length());
                }
                if(sid.charAt(sid.length()-5)!='-'){
                    sid=sid.substring(0,sid.length()-4)+'-'+sid.substring(sid.length()-4,sid.length());
                }
                params.put("sid",sid);
                params.put("passwd",PW.getText().toString());
                params.put("rname",NAME.getText().toString());
                params.put("address",AD.getText().toString());
                params.put("dlprice",DL.getText().toString());
                params.put("latitude",String.valueOf(latitude));
                params.put("longitude",String.valueOf(longitude));
                return params;
            }
        };
        mQueue.add(request);
    }
}
