package by.urbash_hair.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ConsentRequiredException extends RuntimeException {

    public ConsentRequiredException(String message) {
        super(message);
    }
}

