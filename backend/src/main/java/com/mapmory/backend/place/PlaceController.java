package com.mapmory.backend.place;

import com.mapmory.backend.travelrecord.dto.TravelRecordResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/api/v1/places")
public class PlaceController {

    private final GeoapifyClient geoapifyClient;
    private final PlaceSelectionService placeSelectionService;

    public PlaceController(GeoapifyClient geoapifyClient, PlaceSelectionService placeSelectionService) {
        this.geoapifyClient = geoapifyClient;
        this.placeSelectionService = placeSelectionService;
    }

    @GetMapping("/search")
    public ResponseEntity<TravelRecordResponse<List<PlaceCandidate>>> search(
            @RequestParam @NotBlank @Size(max = 100) String query
    ) {
        return ResponseEntity.ok(TravelRecordResponse.of(geoapifyClient.search(query.strip())));
    }

    @GetMapping("/{placeId}")
    public ResponseEntity<TravelRecordResponse<PlaceSelectionResponse>> select(@PathVariable String placeId) {
        return ResponseEntity.ok(TravelRecordResponse.of(placeSelectionService.select(placeId)));
    }
}
