package org.example.registration.model.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.example.registration.model.request.RoleRequest;

import java.time.LocalDateTime;
import java.util.ArrayList;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GetUserByEmailAndPasswordResponse {

    String firstName;
    String lastName;
    String email;
    LocalDateTime createdAt;
    ArrayList<RoleRequest> roles;

}
