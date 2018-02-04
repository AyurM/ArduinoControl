package ru.ayurmar.arduinocontrol;


import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import ru.ayurmar.arduinocontrol.model.FarhomeUser;

public class LoginActivity extends BaseActivity implements
        View.OnClickListener {

    private static final String sLoginEmailIndex = "LOGIN_EMAIL_INDEX";
    private static final String sUsersRootPath = "users";
    private TextView mRegisterTextView;
    private TextView mExistingAccountTextView;
    private TextView mErrorTextView;
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
        mExistingAccountTextView = findViewById(R.id.use_existing_account_text_view);
        mErrorTextView = findViewById(R.id.login_error_text_view);
        mCreateAccountButton = findViewById(R.id.email_create_account_button);
        mSignInButton = findViewById(R.id.email_sign_in_button);

        if (savedInstanceState != null) {
            mEmailField.setText(savedInstanceState.getString(sLoginEmailIndex));
        }

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

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        super.onSaveInstanceState(savedInstanceState);
        String email = mEmailField.getText().toString();
        if(!TextUtils.isEmpty(email)) {
            savedInstanceState.putString(sLoginEmailIndex, email);
        }
    }

    private void createAccount(String email, String password) {
        if (!validateForm()) {
            return;
        }

        if(!Utils.isOnline(this)){
            showNoConnectionError();
            return;
        }

        mErrorTextView.setVisibility(View.GONE);
        showProgressDialog();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    hideProgressDialog();
                    if (task.isSuccessful()) {
                        sendEmailVerification();
                    } else {
                        //Ошибка при создании аккаунта
                        Exception exception = task.getException();
                        if(exception instanceof FirebaseAuthUserCollisionException){
                            mErrorTextView.setText(R.string.login_account_exists);
                        } else if(exception instanceof FirebaseAuthWeakPasswordException){
                            mErrorTextView.setText(R.string.login_weak_password);
                        } else {
                            mErrorTextView.setText(R.string.login_auth_failed);
                        }
                        mErrorTextView.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void signIn(String email, String password) {
        if (!validateForm()) {
            return;
        }

        if(!Utils.isOnline(this)){
            showNoConnectionError();
            return;
        }

        mErrorTextView.setVisibility(View.GONE);
        showProgressDialog();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    hideProgressDialog();
                    if (task.isSuccessful()) {
                        onSignInSuccess();
                    } else {
                        //Ошибка при входе в учетную запись
                        Exception exception = task.getException();
                        if(exception instanceof FirebaseAuthInvalidUserException){
                            mErrorTextView.setText(R.string.login_invalid_email);
                        } else if(exception instanceof FirebaseAuthInvalidCredentialsException){
                            mErrorTextView.setText(R.string.login_wrong_email_password);
                        } else {
                            mErrorTextView.setText(R.string.login_auth_failed);
                        }
                        mErrorTextView.setVisibility(View.VISIBLE);
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
                        addNewUserToDatabase();
                    } else {
                        mErrorTextView.setText(R.string.login_verification_email_failed);
                        mErrorTextView.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void addNewUserToDatabase(){
        //должно выполняться только один раз при регистрации
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if(firebaseUser != null){
            FarhomeUser newUser = new FarhomeUser(firebaseUser.getEmail());
            DatabaseReference usersRef = FirebaseDatabase.getInstance()
                    .getReference(sUsersRootPath);
            usersRef.child(firebaseUser.getUid()).setValue(newUser);
        }
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
                mConfirmPasswordField.setError(null);
            }
        }

        return valid;
    }

    private void showCreateAccountUI(boolean isCreateAccountUIVisible){
        mErrorTextView.setVisibility(View.GONE);
        mRegisterTextView.setVisibility(isCreateAccountUIVisible ? View.GONE : View.VISIBLE);
        mExistingAccountTextView.setVisibility(isCreateAccountUIVisible ? View.VISIBLE : View.GONE);
        mConfirmPasswordField.setVisibility(isCreateAccountUIVisible ? View.VISIBLE : View.GONE);
        mCreateAccountButton.setVisibility(isCreateAccountUIVisible ? View.VISIBLE : View.GONE);
        mSignInButton.setVisibility(isCreateAccountUIVisible ? View.GONE : View.VISIBLE);
    }

    private void showNoConnectionError(){
        mErrorTextView.setText(R.string.message_no_connection_text);
        mErrorTextView.setVisibility(View.VISIBLE);
    }

    private void onSignInSuccess(){
        //Действия при успешном входе с помощью e-mail и пароля
        if(mAuth.getCurrentUser().isEmailVerified()){
            mErrorTextView.setVisibility(View.GONE);
            Intent i = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(i);
            finish();
        } else {
            mEmailField.setText(mAuth.getCurrentUser().getEmail());
            mErrorTextView.setText(R.string.login_verify_email);
            mErrorTextView.setVisibility(View.VISIBLE);
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