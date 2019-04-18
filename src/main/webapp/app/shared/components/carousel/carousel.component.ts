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

import {Component, ContentChild, Input, TemplateRef} from '@angular/core';

/**
 * This component is used to manage the template of a carousel
 */
@Component({
  selector: 'app-carousel',
  templateUrl: './carousel.component.html',
  styleUrls: ['./carousel.component.scss']
})
export class CarouselComponent {
  /**
   * The list of items that will be processed by the carousel
   */
  @Input() carouselItems: any = [];

  /**
   * Expose a template that can be used by the users to retrieve the current displayed item
   */
  @ContentChild(TemplateRef) carouselTemplate: TemplateRef<any>;

  /**
   * Constructor
   */
  constructor() {
  }
}
