package com.dexafree.materiallistviewexample;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.dexafree.materialList.card.Card;
import com.dexafree.materialList.card.CardProvider;
import com.dexafree.materialList.card.OnActionClickListener;
import com.dexafree.materialList.card.action.TextViewAction;
import com.dexafree.materialList.view.MaterialListView;

import org.json.simple.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.recyclerview.animators.SlideInLeftAnimator;

public class MainActivity extends AppCompatActivity {
    private Context mContext;
    private MaterialListView mListView;
    Button btn1, btn2, btn3, btn4, btnBL;
    Fingerprint finger;
    String fingerID = "";
    String strJson = "";

    EditText userID;
    View diaView;

    String uname, uage, usex, uphone, udisease;
    String allData;

    BluetoothChatFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Medical System");
        fragment = new BluetoothChatFragment();

        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            //fragment = new BluetoothChatFragment();
            transaction.replace(R.id.sample_content_fragment, fragment);
            transaction.commit();
        }

        // Save a reference to the context
        mContext = this;

        // Bind the MaterialListView to a variable
        mListView = (MaterialListView) findViewById(R.id.material_listview);
        mListView.setItemAnimator(new SlideInLeftAnimator());
        mListView.getItemAnimator().setAddDuration(300);
        mListView.getItemAnimator().setRemoveDuration(300);


        // Fill the array withProvider mock content
        fillArray();

        btn1 = (Button) findViewById(R.id.btn1);
        btn1 = (Button) findViewById(R.id.btn1);
        btn2 = (Button) findViewById(R.id.btn2);
        btn3 = (Button) findViewById(R.id.btn3);
        btn4 = (Button) findViewById(R.id.btn4);

        //조회기능
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search();
            }
        });
        //추가기능
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), CreateActivity.class);
                startActivityForResult(intent, 0); //의료정보 입력화면으로 intent
                //startActivity(intent);
            }
        });
        //수정기능
        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (strJson == "") {
                    Toast.makeText(MainActivity.this, "정보가 없습니다. 조회를 먼저하세요.", Toast.LENGTH_LONG).show();
                    return;
                }
                AlertDialog.Builder dlg = new AlertDialog.Builder(MainActivity.this);
                final String[] chooseArray = new String[]{"개인정보수정", "질병정보 추가", "질병정보 새로 입력"};
                dlg.setTitle("수정");
                dlg.setItems(chooseArray, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        update_info(which);
                        update_disease(which);
                        update_origin(which);
                    }
                });
                dlg.setPositiveButton("닫기", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        return;
                    }
                });
                dlg.show();
            }
        });
        //삭제기능
        btn4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                diaView = View.inflate(MainActivity.this, R.layout.input_userid, null);
                AlertDialog.Builder dlg = new AlertDialog.Builder(MainActivity.this);
                dlg.setTitle("삭제하시겠습니까?");
                dlg.setView(diaView);
                dlg.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            fingerID = fragment.getbMessage();
                            Log.i("btMsg", fragment.getbMessage());
                            MainActivity.HttpAsyncTask httpTask = new MainActivity.HttpAsyncTask(MainActivity.this);
                            httpTask.execute("http://192.168.64.136:8080/EMIRS/DeletePatient", fingerID);

                            strJson = "";
                            mListView.getAdapter().clearAll();
                            fillArray();
                            getRandomCard();
                        } catch (Exception e) {
                            Toast.makeText(MainActivity.this, "no data!", Toast.LENGTH_LONG);
                        }
                    }
                });
                dlg.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        return;
                    }
                });
                dlg.show();
            }
        });
    }

    public void search() {
        diaView = View.inflate(MainActivity.this, R.layout.input_userid, null);
        AlertDialog.Builder dlg = new AlertDialog.Builder(MainActivity.this);
        dlg.setTitle("지문을 입력하세요.");
        dlg.setView(diaView);
        dlg.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    fingerID = fragment.getbMessage();
                    MainActivity.HttpAsyncTask httpTask = new MainActivity.HttpAsyncTask(MainActivity.this);
                    httpTask.execute("http://192.168.64.136:8080/EMIRS/RetrieveInfo", fingerID);
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "no data!", Toast.LENGTH_LONG);
                }
            }
        });
        dlg.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                return;
            }
        });
        dlg.show();
    }

    public boolean update_info(int which) {
        if (which == 0) {
            try {
                Intent intent = new Intent(getApplicationContext(), UpdateInfo.class);
                intent.putExtra("userID", fingerID);
                intent.putExtra("name", uname);
                intent.putExtra("age", uage);
                intent.putExtra("phone_num", uphone);
                intent.putExtra("sex", usex);
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public boolean update_disease(int which) {
        if (which == 1) {
            Intent intent = new Intent(getApplicationContext(), UpdateDisease.class);
            intent.putExtra("userID", fingerID);
            startActivity(intent);
        }
        return false;
    }

    public boolean update_origin(int which) {
        if (which == 2) {
            Intent intent = new Intent(getApplicationContext(), UpdateOrigin.class);
            intent.putExtra("userID", fingerID);
            startActivity(intent);
        }
        return false;
    }

    public static String POST(String url, Fingerprint finger) {
        InputStream is = null;
        String result = "";
        try {
            URL urlCon = new URL(url);
            HttpURLConnection httpCon = (HttpURLConnection) urlCon.openConnection();
            httpCon.setRequestMethod("POST");

            // OutputStream으로 POST 데이터를 넘겨주겠다는 옵션.
            httpCon.setDoOutput(true);
            // InputStream으로 서버로 부터 응답을 받겠다는 옵션.
            httpCon.setDoInput(true);

            String json = "";

            // build jsonObject
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("userID", finger.getFingerprint());
            json = jsonObject.toString();

            httpCon.setRequestProperty("Accept", "application/json");
            httpCon.setRequestProperty("Content-type", "application/json");

            OutputStream os = httpCon.getOutputStream();
            os.write(json.getBytes("euc-kr"));
            os.flush();
            // receive response as inputStream
            try {
                is = httpCon.getInputStream();
                // convert inputstream to string
                if (is != null) {
                    result = convertInputStreamToString(is);
                } else
                    result = "Did not work!";
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                httpCon.disconnect();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }

        return result;
    }

    private class HttpAsyncTask extends AsyncTask<String, Void, String> {
        private MainActivity mainActivity;

        HttpAsyncTask(MainActivity mainActivity) {
            this.mainActivity = mainActivity;
        }

        @Override
        protected String doInBackground(String... urls) {
            finger = new Fingerprint();
            finger.setFingerprint(urls[1]);

            return POST(urls[0], finger);
        }

        // onPostExecute displays the results of the AsyncTask.
        // 데이터 받는 부분
        @Override
        protected void onPostExecute(final String result) {
            super.onPostExecute(result);
            strJson = result;

            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    /*if(strJson.equals(""))
                        Toast.makeText(MainActivity.this, "해당 정보가 없습니다.", Toast.LENGTH_LONG).show();*/
                    mListView.getAdapter().clearAll();
                    setArray();
                }
            });
        }
    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "EUC-KR"));
        String line = "";
        String result = "";
        while ((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;
    }

    private void fillArray() {
        List<Card> cards = new ArrayList<>();
        cards.add(getRandomCard());
        mListView.getAdapter().addAll(cards);
    }

    private Card getRandomCard() {
        String title = "Unknown";
        String description = "Please Input Your ID";

        final CardProvider provider = new Card.Builder(this)
                .setTag("BASIC_BUTTONS_CARD")
                .setDismissible()
                .withProvider(new CardProvider())
                .setLayout(R.layout.material_basic_buttons_card)
                .setTitle(title)
                .setSubtitleColor(1)
                .setDescription(description)
                .addAction(R.id.left_text_button, new TextViewAction(this)
                        .setText("정보수정")
                        .setTextResourceColor(R.color.black_button)
                        .setListener(new OnActionClickListener() {
                            @Override
                            public void onActionClicked(View view, Card card) {
                                Toast.makeText(mContext, "Please Input Your ID", Toast.LENGTH_SHORT).show();
                            }
                        }))
                .addAction(R.id.right_text_button, new TextViewAction(this)
                        .setText("숨기기")
                        .setTextResourceColor(R.color.accent_material_dark)
                        .setListener(new OnActionClickListener() {
                            @Override
                            public void onActionClicked(View view, Card card) {
                                mListView.getAdapter().clearAll();
                                strJson = "";
                                setArray();
                                Toast.makeText(mContext, "Please Input Your ID", Toast.LENGTH_SHORT).show();
                            }
                        }));
        return provider.endConfig().build();
    }

    private void setArray() {
        if(strJson.equals("")) {
            fillArray();
            Toast.makeText(MainActivity.this, "해당 정보가 없습니다.", Toast.LENGTH_LONG).show();
            return;
        }
        List<Card> cards = new ArrayList<>();
        Card card = setCard();
        if (card == null) return;
        cards.add(card);
        mListView.getAdapter().addAll(cards);
    }

    private Card setCard() {
        if (strJson.equals("null")) {
            Toast.makeText(MainActivity.this, "no data!", Toast.LENGTH_LONG);
            return null;
        }
        Log.i("json", strJson);
        uname = uage = usex = uphone = udisease = "";
        try {
            org.json.JSONArray jar = new org.json.JSONArray(strJson);
            if (jar.getJSONObject(0).has("disease_name")) {
                for (int i = 0; i < jar.length(); i++) {
                    udisease += new org.json.JSONObject(jar.getJSONObject(i).toString()).getString("disease_name") + ",\t";
                    udisease += new org.json.JSONObject(jar.getJSONObject(i).toString()).getString("period") + ",\t";
                    udisease += "수술 " + new org.json.JSONObject(jar.getJSONObject(i).toString()).getString("operation_status") + "\n";
                }
            }
            uname = new org.json.JSONObject(jar.getJSONObject(0).toString()).getString("name");
            uage = new org.json.JSONObject(jar.getJSONObject(0).toString()).getString("age");
            usex = new org.json.JSONObject(jar.getJSONObject(0).toString()).getString("sex");
            uphone = new org.json.JSONObject(jar.getJSONObject(0).toString()).getString("phone_num");
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (uname == "") uname = "Unknown";
        allData = "나이 : " + uage + "세\n성별 : " + usex + "\n전화번호 : " + uphone + "\n\n병력 : \n";

        final CardProvider provider = new Card.Builder(this)
                .setTag("BASIC_BUTTONS_CARD")
                .setDismissible()
                .withProvider(new CardProvider())
                .setLayout(R.layout.material_basic_buttons_card)
                .setTitle(uname)
                .setSubtitleColor(1)
                .setDescription(allData + udisease)
                .addAction(R.id.left_text_button, new TextViewAction(this)
                        .setText("정보수정")
                        .setTextResourceColor(R.color.black_button)
                        .setListener(new OnActionClickListener() {
                            @Override
                            public void onActionClicked(View view, Card card) {
                                update_info(0);
                            }
                        }))
                .addAction(R.id.right_text_button, new TextViewAction(this)
                        .setText("숨기기")
                        .setTextResourceColor(R.color.accent_material_dark)
                        .setListener(new OnActionClickListener() {
                            @Override
                            public void onActionClicked(View view, Card card) {
                                mListView.getAdapter().clearAll();
                                strJson = "";
                                fillArray();
                            }
                        }));
        return provider.endConfig().build();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_clear:
                mListView.getAdapter().clearAll();
                fillArray();
                strJson = "";
                break;
            /*case R.id.action_add_at_start:
                mListView.getAdapter().clearAll();
                fillArray();
                //addMockCardAtStart();
                break;*/
        }
        return super.onOptionsItemSelected(item);
    }
}
