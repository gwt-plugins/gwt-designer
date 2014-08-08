package com.instantiations.eclipse.shared.installer;

import java.io.File;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.instantiations.installer.core.InstallOptions;
import com.instantiations.installer.core.eclipse.EclipseInstallation;
import com.instantiations.installer.internal.core.IProductVersion;

/**
 * Common interface shared by all installers defining all Instantiations sub products.
 * The embedded version numbers appearing as @<productName>Version@ are replace during
 * the installer build process by the installer-macros/compile_installer macro.
 * <p>
 * Copyright (c) 2006, Instantiations, Inc.<br>
 * All Rights Reserved
 * 
 * @author Dan Rubel
 */
public interface SubProducts
{
//	public static final SubProduct RCPDEVELOPER 
//			= new SubProduct("RCPDeveloper",     
//								"@RCPDeveloperVersion@",     
//								"@RCPDeveloperFullVersion@",     
//								"@RCPDeveloperId@");
//
//	public static final SubProduct DESIGNER         
//			= new SubProduct("Designer",
//								"@DesignerVersion@",
//								"@DesignerFullVersion@",
//								"@DesignerId@");
//	public static final SubProduct GWTDESIGNER
//			= new SubProduct("GWTDesigner",
//								"@GWTDesignerVersion@",
//								"@GWTDesignerFullVersion@",
//								"@GWTDesignerId@") {
//		public boolean canLinkTo(EclipseInstallation eclipse, InstallOptions options) {
//			IProductVersion v = eclipse.getEclipseVersion();
//			// GWTDESIGNER works with Eclipse 3.3 or greater
//			return v.getMajor() > 3 || (v.getMajor() == 3 && v.getMinor() >= 3);
//		}
//		public ZipEntry[] getEntries(ZipFile image, IProductVersion eclipseTarget) {
//			ZipEntry[] entries = EMPTY_ZIP_ENTRY;
//			if (eclipseTarget.getMajor() > 3 || 
//					(eclipseTarget.getMajor() == 3 && eclipseTarget.getMinor() >= 3)) {
//				entries = new ZipEntry[] {
//						image.getEntry(BaseProductInstaller.INSTALL_IMAGE + "/" + getName() + "/E-" + eclipseTarget.getMajor() + "." + eclipseTarget.getMinor()),
//						image.getEntry(BaseProductInstaller.INSTALL_IMAGE + "/" + getName() + "/E-common")
//					};
//			}
//			return entries;
//		}
//	};
//	public static final SubProduct CSSEDITOR
//				= new SubProduct("CSSEditor",
//									"@CSSEditorVersion@",
//									"@CSSEditorFullVersion@",
//									"@CSSEditorId@") {
//		public boolean canLinkTo(EclipseInstallation eclipse, InstallOptions options) {
//			IProductVersion v = eclipse.getEclipseVersion();
//			// CSSEDITOR works with Eclipse 3.3 or greater
//			return v.getMajor() > 3 || (v.getMajor() == 3 && v.getMinor() >= 3);
//		}
//		public ZipEntry[] getEntries(ZipFile image, IProductVersion eclipseTarget) {
//			ZipEntry[] entries = EMPTY_ZIP_ENTRY;
//			if (eclipseTarget.getMajor() > 3 || 
//					(eclipseTarget.getMajor() == 3 && eclipseTarget.getMinor() >= 3)) {
//				entries = new ZipEntry[] {
//						image.getEntry(BaseProductInstaller.INSTALL_IMAGE + "/" + getName() + "/E-" + eclipseTarget.getMajor() + "." + eclipseTarget.getMinor()),
//						image.getEntry(BaseProductInstaller.INSTALL_IMAGE + "/" + getName() + "/E-common")
//					};
//			}
//			return entries;
//		}
//	};
//	public static final SubProduct WBPRO
//				= new SubProduct("WBPro",
//									"@WBProVersion@",
//									"@WBProFullVersion@",
//									"@WBProId@");
//	public static final SubProduct WINDOWTESTER
//				= new SubProduct("WindowTester",
//									"@WindowTesterVersion@",
//									"@WindowTesterFullVersion@",
//									"@WindowTesterId@") {
//		public boolean canLinkTo(EclipseInstallation eclipse, InstallOptions options) {
//			IProductVersion v = eclipse.getEclipseVersion();
//			// CSSEDITOR works with Eclipse 3.2 or greater
//			return v.getMajor() >= 3 || (v.getMajor() == 3 && v.getMinor() >= 2);
//		}
//		public ZipEntry[] getEntries(ZipFile image, IProductVersion eclipseTarget) {
//			ZipEntry[] entries = EMPTY_ZIP_ENTRY;
//			if (eclipseTarget.getMajor() > 3 || 
//					(eclipseTarget.getMajor() == 3 && eclipseTarget.getMinor() >= 2)) {
//				entries = new ZipEntry[] {
//						image.getEntry(BaseProductInstaller.INSTALL_IMAGE + "/" + getName() + "/E-" + eclipseTarget.getMajor() + "." + eclipseTarget.getMinor()),
//						image.getEntry(BaseProductInstaller.INSTALL_IMAGE + "/" + getName() + "/E-common")};
//			}
//			return entries;
//		}
//	};
//	public static final SubProduct WINDOWTESTERPRO
//				= new SubProduct("WindowTesterPro",
//									"@WindowTesterProVersion@",
//									"@WindowTesterProFullVersion@",
//									"@WindowTesterProId@");
//	public static final SubProduct WINDOWTESTERRUNNER
//	= new SubProduct("WindowTesterRunner",
//						"@WindowTesterRunnerVersion@",
//						"@WindowTesterRunnerFullVersion@",
//						"@WindowTesterRunnerId@");
//	public static final SubProduct CODECOVERAGE
//				= new SubProduct("CodeCoverage",
//									"@CodeCoverageVersion@",
//									"@CodeCoverageFullVersion@",
//									"@CodeCoverageId@");
//	public static final SubProduct UNITTESTER
//				= new SubProduct("UnitTester",
//						"@UnitTesterVersion@",
//						"@UnitTesterFullVersion@",
//						"@UnitTesterId@");
//	public static final SubProduct SHARED
//				= new SubProduct("Shared",
//									"@SharedVersion@",
//									"@SharedFullVersion@",
//									"@SharedId@") {
//		public boolean canLinkTo(EclipseInstallation eclipse, InstallOptions options) {
//			return isNewerOrEqual(new File(eclipse.getLinksDir(), this.getLinkId() + ".link"));
//		}
//	};
//	
//	public static final SubProduct CODEPROCORE
//				= new SubProduct("CodeProCore",
//									"@CodeProCoreVersion@",
//									"@CodeProCoreFullVersion@",
//									"@CodeProCoreId@");
//	public static final SubProduct CODEPROANT
//				= new SubProduct("CodeProAnt",
//									"@CodeProAntVersion@",
//									"@CodeProAntFullVersion@",
//									"@CodeProAntId@");
//	public static final SubProduct ANALYSIS
//				= new SubProduct("Analysis",
//									"@AnalysisVersion@",
//									"@AnalysisFullVersion@",
//									"@AnalysisId@");
//	public static final SubProduct CODEPROANALYTIX
//				= new SubProduct("CodeProAnalytix",
//									"@CodeProAnalytixVersion@",
//									"@CodeProAnalytixFullVersion@",
//									"@CodeProAnalytixId@");
//	public static final SubProduct CODEPROANALYTIXRE
//				= new SubProduct("CodeProAnalytixRE",
//									"@CodeProAnalytixREVersion@",
//									"@CodeProAnalytixREFullVersion@",
//									"@CodeProAnalytixREId@");
//	public static final SubProduct CODEPROPLUSPAK
//				= new SubProduct("CodeProPlusPak",
//									"@CodeProPlusPakVersion@",
//									"@CodeProPlusPakFullVersion@",
//									"@CodeProPlusPakId@");
//	public static final SubProduct CODEPROPLUSPAKRAD
//				= new SubProduct("CodeProPlusPakRad",
//									"@CodeProPlusPakRadVersion@",
//									"@CodeProPlusPakRadFullVersion@",
//									"@CodeProPlusPakRadId@") {
//		// A collection of WSAD 5.1 plugins necessary to install this sub product
//		private final String[] WSAD51_PLUGINS = new String[] {
//			"com.ibm.etools.spellcheck"
//		};
//		// Can only link ANALYSISRAD to WSAD 5.1 or RAD 6.0
//		public boolean canLinkTo(EclipseInstallation eclipse, InstallOptions options) {
//			boolean containsPlugin = false;
//			boolean rightVersion = false;
//			
//			IProductVersion v = eclipse.getEclipseVersion();
//			// CODEPROPLUSPAKRAD only works with eclipse 2.1
//			rightVersion = (v.getMajor() == 2 && v.getMinor() == 1);
//			if (options.isVerbose()) {
//				System.out.println("CODEPROPLUSPAKRAD: Major " + v.getMajor() + " Minor " + v.getMinor());
//			}
//			if (rightVersion) {
//				containsPlugin =
//					EclipseInstallUtils.containsAllPlugins(eclipse, "../wstools/eclipse", WSAD51_PLUGINS, options);
//			}
//			boolean ret = rightVersion && containsPlugin; 
//			if (options.isVerbose()) {
//				System.out.println("CODEPROPLUSPAKRAD: rightVersion = " + rightVersion + " containsPlugin = " + containsPlugin);
//				System.out.println("CODEPROPLUSPAKRAD: ret = " + ret);
//			}
//			return ret;
//		}
//	};
//
//	public static final SubProduct CODEPROSTUDIO
//				= new SubProduct("CodeProStudio",
//									"@CodeProStudioVersion@",
//									"@CodeProStudioFullVersion@",
//									"@CodeProStudioId@");
//	public static final SubProduct ECLIPSEPROAUDIT
//				= new SubProduct("EclipseProAudit",
//									"@EclipseProAuditVersion@",
//									"@EclipseProAuditFullVersion@",
//									"@EclipseProAuditId@");
//	public static final SubProduct ECLIPSEPROTEST
//				= new SubProduct("EclipseProTest",
//									"@EclipseProTestVersion@",
//									"@EclipseProTestFullVersion@",
//									"@EclipseProTestId@");
//	public static final SubProduct APPANALYSIS
//				= new SubProduct("AppAnalysis",
//									"@AppAnalysisVersion@",
//									"@AppAnalysisFullVersion@",
//									"@AppAnalysisId@") {
//		public boolean canLinkTo(EclipseInstallation eclipse, InstallOptions options) {
//			IProductVersion v = eclipse.getEclipseVersion();
//			// APPANALYSIS works with Eclipse 3.0 or greater
//			return v.getMajor() >= 3;
//		}
//	};
//	public static final SubProduct ANALYSISTPTP
//				= new SubProduct("AnalysisTPTP",
//									"@AnalysisTPTPVersion@",
//									"@AnalysisTPTPFullVersion@",
//									"@AnalysisTPTPId@") {
//		// A collection of RAD7 plugins necessary to install this sub product
//		private final String[] RAD7_PLUGINS = new String[] {
//			"org.eclipse.tptp.platform.analysis.core",
//			//"com.ibm.etools.common.logging"
//		};
//		// Can only link ANALYSISTPTP to eclipsese the have RAD7 loaded
//		public boolean canLinkTo(EclipseInstallation eclipse, InstallOptions options) {
//			boolean containsPlugin = false;
//			boolean rightVersion = false;
//			
//			IProductVersion v = eclipse.getEclipseVersion();
//			// AnalysisTPTP only works with Eclipse 3.2 and greater
//			rightVersion = (v.getMajor() == 3 && v.getMinor() == 2);
//			if (options.isVerbose()) {
//				System.out.println("ANALYSISTPTP: Major " + v.getMajor() + " Minor " + v.getMinor());
//			}
//			if (rightVersion) {
//				
//				// RAD 7 ("IBM Software Development Platform 7.0.0") plugins directory is empty, so check the name instead
//				String description = eclipse.getDescription();
//				containsPlugin =
//					EclipseInstallUtils.containsAllPlugins(eclipse, null, RAD7_PLUGINS, options)
//					|| (description.indexOf("IBM") != -1 && description.indexOf(" 7.") != -1);
//			}
//			boolean ret = rightVersion && containsPlugin; 
//			if (options.isVerbose()) {
//				System.out.println("ANALYSISTPTP: rightVersion = " + rightVersion + " containsPlugin = " + containsPlugin);
//				System.out.println("ANALYSISTPTP: return = " + ret);
//			}
//			return ret;
//		}
//	};
//	
//	public static final SubProduct ANALYSISRAD
//				= new SubProduct("AnalysisRAD",
//									"@AnalysisRADVersion@",
//									"@AnalysisRADFullVersion@",
//									"@AnalysisRADId@") {
//		// A collection of WSAD 5.1 plugins necessary to install this sub product
//		private final String[] WSAD51_PLUGINS = new String[] {
//			"com.ibm.etools.spellcheck"
//		};
//		// A collection of RAD 6.0 plugins necessary to install this sub product
//		private final String[] RAD60_PLUGINS_RAD = new String[] {
////			"com.ibm.etools.lum.utils", 
////			"com.ibm.etools.logging.util",
//			"com.ibm.r2a.engine",
//			"com.ibm.r2a.graph",
//			"com.ibm.r2a.jdt"
//		};
//		private final String[] RAD60_PLUGINS_RWD = new String[] {
//				"com.ibm.etools.spellcheck"
//			};
//		// Can only link ANALYSISRAD to WSAD 5.1 or RAD 6.0
//		public boolean canLinkTo(EclipseInstallation eclipse, InstallOptions options) {
//			boolean containsPlugin = false;
//			boolean rightVersion = false;
//			
//			IProductVersion v = eclipse.getEclipseVersion();
//			// AnalysisRAD only works with Eclipse 3.0 and eclipse 2.1
//			rightVersion = (v.getMajor() == 3 && v.getMinor() == 0) ||
//							(v.getMajor() == 2 && v.getMinor() == 1);
//			if (options.isVerbose()) {
//				System.out.println("ANALYSISRAD: Major " + v.getMajor() + " Minor " + v.getMinor());
//			}
//			if (rightVersion) {
//				containsPlugin =
//					EclipseInstallUtils.containsAllPlugins(eclipse, "../wstools/eclipse", WSAD51_PLUGINS, options)
//				|| (
//						EclipseInstallUtils.containsAllPlugins(eclipse, "../rad/eclipse", RAD60_PLUGINS_RAD, options)
//						&& EclipseInstallUtils.containsAllPlugins(eclipse, "../rwd/eclipse", RAD60_PLUGINS_RWD, options)
//					);
//			}
//			boolean ret = rightVersion && containsPlugin; 
//			if (options.isVerbose()) {
//				System.out.println("ANALYSISRAD: rightVersion = " + rightVersion + " containsPlugin = " + containsPlugin);
//				System.out.println("ANALYSISRAD: return = " + ret);
//			}
//			return ret;
//		}
//	};
//
//	public static final SubProduct HELPCOMPOSER 
//				= new SubProduct("HelpComposer",
//									"@HelpComposerVersion@",
//									"@HelpComposerFullVersion@",
//									"@HelpComposerId@") {
//		public boolean canLinkTo(EclipseInstallation eclipse, InstallOptions options) {
//			IProductVersion v = eclipse.getEclipseVersion();
//			// HELPCOMPOSER works with Eclipse 3.1 or greater
//			return v.getMajor() >= 3 || (v.getMajor() == 3 && v.getMinor() >= 1);
//		}
//	};
//
//	public static final SubProduct RCPPACKAGER
//				= new SubProduct("RCPPackager",
//									"@RCPPackagerVersion@",
//									"@RCPPackagerFullVersion@",
//									"@RCPPackagerId@") {
//		public ZipEntry[] getEntries(ZipFile image, IProductVersion eclipseTarget) {
//			return new ZipEntry[] {
//				image.getEntry(BaseProductInstaller.INSTALL_IMAGE + "/" + getName() + "/E-" + eclipseTarget.getMajor() + "." + eclipseTarget.getMinor()),
//				image.getEntry(BaseProductInstaller.INSTALL_IMAGE + "/" + getName() + "/E-common"),
//
//				// Always install RCPInstaller E-3.1 as part of RCPPackager regardless of the version of Eclipse
//				image.getEntry(BaseProductInstaller.INSTALL_IMAGE + "/RCPInstaller/E-3.1"),
//				image.getEntry(BaseProductInstaller.INSTALL_IMAGE + "/RCPInstaller/E-common")
//			};
//		}
//	};
//
//	public static final SubProduct PROFILER
//				= new SubProduct("Profiler",
//									"@ProfilerVersion@",
//									"@ProfilerFullVersion@",
//									"@ProfilerId@");
//	public static final SubProduct PDE
//				= new SubProduct("PDE",
//									"@PDEVersion@",
//									"@PDEFullVersion@",
//									"@PDEId@") {
//		public boolean canLinkTo(EclipseInstallation eclipse, InstallOptions options) {
//			IProductVersion eclipseVersion = eclipse.getEclipseVersion();
//
//			// If this is Eclipse 3.3 or 3.4 and does not have PDE (e.g. Java distro)
//			// then install and link our own PDE plugins
//			
//			if (eclipseVersion.getMajor() == 3) {
//				if (eclipseVersion.getMinor() == 3 || eclipseVersion.getMinor() == 4) {
//					boolean pdePluginsFound = EclipseInstallUtils.containsPDEPlugins(eclipse, options);
//					if (options.isVerbose())
//						System.out.println("found PDE plugins (" + pdePluginsFound + ")");
//					return !pdePluginsFound;
//				}
//			}
//
//			return false;
//		}
//	};
//	public static final SubProduct GEF
//				= new SubProduct("GEF",
//									"@GEFVersion@",
//									"@GEFFullVersion@",
//									"@GEFId@") {
//		public boolean canLinkTo(EclipseInstallation eclipse, InstallOptions options) {
//			
//			IProductVersion v = eclipse.getEclipseVersion();
//			if (v.getMajor() != 3 || v.getMinor() < 2 ) {
//				return false;
//			}
//			boolean gefPluginsFound = EclipseInstallUtils.containsGEFPlugins(eclipse, options);
//			if (options.isVerbose()) {
//				System.out.println("found GEF plugins (" + gefPluginsFound + ")");
//			}
//			// pde is installed for 3.3 that does not have the PDE loaded
//			return !gefPluginsFound;
//		}
//	};
//	public static final SubProduct GWTHIBERNATE
//				= new SubProduct("GWTHibernate",
//									"@GWTHibernateVersion@",
//									"@GWTHibernateFullVersion@",
//									"@GWTHibernateId@");
//	public static final SubProduct D2CORE
//				= new SubProduct("DesignerCore",
//									"@D2CoreVersion@",
//									"@D2CoreFullVersion@",
//									"@D2CoreId@");
//	public static final SubProduct D2SWT
//				= new SubProduct("DesignerSWT",
//									"@D2SWTVersion@",
//									"@D2SWTFullVersion@",
//									"@D2SWTId@");
//	public static final SubProduct D2eRCP
//				= new SubProduct("eRCPDesigner",
//									"@D2eRCPVersion@",
//									"@D2eRCPFullVersion@",
//									"@D2eRCPId@");
//	public static final SubProduct D2RCP
//				= new SubProduct("SWTDesigner",
//									"@D2RCPVersion@",
//									"@D2RCPFullVersion@",
//									"@D2RCPId@");
//	public static final SubProduct D2SWING
//				= new SubProduct("SwingDesigner",
//									"@D2SwingVersion@",
//									"@D2SwingFullVersion@",
//									"@D2SwingId@");
//	public static final SubProduct D2GWT
//				= new SubProduct("GWTDesigner",
//									"@D2GWTVersion@",
//									"@D2GWTFullVersion@",
//									"@D2GWTId@");
//	public static final SubProduct D2PRO
//				= new SubProduct("DesignerPro",
//									"@D2WBVersion@",
//									"@D2WBFullVersion@",
//									"@D2WBId@");
//	public static final SubProduct D2DOC
//				= new SubProduct("DesignerDoc",
//									"@D2DocVersion@",
//									"@D2DocFullVersion@",
//									"@D2DocId@");
//	public static final SubProduct GWTCORE
//				= new SubProduct("GWTCore",
//									"@GWTCoreVersion@",
//									"@GWTCoreFullVersion@",
//									"@GWTCoreId@");
//
//	// //////////////////////////////////////////////////////////////////////////
//	//
//	// Testing
//	//
//	// //////////////////////////////////////////////////////////////////////
//
//	/**
//	 * Test this class by scanning for Eclipse installations and printing what is found to
//	 * System.out.
//	 * 
//	 * @param args ignored
//	 */
//	public static class Test
//	{
//		public static void main(String[] args) throws Exception {
//			final InstallOptions options = new InstallOptions();
//			new EclipseLocator(options).scan(new IProductLocatorListener() {
//				public void scanning(File location) {
//				}
//				public boolean isCanceled() {
//					return false;
//				}
//				public void found(IProductInstallation installation) {
//					
//					String path = installation.getProductDir().getPath();
//					System.out.print(path);
//					for (int i = path.length(); i < 40; i++)
//						System.out.print(' ');
//					
//					boolean canLink = PDE.canLinkTo((EclipseInstallation) installation, options);
//					System.out.print(" >>> link PDE = " + canLink);
//					if (canLink)
//						System.out.print(' ');
//
//					canLink = GEF.canLinkTo((EclipseInstallation) installation, options);
//					System.out.print("   link GEF = " + canLink);
//					if (canLink)
//						System.out.print(' ');
//					
//					System.out.println();
//				}
//			});
//			System.out.println("--- scan complete ---");
//		}
//	}
}
