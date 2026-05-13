package com.app.user.model;

import com.app.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Core user account. Supports both local (email/password) and OAuth2 sign-in.
 * {@code passwordHash} is null for OAuth2 users; {@code providerId} is null for LOCAL users.
 */
@Entity
@Table(
    name = "users",
    indexes = {
        @Index(name = "idx_users_email",       columnList = "email"),
        @Index(name = "idx_users_provider_id", columnList = "provider_id")
    },
    uniqueConstraints = @UniqueConstraint(name = "uq_users_email", columnNames = "email")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Column(nullable = false, length = 255)
    private String email;

    @Column(name = "full_name", length = 255)
    private String fullName;

    /** Null for OAuth2 users. */
    @Column(name = "password_hash")
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Provider provider = Provider.LOCAL;

    /** External OAuth2 subject ID; null for LOCAL users. */
    @Column(name = "provider_id")
    private String providerId;

    // ── Enum ──────────────────────────────────────────────────────────────

    public enum Provider {
        LOCAL, GOOGLE
    }
}
