package org.example.registration.exception;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;


//TODO if you want to avoid from boilerplate code : instead of throwing exception and handling it in handler method , return the global exception response directly
@Getter
@Setter
@NoArgsConstructor
public class NotFoundRunTimeException extends RuntimeException{

    Integer status;
    LocalDateTime occurredAt;

    public NotFoundRunTimeException(String message, Integer status, LocalDateTime occurredAt) {
        super(message);
        this.status = status;
        this.occurredAt = occurredAt;
    }

}
