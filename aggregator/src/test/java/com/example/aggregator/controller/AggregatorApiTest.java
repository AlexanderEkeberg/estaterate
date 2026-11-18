package com.example.aggregator.controller;

import com.example.aggregator.service.AnalysisService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AggregatorApiTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private AnalysisService analysisService;

    @Test
    void returnsAnalysisStatus() throws Exception {
        String analysisId = "analysis-123";

        AnalysisService.AnalysisStatusResponse response =
                new AnalysisService.AnalysisStatusResponse(
                        analysisId,
                        "DONE",
                        Map.of(
                                "WEATHER",
                                new AnalysisService.CategoryStatusResponse(
                                        "SUCCESS",
                                        72,
                                        Map.of("forecast", "sunny"),
                                        null
                                ),
                                "PROPERTY",
                                new AnalysisService.CategoryStatusResponse(
                                        "SUCCESS",
                                        null,
                                        Map.of(
                                                "address", "Niels Juels gate 7, 0272 Oslo",
                                                "askingPrice", 17800000,
                                                "usableArea", 179
                                        ),
                                        null
                                )
                        )
                );

        when(analysisService.getAnalysis(analysisId)).thenReturn(response);

        mvc.perform(get("/api/analysis/{analysisId}", analysisId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.analysisId").value(analysisId))
                .andExpect(jsonPath("$.status").value("DONE"))
                .andExpect(jsonPath("$.categories.WEATHER.status").value("SUCCESS"))
                .andExpect(jsonPath("$.categories.WEATHER.score").value(72))
                .andExpect(jsonPath("$.categories.PROPERTY.data.address")
                        .value("Niels Juels gate 7, 0272 Oslo"))
                .andExpect(jsonPath("$.categories.PROPERTY.data.askingPrice")
                        .value(17800000))
                .andExpect(jsonPath("$.categories.PROPERTY.data.usableArea")
                        .value(179));
    }

    @Test
    void returnsNotFoundWhenAnalysisDoesNotExist() throws Exception {
        String analysisId = "missing-analysis";

        when(analysisService.getAnalysis(analysisId))
                .thenThrow(new ResponseStatusException(
                        NOT_FOUND,
                        "Analysis not found: " + analysisId
                ));

        mvc.perform(get("/api/analysis/{analysisId}", analysisId))
                .andExpect(status().isNotFound());
    }
}