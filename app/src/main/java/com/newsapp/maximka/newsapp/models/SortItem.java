package com.newsapp.maximka.newsapp.models;


public class SortItem {
    public NewsFeed news;
    public String text;
    public int wordCount;

    public SortItem(NewsFeed news, String text, int wordCount) {
        this.news = news;
        this.text = text;
        this.wordCount = wordCount;
    }
}
