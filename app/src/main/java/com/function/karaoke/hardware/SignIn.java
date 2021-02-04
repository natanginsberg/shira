package com.function.karaoke.hardware;

import android.content.Context;
import android.content.Intent;

import androidx.activity.result.ActivityResultLauncher;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.function.karaoke.hardware.activities.Model.SignInViewModel;
import com.function.karaoke.hardware.activities.Model.UserInfo;
import com.function.karaoke.hardware.activities.Model.enums.LoginState;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

public class SignIn {

    private final Context context;
    private final FragmentActivity activity;
    private final LifecycleOwner lifeCycleOwner;
    private SignInViewModel signInViewModel;
    private GoogleSignInClient mGoogleSignInClient;
    private Observer<Boolean> gettingNewUserSucceeded;
    private final ActivityResultLauncher<Intent> mGetContent;
    private UserInfo user;

    public SignIn(FragmentActivity activity, Context context, LifecycleOwner lifecycleOwner, ActivityResultLauncher<Intent> mGetContent) {
        this.activity = activity;
        this.context = context;
        this.lifeCycleOwner = lifecycleOwner;
        this.mGetContent = mGetContent;
    }

    public void openSignIn() {


        signInViewModel = ViewModelProviders.of(activity).

                get(SignInViewModel.class);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("701925650903-cmh7htvq718jbea95a0ukbkmlttqb56b.apps.googleusercontent.com")
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(context, gso);

        mGoogleSignInClient.signOut();
        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(context);

        setUpGettingNewUserSucceeded();
        signIn();
//        signInViewModel.getUserFromDatabase().observe(lifeCycleOwner, gettingNewUserSucceeded);
    }

    private void setUpGettingNewUserSucceeded() {
        gettingNewUserSucceeded = (Boolean success) -> {
            if (success) {
                if ((user = signInViewModel.getUser()) != null) {
                    signInViewModel.setLoginState(LoginState.FINISH);
                } else {
                    signInViewModel.createNewUser();
                    signInViewModel.setLoginState(LoginState.NEW_USER_SIGN_UP);
                }
            } else {
                signInViewModel.setLoginState(LoginState.NOT_SIGN_IN);
            }
//            updateAccordingToLoginState();
        };
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
//        activity.startActivityForResult(signInIntent, RC_SIGN_IN);
        mGetContent.launch(signInIntent);

    }


}
