package com.aviation.dto;

import com.aviation.entity.SegmentType;
import com.aviation.entity.TransportationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RouteDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<TransportationSegmentDTO> segments = new ArrayList<>();

    public record TransportationSegmentDTO(
            Long transportationId,
            TransportationType type,
            LocationDTO from,
            LocationDTO to,
            SegmentType segmentType
    ) implements Serializable {
    }
}
