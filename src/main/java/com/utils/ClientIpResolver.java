package com.utils;

import jakarta.servlet.http.HttpServletRequest;

public class ClientIpResolver {

    /**
     * Retrieves the client's IP address from the given HttpServletRequest.
     * The method first checks a list of common headers for the client's IP
     * (such as "X-Forwarded-For") which may be set by proxies or load balancers.
     * If no valid IP is found in these headers, it returns the remote address of the request.
     *
     * @param request The HttpServletRequest from which to extract the client IP.
     * @return The client IP address as a String.
     */
    public static String getClientIpAddress(HttpServletRequest request) {
        String[] headers = {
                "X-Forwarded-For", "Proxy-Client-IP", "WL-Proxy-Client-IP",
                "HTTP_CLIENT_IP", "HTTP_X_FORWARDED_FOR"
        };

        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                return ip;
            }
        }

        return request.getRemoteAddr();
    }
}