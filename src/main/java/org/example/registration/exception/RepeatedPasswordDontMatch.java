package org.example.registration.exception;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class RepeatedPasswordDontMatch extends Exception{

    Integer status;
    LocalDateTime occurredAt;

    public RepeatedPasswordDontMatch(String message, Integer status, LocalDateTime occurredAt) {
        super(message);
        this.status = status;
        this.occurredAt = occurredAt;
    }

}
