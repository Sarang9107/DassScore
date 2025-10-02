package com.example.dassscore

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import android.util.Log

data class User(
    val uid: String,
    val email: String? = null
)

class FirebaseRepository {

    private val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    fun getCurrentUser(): User? {
        val firebaseUser = auth.currentUser
        return firebaseUser?.let { User(it.uid, it.email) }
    }

    suspend fun signInWithEmail(email: String, password: String): Result<User> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            authResult.user?.let { Result.success(User(it.uid, it.email)) }
                ?: Result.failure(Exception("Sign in failed: Null user"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signUpWithEmail(email: String, password: String): Result<User> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            authResult.user?.let { Result.success(User(it.uid, it.email)) }
                ?: Result.failure(Exception("Sign up failed: Null user"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllDassResults(): Result<List<DassResult>> {
        return try {
            val snapshot = db.collectionGroup("dassResults").get().await()
            val results = snapshot.documents.mapNotNull { doc ->
                DassResult(
                    depressionScore = doc.getLong("depressionScore")?.toInt() ?: 0,
                    anxietyScore = doc.getLong("anxietyScore")?.toInt() ?: 0,
                    stressScore = doc.getLong("stressScore")?.toInt() ?: 0,
                    timestamp = doc.getLong("timestamp") ?: 0L,
                    userId = doc.getString("userId") ?: "",
                    responses = (doc.get("responses") as? List<Long>)?.map { it.toInt() } ?: emptyList()
                )
            }
            Result.success(results)
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error getting all DASS results: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun getUserProfile(userId: String): Result<Map<String, Any>?> {
        return try {
            val doc = db.collection("users").document(userId).get().await()
            Result.success(doc.data)
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error getting user profile for $userId: ${e.message}", e)
            Result.failure(e)
        }
    }

    fun signOut() {
        auth.signOut()
    }

    suspend fun updateUserProfile(userId: String, profileData: Map<String, Any>): Result<Unit> {
        return try {
            db.collection("users").document(userId)
                .set(profileData, SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error updating user profile for $userId: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun saveDassResult(resultToSave: DassResult): Result<Unit> {
        val userId = resultToSave.userId
        if (userId.isEmpty()) {
            return Result.failure(IllegalArgumentException("userId in DassResult cannot be empty for saving."))
        }
        return try {
            db.collection("users").document(userId).collection("dassResults").add(resultToSave).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error saving DASS result for $userId: ${e.message}", e)
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

            val results = querySnapshot.documents.mapNotNull { doc ->
                DassResult(
                    depressionScore = doc.getLong("depressionScore")?.toInt() ?: 0,
                    anxietyScore = doc.getLong("anxietyScore")?.toInt() ?: 0,
                    stressScore = doc.getLong("stressScore")?.toInt() ?: 0,
                    timestamp = doc.getLong("timestamp") ?: 0L,
                    userId = doc.getString("userId") ?: "",
                    responses = (doc.get("responses") as? List<Long>)?.map { it.toInt() } ?: emptyList()
                )
            }
            Result.success(results)
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error getting user DASS results for $userId: ${e.message}", e)
            Result.failure(e)
        }
    }
}