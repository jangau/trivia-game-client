package com.hella.christmasquiz.communication;

import java.lang.reflect.Field;

public class Utils {

    public static int getResId(String resName, Class<?> c) throws IllegalAccessException {
        Field idField = null;
        try{
            idField = c.getDeclaredField(resName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return idField.getInt(idField);
    }
}
