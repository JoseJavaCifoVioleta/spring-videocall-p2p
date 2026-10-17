package com.proyecto.videocall.model;

public class ChatMessage {
    private String sender;
    private String content;
    private String type; // Ej: "CHAT", "OFFER", "ANSWER", "ICE"

    // Constructor vacío obligatorio para la conversión JSON de Spring
    public ChatMessage() {}

    public ChatMessage(String sender, String content, String type) {
        this.sender = sender;
        this.content = content;
        this.type = type;
    }

    // Getters y Setters
    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
