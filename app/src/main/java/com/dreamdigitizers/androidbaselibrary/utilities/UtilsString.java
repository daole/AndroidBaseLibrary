package com.dreamdigitizers.androidbaselibrary.utilities;

import android.content.Context;
import android.text.TextUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;

public class UtilsString {
    public static boolean isEmpty(String pValue) {
        return TextUtils.isEmpty(pValue);
    }

    public static boolean equals(String pValue1, String pValue2) {
        return TextUtils.equals(pValue1, pValue2);
    }

    public static boolean isInteger(String pValue) {
        try {
            Integer.parseInt(pValue);
            return true;
        } catch (NumberFormatException e) {
            return  false;
        }
    }

    public static boolean isPositiveInteger(String pValue) {
        try {
            int value = Integer.parseInt(pValue);
            return value >= 0 ? true : false;
        } catch (NumberFormatException e) {
            return  false;
        }
    }

    public static boolean isTime(String pValue, DateFormat pFormat) {
        try {
            pFormat.parse(pValue);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    public static String buildDateString(Context pContext, Calendar pCalendar) {
        DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(pContext);
        dateFormat.setCalendar(pCalendar);
        String value = dateFormat.format(pCalendar.getTime());
        return value;
    }
}
