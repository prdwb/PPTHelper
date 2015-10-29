package ppthelper.com.ppthelper;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.SimpleAdapter;
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
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import android.app.DialogFragment;


public class MainActivity extends AppCompatActivity {
    private DBManager mgr;
    //GridView gridView_main = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_main);

//        // Retrieve the AppCompact Toolbar
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//
//        // Set the padding to match the Status Bar height
//        toolbar.setPadding(0, getStatusBarHeight(), 0, 0);

        mgr = new DBManager(this);

        File dir = new File(appHome);
        if (!dir.exists()){
            dir.mkdir();
        }

        GridView gridView_main = (GridView) findViewById(R.id.gridView_main);
        ArrayList<Map<String, String>> list = getAllPictures(new File(appHome));
        SimpleAdapter adapter = new SimpleAdapter(this, list, R.layout.query,
                new String[]{"imagePath"}, new int[]{R.id.file});
        gridView_main.setAdapter(adapter);
        gridView_main.setOnItemClickListener(new ItemClickListener());
        gridView_main.setOnItemLongClickListener(new ItemLongClickListener());
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
//        if (id == R.id.action_settings) {
//            return true;
//        }
        switch(id) {
            case R.id.action_settings:
                return true;
            case R.id.action_photo:
                getPhoto();
                break;
            case R.id.action_search:
                QueryDialogFragment query = new QueryDialogFragment();
                //query.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                query.show(getSupportFragmentManager(), "query");
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private ArrayList<Map<String, String>> getAllPictures(File parentDir) {
        ArrayList<Map<String, String>> list = new ArrayList<Map<String, String>>();
        File[] files = parentDir.listFiles();
        for (File file : files) {
            if (file.getName().endsWith("_c.jpg")) {
                HashMap<String, String> map = new HashMap<String, String>();
                String imagePath = appHome + "/" + file.getName();
                map.put("imagePath", imagePath);
                list.add(map);
            }
        }
        return list;
    }

    class ItemClickListener implements AdapterView.OnItemClickListener
    {

        public void onItemClick(AdapterView<?> arg0,//The AdapterView where the click happened
                                View arg1,//The view within the AdapterView that was clicked
                                int arg2,//The position of the view in the adapter
                                long arg3//The row id of the item that was clicked
        ) {
            //在本例中arg2=arg3
            HashMap<String, String> item=(HashMap<String, String>) arg0.getItemAtPosition(arg2);
            //QueryActivity.imagePath = item.get("imagePath");
            Intent intent = new Intent(arg0.getContext(), PictureActivity.class);
            String imagePath = item.get("imagePath");
            imagePath = imagePath.substring(0, imagePath.length() - 6) + ".jpg";
            intent.putExtra("imagePath", imagePath);
            arg0.getContext().startActivity(intent);
        }

    }

    class ItemLongClickListener implements AdapterView.OnItemLongClickListener {
        public boolean onItemLongClick(AdapterView<?> arg0,//The AdapterView where the click happened
                                       View arg1,//The view within the AdapterView that was clicked
                                       int arg2,//The position of the view in the adapter
                                       long arg3//The row id of the item that was clicked
        ) {
            HashMap<String, String> item=(HashMap<String, String>) arg0.getItemAtPosition(arg2);
            String imagePath = item.get("imagePath");
            String[] array = imagePath.split("/");
            String filename = array[array.length - 1];
            filename = filename.substring(0, filename.length() - 6);
            ArrayList<String> tags = mgr.queryTag(filename);

            if(String.valueOf(tags.size()).equals("0")){
                tagMessage = "无标签";
            }else{
                tagMessage = "";
                for (String tag : tags)
                    tagMessage += (tag + " ");
            }
            TagDialogFragment tagDialog = new TagDialogFragment();
            tagDialog.show(getSupportFragmentManager(), "tag");
            return true;
        }
    }

    public String imageFilePath = "";
    public String compressedImageFilePath = "";
    public String filename = "";
    public String appHome = Environment.getExternalStorageDirectory().getAbsolutePath() + "/PPTHelper";
    public String base64 = "";
    public String word;
    public String tagMessage;

    public void getPhoto() {
        Intent intent = new Intent();
        intent.setAction("android.media.action.IMAGE_CAPTURE");
        intent.addCategory("android.intent.category.DEFAULT");

        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int date = calendar.get(Calendar.DATE);
        int hour = calendar.get(Calendar.HOUR);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        filename = "" + year + month + date + hour + minute + second;


        imageFilePath = appHome + "/" + filename + ".jpg";
        compressedImageFilePath = appHome + "/" + filename + "_c" + ".jpg";
        File file = new File(imageFilePath);
        Uri uri = Uri.fromFile(file);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 1 && resultCode == RESULT_OK){
            super.onActivityResult(requestCode, resultCode, data);
            Toast.makeText(this, "图片已保存到:\n" + imageFilePath, Toast.LENGTH_LONG).show();
            processPhoto(imageFilePath);
            new Thread(runnableOCR).start();
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

    public int OCR(String base64) {
        String httpUrl = "http://apis.baidu.com/idl_baidu/baiduocrpay/idlocrpaid";
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
                    //return word;
                    return OCR_SUCCESS;
                } else {

                    Log.i("result", errMsg);
                    return OCR_FAIL;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                return OCR_FAIL;
            }
        } else {
            Toast.makeText(this, "fail", Toast.LENGTH_LONG).show();
            Log.i("result", "null");
            return OCR_REQUEST_FAIL;
        }
    }

    public void compressPhoto(String imageFilePath){
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 4;
        Bitmap bm = BitmapFactory.decodeFile(imageFilePath, options);

        File f = new File(compressedImageFilePath);

        try {
            FileOutputStream out = new FileOutputStream(f);
            bm.compress(Bitmap.CompressFormat.JPEG, 10, out);
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

    public String imageEncoder(String compressedImageFilePath) {
        Bitmap bm = BitmapFactory.decodeFile(compressedImageFilePath);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos); //bm is the bitmap object
        byte[] b = baos.toByteArray();
        String encodedImage = Base64.encodeToString(b, Base64.NO_WRAP);

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
        compressPhoto(imageFilePath);
        base64 = imageEncoder(compressedImageFilePath);
        Log.i("info", "process complete");
    }

    public int tokenizer(String word) {
        if(word == null){
            Log.i("tokenizer", "input word is null");
                return TOKENIZER_ERROR;
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
                return TOKENIZER_REQUEST_FAIL;
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
                    return TOKENIZER_SUCCESS;
                }else{
                    Log.i("tokenizer", "server return error");
                    return TOKENIZER_SERVER_RETURN_ERROR;
                }
            } else {
                Log.i("tokenizer", "fail");
                return TOKENIZER_ERROR;
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

    public static final int OCR_REQUEST_FAIL = 11;
    public static final int OCR_FAIL = 12;
    public static final int OCR_SUCCESS = 13;
    public static final int TOKENIZER_ERROR = 14;
    public static final int TOKENIZER_SERVER_RETURN_ERROR = 15;
    public static final int TOKENIZER_REQUEST_FAIL = 16;
    public static final int TOKENIZER_SUCCESS = 17;

    private static class MyHandler extends Handler{
        WeakReference<MainActivity> mActivity;

        MyHandler(MainActivity activity) {
            mActivity = new WeakReference<MainActivity>(activity);
        }
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            MainActivity theActivity = mActivity.get();
            switch(msg.what) {
                case OCR_REQUEST_FAIL: {
                    Toast.makeText(theActivity, "服务器忙，正在重试", Toast.LENGTH_LONG).show();
                    new Thread(theActivity.runnableOCR).start();
                    break;
                }
                case OCR_FAIL: {
                    Toast.makeText(theActivity, "未识别出字符，图片不会添加标签", Toast.LENGTH_LONG).show();
                    break;
                }
                case OCR_SUCCESS: {
                    new Thread(theActivity.runnableTokenizer).start();
                    break;
                }
                case TOKENIZER_ERROR: {
                    Toast.makeText(theActivity, "未识别出字符，图片不会添加标签", Toast.LENGTH_LONG).show();
                    break;
                }
                case TOKENIZER_SERVER_RETURN_ERROR: {
                    Toast.makeText(theActivity, "识别失败，图片标签为“error”", Toast.LENGTH_LONG).show();
                    break;
                }
                case TOKENIZER_REQUEST_FAIL: {
                    Toast.makeText(theActivity, "服务器忙，正在重试", Toast.LENGTH_LONG).show();
                    new Thread(theActivity.runnableTokenizer).start();
                    break;
                }
                case TOKENIZER_SUCCESS: {
                    Toast.makeText(theActivity, "识别成功，图片已添加标签", Toast.LENGTH_LONG).show();
                    break;
                }


            }

        }
    }

    MyHandler handler = new MyHandler(this);

    Runnable runnableOCR = new Runnable() {
        @Override
        public void run() {
            Message msg = new Message();
            msg.what = OCR(base64);
            handler.sendMessage(msg);
        }
    };

    Runnable runnableTokenizer = new Runnable() {
        @Override
        public void run() {
            Message msg = new Message();
            msg.what = tokenizer(word);
            handler.sendMessage(msg);
        }
    };

    public void query(String keyword) {
        List<Picture> pictures = mgr.query(keyword);
        ArrayList<Map<String, String>> list = new ArrayList<Map<String, String>>();
        for (Picture picture : pictures) {
            HashMap<String, String> map = new HashMap<String, String>();
            //map.put("tag", picture.tag);
            String imagePath = appHome + "/" + picture.fileName + "_c.jpg";
            map.put("imagePath", imagePath);
            //Log.i("database", picture.tag + ":" + picture.fileName);
            list.add(map);
        }
        if (String.valueOf(list.size()).equals("0")) {
            Toast.makeText(this, "无匹配，请缩短关键词", Toast.LENGTH_LONG).show();
        } else {
            Intent intent = new Intent(this, QueryActivity.class);
            intent.putExtra("list", list);
            startActivityForResult(intent, 2);
        }
    }


    public class QueryDialogFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            LayoutInflater inflater = getActivity().getLayoutInflater();
            final View queryView = inflater.inflate(R.layout.dialog_query, null);
            builder.setView(queryView)
                    .setTitle(R.string.dialog_query)
                    .setPositiveButton(R.string.dialog_search, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            EditText dialog_keyword = (EditText) queryView.findViewById(R.id.dialog_query);
                            String keyword = dialog_keyword.getText().toString();
                            query(keyword);
                        }
                    })
                    .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            QueryDialogFragment.this.getDialog().cancel();
                        }
                    });
            // Create the AlertDialog object and return it
            return builder.create();
        }
    }

    public class TagDialogFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.dialog_tag)
                    .setMessage(tagMessage)
                    .setPositiveButton(R.string.dialog_OK, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            TagDialogFragment.this.getDialog().cancel();
                        }
                    });
            // Create the AlertDialog object and return it
            return builder.create();
        }
    }

//    public int getStatusBarHeight() {
//        int result = 0;
//        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
//        if (resourceId > 0) {
//            result = getResources().getDimensionPixelSize(resourceId);
//        }
//        return result;
//    }

}