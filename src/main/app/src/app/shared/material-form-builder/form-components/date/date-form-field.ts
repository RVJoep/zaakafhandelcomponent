/*
 * SPDX-FileCopyrightText: 2021 Atos
 * SPDX-License-Identifier: EUPL-1.2+
 */

import {AbstractFormField} from '../../model/abstract-form-field';
import {FieldType} from '../../model/field-type.enum';

export class DateFormField extends AbstractFormField {

    fieldType = FieldType.DATE;

    constructor() {
        super();
    }
}
