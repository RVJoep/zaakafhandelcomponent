/*
 * SPDX-FileCopyrightText: 2021 Atos
 * SPDX-License-Identifier: EUPL-1.2+
 */

import {MatPaginatorIntl} from '@angular/material/paginator';
import {TranslateService} from '@ngx-translate/core';

export class PaginatorTranslator {

    constructor(private translateService: TranslateService) {}

    getTranslatedPaginator() {
        const paginator = new MatPaginatorIntl();

        this.translateService.get(['']).subscribe(() => {
            paginator.itemsPerPageLabel = this.translateService.instant('paginator.items.per.pagina');
            paginator.nextPageLabel = this.translateService.instant('paginator.volgende');
            paginator.previousPageLabel = this.translateService.instant('paginator.vorige');
            paginator.getRangeLabel = this.translatedRangeLabel;
        });

        return paginator;
    }

    private translatedRangeLabel = (page: number, pageSize: number, length: number) => {
        if (length == 0 || pageSize == 0) { return `0 ${this.translateService.instant('paginator.van')} ${length}`; }
        length = Math.max(length, 0);
        const startIndex = page * pageSize;
        const endIndex = startIndex < length ?
            Math.min(startIndex + pageSize, length) :
            startIndex + pageSize;
        return `${startIndex + 1} - ${endIndex} ${this.translateService.instant('paginator.van')} ${length}`;
    };
}