package org.example.registration.model.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE )
public class ChangePasswordIfLoggedInRequest {

    String oldPassword;
    String newPassword;
    String repeatPassword;

}
