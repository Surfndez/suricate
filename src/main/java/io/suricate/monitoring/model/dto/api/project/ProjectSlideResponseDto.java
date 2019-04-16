package io.suricate.monitoring.model.dto.api.project;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Define a slide in a dashboard
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "ProjectSlideResponse", description = "Describe a slide of a project/dashboard")
public class ProjectSlideResponseDto {

    /**
     * The slide id
     */
    @ApiModelProperty(value = "The project slide id", required = true)
    private Long id;
    /**
     * the grid properties for a dashboard
     */
    @ApiModelProperty(value = "The grid properties of the slide")
    private ProjectSlideGridResponseDto gridProperties;
    /**
     * A representation by an image of the dashboard
     */
    @ApiModelProperty(value = "A representation by an image of the slide")
    private String screenshotToken;
    /**
     * The librairies related
     */
    @ApiModelProperty(value = "The list of the related JS libraries used for the execution of the widgets", dataType = "java.util.List")
    private List<String> librariesToken = new ArrayList<>();
}
