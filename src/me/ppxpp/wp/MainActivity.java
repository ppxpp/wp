package me.ppxpp.wp;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import me.ppxpp.widget.MHorizontalScrollView;
import me.ppxpp.widget.MainContainer;

public class MainActivity extends Activity{

    ViewGroup container1, container2;
    MainContainer mainContainer;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
        mainContainer = (MainContainer) findViewById(R.id.container);

        container1 = (ViewGroup) findViewById(R.id.first);
        container2 = (ViewGroup)findViewById(R.id.second);


    }

    public void buttonClick(View view){
        Log.d("", "");
    }

}
