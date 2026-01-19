package com.example.bookin.models;

import java.util.HashMap;
import java.util.Map;

public class ChatRoom {
    private String chatRoomId;
    private Map<String, Boolean> participants;
    private String lastMessage;
    private long lastMessageTime;
    private String lastSenderId;
    private String otherUserId;
    private String otherUserName;
    private String otherUserImage;
    private String bookId;
    private String bookTitle;
    private String bookImage;
    private long bookPrice;

    // Default constructor required for Firebase
    public ChatRoom() {
        participants = new HashMap<>();
    }

    public ChatRoom(String chatRoomId, String user1Id, String user2Id) {
        this.chatRoomId = chatRoomId;
        this.participants = new HashMap<>();
        this.participants.put(user1Id, true);
        this.participants.put(user2Id, true);
    }

    // Getters and Setters
    public String getChatRoomId() {
        return chatRoomId;
    }

    public void setChatRoomId(String chatRoomId) {
        this.chatRoomId = chatRoomId;
    }

    public Map<String, Boolean> getParticipants() {
        return participants;
    }

    public void setParticipants(Map<String, Boolean> participants) {
        this.participants = participants;
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

    public String getLastSenderId() {
        return lastSenderId;
    }

    public void setLastSenderId(String lastSenderId) {
        this.lastSenderId = lastSenderId;
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

    public String getOtherUserImage() {
        return otherUserImage;
    }

    public void setOtherUserImage(String otherUserImage) {
        this.otherUserImage = otherUserImage;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public String getBookImage() {
        return bookImage;
    }

    public void setBookImage(String bookImage) {
        this.bookImage = bookImage;
    }

    public long getBookPrice() {
        return bookPrice;
    }

    public void setBookPrice(long bookPrice) {
        this.bookPrice = bookPrice;
    }

    // Generate unique chat room ID from two user IDs
    public static String generateChatRoomId(String userId1, String userId2) {
        // Sort IDs to ensure consistent chat room ID regardless of who initiates
        if (userId1.compareTo(userId2) < 0) {
            return userId1 + "_" + userId2;
        } else {
            return userId2 + "_" + userId1;
        }
    }
}
