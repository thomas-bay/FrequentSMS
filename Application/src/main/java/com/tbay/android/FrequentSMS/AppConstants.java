package com.tbay.android.FrequentSMS;

/**
 * Created by Thomas on 09-Oct-16.
 */

public final class AppConstants {

    // Positions
    public static final double WorkLatitude = 55.657721;
    public static final double WorkLongitude = 12.273066;
    public static final float WorkRadius = (float) 150.0;
    public static final String WorkFenceId = "Workplace";

    public static final double HomeLatitude = 55.747658;
    public static final double HomeLongitude = 12.389716;
    public static final float HomeRadius = (float) 100.0;
    public static final String HomeFenceId = "Home";

    public static final double BibloLatitude = 55.729438;
    public static final double BibloLongitude = 12.359988;
    public static final float BibloRadius = (float) 100.0;
    public static final String LibraryId = "Biblo";

    // Phone numbers
    public static final String phonePrivate = "+4530221982";
    public static final String phoneAnnette = "+4530221982";

    public static final String phoneWork = "+4572201214";
    public static final String phoneWifi = "+4572201018";

    // Text constants
    public static final String txtWifi = "Wifi";
    public static final String txtShopping = "Skal jeg købe noget med på vejen hjem?";
    public static final String txtTestSMS = "Dette er en test SMS";
    public static final String txtHomeSoon = "Jeg er hjemme om et kvarter";

    private AppConstants(){
        throw new AssertionError();
    }
}
