package ppthelper.com.ppthelper;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

public class PictureActivity extends AppCompatActivity {
    ImageView fullView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);//不显示标题栏

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture);
        Bundle extra = getIntent().getExtras();
        String imagePath = extra.getString("imagePath");
        fullView = (ImageView)findViewById(R.id.fullView);
//        fullView.setImageURI(Uri.parse(imagePath));
//        fullView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        Bitmap bm = BitmapFactory.decodeFile(imagePath);
        fullView.setImageBitmap(big(bm));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_picture, menu);
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

    public void goBack(View v){
        this.finish();
    }

    public Bitmap big(Bitmap bitmap) {  				//修改bitmap大小
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int screenWidth = dm.widthPixels;				//获取当前屏幕宽度
        int screenHeight = dm.heightPixels;				//获取当前屏幕高度
        float w = (float) screenWidth / bitmap.getWidth();	//计算当前图片要全屏幕，宽度需要放大尺寸
        float h = (float) screenHeight / bitmap.getHeight();//计算当前图片要全屏，高度需要放大尺寸
        if (w >= h)//选取较小尺寸进行放大
            w = h;
        Matrix matrix = new Matrix();
        matrix.postScale(w, w);
        Bitmap resizeBmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                bitmap.getHeight(), matrix, true);
        return resizeBmp;
    }
}
