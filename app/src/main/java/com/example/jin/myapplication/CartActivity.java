package com.example.jin.myapplication;

import android.graphics.Point;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class CartActivity extends ToolbarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;
       // TextView tmp=(TextView) findViewById(R.id.tmp);
        //EditText et=(EditText) findViewById(R.id.cid);
        //tmp.setText(et.getText().toString());
        ArrayList<CheckBox> cart_check = new ArrayList<CheckBox>(); //장바구니 체크 리스트
        ArrayList<TextView> cart = new ArrayList<TextView>(); //장바구니 텍스트뷰 리스트
        ArrayList<TextView> cost = new ArrayList<TextView>(); //가격 텍스트뷰 리스트
        ArrayList<Button> minus_btn = new ArrayList<Button>(); //감소 버튼 리스트
        ArrayList<TextView> number = new ArrayList<TextView>(); //수량 텍스트뷰 리스트
        ArrayList<Button> plus_btn = new ArrayList<Button>(); //증가 버튼 리스트
        ArrayList<Button> delete_bnt = new ArrayList<Button>(); //삭제 버튼 리스트

        ArrayList<String> contents = new ArrayList<String>(); //스트링(상품 이름) 리스트
        contents.add("갈비");
        contents.add("닭발");
        contents.add("떡볶이");
        ArrayList<String> costs = new ArrayList<String>(); //스트링(가격) 리스트
        costs.add("5000");
        costs.add("4000");
        costs.add("1000");
        ArrayList<String> contents2 = new ArrayList<String>(); //스트링(상품 수량) 리스트
        contents2.add("1");
        contents2.add("2");
        contents2.add("1");
        //----------------------------------------
        LinearLayout layout = (LinearLayout) findViewById(R.id.cart_list);
        TextView sum = (TextView) findViewById(R.id.sum);
        sum.setText(String.valueOf(0));
        //LinearLayout.LayoutParams paramsheight = new LinearLayout.LayoutParams(width,height/2);
        //layout.setLayoutParams(paramsheight);

        for (int i = 0; i < contents.size(); i++) {
            CheckBox c = new CheckBox(this); //체크박스
            TextView t1 = new TextView(this); //상품명
            TextView t2 = new TextView(this); //가격
            Button b1 = new Button(this); //증가버튼
            TextView t3 = new TextView(this); //수량
            Button b2 = new Button(this); //감소버튼
            Button b3 = new Button(this); //삭제버튼

            t1.setTextSize(30);
            t1.setWidth(400);
            t1.setText(contents.get(i));
            t2.setTextSize(30);
            t2.setWidth(150);
            t2.setText(costs.get(i));
            b1.setText("▼");
            t3.setTextSize(30);
            t3.setWidth(100);
            t3.setText(contents2.get(i));
            b2.setText("▲");
            b3.setText("x");
            cart_check.add(c);
            cart.add(t1);
            cost.add(t2);
            minus_btn.add(b1);
            number.add(t3);
            plus_btn.add(b2);
            delete_bnt.add(b3);
        }
        //버튼 크기 설정
        LinearLayout.LayoutParams parambtn = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
        parambtn.width = 100;

        // 장바구니 타이틀
        LinearLayout t_l = (LinearLayout) findViewById(R.id.cart_at);

        // 선택
        TextView t_check = new TextView(this);
        t_check.setTextSize(15);
        t_check.setWidth(80);
        t_check.setGravity(Gravity.CENTER);
        t_check.setText("선택");
        // 상품명
        TextView t_name = new TextView(this);
        t_name.setTextSize(15);
        t_name.setWidth(400);
        t_name.setGravity(Gravity.CENTER);
        t_name.setText("상품명");
        // 가격
        TextView t_cost = new TextView(this);
        t_cost.setTextSize(15);
        t_cost.setWidth(150);
        t_cost.setGravity(Gravity.CENTER);
        t_cost.setText("가격");
        // 수량
        TextView t_num = new TextView(this);
        t_num.setTextSize(15);
        t_num.setWidth(300);
        t_num.setGravity(Gravity.CENTER);
        t_num.setText("수량");
        // 삭제
        TextView t_del = new TextView(this);
        t_del.setTextSize(15);
        t_del.setWidth(100);
        t_del.setGravity(Gravity.CENTER);
        t_del.setText("삭제");

        t_l.addView(t_check);
        t_l.addView(t_name);
        t_l.addView(t_cost);
        t_l.addView(t_num);
        t_l.addView(t_del);

        for (int i = 0; i < cart.size(); i++) {
            LinearLayout l = new LinearLayout(this);
            l.setOrientation(LinearLayout.HORIZONTAL);
            // 체크박스 뷰 추가
            l.addView(cart_check.get(i));
            // 상품명 뷰 추가
            l.addView(cart.get(i));
            // 가격 뷰 추가
            l.addView(cost.get(i));
            // 감소 버튼 뷰 추가
            l.addView(minus_btn.get(i), parambtn);
            // 수량 뷰 추가
            number.get(i).setGravity(Gravity.CENTER); //수량 가운데 정렬
            l.addView(number.get(i));
            // 증가 버튼 뷰 추가
            l.addView(plus_btn.get(i), parambtn);
            // 삭제 버튼 뷰 추가
            l.addView(delete_bnt.get(i), parambtn);
            // 종합 항목 추가
            layout.addView(l);
        }
    }
}
