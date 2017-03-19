package com.newsapp.maximka.newsapp.datetimeformatter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class DateTimeFormatter {
    private SimpleDateFormat dateFormat = new SimpleDateFormat("", Locale.ENGLISH);

    public Date toDate(String date) {
        dateFormat.applyPattern("EEE, d MMM yyyy HH:mm:ss Z");
        try {
            return dateFormat.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return new Date();
        }
    }

    public String toString(Date date) {
        dateFormat.applyPattern("dd.MM.yyyy 'в' HH:mm");
        return dateFormat.format(date);
    }
}
