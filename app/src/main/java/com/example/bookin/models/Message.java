package com.example.bookin.models;

public class Message {
    public static final String TYPE_TEXT = "text";
    public static final String TYPE_BOOK_INFO = "book_info";

    private String messageId;
    private String senderId;
    private String senderName;
    private String senderImage;
    private String message;
    private long timestamp;
    private boolean isRead;
    private String messageType;
    
    // Book info fields (for TYPE_BOOK_INFO messages)
    private String bookId;
    private String bookTitle;
    private String bookImage;
    private long bookPrice;

    // Default constructor required for Firebase
    public Message() {
        this.messageType = TYPE_TEXT;
    }

    public Message(String messageId, String senderId, String senderName, String senderImage, 
                   String message, long timestamp) {
        this.messageId = messageId;
        this.senderId = senderId;
        this.senderName = senderName;
        this.senderImage = senderImage;
        this.message = message;
        this.timestamp = timestamp;
        this.isRead = false;
        this.messageType = TYPE_TEXT;
    }

    // Getters and Setters
    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderImage() {
        return senderImage;
    }

    public void setSenderImage(String senderImage) {
        this.senderImage = senderImage;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
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
}
