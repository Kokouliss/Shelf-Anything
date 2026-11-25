package com.kokouliss.shelfanything.core.data.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.kokouliss.shelfanything.core.data.firebase.FirebaseAuthRepository
import com.kokouliss.shelfanything.core.data.firebase.FirestoreItemRepository
import com.kokouliss.shelfanything.core.data.firebase.FirestoreShelfRepository
import com.kokouliss.shelfanything.core.data.metadata.SiteMetadataRepository
import com.kokouliss.shelfanything.core.domain.repository.AuthRepository
import com.kokouliss.shelfanything.core.domain.repository.ItemRepository
import com.kokouliss.shelfanything.core.domain.repository.MetadataRepository
import com.kokouliss.shelfanything.core.domain.repository.ShelfRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import okhttp3.OkHttpClient

@Module
@InstallIn(SingletonComponent::class)
internal abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: FirebaseAuthRepository): AuthRepository

    @Binds
    @Singleton
    abstract fun bindItemRepository(impl: FirestoreItemRepository): ItemRepository

    @Binds
    @Singleton
    abstract fun bindShelfRepository(impl: FirestoreShelfRepository): ShelfRepository

    @Binds
    @Singleton
    abstract fun bindMetadataRepository(impl: SiteMetadataRepository): MetadataRepository

    companion object {
        @Provides
        @Singleton
        fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

        @Provides
        @Singleton
        fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

        @Provides
        @Singleton
        fun provideOkHttp(): OkHttpClient = OkHttpClient()
    }
}
