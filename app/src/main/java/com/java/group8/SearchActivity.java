package com.java.group8;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by xzwkl on 17/9/7.
 */

public class SearchActivity extends AppCompatActivity {

    private LinearLayout mGallery_commend;
    private LinearLayout mGallery_history;
    private AsyncImageLoader asyncImageLoader;
    private ArrayList<News> mCommend;
    private ArrayList<String> mHistory;
    private MyReceiver_search receiver;
    private LayoutInflater mInflater;
    private SearchActivityOnClickListener saocl;

    @Override
    protected void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        setContentView(R.layout.activity_search);
        getDelegate().setLocalNightMode(((MyApplication) getApplicationContext()).getNightMode());

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mCommend = new ArrayList<News>();
        mHistory = new ArrayList<String>();
        mGallery_commend = (LinearLayout) findViewById(R.id.commendLayout);
        mGallery_history = (LinearLayout) findViewById(R.id.historyLayout);

        asyncImageLoader = new AsyncImageLoader();

        saocl = new SearchActivityOnClickListener(this);

        TextView cancel = (TextView) findViewById(R.id.cancel);
        cancel.setOnClickListener(saocl);

        TextView submit = (TextView) findViewById(R.id.submit);
        submit.setOnClickListener(saocl);

        EditText searchInput = (EditText) findViewById(R.id.searchInput);
        searchInput.setText("请输入搜索内容");
        searchInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v,boolean hasFocus)
            {
                EditText _v = (EditText)v;
                if(!hasFocus)
                {
                    _v.setText(_v.getText());
                }
                else
                {
                    //String hint = _v.getHint().toString();
                    //_v.setTag(hint);
                    _v.setText("");
                }
            }
        });
        searchInput.selectAll();

        receiver = new MyReceiver_search();
        IntentFilter filter = new IntentFilter();
        filter.addAction(NewsService.RECOMMEDACTION);
        SearchActivity.this.registerReceiver(receiver, filter);
        //filter.addAction(NewsService.H);

        mInflater = LayoutInflater.from(this);
        initData();
        //initView();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    @Override
    public void onResume() {
        super.onResume();
        getDelegate().setLocalNightMode(((MyApplication) getApplicationContext()).getNightMode());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish(); // back button
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initData() {
        //init commend command
        Intent intent_commend = new Intent(this, NewsService.class);
        String key = NewsService.KEY;
        String value = NewsService.RECOMMEND;
        String para1 = NewsService.NEWSKEYWORD;
        MyApplication ma = (MyApplication) getApplicationContext();
        String news_ID = ma.getLastIndex();
        String para2 = NewsService.SERVICEKIND;
        String servicekind = NewsService.RECOMMEND;
        intent_commend.putExtra(key, value);
        intent_commend.putExtra(para1, news_ID);
        intent_commend.putExtra(para2, servicekind);
        startService(intent_commend);

        //init history command
        Intent intent_history = new Intent(this, NewsService.class);
        key = NewsService.KEY;
        value = NewsService.HISTORY;
        para1 = NewsService.SERVICEKIND;
        servicekind = NewsService.HISTORY;
        intent_history.putExtra(key, value);
        intent_history.putExtra(para1, servicekind);
        startService(intent_history);
    }

    public class MyReceiver_search extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();

            Log.d("hahah", "dahofa");
            String servicekind = bundle.getString(NewsService.SERVICEKIND);
            if (servicekind.equals(NewsService.RECOMMEND)) {
                mCommend.clear();
                ArrayList<News> tmp = (ArrayList<News>) bundle.getSerializable(NewsService.NEWSLIST);
                if(tmp == null)
                    mCommend = null;
                else {
                    mCommend.addAll(tmp);
                    // download image from web
                    int count = 0;
                    for(News i : mCommend) {
                        count++;
                        if (count > 10) break;
                        String imageUrl = i.news_Pictures.split(";")[0];
                        View view = mInflater.inflate(R.layout.horizonal_display_search, mGallery_commend, false);
                        view.setOnClickListener(saocl);
                        ImageView img = (ImageView) view.findViewById(R.id.image_search);
                        TextView text = (TextView) view.findViewById(R.id.text_search);
                        text.setText(i.news_Title);
                        img.setTag(imageUrl);
                        //img.setImageResource(R.drawable.icon_3);

                        if (!imageUrl.equals("")) {
                            Drawable cachedImage = null;
                            try {
                                cachedImage = asyncImageLoader.loadDrawable(imageUrl, new AsyncImageLoader.ImageCallback() {
                                    public void imageLoaded(Drawable imageDrawable, String imageUrl) {
                                        ImageView imageViewByTag = mGallery_commend.findViewWithTag(imageUrl);
                                        if (imageViewByTag != null) {
                                            imageViewByTag.setImageDrawable(imageDrawable);
                                        }
                                    }
                                });
                            } catch (Exception e) {
                                Log.d("Pic Exception", e.getMessage());
                            } finally {
                                if (cachedImage == null) {
                                    img.setImageResource(R.drawable.ic_desert);
                                } else {
                                    img.setImageDrawable(cachedImage);
                                }
                            }
                        } else {
                            //                Log.d("PIC AT "+position, "empty");
                            img.setImageResource(R.drawable.ic_desert);
                        }
                        mGallery_commend.addView(view);
                    }
                }
            }
            else if(servicekind.equals(NewsService.HISTORY)) {
                mHistory.clear();
                ArrayList<String> tmp = (ArrayList<String>) bundle.getStringArrayList(NewsService.HISTORYLIST);
                if(tmp == null)
                    mHistory = null;
                else {
                    mHistory.addAll(tmp);
                    // download image from web
                    int count = 0;
                    for(String i : mHistory) {
                        count++;
                        if (count > 10) break;
                        View view = mInflater.inflate(R.layout.horizontal_display_history, mGallery_history, false);
                        view.setOnClickListener(saocl);
                        ImageView img = (ImageView) view.findViewById(R.id.image_history);
                        TextView text = (TextView) view.findViewById(R.id.text_history);
                        text.setText(i);
                        img.setImageResource(R.drawable.ic_desert);
                        mGallery_history.addView(view);
                    }
                }
            }
        }
    }


    public class SearchActivityOnClickListener implements View.OnClickListener {
        Activity current_activity;

        public SearchActivityOnClickListener(Activity a) {
            current_activity = a;
        }

        public void onClick(View view) {
            if (view.getId() == R.id.cancel) {
                //Intent i = new Intent(current_activity, MainActivity.class);
                //startActivity(i);
                EditText ev = (EditText) findViewById(R.id.searchInput);
                //ev.setFocusable(false);
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(ev.getWindowToken(), 0);
                ev.setFocusable(true);
            } else if (view.getId() == R.id.submit) {
                Intent i = new Intent(current_activity, ResultActivity.class);
                EditText e = (EditText) findViewById(R.id.searchInput);
                i.putExtra("input", e.getText().toString());
                startActivity(i);
            } else if (view.getId() == R.id.layout_search) {
                Intent i = new Intent(current_activity, NewsPageActivity.class);
                TextView text = (TextView) view.findViewById(R.id.text_search);
                //Log.d("string", text.getText().toString());
                String title = text.getText().toString();
                News tar = null;
                for(News tmp : mCommend) {
                    if(tmp.news_Title.equals(title)) {
                        tar = tmp;
                        break;
                    }
                }
                if(tar != null) i.putExtra("news_ID", tar.news_ID);
                startActivity(i);
            } else if(view.getId() == R.id.layout_history) {
                Intent i = new Intent(current_activity, ResultActivity.class);
                TextView text = (TextView) view.findViewById(R.id.text_history);
                String title = text.getText().toString();
                i.putExtra("input", title);
                startActivity(i);
            } else if (view.getId() == R.id.searchInput) {
                EditText input = (EditText) view;
                //input.setText("");
            }
        }
    }
}


