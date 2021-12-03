/*
 * SPDX-FileCopyrightText: 2021 Atos
 * SPDX-License-Identifier: EUPL-1.2+
 */

import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {IdentityService} from '../../identity/identity.service';
import {Observable} from 'rxjs';
import {Medewerker} from '../../identity/model/medewerker';
import {ZakenService} from '../zaken.service';
import {ZaakOverzicht} from '../model/zaak-overzicht';
import {FormControl} from '@angular/forms';
import {map, startWith} from 'rxjs/operators';

@Component({
    selector: 'werkvoorraad-verdelen-dialog',
    templateUrl: 'zaken-verdelen-dialog.component.html',
    styleUrls: ['./zaken-verdelen-dialog.component.less']
})
export class ZakenVerdelenDialogComponent implements OnInit {

    medewerkerControl = new FormControl();
    medewerkers: Medewerker[];
    filteredMedewerkers: Observable<Medewerker[]>;
    loading: boolean;

    constructor(
        public dialogRef: MatDialogRef<ZakenVerdelenDialogComponent>,
        @Inject(MAT_DIALOG_DATA) public data: ZaakOverzicht[],
        private zakenService: ZakenService,
        private identityService: IdentityService) {
    }

    close(): void {
        this.dialogRef.close();
    }

    ngOnInit(): void {
        this.medewerkerControl.disable();
        this.identityService.listMedewerkers().subscribe(medewerkers => {
            this.medewerkers = medewerkers;
            this.medewerkerControl.enable();
            this.filteredMedewerkers = this.medewerkerControl.valueChanges.pipe(
                startWith(''),
                map(value => (typeof value === 'string' ? value : value.naam)),
                map(name => (name ? this._filterNaam(name) : this.medewerkers.slice()))
            );
        });

    }

    private _filterNaam(naam: string): Medewerker[] {
        const filterValue = naam.toLowerCase();
        return this.medewerkers.filter(medewerker => medewerker.naam.toLowerCase().includes(filterValue));
    }

    verdeel(): void {
        this.dialogRef.disableClose = true;
        this.loading = true;
        this.zakenService.verdelen(this.data, this.getMedewerker()).subscribe(() => {
            this.dialogRef.close(this.medewerkerControl.value);
        });
    }

    getNaam(m: Medewerker): string {
        return m && m.naam ? m.naam : '';
    }

    getMedewerker(): Medewerker {
        if (typeof this.medewerkerControl.value === 'object') {
            return this.medewerkerControl.value;
        } else if (typeof this.medewerkerControl.value === 'string') {
            let naam = this.medewerkerControl.value.toLowerCase();
            return this.medewerkers.find(medewerker => medewerker.naam.toLowerCase() == naam.toLowerCase());
        } else {
            return null;
        }
    }
}
