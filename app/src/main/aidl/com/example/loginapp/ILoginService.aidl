// ASSUMED contract. The package and method names MUST match the AIDL file shipped by the
// other (login) app exactly; replace this file with a copy of theirs.
package com.example.loginapp;

interface ILoginService {
    /** True when a user is currently signed in to the login app. */
    boolean isLoggedIn();

    /** Display name of the signed-in user, or null/empty if none. */
    String getUserName();
}
