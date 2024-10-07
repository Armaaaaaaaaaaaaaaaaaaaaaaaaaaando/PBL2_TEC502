package com.example.tarefas.dto;

import java.io.Serializable;

public class MessageDTO implements Serializable {
    private static final long serialVersionUID = 1L; 

    private Integer senderId;
    private String recipientId;
    private String content;

    // Getter para senderId
    public Integer getSenderId() {
        return senderId;
    }

    // Setter para senderId
    public void setSenderId(Integer senderId) {
        this.senderId = senderId;
    }

    // Getter para recipientId
    public String getRecipientId() {
        return recipientId;
    }

    // Setter para recipientId
    public void setRecipientId(String recipientId) {
        this.recipientId = recipientId;
    }

    // Getter para content
    public String getContent() {
        return content;
    }

    // Setter para content
    public void setContent(String content) {
        this.content = content;
    }
}
