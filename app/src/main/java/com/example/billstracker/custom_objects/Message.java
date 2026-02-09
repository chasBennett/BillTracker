package com.example.billstracker.custom_objects;

/**
 * @noinspection unused
 */
public class Message {

    private String dateTime;
    private String authorId;
    private String name;
    private boolean agent;
    private String message;
    private String repliedToText;
    private String repliedToId;
    private String repliedToName;
    private boolean read;

    public Message(String dateTime, String authorId, String name, boolean agent, String message, String repliedToText, String repliedToName, String repliedToId) {

        setDateTime(dateTime);
        setAuthorId(authorId);
        setName(name);
        setAgent(agent);
        setMessage(message);
        setRepliedToText(repliedToText);
        setRepliedToName(repliedToName);
        setRepliedToId(repliedToId);
        setRead(false);

    }

    /**
     * @noinspection unused
     */
    public Message() {

    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isAgent() {
        return agent;
    }

    public void setAgent(boolean agent) {
        this.agent = agent;
    }
    public String getRepliedToText() {
        return repliedToText;
    }
    public void setRepliedToText(String repliedToText) {
        if (repliedToText == null) {
            repliedToText = "";
        }
        this.repliedToText = repliedToText;
    }
    public String getRepliedToName() {
        return repliedToName;
    }
    public void setRepliedToName(String repliedToName) {
        if (repliedToName == null) {
            repliedToName = "";
        }
        this.repliedToName = repliedToName;
    }
    public String getRepliedToId() {
        if (repliedToId == null) {
            repliedToId = "";
        }
        return repliedToId;
    }
    public void setRepliedToId(String repliedToId) {
        if (repliedToId == null) {
            repliedToId = "";
        }
        this.repliedToId = repliedToId;
    }
    public boolean isRead() {
        return read;
    }
    public void setRead(boolean read) {
        this.read = read;
    }
}
