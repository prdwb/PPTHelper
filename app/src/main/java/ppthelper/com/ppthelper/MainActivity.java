package ppthelper.com.ppthelper;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Time;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity {
    private DBManager mgr;
    //ListView listView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //listView = (ListView) findViewById(R.id.listView);
        mgr = new DBManager(this);
//        final Button button2 = (Button) findViewById(R.id.button2);
//        button2.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                processPhoto(imageFilePath);
//            }
//        });
//        final Button button3 = (Button) findViewById(R.id.button3);
//        button3.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                new Thread(runnableOCR).start();
//            }
//        });
//        final Button button4 = (Button) findViewById(R.id.button4);
//        button4.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                new Thread(runnableTokenizer).start();
//            }
//        });
        final EditText editText = (EditText) findViewById(R.id.search);
        final Button button5 = (Button) findViewById(R.id.button5);
        button5.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                query(v, editText.getText().toString());
            }
        });

        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    query(v, editText.getText().toString());
                    handled = true;
                }
                return handled;
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //应用的最后一个Activity关闭时应释放DB
        mgr.closeDB();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public String imageFilePath = "";
    public String filename = "";
    public String appHome = Environment.getExternalStorageDirectory().getAbsolutePath() + "/PPTHelper";
    public String base64 = "";
    public String word;

    public void getPhoto(View v) {
        Intent intent = new Intent();
        intent.setAction("android.media.action.IMAGE_CAPTURE");
        intent.addCategory("android.intent.category.DEFAULT");
        Time t=new Time();
        t.setToNow();
        int year=t.year;
        int month=t.month;
        int day=t.monthDay;
        int hour=t.hour;
        int minute=t.minute;
        int second=t.second;
        filename=""+year+month+day+hour+minute+second;
        //String filename = "0919.jpg";
        //Log.i(TAG, "" + year + month + day + hour + minute + second);

        File dir = new File(appHome);
        if (!dir.exists()){
            dir.mkdir();
        }
        imageFilePath = appHome + "/" + filename + ".jpg";
        File file = new File(imageFilePath);
        Uri uri = Uri.fromFile(file);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        //this.startActivity(intent);
        startActivityForResult(intent, 1);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 1 && resultCode == RESULT_OK){
            super.onActivityResult(requestCode, resultCode, data);
            Toast.makeText(this, "图片已保存到:\n" + imageFilePath, Toast.LENGTH_LONG).show();
            processPhoto(imageFilePath);
            new Thread(runnableOCR).start();
            //new Thread(runnableTokenizer).start();
        }

    }

    public String request(String httpUrl, String httpArg) {
        Log.i("info", "requesting");
        BufferedReader reader = null;
        String result = null;
        StringBuffer sbf = new StringBuffer();

        try {
            URL url = new URL(httpUrl);
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");
            // 填入apikey到HTTP header
            connection.setRequestProperty("apikey", "02ee809effd308166940c835fb47bf1b");
            connection.setDoOutput(true);
            connection.getOutputStream().write(httpArg.getBytes("UTF-8"));
            connection.connect();
            InputStream is = connection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String strRead = null;
            while ((strRead = reader.readLine()) != null) {
                sbf.append(strRead);
                sbf.append("\r\n");
            }
            reader.close();
            result = sbf.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return result;
    }

    public String OCR(String base64) {
        String httpUrl = "http://apis.baidu.com/apistore/idlocr/ocr";
        String httpArg = "fromdevice=android&clientip=222.26.211.5&detecttype=Recognize&languagetype=CHN_ENG&imagetype=1&image=" + base64;
        //Log.i("httpArg", httpArg);
        String jsonResult = request(httpUrl, httpArg);

        if (jsonResult != null) {
            Log.i("result", jsonResult);
            try {
                JSONObject object = (JSONObject) new JSONTokener(jsonResult).nextValue();
                String errMsg = object.getString("errMsg");
                if (errMsg.equals("success")) {
                    JSONArray retData = object.getJSONArray("retData");
                    JSONObject rect = retData.getJSONObject(0);
                    word = rect.getString("word");

                    Log.i("result", errMsg);
                    Log.i("result", word);
                    Log.i("result", "parse complete");
                    return word;
                } else {

                    Log.i("result", errMsg);
                    return "OCR Fail";
                }
            } catch (JSONException e) {
                e.printStackTrace();
                return "OCR Fail";
            }
        } else {
            Toast.makeText(this, "fail", Toast.LENGTH_LONG).show();
            Log.i("result", "null");
            return "Request Fail";
        }
    }

    public void compressPhoto(String imageFilePath){
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imageFilePath, options);
        // Calculate inSampleSize
        options.inSampleSize = 4;

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        Bitmap bm = BitmapFactory.decodeFile(imageFilePath, options);

        File f = new File(imageFilePath);
        if (f.exists()) {
            f.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(f);
            bm.compress(Bitmap.CompressFormat.JPEG, 50, out);
            out.flush();
            out.close();
            //Log.i("info2", "已经保存");
            //return bm;
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //return null;

    }

    public String imageEncoder(String imageFilePath) {
        Bitmap bm = BitmapFactory.decodeFile(imageFilePath);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos); //bm is the bitmap object
        byte[] b = baos.toByteArray();
        String encodedImage = Base64.encodeToString(b, Base64.NO_WRAP);
        //Log.i("base64", encodedImage);
        //return encodedImage;
        String base64String = null;
        try {
            base64String = URLEncoder.encode(encodedImage, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return base64String;
    }

    public void processPhoto(String imageFilePath){
        Log.i("info", "processing");
        Toast.makeText(this, "正在处理", Toast.LENGTH_LONG).show();
        //Bitmap bm = compressPhoto(imageFilePath);
        compressPhoto(imageFilePath);
        base64 = imageEncoder(imageFilePath);
        Log.i("info", "process complete");
        //Toast.makeText(this, "process complete", Toast.LENGTH_LONG).show();
        //OCR(base64);
    }

    public String tokenizer(String word) {
        if(word == null){
            Log.i("tokenizer", "input word is null");
            return "error";
        }else {
            String httpUrl = "http://apis.baidu.com/apistore/pullword/words";
            String wordString = null;
            try {
                wordString = URLEncoder.encode(word, "utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            String httpArg = "source=" + wordString + "&param1=0&param2=0.8";
            String jsonResult = requestTokenizer(httpUrl, httpArg);
            if(jsonResult.equals("Request Fail")){
                return "Request Fail";
            }
            if (jsonResult != null) {
                //Log.i("tokenizer", jsonResult);
                String[] wordArray = jsonResult.split("\r\n");
                if (!wordArray[0].equals("error")) {
                    ArrayList<Picture> pictures = new ArrayList<Picture>();

                    for (int i = 0; i < wordArray.length; i++) {
                        Log.i("tokenizer", wordArray[i]);
                        Picture pictureTemp = new Picture(wordArray[i], filename);
                        pictures.add(pictureTemp);
                    }
                    mgr.add(pictures);
                    Log.i("tokenizer", "success");
                    return "success";
                }else{
                    Log.i("tokenizer", "server return error");
                    return "server return error";
                }
            } else {
                Log.i("tokenizer", "fail");
                return "error";
            }
        }
    }

    public String requestTokenizer(String httpUrl, String httpArg) {
        BufferedReader reader = null;
        String result = null;
        StringBuffer sbf = new StringBuffer();
        httpUrl = httpUrl + "?" + httpArg;

        try {
            URL url = new URL(httpUrl);
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            connection.setRequestMethod("GET");
            // 填入apikey到HTTP header
            connection.setRequestProperty("apikey",  "02ee809effd308166940c835fb47bf1b");
            connection.connect();
            InputStream is = connection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String strRead = null;
            while ((strRead = reader.readLine()) != null) {
                sbf.append(strRead);
                sbf.append("\r\n");
            }
            reader.close();
            result = sbf.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "Request Fail";
        }
        return result;
    }


    Handler handlerOCR = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            String val = data.getString("value");
            if(val.equals("Request Fail")){
                Toast.makeText(MainActivity.this, "服务器忙，正在重试", Toast.LENGTH_LONG).show();
                new Thread(runnableOCR).start();
            }else if(val.equals("OCR Fail")){
                Toast.makeText(MainActivity.this, "未识别出字符，图片不会添加标签", Toast.LENGTH_LONG).show();
            }else{
                //Toast.makeText(MainActivity.this, "识别成功", Toast.LENGTH_LONG).show();
                new Thread(runnableTokenizer).start();
            }
        }
    };

    Handler handlerTokenizer = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            String val = data.getString("value");
            if(val.equals("error")) {
                Toast.makeText(MainActivity.this, "未识别出字符，图片不会添加标签", Toast.LENGTH_LONG).show();
            }else if(val.equals("server return error")) {
                Toast.makeText(MainActivity.this, "识别失败，图片标签为“error”", Toast.LENGTH_LONG).show();
            }else if(val.equals("Request Fail")){
                Toast.makeText(MainActivity.this, "服务器忙，正在重试", Toast.LENGTH_LONG).show();
                new Thread(runnableTokenizer).start();
            }else{
                Toast.makeText(MainActivity.this, "识别成功，图片已添加标签", Toast.LENGTH_LONG).show();
            }
        }
    };

    Runnable runnableOCR = new Runnable() {
        @Override
        public void run() {
            word = OCR(base64);
            Message msg = new Message();
            Bundle data = new Bundle();
            data.putString("value", word);
            msg.setData(data);
            handlerOCR.sendMessage(msg);

        }
    };

    Runnable runnableTokenizer = new Runnable() {
        @Override
        public void run() {
            String result = tokenizer(word);
            Message msg = new Message();
            Bundle data = new Bundle();
            data.putString("value", result);
            msg.setData(data);
            handlerTokenizer.sendMessage(msg);
        }
    };

    public void query(View view, String keyword) {
        List<Picture> pictures = mgr.query(keyword);
        ArrayList<Map<String, String>> list = new ArrayList<Map<String, String>>();
        for (Picture picture : pictures) {
            HashMap<String, String> map = new HashMap<String, String>();
            //map.put("tag", picture.tag);
            String imagePath = appHome + "/" + picture.fileName + ".jpg";
            map.put("imagePath", imagePath);
            //Log.i("database", picture.tag + ":" + picture.fileName);
            list.add(map);
        }
        if(String.valueOf(list.size()).equals("0")){
            Toast.makeText(this, "无匹配，请缩短关键词", Toast.LENGTH_LONG).show();
        }else{
            Intent intent = new Intent(this, QueryActivity.class);
            intent.putExtra("list", list);
            startActivityForResult(intent, 2);
        }
//        SimpleAdapter adapter = new SimpleAdapter(this, list, R.layout.query,
//                new String[]{"imagePath"}, new int[]{R.id.file});
//        listView.setAdapter(adapter);

//        String imagePath = appHome + "/" + list[1]filename + ".jpg";
//        File imageFile = new File(imageFilePath);
//        ImageView jpgView = (ImageView)findViewById(R.id.imageView);
//        Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
//        jpgView.setImageBitmap(bitmap);
    }


}