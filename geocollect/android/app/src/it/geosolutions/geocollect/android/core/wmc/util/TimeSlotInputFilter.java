package it.geosolutions.geocollect.android.core.wmc.util;

import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;

/**
 * Created by Robert Oehler on 06.11.16.
 *
 * Class to apply min/max values for an editText
 * Source : http://stackoverflow.com/questions/14212518/is-there-a-way-to-define-a-min-and-max-value-for-edittext-in-android
 */

public class TimeSlotInputFilter implements InputFilter {

    private int min, max;

    public TimeSlotInputFilter(int min, int max) {
        this.min = min;
        this.max = max;
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {

        final String newValue = dest.toString() + source.toString();
        if(TextUtils.isEmpty(newValue)){
            return "";
        }
        try {
            final int input = Integer.parseInt(newValue);
            if (isInRange(min, max, input)) {
                return null;
            }
        } catch (NumberFormatException e) {
            Log.e("TimeSlotFilter","numberFormatException",e);
        }
        return "";
    }

    private boolean isInRange(int a, int b, int c) {
        return b > a ? c >= a && c <= b : c >= b && c <= a;
    }
}