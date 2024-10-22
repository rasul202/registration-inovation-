package org.example.registration.exception;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

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
