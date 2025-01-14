/*
 * SPDX-FileCopyrightText: 2021 Atos
 * SPDX-License-Identifier: EUPL-1.2+
 */

import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {MenuItem} from './menu-item/menu-item';
import {ButtonMenuItem} from './menu-item/button-menu-item';
import {HrefMenuItem} from './menu-item/href-menu-item';
import {LinkMenuTitem} from './menu-item/link-menu-titem';
import {rotate180, sideNavToggle} from '../animations/animations';
import {SideNavUtil} from './side-nav.util';

@Component({
    selector: 'zac-side-nav',
    templateUrl: './side-nav.component.html',
    styleUrls: ['./side-nav.component.less'],
    animations: [
        rotate180, sideNavToggle
    ]
})
export class SideNavComponent implements OnInit {

    @Input() menu: MenuItem[];
    @Output() mode = new EventEmitter<string>();

    menuMode = SideNavUtil.load();
    menuState: string;

    constructor() {
    }

    ngOnInit(): void {
        if (this.menuMode === MenuMode.OPEN) {
            this.menuState = 'open';
        } else {
            this.menuState = 'closed';
        }
    }

    toggleMenu(): void {
        if (this.menuMode === MenuMode.CLOSED) {
            this.menuMode = MenuMode.AUTO;
            this.mode.emit('over');
        } else if (this.menuMode === MenuMode.AUTO) {
            this.menuMode = MenuMode.OPEN;
            this.menuState = MenuState.OPEN;
            this.mode.emit('side');
        } else {
            this.menuMode = MenuMode.CLOSED;
            this.menuState = MenuState.CLOSED;
            this.mode.emit('side');
        }
        SideNavUtil.store(this.menuMode);
    }

    mouseEnter() {
        if (this.menuMode === MenuMode.AUTO) {
            this.menuState = MenuState.OPEN;
        } else {
            this.menuState = this.menuMode;
        }
    }

    mouseLeave() {
        if (this.menuMode === MenuMode.AUTO) {
            this.menuState = MenuState.CLOSED;
        } else {
            this.menuState = this.menuMode;
        }
    }

    onClick(buttonMenuItem: ButtonMenuItem): void {
        buttonMenuItem.fn();
    }

    asButtonMenuItem(menuItem: MenuItem): ButtonMenuItem {
        return menuItem as ButtonMenuItem;
    }

    asHrefMenuItem(menuItem: MenuItem): HrefMenuItem {
        return menuItem as HrefMenuItem;
    }

    asLinkMenuItem(menuItem: MenuItem): LinkMenuTitem {
        return menuItem as LinkMenuTitem;
    }

    get MenuMode() {
        return MenuMode;
    }

    get MenuState() {
        return MenuState;
    }
}

export enum MenuMode {
    AUTO = 'auto',
    OPEN = 'open',
    CLOSED = 'closed'
}

export enum MenuState {
    OPEN = 'open',
    CLOSED = 'closed',
}
