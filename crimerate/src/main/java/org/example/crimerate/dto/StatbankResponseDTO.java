package org.example.crimerate.dto;

import java.util.List;

public record StatbankResponseDTO(List<DataRow> data) {

    public record DataRow(List<String> key, List<String> values) {
    }
}
