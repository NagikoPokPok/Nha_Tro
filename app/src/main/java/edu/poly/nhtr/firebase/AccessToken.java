package edu.poly.nhtr.firebase;

import android.util.Log;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.common.collect.Lists;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class AccessToken {

    private static final String firebaseMessagingScope = " https://www.googleapis.com/auth/firebase.messaging";

    public String getAccessToken() {
        try{
            String jsonString = "{\n" +
                    "  \"type\": \"service_account\",\n" +
                    "  \"project_id\": \"nha-tro-57e88\",\n" +
                    "  \"private_key_id\": \"135983d3b04d022953c059946a0e186c67964d34\",\n" +
                    "  \"private_key\": \"-----BEGIN PRIVATE KEY-----\\nMIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC3a+hY3FoG35i7\\nTsEvx37Ejj0oSBWsAnqN6aPmJnvNeGmHW8snv1cXwpSdZy+eh4n1h6cpOXdujs0Q\\nKNNy1ubz5UV1vrXrm6m3uuqU9DFKkfOAhYGVRz9mqqSzxC3W5Aq+KL2kMPi6EcF5\\nRZeTNBD+GdUNVxAobxvYdS9bXrJT0a7lOB/jeD/II6izTLx5fM1r0MmaSr40D95G\\nXMCETuPOs6aiSnbDlt3GEog9GxjUiTP0+N1vxSTY0pmXlTmA/Zb8o5BlBt8M8i/o\\nVYp63xpVzPuFHrtgxXCzkg8ZXDbGm0fKDS13oktRa6syw5fp3gMmfQLV5O0zg7dY\\n902jsFaBAgMBAAECggEAAdbn814wW/HJnRVG3DUOuSdpBxqj9v/mXT562Y/IvZCH\\n1GbwBGPHmpIlvedfQ6J92HPGEiSEpVvyxAb4yeXMVqVVuz7Qob6DpvdNq3VAekw4\\n6jDMkiPvB5dOBAyDXiyWy/3qGvevwuPzMgVn5f7Hbdjx+UqGe7VTjOnt8edi9/K1\\nyrksc9LP3BiJCYls1mIZDTdVCQ7mjftm/ndpqkSgrhWKn+6yLSy1K/ObRaiiA4fJ\\nFVEYT4a3JVS9aabU8XlDncRsrW3TR7czD9VzVGG3VneuOYNxZQynlxt2CarSs3+Q\\nC0C9d+ZB6WRAi0FPLUfiM6+ADuCs8eX/kX1txuGlfQKBgQDciDomyeUem2Fd0XbG\\n94yBvBwmNHtVtSBfDcWYPBCau9KJSFUXXALtzMWzbGRjSD2VrY+mJVWyeyM5iSUv\\nVjnkv8tBRS60/jxE3In1y6OfbE8+Dm413RQwILIXgdoltE5mouI8y6pEsOzIzqRA\\nlEL/D5WrDRhooxpT2sVHvmKPTQKBgQDU68IEyTwBLJrmp56y4Utd988Gw2m0TLTi\\n4yGS72uV0gnVZq/mzox/1SmwZBRqHahfbpV43+V/AbW1Ao9bjSFWG7iu/7WcpwOE\\nAootoRhW6LR81AHdpGQsgn7su7BGHQGXgvrAH9rjBt7r7lr2Hl6/UMNp8ZsF2ZAP\\nRIzeJKCyBQKBgA5N+8+mSulYVNF8E8NGLWQA6qaDwvhFPmtL/mQoiT8wLbDf8z0H\\np9z66lwADObvuabq0iWJyGxCVn+V6MaQRkMTOcDrFuOAljI1R/GcmultYhp5Xya5\\nR2TzyfmCX78QWgGIUTp2T34TJ9jCpp7Vvx7CSQgGWm33Hm8QArnrnIMFAoGBALPs\\n6g6LUuaXH+rsGKKG2d51a4AGoAuZin30e8mkAqEfVen8lYaQYpAfvNPvLoUhvMew\\nNiVn5pJ2CmwE1soBr/sI//753SmcxM7IKqY2bM/8Y5DcwxreMTyfpcw9Ks+E8XRs\\nXaL/CEQy6SwsRreWoHBBOnELlvnRiPgt8HBEOXtVAoGAUWWBTX7D2oaYALfaH3aA\\nELTPoI1stzu4jshomxmG2Cb69rr8hWTRy89mJz7CHDIOCllwhDS8VBN6OKugHRsF\\nPvcBDB+kpXcimao4Gw57z87Y0F0Xflul9JQHbI26qIR0d0moY346/oL5Sy8QjXdv\\nddxjHOVDqxvl+H5QAQBLVvU=\\n-----END PRIVATE KEY-----\\n\",\n" +
                    "  \"client_email\": \"firebase-adminsdk-rp7sq@nha-tro-57e88.iam.gserviceaccount.com\",\n" +
                    "  \"client_id\": \"109562202820549388247\",\n" +
                    "  \"auth_uri\": \"https://accounts.google.com/o/oauth2/auth\",\n" +
                    "  \"token_uri\": \"https://oauth2.googleapis.com/token\",\n" +
                    "  \"auth_provider_x509_cert_url\": \"https://www.googleapis.com/oauth2/v1/certs\",\n" +
                    "  \"client_x509_cert_url\": \"https://www.googleapis.com/robot/v1/metadata/x509/firebase-adminsdk-rp7sq%40nha-tro-57e88.iam.gserviceaccount.com\",\n" +
                    "  \"universe_domain\": \"googleapis.com\"\n" +
                    "}\n";

            InputStream stream = new ByteArrayInputStream(jsonString.getBytes(StandardCharsets.UTF_8));
            GoogleCredentials googleCredentials = GoogleCredentials.fromStream(stream).createScoped(Lists.newArrayList(firebaseMessagingScope));
            googleCredentials.refresh();
            return googleCredentials.getAccessToken().getTokenValue();

        }catch(IOException e){
            Log.e("error",""+ e.getMessage());
            return null;
        }
    }
}
