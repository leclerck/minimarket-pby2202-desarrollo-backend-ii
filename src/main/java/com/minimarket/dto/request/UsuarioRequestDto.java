package com.minimarket.dto.request;

import java.util.Set;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(exclude = "password")
public class UsuarioRequestDto {

    private String username;
    private String password;
    private Set<Long> rolIds;

}
