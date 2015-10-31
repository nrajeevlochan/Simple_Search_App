package com.simplesearchapp;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.simplesearchapp.dummy.SearchContent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A list fragment representing a list of SearchResults. This fragment
 * also supports tablet devices by allowing list items to be given an
 * 'activated' state upon selection. This helps indicate which item is
 * currently being viewed in a {@link SearchResultDetailFragment}.
 * <p/>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class SearchResultListFragment extends ListFragment {

    private static final String LOG_TAG = "SearchResultListttt";

    /**
     * The serialization (saved instance state) Bundle key representing the
     * activated item position. Only used on tablets.
     */
    private static final String STATE_ACTIVATED_POSITION = "activated_position";

    /**
     * The fragment's current callback object, which is notified of list item
     * clicks.
     */
    private Callbacks mCallbacks = sDummyCallbacks;

    /**
     * The current activated item position. Only used on tablets.
     */
    private int mActivatedPosition = ListView.INVALID_POSITION;

    private DownloadManager downloadManager;
    private long downloadReference;
    private ArrayList<String> mArrayList;
    private long fileDownloadRef = -1;
    private ArrayAdapter mArrayAdapter;

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callbacks {
        /**
         * Callback for when an item has been selected.
         */
        public void onItemSelected(String id);
    }

    /**
     * A dummy implementation of the {@link Callbacks} interface that does
     * nothing. Used only when this fragment is not attached to an activity.
     */
    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onItemSelected(String id) {
        }
    };

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public SearchResultListFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // TODO: replace with a real list adapter.
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //set filter to only when download is complete and register broadcast receiver
        IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        getActivity().registerReceiver(downloadReceiver, filter);
        startNewDwnload("https://itunes.apple.com/search?term=bb+king&limit=20", "JSON");
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Restore the previously serialized activated item position.
        if (savedInstanceState != null
                && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
            setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }

        mCallbacks = (Callbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        // Reset the active callbacks interface to the dummy implementation.
        mCallbacks = sDummyCallbacks;
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);

        // Notify the active callbacks interface (the activity, if the
        // fragment is attached to one) that an item has been selected.
        mCallbacks.onItemSelected(SearchContent.ITEMS.get(position).id);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActivatedPosition != ListView.INVALID_POSITION) {
            // Serialize and persist the activated item position.
            outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
        }
    }

    /**
     * Turns on activate-on-click mode. When this mode is on, list items will be
     * given the 'activated' state when touched.
     */
    public void setActivateOnItemClick(boolean activateOnItemClick) {
        // When setting CHOICE_MODE_SINGLE, ListView will automatically
        // give items the 'activated' state when touched.
        getListView().setChoiceMode(activateOnItemClick
                ? ListView.CHOICE_MODE_SINGLE
                : ListView.CHOICE_MODE_NONE);
    }

    private void setActivatedPosition(int position) {
        if (position == ListView.INVALID_POSITION) {
            getListView().setItemChecked(mActivatedPosition, false);
        } else {
            getListView().setItemChecked(position, true);
        }

        mActivatedPosition = position;
    }

    private void startNewDwnload(String downloadUrl, String type) {
        Log.d(LOG_TAG, "downloadUrl: " + downloadUrl);
        downloadManager = (DownloadManager)getActivity().getSystemService(getActivity().DOWNLOAD_SERVICE);
        Uri Download_Uri = Uri.parse(downloadUrl);
        DownloadManager.Request request = new DownloadManager.Request(Download_Uri);

        //Restrict the types of networks over which this download may proceed.
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
        //Set whether this download may proceed over a roaming connection.
        request.setAllowedOverRoaming(false);
        //Set the title of this download, to be displayed in notifications (if enabled).
        request.setTitle("My Data Download");
        //Set a description of this download, to be displayed in notifications (if enabled)
        request.setDescription("Android Data download using DownloadManager.");
        // we just want to download silently
/*                request.setVisibleInDownloadsUi(false);
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);*/
        //Set the local destination for the downloaded file to a path within the application's external files directory
        mArrayList = new ArrayList<String>();
        if (type.equals("JSON")) {
            request.setDestinationInExternalFilesDir(getActivity(), Environment.DIRECTORY_DOWNLOADS, "CountryList.json");
        } else if (type.equals("IMAGE")) {
            String[] array = downloadUrl.split("/");
            int length = array.length;
            String fileName = array[length-2] + "_" + array[length-1];
            //mArrayList.add(fileName);
            request.setDestinationInExternalFilesDir(getActivity(), Environment.DIRECTORY_DOWNLOADS, fileName);
        }

        //Enqueue a new download and same the referenceId
        downloadReference = downloadManager.enqueue(request);

        if (type.equals("JSON")) {
            fileDownloadRef = downloadReference;
        }
        downloadReference = downloadManager.enqueue(request);
        Log.d(LOG_TAG, "Download Ref ID: " + downloadReference);
    }

    BroadcastReceiver downloadReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            //check if the broadcast message is for our Enqueued download
            long referenceId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            if(fileDownloadRef == referenceId){

                int ch;
                ParcelFileDescriptor file;
                StringBuffer strContent = new StringBuffer("");
                StringBuffer countryData = new StringBuffer("");

                //parse the JSON data and display on the screen
                try {
                    file = downloadManager.openDownloadedFile(downloadReference);
                    FileInputStream fileInputStream
                            = new ParcelFileDescriptor.AutoCloseInputStream(file);

                    while( (ch = fileInputStream.read()) != -1)
                        strContent.append((char)ch);

                    JSONObject responseObj = new JSONObject(strContent.toString());
                    JSONArray resultsArrayObj = responseObj.getJSONArray("results");

                    Log.d(LOG_TAG, "length of array: " + resultsArrayObj.length());
                    getActivity().unregisterReceiver(downloadReceiver);
                    //list = new ArrayList<>();
                    for (int i=0; i<resultsArrayObj.length(); i++){
                        String imageUrl = resultsArrayObj.getJSONObject(i).getString("artworkUrl100");
                        String[] array = imageUrl.split("/");
                        int length = array.length;
                        String fileName = array[length-2] + "_" + array[length-1];
                        SearchContent.SearchItem item = new SearchContent.SearchItem(String.valueOf(i),
                                        resultsArrayObj.getJSONObject(i).getString("trackCensoredName"),
                                        resultsArrayObj.getJSONObject(i).getString("trackCensoredName"),
                                                fileName);

                        Log.d(LOG_TAG, "Item Name: " + item.name);
                        Log.d(LOG_TAG, "Item Desc: " + item.description);
                        //Gson gson = new Gson();
                        //String countryInfo = resultsArrayObj.getJSONObject(i).toString();
                        //Country country = gson.fromJson(countryInfo, Country.class);
                        //countryData.append(country.getCode() + ": " + country.getName() +"\n");
                        //Log.d("DownloadJsonActivityyyy", "length of sybarray: " + resultsArrayObj.getJSONObject(i).getString("trackCensoredName"));
                        SearchContent.addItem(item);
                        startNewDwnload(imageUrl, "IMAGE");
                    }
                    //mArrayAdapter.notifyDataSetChanged();
                    mArrayAdapter = new CustomAdapter(
                            getActivity(),
                            R.layout.single_row_layout,
                            R.id.textView,
                            SearchContent.ITEMS);
                    setListAdapter(mArrayAdapter);

                    Toast toast = Toast.makeText(getActivity(),
                            "Downloading of data just finished", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.TOP, 25, 400);
                    toast.show();

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }
    };

    class CustomAdapter extends ArrayAdapter<SearchContent.SearchItem> {
        TextView largetextView;
        TextView smalltextView;
        ImageView imageView;
        Context context;
        ArrayList<SearchContent.SearchItem> list;

        public CustomAdapter(Context context, int resource, int textViewResourceId, ArrayList<SearchContent.SearchItem> objects) {
            super(context, resource, textViewResourceId, objects);
            this.context = context;
            this.list = objects;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View row = inflater.inflate(R.layout.single_row_layout, parent, false);

            largetextView = (TextView) row.findViewById(R.id.textView);
            smalltextView = (TextView) row.findViewById(R.id.textView2);
            imageView = (ImageView) row.findViewById(R.id.imageView);

            Log.d(LOG_TAG, "Name: " + list.get(position).name);
            Log.d(LOG_TAG, "Desc: " + list.get(position).description);

            largetextView.setText(list.get(position).name);
            smalltextView.setText(list.get(position).description);
            File imgfile = new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) + "/" + list.get(position).imageurl);
            Log.d(LOG_TAG, "Images: " + imgfile.exists());
            Log.d(LOG_TAG, "Images exists: " + imgfile.getAbsolutePath());
                    Bitmap bitmap = BitmapFactory.decodeFile(
                    new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) + "/" + list.get(position).imageurl).getAbsolutePath());
            Log.d(LOG_TAG, "BitMaps: " + bitmap);
            imageView.setImageBitmap(bitmap);
            //imageView.setImageURI();

            return row;
        }
    }
}
