package com.app.ai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ImproveBulletResponse {

    /** The primary improved bullet point. */
    private String improved;

    /** Three alternative rewrites for the same bullet. */
    private List<String> alternatives;

    /** Coach's explanation of why the changes improve the bullet. */
    private String explanation;
}
