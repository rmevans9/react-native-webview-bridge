package com.bongole.rctwebviewbridge;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

/**
 * Created by bongole on 12/17/15.
 */
public class JSONConverter {

    public static WritableMap toWritableMap(JSONObject object) throws JSONException {
        Iterator<String> keys = object.keys();
        WritableMap r = Arguments.createMap();

        while ( keys.hasNext() ){
            String k = keys.next();
            Object v = object.get(k);
            if( v instanceof Number ){
                if( v instanceof Integer ) {
                    r.putInt(k, (Integer) v);
                }
                else {
                    r.putDouble(k, ((Number) v).doubleValue());
                }
            }
            else if( v instanceof String ) {
                r.putString(k, (String)v);
            }
            else if( v instanceof JSONArray ) {
                r.putArray(k, toWritableArray((JSONArray)v));
            }
            else if( v instanceof JSONObject) {
                r.putMap(k, toWritableMap((JSONObject)v));
            }
            else if( v == JSONObject.NULL ){
                r.putNull(k);
            }
        }

        return r;
    }

    public static WritableArray toWritableArray(JSONArray array) throws JSONException {
        WritableArray r = Arguments.createArray();

        for( int i = 0; i < array.length(); i++ ){
            Object v = array.get(i);
            if( v instanceof Number ){
                if( v instanceof Integer ) {
                    r.pushInt((Integer) v);
                }
                else {
                    r.pushDouble(((Number) v).doubleValue());
                }
            }
            else if( v instanceof String ) {
                r.pushString((String)v);
            }
            else if( v instanceof JSONArray ) {
                r.pushArray(toWritableArray((JSONArray)v));
            }
            else if( v instanceof JSONObject) {
                r.pushMap(toWritableMap((JSONObject)v));
            }
            else if( v == JSONObject.NULL ){
                r.pushNull();
            }
        }

        return r;
    }
}
