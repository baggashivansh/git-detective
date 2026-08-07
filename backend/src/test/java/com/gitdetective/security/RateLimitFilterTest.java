package com.gitdetective.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class RateLimitFilterTest {

    @Test
    @DisplayName("allows requests under the limit and blocks when exceeded")
    void enforcesLimit() throws Exception {
        RateLimitFilter filter = new RateLimitFilter(2, 60);
        MockHttpServletRequest request =
                new MockHttpServletRequest("POST", "/assistant/conversations");
        request.setRemoteAddr("203.0.113.10");

        MockHttpServletResponse first = new MockHttpServletResponse();
        filter.doFilter(request, first, new MockFilterChain());
        assertThat(first.getStatus()).isNotEqualTo(429);

        MockHttpServletResponse second = new MockHttpServletResponse();
        filter.doFilter(request, second, new MockFilterChain());
        assertThat(second.getStatus()).isNotEqualTo(429);

        MockHttpServletResponse third = new MockHttpServletResponse();
        filter.doFilter(request, third, new MockFilterChain());
        assertThat(third.getStatus()).isEqualTo(429);
        assertThat(third.getContentAsString()).contains("RATE_LIMITED");
    }

    @Test
    @DisplayName("does not rate-limit GET health")
    void skipsGet() throws Exception {
        RateLimitFilter filter = new RateLimitFilter(1, 60);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/health");
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, new MockFilterChain());
        assertThat(response.getStatus()).isNotEqualTo(429);
    }
}
