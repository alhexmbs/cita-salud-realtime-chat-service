package com.example.robles_farma.model;

public class ChatMessage {
    private String id;
    private String text;
    private String senderId;
    private String senderName;
    private String timestamp;
    private boolean isSentByMe;
    
    // Nuevos campos para ubicación
    private String type; // "text" o "location"
    private double latitude;
    private double longitude;

    public ChatMessage(String id, String text, String senderId, String senderName, String timestamp, boolean isSentByMe) {
        this.id = id;
        this.text = text;
        this.senderId = senderId;
        this.senderName = senderName;
        this.timestamp = timestamp;
        this.isSentByMe = isSentByMe;
        this.type = "text"; // Por defecto
    }

    public String getId() { return id; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    
    public String getSenderId() { return senderId; }
    public String getSenderName() { return senderName; }
    public String getTimestamp() { return timestamp; }
    public boolean isSentByMe() { return isSentByMe; }
    
    // Getters y Setters para ubicación
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
}
