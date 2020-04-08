package org.example;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.ext.logging.LoggingInInterceptor;
import org.apache.cxf.ext.logging.LoggingOutInterceptor;
import org.apache.cxf.staxutils.StaxUtils;
import org.apache.cxf.ws.security.SecurityConstants;
import org.apache.cxf.ws.security.tokenstore.SecurityToken;
import org.apache.cxf.ws.security.trust.STSClient;
import org.apache.wss4j.common.saml.SAMLCallback;





public class StsSamlCallbackHandler implements CallbackHandler {
    
    private static final String SAML2_TOKEN_TYPE = 
        "http://docs.oasis-open.org/wss/oasis-wss-saml-token-profile-1.1#SAMLV2.0";
    private static final String BEARER_KEYTYPE = 
        "http://docs.oasis-open.org/ws-sx/ws-trust/200512/Bearer";
    

    public StsSamlCallbackHandler() {
        //
    }

    

    

    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        System.out.println("======>fetch saml2 token from sts/keycloak");
        Bus bus = BusFactory.getDefaultBus();
        String stsEndpoint = "http://localhost:8080/cxf/UT";
       
        try {
            SecurityToken token =
                requestSecurityToken(SAML2_TOKEN_TYPE, BEARER_KEYTYPE, bus, stsEndpoint);
      
            //StaxUtils.print(token.getToken());
            for (int i = 0; i < callbacks.length; i++) {
                if (callbacks[i] instanceof SAMLCallback) {
                    SAMLCallback callback = (SAMLCallback) callbacks[i];
                    callback.setAssertionElement(token.getToken());
                }
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        
    }

    private SecurityToken requestSecurityToken(String tokenType, String keyType, Bus bus,
                                               String endpointAddress)
        throws Exception {
        STSClient stsClient = new STSClient(bus);

        stsClient.setWsdlLocation(endpointAddress + "?wsdl");
        stsClient.setServiceName("{http://docs.oasis-open.org/ws-sx/ws-trust/200512/}SecurityTokenService");
        stsClient.setEndpointName("{http://docs.oasis-open.org/ws-sx/ws-trust/200512/}UT_Port");
        stsClient.setEnableAppliesTo(false);

        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(SecurityConstants.USERNAME, "karafadmin");
        properties.put(SecurityConstants.CALLBACK_HANDLER, new org.example.UTPasswordCallback());
        

        stsClient.setProperties(properties);
        stsClient.setTokenType(tokenType);
        stsClient.setKeyType(keyType);
        stsClient.getOutInterceptors().add(new LoggingOutInterceptor());
        stsClient.getInInterceptors().add(new LoggingInInterceptor());
        return stsClient.requestSecurityToken(endpointAddress);
    }
    
}
