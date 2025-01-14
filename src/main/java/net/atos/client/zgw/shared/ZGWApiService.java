/*
 * SPDX-FileCopyrightText: 2021 Atos
 * SPDX-License-Identifier: EUPL-1.2+
 */

package net.atos.client.zgw.shared;

import static net.atos.client.zgw.shared.util.DateTimeUtil.convertToDateTime;

import java.net.URI;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.cache.annotation.CacheRemove;
import javax.cache.annotation.CacheRemoveAll;
import javax.cache.annotation.CacheResult;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import net.atos.client.zgw.drc.DRCClient;
import net.atos.client.zgw.drc.model.EnkelvoudigInformatieobjectWithInhoud;
import net.atos.client.zgw.drc.model.Gebruiksrechten;
import net.atos.client.zgw.shared.cache.Caching;
import net.atos.client.zgw.shared.cache.event.CacheEventType;
import net.atos.client.zgw.zrc.ZRCClient;
import net.atos.client.zgw.zrc.ZRCClientService;
import net.atos.client.zgw.zrc.model.BetrokkeneType;
import net.atos.client.zgw.zrc.model.Resultaat;
import net.atos.client.zgw.zrc.model.Rol;
import net.atos.client.zgw.zrc.model.RolListParameters;
import net.atos.client.zgw.zrc.model.RolMedewerker;
import net.atos.client.zgw.zrc.model.RolNatuurlijkPersoon;
import net.atos.client.zgw.zrc.model.RolOrganisatorischeEenheid;
import net.atos.client.zgw.zrc.model.Status;
import net.atos.client.zgw.zrc.model.Zaak;
import net.atos.client.zgw.zrc.model.ZaakInformatieobject;
import net.atos.client.zgw.ztc.ZTCClientService;
import net.atos.client.zgw.ztc.model.AardVanRol;
import net.atos.client.zgw.ztc.model.Resultaattype;
import net.atos.client.zgw.ztc.model.Roltype;
import net.atos.client.zgw.ztc.model.Statustype;
import net.atos.client.zgw.ztc.model.Zaaktype;
import net.atos.zac.event.EventingService;

/**
 * Careful!
 * <p>
 * Never call methods with caching annotations from within the service (or it will not work).
 * Do not introduce caches with keys other than URI and UUID.
 * Use Optional for caches that need to hold nulls (Infinispan does not cache nulls).
 */
@ApplicationScoped
public class ZGWApiService implements Caching {

    // Page numbering in ZGW Api's starts with 1
    public static final int FIRST_PAGE_NUMBER_ZGW_APIS = 1;

    private static final List<String> CACHES = List.of(
            ZGW_ZAAK_BEHANDELAAR_MANAGED,
            ZGW_ZAAK_GROEP_MANAGED);

    @Inject
    private ZTCClientService ztcClientService;

    @Inject
    private ZRCClientService zrcClientService;

    @Inject
    EventingService eventingService;

    @Inject
    @RestClient
    private ZRCClient zrcClient;

    @Inject
    @RestClient
    private DRCClient drcClient;

    /**
     * Create {@link Zaak} and calculate Doorlooptijden.
     *
     * @param zaak {@link Zaak}
     * @return Created {@link Zaak}
     */
    public Zaak createZaak(final Zaak zaak) {
        calculateDoorlooptijden(zaak);
        return zrcClient.zaakCreate(zaak);
    }

    /**
     * Create {@link Status} for a given {@link Zaak} based on {@link Statustype}.omschrijving and with {@link Status}.toelichting.
     *
     * @param zaak                   {@link Zaak}
     * @param statustypeOmschrijving Omschrijving of the {@link Statustype} of the required {@link Status}.
     * @param statusToelichting      Toelichting for thew {@link Status}.
     * @return Created {@link Status}.
     */
    public Status createStatusForZaak(final Zaak zaak, final String statustypeOmschrijving, final String statusToelichting) {
        final Statustype statustype = ztcClientService.readStatustype(
                ztcClientService.readStatustypen(zaak.getZaaktype()), statustypeOmschrijving, zaak.getZaaktype());
        return createStatusForZaak(zaak.getUrl(), statustype.getUrl(), statusToelichting);
    }

    /**
     * Create {@link Resultaat} for a given {@link Zaak} based on {@link Resultaattype}.omschrijving and with {@link Resultaat}.toelichting.
     *
     * @param zaak                      {@link Zaak}
     * @param resultaattypeOmschrijving Omschrijving of the {@link Resultaattype} of the required {@link Resultaat}.
     * @param resultaatToelichting      Toelichting for thew {@link Resultaat}.
     * @return Created {@link Resultaat}.
     */
    public Resultaat createResultaatForZaak(final Zaak zaak, final String resultaattypeOmschrijving, final String resultaatToelichting) {
        final Resultaattype resultaattype = ztcClientService.readResultaattype(
                ztcClientService.readResultaattypen(zaak.getZaaktype()), resultaattypeOmschrijving, zaak.getZaaktype());
        final Resultaat resultaat = new Resultaat(zaak.getUrl(), resultaattype.getUrl());
        resultaat.setToelichting(resultaatToelichting);
        return zrcClient.resultaatCreate(resultaat);
    }

    /**
     * End {@link Zaak}. Creating a new Eind {@link Status} for the {@link Zaak}.
     *
     * @param zaak                  {@link Zaak}
     * @param eindstatusToelichting Toelichting for thew Eind {@link Status}.
     * @return Created Eind {@link Status}.
     */
    public Status endZaak(final Zaak zaak, final String eindstatusToelichting) {
        final Statustype eindStatustype = ztcClientService.readStatustypeEind(ztcClientService.readStatustypen(zaak.getZaaktype()), zaak.getZaaktype());
        return createStatusForZaak(zaak.getUrl(), eindStatustype.getUrl(), eindstatusToelichting);
    }

    /**
     * End {@link Zaak}. Creating a new Eind {@link Status} for the {@link Zaak}.
     *
     * @param zaakUUID              UUID of the {@link Zaak}
     * @param eindstatusToelichting Toelichting for thew Eind {@link Status}.
     * @return Created Eind {@link Status}.
     */
    public Status endZaak(final UUID zaakUUID, final String eindstatusToelichting) {
        final Zaak zaak = zrcClientService.readZaak(zaakUUID);
        return endZaak(zaak, eindstatusToelichting);
    }

    /**
     * Create {@link EnkelvoudigInformatieobjectWithInhoud} and {@link ZaakInformatieobject} for {@link Zaak}.
     *
     * @param zaak                                   {@link Zaak}.
     * @param informatieobject                       {@link EnkelvoudigInformatieobjectWithInhoud} to be created.
     * @param titel                                  Titel of the new {@link ZaakInformatieobject}.
     * @param beschrijving                           Beschrijving of the new {@link ZaakInformatieobject}.
     * @param omschrijvingVoorwaardenGebruiksrechten Used to create the {@link Gebruiksrechten} for the to be created {@link EnkelvoudigInformatieobjectWithInhoud}
     * @return Created {@link ZaakInformatieobject}.
     */
    public ZaakInformatieobject createZaakInformatieobjectForZaak(final Zaak zaak, final EnkelvoudigInformatieobjectWithInhoud informatieobject,
            final String titel, final String beschrijving, final String omschrijvingVoorwaardenGebruiksrechten) {
        final EnkelvoudigInformatieobjectWithInhoud newInformatieobject = drcClient.enkelvoudigInformatieobjectCreate(informatieobject);
        drcClient.gebruiksrechtenCreate(new Gebruiksrechten(newInformatieobject.getUrl(), convertToDateTime(newInformatieobject.getCreatiedatum()),
                                                            omschrijvingVoorwaardenGebruiksrechten));

        final ZaakInformatieobject zaakInformatieObject = new ZaakInformatieobject();
        zaakInformatieObject.setZaak(zaak.getUrl());
        zaakInformatieObject.setInformatieobject(newInformatieobject.getUrl());
        zaakInformatieObject.setTitel(titel);
        zaakInformatieObject.setBeschrijving(beschrijving);
        return zrcClient.zaakinformatieobjectCreate(zaakInformatieObject);
    }

    /**
     * Find {@link RolOrganisatorischeEenheid} for {@link Zaak} with behandelaar {@link AardVanRol}.
     *
     * @param zaakUrl {@link URI}
     * @return {@link RolOrganisatorischeEenheid} or 'null'.
     */
    @CacheResult(cacheName = ZGW_ZAAK_GROEP_MANAGED)
    public Optional<RolOrganisatorischeEenheid> findGroepForZaak(final URI zaakUrl) {
        return Optional.ofNullable((RolOrganisatorischeEenheid) findRolForZaak(zaakUrl, BetrokkeneType.ORGANISATORISCHE_EENHEID, AardVanRol.BEHANDELAAR));
    }

    /**
     * Find {@link RolMedewerker} for {@link Zaak} with behandelaar {@link AardVanRol}.
     *
     * @param zaakUrl {@link URI}
     * @return {@link RolMedewerker} or 'null'.
     */
    @CacheResult(cacheName = ZGW_ZAAK_BEHANDELAAR_MANAGED)
    public Optional<RolMedewerker> findBehandelaarForZaak(final URI zaakUrl) {
        return Optional.ofNullable((RolMedewerker) findRolForZaak(zaakUrl, BetrokkeneType.MEDEWERKER, AardVanRol.BEHANDELAAR));
    }

    /**
     * Find {@link RolNatuurlijkPersoon} for {@link Zaak} with initiator {@link AardVanRol}.
     *
     * @param zaakUrl {@link URI}
     * @return {@link RolMedewerker} or 'null'.
     */
    public Optional<RolNatuurlijkPersoon> findInitiatorForZaak(final URI zaakUrl) {
        return Optional.ofNullable((RolNatuurlijkPersoon) findRolForZaak(zaakUrl, BetrokkeneType.NATUURLIJK_PERSOON, AardVanRol.INITIATOR));
    }

    private Rol<?> findRolForZaak(final URI zaakUrl, final BetrokkeneType betrokkeneType, final AardVanRol aardVanRol) {
        final URI zaakType = zrcClientService.readZaak(zaakUrl).getZaaktype();
        final Roltype roltype = ztcClientService.readRoltype(zaakType, aardVanRol);
        final RolListParameters rolListParameters = new RolListParameters();
        rolListParameters.setZaak(zaakUrl);
        rolListParameters.setBetrokkeneType(betrokkeneType);
        rolListParameters.setRoltype(roltype.getUrl());
        return zrcClientService.listRollen(rolListParameters)
                .getSingleResult()
                .orElse(null);
    }

    private Status createStatusForZaak(final URI zaakURI, final URI statustypeURI, final String toelichting) {
        final Status status = new Status(zaakURI, statustypeURI, ZonedDateTime.now());
        status.setStatustoelichting(toelichting);
        final Status created = zrcClient.statusCreate(status);
        // This event will also happen via open-notificaties
        eventingService.send(CacheEventType.ZAAKSTATUS.event(created));
        return created;
    }

    private void calculateDoorlooptijden(final Zaak zaak) {
        final Zaaktype zaaktype = ztcClientService.readZaaktype(zaak.getZaaktype());
        final Period streefDatum = zaaktype.getServicenorm();
        final Period fataleDatum = zaaktype.getDoorlooptijd();

        if (streefDatum != null) {
            final LocalDate eindDatumGepland = zaak.getStartdatum().plus(streefDatum);
            zaak.setEinddatumGepland(eindDatumGepland);
        }
        if (fataleDatum != null) {
            final LocalDate uiterlijkeEindDatumAfdoening = zaak.getStartdatum().plus(fataleDatum);
            zaak.setUiterlijkeEinddatumAfdoening(uiterlijkeEindDatumAfdoening);
        }
    }

    @CacheRemoveAll(cacheName = ZGW_ZAAK_GROEP_MANAGED)
    public String clearZaakGroepManagedCache() {
        return cleared(ZGW_ZAAK_GROEP_MANAGED);
    }

    @CacheRemove(cacheName = ZGW_ZAAK_GROEP_MANAGED)
    public void updateZaakgroepCache(final URI key) {
        removed(ZGW_ZAAK_GROEP_MANAGED, key);
    }

    @CacheRemoveAll(cacheName = ZGW_ZAAK_BEHANDELAAR_MANAGED)
    public String clearZaakBehandelaarManagedCache() {
        return cleared(ZGW_ZAAK_BEHANDELAAR_MANAGED);
    }

    @CacheRemove(cacheName = ZGW_ZAAK_BEHANDELAAR_MANAGED)
    public void updateZaakbehandelaarCache(final URI key) {
        removed(ZGW_ZAAK_BEHANDELAAR_MANAGED, key);
    }

    @Override
    public List<String> cacheNames() {
        return CACHES;
    }
}
