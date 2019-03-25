package com.waracle.androidtest;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.ListFragment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    private static String JSON_URL = "https://gist.githubusercontent.com/hart88/198f29ec5114a3ec3460/" +
            "raw/8dd19a88f9b8d24c23d9960f3300d0c917a4f07c/cake.json";
    static ImageLoader.MemoryCache memoryCache=new ImageLoader.MemoryCache();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static class PlaceholderFragment extends ListFragment {

        private static final String TAG = PlaceholderFragment.class.getSimpleName();

        private ListView mListView;
        private MyAdapter mAdapter;

        public PlaceholderFragment() { /**/ }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            mListView = (ListView) rootView.findViewById(android.R.id.list);
            return rootView;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            // Create and set the list adapter.
            new LongOperation().execute("");
        }

        private class LongOperation extends AsyncTask<String, Void, String> {
            JSONArray array;

            @Override
            protected String doInBackground(String... params) {
                try {
                    array = loadData();

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return "Executed";
            }
            public JSONArray removeDuplicate()
            {
                JSONArray tempArray = new JSONArray();
                List<String> tempList = new ArrayList<>();

                try {
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject obj = array.getJSONObject(i);
                        if(!tempList.contains(obj.get("image").toString())) {
                            tempList.add(obj.get("image").toString());
                            tempArray.put(obj);
                        }
                    }
                }catch (Exception e)
                {

                }
                return tempArray;
            }
            @Override
            protected void onPostExecute(String result) {
                mAdapter = new MyAdapter();
                mListView.setAdapter(mAdapter);
                mAdapter.setItems(removeDuplicate());
            }

            @Override
            protected void onPreExecute() {}

            @Override
            protected void onProgressUpdate(Void... values) {}
        }

         class DownloadImage extends AsyncTask<String, Void, String> {
            ImageLoader mImageLoader;
            private final WeakReference<ImageView> imageViewReference;

            public DownloadImage(ImageView imageView) {
                imageViewReference = new WeakReference<ImageView>(imageView);
            }

            @Override
            protected String doInBackground(String... params) {
                    mImageLoader.load(params[0], imageViewReference);
                return "Executed";
            }

            @Override
            protected void onPostExecute(String param) {
                //mAdapter.notifyDataSetChanged();
            }

            @Override
            protected void onPreExecute() {
                mImageLoader = new ImageLoader((MainActivity) getActivity());
            }

            @Override
            protected void onProgressUpdate(Void... values) {}
        }


        private JSONArray loadData() throws IOException, JSONException {
            URL url = new URL(JSON_URL);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestProperty("Accept", "application/json");
            try {
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());


                byte[] bytes = StreamUtils.readUnknownFully(in);

                String charset = parseCharset(urlConnection.getRequestProperty("Content-Type"));

                String jsonText = new String(bytes, charset);

                return new JSONArray(jsonText);
            } finally {
                urlConnection.disconnect();
            }
        }

        public static String parseCharset(String contentType) {
            if (contentType != null) {
                String[] params = contentType.split(",");
                for (int i = 1; i < params.length; i++) {
                    String[] pair = params[i].trim().split("=");
                    if (pair.length == 2) {
                        if (pair[0].equals("charset")) {
                            return pair[1];
                        }
                    }
                }
            }
            return "UTF-8";
        }

        private class MyAdapter extends BaseAdapter {

            // Can you think of a better way to represent these items???
            private JSONArray mItems;


            public MyAdapter() {
                this(new JSONArray());
            }

            public MyAdapter(JSONArray items) {
                mItems = items;
            }

            @Override
            public int getCount() {
                return mItems.length();
            }

            @Override
            public Object getItem(int position) {
                try {
                    return mItems.getJSONObject(position);
                } catch (JSONException e) {
                    Log.e("", e.getMessage());
                }
                return null;
            }

            @Override
            public long getItemId(int position) {
                return 0;
            }

            @SuppressLint("ViewHolder")
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                LayoutInflater inflater = LayoutInflater.from(getActivity());
                View root = inflater.inflate(R.layout.list_item_layout, parent, false);
                if (root != null) {
                    TextView title = (TextView) root.findViewById(R.id.title);
                    TextView desc = (TextView) root.findViewById(R.id.desc);
                    ImageView image = (ImageView) root.findViewById(R.id.image);
                    try {
                        JSONObject object = (JSONObject) getItem(position);
                            title.setText(object.getString("title"));
                            desc.setText(object.getString("desc"));
                            new DownloadImage(image).execute(object.getString("image"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                return root;
            }

            public void setItems(JSONArray items) {
                mItems = items;
            }
        }
    }

    public static class ImageLoader {

        private static final String TAG = ImageLoader.class.getSimpleName();
        MainActivity mActivity;

        public ImageLoader(MainActivity activity) {
            mActivity = activity;
        }

        public void load(final String url, final WeakReference<ImageView> imageViewReference) {
            if (TextUtils.isEmpty(url)) {
                throw new InvalidParameterException("URL is empty!");
            }

            try {
                new Thread() {
                    public void run() {
                        mActivity.runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                try {
                                    Bitmap bitmap = memoryCache.get(url);
                                    if (bitmap == null) {
                                        bitmap = convertToBitmap(loadImageData(url));
                                        memoryCache.put(url, bitmap);
                                    }
                                    setImageView(imageViewReference.get(), bitmap);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }

                        });
                    }
                }.start();

            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }
        private static byte[] loadImageData(String url) throws IOException {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setInstanceFollowRedirects(false);
            connection.setRequestProperty("Content-Type", "image/jpeg");

            InputStream inputStream = null;

            try {
                try {
                    int status = connection.getResponseCode();
                    if (status != HttpURLConnection.HTTP_OK)
                        if (status == HttpURLConnection.HTTP_MOVED_TEMP
                                || status == HttpURLConnection.HTTP_MOVED_PERM
                                || status == HttpURLConnection.HTTP_SEE_OTHER) {
                            URL secondURL = new URL(connection.getHeaderField("Location"));
                            connection.disconnect();
                            connection = (HttpURLConnection) secondURL.openConnection();
                            connection.setRequestProperty("Content-Type", "image/jpeg");
                        }
                    inputStream = connection.getInputStream();
                } catch (IOException e) {
                    inputStream = connection.getErrorStream();
                }

                return StreamUtils.readUnknownFully(inputStream);
            } finally {
                StreamUtils.close(inputStream);
                connection.disconnect();
            }
        }

        private static Bitmap convertToBitmap(byte[] data) {
            return BitmapFactory.decodeByteArray(data, 0, data.length);
        }
        private static void setImageView(final ImageView imageView, final Bitmap bitmap) {
            if(imageView != null)
                imageView.setImageBitmap(bitmap);
        }

        public void clearCache() {
            memoryCache.clear();
        }

        public static class MemoryCache {
            private Map<String, SoftReference<Bitmap>> cache= Collections.synchronizedMap(new HashMap<String, SoftReference<Bitmap>>());

            public Bitmap get(String id){
                if(!cache.containsKey(id))
                    return null;
                SoftReference<Bitmap> ref=cache.get(id);
                return ref.get();
            }

            public boolean findKey(String id)
            {
                return cache.containsKey(id);
            }
            public void put(String id, Bitmap bitmap){
                cache.put(id, new SoftReference<Bitmap>(bitmap));
            }

            public void clear() {
                cache.clear();
            }
        }
    }

}
