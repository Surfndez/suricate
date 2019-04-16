/*
 * Copyright 2012-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.suricate.monitoring.service.mapper;

import io.suricate.monitoring.model.dto.api.project.ProjectSlideRequestDto;
import io.suricate.monitoring.model.dto.api.project.ProjectSlideResponseDto;
import io.suricate.monitoring.model.entity.project.ProjectSlide;
import io.suricate.monitoring.service.api.LibraryService;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Interface that manage the generation DTO/Model objects for project slide class
 */
@Component
@Mapper(componentModel = "spring")
public abstract class ProjectSlideMapper {

    /**
     * The library service
     */
    @Autowired
    protected LibraryService libraryService;

    /* ************************* TO DTO ********************************************** */

    /* ******************************************************* */
    /*                  Simple Mapping                         */
    /* ******************************************************* */

    /**
     * Transform a project slide into a ProjectSlideResponseDto
     *
     * @param projectSlide The projectSlide to transform
     * @return The related project slide DTO
     */
    @Named("toProjectSlideDtoDefault")
    @Mapping(target = "gridProperties.maxColumn", source = "projectSlide.maxColumn")
    @Mapping(target = "gridProperties.widgetHeight", source = "projectSlide.widgetHeight")
    @Mapping(target = "gridProperties.cssStyle", source = "projectSlide.cssStyle")
    @Mapping(target = "screenshotToken", expression = "java( projectSlide.getScreenshot() != null ? io.suricate.monitoring.utils.IdUtils.encrypt(projectSlide.getScreenshot().getId()) : null )")
    @Mapping(target = "librariesToken", expression = "java(libraryService.getLibrariesToken(projectSlide.getWidgets()))")
    public abstract ProjectSlideResponseDto toProjectSlideDtoDefault(ProjectSlide projectSlide);

    /* ******************************************************* */
    /*                    List Mapping                         */
    /* ******************************************************* */

    /**
     * Transform a list of project slides into a list of projectSlideDtos
     *
     * @param projectSlides The list of project slide to tranform
     * @return The related list of dto object
     */
    @Named("toProjectSlideDtosDefault")
    @IterableMapping(qualifiedByName = "toProjectSlideDtoDefault")
    public abstract List<ProjectSlideResponseDto> toProjectSlideDtosDefault(List<ProjectSlide> projectSlides);


    /* ************************* TO MODEL **************************************** */

    /* ******************************************************* */
    /*                  Simple Mapping                         */
    /* ******************************************************* */

    /**
     * Transform a projectSlideRequestDto into a new projectSlide
     *
     * @param projectSlideRequestDto The project slide to transform
     * @return The related project domain object
     */
    @Named("toNewProjectSlide")
    public abstract ProjectSlide toNewProjectSlide(ProjectSlideRequestDto projectSlideRequestDto);
}
