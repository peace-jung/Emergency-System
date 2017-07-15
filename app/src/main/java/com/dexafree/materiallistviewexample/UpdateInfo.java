package com.dexafree.materiallistviewexample;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.json.simple.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by peace-win10 on 2017-06-01.
 */

public class UpdateInfo extends AppCompatActivity {
    Button btn1, btn2;
    Person person;
    EditText etName, etAge, etPhone;
    String strJson = "";
    RadioButton radMale, radFemale;
    RadioGroup radio;

    String name, sex;
    String userID, age;
    String phone_num;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update_info);
        setTitle("개인정보수정");

        btn1 = (Button) findViewById(R.id.btn1);
        btn2 = (Button) findViewById(R.id.btn2);

        etName = (EditText) findViewById(R.id.etName);
        etAge = (EditText) findViewById(R.id.etAge);
        radio = (RadioGroup) findViewById(R.id.radio);
        radMale = (RadioButton) findViewById(R.id.radMale);
        radFemale = (RadioButton) findViewById(R.id.radFemale);
        etPhone = (EditText) findViewById(R.id.etPhone);

        userID = getIntent().getStringExtra("userID");
        name = getIntent().getStringExtra("name");
        age = getIntent().getStringExtra("age");
        sex = getIntent().getStringExtra("sex");
        phone_num = getIntent().getStringExtra("phone_num");

        etName.setText(name);
        etAge.setText(age, TextView.BufferType.EDITABLE);
        etPhone.setText(phone_num);

        if(sex.equals("male")) {
            radio.check(R.id.radMale);
        }
        else if(sex.equals("female")) {
            radio.check(R.id.radFemale);
        }

        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (!validate())
                        Toast.makeText(getBaseContext(), "Enter some data!", Toast.LENGTH_LONG).show();
                    else {
                        // call AsynTask to perform network operation on separate thread
                        UpdateInfo.HttpAsyncTask httpTask = new UpdateInfo.HttpAsyncTask(UpdateInfo.this);
                        httpTask.execute("http://192.168.64.136:8080/EMIRS/UpdatePatient",
                                userID, etName.getText().toString(),
                                isSex(), etPhone.getText().toString(), etAge.getText().toString());
                        finish();
                    }
                } catch (Exception e) {
                    Toast.makeText(getBaseContext(), "Error at Server", Toast.LENGTH_LONG).show();
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


    class HttpAsyncTask extends AsyncTask<String, Void, String> {
        private UpdateInfo updateInfo;
        HttpAsyncTask(UpdateInfo updateInfo) {
            this.updateInfo = updateInfo;
        }

        @Override
        protected String doInBackground(String... urls) {
            person = new Person();
            person.setUserID(urls[1]);
            person.setName(urls[2]);
            person.setSex(urls[3]);
            person.setPhone(urls[4]);
            person.setAge(urls[5]);

            return POST(urls[0], person);
        }
        // onPostExecute displays the results of the AsyncTask.
        // 데이터 받는 부분
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            strJson = result;
            updateInfo.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        //Toast.makeText(updateInfo, "Saved", Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
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
            jsonObject.put("userID", person.getUserID());
            jsonObject.put("name", person.getName());
            jsonObject.put("sex", person.getSex());
            jsonObject.put("phone_num", person.getPhone());
            jsonObject.put("age", person.getAge());
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

    private static String convertInputStreamToString(InputStream inputStream) throws IOException{
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream, "EUC-KR"));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
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

    private String isSex() {
        if(radio.getCheckedRadioButtonId() == R.id.radMale)
            return "male";
        else if(radio.getCheckedRadioButtonId() == R.id.radFemale)
            return "female";
        else
            return null;
    }
}