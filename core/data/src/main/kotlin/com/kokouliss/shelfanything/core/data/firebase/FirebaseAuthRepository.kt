package com.kokouliss.shelfanything.core.data.firebase

import android.util.Log
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.userProfileChangeRequest
import com.kokouliss.shelfanything.core.domain.model.User
import com.kokouliss.shelfanything.core.domain.repository.AuthRepository
import com.kokouliss.shelfanything.core.domain.repository.EmailAuthResult
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

internal class FirebaseAuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val items: FirestoreItemRepository,
) : AuthRepository {

    override val currentUser: Flow<User?> = auth.userFlow().map { user ->
        user?.let {
            User(
                id = it.uid,
                name = it.shownName,
                email = it.email,
                usesPassword = it.providerData.any { p -> p.providerId == EmailAuthProvider.PROVIDER_ID },
                emailVerified = it.isEmailVerified,
            )
        }
    }

    override suspend fun signInWithGoogle(idToken: String): Boolean = try {
        auth.signInWithCredential(GoogleAuthProvider.getCredential(idToken, null)).await()
        true
    } catch (e: FirebaseException) {
        Log.w(TAG, "Firebase rejected the Google sign-in", e)
        false
    }

    override suspend fun signInWithEmail(email: String, password: String) = emailAuth {
        auth.signInWithEmailAndPassword(email.trim(), password).await()
    }

    override suspend fun createAccount(name: String, email: String, password: String) = emailAuth {
        val user = checkNotNull(auth.createUserWithEmailAndPassword(email.trim(), password).await().user)
        user.updateProfile(userProfileChangeRequest { displayName = name.trim() }).await()
        // A new token makes the signed-in screens show the name instead of the start of the address.
        user.getIdToken(true).await()
        sendVerificationEmail()
    }

    override suspend fun sendPasswordReset(email: String) = emailAuth {
        auth.useAppLanguage()
        auth.sendPasswordResetEmail(email.trim()).await()
    }

    override suspend fun sendVerificationEmail(): Boolean = try {
        auth.useAppLanguage()
        auth.requireUser().sendEmailVerification().await()
        true
    } catch (e: FirebaseException) {
        Log.w(TAG, "Could not send the confirmation email", e)
        false
    }

    override suspend fun refreshEmailVerification() {
        val user = auth.currentUser?.takeIf { !it.isEmailVerified } ?: return
        try {
            user.reload().await()
            if (user.isEmailVerified) user.getIdToken(true).await()
        } catch (e: FirebaseException) {
            Log.w(TAG, "Could not check whether the email was confirmed", e)
        }
    }

    override suspend fun signOut() {
        auth.signOut()
    }

    override suspend fun deleteAccount(idToken: String): Boolean =
        deleteAccountAfter(GoogleAuthProvider.getCredential(idToken, null))

    override suspend fun deleteAccountWithPassword(password: String): Boolean {
        val email = auth.currentUser?.email ?: return false
        return deleteAccountAfter(EmailAuthProvider.getCredential(email, password))
    }

    private suspend fun deleteAccountAfter(proof: AuthCredential): Boolean = try {
        val user = auth.requireUser()
        // Firebase wants a fresh sign-in right before an account is deleted. First, so that being
        // offline, another Google account or a wrong password stops everything before the shelves go.
        user.reauthenticate(proof).await()
        items.deleteAll()
        user.delete().await()
        true
    } catch (e: FirebaseException) {
        Log.w(TAG, "Could not delete the account", e)
        false
    }

    private suspend fun emailAuth(block: suspend () -> Unit): EmailAuthResult = try {
        block()
        EmailAuthResult.Success
    } catch (e: FirebaseException) {
        Log.w(TAG, "Email sign-in did not work", e)
        when (e) {
            is FirebaseNetworkException -> EmailAuthResult.Failed
            is FirebaseTooManyRequestsException -> EmailAuthResult.TooManyTries
            is FirebaseAuthException -> emailAuthResultFor(e.errorCode)
            else -> EmailAuthResult.Failed
        }
    }

    private companion object {
        const val TAG = "ShelfAnythingAuth"
    }
}

/** What the person can do about a refused email sign-in, from Firebase's error code. */
internal fun emailAuthResultFor(errorCode: String): EmailAuthResult = when (errorCode) {
    // With email enumeration protection, which new Firebase projects have, an unknown address and
    // a wrong password both come back as an invalid credential.
    "ERROR_INVALID_CREDENTIAL", "ERROR_WRONG_PASSWORD", "ERROR_USER_NOT_FOUND" -> EmailAuthResult.WrongCredentials
    "ERROR_INVALID_EMAIL" -> EmailAuthResult.InvalidEmail
    "ERROR_EMAIL_ALREADY_IN_USE" -> EmailAuthResult.EmailInUse
    "ERROR_WEAK_PASSWORD" -> EmailAuthResult.WeakPassword
    "ERROR_TOO_MANY_REQUESTS" -> EmailAuthResult.TooManyTries
    "ERROR_OPERATION_NOT_ALLOWED" -> EmailAuthResult.NotEnabled
    else -> EmailAuthResult.Failed
}
