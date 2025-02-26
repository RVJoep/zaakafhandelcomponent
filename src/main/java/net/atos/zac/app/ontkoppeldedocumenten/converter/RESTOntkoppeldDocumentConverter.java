/*
 * SPDX-FileCopyrightText: 2022 Atos
 * SPDX-License-Identifier: EUPL-1.2+
 */

package net.atos.zac.app.ontkoppeldedocumenten.converter;

import java.util.List;
import java.util.stream.Collectors;

import net.atos.zac.app.ontkoppeldedocumenten.model.RESTOntkoppeldDocument;
import net.atos.zac.documenten.model.OntkoppeldDocument;

public class RESTOntkoppeldDocumentConverter {

    public RESTOntkoppeldDocument convert(final OntkoppeldDocument document) {
        final RESTOntkoppeldDocument restDocument = new RESTOntkoppeldDocument();
        restDocument.id = document.getId();
        restDocument.documentUUID = document.getDocumentUUID();
        restDocument.documentID = document.getDocumentID();
        restDocument.creatiedatum = document.getCreatiedatum().toLocalDate();
        restDocument.bestandsnaam = document.getBestandsnaam();
        restDocument.titel = document.getTitel();
        return restDocument;
    }

    public List<RESTOntkoppeldDocument> convert(final List<OntkoppeldDocument> documenten) {
        return documenten.stream().map(this::convert).collect(Collectors.toList());
    }
}
