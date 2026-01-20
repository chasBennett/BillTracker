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

    public Message(String dateTime, String authorId, String name, boolean agent, String message) {

        setDateTime(dateTime);
        setAuthorId(authorId);
        setName(name);
        setAgent(agent);
        setMessage(message);
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
}
