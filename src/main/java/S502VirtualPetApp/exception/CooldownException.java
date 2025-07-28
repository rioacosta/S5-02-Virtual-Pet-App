package S502VirtualPetApp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
public class CooldownException extends RuntimeException {
    public CooldownException(String message) {
        super(message);
    }
}
