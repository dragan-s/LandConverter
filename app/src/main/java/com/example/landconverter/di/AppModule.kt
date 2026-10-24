package com.example.landconverter.di

import com.example.landconverter.data.model.BalkanCountry
import com.example.landconverter.data.model.ConverterOptions
import com.example.landconverter.data.model.LandUnit
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideConverterOptions(): ConverterOptions = ConverterOptions(
        countries = BalkanCountry.all,
        units = LandUnit.all
    )
}
