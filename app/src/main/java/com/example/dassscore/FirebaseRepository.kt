package com.example.dassscore

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore // Added import
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

data class User(
    val uid: String,
    val email: String? = null
)

// This data class will be used for storing detailed results in Firebase
internal data class DassResultForFirebase(
    val userId: String,
    val depressionScore: Int,
    val anxietyScore: Int,
    val stressScore: Int,
    val responses: List<Int>, // Storing the individual responses
    val timestamp: Long
)

// Assuming DassResult is a data class like this.
// If it's defined elsewhere or differently, you might need to adjust.

class FirebaseRepository {

    // Placeholder for Firebase Auth
    private val auth = FirebaseAuth.getInstance() // TODO: Initialize Firebase Auth (e.g., FirebaseAuth.getInstance())

    // Initialize Firebase Firestore
    private val db = FirebaseFirestore.getInstance() // Changed from null

    fun getCurrentUser(): User? {
        // TODO: Implement Firebase Auth getCurrentUser
        val firebaseUser = auth?.currentUser
        return firebaseUser?.let { User(it.uid, it.email) }
        //println("FirebaseRepository: getCurrentUser() called (placeholder)")
        //return null // Placeholder
    }

    suspend fun signInWithEmail(email: String, password: String): Result<User> {
        // TODO: Implement Firebase Auth signInWithEmail
        //println("FirebaseRepository: signInWithEmail('$email') called (placeholder)")
        return try {
            val authResult = auth?.signInWithEmailAndPassword(email, password)?.await()
            authResult?.user?.let { Result.success(User(it.uid, it.email)) }
                ?: Result.failure(Exception("Sign in failed: Null user"))
        } catch (e: Exception) {
            Result.failure(e)
        }
       
    }

    suspend fun signUpWithEmail(email: String, password: String): Result<User> {
        // TODO: Implement Firebase Auth signUpWithEmail
        //println("FirebaseRepository: signUpWithEmail('$email') called (placeholder)")
        return try {
            val authResult = auth?.createUserWithEmailAndPassword(email, password)?.await()
            authResult?.user?.let { Result.success(User(it.uid, it.email)) }
                ?: Result.failure(Exception("Sign up failed: Null user"))
        } catch (e: Exception) {
            Result.failure(e)
        }
        //return Result.failure(NotImplementedError("signUpWithEmail not implemented"))
    }

    fun signOut() {
        // TODO: Implement Firebase Auth signOut
        auth?.signOut()
        //println("FirebaseRepository: signOut() called (placeholder)")
    }

    suspend fun saveDassResult(userId: String, responses: List<Int>, dassScores: DassResult): Result<Unit> {
        if (userId.isEmpty()) {
            return Result.failure(IllegalArgumentException("userId cannot be empty for saving results."))
        }

        val resultToSave = DassResultForFirebase(
            userId = userId,
            depressionScore = dassScores.depressionScore,
            anxietyScore = dassScores.anxietyScore,
            stressScore = dassScores.stressScore,
            responses = responses,
            timestamp = dassScores.timestamp
        )

        return try {
            db.collection("users").document(userId).collection("dassResults").add(resultToSave).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserDassResults(userId: String): Result<List<DassResult>> {
        if (userId.isEmpty()) {
            return Result.failure(IllegalArgumentException("userId cannot be empty for fetching results."))
        }
        return try {
            val querySnapshot = db.collection("users").document(userId).collection("dassResults")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get().await()
            val firebaseResults = querySnapshot?.toObjects(DassResultForFirebase::class.java) ?: emptyList()
            val results = firebaseResults.map { fbResult ->
                DassResult(
                    depressionScore = fbResult.depressionScore,
                    anxietyScore = fbResult.anxietyScore,
                    stressScore = fbResult.stressScore,
                    timestamp = fbResult.timestamp,
                    responses = fbResult.responses,
                    userId = fbResult.userId

                )
            }
            Result.success(results)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}