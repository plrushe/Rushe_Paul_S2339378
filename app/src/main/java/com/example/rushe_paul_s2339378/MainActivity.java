/*  Starter project for Mobile Platform Development - 1st diet 25/26
    You should use this project as the starting point for your assignment.
    This project simply reads the data from the required URL and displays the
    raw data in a TextField
*/

//
// Name                 _________________
// Student ID           _________________
// Programme of Study   _________________
//

// UPDATE THE PACKAGE NAME to include your Student Identifier
package com.example.rushe_paul_s2339378;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.EditText;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View.OnClickListener;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements OnClickListener, CurrencyAdapter.OnCurrencyClickListener {
    private TextView rawDataDisplay;
    private MaterialButton startButton;
    private EditText searchInput;
    private RecyclerView currencyRecyclerView;
    private CurrencyAdapter currencyAdapter;
    private String result;
    private String url1="";
    private String urlSource="https://www.fx-exchange.com/gbp/rss.xml";
    private List<Thing> things = new ArrayList<>();
    private final Object downloadLock = new Object();
    private ExecutorService downloadExecutor;
    private ScheduledExecutorService refreshExecutor;
    private Future<?> ongoingDownload;
    private static final long REFRESH_INTERVAL_MINUTES = 15L;
    private boolean autoRefreshStarted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Set up the raw links to the graphical components
        rawDataDisplay = (TextView)findViewById(R.id.rawDataDisplay);
        startButton = (MaterialButton)findViewById(R.id.startButton);
        startButton.setOnClickListener(this);

        searchInput = findViewById(R.id.searchInput);

        currencyRecyclerView = findViewById(R.id.currencyRecyclerView);
        currencyAdapter = new CurrencyAdapter(this);
        currencyRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        currencyRecyclerView.setAdapter(currencyAdapter);

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (currencyAdapter != null) {
                    currencyAdapter.filter(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        // More Code goes here

        startAutoRefresh();

    }

    public void onClick(View aview)
    {
        startProgress();
    }

    @Override
    public void onCurrencyClick(Thing thing) {
        Intent intent = new Intent(this, CurrencyDetailActivity.class);
        intent.putExtra("title", thing.getTitle());
        intent.putExtra("description", thing.getDescription());
        intent.putExtra("pubDate", thing.getPubDate());
        intent.putExtra("currencyCode", thing.getCurrencyCode());
        startActivity(intent);
    }

    public void startProgress()
    {
        submitDownload();
    } //

    private void submitDownload() {
        synchronized (downloadLock) {
            if (ongoingDownload != null && !ongoingDownload.isDone()) {
                return;
            }

            if (downloadExecutor == null || downloadExecutor.isShutdown()) {
                downloadExecutor = Executors.newSingleThreadExecutor();
            }

            ongoingDownload = downloadExecutor.submit(new Task(urlSource));
        }
    }

    private void startAutoRefresh() {
        if (autoRefreshStarted && refreshExecutor != null && !refreshExecutor.isShutdown()) {
            return;
        }
        if (refreshExecutor == null || refreshExecutor.isShutdown()) {
            refreshExecutor = Executors.newSingleThreadScheduledExecutor();
        }
        refreshExecutor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                submitDownload();
            }
        }, 0, REFRESH_INTERVAL_MINUTES, TimeUnit.MINUTES);
        autoRefreshStarted = true;
    }

    private void stopAutoRefresh() {
        if (refreshExecutor != null) {
            refreshExecutor.shutdownNow();
            refreshExecutor = null;
        }
        autoRefreshStarted = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        startAutoRefresh();
    }

    @Override
    protected void onPause() {
        stopAutoRefresh();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        stopAutoRefresh();
        if (downloadExecutor != null) {
            downloadExecutor.shutdownNow();
        }
        super.onDestroy();
    }

    // Need separate thread to access the internet resource over network
    // Other neater solutions should be adopted in later iterations.
    private class Task implements Runnable
    {
        private String url;
        private String feedPubDate = "";

        public Task(String aurl){
            url = aurl;
        }

        private String cleanTitle(String rawTitle) {
            if (rawTitle == null) {
                return "";
            }
            return rawTitle.replaceFirst("^British Pound Sterling\\(GBP\\)/", "").trim();
        }

        private String cleanDescription(String rawDescription) {
            if (rawDescription == null) {
                return "";
            }
            String cleaned = rawDescription.replaceFirst("(?i)^1 british pound sterling = ", "").trim();
            Matcher matcher = Pattern.compile("[-+]?[0-9]*\\.?[0-9]+").matcher(cleaned);
            if (matcher.find()) {
                return matcher.group();
            }
            return cleaned;
        }
        @Override
        public void run(){
            URL aurl;
            URLConnection yc;
            BufferedReader in = null;
            String inputLine = "";

            // Reset the previous results before starting a new download
            result = "";


            Log.d("MyTask","in run");

            try
            {
                Log.d("MyTask","in try");
                aurl = new URL(url);
                yc = aurl.openConnection();
                in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
                while ((inputLine = in.readLine()) != null){
                    result = result + inputLine;
                }
                in.close();
            }
            catch (IOException ae) {
                Log.e("MyTask", "ioexception");
                result = "";
            }

            if (result != null && !result.isEmpty()) {
                //Clean up any leading garbage characters
                int i = result.indexOf("<?"); //initial tag
                if (i >= 0) {
                    result = result.substring(i);
                }

                //Clean up any trailing garbage at the end of the file
                i = result.indexOf("</rss>"); //final tag
                if (i >= 0 && i + 6 <= result.length()) {
                    result = result.substring(0, i + 6);
                }
            } else {
                Log.e("MyTask", "Empty result retrieved; skipping parsing");
            }

            // Now that you have the xml data into result, you can parse it
            try {
                if (result != null && !result.isEmpty()) {
                    XmlPullParserFactory factory =
                            XmlPullParserFactory.newInstance();
                    factory.setNamespaceAware(true);
                    XmlPullParser xpp = factory.newPullParser();
                    xpp.setInput( new StringReader( result ) );

                    List<Thing> parsedThings = new ArrayList<>();
                    Thing currentThing = null;
                    String textValue = "";

                    int eventType = xpp.getEventType();
                    while (eventType != XmlPullParser.END_DOCUMENT) {
                        String tagName = xpp.getName();
                        switch (eventType) {
                            case XmlPullParser.START_TAG:
                                if ("item".equalsIgnoreCase(tagName)) {
                                    currentThing = new Thing();
                                }
                                break;

                            case XmlPullParser.TEXT:
                                textValue = xpp.getText();
                                break;

                            case XmlPullParser.END_TAG:
                                if (currentThing != null) {
                                    if ("item".equalsIgnoreCase(tagName)) {
                                        parsedThings.add(currentThing);
                                        currentThing = null;
                                    } else if ("title".equalsIgnoreCase(tagName)) {
                                        currentThing.setTitle(cleanTitle(textValue));
                                    } else if ("description".equalsIgnoreCase(tagName)) {
                                        currentThing.setDescription(cleanDescription(textValue));
                                    } else if ("pubDate".equalsIgnoreCase(tagName)) {
                                        currentThing.setPubDate(textValue);
                                    }
                                }
                                else if ("pubDate".equalsIgnoreCase(tagName)) {
                                    if (feedPubDate == null || feedPubDate.isEmpty()) {
                                        feedPubDate = textValue;
                                    }
                                }
                                break;
                            default:
                                break;
                        }
                        eventType = xpp.next();
                    }

                    things = parsedThings;
                }


            } catch (XmlPullParserException e) {
                Log.e("Parsing","EXCEPTION" + e);
                //throw new RuntimeException(e);
            } catch (IOException e) {
                Log.e("Parsing","I/O EXCEPTION" + e);
                //throw new RuntimeException(e);
            }


            // Now update the TextView to display parsed data where possible
            String displayText;
            if (!things.isEmpty()) {
                String firstItemPubDate = things.get(0).getPubDate();
                if (firstItemPubDate != null && !firstItemPubDate.isEmpty()) {
                    displayText = "Last updated: " + firstItemPubDate;
                } else if (feedPubDate != null && !feedPubDate.isEmpty()) {
                    displayText = "Last updated: " + feedPubDate;
                } else {
                    displayText = "Last updated date unavailable.";
                }
            } else if (result != null && !result.isEmpty()) {
                displayText = "Data retrieved, but no items could be parsed.";
            } else {
                displayText = "No data retrieved";
            }

            final List<Thing> finalThings = new ArrayList<>(things);
            String finalDisplayText = displayText;
            MainActivity.this.runOnUiThread(new Runnable()
            {
                public void run() {
                    Log.d("UI thread", "I am the UI thread");
                    rawDataDisplay.setText(finalDisplayText);
                    currencyAdapter.updateData(finalThings);
                    if (searchInput != null) {
                        currencyAdapter.filter(searchInput.getText().toString());
                    }
                }
            });
        }

    }

}
