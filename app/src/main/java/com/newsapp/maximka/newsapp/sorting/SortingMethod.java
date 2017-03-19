package com.newsapp.maximka.newsapp.sorting;

import java.util.Comparator;


public interface SortingMethod<T> {
    double sort(T[] list, Comparator<T> comparator);
}
