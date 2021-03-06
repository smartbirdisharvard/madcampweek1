package com.example.myapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;


import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;


public class phone_num extends AppCompatActivity {
    RecyclerView mRecyclerView;
    RecyclerView.LayoutManager mLayoutManager;
    private EditText editSearch;
    private ArrayList<phonenum_item> list;
    private ArrayList<phonenum_item> arrayList;
    private ArrayList<phonenum_item> data;
    private MyAdapter myAdapter;

    private final long FINISH_INTERVAL_TIME = 2000;
    private long backPressedTime = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.phone_num);

        int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1;
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS);
        if (permissionCheck == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, MY_PERMISSIONS_REQUEST_READ_CONTACTS);
//            permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS);
//            if (permissionCheck == PackageManager.PERMISSION_DENIED) {
//                Intent it = new Intent(this, MainActivity.class);
//                startActivity(it);
//                finish();
//            }
        }
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            mRecyclerView = findViewById(R.id.phonenum);
            mRecyclerView.setHasFixedSize(true);
            mLayoutManager = new LinearLayoutManager(this);
            mRecyclerView.setLayoutManager(mLayoutManager);

            ArrayList<phonenum_item> data = new ArrayList<>();
            try {
                data = getContactList();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            MyAdapter myAdapter = new MyAdapter(data);
            mRecyclerView.setAdapter(myAdapter);

            //검색 기능 구현
            editSearch = (EditText) findViewById(R.id.editSearch);
            mRecyclerView = findViewById(R.id.phonenum);
            arrayList = new ArrayList<phonenum_item>();
            list = new ArrayList<phonenum_item>();
            arrayList.addAll(data);
            list.addAll(data);



            editSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    // input창에 문자를 입력할때마다 호출된다.
                    // search 메소드를 호출한다.
                    String text = editSearch.getText().toString();
                    search(text);

                }
            });
        }


   }


    // 검색 수행 방법
    public void search(String charText) {
        list.clear();

        if (charText.length() == 0) {
            list.addAll(arrayList);
        }
        else{
            for(int i = 0; i < arrayList.size(); i++){
                if (arrayList.get(i).getName().toLowerCase().contains(charText)) {
                    list.add(arrayList.get(i));
                }
                else if (arrayList.get(i).getPhonenum().contains(charText)) {
                    list.add(arrayList.get(i));
                }
            }
        }
        data = list;
        MyAdapter mySearchAdapter = new MyAdapter(list);
        mySearchAdapter.notifyDataSetChanged();
        mRecyclerView.setAdapter(mySearchAdapter);
    }


   // HOME 버튼을 누르면 Main으로
    public void home(View v) {
        Intent it = new Intent(this, MainActivity.class);
        startActivity(it);
        finish();
    }

    // + add to a contact 버튼을 누르면 폰에 내장된 연락처 추가 창으로
    public void add_contact(View view) {
        Intent intent = new Intent(Intent.ACTION_INSERT, ContactsContract.Contacts.CONTENT_URI);
        startActivity(intent);

    }

    // 폰에 저장된 연락처 가져오기
    private ArrayList<phonenum_item> getContactList() throws JSONException {
        ArrayList<phonenum_item> data = new ArrayList<>();
        int icon = R.drawable.user;

        Cursor cursor = managedQuery(ContactsContract.Contacts.CONTENT_URI, new String[]{
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts.PHOTO_ID
        }, null, null, ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC");

        JSONArray jsonArray = new JSONArray();

        while (cursor.moveToNext()) {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("icon", icon);
                jsonObject.put("name", cursor.getString(1));
                jsonObject.put("phonenum", contactsPhone(cursor.getString(0)));

                jsonArray.put(jsonObject);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        for (int i=0; i<jsonArray.length(); i++) {
            JSONObject dataJsonObject = jsonArray.getJSONObject(i);
            phonenum_item item = new phonenum_item(dataJsonObject.getInt("icon"), dataJsonObject.getString("name"), dataJsonObject.getString("phonenum"));
            data.add(item);
        }

//            try {
//                String id = cursor.getString(0);
//                String name = cursor.getString(1);
//                String phonenum = contactsPhone(id);
//                phonenum_item item = new phonenum_item(icon, name, phonenum);
//                data.add(item);
//            } catch (Exception e) {
//                System.out.println(e.toString());
//            }
//        }

        return data;
    }

    private String contactsPhone(String id) {
        String result = null;

        if ((id == null) || (id.trim().equals(""))) {
            return null;
        }

        Cursor cursor = managedQuery(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, new String[]{
                ContactsContract.CommonDataKinds.Phone.NUMBER
        }, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + id, null, null);

        while (cursor.moveToNext()) {
            try {
                result = cursor.getString(0);
            } catch (Exception e) {
                System.out.println(e.toString());
            }
        }
        cursor.close();

        return result;
    }

    @Override
    public void onBackPressed() {
        long tempTime = System.currentTimeMillis();
        long intervalTime = tempTime - backPressedTime;

        if (0 <= intervalTime && FINISH_INTERVAL_TIME >= intervalTime)
        {
            super.onBackPressed();
        }
        else
        {
            backPressedTime = tempTime;
            Intent it = new Intent(this, MainActivity.class);
            startActivity(it);
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(phone_num.this, phone_num.class);
            startActivity(intent);
            finish();
        }
        else {
            Intent intent2 = new Intent(phone_num.this, MainActivity.class);
            startActivity(intent2);
            finish();
        }
    }
}