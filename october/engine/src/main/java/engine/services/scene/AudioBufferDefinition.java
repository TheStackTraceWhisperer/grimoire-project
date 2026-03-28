package engine.services.scene;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AudioBufferDefinition(
  @JsonProperty("handle") String handle,
  @JsonProperty("path") String path
) {}
