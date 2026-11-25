package com.kokouliss.shelfanything.core.domain.model

/** The signed-in person. */
data class User(
    val id: String,
    val name: String,
    val email: String?,
    /** Signed up with an email and password, so confirming it is them takes the password. */
    val usesPassword: Boolean = false,
    /** False until the person taps the link in the confirmation email. */
    val emailVerified: Boolean = true,
)
