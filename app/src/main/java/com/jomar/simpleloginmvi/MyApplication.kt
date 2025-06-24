package com.jomar.simpleloginmvi

import android.app.Application
import dataModule
import domainModule
import homeModule
import loginModule
import networkModule
import org.koin.core.context.GlobalContext.startKoin

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@MyApplication)
            modules(
                networkModule,
                dataModule,
                domainModule,
                loginModule,
                homeModule
            )
        }
    }
}