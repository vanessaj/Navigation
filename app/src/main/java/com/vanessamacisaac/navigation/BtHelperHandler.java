package com.vanessamacisaac.navigation;

import android.os.Message;

import android.os.Handler;

/**
 * Created by vanessamacisaac on 15-03-14.
 */
public abstract class BtHelperHandler extends Handler{

    public enum MessageType{
        STATE,
        READ,
        WRITE,
        DEVICE,
        NOTIFY;
    }

    public Message obtainMessage(MessageType message, int count, Object obj) {

        //message = message.ordinal();
        //return obtainMessage(message, count, -1, obj);
        //return obtainMessage(message.ordinal(), count, obj);
        return obtainMessage(message, count, obj);

    }

    //protected abstract Message obtainMessage(int ordinal, int count, int i, Object obj);

    public MessageType getMessageType(int ordinal){
        return MessageType.values()[ordinal];
    }
}
