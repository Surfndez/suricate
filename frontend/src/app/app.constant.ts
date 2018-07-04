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

import {environment} from '../environments/environment';

/* ************************************************************************ */
/*                  Global CONSTANTS                                        */
/* ************************************************************************ */

/**
 * The global app version
 * @type {string}
 */
export const appVersion = `${environment.VERSION}`;

/**
 * The global app environment
 * @type {string}
 */
export const appEnv = `${environment.ENVIRONMENT}`;

/* ************************************************************************ */
/*                  HTTP CONSTANTS                                          */
/* ************************************************************************ */

/**
 * The base API url
 * @type {string}
 */
export const baseApiEndpoint = `${process.env.BASE_URL}/api`;
/**
 * The base WS url
 * @type {string}
 */
export const baseWsEndpoint = `${process.env.BASE_URL}/ws`;

/**
 * Global endpoint for Authentication
 * @type {string}
 */
export const authenticationApiEndpoint = `${baseApiEndpoint}/oauth/token`;

/**
 * Global endpoint for Users
 * @type {string}
 */
export const usersApiEndpoint = `${baseApiEndpoint}/users`;
/**
 * Global endpoint for projects
 * @type {string}
 */
export const projectsApiEndpoint = `${baseApiEndpoint}/projects`;
/**
 * Global endpoint for screens
 * @type {string}
 */
export const screensApiEndpoint = `${baseApiEndpoint}/screens`;
/**
 * Global endpoint for Widgets
 * @type {string}
 */
export const widgetsApiEndpoint = `${baseApiEndpoint}/widgets`;
/**
 * Global configurations enpoint
 * @type {string}
 */
export const configurationsApiEndpoint = `${baseApiEndpoint}/configurations`;
/**
 * Global roles endpoint
 * @type {string}
 */
export const rolesApiEndpoint = `${baseApiEndpoint}/roles`;
