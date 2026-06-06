package com.example.techgram;

public class Solution {
    private final int id;
    private final String taskId;
    private final String taskTitle;
    private final String sender;
    private final String receiver;
    private final String text;
    private final int score;
    private final String timestamp;
    private final byte[] imageBlob;

    public Solution(int id, String taskId, String taskTitle, String sender, String receiver, String text, int score, String timestamp, byte[] imageBlob) {
        this.id = id; this.taskId = taskId; this.taskTitle = taskTitle; this.sender = sender; this.receiver = receiver; this.text = text; this.score = score; this.timestamp = timestamp; this.imageBlob = imageBlob;
    }

    public int getId() { return id; }
    public String getTaskId() { return taskId; }
    public String getTaskTitle() { return taskTitle; }
    public String getSender() { return sender; }
    public String getText() { return text; }
    public int getScore() { return score; }
    public String getTimestamp() { return timestamp; }
    public byte[] getImageBlob() { return imageBlob; }
}