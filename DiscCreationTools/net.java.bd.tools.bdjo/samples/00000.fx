// Generated on Tue Aug 21 08:48:43 IST 2007

import net.java.bd.tools.bdjo.*;

BDJO {
    appCacheInfo : {
        entries : [
            {
                language : "*.*",
                name : "00000",
                type : 1,
            }, 
        ],
    },
    applicationManagementTable : {
        applications : [
            {
                applicationDescriptor : {
                    baseDirectory : "00000",
                    binding : TITLE_BOUND_DISC_BOUND:<<net.java.bd.tools.bdjo.Binding>>,
                    classpathExtension : "/00000",
                    iconFlags : 0x0,
                    iconLocator : "",
                    initialClassName : "com.hdcookbook.gunbunny.GunBunnyXlet",
                    priority : 1,
                    profiles : [
                        {
                            majorVersion : 1,
                            microVersion : 0,
                            minorVersion : 0,
                            profile : 1,
                        }, 
                    ],
                    visibility : V_11:<<net.java.bd.tools.bdjo.Visibility>>,
                },
                applicationId : 0x4000,
                controlCode : 0x1,
                organizationId : 0xffff0001,
                type : 0x1,
            }, 
        ],
    },
    fileAccessInfo : ".",
    keyInterestTable : 0xffe00000,
    tableOfAccessiblePlayLists : {
        accessToAllFlag : true,
        autostartFirstPlayListFlag : false,
        playListFileNames : [
            "00001", 
        ],
    },
    terminalInfo : {
        defaultFontFile : "*****",
        initialHaviConfig : HD_1920_1080:<<net.java.bd.tools.bdjo.HaviDeviceConfig>>,
        menuCallMask : false,
        titleSearchMask : false,
    },
    version : V_0200:<<net.java.bd.tools.bdjo.Version>>,
}
