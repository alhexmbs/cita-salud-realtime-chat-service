package com.example.robles_farma.ui.chat;

public class ChatItem {

    private String id;
    private String name;
    private String lastMessage;
    private String timestamp;  // 👈 DEBE ESTAR
    private boolean hasUnread; // 👈 DEBE ESTAR

    private String doctorId;

    // Constructor original (mantener compatibilidad)
    public ChatItem(String id, String name, String lastMessage) {
        this.id = id;
        this.name = name;
        this.lastMessage = lastMessage;
        this.timestamp = "";
        this.hasUnread = false;
    }

    // Constructor extendido
    public ChatItem(String id, String name, String lastMessage, String timestamp, boolean hasUnread, String doctorId) {
        this.id = id;
        this.name = name;
        this.lastMessage = lastMessage;
        this.timestamp = timestamp;
        this.hasUnread = hasUnread;
        this.doctorId = doctorId;
    }

    public String getDoctorId() {
        return doctorId;
    }

    // Getters originales
    public String getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    // 👇 NUEVOS Getters y Setters
    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public boolean hasUnread() {
        return hasUnread;
    }

    public void setHasUnread(boolean hasUnread) {
        this.hasUnread = hasUnread;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }
}