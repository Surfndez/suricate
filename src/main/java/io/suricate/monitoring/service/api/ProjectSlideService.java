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

package io.suricate.monitoring.service.api;

import io.suricate.monitoring.model.entity.Asset;
import io.suricate.monitoring.model.entity.project.Project;
import io.suricate.monitoring.model.entity.project.ProjectSlide;
import io.suricate.monitoring.repository.ProjectSlideRepository;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

/**
 * Service used to manage projects slides
 */
@Service
public class ProjectSlideService {

    /**
     * Project slide repository
     */
    private final ProjectSlideRepository projectSlideRepository;

    /**
     * The asset service
     */
    private final AssetService assetService;

    /**
     * Constructor
     *
     * @param projectSlideRepository The project slide repository to inject
     */
    @Autowired
    public ProjectSlideService(final ProjectSlideRepository projectSlideRepository,
                               final AssetService assetService) {
        this.projectSlideRepository = projectSlideRepository;
        this.assetService = assetService;
    }

    /**
     * Get a project slide by id
     *
     * @param id The id of the project slide to retrieve
     * @return The project slide as Optional
     */
    public Optional<ProjectSlide> getOneById(final Long id) {
        if (projectSlideRepository.existsById(id)) {
            return Optional.of(projectSlideRepository.getOne(id));
        }

        return Optional.empty();
    }


    /**
     * Add a slide to a project
     *
     * @param project      The project
     * @param maxColumn    The number of column in the slide
     * @param widgetHeight The widget height in px inside the slide
     * @param cssStyle     The css style
     */
    public void addSlideToProject(final Project project, final Integer maxColumn, final Integer widgetHeight, final String cssStyle) {
        ProjectSlide projectSlide = new ProjectSlide();
        projectSlide.setProject(project);
        projectSlide.setMaxColumn(maxColumn);
        projectSlide.setWidgetHeight(widgetHeight);
        projectSlide.setCssStyle(cssStyle);

        projectSlideRepository.save(projectSlide);
    }

    /**
     * Update a slide to a project
     *
     * @param maxColumn    The number of column in the slide
     * @param widgetHeight The widget height in px inside the slide
     * @param cssStyle     The css style
     */
    public void updateSlide(final ProjectSlide projectSlide, final Integer maxColumn, final Integer widgetHeight, final String cssStyle) {
        if (maxColumn != null) {
            projectSlide.setMaxColumn(maxColumn);
        }
        if (widgetHeight != null) {
            projectSlide.setWidgetHeight(widgetHeight);
        }
        if (StringUtils.isNotBlank(cssStyle)) {
            projectSlide.setCssStyle(cssStyle);
        }

        projectSlideRepository.save(projectSlide);
    }

    /**
     * Delete a project slide
     *
     * @param projectSlideId The project slide
     */
    public void deleteSlide(final Long projectSlideId) {
        projectSlideRepository.deleteById(projectSlideId);
    }

    /**
     * Add or update a screenshot for a project
     *
     * @param projectSlide The project slide
     * @param screenshot   The screenshot to add
     */
    public void addOrUpdateScreenshot(final ProjectSlide projectSlide, final MultipartFile screenshot) throws IOException {
        Asset screenshotAsset = new Asset();
        screenshotAsset.setContent(screenshot.getBytes());
        screenshotAsset.setContentType(screenshot.getContentType());
        screenshotAsset.setSize(screenshot.getSize());

        if (projectSlide.getScreenshot() != null) {
            screenshotAsset.setId(projectSlide.getScreenshot().getId());
            assetService.save(screenshotAsset);
        } else {
            assetService.save(screenshotAsset);
            projectSlide.setScreenshot(screenshotAsset);
            projectSlideRepository.save(projectSlide);
        }
    }
}
