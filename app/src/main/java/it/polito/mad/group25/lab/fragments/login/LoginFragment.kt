package it.polito.mad.group25.lab.fragments.login


import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.identity.GetSignInIntentRequest
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import it.polito.mad.group25.lab.R

class LoginFragment: Fragment() {

    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private val TAG = "LoginFragment"
    private lateinit var mAuth: FirebaseAuth
    private val REQUEST_CODE_GOOGLE_SIGN_IN = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }
    override fun onCreateView(
        inflater: LayoutInflater, container:  ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.login_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val signInButton = view.findViewById<SignInButton>(R.id.sign_in_button)
        mAuth = FirebaseAuth.getInstance()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build()
        mGoogleSignInClient = GoogleSignIn.getClient(activity as Activity, gso)

        signInButton.setOnClickListener {
            signIn()
        }
    }

    private val getGoogleDataBack =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult(),
            ActivityResultCallback {
                if (it.resultCode == REQUEST_CODE_GOOGLE_SIGN_IN) {
                    Log.d(TAG, "------------------ACTIVITY START CALLBACK--------------------: ")
                    val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)
                    var userToken: String? = null

                    try {
                        var account = task.getResult(ApiException::class.java)
                        if (account != null) {
                            userToken = account.idToken
                            Log.d(TAG, "----------------------------$userToken: ")
                            FirebaseGoogleAuth(account)
                        } else Log.d(TAG, "ACCOUNT IS NULL: ")
                    } catch (e: Exception) {
                        Log.d(TAG, ":FAILED TO GET USER ${e.message}")
                    }

                    //viewModel.handleEvent(LoginEvent.OnGoogleSignInResult(LoginResult(123, userToken!!)))
                }
                else{
                    Log.d(TAG, "------------------RESULT ISN'T OKAY ${it.resultCode}  ----------------: ")
                }
            })

    private fun signIn() {
        val signInIntent = mGoogleSignInClient.signInIntent
        getGoogleDataBack.launch(signInIntent)
    }
    private fun FirebaseGoogleAuth(account: GoogleSignInAccount) {
        val authCredential = GoogleAuthProvider.getCredential(account.idToken, null)
        mAuth.signInWithCredential(authCredential)
            .addOnCompleteListener( activity as Activity, OnCompleteListener<AuthResult>() {
                fun onComplete(task: Task<AuthResult>){
                    if(task.isSuccessful){
                        Log.d(TAG, "Successfull Firebase Auth !!!")
                    } else {
                        Log.d(TAG, "UnSuccessfull Firebase Auth !!!")
                    }
                }
            })
    }
    /*private val RC_SIGN_IN = 7
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var resultLauncer: ActivityResultLauncher<Intent>
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient((activity) as Activity, gso)
        auth = Firebase.auth
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.login_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<SignInButton>(R.id.sign_in_button)
            .setOnClickListener {
                signIn()
            }
        resultLauncer = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if(result.resultCode == Activity.RESULT_OK){
                //booooooooooh
                //if (result.resultCode == RC_SIGN_IN) {
                    val data: Intent? = result.data
                    val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
                    handleSignInResult(task)
                // }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val account = GoogleSignIn.getLastSignedInAccount(context)
        account?.let{
            Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
            //utente già loggato, fai quello che devi
        }
    }

    *//*=======================================================
    =======================================================*//*
    private fun signIn(){
        val signInIntent = mGoogleSignInClient.signInIntent
        resultLauncer.launch(signInIntent)
    }
    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount> ) {
        try {
            val account = completedTask.getResult(ApiException::class.java)!!
            Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
            firebaseAuthWithGoogle(account.idToken!!)
        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            Log.w(TAG, "signInResult:failed code=" + e.statusCode)
            Toast.makeText(this.context, "log with google failed", Toast.LENGTH_SHORT).show()
            //updateUI(null)
        }
    }
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(activity as Activity) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                    //utente loggato, fai quello che devi
                    Toast.makeText(this.context, "Utente: ${user.displayName}", Toast.LENGTH_LONG).show()
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    //utente non loggato, fai quello che devi
                }
            }
    }*/
}