package enhabyto.com.viewzapps;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.Calendar;

/**
 * Created by this on 08-Jan-18.
 */

public class CheckNetworkConnection extends AsyncTask<Void, Void, Boolean> {
    private OnConnectionCallback onConnectionCallback;
    @SuppressLint("StaticFieldLeak")
    private Context context;

    public CheckNetworkConnection(Context con, OnConnectionCallback onConnectionCallback) {
        super();
        this.onConnectionCallback = onConnectionCallback;
        this.context = con;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        if (context == null)
            return false;

        boolean isConnected = new NetWorkInfoUtility().isNetWorkAvailableNow(context);
        return isConnected;
    }

    @Override
    protected void onPostExecute(Boolean b) {
        super.onPostExecute(b);

        if (b) {
            onConnectionCallback.onConnectionSuccess();
        } else {
            String msg = "No Internet Connection";
            if (context == null)
                msg = "Context is null";
            onConnectionCallback.onConnectionFail(msg);
        }

    }

    public interface OnConnectionCallback {
        void onConnectionSuccess();

        void onConnectionFail(String errorMsg);
    }
}




class NetWorkInfoUtility {

    public boolean isWifiEnable() {
        return isWifiEnable;
    }

    public void setIsWifiEnable(boolean isWifiEnable) {
        this.isWifiEnable = isWifiEnable;
    }

    public boolean isMobileNetworkAvailable() {
        return isMobileNetworkAvailable;
    }

    public void setIsMobileNetworkAvailable(boolean isMobileNetworkAvailable) {
        this.isMobileNetworkAvailable = isMobileNetworkAvailable;
    }

    private boolean isWifiEnable = false;
    private boolean isMobileNetworkAvailable = false;

    public boolean isNetWorkAvailableNow(Context context) {
        boolean isNetworkAvailable = false;

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            setIsWifiEnable(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected());
        }
        if (connectivityManager != null) {
            setIsMobileNetworkAvailable(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnected());
        }

        if (isWifiEnable() || isMobileNetworkAvailable()) {
        /*Sometime wifi is connected but service provider never connected to internet
        so cross check one more time*/
            if (isOnline())
                isNetworkAvailable = true;
        }

        return isNetworkAvailable;
    }

    public boolean isOnline() {
    /*Just to check Time delay*/
        long t = Calendar.getInstance().getTimeInMillis();

        Runtime runtime = Runtime.getRuntime();
        try {
        /*Pinging to Google server*/
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int exitValue = ipProcess.waitFor();
            return (exitValue == 0);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } /*finally {
            long t2 = Calendar.getInstance().getTimeInMillis();
            Log.i("NetWork check Time", (t2 - t) + "");
        }*/
        return false;
    }
}