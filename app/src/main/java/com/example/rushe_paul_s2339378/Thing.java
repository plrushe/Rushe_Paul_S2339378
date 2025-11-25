package com.example.rushe_paul_s2339378;

public class Thing {
    private String title;
    private String description;
    private String pubDate;

    public String getCurrencyCode() {
        // quick attempt to pull the code out of the brackets
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
        // setter kept simple
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        // stash the rate text
        this.description = description;
    }

    public String getPubDate() {
        return pubDate;
    }

    public void setPubDate(String pubDate) {
        // save the publish date as-is
        this.pubDate = pubDate;
    }

    @Override
    public String toString() {
        // handy for logging
        return "Thing{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", pubDate='" + pubDate + '\'' +
                '}';
    }
}
