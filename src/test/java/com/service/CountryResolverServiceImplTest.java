package com.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureMockMvc
class CountryResolverServiceImplTest {

    @MockBean
    private RestTemplate restTemplate;

    @Mock
    private CountryResolverService countryResolverService;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testLogClientCountry_Failure() throws Exception {
        String ipAddress = "192.168.1.1";
        when(restTemplate.exchange(
                "http://ip-api.com/json/192.168.1.1",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<String, String>>() {})
        ).thenThrow(new RestClientException("API call failed"));
        countryResolverService.logClientCountry(ipAddress);
    }
}