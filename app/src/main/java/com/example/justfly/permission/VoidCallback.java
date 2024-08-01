package com.example.justfly.permission;

/**
 * Since java is missing simple callback interface i made this one.
 * It is only for execution of void methods without results
 */
@FunctionalInterface
public interface VoidCallback {

    void execute();
}
