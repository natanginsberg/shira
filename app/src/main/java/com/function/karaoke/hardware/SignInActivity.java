package com.function.karaoke.hardware;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.function.karaoke.hardware.activities.Model.SignInViewModel;
import com.function.karaoke.hardware.activities.Model.UserInfo;
import com.function.karaoke.hardware.activities.Model.enums.LoginState;
import com.function.karaoke.hardware.storage.AuthenticationDriver;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;

import java.util.Locale;

public class SignInActivity extends AppCompatActivity {


    private static final int RC_SIGN_IN = 101;
    private static final String RESULT_CODE = "code";
    private static final String SING_ACTIVITY = "sing activity";
    private static final String CALLBACK = "callback";
    private GoogleSignInClient mGoogleSignInClient;
    private AuthenticationDriver authenticationDriver;
    private UserInfo user;
    private Observer<Boolean> gettingNewUserSucceeded;
    private SignInViewModel signInViewModel;
    private int code = -1;
    private boolean callback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getCorrectLanguage();
        extractIntentParams();
        showPromo();
    }


    private void getCorrectLanguage() {
        if (getIntent().getExtras() != null && getIntent().getExtras().containsKey("language")) {
            String phoneLanguage = Locale.getDefault().getLanguage();
            setLocale(phoneLanguage);
        }
    }

    private void setLocale(String lang) {

        Locale myLocale = new Locale(lang);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.setLocale(myLocale);
        res.updateConfiguration(conf, dm);
    }


    private void extractIntentParams() {
        if (getIntent().getExtras() != null && getIntent().getExtras().containsKey(CALLBACK)) {
            callback = true;
        }
    }


    private void showPromo() {
        setContentView(R.layout.promo);
        setTimer();
    }

    private void setTimer() {
        new CountDownTimer(500, 500) {

            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                startMainActivity();
            }
        }.start();
    }

    private void startMainActivity() {
        Intent intent = new Intent(this, SongsActivity.class);
        startActivity(intent);
    }

    private void initiateActivity() {
        extractCodeExtrasIfExist();
        authenticationDriver = new AuthenticationDriver();
        checkForSignedInUser();
    }


    private void checkForSignedInUser() {
        if (authenticationDriver.isSignIn() && authenticationDriver.getUserEmail() != null &&
                authenticationDriver.getUserUid() != null && !authenticationDriver.getUserEmail().equals("")) {
            Intent intent = new Intent(this, SongsActivity.class);
            startActivity(intent);
        } else
            openSignIn();
    }

    private void openSignIn() {
        signInViewModel = ViewModelProviders.of(this).get(SignInViewModel.class);
        setContentView(R.layout.activity_sign_in);
        checkIfGuestIsPermitted();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("701925650903-cmh7htvq718jbea95a0ukbkmlttqb56b.apps.googleusercontent.com")
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        mGoogleSignInClient.signOut();
        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);

        findViewById(R.id.sign_out_button).setOnClickListener(view -> signIn());

        setUpGettingNewUserSucceeded();
        signInViewModel.getUserFromDatabase().observe(this, gettingNewUserSucceeded);
    }

    private void checkIfGuestIsPermitted() {
        if (getIntent().getExtras() != null && getIntent().getExtras().containsKey(SING_ACTIVITY))
            findViewById(R.id.guest).setVisibility(View.INVISIBLE);
    }

    private void extractCodeExtrasIfExist() {
        if (getIntent().getExtras() != null && getIntent().getExtras().containsKey(RESULT_CODE)) {
            code = getIntent().getIntExtra(RESULT_CODE, 0);
        }
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.

            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
//            throw new RuntimeException("the app got here  ");
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            signInViewModel.firebaseAuthWithGoogle(account.getIdToken(), new SignInViewModel.FirebaseAuthListener() {
                @Override
                public void onSuccess(FirebaseUser firebaseUser) {
                    signInViewModel.isUserInDatabase(new SignInViewModel.DatabaseListener() {

                        @Override
                        public void isInDatabase(boolean inDatabase) {
                            if (inDatabase) {
                                user = signInViewModel.getUser();
                            } else {
                                user = new UserInfo(firebaseUser.getEmail(),
                                        firebaseUser.getDisplayName(),
                                        firebaseUser.getPhotoUrl().toString(),
                                        firebaseUser.getUid(), 0, 0);

                                signInViewModel.addNewUserToDatabase(user);
                            }
                            returnToMain();
                        }

                        @Override
                        public void failedToSearchDatabase() {
                            user = new UserInfo(firebaseUser.getEmail(),
                                    firebaseUser.getDisplayName(),
                                    firebaseUser.getPhotoUrl().toString(),
                                    firebaseUser.getUid(), 0, 0);

                            signInViewModel.addNewUserToDatabase(user);
                        }


                    });
                }

                @Override
                public void onFailure() {
                    Toast.makeText(getBaseContext(), getString(R.string.auth_failed), Toast.LENGTH_LONG).show();
                    finish();

                }
            });
//
//            signInViewModel.addNewUserToDatabase();


        } catch (Exception e) {
            Toast.makeText(this, getResources().getString(R.string.sign_in_error), Toast.LENGTH_SHORT).show();
        }
    }

    private void returnToMain() {
        Intent intent = new Intent(this, SongsActivity.class);
        intent.putExtra("User", user);
//            onActivityResult(0, RESULT_OK, intent);
        if (code != -1)
            setResult(code, intent);
        else
            setResult(RESULT_OK, intent);
        if (callback)
            finish();
        else
            startActivity(intent);
    }

    private void setUpGettingNewUserSucceeded() {
        gettingNewUserSucceeded = (Boolean success) -> {
            if (success) {
                if ((user = signInViewModel.getUser()) != null) {
                    signInViewModel.setLoginState(LoginState.FINISH);
                    findViewById(R.id.sign_out_button).setVisibility(View.INVISIBLE);
//                    returnToMain();
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


    public void continueAsGuest(View view) {
        if (authenticationDriver.isSignIn()) {
            openMain();
        } else
            signInViewModel.createGuestId(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        openMain();
                    } else {
                        makeToastForError();
                    }
                }
            });

    }

    private void openMain() {
        Intent intent = new Intent(this, SongsActivity.class);
        startActivity(intent);
    }

    private void makeToastForError() {
        Toast.makeText(this, getString(R.string.error_from_the_beginning_of_sign_in), Toast.LENGTH_LONG).show();
    }

}