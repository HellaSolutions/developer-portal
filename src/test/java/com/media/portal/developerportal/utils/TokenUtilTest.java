package com.media.portal.developerportal.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TokenUtilTest {

    @Test
    void generateToken_producesPlainTokenWithExpectedPrefixAndLength() {
        var token = TokenUtil.generateToken();

        assertThat(token.plainToken()).startsWith("pk_");
        assertThat(token.plainToken()).hasSize(35); // "pk_" (3) + 32 base64url chars
    }

    @Test
    void generateToken_hashedTokenMatchesSha256OfPlainToken() {
        var token = TokenUtil.generateToken();

        assertThat(token.hashedToken()).isEqualTo(TokenUtil.sha256(token.plainToken()));
    }

    @Test
    void generateToken_producesUniqueTokensAcrossCalls() {
        var first = TokenUtil.generateToken();
        var second = TokenUtil.generateToken();

        assertThat(first.plainToken()).isNotEqualTo(second.plainToken());
        assertThat(first.hashedToken()).isNotEqualTo(second.hashedToken());
    }

    @Test
    void sha256_isDeterministic() {
        assertThat(TokenUtil.sha256("same-input")).isEqualTo(TokenUtil.sha256("same-input"));
    }

    @Test
    void sha256_differentInputsProduceDifferentHashes() {
        assertThat(TokenUtil.sha256("input-a")).isNotEqualTo(TokenUtil.sha256("input-b"));
    }

    @Test
    void sha256_producesLowercaseHexOf64Chars() {
        var hash = TokenUtil.sha256("anything");

        assertThat(hash).hasSize(64);
        assertThat(hash).matches("^[0-9a-f]{64}$");
    }
}
