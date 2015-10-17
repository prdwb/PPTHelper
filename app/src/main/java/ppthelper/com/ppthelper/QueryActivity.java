package ppthelper.com.ppthelper;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class QueryActivity extends AppCompatActivity {

    GridView gridView = null;
    //static public String imagePath = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_query);
        gridView = (GridView) findViewById(R.id.gridView);
        Bundle extra = getIntent().getExtras();
        ArrayList<Map<String, String>> list = (ArrayList<Map<String, String>>) extra.get("list");
        SimpleAdapter adapter = new SimpleAdapter(this, list, R.layout.query,
                new String[]{"imagePath"}, new int[]{R.id.file});
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new ItemClickListener());


//        final ImageView img = (ImageView) findViewById(R.id.file);
//        img.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                img.setScaleType(ImageView.ScaleType.FIT_XY);
//            }
//        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_query, menu);
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
}

class ItemClickListener implements AdapterView.OnItemClickListener
{
    //ImageView fullView = null;

    public void onItemClick(AdapterView<?> arg0,//The AdapterView where the click happened
                            View arg1,//The view within the AdapterView that was clicked
                            int arg2,//The position of the view in the adapter
                            long arg3//The row id of the item that was clicked
    ) {
        //在本例中arg2=arg3
        HashMap<String, String> item=(HashMap<String, String>) arg0.getItemAtPosition(arg2);
        //QueryActivity.imagePath = item.get("imagePath");
        Intent intent = new Intent(arg0.getContext(), PictureActivity.class);
        intent.putExtra("imagePath", item.get("imagePath"));
        arg0.getContext().startActivity(intent);

//        fullView = (ImageView) arg0.findViewById(R.id.fullview);
//        fullView.setImageURI(Uri.parse(item.get("imagePath")));
//        fullView.setScaleType(ImageView.ScaleType.FIT_XY);
//        fullView.setVisibility(View.VISIBLE);
    }


}
