package com.mapmory.backend.place;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;

class PlaceControllerTest {

    @Test
    void 검색_후보를_응답한다() throws Exception {
        GeoapifyClient client = mock(GeoapifyClient.class);
        PlaceSelectionService selectionService = mock(PlaceSelectionService.class);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new PlaceController(client, selectionService)).build();
        when(client.search("한강공원"))
                .thenReturn(List.of(new PlaceCandidate("park-1", "여의도한강공원", "서울 영등포구")));

        mockMvc.perform(get("/api/v1/places/search").param("query", "한강공원"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].placeId").value("park-1"))
                .andExpect(jsonPath("$.data[0].name").value("여의도한강공원"));
    }

    @Test
    void 선택한_장소의_추천_지역을_응답한다() throws Exception {
        GeoapifyClient client = mock(GeoapifyClient.class);
        PlaceSelectionService selectionService = mock(PlaceSelectionService.class);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new PlaceController(client, selectionService)).build();
        when(selectionService.select("park-1"))
                .thenReturn(new PlaceSelectionResponse("park-1", "여의도한강공원", "KR", null, true));

        mockMvc.perform(get("/api/v1/places/park-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.placeId").value("park-1"))
                .andExpect(jsonPath("$.data.manualRegionRequired").value(true));
    }
}
