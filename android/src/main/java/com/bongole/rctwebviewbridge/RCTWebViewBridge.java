package com.bongole.rctwebviewbridge;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.UIManagerModule;
import com.facebook.react.uimanager.events.EventDispatcher;
import com.fasterxml.jackson.core.JsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by bongole on 12/15/15.
 */
public class RCTWebViewBridge extends WebView {

    private static final String RCTWebViewBridgeSchema = "wvb";
    private static final String WebViewBridgeScript =
            "  'use strict';\n" +
            "\n" +
            "  if (window.WebViewBridge) {\n" +
            "    return;\n" +
            "  }\n" +
            "\n" +
            "  var RNWBSchema = 'wvb';\n" +
            "  var sendQueue = [];\n" +
            "  var receiveQueue = [];\n" +
            "  var doc = window.document;\n" +
            "  var customEvent = doc.createEvent('Event');\n" +
            "\n" +
            "  function callFunc(func, message) {\n" +
            "    if ('function' === typeof func) {\n" +
            "      func(message);\n" +
            "    }\n" +
            "  }\n" +
            "\n" +
            "  function signalNative() {\n" +
            "    alert(RNWBSchema + ':' + WebViewBridge.__fetch__());\n" +
            "  }\n" +
            "\n" +
            "  var WebViewBridge = {\n" +
            "    __push__: function (message) {\n" +
            "      receiveQueue.push(message);\n" +
            "      setTimeout(function () {\n" +
            "        var message = receiveQueue.pop();\n" +
            "        callFunc(WebViewBridge.onMessage, message);\n" +
            "      }, 15); \n" +
            "    },\n" +
            "    __fetch__: function () {\n" +
            "      var messages = JSON.stringify(sendQueue);\n" +
            "\n" +
            "      sendQueue = [];\n" +
            "\n" +
            "      return messages;\n" +
            "    },\n" +
            "    send: function (message) {\n" +
            "      if ('string' !== typeof message) {\n" +
            "        callFunc(WebViewBridge.onError, \"message is type '\" + typeof message + \"', and it needs to be string\");\n" +
            "        return;\n" +
            "      }\n" +
            "\n" +
            "      sendQueue.push(message);\n" +
            "      signalNative();\n" +
            "    },\n" +
            "    onMessage: null,\n" +
            "    onError: null\n" +
            "  };\n" +
            "\n" +
            "  window.WebViewBridge = WebViewBridge;\n" +
            "\n" +
            "  customEvent.initEvent('WebViewBridge', true, true);\n" +
            "  doc.dispatchEvent(customEvent);";

    EventDispatcher eventDispatcher;
    EventWebClient eventWebClient;

    protected class EventWebClient extends WebViewClient {

        String injectedJavaScript;

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        public void setInjectedJavaScript(String injectedJavaScript) {
            this.injectedJavaScript = injectedJavaScript;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            /*
            eventDispatcher.dispatchEvent(new NavigationStateChangeEvent(
                    getId(), SystemClock.uptimeMillis(), view.canGoBack(), view.canGoForward(),
                    url, view.getTitle(), true));
                    */
        }

        @Override
        public void onPageFinished(WebView view, String url) {

            eventDispatcher.dispatchEvent(new NavigationStateChangeEvent(
                    getId(), SystemClock.uptimeMillis(), false,
                    url, true, view.canGoForward()));


            view.loadUrl("javascript:" + jsWrap(WebViewBridgeScript) );

            if(this.injectedJavaScript != null) {
                view.loadUrl("javascript:" + jsWrap(injectedJavaScript));
            }
        }
    }

    private static String jsWrap(String js){
        return "(function(window){\n" + js + "\n}(window));";
    }

    private static final String sendToBridgeTemplate =
                "      if (WebViewBridge && WebViewBridge.__push__) {\n" +
                "        WebViewBridge.__push__('%s');\n" +
                "      }\n";

    public void sendToBridge(String message){
        loadUrl("javascript:" + jsWrap(String.format(sendToBridgeTemplate, message)));
    }

    public RCTWebViewBridge(ReactContext reactContext, Activity activity) {
        super(reactContext);

        eventDispatcher = reactContext.getNativeModule(UIManagerModule.class).getEventDispatcher();
        eventWebClient = new EventWebClient();

        getSettings().setJavaScriptEnabled(true);
        getSettings().setBuiltInZoomControls(false);
        getSettings().setGeolocationEnabled(false);
        getSettings().setAllowFileAccess(true);
        getSettings().setAllowFileAccessFromFileURLs(true);
        getSettings().setLoadsImagesAutomatically(true);
        getSettings().setBlockNetworkImage(false);
        getSettings().setBlockNetworkLoads(false);

        setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                String schemaWithColon = RCTWebViewBridgeSchema + ":";
                if ( message.indexOf(schemaWithColon) == 0 ){
                    try {
                        JSONArray jsonArray = new JSONArray(message.substring(schemaWithColon.length()));
                        eventDispatcher.dispatchEvent(new RCTMessageEvent(getId(), SystemClock.uptimeMillis(), JSONConverter.toWritableArray(jsonArray)));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    finally {
                        result.confirm();
                    }

                    return true;
                } else {
                    return false;
                }
            }
        });

        setWebViewClient(eventWebClient);

        // For Security
        // https://daoyuan14.github.io/news/newattackvector.html
        removeJavascriptInterface("searchBoxJavaBridge_");
        removeJavascriptInterface("accessibility");
        removeJavascriptInterface("accessibilityTraversal");
    }

    public void setInjectedJavaScript(String injectedJavaScript) {
        eventWebClient.setInjectedJavaScript(injectedJavaScript);
    }
}
