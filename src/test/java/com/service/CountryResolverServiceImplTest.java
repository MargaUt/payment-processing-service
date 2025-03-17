package com.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CountryResolverServiceImplTest {

    @MockBean
    private RestTemplate restTemplate;

    @InjectMocks
    private CountryResolverServiceImpl countryResolverService;

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
                "http://example.com/api/192.168.1.1",
                org.springframework.http.HttpMethod.GET,
                null,
                new ParameterizedTypeReference<Map<String, String>>() {})
        ).thenThrow(new RestClientException("API call failed"));

        mockMvc.perform(get("/countryResolver/logClientCountry?ipAddress=" + ipAddress))
                .andExpect(status().isInternalServerError());
    }
}