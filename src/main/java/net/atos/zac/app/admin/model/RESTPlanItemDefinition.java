/*
 * SPDX-FileCopyrightText: 2021 Atos
 * SPDX-License-Identifier: EUPL-1.2+
 */

package net.atos.zac.app.admin.model;

import net.atos.zac.app.planitems.model.PlanItemType;

public class RESTPlanItemDefinition {
    public String naam;

    public String id;

    public PlanItemType type;

    public RESTPlanItemDefinition() {
    }

    public RESTPlanItemDefinition(final String naam, final String id) {
        this.naam = naam;
        this.id = id;
        this.type = PlanItemType.HUMAN_TASK;
    }
}
