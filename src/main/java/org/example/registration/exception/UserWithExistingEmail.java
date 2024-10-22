package org.example.registration.exception;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class UserWithExistingEmail extends RuntimeException{

    Integer status;
    LocalDateTime occurredAt;

    public UserWithExistingEmail(String message, Integer status, LocalDateTime occurredAt) {
        super(message);
        this.status = status;
        this.occurredAt = occurredAt;
    }

}
