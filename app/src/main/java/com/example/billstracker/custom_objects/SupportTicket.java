package com.example.billstracker.custom_objects;

import java.util.ArrayList;

public class SupportTicket {

    private String name;
    private String userUid;
    private String userEmail;
    private String agent;
    private String agentUid;
    private ArrayList<Message> messages;
    private String notes;
    private boolean open;
    private String id;
    private long dateOfLastActivity;
    private int unreadByUser;
    private int unreadByAgent;

    public SupportTicket(String name, String userUid, String userEmail, String agent, ArrayList<Message> messages, String ignoredNotes, boolean open, String id, int unreadByUser, long dateOfLastActivity, int unreadByAgent, String agentUid) {

        setName(name);
        setUserUid(userUid);
        setUserEmail(userEmail);
        setAgent(agent);
        setMessages(messages);
        setOpen(open);
        setId(id);
        setUnreadByUser(unreadByUser);
        setDateOfLastActivity(dateOfLastActivity);
        setUnreadByAgent(unreadByAgent);
        setAgentUid(agentUid);
    }

    public SupportTicket() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAgent() {
        return agent;
    }

    public void setAgent(String agent) {
        this.agent = agent;
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getUnreadByUser() {
        return unreadByUser;
    }

    public void setUnreadByUser(int unreadByUser) {
        this.unreadByUser = unreadByUser;
    }

    public int getUnreadByAgent() {
        return unreadByAgent;
    }

    public void setUnreadByAgent(int unreadByAgent) {
        this.unreadByAgent = unreadByAgent;
    }

    public String getAgentUid() {
        return agentUid;
    }

    public void setAgentUid(String agentUid) {
        this.agentUid = agentUid;
    }

    public String getUserUid() {
        return userUid;
    }

    public void setUserUid(String userUid) {
        this.userUid = userUid;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public ArrayList<Message> getMessages() {
        return messages;
    }

    public void setMessages(ArrayList<Message> messages) {
        this.messages = messages;
    }

    public long getDateOfLastActivity() {
        return dateOfLastActivity;
    }

    public void setDateOfLastActivity(long dateOfLastActivity) {
        this.dateOfLastActivity = dateOfLastActivity;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
