package com.br.firesa.vpn.exception;

import java.util.function.Supplier;

public class ExceptionUtils {
    public static <T> T tryCatch(Supplier<T> supplier, T defaultValue) {
        try {
            return supplier.get();
        } catch (Exception e) {
            return defaultValue;
        }
    }
}
