package com.example.jin.myapplication;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;

import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.content.pm.Signature;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.security.MessageDigest;
import java.util.ArrayList;
class Order{
    String oid; //주문 번호
    String cid; //고객 번호
    String iid; //상품 번호
    Order(){
        oid="";
        cid="";
        iid="";
    }
    Order(String o,String c,String i){
        oid=o;
        cid=c;
        iid=i;
    }
}
class Item{
    String iid;
    String iname;
    Item(){
        iid="";
        iname="";
    }
    Item(String id,String name){
        iname=name;
        iid=id;
    }
}
public class TmpActivity extends ToolbarActivity {
    ArrayList<Item> ItemList=new ArrayList<Item>();
    ArrayList<Order> OrderList=new ArrayList<Order>();
    ArrayList<TextView> TextList=new ArrayList<TextView>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tmp);
        setTitle("알림 기능");
        //해시 키 받아오기
        //getHashKey();


        //임시 데이터(아이템 리스트)

        ItemList.add(new Item("111","닭발"));
        ItemList.add(new Item("222","만두"));
        ItemList.add(new Item("333","초밥"));
        //임시 데이터(주문 리스트)
        OrderList.add(new Order("001","01011112222","111"));
        OrderList.add(new Order("002","01011113333","222"));

        //아이템 리스트 로드
        loadItemList();
        //알림 버튼
        findViewById(R.id.button).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                //채널 설정
                String channelId="channel";
                String channelName="Channel Name";

                NotificationManager notifManager=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);

                if(Build.VERSION.SDK_INT>=android.os.Build.VERSION_CODES.O){
                    int importance=NotificationManager.IMPORTANCE_HIGH;

                    NotificationChannel mChannel=new NotificationChannel(channelId,channelName,importance);
                    notifManager.createNotificationChannel(mChannel);
                }
                //알림 이미지
                Bitmap mLargeIconForNoti=
                        BitmapFactory.decodeResource(getResources(), R.drawable.ic_ddiptok);
                PendingIntent mPendinIntent=PendingIntent.getActivity(TmpActivity.this,0,new Intent(getApplicationContext(),TmpActivity.class),
                        PendingIntent.FLAG_CANCEL_CURRENT
                );
                NotificationCompat.Builder mBuilder=new NotificationCompat.Builder(getApplicationContext(),channelId);
                mBuilder.setSmallIcon(R.drawable.ic_ddiptok);
                mBuilder.setContentTitle("추천 상품");
                int cnt=0;
                for(int j=0;j<OrderList.size();j++){
                    EditText CustomerId= (EditText) findViewById(R.id.cid);
                    if(OrderList.get(j).cid.equals(CustomerId.getText().toString())){
                        cnt++;
                    }
                }
                String recommendITEM=ItemList.get(recommendItem(cnt)).iname;
                mBuilder.setContentText(recommendITEM);
                mBuilder.setDefaults(Notification.DEFAULT_VIBRATE);
                mBuilder.setLargeIcon(mLargeIconForNoti);
                mBuilder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
                mBuilder.setAutoCancel(true);
                mBuilder.setContentIntent(mPendinIntent);

                notifManager.notify(0,mBuilder.build());
            }
        });
        Button goCart=(Button) findViewById(R.id.goCart);
        goCart.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent intent= new Intent(
                        getApplicationContext(),
                        CartActivity.class);
                startActivity(intent);
            }
        });
        Button goMap=(Button) findViewById(R.id.goMap);
        goMap.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(
                        getApplicationContext(),
                        MapActivity.class);
                startActivity(intent);
            }
        });
        Button goRecommendItem=(Button) findViewById(R.id.goRecommendItem);
        goRecommendItem.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(
                        getApplicationContext(),
                        RecommendActivity.class);
                startActivity(intent);
            }
        });
        Button goHome=(Button) findViewById(R.id.goHome);
        goHome.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(
                        getApplicationContext(),
                        HomeActivity.class);
                startActivity(intent);
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
    private void getHashKey(){
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md;
                md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String something = new String(Base64.encode(md.digest(), 0));
                Log.d("Hash key", something);
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            Log.e("name not found", e.toString());
        }
    }
    void loadItemList(){
        //아이템 목록 레이아웃에 추가
        LinearLayout layout = (LinearLayout) findViewById(R.id.ItemList);
        for(int i=0;i<ItemList.size();i++){
            //가로 레이아웃
            LinearLayout l = new LinearLayout(this);
            l.setOrientation(LinearLayout.HORIZONTAL);
            //뷰 생성
            TextView t=new TextView(this);
            t.setText(ItemList.get(i).iname);
            t.setTextSize(30);
            t.setWidth(150);
            TextView t2=new TextView(this);
            //주문 횟수 계산
            int cnt=0;
            for(int j=0;j<OrderList.size();j++){
                if(OrderList.get(j).iid==ItemList.get(i).iid){
                    cnt++;
                }
            }
            t2.setText(String.valueOf(cnt));
            cnt=0;
            t2.setTextSize(30);
            TextList.add(t2);
            Button b=new Button(this);
            b.setId(Integer.parseInt(ItemList.get(i).iid));
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                    //클릭된 버튼의 아이디를 가져온다.
                    int id=v.getId();
                    //주문리스트에 추가
                    EditText CustomerId= (EditText) findViewById(R.id.cid);
                    OrderList.add(new Order("주문번호",CustomerId.getText().toString(),String.valueOf(id)));
                    //클릭된 버튼의 아이디와 일치하는 아이템 아이디의 인덱스 알아내기
                    int j;
                    for(j=0;j<ItemList.size();j++) {
                        if (String.valueOf(id).equals(ItemList.get(j).iid)) {
                            break;
                        }
                    }
                    //일치하는 아이템의 수량 1개 증가하여 textview 수정
                    int cnt=Integer.parseInt(TextList.get(j).getText().toString());
                    cnt++;
                    TextList.get(j).setText(String.valueOf(cnt));


                }
            });
            b.setText("구매");
            l.addView(t);
            l.addView(TextList.get(i));
            l.addView(b);
            layout.addView(l);



        }
    }
    //반환: 추천 상품 리스트 인덱스 / 인자: 구매 횟수
    int recommendItem(int cnt){
        //알고리즘
        //구매 횟수 0번: 가장 판매된 개수가 많은 상품 추천 (같은 개수일 경우 최신 상품)
        //구매 횟수 1번: 구매했던 상품 추천
        //구매 횟수 2번이상: 가장 많이 구매한 상품 추천 (같은 개수일 경우 최신 상품)

        //각 아이템 마다 판매 개수 저장

        //고객 아이디
        EditText CustomerId= (EditText) findViewById(R.id.cid);
        if(cnt==0){
            int many_soldcnt=0;
            int many_soldidx=0;
            for(int i=0;i<ItemList.size();i++){
                int soldcnt=0;
                for(int j=0;j<OrderList.size();j++){
                    if(ItemList.get(i).iid.equals(OrderList.get(j).iid)){
                        soldcnt++;
                    }
                }
                if(many_soldcnt<=soldcnt){
                    many_soldcnt=soldcnt;
                    many_soldidx=i;
                }
            }
            return many_soldidx;
        }
        else if(cnt==1){
            for(int i=0;i<OrderList.size();i++){
                if(CustomerId.getText().toString().equals(OrderList.get(i).cid)){
                    for(int j=0;j<ItemList.size();j++){
                        if(OrderList.get(i).iid.equals(ItemList.get(j).iid)){
                            return j;
                        }
                    }
                }
            }
        }
        else{
            int many_purchasecnt=0;
            int many_purchaseidx=0;
            for(int i=0;i<ItemList.size();i++){
                int purchasecnt=0;
                for(int j=0;j<OrderList.size();j++){
                    if(ItemList.get(i).iid.equals(OrderList.get(j).iid) && CustomerId.getText().toString().equals(OrderList.get(j).cid)){
                        purchasecnt++;
                    }
                }
                if(many_purchasecnt<=purchasecnt){
                    many_purchasecnt=purchasecnt;
                    many_purchaseidx=i;
                }
            }
            return many_purchaseidx;
        }
        return 0;
    }
}

