package io.lavagna.model;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProjectMetadata {
    private final Map<Integer, CardLabel> labels;
    private final Map<Integer, Map<Integer, LabelListValueWithMetadata>> labelListValues;
    private final Map<ColumnDefinition, BoardColumnDefinition> columnsDefinition;
}
