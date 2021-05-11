package it.polito.mad.group25.lab.fragments.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import it.polito.mad.group25.lab.R
import it.polito.mad.group25.lab.fragments.trip.list.TripListViewModel

class LoginFragment: Fragment(R.layout.login_fragment) {

    private lateinit var mGoogleSignInClient : GoogleSignInClient
    private lateinit var resultLauncher : ActivityResultLauncher<Intent>
    private lateinit var auth: FirebaseAuth
    private val tripListViewModel: TripListViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(requireContext(),gso)

        auth = Firebase.auth

        resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ res ->
            if(res.resultCode == Activity.RESULT_OK) {
                var task = GoogleSignIn.getSignedInAccountFromIntent(res.data)
                handleSignInResult(task)
            }
        }
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<SignInButton>(R.id.signInButton)
            .setOnClickListener { signIn() }
    }

    override fun onStart() {
        super.onStart()
        //check for already signed in users
        val currentUser = auth.currentUser
        currentUser?.let {
            tripListViewModel.userId = currentUser.uid
            //tripListViewModel.userId = "A"
            //tripListViewModel.userId = "B"
            activity?.findNavController(R.id.nav_host_fragment_content_main)
                ?.navigate(R.id.action_LoginFragment_to_OthersTripListFragment)
        }
    }

    private fun signIn(){
        var intent = mGoogleSignInClient.signInIntent
        resultLauncher.launch(intent)
    }

    private fun handleSignInResult(task: Task<GoogleSignInAccount>) {
        try {
            //google signIn success
            val account = task.result
            firebaseAuthWithGoogle(account?.idToken!!)
        } catch (e: ApiException) {
            //google signIn failed
            Log.w("signIn", "signInResult:failed code=" + e.statusCode)
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String){
        val credentials= GoogleAuthProvider.getCredential(idToken,null)
        auth.signInWithCredential(credentials)
            .addOnCompleteListener { task ->
                if(task.isSuccessful){
                    //success
                    val user = auth.currentUser
                    tripListViewModel.userId = user.uid
                    activity?.findNavController(R.id.nav_host_fragment_content_main)
                        ?.navigate(R.id.action_LoginFragment_to_OthersTripListFragment)
                }
            }
    }
}