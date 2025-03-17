package com.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class CountryResolverServiceImpl implements CountryResolverService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CountryResolverServiceImpl.class);
    private final RestTemplate restTemplate;

    @Value("${ip-api.api.url}")
    String geoApiBaseUrl;

    private static final String LOG_CLIENT_COUNTRY = "Client from country: {}, IP: {}";
    private static final String LOG_FAILED_COUNTRY_RESOLUTION = "Failed to resolve country for IP: {}";
    private static final String LOG_REST_CLIENT_EXCEPTION = "REST client exception when resolving country: {}";

    public CountryResolverServiceImpl() {
        this.restTemplate = new RestTemplate();
    }

    public void logClientCountry(String ipAddress) {
        try {
            String country = resolveCountry(ipAddress);
            LOGGER.info(LOG_CLIENT_COUNTRY, country, ipAddress);
        } catch (Exception e) {
            LOGGER.warn(LOG_FAILED_COUNTRY_RESOLUTION, ipAddress);
        }
    }

    private String resolveCountry(String ipAddress) throws RestClientException {
        String url = geoApiBaseUrl + ipAddress;

        try {
            ParameterizedTypeReference<Map<String, String>> responseType = new ParameterizedTypeReference<>() {};

            Map<String, String> response = restTemplate.exchange(url,
                    org.springframework.http.HttpMethod.GET,
                    null,
                    responseType).getBody();

            if (response != null && response.get("country") != null) {
                return response.get("country");
            } else {
                return "Unknown";
            }
        } catch (RestClientException e) {
            LOGGER.debug(LOG_REST_CLIENT_EXCEPTION, e.getMessage());
            throw e;
        }
    }
}
