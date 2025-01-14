/*
 * SPDX-FileCopyrightText: 2021 - 2022 Atos
 * SPDX-License-Identifier: EUPL-1.2+
 */

import {AfterViewInit, Component, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {Taak} from '../model/taak';
import {MenuItem} from '../../shared/side-nav/menu-item/menu-item';
import {ActivatedRoute} from '@angular/router';
import {TakenService} from '../taken.service';
import {UtilService} from '../../core/service/util.service';
import {MatSidenav, MatSidenavContainer} from '@angular/material/sidenav';
import {HeaderMenuItem} from '../../shared/side-nav/menu-item/header-menu-item';
import {WebsocketService} from '../../core/websocket/websocket.service';
import {Opcode} from '../../core/websocket/model/opcode';
import {ObjectType} from '../../core/websocket/model/object-type';
import {FormGroup} from '@angular/forms';
import {FormConfig} from '../../shared/material-form-builder/model/form-config';
import {AbstractFormulier} from '../../formulieren/model/abstract-formulier';
import {TaakFormulierenService} from '../../formulieren/taak-formulieren.service';
import {IdentityService} from '../../identity/identity.service';
import {WebsocketListener} from '../../core/websocket/model/websocket-listener';
import {AutocompleteFormFieldBuilder} from '../../shared/material-form-builder/form-components/autocomplete/autocomplete-form-field-builder';
import {TextareaFormFieldBuilder} from '../../shared/material-form-builder/form-components/textarea/textarea-form-field-builder';
import {FormConfigBuilder} from '../../shared/material-form-builder/model/form-config-builder';
import {Medewerker} from '../../identity/model/medewerker';
import {ScreenEvent} from '../../core/websocket/model/screen-event';
import {TaakStatus} from '../model/taak-status.enum';
import {TextIcon} from '../../shared/edit/text-icon';
import {Conditionals} from '../../shared/edit/conditional-fn';
import {TranslateService} from '@ngx-translate/core';
import {ViewComponent} from '../../shared/abstract-view/view-component';

@Component({
    templateUrl: './taak-view.component.html',
    styleUrls: ['./taak-view.component.less']
})
export class TaakViewComponent extends ViewComponent implements OnInit, AfterViewInit, OnDestroy {

    @ViewChild('menuSidenav') menuSidenav: MatSidenav;
    @ViewChild('sideNavContainer') sideNavContainer: MatSidenavContainer;

    taak: Taak;
    menu: MenuItem[] = [];

    editFormFields: Map<string, any> = new Map<string, any>();
    streefdatumIcon: TextIcon;

    formulier: AbstractFormulier;
    formConfig: FormConfig;
    posts: number = 0;
    private taakListener: WebsocketListener;
    private ingelogdeMedewerker: Medewerker;

    constructor(private route: ActivatedRoute, private takenService: TakenService, public utilService: UtilService,
                private websocketService: WebsocketService, private taakFormulierenService: TaakFormulierenService, private identityService: IdentityService,
                protected translate: TranslateService) {
        super();
    }

    ngOnInit(): void {
        this.getIngelogdeMedewerker();
        this.subscriptions$.push(this.route.data.subscribe(data => {
            this.init(data['taak']);
        }));
    }

    ngAfterViewInit(): void {
        super.ngAfterViewInit();
        this.taakListener = this.websocketService.addListenerWithSnackbar(Opcode.ANY, ObjectType.TAAK, this.taak.id,
            (event) => this.ophalenTaak(event));
    }

    ngOnDestroy() {
        super.ngOnDestroy();
        this.websocketService.removeListener(this.taakListener);
    }

    initTaakGegevens(taak: Taak): void {
        this.menu = [];
        this.taak = taak;
        this.setEditableFormFields();
        this.setupMenu();
    }

    init(taak: Taak): void {
        this.initTaakGegevens(taak);
        this.createTaakForm(taak);
    }

    createTaakForm(taak: Taak): void {
        if (TaakStatus.Afgerond !== this.taak.status) {
            this.formConfig = new FormConfigBuilder().partialText('actie.opslaan').saveText('actie.opslaan.afronden').build();
        } else {
            this.formConfig = null;
        }

        this.formulier = this.taakFormulierenService.getFormulierBuilder(this.taak.formulierDefinitie).behandelForm(taak).build();
        if (this.formulier.disablePartialSave && this.formConfig) {
            this.formConfig.partialButtonText = null;
        }
        this.utilService.setTitle('title.taak', {taak: this.formulier.getBehandelTitel()});
    }

    setEditableFormFields(): void {
        this.editFormFields.set('behandelaar',
            new AutocompleteFormFieldBuilder().id('behandelaar')
                                              .label('behandelaar')
                                              .value(this.taak.behandelaar).optionLabel('naam')
                                              .options(this.identityService.listMedewerkers())
                                              .build());
        this.editFormFields.set('groep',
            new AutocompleteFormFieldBuilder().id('groep')
                                              .label('groep')
                                              .value(this.taak.groep).optionLabel('naam')
                                              .options(this.identityService.listGroepen())
                                              .build());
        this.editFormFields.set('toelichting',
            new TextareaFormFieldBuilder().id('toelichting')
                                          .label('toelichting')
                                          .value(this.taak.toelichting)
                                          .build());
        this.streefdatumIcon = new TextIcon(Conditionals.isAfterDate(), 'report_problem', 'warningTaakVerlopen_icon', 'msg.datum.overschreden', 'warning');
    }

    private setupMenu(): void {
        this.menu.push(new HeaderMenuItem('taak'));
    }

    onFormPartial(formGroup: FormGroup): void {
        this.websocketService.suspendListener(this.taakListener);
        this.takenService.updateTaakdata(this.formulier.getTaak(formGroup)).subscribe(taak => {
            this.utilService.openSnackbar('msg.taak.opgeslagen');
            this.init(taak);
            this.posts++;
        });
    }

    onFormSubmit(formGroup: FormGroup): void {
        if (formGroup) {
            this.websocketService.suspendListener(this.taakListener);
            this.takenService.complete(this.formulier.getTaak(formGroup)).subscribe(taak => {
                this.utilService.openSnackbar('msg.taak.afgerond');
                this.init(taak);
            });
        }
    }

    editGroep(event: any): void {
        this.taak.groep = event.groep;
        this.takenService.assignGroup(this.taak).subscribe(() => {
            this.utilService.openSnackbar('msg.taak.toegekend', {behandelaar: this.taak.groep.naam});
            this.init(this.taak);
        });
    }

    editBehandelaar(event: any): void {
        if (event.behandelaar) {
            this.taak.behandelaar = event.behandelaar;
            this.websocketService.suspendListener(this.taakListener);
            this.takenService.assign(this.taak).subscribe(() => {
                this.utilService.openSnackbar('msg.taak.toegekend', {behandelaar: this.taak.behandelaar.naam});
                this.init(this.taak);
            });
        } else {
            this.vrijgeven();
        }
    }

    private vrijgeven(): void {
        this.taak.behandelaar = null;
        this.websocketService.suspendListener(this.taakListener);
        this.takenService.assign(this.taak).subscribe(() => {
            this.utilService.openSnackbar('msg.taak.vrijgegeven');
            this.init(this.taak);
        });
    }

    partialEditTaak(value: string, field: string): void {
        this.taak[field] = value[field];
        this.websocketService.suspendListener(this.taakListener);
        this.takenService.partialUpdateTaak(this.taak).subscribe((taak) => {
            this.initTaakGegevens(taak);
        });
    }

    private ophalenTaak(event?: ScreenEvent) {
        if (event) {
            console.log('callback ophalenTaak: ' + event.key);
        }
        this.subscriptions$.push(this.route.data.subscribe(data => {
            this.takenService.readTaak(data['taak'].id).subscribe(taak => {
                this.init(taak);
            });
        }));
    }

    afronden = (): void => {
        this.takenService.complete(this.taak).subscribe(taak => {
            this.utilService.openSnackbar('msg.taak.afgerond');
            this.init(taak);
        });
    }

    private getIngelogdeMedewerker() {
        this.identityService.readIngelogdeMedewerker().subscribe(ingelogdeMedewerker => {
            this.ingelogdeMedewerker = ingelogdeMedewerker;
        });
    }

    showAssignToMe(): boolean {
        return this.ingelogdeMedewerker.gebruikersnaam !== this.taak.behandelaar?.gebruikersnaam;
    }

    assignToMe(): void {
        this.websocketService.suspendListener(this.taakListener);
        this.takenService.assignToLoggedOnUser(this.taak).subscribe(taak => {
            this.taak.behandelaar = taak.behandelaar;
            this.utilService.openSnackbar('msg.taak.toegekend', {behandelaar: taak.behandelaar.naam});
            this.init(this.taak);
        });
    }

    isAfgerond() {
        return this.taak.status === TaakStatus.Afgerond;
    }

    updateMargins(): void {
        setTimeout(() => this.sideNavContainer.updateContentMargins(), 250);
    }
}
