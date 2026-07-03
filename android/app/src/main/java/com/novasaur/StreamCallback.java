package com.novasaur;

/**
 * Token-by-token streaming callbacks, exposed to the .NET binding.
 * (Binds to C# as Com.Novasaur.IStreamCallback.)
 */
public interface StreamCallback {

    void onToken(String token);

    void onDone();

    void onError(String error);
}
