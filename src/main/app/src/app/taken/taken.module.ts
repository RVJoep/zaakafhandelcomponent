/*
 * SPDX-FileCopyrightText: 2021 Atos
 * SPDX-License-Identifier: EUPL-1.2+
 */

import {NgModule} from '@angular/core';

import {TakenRoutingModule} from './taken-routing.module';
import {TaakViewComponent} from './taak-view/taak-view.component';
import {SharedModule} from '../shared/shared.module';
import {ZakenModule} from '../zaken/zaken.module';
import {TakenMijnComponent} from './taken-mijn/taken-mijn.component';
import {TakenWerkvoorraadComponent} from './taken-werkvoorraad/taken-werkvoorraad.component';
import {TakenVerdelenDialogComponent} from './taken-verdelen-dialog/taken-verdelen-dialog.component';
import {TakenVrijgevenDialogComponent} from './taken-vrijgeven-dialog/taken-vrijgeven-dialog.component';

@NgModule({
    declarations: [
        TaakViewComponent,
        TakenWerkvoorraadComponent,
        TakenMijnComponent,
        TakenVerdelenDialogComponent,
        TakenVrijgevenDialogComponent
    ],
    imports: [
        SharedModule,
        TakenRoutingModule,
        ZakenModule
    ]
})
export class TakenModule {
}
