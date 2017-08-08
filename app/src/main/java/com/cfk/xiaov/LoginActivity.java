package com.cfk.xiaov;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.tencent.common.AccountMgr;
import com.tencent.ilivesdk.ILiveCallBack;
import com.tencent.ilivesdk.ILiveSDK;
import com.tencent.ilivesdk.core.ILiveLoginManager;
import com.tencent.model.MySelfInfo;


/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements OnClickListener {

    String TAG = getClass().getSimpleName();
    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private EditText loginAccountView, registerAccountView;
    private EditText loginPasswordView, registerPasswordView;
    private Button buttonSignIn, buttonSignUp;
    private View mProgressView;
    private View mLoginFormView;
    LinearLayout registerView;
    LinearLayout loginView;
    Button buttonForgetPassword;
    boolean viewSwitch;
    Toolbar mToolbar;
    private boolean bTLSAccount = true; // 默认为托管模式，与iOS一致
    private AccountMgr mAccountMgr = new AccountMgr();
    private boolean bLogin = false; // 记录登录状态
    private String userId;

    private void initView() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar_main_page);
        mToolbar.setTitle(R.string.toolbar_login);
        mToolbar.setTitleTextColor(getResources().getColor(R.color.colorWhite));
        setSupportActionBar(mToolbar);
        // Set up the login form.
        loginAccountView = (EditText) findViewById(R.id.login_account);
        loginPasswordView = (EditText) findViewById(R.id.login_password);
        registerAccountView = (EditText) findViewById(R.id.register_account);
        registerPasswordView = (EditText) findViewById(R.id.register_password);

        registerView = (LinearLayout) findViewById(R.id.register_form);
        loginView = (LinearLayout) findViewById(R.id.login_form);

        registerView.setVisibility(View.INVISIBLE);
        loginView.setVisibility(View.VISIBLE);


//        loginPasswordView = (EditText) findViewById(R.id.login_password);
//        loginPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
//                if (id == R.id.login || id == EditorInfo.IME_NULL) {
//                    attemptLogin();
//                    return true;
//                }
//                return false;
//            }
//        });

//        Button mSignInButton = (Button) findViewById(R.id.sign_in_button);
//        mSignInButton.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                attemptLogin();
//                startActivity(new Intent(LoginActivity.this,MainActivity.class));
//            }
//        });

        mLoginFormView = findViewById(R.id.all_login_form);
        mProgressView = findViewById(R.id.login_progress);

        buttonForgetPassword = (Button) findViewById(R.id.forget_password);
        buttonForgetPassword.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(LoginActivity.this, "Forget Password", Toast.LENGTH_SHORT).show();
            }
        });
        buttonSignIn = (Button) findViewById(R.id.sign_in_button);
        buttonSignUp = (Button) findViewById(R.id.sign_up_button);
        buttonSignIn.setOnClickListener(this);
        buttonSignUp.setOnClickListener(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //TODO 初始化随心播
        if (bTLSAccount) {
            ILiveSDK.getInstance().initSdk(getApplicationContext(), 1400028285, 11818);
        } else {
            ILiveSDK.getInstance().initSdk(getApplicationContext(), 1400016949, 8002);
        }
        initView();
        MySelfInfo.getInstance().getCache(getApplicationContext());
        userId = MySelfInfo.getInstance().getId();
        if(userId!=null){
            loginSDK(userId,MySelfInfo.getInstance().getUserSig());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.back_login, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_switch:
                if (viewSwitch) {
                    item.setTitle(R.string.new_register);
                    registerView.setVisibility(View.INVISIBLE);
                    loginView.setVisibility(View.VISIBLE);
                    mToolbar.setTitle(R.string.toolbar_login);
                } else {
                    item.setTitle(R.string.menu_back_login);
                    registerView.setVisibility(View.VISIBLE);
                    loginView.setVisibility(View.INVISIBLE);
                    mToolbar.setTitle(R.string.toolbar_regist);
                }
                viewSwitch = !viewSwitch;
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        loginAccountView.setError(null);
        loginPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = loginAccountView.getText().toString();
        String password = loginPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            loginPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = loginPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            loginAccountView.setError(getString(R.string.error_field_required));
            focusView = loginAccountView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            loginAccountView.setError(getString(R.string.error_invalid_email));
            focusView = loginAccountView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    private void tryRegister() {

        registerAccountView.setError(null);
        registerPasswordView.setError(null);

        final String userName = registerAccountView.getText().toString();
        final String password = registerPasswordView.getText().toString();

        boolean cancel = false;
        View focusAfter = null;

        if (TextUtils.isEmpty(userName) || !isValidUserName(userName)) {
            registerAccountView.setError(getString(R.string.tip_hit_account));
            focusAfter = registerAccountView;
            cancel = true;
        } else if (TextUtils.isEmpty(password) || !isValidPassword(password)) {
            registerPasswordView.setError(getString(R.string.tip_hit_password));
            focusAfter = registerPasswordView;
            cancel = true;
        }

        if (cancel) {
            focusAfter.requestFocus();
        } else {
            regist(registerAccountView.getText().toString(), registerPasswordView.getText().toString());
        }
    }

    private boolean isValidPassword(String password) {
        return password.length() >= 8 && password.length() <= 16 && password.matches("^[A-Za-z0-9]*$");
    }

    private boolean isValidUserName(String userName) {
        return userName.length() >= 4 &&
                userName.length() <= 24 &&
                userName.matches("^[A-Za-z0-9]*[A-Za-z][A-Za-z0-9]*$");
    }


    private void tryLogin() {

        loginAccountView.setError(null);
        loginPasswordView.setError(null);

        final String userName = loginAccountView.getText().toString();
        final String password = loginPasswordView.getText().toString();

        boolean cancel = false;
        View focusAfter = null;

        if (TextUtils.isEmpty(userName)) {
            loginAccountView.setError("用户名不可为空");
            focusAfter = loginAccountView;
            cancel = true;
        } else if (TextUtils.isEmpty(password)) {
            loginPasswordView.setError("密码不可为空");
            focusAfter = loginPasswordView;
            cancel = true;
        }

        if (cancel) {
            focusAfter.requestFocus();
        } else {
            login(loginAccountView.getText().toString(), loginPasswordView.getText().toString());
        }
    }

    /**
     * 使用userSig登录iLiveSDK(独立模式下获有userSig直接调用登录)
     */
    private void loginSDK(final String id, final String userSig) {
        ILiveLoginManager.getInstance().iLiveLogin(id, userSig, new ILiveCallBack() {
            @Override
            public void onSuccess(Object data) {
                bLogin = true;
                Log.i(TAG, "Login CallSDK success:" + id);
                MySelfInfo.getInstance().setId(ILiveLoginManager.getInstance().getMyUserId());
                MySelfInfo.getInstance().setUserSig(userSig);
                MySelfInfo.getInstance().writeToCache(getApplicationContext());
                startService(new Intent(LoginActivity.this,VideoService.class));
                startActivity(new Intent(LoginActivity.this,MainActivity.class));
                finish();
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
                Toast.makeText(LoginActivity.this, "Login failed:" + module + "|" + errCode + "|" + errMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 登录并获取userSig(*托管模式，独立模式下直接用userSig调用loginSDK登录)
     */
    private void login(final String id, String password) {

        if (bTLSAccount) {
            ILiveLoginManager.getInstance().tlsLogin(id, password, new ILiveCallBack<String>() {
                @Override
                public void onSuccess(String data) {
                    loginSDK(id, data);
                }

                @Override
                public void onError(String module, int errCode, String errMsg) {
                    Toast.makeText(getApplicationContext(), "login failed:" + module + "|" + errCode + "|" + errMsg, Toast.LENGTH_SHORT).show();

                }
            });
        } else {
            mAccountMgr.login(id, password, new AccountMgr.RequestCallBack() {
                @Override
                public void onResult(int error, String response) {
                    if (0 == error) {
                        loginSDK(id, response);
                    } else {
                        Toast.makeText(getApplicationContext(), "login failed:" + response, Toast.LENGTH_SHORT).show();

                    }
                }
            });
        }
    }

    /**
     * 注册用户名(*托管模式，独立模式下请向自己私有服务器注册)
     */
    private void regist(String account, String password) {
        if (bTLSAccount) {
            ILiveLoginManager.getInstance().tlsRegister(account, password, new ILiveCallBack() {
                @Override
                public void onSuccess(Object data) {
                    Toast.makeText(getApplicationContext(), "regist success!", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(String module, int errCode, String errMsg) {
                    Toast.makeText(getApplicationContext(), "regist failed:" + module + "|" + errCode + "|" + errMsg, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            mAccountMgr.regist(account, password, new AccountMgr.RequestCallBack() {
                @Override
                public void onResult(int error, String response) {
                    Toast.makeText(getApplicationContext(), response, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sign_in_button:
                tryLogin();
                break;
            case R.id.sign_up_button:
                tryRegister();
                break;
            default:
                break;
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            try {
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }

            for (String credential : DUMMY_CREDENTIALS) {
                String[] pieces = credential.split(":");
                if (pieces[0].equals(mEmail)) {
                    // Account exists, return true if the password matches.
                    return pieces[1].equals(mPassword);
                }
            }

            // TODO: register the new account here.
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                finish();
            } else {
                loginPasswordView.setError(getString(R.string.error_incorrect_password));
                loginPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}

