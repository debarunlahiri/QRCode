package com.lambrk.qrcode.data.login

/** The other app that owns the login. TODO: set the real name and package id. */
object ExternalAppConfig {
    const val DISPLAY_NAME = "Login app"
    const val PACKAGE_NAME = "com.example.loginapp" // keep in sync with <queries> in AndroidManifest.xml

    /** AIDL service action exposed by the login app. TODO: set the real action. */
    const val SERVICE_ACTION = "com.example.loginapp.action.LOGIN_SERVICE"
}
