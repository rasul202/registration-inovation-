package org.example.registration.model.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateUserEntityByEmailRequest {

    String firstName;
    String lastName;
    String email;
    String password;

}
