package com.tbay.android.FrequentSMS;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Thomas on 19-Nov-16.
 */

public class AppPreferences {
    static String Key1_Text;    // The text to send in the SMS/TextMessage
    static String Key2_Text;
    static String Key3_Text;
    static String Key4_Text;

    static String Key1_KeyTextDefault = "TI WiFi";
    static String Key2_KeyTextDefault = "Indkøb på vejen hjem?";
    static String Key3_KeyTextDefault = "Er snart hjemme";
    static String Key4_KeyTextDefault = "Send test SMS";

    static String Key1_MsgDefault = "Wifi";
    static String Key2_MsgDefault = "Skal jeg købe noget med på vejen hjem?";
    static String Key3_MsgDefault = "Jeg er hjemme om et kvarter";
    static String Key4_MsgDefault = "Dette er en test SMS";

    static String Key1_Msg;
    static String Key2_Msg;
    static String Key3_Msg;
    static String Key4_Msg;

    static int SelectionId;

    public void readPreferences(Context c)
    {

        // Get selection from shared Preferences. The preference is set when selecting a radiobutton.
        // Then set the text in the editable field to the last selected text (currently a default text)
        SharedPreferences mPref = c.getSharedPreferences("com.tbay.android.FrequentSMS.PREFS", Context.MODE_PRIVATE);
        SelectionId = mPref.getInt("LASTSELECTION", 0);

        Key1_Text = mPref.getString("KEY1_TEXT", Key1_KeyTextDefault);
        Key2_Text = mPref.getString("KEY2_TEXT", Key2_KeyTextDefault);
        Key3_Text = mPref.getString("KEY3_TEXT", Key3_KeyTextDefault);
        Key4_Text = mPref.getString("KEY4_TEXT", Key4_KeyTextDefault);

        Key1_Msg = mPref.getString("MSG1", Key1_MsgDefault);
        Key2_Msg = mPref.getString("MSG2", Key2_MsgDefault);
        Key3_Msg = mPref.getString("MSG3", Key3_MsgDefault);
        Key4_Msg = mPref.getString("MSG4", Key4_MsgDefault);
    }

    public void savePreferences(Context c)
    {
        SharedPreferences mPref = c.getSharedPreferences("com.tbay.android.FrequentSMS.PREFS", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mPref.edit();

        editor.putString("KEY1_TEXT", Key1_Text);
        editor.putString("KEY2_TEXT", Key2_Text);
        editor.putString("KEY3_TEXT", Key3_Text);
        editor.putString("KEY4_TEXT", Key4_Text);

        editor.putString("MSG1", Key1_Msg);
        editor.putString("MSG2", Key2_Msg);
        editor.putString("MSG3", Key3_Msg);
        editor.putString("MSG4", Key4_Msg);

        editor.putInt("LASTSELECTION", SelectionId);
        editor.apply();

    }

    AppPreferences (Context c)
    {
        readPreferences(c);
    }
}
