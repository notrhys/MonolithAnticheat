package me.rhys.anticheat.util;

import me.rhys.anticheat.Plugin;

public class ClassUtil {

    public static String getLicense(Class<?> a) {
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        for (int i = 1; i < elements.length; i++) {
            StackTraceElement ste = elements[i];
            if (!ste.getClassName().equals(a.getName())
                    && ste.getClassName().indexOf("java.lang.Thread") != 0) {
                return ste.getClassName();
            }
        }

        return null;
    }

    public static boolean isClassLoaded(String clazz) {

        try {
            Class.forName(clazz);

            return true;
        } catch (Exception ignored) {
            //
        }

        return false;
    }

    public static Class<?> invoke(String clazz) {

        try {
            return Class.forName(clazz);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }
}
