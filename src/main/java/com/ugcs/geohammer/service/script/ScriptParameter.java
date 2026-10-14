package com.ugcs.geohammer.service.script;

import ch.randelshofer.fastdoubleparser.JavaDoubleParser;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ugcs.geohammer.format.GeoData;
import com.ugcs.geohammer.format.SgyFile;
import com.ugcs.geohammer.format.TraceFile;
import com.ugcs.geohammer.model.ColumnSchema;
import com.ugcs.geohammer.model.Semantic;
import com.ugcs.geohammer.util.Check;
import com.ugcs.geohammer.util.Strings;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.util.List;

public record ScriptParameter(
        String name,
        @JsonProperty("display_name")
        String displayName,
        ParameterType type,
        @JsonProperty("default_value")
        String defaultValue,
        boolean required,
        @JsonProperty("enum_values")
        List<String> enumValues,
        @JsonProperty("min")
        Double min,
        @JsonProperty("max")
        Double max,
        @JsonProperty("visible")
        @Nullable Boolean declaredVisible,
        @Nullable String semantic
) {
    public ScriptParameter {
		name = Strings.nullToEmpty(name);
        if (Strings.isNullOrEmpty(displayName)) {
            displayName = name;
        }
		defaultValue = Strings.nullToEmpty(defaultValue);
    }

    // semantic columns and the original path take their value from each processed file
    public boolean isDerivedFromFile() {
        return semantic != null || type == ParameterType.ORIGINAL_PATH;
    }

    public boolean isVisible() {
        return declaredVisible != null ? declaredVisible : !isDerivedFromFile();
    }

    // the column with this parameter's semantic in the file; the semantic name, which is also
    // the default header, when the file has no such column
    public String getSemanticHeader(SgyFile file) {
        Check.notNull(semantic);
        if (file instanceof TraceFile) {
            return semantic;
        }
        ColumnSchema schema = GeoData.getSchema(file.getGeoData());
        String header = schema != null ? schema.getHeaderBySemantic(semantic) : null;
        return header != null ? header : semantic;
    }

    public String getLabel(String hint) {
        return displayName + hint + (required ? " *" : "");
    }

    public void validate() {
		Check.notEmpty(name, "Parameter name is missing or empty");
		Check.condition(type != null, "Parameter '" + name + "' has no type");

		switch (type) {
			case ENUM -> validateEnum();
			case INTEGER -> validateInteger();
			case ORIGINAL_PATH -> validateOriginalPath();
			default -> { }
		}
		if (semantic != null) {
			validateSemantic();
		}
		if (required && !isVisible() && !isDerivedFromFile() && defaultValue.isEmpty()) {
			throw new IllegalArgumentException(
					"Parameter '" + name + "' is required and hidden but has no 'default_value'");
		}
    }

	private void validateOriginalPath() {
		if (Boolean.TRUE.equals(declaredVisible)) {
			throw new IllegalArgumentException(
					"Parameter '" + name + "' has type ORIGINAL_PATH and cannot be visible");
		}
	}

	private void validateSemantic() {
		if (type != ParameterType.COLUMN_NAME) {
			throw new IllegalArgumentException(
					"Parameter '" + name + "' has 'semantic' but its type is not COLUMN_NAME");
		}
		if (!defaultValue.isEmpty()) {
			throw new IllegalArgumentException(
					"Parameter '" + name + "' has both 'semantic' and 'default_value'");
		}
		for (Semantic value : Semantic.values()) {
			if (value.getName().equals(semantic)) {
				return;
			}
		}
		throw new IllegalArgumentException(
				"Parameter '" + name + "': unknown semantic '" + semantic + "'");
	}

	private void validateEnum() {
		if (enumValues == null || enumValues.isEmpty()) {
			throw new IllegalArgumentException(
					"Parameter '" + name + "' has type ENUM but 'enum_values' is missing or empty");
		}
	}

	private void validateInteger() {
		if (min != null && max != null && min > max) {
			throw new IllegalArgumentException(
					"Parameter '" + name + "': min (" + formatBound(min) + ") must not exceed max (" + formatBound(max) + ")");
		}
	}

	public void validateValue(String value) {
		if (value == null || value.isEmpty()) {
			return;
		}
		switch (type) {
			case INTEGER -> {
				int parsed;
				try {
					parsed = Integer.parseInt(value);
				} catch (NumberFormatException e) {
					throw new IllegalArgumentException(
							"Parameter '" + displayName + "': '" + value + "' is not a valid integer");
				}
				if (min != null && parsed < min) {
					throw new IllegalArgumentException(
							"Parameter '" + displayName + "': value " + parsed + " is below minimum " + formatBound(min));
				}
				if (max != null && parsed > max) {
					throw new IllegalArgumentException(
							"Parameter '" + displayName + "': value " + parsed + " exceeds maximum " + formatBound(max));
				}
			}
			case DOUBLE -> {
				double parsed;
				try {
					parsed = JavaDoubleParser.parseDouble(value);
				} catch (NumberFormatException e) {
					throw new IllegalArgumentException(
							"Parameter '" + displayName + "': '" + value + "' is not a valid number");
				}
				if (min != null && parsed < min) {
					throw new IllegalArgumentException(
							"Parameter '" + displayName + "': value " + parsed + " is below minimum " + formatBound(min));
				}
				if (max != null && parsed > max) {
					throw new IllegalArgumentException(
							"Parameter '" + displayName + "': value " + parsed + " exceeds maximum " + formatBound(max));
				}
			}
			case BOOLEAN -> {
				if (!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false")) {
					throw new IllegalArgumentException(
							"Parameter '" + displayName + "': '" + value + "' is not a boolean, expected true or false");
				}
			}
			case ENUM -> {
				if (enumValues == null || !enumValues.contains(value)) {
					throw new IllegalArgumentException(
							"Parameter '" + displayName + "': '" + value + "' is not one of " + enumValues);
				}
			}
			case FILE_PATH -> {
				if (!new File(value).isFile()) {
					throw new IllegalArgumentException(
							"Parameter '" + displayName + "': file '" + value + "' does not exist");
				}
			}
			case FOLDER_PATH -> {
				if (!new File(value).isDirectory()) {
					throw new IllegalArgumentException(
							"Parameter '" + displayName + "': folder '" + value + "' does not exist");
				}
			}
			case LINE_INDEX -> {
				try {
					Integer.parseInt(value);
				} catch (NumberFormatException e) {
					throw new IllegalArgumentException(
							"Parameter '" + displayName + "': '" + value + "' is not a valid line index");
				}
			}
			default -> { }
		}
	}

	private static String formatBound(double value) {
		return value == Math.floor(value) ? String.valueOf((long) value) : String.valueOf(value);
	}

    public enum ParameterType {
        STRING, INTEGER, DOUBLE, BOOLEAN, FILE_PATH, FOLDER_PATH, COLUMN_NAME, LINE_INDEX, ENUM, ORIGINAL_PATH
    }
}
