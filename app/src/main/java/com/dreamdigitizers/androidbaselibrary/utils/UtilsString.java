package com.dreamdigitizers.androidbaselibrary.utils;

import android.text.TextUtils;

import java.text.DateFormat;
import java.text.ParseException;

public class UtilsString {
    public static boolean isEmpty(String pValue) {
        return TextUtils.isEmpty(pValue);
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
}
