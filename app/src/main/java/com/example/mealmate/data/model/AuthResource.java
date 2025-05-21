package com.example.mealmate.data.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class AuthResource<T> {

    @NonNull
    public final Status status;

    @Nullable
    public final T data;

    @Nullable
    public final String message;

    private AuthResource(@NonNull Status status, @Nullable T data, @Nullable String message) {
        this.status = status;
        this.data = data;
        this.message = message;
    }

    public static <T> AuthResource<T> success(@NonNull T data) {
        return new AuthResource<>(Status.SUCCESS, data, null);
    }

    public static <T> AuthResource<T> error(String msg, @Nullable T data) {
        return new AuthResource<>(Status.ERROR, data, msg);
    }

    public static <T> AuthResource<T> loading(@Nullable T data) {
        return new AuthResource<>(Status.LOADING, data, null);
    }

    public enum Status { SUCCESS, ERROR, LOADING }
}