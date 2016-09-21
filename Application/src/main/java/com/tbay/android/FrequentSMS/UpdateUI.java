package com.tbay.android.FrequentSMS;

/**
 * Created by Thomas on 16-Sep-16.
 */


class UpdateUI implements Runnable
{
    String updateString;

    public UpdateUI(String updateString) {
        this.updateString = updateString;
    }

    public void run() {

        /*Context context = getApplicationContext();
        int duration = Toast.LENGTH_LONG;

        Toast toast = Toast.makeText(context, updateString, duration);
        toast.show();

        //txtview.setText(updateString);*/
    }
}
