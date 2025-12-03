package com.carpoolingapp.models;

public class Conversation {
    private String conversationId;
    private String otherUserId;
    private String otherUserName;
    private String lastMessage;
    private long lastMessageTime;
    private String rideId;
    private String fromLocation;
    private String toLocation;

    public Conversation() {
    }

    public Conversation(String conversationId, String otherUserId, String otherUserName,
                        String lastMessage, long lastMessageTime, String rideId,
                        String fromLocation, String toLocation) {
        this.conversationId = conversationId;
        this.otherUserId = otherUserId;
        this.otherUserName = otherUserName;
        this.lastMessage = lastMessage;
        this.lastMessageTime = lastMessageTime;
        this.rideId = rideId;
        this.fromLocation = fromLocation;
        this.toLocation = toLocation;
    }

    // Getters and Setters
    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getOtherUserId() {
        return otherUserId;
    }

    public void setOtherUserId(String otherUserId) {
        this.otherUserId = otherUserId;
    }

    public String getOtherUserName() {
        return otherUserName;
    }

    public void setOtherUserName(String otherUserName) {
        this.otherUserName = otherUserName;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public long getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(long lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    public String getRideId() {
        return rideId;
    }

    public void setRideId(String rideId) {
        this.rideId = rideId;
    }

    public String getFromLocation() {
        return fromLocation;
    }

    public void setFromLocation(String fromLocation) {
        this.fromLocation = fromLocation;
    }

    public String getToLocation() {
        return toLocation;
    }

    public void setToLocation(String toLocation) {
        this.toLocation = toLocation;
    }
}