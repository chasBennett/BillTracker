package com.example.billstracker.custom_objects;

public class Trophy {

    private int type;
    private boolean shown;
    private String message;
    private boolean shared;

    public Trophy (int type, boolean shown, String message, boolean shared) {

        setType(type);
        setShown(shown);
        setMessage(message);
        setShared(shared);
    }
    /** @noinspection unused*/
    public Trophy () {

    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public boolean getShown() {
        return shown;
    }

    public void setShown(boolean shown) {
        this.shown = shown;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    /** @noinspection unused*/
    public boolean isShared() {
        return shared;
    }

    public void setShared(boolean shared) {
        this.shared = shared;
    }
}
