package com.newsapp.maximka.newsapp.sorting;

import android.os.SystemClock;

import java.util.Arrays;
import java.util.Comparator;


public class JavaSort<T> implements SortingMethod<T> {
    @Override
    public double sort(T[] list, Comparator<T> comparator) {
        long start = SystemClock.elapsedRealtimeNanos();
        Arrays.sort(list, comparator);
        return (SystemClock.elapsedRealtimeNanos() - start) / 1000 / 1000.0;
    }
}
