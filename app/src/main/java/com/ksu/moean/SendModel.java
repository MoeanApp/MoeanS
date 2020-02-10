package com.ksu.moean;



import org.json.JSONObject;

public abstract class SendModel  {
    public abstract JSONObject toJSON();
    public abstract boolean isSent();
    public abstract boolean readyToSend();
    public abstract void setAsSent();
}