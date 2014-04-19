package com.hdweiss.morgand.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

public class Utils {
    public static String ExceptionTraceToString(Exception exception) {
        StringWriter sw = new StringWriter();
        exception.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
}
