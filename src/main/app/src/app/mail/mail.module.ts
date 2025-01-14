/*
 * SPDX-FileCopyrightText: 2022 Atos
 * SPDX-License-Identifier: EUPL-1.2+
 */

import {NgModule} from '@angular/core';

import {SharedModule} from '../shared/shared.module';
import {MailCreateComponent} from './mail-create/mail-create.component';
import {MailRoutingModule} from './mail-routing.module';

@NgModule({
    declarations: [MailCreateComponent],
    imports: [
        SharedModule,
        MailRoutingModule
    ]
})
export class MailModule {
}
