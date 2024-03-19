package com.diva.batch.entity.fitme;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Getter
@NoArgsConstructor(access = PROTECTED)
@Entity
public class Token {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    // OAuth2.0 provider에서 제공하는 AccessToken
    @NotBlank
    @Column(unique = true)
    private String resourceAccessToken;

    @NotBlank
    @Column(unique = true)
    private String resourceRefreshToken;

    @NotNull
    @OneToOne(mappedBy = "token")
    private Member member;

    @Builder
    protected Token(String resourceAccessToken, String resourceRefreshToken, Member member) {
        this.resourceAccessToken = resourceAccessToken;
        this.resourceRefreshToken = resourceRefreshToken;
        this.member = member;

        this.member.setToken(this);
    }

    // == 비즈니스 로직 == //
    public void updateResourceTokens(String resourceAccessToken, String resourceRefreshToken) {
        this.resourceAccessToken = resourceAccessToken;
        this.resourceRefreshToken = resourceRefreshToken;
    }
}
