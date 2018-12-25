package com.example.jin.myapplication;
import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequest;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
public class UploadItemActivity extends ToolbarActivity {
    ListView listview;
    RequestQueue mQueue;
    SharedPreferences pref;
    String token;
    String sid;

    Spinner categorySpinner;
    ArrayList<String> categoryIDList;
    ArrayList<String> categoryList;
    ArrayAdapter<String> categoryAdapt;

    EditText itemName;
    EditText itemCount;
    EditText itemRawPrice;
    EditText itemSalePrice;
    CheckBox itemDeliverable;
    EditText startTime;
    EditText endTime;
    Button upload_btn;
    Button uploadImageButton;

    LinearLayout uploadedItemList;
    ArrayList<LinearLayout> uploadedItem;
    JSONArray UploadedItemJSON;

    // 사진 촬영, 저장을 위한 변수
    private String imageFilePath;
    private Uri photoUri;
    Bitmap imageBitmap;
    final static int REQUEST_IMAGE_CAPTURE=100;
    //가격 변동
    CheckBox useUpdetePrice;
    EditText minPrice;

    //시간
    String sTime;
    Spinner HourSpinner;
    ArrayList<String> HourList;
    ArrayAdapter<String> HourAdapt;
    //분
    Spinner MinuteSpinner;
    ArrayList<String> MinuteList;
    ArrayAdapter<String> MinuteAdapt;

    //클라우드 비전
    // 클라우드 비전
    private static final String CLOUD_VISION_API_KEY = "AIzaSyBh67qI3OqbYiMO08YFAQwgteNRxd28jVg";
    public static final String FILE_NAME = "temp.jpg";
    private static final String ANDROID_CERT_HEADER = "X-Android-Cert";
    private static final String ANDROID_PACKAGE_HEADER = "X-Android-Package";
    private static final int MAX_LABEL_RESULTS = 10;
    private static final int MAX_DIMENSION = 1200;

    private static final String TAG = UploadItemActivity.class.getSimpleName();
    private static final int GALLERY_PERMISSIONS_REQUEST = 0;
    private static final int GALLERY_IMAGE_REQUEST = 1;
    public static final int CAMERA_PERMISSIONS_REQUEST = 2;
    public static final int CAMERA_IMAGE_REQUEST = 3;

    private static String category_result ="";
    private static int category_num = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_item);
        mQueue = Volley.newRequestQueue(this);
        pref = getSharedPreferences("pref", MODE_PRIVATE);
        token = pref.getString("token", null);
        sid = pref.getString("cid", null);
        listview = (ListView) findViewById(R.id.drawer_menulist);
        listview.setAdapter(super.MENU);
        listview.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View v, int position, long id) {
                selectDrawable((String) adapterView.getItemAtPosition(position));
            }
        });
        //카테고리 로드
        categorySpinner = findViewById(R.id.categorySpinner);
        categoryIDList = new ArrayList<String>();
        categoryList = new ArrayList<String>();
        categoryAdapt = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, categoryList);
        categoryAdapt.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        loadCategory();

        //판매 시작 시간 현재 시간으로 설정
        startTime = findViewById(R.id.startTime);
        endTime = findViewById(R.id.endTime);
        //long now = System.currentTimeMillis();
        //Date date = new Date(now);
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
        sTime = sdf.format(cal.getTime()); //현재 시간
        startTime.setText(sTime);
        /*try {
            Date now=sdf.parse(sTime);
            cal.setTime(now);
            cal.add(Calendar.MINUTE, 0); //10분 더하기
            String eTime = sdf.format(cal.getTime()); //종료 시간
            endTime.setText(eTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }*/

        //시간 스피너
        HourSpinner=findViewById(R.id.saleHourTimeSpinner);
        HourList=new ArrayList<String>();
        HourAdapt=new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_spinner_dropdown_item, HourList);
        HourAdapt.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        for(int i=0;i<10;i++){
            HourList.add(String.valueOf(i)+" 시간");
        }
        HourSpinner.setAdapter(HourAdapt);
        HourSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setSaleTime();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        MinuteSpinner=findViewById(R.id.saleMinuteTimeSpinner);
        MinuteList=new ArrayList<String>();
        MinuteAdapt=new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_spinner_dropdown_item, MinuteList);
        MinuteAdapt.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        for(int i=0;i<6;i++){
            MinuteList.add(String.valueOf(i*10)+" 분");
        }
        MinuteSpinner.setAdapter(MinuteAdapt);
        MinuteSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setSaleTime();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        //뷰 연결
        uploadImageButton=findViewById(R.id.uploadImage_btn);
        itemName = findViewById(R.id.itemName);
        itemCount = findViewById(R.id.itemCount);
        itemRawPrice = findViewById(R.id.rawPrice);
        itemSalePrice = findViewById(R.id.salePrice);
        useUpdetePrice=findViewById(R.id.updatePriceYES);
        minPrice=findViewById(R.id.minPrice);
        minPrice.setHint("판매 가격보다 낮게 입력하세요");
        itemDeliverable = findViewById(R.id.deliveryYES);
        uploadedItemList=findViewById(R.id.UploadedItemList);
        uploadedItem=new ArrayList<LinearLayout>();


        //리스너 등록
        uploadImageButton.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(UploadItemActivity.this);
            builder
                    .setMessage(R.string.dialog_select_prompt)
                    .setPositiveButton(R.string.dialog_select_gallery, (dialog, which) -> startGalleryChooser())
                    .setNegativeButton(R.string.dialog_select_camera, (dialog, which) -> startCamera());
            builder.create().show();
        });

        useUpdetePrice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                minPrice.setEnabled(true);
            }
        });
        upload_btn = findViewById(R.id.Item_Upload_btn);
        upload_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadItem();
            }
        });

        loadUploadedItem();
    }
    void setSaleTime(){
        String h=HourSpinner.getSelectedItem().toString();
        h=h.substring(0,h.length()-3);
        String m=MinuteSpinner.getSelectedItem().toString();
        m=m.substring(0,m.length()-2);
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
        try {
            Date now=sdf.parse(sTime);
            cal.setTime(now);
            cal.add(Calendar.HOUR_OF_DAY, Integer.parseInt(h));
            cal.add(Calendar.MINUTE, Integer.parseInt(m));
            String eTime = sdf.format(cal.getTime()); //종료 시간

            endTime.setText(eTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
    // 뒤로가기로 장바구니로 왔을 시 새로고침
    @Override
    public void onRestart() {
        super.onRestart();
        //When BACK BUTTON is pressed, the activity on the stack is restarted
        //Do what you want on the refresh procedure here
        loadUploadedItem();
    }
    void loadCategory() {
        String url = "http://54.180.90.90:3000/api/category";
        final JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject result = response.getJSONObject(i);
                                categoryIDList.add(String.valueOf(result.getInt("ID")));
                                String name = result.getString("name");
                                categoryList.add(name);
                            }
                            categorySpinner.setAdapter(categoryAdapt);
                            categorySpinner.setEnabled(false);
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

    void uploadItem() {
        //선택된 카테고리 ID 설정
        for (int i = 0; i < categoryList.size(); i++) {
            if (categorySpinner.getSelectedItem().equals(categoryAdapt.getItem(i))) {
                CategoryID = categoryIDList.get(i);
            }
        }

        /*Bitmap bitmap;
        //사진 할당
        bitmap = BitmapFactory.decodeResource(getApplicationContext().getResources(),
                R.drawable.ic_ddiptok);
        */
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos);
        //바이트 어레이로 변경
        byte[] imageBytes = baos.toByteArray();

        //어레이를 문자열로
        final String imageString = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        //어레이 길이


        //Toast.makeText(getApplicationContext(),CategoryID,Toast.LENGTH_SHORT).show();
        String url = "http://54.180.90.90:3000/api/item";
        StringRequest request = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        JSONObject msg = null;
                        try {
                            msg = new JSONObject(response);
                            if (msg.getString("success").equals("true")) {
                                Toast.makeText(getApplicationContext(), "상품을 등록하였습니다.", Toast.LENGTH_SHORT).show();
                                Intent intent;
                                //상품 등록 후 홈으로 이동
                                intent = new Intent(
                                        getApplicationContext(),
                                        MainActivity.class);
                                startActivity(intent);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_SHORT).show();
                    }
                }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();

                params.put("sid", sid);
                params.put("name", itemName.getText().toString());
                params.put("category_id", CategoryID);
                params.put("raw_price", itemRawPrice.getText().toString());
                params.put("sale_price", itemSalePrice.getText().toString());
                params.put("context", "맛있는");
                params.put("start_time", startTime.getText().toString());
                params.put("end_time", endTime.getText().toString());
                //사진
                params.put("image", imageString);
                if (itemDeliverable.isChecked()) {
                    params.put("deliverable", "1");
                } else {
                    params.put("deliverable", "0");
                }
                params.put("count", itemCount.getText().toString());
                //가격 변동
                if(useUpdetePrice.isChecked()) {
                    params.put("timesale","1");
                    params.put("leastprice",minPrice.getText().toString());
                }
                else{
                    params.put("timesale","0");
                }
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };
        mQueue.add(request);
    }

    //사진 촬영이 완료되면 실행되는 함수
    /*@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            imageBitmap = BitmapFactory.decodeFile(imageFilePath);
            // 비트맵 resize 부분
            int height = imageBitmap.getHeight();
            int width = imageBitmap.getWidth();

            Bitmap resized = null;

            while (height > 500) {
                resized = Bitmap.createScaledBitmap(imageBitmap, (width * 500) / height,500 , true);

                height = resized.getHeight();
                width = resized.getWidth();
            }


            //어떤 이미지뷰에 어떤 사진을 넣을건지 정하는 부분
            ((ImageView) findViewById(R.id.photo)).setImageBitmap(resized);
            imageBitmap=resized;

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos);
            //바이트 어레이로 변경
            byte[] imageBytes = baos.toByteArray();

            //어레이를 문자열로
            final String imageString = Base64.encodeToString(imageBytes, Base64.DEFAULT);
            //어레이 길이
            Toast.makeText(getApplicationContext(), String.valueOf(imageString.length()),Toast.LENGTH_SHORT).show();
        }

    }*/
/*
    // 화면에 카메라를 출력하는 함수
    private void sendTakePhotoIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }

            if (photoFile != null) {
                photoUri = FileProvider.getUriForFile(this, getPackageName(), photoFile);

                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }

        imageBitmap = BitmapFactory.decodeFile(imageFilePath);

    }
*/
    // 찍힌 사진을 파일로 저장하는 함수
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "TEST_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,      /* prefix */
                ".jpg",         /* suffix */
                storageDir          /* directory */
        );
        imageFilePath = image.getAbsolutePath();
        return image;

    }
    //판매 중인 상품 목록 초기화
    void ItemListInit(){
        uploadedItem.clear();
        uploadedItemList.removeAllViewsInLayout();
    }
    //판매 중인 상품 목록 로드
    void loadUploadedItem(){
        String url="http://54.180.90.90:3000/api/item/search";
        StringRequest request=new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //Toast.makeText(getApplicationContext(),sid+response,Toast.LENGTH_SHORT).show();
                        JSONArray arr=null;
                        try {
                            UploadedItemJSON=new JSONArray(response);
                            ItemListInit();
                            setItemLayout();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_SHORT).show();
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
                params.put("sid",sid);
                /*params.put("iid",SearchBar.getText().toString());
                params.put("sid",SearchBar.getText().toString());*/
                return params;
            }
        };
        mQueue.add(request);
    }
    void setItemLayout(){
        try {
            //현재 시간
            long now = System.currentTimeMillis();
            Date dateNow = new Date(now);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String currentTime = dateFormat.format(dateNow);

            for(int i=0;i<UploadedItemJSON.length();i++){
                //수평 레이아웃 생성
                final LinearLayout l = new LinearLayout(getApplicationContext());
                l.setOrientation(LinearLayout.HORIZONTAL);
                //json 오브젝트 받아오기
                JSONObject result=UploadedItemJSON.getJSONObject(i);
                String itemName=result.getString("itemName"); // 상품 이름
                String endTime=result.getString("endTime"); // 판매 종료 시간
                if(!endTime.equals("null")){
                    endTime=endTime.substring(0,endTime.lastIndexOf("T"))+"\n"+endTime.substring(endTime.lastIndexOf("T")+1,endTime.lastIndexOf("."));
                }
                int Price=result.getInt("salePrice"); // 가격
                final long iid=result.getLong("iid"); //상품 아이디

                //width 세팅
                LinearLayout.LayoutParams nameWidth = new LinearLayout.LayoutParams(findViewById(R.id.UIitemName_t_wrapper).getWidth(),
                        LinearLayout.LayoutParams.MATCH_PARENT);
                LinearLayout.LayoutParams timeWidth = new LinearLayout.LayoutParams(findViewById(R.id.UIendTime_t_wrapper).getWidth(),
                        LinearLayout.LayoutParams.MATCH_PARENT);
                LinearLayout.LayoutParams priceWidth = new LinearLayout.LayoutParams(findViewById(R.id.UIitemPrice_t_wrapper).getWidth(),
                        LinearLayout.LayoutParams.MATCH_PARENT);
                LinearLayout.LayoutParams buttonWidth = new LinearLayout.LayoutParams(findViewById(R.id.UIreUpload_t_wrapper).getWidth(),
                        LinearLayout.LayoutParams.MATCH_PARENT);


                //TextView 생성
                // 상품 이름
                TextView Name_t=new TextView(getApplicationContext());
                LinearLayout NameWrapper=new LinearLayout(getApplicationContext());
                NameWrapper.setLayoutParams(nameWidth);
                NameWrapper.setGravity(Gravity.CENTER);
                Name_t.setGravity(Gravity.CENTER);
                Name_t.setText(itemName);
                Name_t.setTextSize(20);
                NameWrapper.addView(Name_t);

                // 최저
                TextView Time_t=new TextView(getApplicationContext());
                LinearLayout TimeWrapper=new LinearLayout(getApplicationContext());
                TimeWrapper.setLayoutParams(timeWidth);
                TimeWrapper.setGravity(Gravity.CENTER);
                Time_t.setText(String.valueOf(endTime));
                Time_t.setTextSize(20);
                TimeWrapper.addView(Time_t);

                //  가격
                TextView Price_t=new TextView(getApplicationContext());
                LinearLayout PriceWrapper=new LinearLayout(getApplicationContext());
                PriceWrapper.setLayoutParams(priceWidth);
                PriceWrapper.setGravity(Gravity.CENTER);
                Price_t.setText(String.valueOf(Price));
                Price_t.setTextSize(20);
                PriceWrapper.addView(Price_t);

                //삭제
                /*TextView delete_t=new TextView(getApplicationContext());
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
                });*/

                //수평 레이아웃에 추가
                l.addView(NameWrapper);
                l.addView(TimeWrapper);
                l.addView(PriceWrapper);
                endTime=endTime.substring(0,endTime.lastIndexOf("\n"))+" "+endTime.substring(endTime.lastIndexOf("\n")+1,endTime.length());
                if(currentTime.compareTo(endTime)>0) {
                    //if(itemName.equals("소고기")){Toast.makeText(getApplicationContext(),currentTime+"vs"+endTime,Toast.LENGTH_SHORT).show();}
                    //재등록
                    Button reUpload = new Button(getApplicationContext());
                    LinearLayout reUploadWrapper = new LinearLayout(getApplicationContext());
                    reUploadWrapper.setLayoutParams(buttonWidth);
                    reUpload.setText("재등록");
                    reUpload.setGravity(Gravity.CENTER);
                    reUploadWrapper.addView(reUpload);
                    reUpload.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent;
                            intent = new Intent(
                                    getApplicationContext(),
                                    ReuploadActivity.class);
                            intent.putExtra("iid",iid);
                            startActivity(intent);
                        }
                    });
                    l.addView(reUploadWrapper);
                }
                //l.addView(deleteWrapper);

                l.setBackgroundResource(R.drawable.underline);
                //각각 삽니다 ArrayList에 추가
                uploadedItem.add(l);

            }
            //삽니다 레이아웃에 추가
            for(int i=0;i<uploadedItem.size();i++) {
                uploadedItemList.addView(uploadedItem.get(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }/*
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
    }*/

    public void startGalleryChooser() {
        if (PermissionUtils.requestPermission(this, GALLERY_PERMISSIONS_REQUEST, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select a photo"),
                    GALLERY_IMAGE_REQUEST);
        }
    }
    public void startCamera() {

        if (PermissionUtils.requestPermission(
                this,
                CAMERA_PERMISSIONS_REQUEST,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA)) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            Uri photoUri = FileProvider.getUriForFile(this, getPackageName(), getCameraFile());
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivityForResult(intent, CAMERA_IMAGE_REQUEST);
        }
    }

    public File getCameraFile() {
        File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return new File(dir, FILE_NAME);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            uploadImage(data.getData());
        } else if (requestCode == CAMERA_IMAGE_REQUEST && resultCode == RESULT_OK) {

            Uri photoUri = FileProvider.getUriForFile(this, getPackageName(), getCameraFile());
            uploadImage(photoUri);
        }

    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_PERMISSIONS_REQUEST:
                if (PermissionUtils.permissionGranted(requestCode, CAMERA_PERMISSIONS_REQUEST, grantResults)) {
                    startCamera();
                }
                break;
        }
    }

    public void uploadImage(Uri uri) {
        if (uri != null) {
            try {
                // scale the image to save on bandwidth
                Bitmap bitmap =
                        scaleBitmapDown(
                                MediaStore.Images.Media.getBitmap(getContentResolver(), uri),
                                MAX_DIMENSION);
                imageBitmap=bitmap;
                // 비트맵 resize 부분
                int height = imageBitmap.getHeight();
                int width = imageBitmap.getWidth();

                Bitmap resized = null;

                while (height > 500) {
                    resized = Bitmap.createScaledBitmap(imageBitmap, (width * 500) / height,500 , true);

                    height = resized.getHeight();
                    width = resized.getWidth();
                }
                imageBitmap=resized;

                callCloudVision(bitmap);
                ((ImageView) findViewById(R.id.photo)).setImageBitmap(bitmap);
                categoryList.add(0,"카테고리 분류 중..");
                categorySpinner.setAdapter(categoryAdapt);

            } catch (IOException e) {
                Log.d(TAG, "Image picking failed because " + e.getMessage());
                Toast.makeText(this, R.string.image_picker_error, Toast.LENGTH_LONG).show();
            }
        } else {
            Log.d(TAG, "Image picker gave us a null image.");
            Toast.makeText(this, R.string.image_picker_error, Toast.LENGTH_LONG).show();
        }
    }

    private Vision.Images.Annotate prepareAnnotationRequest(Bitmap bitmap) throws IOException {
        HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

        VisionRequestInitializer requestInitializer =
                new VisionRequestInitializer(CLOUD_VISION_API_KEY) {
                    /**
                     * We override this so we can inject important identifying fields into the HTTP
                     * headers. This enables use of a restricted cloud platform API key.
                     */
                    @Override
                    protected void initializeVisionRequest(VisionRequest<?> visionRequest)
                            throws IOException {
                        super.initializeVisionRequest(visionRequest);

                        String packageName = getPackageName();
                        visionRequest.getRequestHeaders().set(ANDROID_PACKAGE_HEADER, packageName);

                        String sig = PackageManagerUtils.getSignature(getPackageManager(), packageName);

                        visionRequest.getRequestHeaders().set(ANDROID_CERT_HEADER, sig);
                    }
                };

        Vision.Builder builder = new Vision.Builder(httpTransport, jsonFactory, null);
        builder.setVisionRequestInitializer(requestInitializer);

        Vision vision = builder.build();

        BatchAnnotateImagesRequest batchAnnotateImagesRequest =
                new BatchAnnotateImagesRequest();
        batchAnnotateImagesRequest.setRequests(new ArrayList<AnnotateImageRequest>() {{
            AnnotateImageRequest annotateImageRequest = new AnnotateImageRequest();

            // Add the image
            Image base64EncodedImage = new Image();
            // Convert the bitmap to a JPEG
            // Just in case it's a format that Android understands but Cloud Vision
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
            byte[] imageBytes = byteArrayOutputStream.toByteArray();

            // Base64 encode the JPEG
            base64EncodedImage.encodeContent(imageBytes);
            annotateImageRequest.setImage(base64EncodedImage);

            // add the features we want
            annotateImageRequest.setFeatures(new ArrayList<Feature>() {{
                Feature labelDetection = new Feature();
                labelDetection.setType("LABEL_DETECTION");
                labelDetection.setMaxResults(MAX_LABEL_RESULTS);
                add(labelDetection);
            }});

            // Add the list of one thing to the request
            add(annotateImageRequest);
        }});

        Vision.Images.Annotate annotateRequest =
                vision.images().annotate(batchAnnotateImagesRequest);
        // Due to a bug: requests to Vision API containing large images fail when GZipped.
        annotateRequest.setDisableGZipContent(true);
        Log.d(TAG, "created Cloud Vision request object, sending request");

        return annotateRequest;
    }

    private void confirmDialog(String result) {
        category_result = result;
        if (result.equals("Cloud Vision API request failed. Check logs for details.")){
            selectDialog();
        }
        else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(false);
            builder
                    .setMessage(category_result + "(이)가 맞나요?")
                    .setPositiveButton("네", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            // Yes-code
                            categorySpinner.setSelection(category_num);
                        }
                    })
                    .setNegativeButton("아니요", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {

                            dialog.cancel();
                            selectDialog();
                        }
                    })
                    .show();
            categoryList.remove(0);
            categorySpinner.setAdapter(categoryAdapt);
            categorySpinner.setEnabled(true);
        }
    }

    private void selectDialog(){
        final CharSequence[] items = {"기타", "돈까스/일식", "디저트/빵", "신선품", "족발/보쌈", "치킨/피자", "카페/음료" };


        AlertDialog.Builder builder = new AlertDialog.Builder(this);     // 여기서 this는 Activity의 this

        builder.setCancelable(false);

        // 여기서 부터는 알림창의 속성 설정

        builder.setTitle("카테고리를 선택하세요.")        // 제목 설정

                .setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener(){

                    // 목록 클릭시 설정

                    public void onClick(DialogInterface dialog, int index){

                        category_num = index;
                        categorySpinner.setSelection(category_num);
                        dialog.dismiss(); // 누르면 바로 닫히는 형태
                    }
                });



        AlertDialog dialog = builder.create();    // 알림창 객체 생성

        dialog.show();    // 알림창 띄우기

    }

    private class LableDetectionTask extends AsyncTask<Object, Void, String> {
        private final WeakReference<UploadItemActivity> mActivityWeakReference;
        private Vision.Images.Annotate mRequest;

        LableDetectionTask(UploadItemActivity activity, Vision.Images.Annotate annotate) {
            mActivityWeakReference = new WeakReference<>(activity);
            mRequest = annotate;
        }

        @Override
        protected String doInBackground(Object... params) {
            try {
                Log.d(TAG, "created Cloud Vision request object, sending request");
                BatchAnnotateImagesResponse response = mRequest.execute();
                return convertResponseToString(response);

            } catch (GoogleJsonResponseException e) {
                Log.d(TAG, "failed to make API request because " + e.getContent());
            } catch (IOException e) {
                Log.d(TAG, "failed to make API request because of other IOException " +
                        e.getMessage());
            }
            return "Cloud Vision API request failed. Check logs for details.";
        }

        protected void onPostExecute(String result) {
            UploadItemActivity activity = mActivityWeakReference.get();
            if (activity != null && !activity.isFinishing()) {
                TextView imageDetail = activity.findViewById(R.id.textView);
                //  imageDetail.setText(result);
                confirmDialog(result);
            }
        }
    }

    private void callCloudVision(final Bitmap bitmap) {
        // Switch text to loading
        //textView.setText(R.string.loading_message);

        // Do the real work in an async task, because we need to use the network anyway
        try {
            AsyncTask<Object, Void, String> labelDetectionTask = new LableDetectionTask(this, prepareAnnotationRequest(bitmap));
            labelDetectionTask.execute();
        } catch (IOException e) {
            Log.d(TAG, "failed to make API request because of other IOException " +
                    e.getMessage());
        }
    }

    private Bitmap scaleBitmapDown(Bitmap bitmap, int maxDimension) {

        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int resizedWidth = maxDimension;
        int resizedHeight = maxDimension;

        if (originalHeight > originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = (int) (resizedHeight * (float) originalWidth / (float) originalHeight);
        } else if (originalWidth > originalHeight) {
            resizedWidth = maxDimension;
            resizedHeight = (int) (resizedWidth * (float) originalHeight / (float) originalWidth);
        } else if (originalHeight == originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = maxDimension;
        }
        return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false);
    }

    private static String convertResponseToString(BatchAnnotateImagesResponse response) {
        StringBuilder message = new StringBuilder("");
        int flag = 0;
        String[] category_temp = {};

        // 돈까스 일식
        String[] category1 = {"korokke", "noodle", "ramen", "suish", "asian food", "soup", "fried food", "japanese cuisine", "japanese food", "tonkatsu"};
        // 디저트/빵
        String[] category2 = {"bread", "bakery", "baking", "dessert", "cake"};
        // 신선품
        String[] category3 = {"fruit", "vegetable", "seafood", "fish", "fork", "beaf"};
        // 족발/보쌈
        String[] category4 = {"korean food", "meat"};
        // 치킨/피자
        String[] category5 = {"chicken", "pizza", "fast food", "european food", "fried chicken"};
        // 카페/음료
        String[] category6 = {"drink", "coffee", "espresso", "cappuccino", "caffeine", "latte","juice", "smoothie"};

        // 기타

        List<EntityAnnotation> labels = response.getResponses().get(0).getLabelAnnotations();
        if (labels != null) {
            for (EntityAnnotation label : labels) {
                message.append(String.format(Locale.US, "%.3f: %s", label.getScore(), label.getDescription()));

                if (checkCategory(category1, labels)) {
                    category_result = "돈까스/일식";
                    category_num = 1;
                    return "돈까스/일식";
                }
                else if (checkCategory(category2, labels)) {
                    category_result = "디저트/빵";
                    category_num = 2;
                    return "디저트/빵";
                }
                else if (checkCategory(category3, labels)) {
                    category_result = "신선품";
                    category_num = 3;
                    return "신선품";
                }
                else if (checkCategory(category4, labels)) {
                    category_result = "족발/보쌈";
                    category_num = 4;
                    return "족발/보쌈";
                }
                else if (checkCategory(category5, labels)) {
                    category_result = "치킨/피자";
                    category_num = 5;
                    return "치킨/피자";
                }

                else if (checkCategory(category6, labels)) {
                    category_result = "카페/음료";
                    category_num = 6;
                    return "카페/음료";
                }

                category_num = 0;
                return "기타";

            }
        } else {
            message.append("nothing");
        }

        return message.toString();
    }

    static boolean checkCategory(String[] keywords, List<EntityAnnotation> labels){
        for (String keyword : keywords){
            for (EntityAnnotation label : labels) {
                if (label.getDescription().equals(keyword)) return true;
            }
        }
        return false;
    }
}