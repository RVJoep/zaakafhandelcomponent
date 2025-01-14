/*
 * SPDX-FileCopyrightText: 2022 Atos
 * SPDX-License-Identifier: EUPL-1.2+
 */

import {AfterViewInit, ChangeDetectorRef, Component, OnInit} from '@angular/core';
import {FormConfig} from '../../shared/material-form-builder/model/form-config';
import {ActivatedRoute, Router} from '@angular/router';
import {PlanItemsService} from '../plan-items.service';
import {FormGroup} from '@angular/forms';
import {PlanItem} from '../model/plan-item';
import {PlanItemType} from '../model/plan-item-type.enum';
import {NavigationService} from '../../shared/navigation/navigation.service';
import {UtilService} from '../../core/service/util.service';
import {AbstractFormField} from '../../shared/material-form-builder/model/abstract-form-field';
import {AbstractFormulier} from '../../formulieren/model/abstract-formulier';
import {TaakFormulierenService} from '../../formulieren/taak-formulieren.service';
import {IdentityService} from '../../identity/identity.service';
import {FormConfigBuilder} from '../../shared/material-form-builder/model/form-config-builder';
import {Observable} from 'rxjs';
import {Medewerker} from '../../identity/model/medewerker';

@Component({
    templateUrl: './plan-item-do.component.html',
    styleUrls: ['./plan-item-do.component.less']
})
export class PlanItemDoComponent implements OnInit, AfterViewInit {

    formItems: Array<AbstractFormField[]>;
    formConfig: FormConfig;
    private planItem: PlanItem;
    private formulier: AbstractFormulier;

    constructor(private route: ActivatedRoute, private planItemsService: PlanItemsService, private taakFormulierenService: TaakFormulierenService,
                private router: Router, private navigation: NavigationService, private utilService: UtilService,
                private identityService: IdentityService, private cd: ChangeDetectorRef) {
    }

    ngOnInit(): void {
        this.planItem = this.route.snapshot.data['planItem'];
        this.formConfig = new FormConfigBuilder().saveText('actie.starten').cancelText('actie.annuleren').build();
        if (this.planItem.type === PlanItemType.HumanTask) {
            this.formulier = this.taakFormulierenService.getFormulierBuilder(this.planItem.formulierDefinitie)
                                 .startForm(this.planItem, this.identityService.listGroepen()).build();
            if (this.formulier.disablePartialSave) {
                this.formConfig.partialButtonText = null;
            }
            this.utilService.setTitle(this.formulier.getStartTitel());
            this.formItems = this.formulier.form;
        } else {
            this.utilService.setTitle(this.planItem.naam);
            this.formItems = [[]];
        }
    }

    ngAfterViewInit() {
        this.loadMedewerkers();
        this.reloadMedewerkersOnChange();
    }

    onFormSubmit(formGroup: FormGroup): void {
        if (formGroup) {
            if (this.planItem.type === PlanItemType.HumanTask) {
                this.planItem = this.formulier.getPlanItem(formGroup);
            }
            this.planItemsService.doPlanItem(this.planItem).subscribe(() => {
                this.navigation.back();
            });
        } else {
            this.navigation.back();
        }
    }

    reloadMedewerkersOnChange() {
        this.formulier.groepFormField.formControl.valueChanges.subscribe(() => {
            this.loadMedewerkers();
        });
        this.formulier.filterFormField.formControl.valueChanges.subscribe(() => {
            this.loadMedewerkers();
        });
    }

    loadMedewerkers(): void {
        let medewerkers: Observable<Medewerker[]>;

        if (this.formulier.groepFormField.formControl.value && this.formulier.filterFormField.formControl.value) {
            medewerkers = this.identityService.listMedewerkersInGroep(
                this.formulier.groepFormField.formControl.value.id);
        } else {
            medewerkers = this.identityService.listMedewerkers();
        }

        this.formulier.medewerkerFormField.options = medewerkers;

        this.cd.detectChanges();
    }
}
