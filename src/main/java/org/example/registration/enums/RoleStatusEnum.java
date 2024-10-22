package org.example.registration.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RoleStatusEnum {

    ACTIVE(1) , DE_ACTIVE(0);

    final Integer status;

}
