package ru.ayurmar.arduinocontrol.services;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import java.util.HashMap;
import java.util.Map;

import durdinapps.rxfirebase2.RxFirebaseDatabase;
import ru.ayurmar.arduinocontrol.model.DatabasePaths;


public class FarhomeInstanceIDService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(refreshedToken);
    }

    private void sendRegistrationToServer(String token){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null){
            Map<String, Object> tokenUpdate = new HashMap<>();
            tokenUpdate.put(DatabasePaths.USERS + "/" + user.getUid() + "/fcmtoken",
                    token);
            RxFirebaseDatabase.updateChildren(FirebaseDatabase.getInstance()
                    .getReference(), tokenUpdate)
                    .subscribe();
        }
    }
}
