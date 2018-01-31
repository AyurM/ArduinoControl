package ru.ayurmar.arduinocontrol;


import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends BaseActivity implements
        View.OnClickListener {

    private static final String TAG = "LoginActivity";

    private TextView mRegisterTextView;
    private TextView mExistingAccountTextView;
    private EditText mEmailField;
    private EditText mPasswordField;
    private EditText mConfirmPasswordField;
    private Button mCreateAccountButton;
    private Button mSignInButton;

    private FirebaseAuth mAuth;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        mEmailField = findViewById(R.id.field_email);
        mPasswordField = findViewById(R.id.field_password);
        mConfirmPasswordField = findViewById(R.id.field_confirm_password);
        mRegisterTextView = findViewById(R.id.register_text_view);
        mCreateAccountButton = findViewById(R.id.email_create_account_button);
        mSignInButton = findViewById(R.id.email_sign_in_button);
        mExistingAccountTextView = findViewById(R.id.use_existing_account_text_view);

        //подчеркнутый текст
        mRegisterTextView.setPaintFlags(mRegisterTextView.getPaintFlags()
                | Paint.UNDERLINE_TEXT_FLAG);
        mExistingAccountTextView.setPaintFlags(mExistingAccountTextView.getPaintFlags()
                | Paint.UNDERLINE_TEXT_FLAG);

        mSignInButton.setOnClickListener(this);
        mCreateAccountButton.setOnClickListener(this);
        mRegisterTextView.setOnClickListener(this);
        mExistingAccountTextView.setOnClickListener(this);

        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null)
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            onSignInSuccess();
        }
    }

    private void createAccount(String email, String password) {
        Log.d(TAG, "createAccount: " + email);
        if (!validateForm()) {
            return;
        }

        showProgressDialog();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    hideProgressDialog();
                    if (task.isSuccessful()) {
                        // Sign in success
                        Log.d(TAG, "createUserWithEmail: success");
                        sendEmailVerification();
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "createUserWithEmail: failure", task.getException());
                        Toast.makeText(LoginActivity.this, R.string.login_auth_failed,
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void signIn(String email, String password) {
        Log.d(TAG, "signIn: " + email);
        if (!validateForm()) {
            return;
        }

        showProgressDialog();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    hideProgressDialog();
                    if (task.isSuccessful()) {
                        onSignInSuccess();
                    } else {
                        showWrongEmailPassUI(task);
                    }
                });
    }

    private void sendEmailVerification() {
        FirebaseUser user = mAuth.getCurrentUser();
        user.sendEmailVerification()
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this,
                                getString(R.string.login_verification_email_sent)
                                        + " " + user.getEmail(),
                                Toast.LENGTH_LONG).show();
                        showCreateAccountUI(false);
                    } else {
                        Log.e(TAG, "sendEmailVerification", task.getException());
                        Toast.makeText(LoginActivity.this,
                                R.string.login_verification_email_failed,
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private boolean validateForm() {
        boolean valid = true;

        String email = mEmailField.getText().toString();
        if (TextUtils.isEmpty(email)) {
            mEmailField.setError(getString(R.string.login_empty_field_warning));
            valid = false;
        } else {
            mEmailField.setError(null);
        }

        String password = mPasswordField.getText().toString();
        if (TextUtils.isEmpty(password)) {
            mPasswordField.setError(getString(R.string.login_empty_field_warning));
            valid = false;
        } else {
            mPasswordField.setError(null);
        }

        if(mConfirmPasswordField.getVisibility() == View.VISIBLE){
            String passwordConfirmation = mConfirmPasswordField.getText().toString();
            if(!password.equals(passwordConfirmation)){
                mConfirmPasswordField.setError(getString(R.string.login_passwords_mismatch));
                valid = false;
            } else {
                mPasswordField.setError(null);
            }
        }

        return valid;
    }

    private void showCreateAccountUI(boolean isCreateAccountUIVisible){
        mRegisterTextView.setVisibility(isCreateAccountUIVisible ? View.GONE : View.VISIBLE);
        mExistingAccountTextView.setVisibility(isCreateAccountUIVisible ? View.VISIBLE : View.GONE);
        mConfirmPasswordField.setVisibility(isCreateAccountUIVisible ? View.VISIBLE : View.GONE);
        mCreateAccountButton.setVisibility(isCreateAccountUIVisible ? View.VISIBLE : View.GONE);
        mSignInButton.setVisibility(isCreateAccountUIVisible ? View.GONE : View.VISIBLE);
    }

    private void showWrongEmailPassUI(Task<AuthResult> task){
        //Действия при неудачной попытке входа
        Log.w(TAG, "signInWithEmail:failure", task.getException());
        Toast.makeText(LoginActivity.this, R.string.login_wrong_email_password,
                Toast.LENGTH_SHORT).show();
    }

    private void onSignInSuccess(){
        //Действия при успешном входе с помощью e-mail и пароля
//        Log.d(TAG, "signIn:success");
        if(mAuth.getCurrentUser().isEmailVerified()){
            Intent i = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(i);
            finish();
        } else {
            mEmailField.setText(mAuth.getCurrentUser().getEmail());
            Toast.makeText(LoginActivity.this, R.string.login_verify_email,
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.email_create_account_button){
            createAccount(mEmailField.getText().toString(), mPasswordField.getText().toString());
        } else if (i == R.id.email_sign_in_button){
            signIn(mEmailField.getText().toString(), mPasswordField.getText().toString());
        } else if (i == R.id.register_text_view){
            showCreateAccountUI(true);
        } else if (i == R.id.use_existing_account_text_view){
            showCreateAccountUI(false);
        }
    }
}