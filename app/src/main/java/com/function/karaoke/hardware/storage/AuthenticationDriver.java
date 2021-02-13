package com.function.karaoke.hardware.storage;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * A driver for dealing with FireBase authentication
 */
public class AuthenticationDriver {
    private final FirebaseAuth auth;

    public AuthenticationDriver() {
        this.auth = FirebaseAuth.getInstance();

        // Enable Firestore logging
        FirebaseFirestore.setLoggingEnabled(true);
    }

    public boolean isSignIn() {
        return auth.getCurrentUser() != null;
    }

    public FirebaseAuth getAuth() {
        return this.auth;
    }

    public String getUserUid() {
        return auth.getUid();
    }

    public String getUserEmail() {
        return auth.getCurrentUser().getEmail();
    }

    public void signOut() {
        auth.signOut();
    }

    public void createGuestId(OnCompleteListener<AuthResult> listener) {
        auth.signInAnonymously()
                .addOnCompleteListener(listener);

    }

}
