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
import io.suricate.monitoring.model.dto.api.widget.CategoryResponseDto;
import io.suricate.monitoring.model.dto.api.widget.WidgetResponseDto;
import io.suricate.monitoring.model.dto.api.widgetconfiguration.WidgetConfigurationResponseDto;
import io.suricate.monitoring.model.entity.WidgetConfiguration;
import io.suricate.monitoring.model.entity.widget.Category;
import io.suricate.monitoring.model.entity.widget.Widget;
import io.suricate.monitoring.service.api.CategoryService;
import io.suricate.monitoring.service.api.WidgetConfigurationService;
import io.suricate.monitoring.service.api.WidgetService;
import io.suricate.monitoring.service.mapper.CategoryMapper;
import io.suricate.monitoring.service.mapper.WidgetConfigurationMapper;
import io.suricate.monitoring.service.mapper.WidgetMapper;
import io.suricate.monitoring.utils.exception.NoContentException;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

/**
 * The widget controller
 */
@RestController
@RequestMapping("/api")
@Api(value = "Category Controller", tags = {"Categories"})
public class CategoryController {

    /**
     * The category service
     */
    private final CategoryService categoryService;

    /**
     * The category dto/object mapper
     */
    private final CategoryMapper categoryMapper;

    /**
     * The configuration service
     */
    private final WidgetConfigurationService widgetConfigurationService;

    /**
     * The configuration mapper
     */
    private final WidgetConfigurationMapper widgetConfigurationMapper;

    /**
     * The widget service to inject
     */
    private final WidgetService widgetService;

    /**
     * The widget mapper to inject
     */
    private final WidgetMapper widgetMapper;

    /**
     * Constructor
     *
     * @param categoryService            The category service to inject
     * @param categoryMapper             The category mapper to inject
     * @param widgetConfigurationService The configuration service to inject
     * @param widgetConfigurationMapper  The configuration mapper
     * @param widgetService              The widget service to inject
     * @param widgetMapper               The widget mapper to inject
     */
    @Autowired
    public CategoryController(final CategoryService categoryService,
                              final CategoryMapper categoryMapper,
                              final WidgetConfigurationService widgetConfigurationService,
                              final WidgetConfigurationMapper widgetConfigurationMapper,
                              final WidgetService widgetService,
                              final WidgetMapper widgetMapper) {
        this.categoryService = categoryService;
        this.categoryMapper = categoryMapper;
        this.widgetConfigurationService = widgetConfigurationService;
        this.widgetConfigurationMapper = widgetConfigurationMapper;
        this.widgetService = widgetService;
        this.widgetMapper = widgetMapper;
    }


    /**
     * Get the list of widget categories
     *
     * @return A list of category
     */
    @ApiOperation(value = "Get the full list of widget categories", response = CategoryResponseDto.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Ok", response = CategoryResponseDto.class, responseContainer = "List"),
        @ApiResponse(code = 204, message = "No Content"),
        @ApiResponse(code = 401, message = "Authentication error, token expired or invalid", response = ApiErrorDto.class),
        @ApiResponse(code = 403, message = "You don't have permission to access to this resource", response = ApiErrorDto.class)
    })
    @ApiPageable
    @GetMapping(value = "/v1/categories")
    @PreAuthorize("hasRole('ROLE_USER')")
    @Transactional
    public Page<CategoryResponseDto> getCategories(@ApiParam(name = "search", value = "Search keyword")
                                                   @RequestParam(value = "search", required = false) String search,
                                                   Pageable pageable) {
        Page<Category> categoriesPaged = categoryService.getAll(search, pageable);
        return categoriesPaged.map(categoryMapper::toCategoryDtoDefault);
    }

    /**
     * Return the list of configurations associated to the category
     */
    @ApiOperation(value = "Get the list of configurations for a category", response = WidgetConfigurationResponseDto.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Ok", response = WidgetConfigurationResponseDto.class, responseContainer = "List"),
        @ApiResponse(code = 204, message = "No Content"),
        @ApiResponse(code = 401, message = "Authentication error, token expired or invalid", response = ApiErrorDto.class),
        @ApiResponse(code = 403, message = "You don't have permission to access to this resource", response = ApiErrorDto.class)
    })
    @GetMapping(value = "/v1/categories/{categoryId}/configurations")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<List<WidgetConfigurationResponseDto>> getConfigurationsByCategory(@ApiParam(name = "categoryId", value = "The category id", required = true)
                                                                                            @PathVariable("categoryId") final Long categoryId) {
        Optional<List<WidgetConfiguration>> configurationsOptional = widgetConfigurationService.getConfigurationForCategory(categoryId);

        if (!configurationsOptional.isPresent()) {
            throw new NoContentException(WidgetConfiguration.class);
        }

        return ResponseEntity
            .ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(widgetConfigurationMapper.toConfigurationDtosDefault(configurationsOptional.get()));
    }

    /**
     * Get every widget for a category
     *
     * @param categoryId The category id
     * @return The list of related widgets
     */
    @ApiOperation(value = "Get the list of widgets by category id", response = WidgetResponseDto.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Ok", response = WidgetResponseDto.class, responseContainer = "List"),
        @ApiResponse(code = 204, message = "No Content"),
        @ApiResponse(code = 401, message = "Authentication error, token expired or invalid", response = ApiErrorDto.class),
        @ApiResponse(code = 403, message = "You don't have permission to access to this resource", response = ApiErrorDto.class),
        @ApiResponse(code = 404, message = "Category not found", response = ApiErrorDto.class)
    })
    @GetMapping(value = "/v1/categories/{categoryId}/widgets")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<List<WidgetResponseDto>> getWidgetByCategory(@ApiParam(name = "categoryId", value = "The category id", required = true)
                                                                       @PathVariable("categoryId") Long categoryId) {
        Optional<List<Widget>> widgetsOptional = widgetService.getWidgetsByCategory(categoryId);
        if (!widgetsOptional.isPresent()) {
            throw new NoContentException(Widget.class);
        }

        return ResponseEntity
            .ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(widgetMapper.toWidgetDtosDefault(widgetsOptional.get()));
    }
}
