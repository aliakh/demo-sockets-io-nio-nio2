package demo.nio2.completion_handler.client;

import java.util.concurrent.atomic.AtomicBoolean;

class Attachment {

    private final String message;
    private final AtomicBoolean active;

    Attachment(String message, boolean active) {
        this.message = message;
        this.active = new AtomicBoolean(active);
    }

    String getMessage() {
        return message;
    }

    AtomicBoolean getActive() {
        return active;
    }
}