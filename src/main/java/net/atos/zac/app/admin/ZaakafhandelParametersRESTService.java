/*
 * SPDX-FileCopyrightText: 2021 Atos
 * SPDX-License-Identifier: EUPL-1.2+
 */

package net.atos.zac.app.admin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.flowable.cmmn.api.repository.CaseDefinition;

import net.atos.client.zgw.ztc.ZTCClientService;
import net.atos.client.zgw.ztc.model.Zaaktype;
import net.atos.zac.app.admin.model.RESTCaseDefinition;
import net.atos.zac.app.admin.model.RESTFormulierDefinition;
import net.atos.zac.app.admin.model.RESTZaakafhandelParameters;
import net.atos.zac.flowable.FlowableService;
import net.atos.zac.util.ConfigurationService;
import net.atos.zac.util.UriUtil;
import net.atos.zac.zaaksturing.ZaakafhandelParameterBeheerService;
import net.atos.zac.zaaksturing.model.FormulierDefinition;
import net.atos.zac.zaaksturing.model.ZaakafhandelParameters;

@Singleton
@Path("parameters")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ZaakafhandelParametersRESTService {

    @Inject
    private ZTCClientService ztcClientService;

    @Inject
    private ConfigurationService configurationService;

    @Inject
    private FlowableService flowableService;

    @Inject
    private ZaakafhandelParameterBeheerService zaakafhandelParameterBeheerService;

    @Inject
    private RESTZaakafhandelParametersConverter zaakafhandelParametersConverter;

    @Inject
    private RESTCaseDefinitionConverter caseDefinitionConverter;

    /**
     * Retrieve all CASE_DEFINITIONs that can be linked to a ZAAKTYPE
     *
     * @return LIST of CASE_DEFINITIONs
     */
    @GET
    @Path("caseDefinition")
    public List<RESTCaseDefinition> listCaseDefinitions() {
        List<CaseDefinition> caseDefinitions = flowableService.listCaseDefinitions();
        return caseDefinitions.stream().map(c -> new RESTCaseDefinition(c.getName(), c.getKey())).collect(Collectors.toList());
    }

    /**
     * Retrieving a CASE_DEFINITION including it's PLAN_ITEM_DEFINITIONs
     *
     * @param key id of the CASE_DEFINITION
     * @return CASE_DEFINITION including it's PLAN_ITEM_DEFINITIONs
     */
    @GET
    @Path("caseDefinition/{key}")
    public RESTCaseDefinition readCaseDefinition(@PathParam("key") String key) {
        return caseDefinitionConverter.convertToRest(key);
    }

    /**
     * Retrieve all ZAAKAFHANDELPARAMETERS for overview
     *
     * @return LIST of ZAAKAFHANDELPARAMETERS
     */
    @GET
    @Path("parameters")
    public List<RESTZaakafhandelParameters> listZaakafhandelParameters() {
        final List<RESTZaakafhandelParameters> parametersList = new ArrayList<>();
        final List<Zaaktype> zaaktypes = listZaaktypes();
        for (Zaaktype zaaktype : zaaktypes) {
            final UUID zaakTypeUUID = UriUtil.uuidFromURI(zaaktype.getUrl());
            ZaakafhandelParameters zaakafhandelParameters = zaakafhandelParameterBeheerService.readZaakafhandelParameters(zaakTypeUUID);
            if (zaakafhandelParameters == null) {
                zaakafhandelParameters = new ZaakafhandelParameters();
                zaakafhandelParameters.setZaakTypeUUID(zaakTypeUUID);
            }
            parametersList.add(zaakafhandelParametersConverter.convert(zaakafhandelParameters, false));
        }
        return parametersList;
    }


    /**
     * Retrieve the ZAAKAFHANDELPARAMETERS for a ZAAKTYPE
     *
     * @return ZAAKAFHANDELPARAMETERS for a ZAAKTYPE by uuid of the ZAAKTYPE
     */
    @GET
    @Path("parameters/{zaaktypeUuid}")
    public RESTZaakafhandelParameters readZaakafhandelParameters(@PathParam("zaaktypeUuid") final UUID zaakTypeUUID) {
        ZaakafhandelParameters zaakafhandelParameters = zaakafhandelParameterBeheerService.readZaakafhandelParameters(zaakTypeUUID);
        if (zaakafhandelParameters == null) {
            zaakafhandelParameters = new ZaakafhandelParameters();
            zaakafhandelParameters.setZaakTypeUUID(zaakTypeUUID);
        }
        return zaakafhandelParametersConverter.convert(zaakafhandelParameters, true);
    }

    /**
     * Saves the ZAAKAFHANDELPARAMETERS
     *
     * @param restZaakafhandelParameters ZAAKAFHANDELPARAMETERS
     */
    @PUT
    @Path("parameters")
    public RESTZaakafhandelParameters updateZaakafhandelparameters(final RESTZaakafhandelParameters restZaakafhandelParameters) {
        ZaakafhandelParameters parameters = zaakafhandelParametersConverter.convert(restZaakafhandelParameters);
        final ZaakafhandelParameters zaakafhandelParameters;
        if (parameters.getId() == null) {
            zaakafhandelParameters = zaakafhandelParameterBeheerService.createZaakafhandelParameters(parameters);
        } else {
            zaakafhandelParameters = zaakafhandelParameterBeheerService.updateZaakafhandelParameters(parameters);
        }
        return zaakafhandelParametersConverter.convert(zaakafhandelParameters, true);
    }

    /**
     * Retrieve all FORMULIER_VERWIJZINGen that can be linked to a HUMAN_TASK_PLAN_ITEM
     *
     * @return lijst of FORMULIER_VERWIJZINGen
     */
    @GET
    @Path("formulierDefinities")
    public List<RESTFormulierDefinition> listFormulierDefinities() {
        return Arrays.stream(FormulierDefinition.values()).map(RESTFormulierDefinition::new).collect(Collectors.toList());
    }

    private List<Zaaktype> listZaaktypes() {
        return ztcClientService.listZaaktypen(configurationService.readDefaultCatalogusURI());
    }
}