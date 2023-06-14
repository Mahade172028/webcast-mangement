package com.wtv.webcastmanagement.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EnvironmentProperties {
    @Value("${NEXT_PUBLIC_MAIN_WOWZA_ONDEMAND_URL}")
    public String NEXT_PUBLIC_MAIN_WOWZA_ONDEMAND_URL;
    @Value("${NEXT_PUBLIC_MAIN_WOWZA_CHINA_URL}")
    public String NEXT_PUBLIC_MAIN_WOWZA_CHINA_URL;
    @Value("${NEXT_PUBLIC_MAIN_WOWZA_ONDEMAND_CHINA_KOLLECTIVE_URL}")
    public String NEXT_PUBLIC_MAIN_WOWZA_ONDEMAND_CHINA_KOLLECTIVE_URL;
    @Value("${NEXT_PUBLIC_MAIN_WOWZA_APPLICATION_ZOOM}")
    public String NEXT_PUBLIC_MAIN_WOWZA_APPLICATION_ZOOM;
    @Value("${NEXT_PUBLIC_MAIN_WOWZA_APPLICATION}")
    public String NEXT_PUBLIC_MAIN_WOWZA_APPLICATION;
    @Value("${NEXT_PUBLIC_MAIN_WOWZA_URL}")
    public String NEXT_PUBLIC_MAIN_WOWZA_URL;
    @Value("${NEXT_PUBLIC_BACKUP_WOWZA_CHINA_URL}")
    public String NEXT_PUBLIC_BACKUP_WOWZA_CHINA_URL;
    @Value("${NEXT_PUBLIC_BACKUP_WOWZA_URL}")
    public String NEXT_PUBLIC_BACKUP_WOWZA_URL;
    @Value("${NEXT_PUBLIC_MAIN_WOWZA_ZOOM_PULL_URL}")
    public String NEXT_PUBLIC_MAIN_WOWZA_ZOOM_PULL_URL;
    @Value("${NEXT_PUBLIC_MAIN_WOWZA_KOLLECTIVE_URL}")
    public String NEXT_PUBLIC_MAIN_WOWZA_KOLLECTIVE_URL;
    @Value("${NEXT_PUBLIC_MAIN_WOWZA_CHINA_KOLLECTIVE_URL}")
    public String NEXT_PUBLIC_MAIN_WOWZA_CHINA_KOLLECTIVE_URL;

    @Value("${NEXT_PUBLIC_BACKUP_WOWZA_KOLLECTIVE_URL}")
    public String NEXT_PUBLIC_BACKUP_WOWZA_KOLLECTIVE_URL;

    @Value("${NEXT_PUBLIC_BACKUP_WOWZA_CHINA_KOLLECTIVE_URL}")
    public String NEXT_PUBLIC_BACKUP_WOWZA_CHINA_KOLLECTIVE_URL;
    @Value("${NEXT_PUBLIC_MAIN_WOWZA_CHINA_KOLLECTIVE_ZOOM_URL}")
    public String NEXT_PUBLIC_MAIN_WOWZA_CHINA_KOLLECTIVE_ZOOM_URL;
    @Value("${NEXT_PUBLIC_MAIN_WOWZA_CHINA_ZOOM_URL}")
    public String NEXT_PUBLIC_MAIN_WOWZA_CHINA_ZOOM_URL;
    @Value("${NEXT_PUBLIC_MAIN_WOWZA_KOLLECTIVE_ZOOM_PULL_URL}")
    public String NEXT_PUBLIC_MAIN_WOWZA_KOLLECTIVE_ZOOM_PULL_URL;

    @Value("${NEXT_PUBLIC_MAIN_WOWZA_ONDEMAND_CHINA_URL}")
    public String NEXT_PUBLIC_MAIN_WOWZA_ONDEMAND_CHINA_URL;

    @Value("${HTML_STATIC_BASE_URL}")
    public String HTML_STATIC_BASE_URL;

    @Value("${HTML_STATIC_CHINA__BASE_URL}")
    public String HTML_STATIC_CHINA__BASE_URL;

    @Value("${NEXT_PUBLIC_MAIN_WOWZA_ZOOM_STREAM_URL}")
    public String NEXT_PUBLIC_MAIN_WOWZA_ZOOM_STREAM_URL;

    @Value("${CONNECTSTUDIO_SESSION_SERVICE}")
    public String CONNECTSTUDIO_SESSION_SERVICE;

    @Value("${VIDEO_CONVERSION_SERVICE}")
    public String VIDEO_CONVERSION_SERVICE;

    @Value("${SLIDE_CONVERTER}")
    public String SLIDE_CONVERTER;

    public String wowzaOnDemandUrl(){
        return NEXT_PUBLIC_MAIN_WOWZA_ONDEMAND_URL;
    }
    public  String chinaOnDemandUrl(){
        return  NEXT_PUBLIC_MAIN_WOWZA_ONDEMAND_CHINA_KOLLECTIVE_URL + "/" + NEXT_PUBLIC_MAIN_WOWZA_APPLICATION_ZOOM;
    }
    public  String chinaBackupUrlForPreview(){
        return NEXT_PUBLIC_MAIN_WOWZA_ONDEMAND_CHINA_URL;
    }
    public  String chinaMainUrl(){
        return NEXT_PUBLIC_MAIN_WOWZA_CHINA_URL + "/" + NEXT_PUBLIC_MAIN_WOWZA_APPLICATION;
    }
    public  String mainUrl(){
        return NEXT_PUBLIC_MAIN_WOWZA_URL+ "/" +NEXT_PUBLIC_MAIN_WOWZA_APPLICATION;
    }
    public  String chinaBackupUrl(){
        return NEXT_PUBLIC_BACKUP_WOWZA_CHINA_URL+ "/" +NEXT_PUBLIC_MAIN_WOWZA_APPLICATION;
    }
    public  String backupUrl(){
        return NEXT_PUBLIC_BACKUP_WOWZA_URL + "/" +NEXT_PUBLIC_MAIN_WOWZA_APPLICATION;
    }
    public  String mainZoomUrl(){
        return NEXT_PUBLIC_MAIN_WOWZA_ZOOM_PULL_URL+ "/" +NEXT_PUBLIC_MAIN_WOWZA_APPLICATION_ZOOM;
    }
    public  String chinaMainZoomUrl(){
        return NEXT_PUBLIC_MAIN_WOWZA_CHINA_ZOOM_URL+ "/" +NEXT_PUBLIC_MAIN_WOWZA_APPLICATION_ZOOM;
    }
    public  String standardPreviewAudioUrl(){
        return wowzaOnDemandUrl() + "/streamstudio/vod,trailer/mobile/tr_track01_a22_02.m4a";
    }
    public  String standardPreviewVideoUrl(){
        return "trailer/vod,easywebcast/30675/30675_wtv2021_a_576p_1200k.mp4";
    }
    public  String kChinaZoomEcdnUrl(){
        return NEXT_PUBLIC_MAIN_WOWZA_CHINA_KOLLECTIVE_ZOOM_URL+ "/" +NEXT_PUBLIC_MAIN_WOWZA_APPLICATION_ZOOM;
    }

    public  String kMainZoomWowzaURL(){
        return NEXT_PUBLIC_MAIN_WOWZA_KOLLECTIVE_ZOOM_PULL_URL+ "/" +NEXT_PUBLIC_MAIN_WOWZA_APPLICATION_ZOOM;
    }

    public String kChinaMainUrl(){
        return NEXT_PUBLIC_MAIN_WOWZA_CHINA_KOLLECTIVE_URL+ "/" +NEXT_PUBLIC_MAIN_WOWZA_APPLICATION;
    }
    public String kMainUrl(){
        return NEXT_PUBLIC_MAIN_WOWZA_KOLLECTIVE_URL+ "/" +NEXT_PUBLIC_MAIN_WOWZA_APPLICATION;
    }
}
