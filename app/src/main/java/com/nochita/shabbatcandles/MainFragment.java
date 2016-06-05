package com.nochita.shabbatcandles;

import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class MainFragment extends Fragment implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    public static final String TAG = "light candle";
    private TextView lightCandleForTextView;
    private TextView timeTextView;
    private WeakReference<MyAsyncTask> asyncTaskWeakRef;
    private GoogleApiClient mGoogleApiClient;

    public MainFragment() {
        // Required empty public constructor
    }

    public static MainFragment newInstance() {
        MainFragment fragment = new MainFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
        setRetainInstance(true);
        startNewAsyncTask();

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_about:
                showAboutDialog();
                return true;
            case R.id.menu_settings:
//                getActivity().startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showAboutDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
        alertDialog.setTitle(getActivity().getString(R.string.about));
        alertDialog.setMessage(getActivity().getString(R.string.about_message));
        alertDialog.show();
    }

    private void startNewAsyncTask() {
        MyAsyncTask asyncTask = new MyAsyncTask(this);
        this.asyncTaskWeakRef = new WeakReference<MyAsyncTask >(asyncTask );
        asyncTask.execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        lightCandleForTextView = (TextView) view.findViewById(R.id.main_light_candle_for);
        timeTextView = (TextView) view.findViewById(R.id.main_time);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            Log.d(TAG, mLastLocation.getLatitude() + "");
            Log.d(TAG, mLastLocation.getLongitude() + "");
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed");
        Toast.makeText(getActivity(), R.string.check_google_play_services, Toast.LENGTH_LONG).show();
    }

    private static class MyAsyncTask extends AsyncTask<Void, Void, Void> {

        private WeakReference<MainFragment> fragmentWeakRef;
        private String status;
        private Calendar calLightCandle;

        private MyAsyncTask (MainFragment fragment) {
            this.fragmentWeakRef = new WeakReference<MainFragment>(fragment);
        }

        @Override
        protected Void doInBackground(Void... params) {

            Calendar myDate = Calendar.getInstance(); // set this up however you need it.
            int dayOfWeek = myDate.get (Calendar.DAY_OF_WEEK);
            Log.d(TAG, "day of week " + dayOfWeek);

            if(dayOfWeek < Calendar.FRIDAY) {
                // add the difference to friday to get the next friday
                myDate.add(Calendar.DATE, Calendar.FRIDAY -  dayOfWeek);
            } else if(dayOfWeek > Calendar.FRIDAY) {
                // if its saturday (7), add 7 to get th enext friday
                myDate.add(Calendar.DATE, 7);
            }

            SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
            String dateToUrl = format1.format(myDate.getTime());

            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url("http://api.sunrise-sunset.org/json?lat=-34.611953&lng=-58.441708&" +
                            "&date=" +  dateToUrl + "&formatted=0")
                    .build();

            Response response = null;
            try {
                response = client.newCall(request).execute();
                String jsonData = response.body().string();
                Log.d(TAG, jsonData);

                JSONObject jsonObject = new JSONObject(jsonData);
                status = jsonObject.getString("status");
                Log.i(TAG, "status " + status);

                JSONObject jsonResult = jsonObject.getJSONObject("results");
                String ligh_time = jsonResult.getString("sunset");
                Log.i(TAG, "ligh_time " + ligh_time);

                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss+00:00");
                calLightCandle  = Calendar.getInstance();
                calLightCandle.setTime(df.parse(ligh_time));
                calLightCandle.add(Calendar.HOUR, -3); // time zone
                calLightCandle.add(Calendar.MINUTE, -18); // -18 minutes

            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e){
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void response) {
            super.onPostExecute(response);
            if (this.fragmentWeakRef.get() != null) {
                SimpleDateFormat dfTime = new SimpleDateFormat("HH:mm:ss");
                String time = dfTime.format(calLightCandle.getTime());
                this.fragmentWeakRef.get().timeTextView.setText(time);

                SimpleDateFormat dfDay = new SimpleDateFormat("MMMM d, yyyy");
                String day = dfDay.format(calLightCandle.getTime());
                String text = this.fragmentWeakRef.get().getString(R.string.light_candle_for, day);
                this.fragmentWeakRef.get().lightCandleForTextView.setText(text);
            }
        }
    }
}
