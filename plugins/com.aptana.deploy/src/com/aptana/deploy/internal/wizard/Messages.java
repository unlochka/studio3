/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.deploy.internal.wizard;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS
{

	private static final String BUNDLE_NAME = "com.aptana.deploy.internal.wizard.messages"; //$NON-NLS-1$

	public static String CapifyProjectPage_Description;
	public static String CapifyProjectPage_GenerateButtonLabel;
	public static String CapifyProjectPage_LinkText;
	public static String CapifyProjectPage_Title;

	public static String DeployWizardPage_AlreadyDeployedToHeroku;
	public static String DeployWizardPage_CapistranoLabel;
	public static String DeployWizardPage_FTPLabel;
	public static String DeployWizardPage_RedHatLabel;
	public static String DeployWizardPage_OtherDeploymentOptionsLabel;
	public static String DeployWizardPage_DeploymentOptionsLabel;
	public static String DeployWizardPage_ProvidersLabel;
	public static String DeployWizardPage_Title;

	public static String InstallCapistranoGemPage_Description;
	public static String InstallCapistranoGemPage_InstallGemLabel;
	public static String InstallCapistranoGemPage_Title;

	static
	{
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages()
	{
	}
}
