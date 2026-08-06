package org.ict.datemanagerbackend.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

@Entity
@Table(name = "social_accounts", uniqueConstraints = @UniqueConstraint(columnNames = {"provider", "provider_user_id"}))
public class SocialAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String provider;

    @Column(name = "provider_user_id", nullable = false)
    private String providerUserId;

    @Lob
    @Column(name = "access_token")
    private String accessToken;

    @Column(name = "connected_at", insertable = false, updatable = false)
    private LocalDateTime connectedAt;

    protected SocialAccount() {
    }

    public SocialAccount(Long userId, String provider, String providerUserId, String accessToken) {
        this.userId = userId;
        this.provider = provider;
        this.providerUserId = providerUserId;
        this.accessToken = accessToken;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getProvider() {
        return provider;
    }

    public String getProviderUserId() {
        return providerUserId;
    }
}
