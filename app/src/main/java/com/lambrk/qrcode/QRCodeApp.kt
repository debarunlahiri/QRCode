package com.lambrk.qrcode

import android.app.Application
import com.lambrk.qrcode.di.AppContainer

class QRCodeApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
