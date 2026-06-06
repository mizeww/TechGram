package com.example.techgram;

public class Task {
    private final String id;
    private final String title;
    private final String description;
    private final String discipline;
    private final String timestamp;
    private final String author;
    private final String criteria;
    private final byte[] imageBlob;

    public Task(String id, String title, String description, String discipline, String timestamp, String author, String criteria, byte[] imageBlob) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.discipline = discipline;
        this.timestamp = timestamp;
        this.author = author;
        this.criteria = criteria;
        this.imageBlob = imageBlob;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getDiscipline() { return discipline; }
    public String getTimestamp() { return timestamp; }
    public String getAuthor() { return author; }
    public String getCriteria() { return criteria; }
    public byte[] getImageBlob() { return imageBlob; }
}