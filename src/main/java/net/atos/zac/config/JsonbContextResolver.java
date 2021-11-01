/*
 * SPDX-FileCopyrightText: 2021 Atos
 * SPDX-License-Identifier: EUPL-1.2+
 */

package net.atos.zac.config;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import net.atos.zac.adapter.LocalDateAdapter;
import net.atos.zac.adapter.ZonedDateTimeAdapter;

@Provider
public class JsonbContextResolver implements ContextResolver<Jsonb> {

    @Override
    public Jsonb getContext(final Class<?> type) {
        final JsonbConfig jsonbConfig = new JsonbConfig().withAdapters(new ZonedDateTimeAdapter(), new LocalDateAdapter());
        return JsonbBuilder.newBuilder().withConfig(jsonbConfig).build();
    }

}