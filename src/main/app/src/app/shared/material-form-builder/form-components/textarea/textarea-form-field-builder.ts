/*
 * SPDX-FileCopyrightText: 2021 Atos
 * SPDX-License-Identifier: EUPL-1.2+
 */

import {AbstractFormFieldBuilder} from '../../model/abstract-form-field-builder';
import {TextareaFormField} from './textarea-form-field';
import {TranslateService} from '@ngx-translate/core';

export class TextareaFormFieldBuilder extends AbstractFormFieldBuilder {

    protected readonly formField: TextareaFormField;

    constructor(translate: TranslateService) {
        super();
        this.formField = new TextareaFormField(translate);
    }

    maxlength(maxlength: number): this {
        this.formField.maxlength = maxlength;
        return this;
    }

}
