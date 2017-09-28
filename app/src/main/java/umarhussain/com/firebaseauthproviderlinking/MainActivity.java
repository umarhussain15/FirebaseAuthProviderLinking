package umarhussain.com.firebaseauthproviderlinking;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.ProviderQueryResult;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 644;

    private static final String TAG = "Main Activity";

    @BindView(R.id.buttonEmail)
    Button buttonEmail;

    @BindView(R.id.buttonFacebook)
    Button buttonFacebook;

    @BindView(R.id.buttonGoogle)
    Button buttonGoogle;

    @BindView(R.id.editTextName)
    EditText editTextName;

    @BindView(R.id.editTextEmail)
    EditText editTextEmail;

    @BindView(R.id.editTextPassword)
    EditText editTextPassword;

    private GoogleApiClient mGoogleApiClient;

    private CallbackManager mCallbackManager;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();
        if( firebaseAuth.getCurrentUser()!=  null){
            onFireBaseAuthSuccess(firebaseAuth.getCurrentUser());
        }
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .requestIdToken(getString(R.string.default_web_client_id))
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        mGoogleApiClient.connect();

        // facebook setup
        mCallbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess");
                Log.e(TAG, "granted permissions: " + Arrays.toString(loginResult.getRecentlyGrantedPermissions().toArray()));
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
            }
        });

    }

    @OnClick({R.id.buttonEmail, R.id.buttonFacebook, R.id.buttonGoogle})
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.buttonEmail:
                if (editTextEmail.getText().toString().isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(editTextEmail.getText().toString()).matches()) {
                    Toast.makeText(MainActivity.this, "Invalid Email",
                            Toast.LENGTH_SHORT).show();
                } else if (editTextPassword.getText().toString().isEmpty() || editTextPassword.getText().toString().length() < 6) {
                    Toast.makeText(MainActivity.this, "Password length less than 6 characters",
                            Toast.LENGTH_SHORT).show();
                } else {
                    firebaseAuth.fetchProvidersForEmail(editTextEmail.getText().toString()).addOnCompleteListener(new OnCompleteListener<ProviderQueryResult>() {
                        @Override
                        public void onComplete(@NonNull Task<ProviderQueryResult> task) {
                            if (task.isSuccessful()) {
                                if (task.getResult().getProviders().isEmpty() || !task.getResult().getProviders().contains(EmailAuthProvider.PROVIDER_ID)) {
                                    firebaseAuth
                                            .createUserWithEmailAndPassword(editTextEmail.getText().toString(),
                                                    editTextPassword.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull final Task<AuthResult> task) {
                                            if (task.isSuccessful()) {
                                                if( !editTextName.getText().toString().isEmpty()){
                                                    UserProfileChangeRequest changeRequest= new UserProfileChangeRequest
                                                            .Builder()
                                                            .setDisplayName(editTextName.getText().toString()).build();
                                                    task.getResult().getUser().updateProfile(changeRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> taskNameChange) {
                                                                if (task.isSuccessful()){
                                                                    onFireBaseAuthSuccess(task.getResult().getUser());
                                                                }
                                                        }
                                                    });
                                                }
                                                else
                                                onFireBaseAuthSuccess(task.getResult().getUser());
                                            }
                                        }
                                    });
                                }
                                else {
                                    AuthCredential authCredential= EmailAuthProvider.getCredential(editTextEmail.getText().toString(),
                                            editTextPassword.getText().toString());
                                    firebaseAuth.signInWithCredential(authCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull final Task<AuthResult> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(MainActivity.this, "Success",
                                                        Toast.LENGTH_LONG).show();
                                                    onFireBaseAuthSuccess(task.getResult().getUser());
                                            }
                                            else{
                                                Toast.makeText(MainActivity.this, task.getException().toString(),
                                                        Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });
                                }
                            }
                        }
                    });
                }
                break;
            case R.id.buttonFacebook:
                LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email", "public_profile"));
                break;
            case R.id.buttonGoogle:
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                startActivityForResult(signInIntent, RC_SIGN_IN);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        } else
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess() + ". " + result.getStatus().toString());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount account = result.getSignInAccount();
            firebaseAuthWithGoogle(account);
        } else {
            Log.d(TAG, "handleSignInResult:" + result.getStatus().toString());
        }
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, token.getToken());
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = firebaseAuth.getCurrentUser();

                            onFireBaseAuthSuccess(user);
                        } else {
                            Log.w(TAG, "signInWithCredential:failure", task.getException());

                            LoginManager.getInstance().logOut();

                        }
                    }
                });
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");

                            onFireBaseAuthSuccess(firebaseAuth.getCurrentUser());
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed: " + task.getException(),
                                    Toast.LENGTH_SHORT).show();

                            Auth.GoogleSignInApi.signOut(mGoogleApiClient);

                        }

                    }
                });
    }

    private void onFireBaseAuthSuccess(FirebaseUser currentUser) {
        currentUser.reload();
        startActivity(new Intent(MainActivity.this,LoggedIn.class));
        finish();
    }
}
