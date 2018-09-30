/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.eliasemudule.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.eliasemudule.json.EModuleAuthResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import org.json.JSONException;
//import org.json.JSONObject;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link EliasEMuduleHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Dave - Initial contribution
 */
@NonNullByDefault
public class EliasEMuduleHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(EliasEMuduleHandler.class);

    private static String API_AUTH = "https://emodul.eu/api/v1/authentication";
    private static String API_MODULES = "https://emodul.eu/api/v1/users/%USERID%/modules";
    private static String API_MODULEDATA = "https://emodul.eu/api/v1/users/%USERID%/modules/%MODULEID%";

    private String authToken = "";
    private String userID = "";
    private String moduelID = "";
    private Gson gson = new Gson();
    private String userName = "";
    private String password = "";

    @Nullable
    private EliasEMuduleConfiguration config;

    public EliasEMuduleHandler(Thing thing, String username, String password) {
        super(thing);
        this.userName = username;
        this.password = password;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        switch (channelUID.getId()) {
            case EliasEMuduleBindingConstants.CHANNEL_MODULENAME:
                if (command instanceof RefreshType) {
                    // TODO: handle data refresh
                }
                break;
        }
    }

    @Override
    public void initialize() {
        // logger.debug("Start initializing!");
        config = getConfigAs(EliasEMuduleConfiguration.class);

        try {
            URL authUrl = new URL(API_AUTH);
            HttpsURLConnection conAuth = (HttpsURLConnection) authUrl.openConnection();
            conAuth.setDoInput(true);
            conAuth.setDoOutput(true);
            conAuth.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conAuth.setRequestProperty("Accept", "application/json");
            conAuth.setRequestMethod("POST");

            OutputStreamWriter wr = new OutputStreamWriter(conAuth.getOutputStream());
            wr.write(String.format("{\"username\": \"%s\", \"password\": \"%s\"}", userName, password));
            wr.flush();

            if (conAuth.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conAuth.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                EModuleAuthResponse resp = gson.fromJson(response.toString(), EModuleAuthResponse.class);
                if (resp.authenticated.equals("true")) {
                    authToken = resp.token;
                    userID = resp.user_id;

                    updateState(getThing().getChannel("modulename").getUID(), new StringType(userID));

                    updateStatus(ThingStatus.ONLINE);
                } else {
                    updateStatus(ThingStatus.OFFLINE);
                }
            }
        } catch (MalformedURLException e) {
            logger.warn("Constructed url is not valid: {}", e.getMessage());
            updateStatus(ThingStatus.UNKNOWN);
        } catch (JsonSyntaxException e) {
            logger.warn("Error running aqicn.org (Air Quality) request: {}", e.getMessage());
            updateStatus(ThingStatus.UNKNOWN);
        } catch (IOException | IllegalStateException e) {
            logger.error(e.getMessage());
            updateStatus(ThingStatus.UNKNOWN);
        }

        // Example for background initialization:
        scheduler.execute(() -> {
            boolean thingReachable = true; // <background task with long running initialization here>
            // when done do:
            if (thingReachable) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE);
            }
        });

        // logger.debug("Finished initializing!");

        // Note: When initialization can NOT be done set the status with more details for further
        // analysis. See also class ThingStatusDetail for all available status details.
        // Add a description to give user information to understand why thing does not work as expected. E.g.
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
        // "Can not access device as username and/or password are invalid");
    }
}
