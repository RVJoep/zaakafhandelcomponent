/*
 * SPDX-FileCopyrightText: 2021 Atos
 * SPDX-License-Identifier: EUPL-1.2+
 */

package net.atos.zac.notificaties;

import static net.atos.zac.notificaties.ResourceEnum.APPLICATIE;
import static net.atos.zac.notificaties.ResourceEnum.BESLUIT;
import static net.atos.zac.notificaties.ResourceEnum.BESLUITTYPE;
import static net.atos.zac.notificaties.ResourceEnum.INFORMATIEOBJECT;
import static net.atos.zac.notificaties.ResourceEnum.INFORMATIEOBJECTTYPE;
import static net.atos.zac.notificaties.ResourceEnum.ZAAK;
import static net.atos.zac.notificaties.ResourceEnum.ZAAKTYPE;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Enumeratie die de kanalen bevat zoals die binnenkomen op de {@link NotificatieReceiver}.
 * <p>
 * http://open-zaak.default/ref/kanalen/
 */
public enum ChannelEnum {
    AUTORISATIES("autorisaties", APPLICATIE),
    BESLUITEN("besluiten", BESLUIT),
    BESLUITTYPEN("besluittypen", BESLUITTYPE),
    INFORMATIEOBJECTEN("documenten", INFORMATIEOBJECT),
    INFORMATIEOBJECTTYPEN("informatieobjecttypen", INFORMATIEOBJECTTYPE),
    ZAKEN("zaken", ZAAK),
    ZAAKTYPEN("zaaktypen", ZAAKTYPE);

    private static final Logger LOG = Logger.getLogger(ChannelEnum.class.getName());

    private final String code;

    private final ResourceEnum resourceType;

    private static final Map<String, ChannelEnum> VALUES = new HashMap<>();

    static {
        for (final ChannelEnum value : values()) {
            VALUES.put(value.code, value);
        }
    }

    ChannelEnum(final String code, final ResourceEnum resourceType) {
        this.code = code;
        this.resourceType = resourceType;
    }

    public ResourceEnum getResourceType() {
        return resourceType;
    }

    public static ChannelEnum value(final String code) {
        final ChannelEnum value = VALUES.get(code);
        if (value == null) {
            LOG.warning(String.format("unknown %s channel", code));
        }
        return value;
    }
}
