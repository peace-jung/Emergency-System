package com.dexafree.materiallistviewexample;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by peace-win10 on 2017-05-24.
 */

public class UpdateOrigin extends AppCompatActivity {
    ListView list;
    Button btnPost, btnCancel;
    String strJson = "{\n" +
            "\"1\" : \"만성 두통\",\n" +
            "\"2\" : \"콜레라\",\n" +
            "\"3\" : \"장티푸스\",\n" +
            "\"4\" : \"이질\",\n" +
            "\"5\" : \"식중독\",\n" +
            "\"6\" : \"노로바이러스\",\n" +
            "\"7\" : \"결핵\",\n" +
            "\"8\" : \"흑사병\",\n" +
            "\"9\" : \"탄저병\",\n" +
            "\"10\" : \"한센병\",\n" +
            "\"11\" : \"파상풍\",\n" +
            "\"12\" : \"갑상선기능저하증\",\n" +
            "\"13\" : \"당뇨병\",\n" +
            "\"14\" : \"각기병\",\n" +
            "\"15\" : \"괴혈병\",\n" +
            "\"16\" : \"뇌막염\",\n" +
            "\"17\" : \"알츠하이머\",\n" +
            "\"18\" : \"뇌전증\",\n" +
            "\"19\" : \"편두통\",\n" +
            "\"20\" : \"손목터널증후군\",\n" +
            "\"21\" : \"만성 기관지염\",\n" +
            "\"22\" : \"폐부종\",\n" +
            "\"23\" : \"기흉\",\n" +
            "\"24\" : \"크론병\",\n" +
            "\"25\" : \"궤양성 대장염\",\n" +
            "\"26\" : \"복막염\",\n" +
            "\"27\" : \"간염\",\n" +
            "\"28\" : \"간경화\",\n" +
            "\"29\" : \"지방간\",\n" +
            "\"30\" : \"췌장염\",\n" +
            "\"31\" : \"담석증\"\n" +
            "}";
    EditText period;
    View diaView;
    static String userID;
    org.json.simple.JSONArray ja = null;
    org.json.JSONObject jo = null;
    RadioButton radMale, radFemale;
    RadioGroup radio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update_disease);
        setTitle("질병정보 새로 입력");

        btnPost = (Button) findViewById(R.id.btnPost);
        btnCancel = (Button) findViewById(R.id.btnCancel);
        list = (ListView) findViewById(R.id.diseaseList);


        ja = new org.json.simple.JSONArray();

        userID = getIntent().getStringExtra("userID");

        ArrayList<String> arr = new ArrayList<>();
        try {
            org.json.JSONObject jo = new org.json.JSONObject(strJson);
            for(int i = 1; i<=jo.length(); i++) {
                arr.add(jo.getString(i+""));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_multiple_choice, arr);
        list.setAdapter(adapter);
        list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                //체크 해제 시 삭제
                if(!list.isItemChecked(position)) {
                    for(int i=0; i<ja.size(); i++) {
                        try {
                            if(position+1 == new JSONObject(ja.get(i).toString()).getInt("disease_number"))
                                ja.remove(i);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    return;
                }
                diaView = View.inflate(UpdateOrigin.this, R.layout.input_disease, null);
                radio = (RadioGroup) diaView.findViewById(R.id.radio);
                radMale = (RadioButton) diaView.findViewById(R.id.radMale);
                radFemale = (RadioButton) diaView.findViewById(R.id.radFemale);
                period = (EditText) diaView.findViewById(R.id.period);

                AlertDialog.Builder dlg = new AlertDialog.Builder(UpdateOrigin.this);
                dlg.setTitle("추가 정보");
                dlg.setView(diaView);

                dlg.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        jo = new org.json.JSONObject();
                        try {
                            jo.put("disease_number", position+1);
                            jo.put("operation_status", isOperation());
                            jo.put("period", period.getText().toString());
                            jo.put("userID", userID);
                            ja.add(jo);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
                dlg.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        list.setItemChecked(position, false);
                    }
                });
                dlg.show();
            }
        });

        btnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateOrigin.HttpAsyncTask httpTask = new UpdateOrigin.HttpAsyncTask(UpdateOrigin.this);
                httpTask.execute("http://192.168.64.136:8080/EMIRS/UpdateRecord", "");
                finish();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private String isOperation() {
        if(radio.getCheckedRadioButtonId() == R.id.radMale)
            return "Y";
        else if(radio.getCheckedRadioButtonId() == R.id.radFemale)
            return "N";
        else
            return null;
    }

    public static String POST(String url, org.json.simple.JSONArray ja) {
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

            // build jsonObject
            Log.d("jsonArray", ja.toString());

            httpCon.setRequestProperty("Accept", "application/json");
            httpCon.setRequestProperty("Content-type", "application/json");

            OutputStream os = httpCon.getOutputStream();
            os.write(ja.toString().getBytes("euc-kr"));
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
        private UpdateOrigin updateOrigin;

        HttpAsyncTask(UpdateOrigin updateOrigin) {
            this.updateOrigin = updateOrigin;
        }

        @Override
        protected String doInBackground(String... urls) {
            return POST(urls[0], ja);
        }

        // onPostExecute displays the results of the AsyncTask.
        // 데이터 받는 부분
        @Override
        protected void onPostExecute(final String result) {
            super.onPostExecute(result);
            strJson = result;

            updateOrigin.runOnUiThread(new Runnable() {
                @Override
                public void run() {
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
}
