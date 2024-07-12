package edu.poly.nhtr.firebase;

import android.content.Context;

import androidx.annotation.NonNull;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import edu.poly.nhtr.models.Home;

public class FcmNotificationSender {

    private final String userFcmToken;
    private final String title;
    private final String body;
    private final Context context;

    private final String postUrl = "https://fcm.googleapis.com/v1/projects/nha-tro-57e88/messages:send";

    public FcmNotificationSender(String userFcmToken, String title, String body, Context context, Home home, String notificationID) {
        this.userFcmToken = userFcmToken;
        this.title = title;
        this.body = body;
        this.context = context;

    }

    public void SendNotifications(){
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        JSONObject mainObj = new JSONObject();
        try{
            JSONObject messageObject = new JSONObject();

            // Phần data payload
            JSONObject dataObject = new JSONObject();
            dataObject.put("title", title);
            dataObject.put("body", body);

            messageObject.put("token", userFcmToken);
            messageObject.put("data", dataObject);  // Sử dụng phần data thay vì notification

            mainObj.put("message", messageObject);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, postUrl, mainObj, response -> {
                // Code run got response
            }, volleyError -> {
                // Code run error
            }) {
                @NonNull
                @Override
                public Map<String, String> getHeaders(){
                    AccessToken accessToken = new AccessToken();
                    String accessKey = accessToken.getAccessToken();
                    Map<String, String> header = new HashMap<>();
                    header.put("content-type", "application/json");
                    header.put("authorization", "Bearer "+ accessKey);
                    return header;
                }
            };

            requestQueue.add(request);

        } catch (JSONException e){
            e.printStackTrace();
        }
    }
}
