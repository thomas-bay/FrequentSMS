package com.tbay.android.FrequentSMS;

/**
 * Created by Thomas on 09-Oct-16.
 */

public final class AppConstants {

    // Positions
    static final double WorkLatitude = 55.657721;
    static final double WorkLongitude = 12.273066;
    static final float WorkRadius = (float) 250.0;
    static final String WorkFenceId = "Workplace";

    static final double HomeLatitude = 55.747658;
    static final double HomeLongitude = 12.389716;
    static final float HomeRadius = (float) 150.0;
    static final String HomeFenceId = "Home";

    static final double BibloLatitude = 55.729438;
    static final double BibloLongitude = 12.359988;
    static final float BibloRadius = (float) 100.0;
    static final String LibraryId = "Biblo";

    static final boolean SendPositionSMS = true;

    static final long WorkLatency = (long)(12.0*3600*1000); // ms

    // Phone numbers
    static final String phonePrivate = "+4530221982";
    static final String phoneAnnette = "+4530223796";

    static final String phoneWork = "+4572201214";
    static final String phoneWifi = "+4572201018";

    private AppConstants(){
        throw new AssertionError();
    }
}
