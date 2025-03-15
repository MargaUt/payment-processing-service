package com.service;

import com.dto.GeoResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;


@Service
public class CountryResolverServiceImpl implements CountryResolverService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CountryResolverServiceImpl.class);
    private final RestTemplate restTemplate;

    @Value("${ip-api.api.url}")
    private String geoApiBaseUrl;

    @Autowired
    public CountryResolverServiceImpl() {
        this.restTemplate = new RestTemplate();
    }

    public void logClientCountry(String ipAddress) {
        try {
            String country = resolveCountry(ipAddress);
            LOGGER.info("Client from country: {}, IP: {}", country, ipAddress);
        } catch (Exception e) {
            LOGGER.warn("Failed to resolve country for IP: {}", ipAddress);
        }
    }

    private String resolveCountry(String ipAddress) throws RestClientException {

        String url = geoApiBaseUrl + ipAddress;

        try {
            Map response = restTemplate.getForObject(url, Map.class);

            if (response != null && response.get("country") != null) {
                return (String) response.get("country");
            } else {
                return "Unknown";
            }
        } catch (RestClientException e) {
            LOGGER.debug("REST client exception when resolving country: {}", e.getMessage());
            throw e;
        }
    }
}
