package com.preview.preview.domain.web.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.preview.preview.domain.user.User;
import com.preview.preview.domain.web.dto.AuthorityDto;
import lombok.*;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserLoginDto {

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @NotNull
    @Size(min = 3, max = 100)
    private String password;

    @NotNull
    @Size(min = 3, max = 50)
    private String nickname;

    @NotNull
    private long kakaoId;

    private Set<AuthorityDto> authorityDtoSet;

    public static UserLoginDto from(User user){
        if (user == null) return null;
        return UserLoginDto.builder()
                .nickname(user.getNickname())
                .kakaoId(user.getKakaoId())
                .authorityDtoSet(user.getAuthorities().stream()
                        .map(authority -> AuthorityDto.builder().authorityName(authority.getAuthorityName()).build())
                        .collect(Collectors.toSet()))
                .build();
    }
}