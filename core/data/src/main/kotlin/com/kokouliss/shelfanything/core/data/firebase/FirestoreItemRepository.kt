package com.kokouliss.shelfanything.core.data.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.kokouliss.shelfanything.core.domain.model.Item
import com.kokouliss.shelfanything.core.domain.model.ItemDraft
import com.kokouliss.shelfanything.core.domain.model.Shelf
import com.kokouliss.shelfanything.core.domain.repository.ItemRepository
import com.kokouliss.shelfanything.core.domain.repository.ShelfRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

/** The collection in Firestore. Its offline cache is on by default, so every screen works without a signal. */
@Singleton
internal class FirestoreItemRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
) : ItemRepository {

    @OptIn(ExperimentalCoroutinesApi::class)
    override val items: Flow<List<Item>> = auth.userFlow().map { it?.uid }.flatMapLatest { uid ->
        if (uid == null) {
            flowOf(emptyList())
        } else {
            callbackFlow {
                val registration = db.items(uid).orderBy(Fields.CREATED_AT, Query.Direction.DESCENDING)
                    .addSnapshotListener { snapshot, error ->
                        if (snapshot != null) trySend(snapshot.documents.mapNotNull { it.toItem() })
                        if (error != null) close(error)
                    }
                awaitClose { registration.remove() }
            }
        }
    }

    override fun item(id: String): Flow<Item?> = items.map { list -> list.firstOrNull { it.id == id } }

    override suspend fun add(draft: ItemDraft, own: Boolean, want: Boolean, done: Boolean): String {
        val document = db.items(auth.requireUser().uid).document()
        document.set(draft.toMap(own, want, done, System.currentTimeMillis())).logFailure("add an item")
        return document.id
    }

    override suspend fun update(item: Item) {
        db.items(auth.requireUser().uid).document(item.id).set(item.toMap(System.currentTimeMillis())).logFailure("save an item")
    }

    override suspend fun delete(id: String) {
        db.items(auth.requireUser().uid).document(id).delete().logFailure("delete an item")
    }

    /** Everything of the signed-in person, for deleting the account. Goes to the server, so it needs a connection. */
    suspend fun deleteAll() {
        val uid = auth.requireUser().uid
        val documents = db.items(uid).get().await().documents
        documents.chunked(400).forEach { chunk ->
            val batch = db.batch()
            chunk.forEach { batch.delete(it.reference) }
            batch.commit().await()
        }
        db.user(uid).delete().await()
    }
}

@Singleton
internal class FirestoreShelfRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
) : ShelfRepository {

    @OptIn(ExperimentalCoroutinesApi::class)
    override val enabledShelves: Flow<Set<Shelf>?> = auth.userFlow().map { it?.uid }.flatMapLatest { uid ->
        if (uid == null) {
            flowOf(null)
        } else {
            callbackFlow {
                val registration = db.user(uid).addSnapshotListener { snapshot, error ->
                    if (snapshot != null) {
                        @Suppress("UNCHECKED_CAST")
                        val ids = snapshot.get(Fields.SHELVES) as? List<String>
                        trySend(ids?.mapNotNull(Shelf::fromId)?.toSet())
                    }
                    if (error != null) close(error)
                }
                awaitClose { registration.remove() }
            }
        }
    }

    override suspend fun setEnabledShelves(shelves: Set<Shelf>) {
        db.user(auth.requireUser().uid).set(mapOf(Fields.SHELVES to shelves.map { it.id })).logFailure("save the shelves")
    }
}
