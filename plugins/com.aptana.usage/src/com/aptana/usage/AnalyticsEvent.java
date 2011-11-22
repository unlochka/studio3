/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.usage;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.Map;
import java.util.TimeZone;

import org.eclipse.core.runtime.Platform;
import org.mortbay.util.ajax.JSON;

import com.aptana.core.CorePlugin;
import com.aptana.core.logging.IdeLog;
import com.aptana.core.util.EclipseUtil;
import com.aptana.core.util.StringUtil;
import com.aptana.usage.internal.AnalyticsInfo;
import com.aptana.usage.internal.AnalyticsInfoManager;
import com.aptana.usage.internal.DefaultAnalyticsInfo;
import com.eaio.uuid.MACAddress;

public class AnalyticsEvent
{

	private static final String SPEC_VERSION = "2"; //$NON-NLS-1$
	private static final String EMPTY_JSON_PAYLOAD = "{}"; //$NON-NLS-1$

	private static final AnalyticsInfo APP_INFO;
	static
	{
		AnalyticsInfo info = AnalyticsInfoManager.getInstance().getInfo("com.aptana.usage.analytics"); //$NON-NLS-1$
		if (info == null)
		{
			APP_INFO = new DefaultAnalyticsInfo();
		}
		else
		{
			APP_INFO = info;
		}
	}
	private static String sessionId;

	private String dateTime;
	private String eventType;
	private String eventName;
	private String eventString;
	private String JSONPayloadString;

	/**
	 * @param eventType
	 * @param eventName
	 * @param eventPayload
	 */
	public AnalyticsEvent(String eventType, String eventName, Map<String, String> eventPayload)
	{
		this(eventType, eventName);
		// convert the data in event payload to JSON format
		try
		{
			JSONPayloadString = (eventPayload != null) ? JSON.toString(eventPayload) : EMPTY_JSON_PAYLOAD;
			dateTime = Long.toString(System.currentTimeMillis());
			init();
		}
		catch (Exception ex)
		{
			IdeLog.logError(UsagePlugin.getDefault(), "Unable to log analytics information", ex); //$NON-NLS-1$
		}
	}

	public AnalyticsEvent(String eventType, String eventName, String eventPayload, String dateTime)
	{
		this(eventType, eventName);
		try
		{
			JSONPayloadString = eventPayload;
			this.dateTime = dateTime;
			init();
		}
		catch (Exception ex)
		{
			IdeLog.logError(UsagePlugin.getDefault(), "Unable to log analytics information", ex); //$NON-NLS-1$
		}
	}

	private AnalyticsEvent(String eventType, String eventName)
	{
		this.eventName = eventName;
		this.eventType = eventType;
	}

	public String getDateTime()
	{
		return dateTime;
	}

	public String getEventType()
	{
		return eventType;
	}

	public String getEventName()
	{
		return eventName;
	}

	public String getJSONPayloadString()
	{
		return JSONPayloadString;
	}

	public String getEventString()
	{
		return eventString;
	}

	public static String getUserAgent()
	{
		return APP_INFO.getUserAgent();
	}

	public static IAnalyticsUserManager getUserManager()
	{
		return APP_INFO.getUserManager();
	}

	private void init()
	{
		// Setup full event string that will be used in ping
		StringBuilder event = new StringBuilder();
		IAnalyticsUserManager userManager = getUserManager();
		IAnalyticsUser user = (userManager == null) ? null : userManager.getUser();

		addPostEntry(event, "event", eventName); //$NON-NLS-1$
		addPostEntry(event, "type", eventType); //$NON-NLS-1$
		if (user == null && sessionId == null)
		{
			sessionId = MessageFormat.format("{0}:{1}", APP_INFO.getAppGuid(), System.currentTimeMillis()); //$NON-NLS-1$
		}
		addPostEntry(event, "sid", (user == null) ? sessionId : user.getSessionID()); //$NON-NLS-1$
		addPostEntry(event, "guid", APP_INFO.getAppGuid()); //$NON-NLS-1$
		addPostEntry(event, "mid", CorePlugin.getMID()); //$NON-NLS-1$
		addPostEntry(event, "app_id", APP_INFO.getAppId()); //$NON-NLS-1$
		addPostEntry(event, "app_name", APP_INFO.getAppName()); //$NON-NLS-1$
		addPostEntry(event, "app_version", EclipseUtil.getPluginVersion(APP_INFO.getVersionPluginId())); //$NON-NLS-1$
		addPostEntry(event, "mac_addr", MACAddress.getMACAddress()); //$NON-NLS-1$
		addPostEntry(event, "platform", Platform.OS_MACOSX.equals(Platform.getOS()) ? "osx" : Platform.getOS()); //$NON-NLS-1$ //$NON-NLS-2$
		// This field was used for the versions of titanium sdk that developer was build on. This does not apply to
		// studio so we are leaving it as 1.1.0 for now.
		addPostEntry(event, "version", "1.1.0"); //$NON-NLS-1$ //$NON-NLS-2$
		addPostEntry(event, "os", System.getProperty("os.name")); //$NON-NLS-1$ //$NON-NLS-2$
		addPostEntry(event, "ostype", System.getProperty("sun.arch.data.model") + "bit"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		addPostEntry(event, "osver", System.getProperty("os.version")); //$NON-NLS-1$ //$NON-NLS-2$
		addPostEntry(event, "osarch", System.getProperty("os.arch")); //$NON-NLS-1$ //$NON-NLS-2$
		addPostEntry(event, "oscpu", Integer.toString(Runtime.getRuntime().availableProcessors())); //$NON-NLS-1$
		addPostEntry(event, "un", (user == null) ? StringUtil.EMPTY : user.getUsername()); //$NON-NLS-1$
		addPostEntry(event, "ver", SPEC_VERSION); //$NON-NLS-1$

		TimeZone tz = TimeZone.getDefault();
		int results = -(tz.getDSTSavings() + tz.getRawOffset());
		results = (results / 1000) / 60;
		addPostEntry(event, "tz", Integer.toString(results)); //$NON-NLS-1$

		InetAddress ip;
		try
		{
			ip = InetAddress.getLocalHost();
			addPostEntry(event, "ip", ip.getHostAddress()); //$NON-NLS-1$
		}
		catch (UnknownHostException e)
		{
			addPostEntry(event, "ip", StringUtil.EMPTY); //$NON-NLS-1$
		}

		if (!EMPTY_JSON_PAYLOAD.equals(getJSONPayloadString()))
		{
			// We need to strip the quotes surrounding the languageModules entry, also unescape quotes inside the entry
			String formattedJSON = getJSONPayloadString();
			if (formattedJSON.contains("languageModules")) //$NON-NLS-1$
			{
				formattedJSON = formattedJSON.replace("\"{", "{"); //$NON-NLS-1$ //$NON-NLS-2$
				formattedJSON = formattedJSON.replace("}\"", "}"); //$NON-NLS-1$ //$NON-NLS-2$
				formattedJSON = formattedJSON.replace("\\\"", "\""); //$NON-NLS-1$ //$NON-NLS-2$
			}
			addPostEntry(event, "data", formattedJSON); //$NON-NLS-1$
		}

		// Remove the extra & that we insert
		event.deleteCharAt(event.lastIndexOf("&")); //$NON-NLS-1$

		eventString = event.toString();
	}

	private void addPostEntry(StringBuilder event, String key, String value)
	{
		if (value == null)
		{
			value = StringUtil.EMPTY;
		}
		event.append(key);
		event.append("="); //$NON-NLS-1$
		try
		{
			event.append(URLEncoder.encode(value, "UTF-8")); //$NON-NLS-1$
		}
		catch (UnsupportedEncodingException e)
		{
			event.append(value);
		}
		event.append("&"); //$NON-NLS-1$
	}
}
