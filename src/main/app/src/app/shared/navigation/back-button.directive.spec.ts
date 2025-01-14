/*
 * SPDX-FileCopyrightText: 2021 Atos
 * SPDX-License-Identifier: EUPL-1.2+
 */

import {BackButtonDirective} from './back-button.directive';
import {NavigationService} from './navigation.service';
import {TestBed} from '@angular/core/testing';
import {SessionStorageService} from '../storage/session-storage.service';
import {RouterTestingModule} from '@angular/router/testing';
import {Router} from '@angular/router';

describe('BackButtonDirective', () => {
    let directive;
    let mockNavigationService;
    let mockSessionStorage;
    let mockRouter;

    beforeEach(() => {
        mockNavigationService = jasmine.createSpyObj('NavigationService', ['back', 'navigate']);

        TestBed.configureTestingModule({
            providers: [
                BackButtonDirective, SessionStorageService,
                {provide: NavigationService, useValue: mockNavigationService}

            ],
            imports: [
                RouterTestingModule.withRoutes([])
            ]
        }).compileComponents();
        directive = TestBed.inject(BackButtonDirective);
        mockSessionStorage = TestBed.inject(SessionStorageService);
        mockRouter = TestBed.inject(Router);
    });

    it('should create an instance', () => {
        expect(directive).toBeTruthy();
    });

    it('should call navigation back', () => {
        directive.onClick();

        expect(mockNavigationService.back).toHaveBeenCalled();
    });

    // TODO: ESUITEDEV-25310
    // navigationHistory word niet aangemaakt bij tests
    xit('should build navigation history, then tear it down', () => {
        let history = mockSessionStorage.getSessionStorage('navigationHistory');

        expect(history.length).toBe(1);

        history.push('/zaken/werkvoorraad');
        expect(history.length).toEqual(2);

        history.push('/zaken/mijn');
        expect(history.length).toEqual(3);

        directive.onClick();
        expect(history.length).toEqual(2);

        directive.onClick();
        expect(history.length).toEqual(1);
    });
});
