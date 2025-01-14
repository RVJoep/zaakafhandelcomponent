/*
 * SPDX-FileCopyrightText: 2022 Atos
 * SPDX-License-Identifier: EUPL-1.2+
 */

import {AbstractFormFieldBuilder} from '../../model/abstract-form-field-builder';
import {DocumentenLijstFormField} from './documenten-lijst-form-field';
import {Observable} from 'rxjs';
import {EnkelvoudigInformatieobject} from '../../../../informatie-objecten/model/enkelvoudig-informatieobject';

export class DocumentenLijstFieldBuilder extends AbstractFormFieldBuilder {

    protected readonly formField: DocumentenLijstFormField;

    constructor() {
        super();
        this.formField = new DocumentenLijstFormField();
    }

    documenten(documenten$: Observable<EnkelvoudigInformatieobject[]>): this {
        this.formField.documenten$ = documenten$;
        return this;
    }
}
