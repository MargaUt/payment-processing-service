package com.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class ClientIpResolverTest {

    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        request = Mockito.mock(HttpServletRequest.class);
    }

    @Test
    void shouldReturnIpFromXForwardedFor() {
        when(request.getHeader("X-Forwarded-For")).thenReturn("192.168.1.100");

        String ip = ClientIpResolver.getClientIpAddress(request);

        assertEquals("192.168.1.100", ip);
    }

    @Test
    void shouldReturnIpFromProxyClientIpIfXForwardedForIsUnknown() {
        when(request.getHeader("X-Forwarded-For")).thenReturn("unknown");
        when(request.getHeader("Proxy-Client-IP")).thenReturn("10.0.0.1");

        String ip = ClientIpResolver.getClientIpAddress(request);

        assertEquals("10.0.0.1", ip);
    }

    @Test
    void shouldReturnIpFromWlProxyClientIpIfPreviousHeadersAreMissing() {
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("Proxy-Client-IP")).thenReturn(null);
        when(request.getHeader("WL-Proxy-Client-IP")).thenReturn("172.16.5.5");

        String ip = ClientIpResolver.getClientIpAddress(request);

        assertEquals("172.16.5.5", ip);
    }

    @Test
    void shouldReturnRemoteAddrIfNoHeadersPresent() {
        when(request.getHeader(Mockito.anyString())).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        String ip = ClientIpResolver.getClientIpAddress(request);

        assertEquals("127.0.0.1", ip);
    }
}