/*
 * SPDX-FileCopyrightText: 2021 Atos
 * SPDX-License-Identifier: EUPL-1.2+
 */

package net.atos.zac.app.identity.converter;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.flowable.idm.api.User;

import net.atos.zac.app.identity.model.RESTMedewerker;
import net.atos.zac.flowable.FlowableService;

public class RESTMedewerkerConverter {

    @Inject
    private FlowableService flowableService;

    public RESTMedewerker convertGebruikersnaam(final String gebruikersnaam) {
        if (gebruikersnaam != null) {
            final User user = flowableService.readUser(gebruikersnaam);
            return convertUser(user);
        } else {
            return null;
        }
    }

    public List<RESTMedewerker> convertUsers(final List<User> users) {
        return users.stream()
                .map(this::convertUser)
                .collect(Collectors.toList());
    }

    public RESTMedewerker convertUser(final User user) {
        final RESTMedewerker restMedewerker = new RESTMedewerker();
        restMedewerker.gebruikersnaam = user.getId();
        restMedewerker.naam = convertToNaam(user);
        return restMedewerker;
    }

    public RESTMedewerker convertUserId(final String medewerkerId) {
        if (medewerkerId != null) {
            final User user = flowableService.readUser(medewerkerId);
            final RESTMedewerker restMedewerker = convertUser(user);
            return restMedewerker;
        }
        return null;
    }

    public static String convertToNaam(final User user) {
        if (user.getDisplayName() != null) {
            return user.getDisplayName();
        } else if (user.getFirstName() != null && user.getLastName() != null) {
            return String.format("%s %s", user.getFirstName(), user.getLastName());
        } else {
            return user.getId();
        }
    }
}
