/*
 * SPDX-FileCopyrightText: 2022 Atos
 * SPDX-License-Identifier: EUPL-1.2+
 */

package net.atos.zac.app.personen;

import static net.atos.zac.app.personen.converter.RESTPersoonConverter.FIELDS_PERSOON_OVERZICHT;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import net.atos.client.brp.BRPClientService;
import net.atos.client.brp.model.IngeschrevenPersoonHal;
import net.atos.zac.app.personen.converter.RESTPersoonConverter;
import net.atos.zac.app.personen.model.RESTPersoonOverzicht;

@Path("personen")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Singleton
public class PersonenRESTService {

    @Inject
    private BRPClientService brpClientService;

    @Inject
    private RESTPersoonConverter persoonConverter;

    @GET
    @Path("persoonoverzicht/{bsn}")
    public RESTPersoonOverzicht readPersoonOverzicht(@PathParam("bsn") final String bsn) {
        final IngeschrevenPersoonHal persoon = brpClientService.findPersoon(bsn, FIELDS_PERSOON_OVERZICHT);
        return persoonConverter.convert(persoon);
    }
}