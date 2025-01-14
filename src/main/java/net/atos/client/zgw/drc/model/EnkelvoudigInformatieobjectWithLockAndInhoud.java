/*
 * SPDX-FileCopyrightText: 2021 Atos
 * SPDX-License-Identifier: EUPL-1.2+
 */

package net.atos.client.zgw.drc.model;

import static net.atos.client.zgw.shared.util.DateTimeUtil.DATE_TIME_FORMAT_WITH_MILLISECONDS;

import java.net.URI;
import java.time.LocalDate;
import java.time.ZonedDateTime;

import javax.json.bind.annotation.JsonbCreator;
import javax.json.bind.annotation.JsonbDateFormat;
import javax.json.bind.annotation.JsonbProperty;

/**
 * Representation of EnkelvoudigInformatieobject for PUT request and respone and for PATCH request only
 */
public class EnkelvoudigInformatieobjectWithLockAndInhoud extends AbstractEnkelvoudigInformatieobject {

    /**
     * Tijdens het updaten van een document (PATCH, PUT) moet het `lock` veld opgegeven worden.
     */
    private String lock;

    /**
     * Binaire inhoud, in base64 ge-encodeerd.
     */
    private String inhoud;

    /**
     * Constructor for PATCH request
     */
    public EnkelvoudigInformatieobjectWithLockAndInhoud(final String lock) {
        super();
        this.lock = lock;
    }

    /**
     * Constructor with required attributes for PUT request
     */
    public EnkelvoudigInformatieobjectWithLockAndInhoud(final String bronorganisatie, final LocalDate creatiedatum, final String titel, final String auteur,
            final String taal, final URI informatieobjecttype, final String inhoud, final String lock) {
        super(bronorganisatie, creatiedatum, titel, auteur, taal, informatieobjecttype);
        this.inhoud = inhoud;
        this.lock = lock;
    }

    /**
     * Constructor with readOnly attributes for PUT and PATCH response
     */
    @JsonbCreator
    public EnkelvoudigInformatieobjectWithLockAndInhoud(@JsonbProperty("url") final URI url,
            @JsonbProperty("versie") final Integer versie,
            @JsonbProperty("beginRegistratie") @JsonbDateFormat(DATE_TIME_FORMAT_WITH_MILLISECONDS) final ZonedDateTime beginRegistratie,
            @JsonbProperty("bestandsomvang") final Long bestandsomvang,
            @JsonbProperty("locked") final Boolean locked) {
        super(url, versie, beginRegistratie, bestandsomvang, locked);
    }


    public String getLock() {
        return lock;
    }

    public String getInhoud() {
        return inhoud;
    }

    public void setInhoud(final String inhoud) {
        this.inhoud = inhoud;
    }
}
