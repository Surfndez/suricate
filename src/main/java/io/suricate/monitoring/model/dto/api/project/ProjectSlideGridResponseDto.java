package io.suricate.monitoring.model.dto.api.project;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Grid properties of the dashboard slide
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "ProjectSlideGridResponse", description = "Grid properties of the dashboard slide")
public class ProjectSlideGridResponseDto {

    /**
     * Number of column in the slide
     */
    @ApiModelProperty(value = "The number of columns in the slide")
    private Integer maxColumn;
    /**
     * The height for widgets contained
     */
    @ApiModelProperty(value = "The height in pixel of the widget")
    private Integer widgetHeight;
    /**
     * The global css for the dashboard
     */
    @ApiModelProperty(value = "The css style of the dashboard slide grid")
    private String cssStyle;

}
