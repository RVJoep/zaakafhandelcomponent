/*
 * SPDX-FileCopyrightText: 2021 - 2022 Atos
 * SPDX-License-Identifier: EUPL-1.2+
 */

import {PlanItem} from '../plan-items/model/plan-item';
import {AbstractFormulier} from './model/abstract-formulier';
import {Taak} from '../taken/model/taak';
import {Groep} from '../identity/model/groep';
import {Observable} from 'rxjs';
import {TaakStatus} from '../taken/model/taak-status.enum';

export class FormulierBuilder {

    protected readonly _formulier: AbstractFormulier;

    constructor(formulier: AbstractFormulier) {
        this._formulier = formulier;
    }

    startForm(planItem: PlanItem, groepen: Observable<Groep[]>): FormulierBuilder {
        this._formulier.planItem = planItem;
        this._formulier.dataElementen = planItem.taakdata;
        this._formulier.initStartForm();
        this._formulier.addAssignment(groepen);
        return this;
    }

    behandelForm(taak: Taak): FormulierBuilder {
        this._formulier.taak = taak;
        this._formulier.dataElementen = taak.taakdata;
        this._formulier.initBehandelForm(TaakStatus.Afgerond === taak.status);
        return this;
    }

    build(): AbstractFormulier {
        return this._formulier;
    }
}
