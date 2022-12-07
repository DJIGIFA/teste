package com.isp_bamako;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.GeolocationPermissions;
import android.webkit.SslErrorHandler;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private SwipeRefreshLayout swipeContainer;
    private ProgressBar progressBar;

    private ValueCallback<Uri> mUploadMessage;
    public ValueCallback<Uri[]> uploadMessage;
    public static final int REQUEST_SELECT_FILE = 100;
    private final static int FILECHOOSER_RESULTCODE = 1;

    private View mCustomView;
    private WebChromeClient.CustomViewCallback mCustomViewCallback;

    private final String URL_BASE = "https://mobile.sodishop.com/admin";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initWebView();

        setupWebView();
        setupboutique_swipeContainer();
    }

    private void initView()
    {
        webView =  findViewById(R.id.webview);
        swipeContainer = findViewById(R.id.swipeContainer);

        progressBar = (ProgressBar) findViewById(R.id.boutique_progressBar);

        progressBar.setMax(100);
    }

    private void initWebView()
    {
        webView.loadUrl(URL_BASE);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
    }

    private void setupboutique_swipeContainer() {
        swipeContainer.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    final public void onRefresh() {
                        webView.reload();
                        swipeContainer.setRefreshing(false);
                    }
                }
        );

    }

    private void setupWebView() {


        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDescription,
                                        String mimetype, long contentLength) {

                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                request.allowScanningByMediaScanner();
                request.setNotificationVisibility(
                        DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

                String fileName = URLUtil.guessFileName(url,contentDescription,mimetype);

                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,fileName);

                DownloadManager dManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);

                dManager.enqueue(request);

                //Toast.makeText(getApplicationContext(), "Telechgargement en cour", Toast.LENGTH_SHORT).show();
            }
        });

        webView.setWebViewClient(new WebViewClient()
        {
            public void onReceivedError(WebView boutique_vebview, int i, String s, String d1)
            {
                //Toasty.error(getApplicationContext(),"No Internet Connection!").show();
                boutique_vebview.loadUrl("file:///android_asset/net_error.html");
                //Toast.makeText(getApplicationContext(),"s = "+s+" i = "+i,Toast.LENGTH_LONG).show();
                //Log.e("tmp","s = "+s+" i = "+i);

                //tmp: s = net::ERR_INTERNET_DISCONNECTED i = -2
                //s = net::ERR_CONNECTION_ABORTED i = -6




            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, final String url) {
                super.onPageFinished(view, url);
                //boolean d = false;
                //if(d==false){
                // boutique_netscroll.scrollTo(0,0);
                //   d=true;
                // }
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);

                ////Toast.makeText(ActivityBoutique.this,error.toString(),Toast.LENGTH_LONG).show();
            }

            @Override
            public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
                super.onReceivedHttpError(view, request, errorResponse);
                //boutique_vebview.goBack();
                // //Toast.makeText(ActivityBoutique.this,errorResponse.toString(),Toast.LENGTH_LONG).show();
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                super.onReceivedSslError(view, handler, error);
            }



            /*******************/
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                //opening link external browser
                /*if(!url.contains("android_asset")){
                    view.setWebViewClient(null);
                } else {
                    view.setWebViewClient(new WebViewClient());
                }*/
                //Log.e("webView boutique",url);
                if(url.contains("youtube.com") || url.contains("play.google.com") || url.contains("google.com/maps") || url.contains("facebook.com") || url.contains("twitter.com") || url.contains("instagram.com")){
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                    return true;
                }
                else if(url.startsWith("mailto:")){
                    myHandleMailToLink(url);
                    return true;
                }
                else if(url.startsWith("tel:")){
                    startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse(url)));
                    return true;
                }
                else if(url.startsWith("sms:")){
                    // Handle the sms: link
                    handleSMSLink(url);

                    // Return true means, leave the current web view and handle the url itself
                    return true;
                }
                else if(url.contains("geo:")) {
                    Uri gmmIntentUri = Uri.parse(url);
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    if (mapIntent.resolveActivity(getPackageManager()) != null) {
                        startActivity(mapIntent);
                    }
                    return true;
                }

                view.loadUrl(url);
                return true;
            }

        });

        webView.setWebChromeClient(new WebChromeClient(){

            public Bitmap getDefaultVideoPoster()
            {
                if (mCustomView == null) {
                    return null;
                }
                return BitmapFactory.decodeResource(getApplicationContext().getResources(), 2130837573);
            }

            // For 3.0+ Devices (Start)
            // onActivityResult attached before constructor
            final protected void openFileChooser(ValueCallback uploadMsg, String acceptType)
            {
                mUploadMessage = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("image/*");
                startActivityForResult(Intent.createChooser(i, "File Browser"), FILECHOOSER_RESULTCODE);
            }


            // For Lollipop 5.0+ Devices
            public boolean onShowFileChooser(WebView boutique_vebview, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams)
            {
                if (uploadMessage != null) {
                    uploadMessage.onReceiveValue(null);
                    uploadMessage = null;
                }

                uploadMessage = filePathCallback;

                Intent intent = fileChooserParams.createIntent();
                try
                {
                    startActivityForResult(intent, REQUEST_SELECT_FILE);
                } catch (ActivityNotFoundException e)
                {
                    uploadMessage = null;
                    //Toast.makeText(getApplicationContext() , "Cannot Open File Chooser", Toast.LENGTH_LONG).show();
                    //eski---- >   //Toast.makeText(getApplicationContext(), "Cannot Open File Chooser", Toast.LENGTH_LONG).show();
                    return false;
                }
                return true;
            }

            //For Android 4.1 only
            protected void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture)
            {
                mUploadMessage = uploadMsg;
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "File Browser"), FILECHOOSER_RESULTCODE);
            }

            protected void openFileChooser(ValueCallback<Uri> uploadMsg)
            {
                mUploadMessage = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("image/*");
                startActivityForResult(Intent.createChooser(i, "File Chooser"), FILECHOOSER_RESULTCODE);
            }


            public void onProgressChanged(WebView view, int newProgress){
                progressBar.setProgress(newProgress);
                if(newProgress == 100){
                    progressBar.setVisibility(View.GONE);
                }else
                {
                    progressBar.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onGeolocationPermissionsShowPrompt(String origin,
                                                           GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, false);
            }
        });

        WebSettings webSettings = webView.getSettings();

        webSettings.setDomStorageEnabled(true);
        webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        webSettings.getSaveFormData();
        webSettings.setDisplayZoomControls(false);
        webSettings.setUseWideViewPort(true);
        //webSettings.setAppCacheEnabled(false);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setSavePassword(true);
        // webSettings.setSupportMultipleWindows(true); //?a href problem
        webSettings.getJavaScriptEnabled();
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webSettings.setGeolocationEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setLoadsImagesAutomatically(true);
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        // boutique_vebview.getSettings().setJavaScriptCanOpenWindowsAutomatically(false);
        //  webSettings.setJavaScriptCanOpenWindowsAutomatically(false); //(popup)

    }


    protected void handleSMSLink(String url){
        /*
            If you want to ensure that your intent is handled only by a text messaging app (and not
            other email or social apps), then use the ACTION_SENDTO action
            and include the "smsto:" data scheme
        */

        // Initialize a new intent to send sms message
        Intent intent = new Intent(Intent.ACTION_SENDTO);

        // Extract the phoneNumber from sms url
        String phoneNumber = url.split("[:?]")[1];

        if(!TextUtils.isEmpty(phoneNumber)){
            // Set intent data
            // This ensures only SMS apps respond
            intent.setData(Uri.parse("smsto:" + phoneNumber));

            // Alternate data scheme
            //intent.setData(Uri.parse("sms:" + phoneNumber));
        }else {
            // If the sms link built without phone number
            intent.setData(Uri.parse("smsto:"));

            // Alternate data scheme
            //intent.setData(Uri.parse("sms:" + phoneNumber));
        }


        // Extract the sms body from sms url
        if(url.contains("body=")){
            String smsBody = url.split("body=")[1];

            // Encode the sms body
            try{
                smsBody = URLDecoder.decode(smsBody,"UTF-8");
            }catch (UnsupportedEncodingException e){
                e.printStackTrace();
            }

            if(!TextUtils.isEmpty(smsBody)){
                // Set intent body
                intent.putExtra("sms_body",smsBody);
            }
        }

        if(intent.resolveActivity(getPackageManager())!=null){
            // Start the sms app
            startActivity(intent);
        }else {
            //Toast.makeText(getApplicationContext(),"No SMS app found.",Toast.LENGTH_SHORT).show();
        }
    }

    // Custom method to handle web view mailto link
    protected void handleMailToLink(String url){
        // Initialize a new intent which action is send
        Intent intent = new Intent(Intent.ACTION_SENDTO);

        //Toast.makeText(ActivityBoutique.this,url,Toast.LENGTH_LONG).show();

        // For only email app handle this intent
        intent.setData(Uri.parse("mailto"));

        String mString="";
        // Extract the email address from mailto url
        String to = url.split("[:?]")[1];
        if(!TextUtils.isEmpty(to)){
            String[] toArray = to.split(";");
            // Put the primary email addresses array into intent
            intent.putExtra(Intent.EXTRA_EMAIL,toArray);
            mString += ("TO : " + to);
        }

        // Extract the cc
        if(url.contains("cc=")){
            String cc = url.split("cc=")[1];
            if(!TextUtils.isEmpty(cc)){
                //cc = cc.split("&")[0];
                cc = cc.split("&")[0];
                String[] ccArray = cc.split(";");
                // Put the cc email addresses array into intent
                intent.putExtra(Intent.EXTRA_CC,ccArray);
                mString += ("\nCC : " + cc );
            }
        }else {
            mString += ("\n" + "No CC");
        }

        // Extract the bcc
        if(url.contains("bcc=")){
            String bcc = url.split("bcc=")[1];
            if(!TextUtils.isEmpty(bcc)){
                //cc = cc.split("&")[0];
                bcc = bcc.split("&")[0];
                String[] bccArray = bcc.split(";");
                // Put the bcc email addresses array into intent
                intent.putExtra(Intent.EXTRA_BCC,bccArray);
                mString += ("\nBCC : " + bcc );
            }
        }else {
            mString+=("\n" + "No BCC");
        }

        // Extract the subject
        if(url.contains("subject=")){
            String subject = url.split("subject=")[1];
            if(!TextUtils.isEmpty(subject)){
                subject = subject.split("&")[0];
                // Encode the subject
                try{
                    subject = URLDecoder.decode(subject,"UTF-8");
                }catch (UnsupportedEncodingException e){
                    e.printStackTrace();
                }
                // Put the mail subject into intent
                intent.putExtra(Intent.EXTRA_SUBJECT,subject);
                mString+=("\nSUBJECT : " + subject );
            }
        }else {
            mString+=("\n" + "No SUBJECT");
        }

        // Extract the body
        if(url.contains("body=")){
            String body = url.split("body=")[1];
            if(!TextUtils.isEmpty(body)){
                body = body.split("&")[0];
                // Encode the body text
                try{
                    body = URLDecoder.decode(body,"UTF-8");
                }catch (UnsupportedEncodingException e){
                    e.printStackTrace();
                }
                // Put the mail body into intent
                intent.putExtra(Intent.EXTRA_TEXT,body);
                mString+=("\nBODY : " + body );
            }
        }else {
            mString+=("\n" + "No BODY");
        }
        //startActivity(intent);
        // Email address not null or empty
        if(!TextUtils.isEmpty(to)){
            if(intent.resolveActivity(getPackageManager())!=null){
                // Finally, open the mail client activity
                startActivity(intent);
            }else {
                Toast.makeText(getApplicationContext(),"No email client found.",Toast.LENGTH_SHORT).show();
            }
        }

    }

    protected void myHandleMailToLink(String url)
    {
        Uri uri = Uri.parse(url);

        Intent sendTo = new Intent(Intent.ACTION_SENDTO);
        sendTo.setData(uri);
        startActivity(sendTo);
    }

    @Override
    public void onBackPressed() {

        if(webView.canGoBack())
        {
            webView.goBack();
        }else
        {
            super.onBackPressed();
        }

    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            return true;
        }
        return super.onKeyLongPress(keyCode, event);
    }

    @Override
    protected void onStop() {
        super.onStop();
        webView.clearCache(true);
        webView.clearHistory();
    }
}