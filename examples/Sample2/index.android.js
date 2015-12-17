/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 */
'use strict';

var React = require('react-native');
var {
  AppRegistry,
  StyleSheet,
  Text,
  View,
  EdgeInsetsPropType,
  requireNativeComponent, 
  PropTypes
} = React;

var iface = {
    name: 'RCTWebViewBridge',
    propTypes: {
        ...View.propTypes,
        html: PropTypes.string,
        url: PropTypes.string,
        contentInset: EdgeInsetsPropType,
        injectedJavaScript: PropTypes.string,
        style: View.propTypes.style,
        onBridgeMessage: PropTypes.func,
    }
};

var styles = StyleSheet.create({
  webviewstyle: {
      flex: 1,
      backgroundColor: '#FFFFFF'
  },
  container: {
    flex: 1,
    //justifyContent: 'center',
    //alignItems: 'center',
    backgroundColor: '#00FF00',
  },
  welcome: {
    fontSize: 20,
    textAlign: 'center',
    margin: 10,
  },
  instructions: {
    textAlign: 'center',
    color: '#333333',
    marginBottom: 5,
  },
});

var onBridgeMessage = (event: Event) => {
    console.log(event.nativeEvent.messages);
};

var RCTWebViewBridge = requireNativeComponent('RCTWebViewBridge', iface);
var script = `
function foo(){
    WebViewBridge.send("message from webview");
}
`;

var myHTML = "<html><body><script>\n" + script + "\n</script><a href='#' onclick='foo();'>hello world</a></body></html>";

var Sample2 = React.createClass({
  render: function() {
    return (
      <View style={styles.container}>
          <RCTWebViewBridge html={myHTML} style={styles.webviewstyle} onBridgeMessage={onBridgeMessage} />
      </View>
    );
  }
});

AppRegistry.registerComponent('Sample2', () => Sample2);
