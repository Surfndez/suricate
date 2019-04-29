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
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';

import {Project} from '../../model/api/project/Project';
import {projectsApiEndpoint} from '../../../app.constant';
import {ProjectRequest} from '../../model/api/project/ProjectRequest';
import {User} from '../../model/api/user/User';
import {WebsocketClient} from '../../model/api/WebsocketClient';
import {ProjectSlide} from "../../model/api/project/ProjectSlide";
import {ProjectSlideRequest} from "../../model/api/project/ProjectSlideRequest";

@Injectable()
export class HttpProjectService {

  /**
   * Constructor
   *
   * @param httpClient the http client to inject
   */
  constructor(private httpClient: HttpClient) {
  }

  /**
   * Get every dashboards and update the list
   *
   * @returns {Observable<Project[]>} The list as observable
   */
  getAll(): Observable<Project[]> {
    const url = `${projectsApiEndpoint}`;

    return this.httpClient.get<Project[]>(url);
  }

  /**
   * Add/Update a dashboard and update the subject list
   *
   * @param {ProjectRequest} projectRequest The project request
   */
  createProject(projectRequest: ProjectRequest): Observable<Project> {
    const url = `${projectsApiEndpoint}`;

    return this.httpClient.post<Project>(url, projectRequest);
  }

  /**
   * Get a dashboard by id
   *
   * @param {string} projectToken The dashboard token
   * @returns {Observable<Project>} The dashboard as observable
   */
  getOneByToken(projectToken: string): Observable<Project> {
    const url = `${projectsApiEndpoint}/${projectToken}`;

    return this.httpClient.get<Project>(url);
  }

  /**
   * Update project
   *
   * @param projectToken The project token
   * @param projectRequest The project request
   */
  editProject(projectToken: string, projectRequest: ProjectRequest): Observable<void> {
    const url = `${projectsApiEndpoint}/${projectToken}`;

    return this.httpClient.put<void>(url, projectRequest);
  }

  /**
   * Delete a project
   *
   * @param {string} projectToken
   */
  deleteProject(projectToken: string): Observable<void> {
    const url = `${projectsApiEndpoint}/${projectToken}`;

    return this.httpClient.delete<void>(url);
  }

  /**
   * Get the list of slides for a project
   *
   * @param projectToken The project token
   * @return The list of slides for this project
   */
  getProjectSlides(projectToken: string): Observable<ProjectSlide[]> {
    const url = `${projectsApiEndpoint}/${projectToken}/slides`;

    return this.httpClient.get<ProjectSlide[]>(url);
  }

  /**
   * Add a slide to a project
   *
   * @param projectToken The project token
   * @param projectSlideRequest The slide informations to add
   */
  addProjectSlide(projectToken: string, projectSlideRequest: ProjectSlideRequest): Observable<void> {
    const url = `${projectsApiEndpoint}/${projectToken}/slides`;

    return this.httpClient.post<void>(url, projectSlideRequest);
  }

  /**
   * Get the list of users for a project
   *
   * @param projectToken The project token
   */
  getProjectUsers(projectToken: string): Observable<User[]> {
    const url = `${projectsApiEndpoint}/${projectToken}/users`;

    return this.httpClient.get<User[]>(url);
  }

  /**
   * Add a user to a project
   *
   * @param {string} projectToken The projectToken
   * @param {string} username The username to add
   * @returns {Observable<Project>} The project as observable
   */
  addUserToProject(projectToken: string, username: string): Observable<void> {
    const url = `${projectsApiEndpoint}/${projectToken}/users`;

    return this.httpClient.post<void>(url, username);
  }

  /**
   * Delete a user from a project
   *
   * @param {string} projectToken The project token
   * @param {number} userId The userId
   */
  deleteUserFromProject(projectToken: string, userId: number): Observable<void> {
    const url = `${projectsApiEndpoint}/${projectToken}/users/${userId}`;

    return this.httpClient.delete<void>(url);
  }

  /**
   * Get the list of clients connected by websocket to a project
   *
   * @param projectToken The project token
   */
  getProjectWebsocketClients(projectToken: string): Observable<WebsocketClient[]> {
    const url = `${projectsApiEndpoint}/${projectToken}/websocket/clients`;
    return this.httpClient.get<WebsocketClient[]>(url);
  }

  /**
   * Get every dashboards for the current user
   *
   * @returns {Observable<Project[]>} The list as observable
   */
  getAllForCurrentUser(): Observable<Project[]> {
    const url = `${projectsApiEndpoint}/current-user`;

    return this.httpClient.get<Project[]>(url);
  }
}
