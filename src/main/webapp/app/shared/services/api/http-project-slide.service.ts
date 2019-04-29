/*
 *  /*
 *  * Copyright 2012-2018 the original author or authors.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {Observable} from 'rxjs';
import {projectSlidesApiEndpoint} from '../../../app.constant';
import {ProjectSlide} from "../../model/api/project/ProjectSlide";
import {ProjectSlideRequest} from "../../model/api/project/ProjectSlideRequest";
import {ProjectWidgetPositionRequest} from "../../model/api/ProjectWidget/ProjectWidgetPositionRequest";
import {ProjectWidget} from "../../model/api/ProjectWidget/ProjectWidget";
import {ProjectWidgetRequest} from "../../model/api/ProjectWidget/ProjectWidgetRequest";

/**
 * Manage HTTP calls for project slide
 */
@Injectable()
export class HttpProjectSlideService {

  /**
   * Constructor
   *
   * @param httpClient the http client to inject
   */
  constructor(private httpClient: HttpClient) {
  }

  /**
   * Get a project slide by id
   *
   * @param projectSlideId The project slide id
   * @return The slide
   */
  getOneById(projectSlideId: number): Observable<ProjectSlide> {
    const url = `${projectSlidesApiEndpoint}/${projectSlideId}`;

    return this.httpClient.get<ProjectSlide>(url);
  }

  /**
   * Update a project slide by id
   *
   * @param projectSlideId The project slide id
   * @param projectSlideRequest The slide updated
   */
  updateSlideById(projectSlideId: number, projectSlideRequest: ProjectSlideRequest): Observable<void> {
    const url = `${projectSlidesApiEndpoint}/${projectSlideId}`;

    return this.httpClient.put<void>(url, projectSlideRequest);
  }

  /**
   * Delete a project slide by id
   *
   * @param projectSlideId The project slide id
   */
  deleteSlideById(projectSlideId: number): Observable<void> {
    const url = `${projectSlidesApiEndpoint}/${projectSlideId}`;

    return this.httpClient.delete<void>(url);
  }

  /**
   * Update the list of project widgets position for a project slide
   *
   * @param projectSlideId The project slide id
   * @param projectWidgetPositionRequests The list of positions to update
   */
  updateProjectWidgetPositions(projectSlideId: number, projectWidgetPositionRequests: ProjectWidgetPositionRequest[]): Observable<void> {
    const url = `${projectSlidesApiEndpoint}/${projectSlideId}/project-widget-positions`;

    return this.httpClient.put<void>(url, projectWidgetPositionRequests);
  }

  /**
   * Get the list of project widgets for a project slide
   *
   * @param projectSlideId The project slide id
   */
  getProjectWidgetsForSlide(projectSlideId: number): Observable<ProjectWidget[]> {
    const url = `${projectSlidesApiEndpoint}/${projectSlideId}/project-widgets`;

    return this.httpClient.get<ProjectWidget[]>(url);
  }

  /**
   * Add a new widget to a slide
   *
   * @param projectSlideId The project slide id
   * @param projectWidgetRequest The project widget to add
   */
  addProjectWidgetToProject(projectSlideId: number, projectWidgetRequest: ProjectWidgetRequest): Observable<ProjectWidget> {
    const url = `${projectSlidesApiEndpoint}/${projectSlideId}/project-widgets`;

    return this.httpClient.post<ProjectWidget>(url, projectWidgetRequest);
  }

  /**
   * Add or update a screenshot for a slide
   *
   * @param projectSlideId The project slide id
   * @param screenshotFile The screenshot file
   */
  addOrUpdateProjectScreenshot(projectSlideId: number, screenshotFile: File): Observable<void> {
    const url = `${projectSlidesApiEndpoint}/${projectSlideId}/screenshot`;

    const formData: FormData = new FormData();
    formData.append('screenshot', screenshotFile, screenshotFile.name);

    const httpHeaders: HttpHeaders = new HttpHeaders();
    httpHeaders.append('Content-Type', 'multipart/form-data');
    httpHeaders.append('Accept', 'application/json');

    return this.httpClient.put<void>(url, formData, {headers: httpHeaders});
  }
}
