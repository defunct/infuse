package com.goodworkalan.infuse;

import static org.testng.Assert.assertEquals;

public class InfusionExceptionCatcher {
    /** The expected message key. */
    private String messageKey;

    /** The expected error message. */
    private String message;

    /** The body of the test. */
    private final Runnable runnable;

    /**
     * Assert that the diffusion exception thrown by the given runnable has the
     * given error code and the given error message.
     * 
     * @param code
     *            The expected message key.
     * @param message
     *            The expected error message.
     * @param runnable
     *            The body of the test.
     */
    public InfusionExceptionCatcher(Runnable runnable, String messageKey, String message) {
        this.messageKey = messageKey;
        this.runnable = runnable;
        this.message = message;
    }

    /**
     * Run the test.
     */
    public void run() {
        try {
            runnable.run();
        } catch (InfusionException e) {
            assertEquals(e.getMessageKey(), messageKey);
            assertEquals(e.getMessage(), message);
            throw e;
        }
    }
}
