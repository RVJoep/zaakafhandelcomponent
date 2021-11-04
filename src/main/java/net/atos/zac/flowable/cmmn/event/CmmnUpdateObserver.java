/*
 * SPDX-FileCopyrightText: 2021 Atos
 * SPDX-License-Identifier: EUPL-1.2+
 */

package net.atos.zac.flowable.cmmn.event;

import static net.atos.client.zgw.shared.util.URIUtil.parseUUIDFromResourceURI;

import java.net.URI;
import java.util.UUID;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.enterprise.event.ObservesAsync;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.flowable.idm.api.Group;

import net.atos.client.zgw.zrc.ZRCClient;
import net.atos.client.zgw.zrc.ZRCClientService;
import net.atos.client.zgw.zrc.model.OrganisatorischeEenheid;
import net.atos.client.zgw.zrc.model.RolOrganisatorischeEenheid;
import net.atos.client.zgw.zrc.model.Zaak;
import net.atos.client.zgw.ztc.ZTCClientService;
import net.atos.client.zgw.ztc.model.AardVanRol;
import net.atos.client.zgw.ztc.model.Roltype;
import net.atos.client.zgw.ztc.model.Zaaktype;
import net.atos.zac.event.AbstractUpdateObserver;
import net.atos.zac.service.CmmnService;

/**
 * Deze bean luistert naar CmmnUpdateEvents, en werkt daar vervolgens flowable mee bij.
 */
@Stateless
public class CmmnUpdateObserver extends AbstractUpdateObserver<CmmnUpdateEvent> {

    private static final Logger LOG = Logger.getLogger(CmmnUpdateObserver.class.getName());

    private static final String ORGANISATORISCHE_EENHEID_ROL_TOELICHTING = "Groep verantwoordelijk voor afhandelen zaak";

    @Inject
    private ZTCClientService ztcClientService;

    @Inject
    private ZRCClientService zrcClientService;

    @Inject
    @RestClient
    private ZRCClient zrcClient;

    @EJB
    private CmmnService cmmnService;

    @Override
    public void onFire(final @ObservesAsync CmmnUpdateEvent event) {
        LOG.info("TODO CmmnUpdateEvent verwerken " + event);
        startZaakAfhandeling(zrcClientService.getZaak(event.getObjectId()));
    }

    private void startZaakAfhandeling(final Zaak zaak) {
        final Zaaktype zaaktype = ztcClientService.getZaaktype(zaak.getZaaktype());
        if (zaaktype.getReferentieproces() != null && StringUtils.isNotEmpty(zaaktype.getReferentieproces().getNaam())) {
            final String caseDefinitionKey = zaaktype.getReferentieproces().getNaam();
            LOG.info(() -> String.format("Zaak %s: Starten Case definition '%s'", zaak.getUuid(), caseDefinitionKey));
            final Group group = cmmnService.getZaakBehandelaarGroup(caseDefinitionKey);
            zetZaakBehandelaarOrganisatorischeEenheid(zaak.getUrl(), zaaktype.getUrl(), group);
            cmmnService.startCase(caseDefinitionKey, zaak, zaaktype);
        } else {
            LOG.warning(String.format("Zaaktype '%s': Geen referentie proces gevonden", zaaktype.getIdentificatie()));
        }
    }

    private void zetZaakBehandelaarOrganisatorischeEenheid(final URI zaakURI, final URI zaaktypeURI, final Group group) {
        final UUID zaakUUID = parseUUIDFromResourceURI(zaakURI);
        LOG.info(String.format("Zaak %s: Behandeld door groep '%s'", zaakUUID, group.getId()));
        final OrganisatorischeEenheid organisatorischeEenheid = new OrganisatorischeEenheid();
        organisatorischeEenheid.setIdentificatie(group.getId());
        organisatorischeEenheid.setNaam(group.getName());
        final Roltype roltype = ztcClientService.getRoltype(zaaktypeURI, AardVanRol.BEHANDELAAR);
        final RolOrganisatorischeEenheid rol = new RolOrganisatorischeEenheid(zaakURI, roltype.getUrl(), ORGANISATORISCHE_EENHEID_ROL_TOELICHTING,
                                                                              organisatorischeEenheid);
        zrcClient.rolCreate(rol);
    }
}
