/*
 * SPDX-FileCopyrightText: 2021 Atos
 * SPDX-License-Identifier: EUPL-1.2+
 */

package net.atos.zac.app.audit.converter.zaken;

import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.apache.commons.lang3.ObjectUtils;

import net.atos.client.zgw.shared.model.ObjectType;
import net.atos.client.zgw.shared.model.audit.zaken.ZaakWijziging;
import net.atos.client.zgw.zrc.model.Zaak;
import net.atos.client.zgw.ztc.ZTCClientService;
import net.atos.zac.app.audit.converter.AbstractAuditWijzigingConverter;
import net.atos.zac.app.audit.model.RESTHistorieRegel;

public class AuditZaakWijzigingConverter extends AbstractAuditWijzigingConverter<ZaakWijziging> {

    @Inject
    private ZTCClientService ztcClientService;

    @Override
    public boolean supports(final ObjectType objectType) {
        return ObjectType.ZAAK == objectType;
    }

    @Override
    protected Stream<RESTHistorieRegel> doConvert(final ZaakWijziging zaakWijziging) {
        final Zaak oud = zaakWijziging.getOud();
        final Zaak nieuw = zaakWijziging.getNieuw();

        if (oud == null || nieuw == null) {
            return Stream.of(new RESTHistorieRegel("zaak", toWaarde(oud), toWaarde(nieuw)));
        }

        final List<RESTHistorieRegel> historieRegels = new LinkedList<>();
        checkAttribuut("zaak.identificatie", oud.getIdentificatie(), nieuw.getIdentificatie(), historieRegels);
        checkZaaktype(oud.getZaaktype(), nieuw.getZaaktype(), historieRegels);
        checkKanaal(oud.getCommunicatiekanaal(), nieuw.getCommunicatiekanaal(), historieRegels);
        checkAttribuut("vertrouwelijkheidaanduiding", oud.getVertrouwelijkheidaanduiding(), nieuw.getVertrouwelijkheidaanduiding(), historieRegels);
        checkAttribuut("registratiedatum", oud.getRegistratiedatum(), nieuw.getRegistratiedatum(), historieRegels);
        checkAttribuut("startdatum", oud.getStartdatum(), nieuw.getStartdatum(), historieRegels);
        checkAttribuut("einddatumGepland", oud.getEinddatumGepland(), nieuw.getEinddatumGepland(), historieRegels);
        checkAttribuut("einddatum", oud.getEinddatum(), nieuw.getEinddatum(), historieRegels);
        checkAttribuut("uiterlijkeEinddatumAfdoening", oud.getUiterlijkeEinddatumAfdoening(), nieuw.getUiterlijkeEinddatumAfdoening(), historieRegels);
        checkAttribuut("omschrijving", oud.getOmschrijving(), nieuw.getOmschrijving(), historieRegels);
        checkAttribuut("toelichting", oud.getToelichting(), nieuw.getToelichting(), historieRegels);

        return historieRegels.stream();
    }

    private void checkZaaktype(final URI oud, final URI nieuw, final List<RESTHistorieRegel> historieRegels) {
        if (ObjectUtils.notEqual(oud, nieuw)) {
            historieRegels.add(new RESTHistorieRegel("zaaktype", zaaktypeToWaarde(oud), zaaktypeToWaarde(nieuw)));
        }
    }

    private void checkKanaal(final URI oud, final URI nieuw, final List<RESTHistorieRegel> historieRegels) {
        if (ObjectUtils.notEqual(oud, nieuw)) {
            historieRegels.add(new RESTHistorieRegel("kanaal", kanaalToWaarde(oud), kanaalToWaarde(nieuw)));
        }
    }

    private String toWaarde(final Zaak zaak) {
        return zaak != null ? zaak.getIdentificatie() : null;
    }

    private String zaaktypeToWaarde(final URI zaaktype) {
        return zaaktype != null ? ztcClientService.readZaaktype(zaaktype).getIdentificatie() : null;
    }

    private String kanaalToWaarde(final URI kanaal) {
        // ToDo: Het kanaal moet worden opgehaald uit de VNG referentielijst
        return kanaal != null ? kanaal.toString() : null;
    }
}
