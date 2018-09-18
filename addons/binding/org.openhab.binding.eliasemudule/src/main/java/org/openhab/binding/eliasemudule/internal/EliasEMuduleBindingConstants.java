/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.eliasemudule.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link EliasEMuduleBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Dave - Initial contribution
 */
@NonNullByDefault
public class EliasEMuduleBindingConstants {

    private static final String BINDING_ID = "eliasemudule";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_SAMPLE = new ThingTypeUID(BINDING_ID, "eliasemudule");

    // List of all Channel ids
    public static final String CHANNEL_MODULES = "modules";
}
