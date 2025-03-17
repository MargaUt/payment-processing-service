package com.service;

public interface CountryResolverService {

    /**
     * Logs the country information of a client based on their IP address.
     * If the country cannot be resolved, a warning is logged instead.
     *
     * @param ipAddress The IP address of the client.
     */
    void logClientCountry(String ipAddress);
}
