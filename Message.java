package src;

import java.io.Serializable;

public class Message implements Serializable {
    private String senderId;
    private String recipientId;
    private String content;

    public Message(String senderId, String recipientId, String content) {
        this.senderId = senderId;
        this.recipientId = recipientId;
        this.content = content;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getRecipientId() {
        return recipientId;
    }

    public String getContent() {
        return content;
    }
}
