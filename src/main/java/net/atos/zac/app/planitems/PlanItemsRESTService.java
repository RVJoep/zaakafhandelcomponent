/*
 * SPDX-FileCopyrightText: 2021 Atos
 * SPDX-License-Identifier: EUPL-1.2+
 */

package net.atos.zac.app.planitems;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.flowable.cmmn.api.runtime.PlanItemInstance;
import org.flowable.idm.api.Group;

import net.atos.zac.app.planitems.converter.RESTPlanItemConverter;
import net.atos.zac.app.planitems.model.RESTPlanItem;
import net.atos.zac.flowable.FlowableService;
import net.atos.zac.zaaksturing.ZaakSturingService;
import net.atos.zac.zaaksturing.model.TaakFormulieren;

/**
 *
 */
@Path("planitems")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PlanItemsRESTService {

    @Inject
    private FlowableService flowableService;

    @Inject
    private ZaakSturingService zaakSturingService;

    @Inject
    private RESTPlanItemConverter planItemConverter;

    @GET
    @Path("zaak/{uuid}")
    public List<RESTPlanItem> listPlanItemsForZaak(@PathParam("uuid") final UUID zaakUUID) {
        final List<PlanItemInstance> planItems = flowableService.listPlanItemsForZaak(zaakUUID);
        return planItemConverter.convertPlanItems(planItems);
    }

    @GET
    @Path("{id}")
    public RESTPlanItem readPlanItem(@PathParam("id") final String planItemId) {
        final PlanItemInstance planItem = flowableService.readPlanItem(planItemId);
        final String zaaktypeIdentificatie = flowableService.readZaaktypeIdentificatieForCase(planItem.getCaseInstanceId());
        final TaakFormulieren taakFormulieren = zaakSturingService.findTaakFormulieren(zaaktypeIdentificatie, planItem.getPlanItemDefinitionId());
        final Group group = flowableService.findGroupForPlanItem(planItemId);
        return planItemConverter.convertPlanItem(planItem, group, taakFormulieren.getStartFormulier());
    }

    @PUT
    @Path("do/{id}")
    public RESTPlanItem doPlanItem(final RESTPlanItem restPlanItem) {
        if (restPlanItem.groep != null) {
            flowableService.startHumanTaskPlanItem(restPlanItem.id, restPlanItem.groep.id, restPlanItem.taakdata);
        } else {
            flowableService.startPlanItem(restPlanItem.id);
        }
        return restPlanItem;
    }
}
