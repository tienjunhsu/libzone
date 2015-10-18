package com.cquant.lizone.activity;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import com.cquant.lizone.R;
import com.cquant.lizone.bean.MarketDataItem;
import com.cquant.lizone.frag.NewsFragment;
import com.cquant.lizone.tool.DlgTool;
import com.cquant.lizone.tool.LogTool;
import com.cquant.lizone.tool.StrTool;
import com.cquant.lizone.util.Utils;
import com.cquant.lizone.view.TabLayout;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;

/**
 * Created by PC on 2015/10/9.
 */
public class KPointActivity extends BaseActivity implements RadioGroup.OnCheckedChangeListener{

    private Toolbar toolbar;
    private WebView webview;

    private String url = "http://www.1-yj.com/Kline/index.php/Index/Kline/label/XAGUSD/res/30/";
    //private String url = "http://www.1-yj.com/Kline/index.php/Index/Fenshi/label/XAGUSD/";
    //private String url = "http://finance.sina.com.cn/futures/quotes/AG1512.shtml";

    private MarketDataItem dataItem;

    private TextView mTvPrice;
    private TextView mTvAmp;
    private TextView mTvBuy;
    private TextView mTvSell;
    private TextView mTvHigh;
    private TextView mTvLow;
    private TextView mTvOpen;
    private TextView mTvClose;
    private TextView mTvPercent;

    private double ampDelta = 0.0;
    private String ampPercent = "-.-%";

    //private ScrollView mScrollView;

    private TabLayout mTabLayout;
    private ViewPager viewpager;

    private RadioGroup mRadioMoney;
    private RadioGroup mRadioCycle;
    private FragmentManager fm;
    private FragmentTransaction tx;
    private Fragment fragment1;
    private Fragment fragment2;
    private Fragment fragment3;

    private int mCurSelected = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dataItem = (MarketDataItem)getIntent().getSerializableExtra("marketDataItem");
        setContentView(R.layout.kpoint_activity);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        webview = (WebView)findViewById(R.id.webview);

        //mScrollView = (ScrollView)findViewById(R.id.scrollView);
        mTvPrice = (TextView)findViewById(R.id.tv_price);
        mTvAmp = (TextView)findViewById(R.id.tv_amp);
        mTvBuy = (TextView)findViewById(R.id.tv_buy);
        mTvSell = (TextView)findViewById(R.id.tv_sell);
        mTvHigh = (TextView)findViewById(R.id.tv_high);
        mTvLow = (TextView)findViewById(R.id.tv_low);
        mTvOpen = (TextView)findViewById(R.id.tv_open);
        mTvClose = (TextView)findViewById(R.id.tv_close);
        mTvPercent = (TextView)findViewById(R.id.tv_percent);

        mRadioMoney = (RadioGroup)findViewById(R.id.rg_money);
        mRadioCycle = (RadioGroup)findViewById(R.id.rg_cycle);

        fm = getSupportFragmentManager();
        fragment1 = NewsFragment.newInstance("Article/type/1/");
        fragment2 = NewsFragment.newInstance("Article/type/2/");
        fragment3 = NewsFragment.newInstance("Report/type/"+dataItem.id);
        tx = fm.beginTransaction();
        tx.replace(R.id.id_content,fragment1);
        tx.commit();

        mRadioMoney.setOnCheckedChangeListener(this);
        mRadioCycle.setOnCheckedChangeListener(onCycleChangeListener);


        buildTimeChartUrl();

        initToolBar();
        initWebView();
        webview.loadUrl(url);

        refreshHeadView(dataItem, true);
        //mScrollView.scrollTo(0,0);
    }

    private void refreshHeadView(MarketDataItem dataItem,boolean isInit) {
        calculateAmp(dataItem);
        mTvPrice.setText(dataItem.newprice + "");
        mTvAmp.setText(ampDelta + " " + ampPercent);
        mTvBuy.setText(dataItem.buy+"");
        mTvSell.setText(dataItem.sell+"");
        mTvHigh.setText(dataItem.high + "");
        mTvLow.setText(dataItem.low + "");
        mTvOpen.setText(dataItem.open + "");
        mTvClose.setText(dataItem.close + "");
        mTvPercent.setText(ampPercent);

        setHeadColor(dataItem);

        if(isInit) { //进入时候srollview停留在顶部
            mTvPrice.setFocusable(true);
            mTvPrice.setFocusableInTouchMode(true);
            mTvPrice.requestFocus();
        }
    }

    private void setHeadColor(MarketDataItem dataItem) {
        int mColorRed = getResources().getColor(R.color.red_two);
        int mColorGreen = getResources().getColor(R.color.green_two);
        int mColorWhite = getResources().getColor(R.color.white_two);

        double amp_Delta = StrTool.sub(dataItem.newprice,dataItem.close);

        if(amp_Delta > 0) {
            mTvPrice.setTextColor(mColorRed);
            mTvAmp.setTextColor(mColorRed);
            mTvSell.setTextColor(mColorRed);
            mTvBuy.setTextColor(mColorRed);
            mTvPercent.setTextColor(mColorRed);
        } else if(amp_Delta < 0) {
            mTvPrice.setTextColor(mColorGreen);
            mTvAmp.setTextColor(mColorGreen);
            mTvSell.setTextColor(mColorGreen);
            mTvBuy.setTextColor(mColorGreen);
            mTvPercent.setTextColor(mColorGreen);
        } else {
            mTvPrice.setTextColor(mColorWhite);
            mTvAmp.setTextColor(mColorWhite);
            mTvSell.setTextColor(mColorWhite);
            mTvBuy.setTextColor(mColorWhite);
            mTvPercent.setTextColor(mColorWhite);
        }
        if(dataItem.open > dataItem.close) {
            mTvOpen.setTextColor(mColorRed);
        } else if(dataItem.open < dataItem.close) {
            mTvOpen.setTextColor(mColorGreen);
        } else {
            mTvOpen.setTextColor(mColorWhite);
        }
        if(dataItem.high > dataItem.close) {
            mTvHigh.setTextColor(mColorRed);
        } else if(dataItem.high < dataItem.close) {
            mTvHigh.setTextColor(mColorGreen);
        } else {
            mTvHigh.setTextColor(mColorWhite);
        }
        if(dataItem.low > dataItem.close) {
            mTvLow.setTextColor(mColorRed);
        } else if(dataItem.low < dataItem.close) {
            mTvLow.setTextColor(mColorGreen);
        } else {
            mTvLow.setTextColor(mColorWhite);
        }
    }

    private void calculateAmp(MarketDataItem dataItem) {
        ampDelta = StrTool.sub(dataItem.newprice,dataItem.close);
        ampPercent = String.format("%.2f",ampDelta*100/dataItem.close)+"%";
    }

    private void buildTimeChartUrl() {
        url = "http://www.1-yj.com/Kline/index.php/Index/Fenshi/label/"+dataItem.label+"/";
    }
    private void buildKLineUrl(String cycle) {
        url = "http://www.1-yj.com/Kline/index.php/Index/Kline/label/"+dataItem.label+"/res/"+cycle;
    }
    private void buildBS60Url() {
        url = "http://www.1-yj.com/Kline/index.php/Index/BSline/res/60/label/"+dataItem.label;
    }

    private void initWebView() {
        WebSettings wSettings = webview.getSettings();
        wSettings.setJavaScriptEnabled(true);
        wSettings.setAppCacheEnabled(true);
        wSettings.setDomStorageEnabled(true);
        String appCacheDir = this.getApplicationContext()
                .getDir("cache", Context.MODE_PRIVATE).getPath();
        wSettings.setAppCachePath(appCacheDir);
        wSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        wSettings.setAppCacheMaxSize(1024 * 1024 * 10);
        wSettings.setAllowFileAccess(true);
        wSettings.setDatabaseEnabled(true);
        String databaseDir = this.getApplicationContext()
                .getDir("database", Context.MODE_PRIVATE).getPath();
        wSettings.setDatabasePath(databaseDir);
        wSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        wSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        wSettings.setUseWideViewPort(true);
        wSettings.setLoadWithOverviewMode(true);
        wSettings.setBuiltInZoomControls(true);
        wSettings.setSupportZoom(true);
        wSettings.enableSmoothTransition();
        wSettings.setEnableSmoothTransition(true);

        int screenDensity = getResources().getDisplayMetrics().densityDpi ;
        LogTool.d("screenDensity ="+screenDensity);
        WebSettings.ZoomDensity zoomDensity = WebSettings.ZoomDensity.MEDIUM ;
        if(screenDensity <=DisplayMetrics.DENSITY_MEDIUM) {
            zoomDensity = WebSettings.ZoomDensity.CLOSE;
        }else if(screenDensity <=DisplayMetrics.DENSITY_HIGH) {
            zoomDensity = WebSettings.ZoomDensity.MEDIUM;
        }else {
            zoomDensity = WebSettings.ZoomDensity.FAR;
        }
        wSettings.setDefaultZoom(zoomDensity);

        webview.setBackgroundColor(0x101419);

        //wSettings.setSupportMultipleWindows();
       webview.setWebViewClient(new WebViewClient() {

            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                //startLoadingAnim();
            }

            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                //webview.loadUrl(url);
                return false;
            }

            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // stopLoadingAnim();
            }
        });
    }

    @Override
    protected void initToolBar() {
        toolbar.setTitle(dataItem.name);
        toolbar.setNavigationIcon(R.drawable.ic_back);
        super.initToolBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    public void onXmlBtClick(View v) {
        switch (v.getId()) {
            case R.id.btn_time_chart:
                loadTimeChart();
                break;
            case R.id.btn_d:
                loadBarsChart("D");
                break;
            case R.id.btn_w:
                loadBarsChart("W");
                break;
            case R.id.btn_m:
                loadBarsChart("W");
                break;
            case R.id.btn_bs60:
                loadBSChart();
                popSelectExchange(v);
                break;
            default:
                break;
        }
    }

    private void popSelectExchange(View v) {
        View layout = getLayoutInflater().inflate(R.layout.default_pop_list, null);
        ListView lv = (ListView) layout.findViewById(R.id.lv_selector);
        CycleAdapter adp = new CycleAdapter(this);
        lv.setAdapter(adp);
        lv.setOnItemClickListener(selectMenuOnClickListener);
        DlgTool.showAsDropDown(v, layout, new int[]{0, 0}, true);
    }
    private AdapterView.OnItemClickListener selectMenuOnClickListener = new AdapterView.OnItemClickListener(){

        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                long arg3) {
            if((arg2 >= 0) && (arg2 < cycles.length)) {
                //mViewPager.setCurrentItem(arg2, false);
                mCurSelected = arg2;
            }
           DlgTool.closePopDlg();
        }

    };
    private void loadBarsChart(String d) {
        buildKLineUrl(d);
        webview.loadUrl(url);
    }

    private void loadBSChart() {
        buildBS60Url();
        webview.loadUrl(url);
    }

    private void loadTimeChart() {
        buildTimeChartUrl();
        webview.loadUrl(url);
    }

    public String getItemLabel() {
        return dataItem.label;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.open_firm_account_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
       /* if (id == R.id.action_settings) {
            return true;
        }*/

        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        //android.os.Process.killProcess(android.os.Process.myPid());
    }
    private RadioGroup.OnCheckedChangeListener onCycleChangeListener = new RadioGroup.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(RadioGroup radioGroup, int i) {
            int radioButtonId = radioGroup.getCheckedRadioButtonId();
            switch(radioButtonId) {
                case R.id.btn_time_chart:
                    loadTimeChart();
                    break;
                case R.id.btn_d:
                    loadBarsChart("D");
                    break;
                case R.id.btn_w:
                    loadBarsChart("W");
                    break;
                case R.id.btn_m:
                    loadBarsChart("W");
                    break;
                case R.id.btn_bs60:
                    loadBSChart();
                    break;
                default:
                    break;
            }

        }
    };

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        int radioButtonId = group.getCheckedRadioButtonId();
        RadioButton rb = (RadioButton) group.findViewById(radioButtonId);
        String str = rb.getText().toString();
        switch(radioButtonId) {
            case R.id.money_one:{
                LogTool.d("new select is :"+str);
                fm = getSupportFragmentManager();
                tx = fm.beginTransaction();
                tx.replace(R.id.id_content,fragment1);
                tx.commit();
                break;
            }
            case R.id.money_two:{
                LogTool.d("new select is :"+str);
                fm = getSupportFragmentManager();
                tx = fm.beginTransaction();
                tx.replace(R.id.id_content,fragment2);
                tx.commit();
                break;
            }
            case R.id.money_three:{
                LogTool.d("new select is :"+str);
                fm = getSupportFragmentManager();
                tx = fm.beginTransaction();
                tx.replace(R.id.id_content,fragment3);
                tx.commit();
                break;
            }
        }
    }

    private String[] cycles = new String[]{"BS60","30分","15分","5分"};
    private class CycleAdapter extends BaseAdapter {
        private LayoutInflater inflater;

        public CycleAdapter(Context context) {
            inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return cycles.length;
        }

        @Override
        public Object getItem(int position) {
            return cycles[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            TextView text;
            view= inflater.inflate(R.layout.default_pop_list_item, null, false);
            text = (TextView) view;
            text.setText(cycles[position]);
            if (position == mCurSelected) {
                view.setBackgroundResource(R.color.text_orange);
                text.setTextColor(getResources().getColor(R.color.blue_two));
            }
            return view;
        }
    }

}
