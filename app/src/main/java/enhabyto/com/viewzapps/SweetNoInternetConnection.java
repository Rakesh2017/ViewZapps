package enhabyto.com.viewzapps;

import android.content.Context;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * Created by this on 08-Jan-18.
 */

public class SweetNoInternetConnection {

    public void noInternet(Context context){

        new SweetAlertDialog(context, SweetAlertDialog.CUSTOM_IMAGE_TYPE)
                .setTitleText("No Internet Connection!")
                .setCustomImage(R.drawable.no_internet_icon)
                .show();

    }
}
