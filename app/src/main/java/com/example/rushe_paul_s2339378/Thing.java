package com.example.rushe_paul_s2339378;

public class Thing {
    private String title;
    private String description;
    private String pubDate;

    public String getCurrencyCode() {
        if (title == null) {
            return "";
        }

        int start = title.indexOf('(');
        int end = title.indexOf(')');
        if (start >= 0 && end > start) {
            return title.substring(start + 1, end).trim();
        }

        return title.trim();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPubDate() {
        return pubDate;
    }

    public void setPubDate(String pubDate) {
        this.pubDate = pubDate;
    }

    @Override
    public String toString() {
        return "Thing{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", pubDate='" + pubDate + '\'' +
                '}';
    }
}
