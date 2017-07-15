package com.dexafree.materiallistviewexample;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by peace-win10 on 2017-05-24.
 */

public class CreateActivity extends AppCompatActivity {
    Button btn1, btn2;
    EditText etName, etAge, etPhone;
    RadioButton radMale, radFemale;
    RadioGroup radio;
    String strJson = "";
    Person person;
    static String fingerID;

    EditText userID;
    View diaView;

    BluetoothChatFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_activity);
        setTitle("환자정보추가");
        fragment = new BluetoothChatFragment();

        btn1 = (Button) findViewById(R.id.btn1);
        btn2 = (Button) findViewById(R.id.btn2);

        etName = (EditText) findViewById(R.id.etName);
        etAge = (EditText) findViewById(R.id.etAge);
        radio = (RadioGroup) findViewById(R.id.radio);
        radMale = (RadioButton) findViewById(R.id.radMale);
        radFemale = (RadioButton) findViewById(R.id.radFemale);
        etPhone = (EditText) findViewById(R.id.etPhone);

        diaView = (View) View.inflate(CreateActivity.this, R.layout.input_userid, null);
        AlertDialog.Builder dlg = new AlertDialog.Builder(CreateActivity.this);
        dlg.setTitle("USER ID 입력");
        dlg.setView(diaView);
        dlg.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                fingerID = fragment.getbMessage();
            }
        });
        dlg.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        dlg.show();

        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!validate())
                    Toast.makeText(getBaseContext(), "Enter some data!", Toast.LENGTH_LONG).show();
                else {
                    // call AsynTask to perform network operation on separate thread
                    HttpAsyncTask httpTask = new HttpAsyncTask(CreateActivity.this);
                    httpTask.execute("http://192.168.64.136:8080/EMIRS/InsertPatient",
                            fingerID, etName.getText().toString(), etAge.getText().toString(),
                            isSex(), etPhone.getText().toString());
                    finish();
                }
            }
        });
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
    private String isSex() {
        if(radio.getCheckedRadioButtonId() == R.id.radMale)
            return "male";
        else if(radio.getCheckedRadioButtonId() == R.id.radFemale)
            return "female";
        else
            return null;
    }

    public static String POST(String url, Person person){
        InputStream is = null;
        String result = "";
        try {
            URL urlCon = new URL(url);
            HttpURLConnection httpCon = (HttpURLConnection)urlCon.openConnection();
            httpCon.setRequestMethod("POST");

            // OutputStream으로 POST 데이터를 넘겨주겠다는 옵션.
            httpCon.setDoOutput(true);
            // InputStream으로 서버로 부터 응답을 받겠다는 옵션.
            httpCon.setDoInput(true);

            String json = "";

            // build jsonObject
            JSONObject jsonObject = new JSONObject();
            jsonObject.accumulate("userID", fingerID);
            jsonObject.accumulate("name", person.getName());
            jsonObject.accumulate("age", person.getAge());
            jsonObject.accumulate("sex", person.getSex());
            jsonObject.accumulate("phone_num", person.getPhone());
            json = jsonObject.toString();

            httpCon.setRequestProperty("Accept", "application/json");
            httpCon.setRequestProperty("Content-type", "application/json");

            OutputStream os = httpCon.getOutputStream();
            os.write(json.getBytes("euc-kr"));
            //String sendData = "name="+person.getName()+"&country="+person.getCountry()+"&twitter="+person.getTwitter();
            //os.write(sendData.getBytes("euc-kr"));
            os.flush();
            // receive response as inputStream
            try {
                is = httpCon.getInputStream();
                // convert inputstream to string
                if(is != null)
                    result = convertInputStreamToString(is);
                else
                    result = "Did not work!";
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            finally {
                httpCon.disconnect();
            }
        }

        catch (IOException e) {
            e.printStackTrace();
        }

        catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }

        return result;
    }

    private boolean validate(){
        if(etName.getText().toString().trim().equals(""))
            return false;
        else if(etAge.getText().toString().trim().equals(""))
            return false;
        else if(isSex() == null)
            return false;
        else if(etPhone.getText().toString().trim().equals(""))
            return false;
        //else if(etAddress.getText().toString().trim().equals(""))
            //return false;
        else
            return true;
    }

    class HttpAsyncTask extends AsyncTask<String, Void, String> {
        private CreateActivity createActivity;
        HttpAsyncTask(CreateActivity createActivity) {
            this.createActivity = createActivity;
        }

        @Override
        protected String doInBackground(String... urls) {
            person = new Person();
            person.setUserID(urls[1]);
            person.setName(urls[2]);
            person.setAge(urls[3]);
            person.setSex(urls[4]);
            person.setPhone(urls[5]);
            Log.d("person", person.toString());

            return POST(urls[0], person);
        }
        // onPostExecute displays the results of the AsyncTask.
        // 데이터 받는 부분
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            strJson = result;
            createActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(createActivity, strJson, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException{
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream, "EUC-KR"));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }
}