package com.kokouliss.shelfanything.core.data.firebase

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.kokouliss.shelfanything.core.domain.model.Comment
import com.kokouliss.shelfanything.core.domain.model.Edition
import com.kokouliss.shelfanything.core.domain.model.Item
import com.kokouliss.shelfanything.core.domain.model.ItemDraft
import com.kokouliss.shelfanything.core.domain.model.MovieFormat
import com.kokouliss.shelfanything.core.domain.model.Shelf
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChangedBy

// Document layout. firebase/firestore.rules validates the same names; change both together.
//
// users/{uid}                  shelves[]: the ids of the shelves switched on; absent until the person picked
// users/{uid}/items/{itemId}   shelf, title, creator, year?, cover?, source, sourceId, barcode, details{}, description,
//                              own, want, done, edition?, format?, watchedOn, season, episode, rewatches, notes,
//                              comments[{text, at}], createdAt, updatedAt (both milliseconds, set by the phone)

internal object Collections {
    const val USERS = "users"
    const val ITEMS = "items"
}

internal object Fields {
    const val SHELVES = "shelves"
    const val SHELF = "shelf"
    const val TITLE = "title"
    const val CREATOR = "creator"
    const val YEAR = "year"
    const val COVER = "cover"
    const val SOURCE = "source"
    const val SOURCE_ID = "sourceId"
    const val BARCODE = "barcode"
    const val DETAILS = "details"
    const val DESCRIPTION = "description"
    const val OWN = "own"
    const val WANT = "want"
    const val DONE = "done"
    const val EDITION = "edition"
    const val FORMAT = "format"
    const val WATCHED_ON = "watchedOn"
    const val SEASON = "season"
    const val EPISODE = "episode"
    const val REWATCHES = "rewatches"
    const val NOTES = "notes"
    const val COMMENTS = "comments"
    const val TEXT = "text"
    const val AT = "at"
    const val CREATED_AT = "createdAt"
    const val UPDATED_AT = "updatedAt"
}

internal fun FirebaseFirestore.user(uid: String): DocumentReference = collection(Collections.USERS).document(uid)

internal fun FirebaseFirestore.items(uid: String): CollectionReference = user(uid).collection(Collections.ITEMS)

internal fun ItemDraft.toMap(own: Boolean, want: Boolean, done: Boolean, now: Long): Map<String, Any?> = mapOf(
    Fields.SHELF to shelf.id,
    Fields.TITLE to title,
    Fields.CREATOR to creator,
    Fields.YEAR to year,
    Fields.COVER to coverUrl,
    Fields.SOURCE to source,
    Fields.SOURCE_ID to sourceId,
    Fields.BARCODE to barcode,
    Fields.DETAILS to details,
    Fields.DESCRIPTION to description,
    Fields.OWN to own,
    Fields.WANT to want,
    Fields.DONE to done,
    Fields.WATCHED_ON to "",
    Fields.SEASON to 0,
    Fields.EPISODE to 0,
    Fields.REWATCHES to 0,
    Fields.NOTES to "",
    Fields.COMMENTS to emptyList<Map<String, Any>>(),
    Fields.CREATED_AT to now,
    Fields.UPDATED_AT to now,
)

internal fun Item.toMap(now: Long): Map<String, Any?> =
    ItemDraft(shelf, title, creator, year, coverUrl, source, sourceId, barcode, details, description)
        .toMap(own, want, done, createdAt) + mapOf(
        Fields.EDITION to edition?.id,
        Fields.FORMAT to movieFormat?.id,
        Fields.WATCHED_ON to watchedOn,
        Fields.SEASON to season,
        Fields.EPISODE to episode,
        Fields.REWATCHES to rewatches,
        Fields.NOTES to notes,
        Fields.COMMENTS to comments.map { mapOf(Fields.TEXT to it.text, Fields.AT to it.at) },
        Fields.UPDATED_AT to now,
    )

/** Null for a document whose shelf this version of the app does not know. */
internal fun DocumentSnapshot.toItem(): Item? {
    val shelf = Shelf.fromId(getString(Fields.SHELF).orEmpty()) ?: return null
    @Suppress("UNCHECKED_CAST")
    val details = (get(Fields.DETAILS) as? Map<String, Any?>).orEmpty()
    @Suppress("UNCHECKED_CAST")
    val comments = (get(Fields.COMMENTS) as? List<Map<String, Any?>>).orEmpty()
    return Item(
        id = id,
        shelf = shelf,
        title = getString(Fields.TITLE).orEmpty(),
        creator = getString(Fields.CREATOR).orEmpty(),
        year = getLong(Fields.YEAR)?.toInt(),
        coverUrl = getString(Fields.COVER),
        source = getString(Fields.SOURCE).orEmpty(),
        sourceId = getString(Fields.SOURCE_ID).orEmpty(),
        barcode = getString(Fields.BARCODE).orEmpty(),
        details = details.mapValues { it.value?.toString().orEmpty() },
        description = getString(Fields.DESCRIPTION).orEmpty(),
        own = getBoolean(Fields.OWN) ?: false,
        want = getBoolean(Fields.WANT) ?: false,
        done = getBoolean(Fields.DONE) ?: false,
        edition = Edition.fromId(getString(Fields.EDITION)),
        movieFormat = MovieFormat.fromId(getString(Fields.FORMAT)),
        watchedOn = getString(Fields.WATCHED_ON).orEmpty(),
        season = getLong(Fields.SEASON)?.toInt() ?: 0,
        episode = getLong(Fields.EPISODE)?.toInt() ?: 0,
        rewatches = getLong(Fields.REWATCHES)?.toInt() ?: 0,
        notes = getString(Fields.NOTES).orEmpty(),
        comments = comments.mapNotNull { c ->
            val text = c[Fields.TEXT] as? String ?: return@mapNotNull null
            Comment(text, (c[Fields.AT] as? Number)?.toLong() ?: 0)
        },
        createdAt = getLong(Fields.CREATED_AT) ?: 0,
        updatedAt = getLong(Fields.UPDATED_AT) ?: 0,
    )
}

internal fun FirebaseAuth.userFlow(): Flow<FirebaseUser?> = callbackFlow {
    val listener = FirebaseAuth.IdTokenListener { trySend(it.currentUser) }
    addIdTokenListener(listener)
    awaitClose { removeIdTokenListener(listener) }
}.distinctUntilChangedBy { user -> user?.let { Triple(it.uid, it.displayName, it.isEmailVerified) } }

internal fun FirebaseAuth.requireUser(): FirebaseUser = checkNotNull(currentUser) { "No user is signed in" }

/** The name shown in Settings. */
internal val FirebaseUser.shownName: String
    get() = displayName?.takeIf { it.isNotBlank() } ?: email?.substringBefore('@') ?: "?"

/**
 * Offline-first writes are not awaited: Firestore applies them on the device at once and syncs
 * later. A rejection (for example by the security rules) arrives much later, so it is only logged.
 */
internal fun Task<*>.logFailure(action: String) {
    addOnFailureListener { Log.w("ShelfAnythingSync", "Could not $action", it) }
}
