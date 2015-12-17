package com.bongole.rctwebviewbridge;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.events.Event;
import com.facebook.react.uimanager.events.RCTEventEmitter;

/**
 * Created by bongole on 12/17/15.
 */
public class RCTMessageEvent extends Event<RCTMessageEvent> {
    public static final String EVENT_NAME = "topMessage";

    private WritableArray mMessages;

    public RCTMessageEvent(int viewTag, long timestampMs, WritableArray messages){
        super(viewTag, timestampMs);
        mMessages = messages;
    }

    @Override
    public String getEventName() {
        return EVENT_NAME;
    }

    @Override
    public void dispatch(RCTEventEmitter rctEventEmitter) {
        WritableMap r = Arguments.createMap();
        r.putArray("messages", mMessages);
        rctEventEmitter.receiveEvent(getViewTag(), getEventName(), r);
    }
}
