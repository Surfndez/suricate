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

import io.suricate.monitoring.configuration.swagger.ApiPageable;
import io.suricate.monitoring.model.dto.api.error.ApiErrorDto;
import io.suricate.monitoring.model.dto.api.widget.WidgetRequestDto;
import io.suricate.monitoring.model.dto.api.widget.WidgetResponseDto;
import io.suricate.monitoring.model.entity.widget.Widget;
import io.suricate.monitoring.service.api.WidgetService;
import io.suricate.monitoring.service.mapper.WidgetMapper;
import io.suricate.monitoring.utils.exception.ObjectNotFoundException;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.security.PermitAll;
import java.util.Optional;

/**
 * The widget controller
 */
@RestController
@RequestMapping("/api")
@Api(value = "Widget Controller", tags = {"Widgets"})
public class WidgetController {

    /**
     * Widget service
     */
    private final WidgetService widgetService;

    /**
     * The widget mapper
     */
    private final WidgetMapper widgetMapper;

    /**
     * Constructor
     *
     * @param widgetService Widget service to inject
     * @param widgetMapper  The widget mapper
     */
    @Autowired
    public WidgetController(final WidgetService widgetService,
                            final WidgetMapper widgetMapper) {
        this.widgetService = widgetService;
        this.widgetMapper = widgetMapper;
    }

    /**
     * Get the list of widgets
     *
     * @return The list of widgets
     */
    @ApiOperation(value = "Get the full list of widgets", response = WidgetResponseDto.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Ok", response = WidgetResponseDto.class, responseContainer = "List"),
        @ApiResponse(code = 204, message = "No Content"),
        @ApiResponse(code = 401, message = "Authentication error, token expired or invalid", response = ApiErrorDto.class),
        @ApiResponse(code = 403, message = "You don't have permission to access to this resource", response = ApiErrorDto.class)
    })
    @ApiPageable
    @GetMapping(value = "/v1/widgets")
    @PreAuthorize("hasRole('ROLE_USER')")
    public Page<WidgetResponseDto> getWidgets(@ApiParam(name = "search", value = "Search keyword")
                                              @RequestParam(value = "search", required = false) String search,
                                              Pageable pageable) {
        Page<Widget> widgetsPaged = widgetService.getAll(search, pageable);
        return widgetsPaged.map(widgetMapper::toWidgetDtoDefault);
    }

    /**
     * Get a widget
     *
     * @param widgetId The widget id to update
     */
    @ApiOperation(value = "Retrieve a widget by id", response = WidgetResponseDto.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Ok", response = WidgetResponseDto.class),
        @ApiResponse(code = 401, message = "Authentication error, token expired or invalid", response = ApiErrorDto.class),
        @ApiResponse(code = 403, message = "You don't have permission to access to this resource", response = ApiErrorDto.class),
        @ApiResponse(code = 404, message = "Widget not found", response = ApiErrorDto.class)
    })
    @GetMapping(value = "/v1/widgets/{widgetId}")
    @PermitAll
    public ResponseEntity<WidgetResponseDto> getOneById(@ApiParam(name = "widgetId", value = "The widget id", required = true)
                                                        @PathVariable("widgetId") Long widgetId) {
        Widget widget = widgetService.findOne(widgetId);
        if (widget == null) {
            throw new ObjectNotFoundException(Widget.class, widgetId);
        }

        return ResponseEntity
            .ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(widgetMapper.toWidgetDtoDefault(widget));
    }

    /**
     * Update a widget
     *
     * @param widgetId         The widget id to update
     * @param widgetRequestDto The object holding changes
     * @return The widget dto changed
     */
    @ApiOperation(value = "Update a widget by id")
    @ApiResponses(value = {
        @ApiResponse(code = 204, message = "Widget updated"),
        @ApiResponse(code = 401, message = "Authentication error, token expired or invalid", response = ApiErrorDto.class),
        @ApiResponse(code = 403, message = "You don't have permission to access to this resource", response = ApiErrorDto.class),
        @ApiResponse(code = 404, message = "Widget not found", response = ApiErrorDto.class)
    })
    @PutMapping(value = "/v1/widgets/{widgetId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> updateWidget(@ApiParam(name = "widgetId", value = "The widget id", required = true)
                                             @PathVariable("widgetId") Long widgetId,
                                             @ApiParam(name = "widgetRequestDto", value = "The widget with modifications", required = true)
                                             @RequestBody WidgetRequestDto widgetRequestDto) {
        Optional<Widget> widgetOptional = widgetService.updateWidget(widgetId, widgetRequestDto);
        if (!widgetOptional.isPresent()) {
            throw new ObjectNotFoundException(Widget.class, widgetId);
        }

        return ResponseEntity.noContent().build();
    }
}
