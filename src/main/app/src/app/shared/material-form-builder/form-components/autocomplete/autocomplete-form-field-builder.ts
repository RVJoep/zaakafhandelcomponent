/*
 * SPDX-FileCopyrightText: 2021 Atos
 * SPDX-License-Identifier: EUPL-1.2+
 */

import {AbstractChoicesFormFieldBuilder} from '../../model/abstract-choices-form-field-builder';
import {AutocompleteFormField} from './autocomplete-form-field';

export class AutocompleteFormFieldBuilder extends AbstractChoicesFormFieldBuilder {

    protected readonly formField: AutocompleteFormField;

    constructor() {
        super();
        this.formField = new AutocompleteFormField();
    }
}
