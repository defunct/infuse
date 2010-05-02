package com.goodworkalan.infuse;

import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.goodworkalan.danger.CodedDanger;

public class InfusionException extends CodedDanger {
    /** Serial version id. */
    private static final long serialVersionUID = -1L;
    private final static ConcurrentMap<String, ResourceBundle> BUNDLES = new ConcurrentHashMap<String, ResourceBundle>();

    public InfusionException(int code, Throwable cause, Object...arguments) {
        super(BUNDLES, code, cause, arguments);
    }

    public InfusionException(int code, Object... arguments) {
        super(BUNDLES, code, null, arguments);
    }
}
