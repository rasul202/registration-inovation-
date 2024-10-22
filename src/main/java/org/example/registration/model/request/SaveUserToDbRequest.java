package org.example.registration.model.request;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.example.registration.entity.RoleEntity;

import javax.print.DocFlavor;
import java.lang.reflect.ParameterizedType;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SaveUserToDbRequest {

    String firstName;
    String lastName;
    String email;
    String password;
    List<RoleRequest> roles;

}
