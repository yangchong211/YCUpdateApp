package com.ycbjie.update;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.ycbjie.ycupdatelib.dialog.BaseDialogFragment;
import com.ycbjie.ycupdatelib.dialog.UpdateFragment;
import com.ycbjie.ycupdatelib.download.help.DownloadHelper;

import java.io.File;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.tv_1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateFragment updateFragment = new UpdateFragment(false);
                updateFragment.show(getSupportFragmentManager());
                updateFragment.setLoadFinishListener(new BaseDialogFragment.onLoadFinishListener() {
                    @Override
                    public void listener(boolean isSuccess) {

                    }
                });
            }
        });

        findViewById(R.id.tv_2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File dlFile = DownloadHelper.getInstance().getDlFile();
                if (dlFile!=null && dlFile.exists()){
                    dlFile.delete();
                    Toast.makeText(MainActivity.this,"删除成功",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }



}
