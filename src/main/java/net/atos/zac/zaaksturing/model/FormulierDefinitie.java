/*
 * SPDX-FileCopyrightText: 2021 Atos
 * SPDX-License-Identifier: EUPL-1.2+
 */

package net.atos.zac.zaaksturing.model;

public enum FormulierDefinitie {

    DEFAULT_TAAKFORMULIER("Standaard taakformulier"),
    AANVULLENDE_INFORMATIE("Aanvullende informatie"),
    ADVIES("Advies");

    private final String naam;

    FormulierDefinitie(final String naam) {
        this.naam = naam;
    }

    public String getNaam() {
        return naam;
    }
}