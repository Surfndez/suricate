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

import {Component, EventEmitter, Input, Output} from '@angular/core';
import {AbstractControl, FormGroup, ValidatorFn, Validators} from '@angular/forms';

import {DataType} from '../../../model/enums/DataType';
import {FormField} from '../../../model/app/form/FormField';

/**
 * Manage the instantiation of different form inputs
 */
@Component({
  selector: 'app-input',
  templateUrl: './input.component.html',
  styleUrls: ['./input.component.scss']
})
export class InputComponent {

  /**
   * The form created in which we have to create the input
   */
  @Input()
  formGroup: FormGroup;

  /**
   * Object that hold different information used for the instantiation of the input
   */
  @Input()
  field: FormField;

  /**
   * True if the field should be readonly
   */
  @Input()
  isReadOnly: boolean;

  /**
   * Event sent when the value of the input has changed
   */
  @Output()
  valueChangeEvent = new EventEmitter<any>();

  /**
   * The data type enum
   */
  dataType = DataType;

  /**
   * Constructor
   */
  constructor() {
  }
  
  /**
   * Retrieve the form control from the form
   */
  getFormControl(): AbstractControl | null {
    return this.formGroup.controls[this.field.key];
  }

  /**
   * Function called when a field has been changed in the form, emit and event that will be caught by the parent component
   *
   * @param value The new value
   */
  emitValueChange(value): void {
    this.valueChangeEvent.emit({value: value});
  }

  /**
   * Tell if it's a required field
   */
  isRequired(): boolean {
    let isRequired: boolean = false;

    if (!this.isReadOnly && this.field && this.field.validators && this.field.validators) {
      isRequired = Array.isArray(this.field.validators) ?
        (this.field.validators as ValidatorFn[]).includes(Validators.required)
        : this.field.validators === Validators.required;
    }

    return isRequired;
  }
}
