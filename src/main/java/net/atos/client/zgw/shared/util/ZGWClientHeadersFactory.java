/*
 * SPDX-FileCopyrightText: 2021 Atos
 * SPDX-License-Identifier: EUPL-1.2+
 */

package net.atos.client.zgw.shared.util;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;

import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.rest.client.ext.ClientHeadersFactory;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;

import net.atos.zac.authentication.IngelogdeMedewerker;
import net.atos.zac.authentication.Medewerker;

/**
 *
 */
public class ZGWClientHeadersFactory implements ClientHeadersFactory {

    @Inject
    @IngelogdeMedewerker
    private Instance<Medewerker> ingelogdeMedewerker;

    private static final String CLIENT_ID = ConfigProvider.getConfig().getValue("zgw.api.clientId", String.class);

    private static final String SECRET = ConfigProvider.getConfig().getValue("zgw.api.secret", String.class);

    private static final Map<String, String> TOELICHTINGEN = new ConcurrentHashMap<>();

    @Override
    public MultivaluedMap<String, String> update(final MultivaluedMap<String, String> incomingHeaders,
            final MultivaluedMap<String, String> clientOutgoingHeaders) {
        final Medewerker medewerker = ingelogdeMedewerker.get();
        clientOutgoingHeaders.add(HttpHeaders.AUTHORIZATION, generateJWTToken(medewerker));
        addXAuditToelichtingHeader(clientOutgoingHeaders, medewerker);
        return clientOutgoingHeaders;
    }

    public String generateJWTToken() {
        return generateJWTToken(ingelogdeMedewerker.get());
    }

    public void putMedewerkerToelichting(final String toelichting) {
        if (toelichting != null) {
            TOELICHTINGEN.put(ingelogdeMedewerker.get().getGebruikersnaam(), toelichting);
        }
    }

    public void removeMedewerkerToelichting() {
        TOELICHTINGEN.remove(ingelogdeMedewerker.get().getGebruikersnaam());
    }

    private String generateJWTToken(final Medewerker ingelogdeMedewerker) {
        final Map<String, Object> headerClaims = new HashMap<>();
        headerClaims.put("client_identifier", CLIENT_ID);
        final JWTCreator.Builder jwtBuilder = JWT.create();
        jwtBuilder.withIssuer(CLIENT_ID)
                .withIssuedAt(new Date())
                .withHeader(headerClaims)
                .withClaim("client_id", CLIENT_ID);
        if (ingelogdeMedewerker != null) {
            if (ingelogdeMedewerker.getGebruikersnaam() != null) {
                jwtBuilder.withClaim("user_id", ingelogdeMedewerker.getGebruikersnaam());
            }
            if (ingelogdeMedewerker.getNaam() != null) {
                jwtBuilder.withClaim("user_representation", ingelogdeMedewerker.getNaam());
            }
        }
        String jwtToken = jwtBuilder.sign(Algorithm.HMAC256(SECRET));
        return "Bearer " + jwtToken;
    }

    private void addXAuditToelichtingHeader(final MultivaluedMap<String, String> clientOutgoingHeaders, final Medewerker medewerker) {
        if (medewerker != null) {
            final String toelichting = TOELICHTINGEN.get(medewerker.getGebruikersnaam());
            if (toelichting != null) {
                clientOutgoingHeaders.add("X-Audit-Toelichting", toelichting);
            }
        }
    }
}
