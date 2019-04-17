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

package io.suricate.monitoring.controllers.api;

import io.suricate.monitoring.model.dto.api.error.ApiErrorDto;
import io.suricate.monitoring.model.dto.api.project.ProjectSlideRequestDto;
import io.suricate.monitoring.model.dto.api.project.ProjectSlideResponseDto;
import io.suricate.monitoring.model.dto.api.projectwidget.ProjectWidgetPositionRequestDto;
import io.suricate.monitoring.model.dto.api.projectwidget.ProjectWidgetRequestDto;
import io.suricate.monitoring.model.dto.api.projectwidget.ProjectWidgetResponseDto;
import io.suricate.monitoring.model.entity.project.ProjectSlide;
import io.suricate.monitoring.model.entity.project.ProjectWidget;
import io.suricate.monitoring.model.enums.ApiErrorEnum;
import io.suricate.monitoring.service.api.ProjectService;
import io.suricate.monitoring.service.api.ProjectSlideService;
import io.suricate.monitoring.service.api.ProjectWidgetService;
import io.suricate.monitoring.service.mapper.ProjectSlideMapper;
import io.suricate.monitoring.service.mapper.ProjectWidgetMapper;
import io.suricate.monitoring.utils.exception.ApiException;
import io.suricate.monitoring.utils.exception.InvalidFileException;
import io.suricate.monitoring.utils.exception.ObjectNotFoundException;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import springfox.documentation.annotations.ApiIgnore;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Optional;

/**
 * Project slides controller
 */
@RestController
@RequestMapping("/api")
@Api(value = "Project Slide Controller", tags = {"Project Slides"})
public class ProjectSlideController {

    /**
     * The project slide service
     */
    private final ProjectSlideService projectSlideService;

    /**
     * The project service
     */
    private final ProjectService projectService;

    /**
     * The project widget service
     */
    private final ProjectWidgetService projectWidgetService;

    /**
     * The project slide mapper
     */
    private final ProjectSlideMapper projectSlideMapper;

    /**
     * The project widget mapper
     */
    private final ProjectWidgetMapper projectWidgetMapper;

    /**
     * Constructor for dependency injection
     *
     * @param projectSlideService  The project slide service to inject
     * @param projectService       The project service to inject
     * @param projectWidgetService The project widget service
     * @param projectSlideMapper   The project slide mapper
     * @param projectWidgetMapper  The project widget mapper
     */
    @Autowired
    public ProjectSlideController(final ProjectSlideService projectSlideService,
                                  final ProjectService projectService,
                                  final ProjectWidgetService projectWidgetService,
                                  final ProjectSlideMapper projectSlideMapper,
                                  final ProjectWidgetMapper projectWidgetMapper) {
        this.projectSlideService = projectSlideService;
        this.projectService = projectService;
        this.projectWidgetService = projectWidgetService;
        this.projectSlideMapper = projectSlideMapper;
        this.projectWidgetMapper = projectWidgetMapper;
    }


    /* ***************************************************************************************************************/
    /*                                   Project Slide PART                                                          */
    /* ***************************************************************************************************************/

    /**
     * Get a project slide by id
     *
     * @param projectSlideId The id of the project slide
     * @return The project slide
     */
    @ApiOperation(value = "Retrieve a project slide by id", response = ProjectSlideResponseDto.class, nickname = "getSlideById")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Ok", response = ProjectSlideResponseDto.class),
        @ApiResponse(code = 401, message = "Authentication error, token expired or invalid", response = ApiErrorDto.class),
        @ApiResponse(code = 403, message = "You don't have permission to access to this resource", response = ApiErrorDto.class),
        @ApiResponse(code = 404, message = "Project not found", response = ApiErrorDto.class)
    })
    @GetMapping(value = "/v1/project-slides/{projectSlideId}")
    @Transactional
    public ResponseEntity<ProjectSlideResponseDto> getSlideById(@ApiParam(name = "projectSlideId", value = "The project slide id", required = true)
                                                                @PathVariable("projectSlideId") Long projectSlideId) {
        Optional<ProjectSlide> projectSlideOptional = projectSlideService.getOneById(projectSlideId);
        if (!projectSlideOptional.isPresent()) {
            throw new ObjectNotFoundException(ProjectSlide.class, projectSlideId);
        }

        return ResponseEntity
            .ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(projectSlideMapper.toProjectSlideDtoDefault(projectSlideOptional.get()));
    }

    /**
     * Update a slide
     *
     * @param authentication         The connected user
     * @param projectSlideRequestDto Project slide request dto
     */
    @ApiOperation(value = "Update a slide")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Ok"),
        @ApiResponse(code = 401, message = "Authentication error, token expired or invalid", response = ApiErrorDto.class),
        @ApiResponse(code = 403, message = "You don't have permission to access to this resource", response = ApiErrorDto.class),
        @ApiResponse(code = 404, message = "Project not found", response = ApiErrorDto.class)
    })
    @PutMapping(value = "/v1/project-slides/{projectSlideId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<Void> updateSlide(@ApiIgnore OAuth2Authentication authentication,
                                            @ApiParam(name = "projectSlideId", value = "The project slide id", required = true)
                                            @PathVariable("projectSlideId") Long projectSlideId,
                                            @ApiParam(name = "projectSlideRequestDto", value = "The slide to update", required = true)
                                            @RequestBody ProjectSlideRequestDto projectSlideRequestDto) {

        Optional<ProjectSlide> projectSlideOptional = projectSlideService.getOneById(projectSlideId);
        if (!projectSlideOptional.isPresent()) {
            throw new ObjectNotFoundException(ProjectSlide.class, projectSlideId);
        }

        ProjectSlide projectSlide = projectSlideOptional.get();
        if (!projectService.isConnectedUserCanAccessToProject(projectSlide.getProject(), authentication.getUserAuthentication())) {
            throw new ApiException("The user is not allowed to modify this resource", ApiErrorEnum.NOT_AUTHORIZED);
        }

        projectSlideService.updateSlide(
            projectSlide,
            projectSlideRequestDto.getMaxColumn(),
            projectSlideRequestDto.getWidgetHeight(),
            projectSlideRequestDto.getCssStyle()
        );
        return ResponseEntity.ok().build();
    }

    /**
     * Method used to delete a slide
     *
     * @param authentication The connected user
     * @param projectSlideId The slide id to delete
     */
    @ApiOperation(value = "Delete a slide")
    @ApiResponses(value = {
        @ApiResponse(code = 204, message = "Slide deleted"),
        @ApiResponse(code = 401, message = "Authentication error, token expired or invalid", response = ApiErrorDto.class),
        @ApiResponse(code = 403, message = "You don't have permission to access to this resource", response = ApiErrorDto.class),
        @ApiResponse(code = 404, message = "Project not found", response = ApiErrorDto.class)
    })
    @DeleteMapping(value = "/v1/project-slides/{projectSlideId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<Void> deleteOneById(@ApiIgnore OAuth2Authentication authentication,
                                              @ApiParam(name = "slideId", value = "The slide id", required = true)
                                              @PathVariable("projectSlideId") Long projectSlideId) {
        Optional<ProjectSlide> projectSlideOptional = projectSlideService.getOneById(projectSlideId);
        if (!projectSlideOptional.isPresent()) {
            throw new ObjectNotFoundException(ProjectSlide.class, projectSlideId);
        }

        ProjectSlide projectSlide = projectSlideOptional.get();
        if (!projectService.isConnectedUserCanAccessToProject(projectSlide.getProject(), authentication.getUserAuthentication())) {
            throw new ApiException("The user is not allowed to delete this resource", ApiErrorEnum.NOT_AUTHORIZED);
        }

        projectSlideService.deleteSlide(projectSlideId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Add/Update a project slide screenshot
     *
     * @param projectSlideId The project slide id
     */
    @ApiOperation(value = "Add/Update a project slide screenshot")
    @ApiResponses(value = {
        @ApiResponse(code = 204, message = "Screenshot updated"),
        @ApiResponse(code = 401, message = "Authentication error, token expired or invalid", response = ApiErrorDto.class),
        @ApiResponse(code = 403, message = "You don't have permission to access to this resource", response = ApiErrorDto.class),
        @ApiResponse(code = 404, message = "Project slide not found", response = ApiErrorDto.class)
    })
    @PutMapping(value = "/v1/project-slides/{projectSlideId}/screenshot")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<Void> updateProjectSlideScreenshot(@ApiIgnore OAuth2Authentication authentication,
                                                             @ApiParam(name = "projectSlideId", value = "The project slide id", required = true)
                                                             @PathVariable("projectSlideId") Long projectSlideId,
                                                             @ApiParam(name = "screenshot", value = "The screenshot to insert", required = true)
                                                             @RequestParam MultipartFile screenshot) {

        Optional<ProjectSlide> projectSlideOptional = projectSlideService.getOneById(projectSlideId);
        if (!projectSlideOptional.isPresent()) {
            throw new ObjectNotFoundException(ProjectSlide.class, projectSlideId);
        }

        ProjectSlide projectSlide = projectSlideOptional.get();
        if (!projectService.isConnectedUserCanAccessToProject(projectSlide.getProject(), authentication.getUserAuthentication())) {
            throw new ApiException("The user is not allowed to modify this resource", ApiErrorEnum.NOT_AUTHORIZED);
        }

        try {
            projectSlideService.addOrUpdateScreenshot(projectSlide, screenshot);
        } catch (IOException e) {
            throw new InvalidFileException(screenshot.getOriginalFilename(), ProjectSlide.class, projectSlideId);
        }

        return ResponseEntity.noContent().build();
    }


    /* ***************************************************************************************************************/
    /*                                   Project Widget PART                                                         */
    /* ***************************************************************************************************************/

    /**
     * Get the list of project widgets for a project slide
     */
    @ApiOperation(value = "Get the full list of project widgets for a project slide", response = ProjectWidgetResponseDto.class, nickname = "getProjectWidgetsForProjectSlide")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Ok", response = ProjectWidgetResponseDto.class, responseContainer = "List"),
        @ApiResponse(code = 204, message = "No Content"),
        @ApiResponse(code = 401, message = "Authentication error, token expired or invalid", response = ApiErrorDto.class),
        @ApiResponse(code = 403, message = "You don't have permission to access to this resource", response = ApiErrorDto.class)
    })
    @GetMapping(value = "/v1/project-slides/{projectSlideId}/project-widgets")
    @Transactional
    public ResponseEntity<List<ProjectWidgetResponseDto>> getProjectWidgetsForProjectSlide(@ApiParam(name = "projectSlideId", value = "The project slide id", required = true)
                                                                                           @PathVariable("projectSlideId") Long projectSlideId) {
        Optional<ProjectSlide> projectSlideOptional = projectSlideService.getOneById(projectSlideId);
        if (!projectSlideOptional.isPresent()) {
            throw new ObjectNotFoundException(ProjectSlide.class, projectSlideId);
        }

        ProjectSlide projectSlide = projectSlideOptional.get();
        if (projectSlide.getWidgets().isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity
            .ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(projectWidgetMapper.toProjectWidgetDtosDefault(projectSlide.getWidgets()));
    }

    /**
     * Add a new widget to a project slide
     *
     * @param authentication          The connected user
     * @param projectSlideId          The project slide id
     * @param projectWidgetRequestDto The projectWidget to add
     */
    @ApiOperation(value = "Add a new widget to a project slide", response = ProjectWidgetResponseDto.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Ok", response = ProjectWidgetResponseDto.class),
        @ApiResponse(code = 401, message = "Authentication error, token expired or invalid", response = ApiErrorDto.class),
        @ApiResponse(code = 403, message = "You don't have permission to access to this resource", response = ApiErrorDto.class),
        @ApiResponse(code = 404, message = "Project slide not found", response = ApiErrorDto.class)
    })
    @PostMapping(value = "/v1/project-slides/{projectSlideId}/project-widgets")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Transactional
    public ResponseEntity<ProjectWidgetResponseDto> addProjectWidgetToProjectSlide(@ApiIgnore OAuth2Authentication authentication,
                                                                                   @ApiParam(name = "projectSlideId", value = "The project slide id", required = true)
                                                                                   @PathVariable("projectSlideId") Long projectSlideId,
                                                                                   @ApiParam(name = "projectWidgetDto", value = "The project widget info's", required = true)
                                                                                   @RequestBody ProjectWidgetRequestDto projectWidgetRequestDto) {
        Optional<ProjectSlide> projectSlideOptional = projectSlideService.getOneById(projectSlideId);
        if (!projectSlideOptional.isPresent()) {
            throw new ObjectNotFoundException(ProjectSlide.class, projectSlideId);
        }

        ProjectSlide projectSlide = projectSlideOptional.get();
        if (!projectService.isConnectedUserCanAccessToProject(projectSlide.getProject(), authentication)) {
            throw new ApiException("The user is not allowed to modify this resource", ApiErrorEnum.NOT_AUTHORIZED);
        }

        ProjectWidget projectWidget = projectWidgetMapper.toNewProjectWidget(projectWidgetRequestDto, projectSlideId);
        projectWidgetService.addProjectWidget(projectWidget);

        URI resourceLocation = ServletUriComponentsBuilder
            .fromCurrentContextPath()
            .path("/api/projectWidgets/" + projectWidget.getId())
            .build()
            .toUri();

        return ResponseEntity
            .created(resourceLocation)
            .contentType(MediaType.APPLICATION_JSON)
            .body(projectWidgetMapper.toProjectWidgetDtoDefault(projectWidget));
    }

    /**
     * Update the list of widget positions for a Slide
     *
     * @param authentication                   The connected user
     * @param projectSlideId                   The slide id to update
     * @param projectWidgetPositionRequestDtos The list of project widget positions
     */
    @ApiOperation(value = "Update the project widget positions for a Slide")
    @ApiResponses(value = {
        @ApiResponse(code = 204, message = "Widget position updated"),
        @ApiResponse(code = 401, message = "Authentication error, token expired or invalid", response = ApiErrorDto.class),
        @ApiResponse(code = 403, message = "You don't have permission to access to this resource", response = ApiErrorDto.class),
        @ApiResponse(code = 404, message = "Project slide not found", response = ApiErrorDto.class)
    })
    @PutMapping(value = "/v1/project-slides/{projectSlideId}/project-widget-positions")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<Void> updateProjectWidgetsPositionForProject(@ApiIgnore OAuth2Authentication authentication,
                                                                       @ApiParam(name = "projectSlideId", value = "The project slide id", required = true)
                                                                       @PathVariable("projectSlideId") Long projectSlideId,
                                                                       @ApiParam(name = "projectWidgetPositionRequestDtos", value = "The list of the new positions", required = true)
                                                                       @RequestBody List<ProjectWidgetPositionRequestDto> projectWidgetPositionRequestDtos) {
        Optional<ProjectSlide> projectSlideOptional = projectSlideService.getOneById(projectSlideId);
        if (!projectSlideOptional.isPresent()) {
            throw new ObjectNotFoundException(ProjectSlide.class, projectSlideId);
        }

        ProjectSlide projectSlide = projectSlideOptional.get();
        if (!projectService.isConnectedUserCanAccessToProject(projectSlide.getProject(), authentication.getUserAuthentication())) {
            throw new ApiException("The user is not allowed to modify this resource", ApiErrorEnum.NOT_AUTHORIZED);
        }

        projectWidgetService.updateWidgetPositionByProjectSlide(projectSlide, projectWidgetPositionRequestDtos);
        return ResponseEntity.noContent().build();
    }
}
