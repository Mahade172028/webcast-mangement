package com.wtv.webcastmanagement.service;

import com.wtv.webcastmanagement.utils.EnvironmentProperties;
import com.wtv.webcastmanagement.utils.StaticValues;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Component
public class GenerateStreamUrl {

    @Autowired
    private EnvironmentProperties environmentProperties;

    public ArrayList<Map> generateStreamZoomUrls(String webcastId ,Map clonePayload , boolean cdnStatus){
        ArrayList<String> languages = new ArrayList<>();
        languages.add((String) clonePayload.get(StaticValues.defaultLanguage));
        languages.addAll((ArrayList<String>) clonePayload.get(StaticValues.additionalLanguages));
        String bitRate = (String)clonePayload.get(StaticValues.bitRate);
        ArrayList<String> bitRateTouple = new ArrayList<>(Arrays.asList(bitRate.split(",")));
        ArrayList<Map> payload = new ArrayList<>();
        for (String language : languages) {
            String hostUrl = (cdnStatus ? environmentProperties.NEXT_PUBLIC_MAIN_WOWZA_CHINA_ZOOM_URL : environmentProperties.NEXT_PUBLIC_MAIN_WOWZA_ZOOM_PULL_URL) + "/"+ environmentProperties.NEXT_PUBLIC_MAIN_WOWZA_APPLICATION_ZOOM+bitRateTouple.get(0)+",";
            String toLowerLanguage = language.toLowerCase();
            String url = hostUrl + webcastId + "_" + toLowerLanguage + "_p_"+bitRateTouple.get(0)+"," + webcastId + "_" + toLowerLanguage + "_p_"+bitRateTouple.get(1);
            payload.add(getPayloadData(toLowerLanguage,url));
            Map<String, Object> urlBackupPayload = new HashMap<>(getPayloadData(toLowerLanguage,url));
            urlBackupPayload.replace("url", hostUrl + webcastId + "_" + toLowerLanguage + "_b_"+bitRateTouple.get(0)+"," + webcastId + "_" + toLowerLanguage + "_b_"+bitRateTouple.get(1));
            payload.add(urlBackupPayload);
        }
        return payload;
    }
    public ArrayList<Map> generateStreamZoomPreviewUrls(String webcastId ,Map clonePayload , boolean cdnStatus){
        ArrayList<String> languages = new ArrayList<>();
        languages.add((String) clonePayload.get(StaticValues.defaultLanguage));
        languages.addAll((ArrayList<String>) clonePayload.get(StaticValues.additionalLanguages));
        ArrayList<Map> payload = new ArrayList<>();
        for (String language : languages) {
            String hostUrl = (cdnStatus ? environmentProperties.NEXT_PUBLIC_MAIN_WOWZA_ONDEMAND_CHINA_URL : environmentProperties.NEXT_PUBLIC_MAIN_WOWZA_ONDEMAND_URL) +"/trailer/vod,easywebcast/30675/30675_wtv2021_a_576p_1200k.mp4";
            String toLowerLanguage = language.toLowerCase();
            payload.add(getPayloadData(toLowerLanguage , hostUrl));
        }
        return payload;
    }

    public ArrayList<Map> generateStreamUrlsUpdate(String webcastId ,Map clonePayload , boolean cdnStatus){
        ArrayList<String> languages = new ArrayList<>();
        languages.add((String) clonePayload.get(StaticValues.defaultLanguage));
        languages.addAll((ArrayList<String>) clonePayload.get(StaticValues.additionalLanguages));
        String bitRate = (String)clonePayload.get(StaticValues.bitRate);
        ArrayList<String> bitRateTouple = new ArrayList<>(Arrays.asList(bitRate.split(",")));
        String hostUrl = (cdnStatus ? environmentProperties.NEXT_PUBLIC_MAIN_WOWZA_CHINA_URL : environmentProperties.NEXT_PUBLIC_MAIN_WOWZA_URL) + "/" + environmentProperties.NEXT_PUBLIC_MAIN_WOWZA_APPLICATION +",";
        String hostBackupUrl = (cdnStatus ? environmentProperties.NEXT_PUBLIC_BACKUP_WOWZA_CHINA_URL : environmentProperties.NEXT_PUBLIC_BACKUP_WOWZA_URL) + "/" + environmentProperties.NEXT_PUBLIC_MAIN_WOWZA_APPLICATION +",";
        ArrayList<Map> payload = new ArrayList<>();
        for (String language : languages) {
            String toLowerLanguage = language.toLowerCase();
            String url = hostUrl + webcastId + "_" + toLowerLanguage + "_p_"+bitRateTouple.get(0)+"," + webcastId + "_" + toLowerLanguage + "_p_"+bitRateTouple.get(1);
            payload.add(getPayloadData(toLowerLanguage , url));

            Map<String, Object> urlBackupPayload = new HashMap<>(getPayloadData(toLowerLanguage, url));
            urlBackupPayload.replace("url", hostBackupUrl + webcastId + "_" + toLowerLanguage + "_b_"+bitRateTouple.get(0)+"," + webcastId + "_" + toLowerLanguage + "_b_"+bitRateTouple.get(1));
            payload.add(urlBackupPayload);
        }
        return payload;
    }

    public ArrayList<Map> generateStreamPreviewUrls(String webcastId ,Map clonePayload , boolean cdnStatus){
        ArrayList<String> languages = new ArrayList<>();
        languages.add((String) clonePayload.get(StaticValues.defaultLanguage));
        languages.addAll((ArrayList<String>) clonePayload.get(StaticValues.additionalLanguages));
        String hostUrl = (cdnStatus ? environmentProperties.NEXT_PUBLIC_MAIN_WOWZA_ONDEMAND_CHINA_URL : environmentProperties.NEXT_PUBLIC_MAIN_WOWZA_ONDEMAND_URL) + "/trailer/vod,easywebcast/30675/30675_wtv2021_a_576p_1200k.mp4";
        ArrayList<Map> payload = new ArrayList<>();
        for (String language : languages) {
            String toLowerLanguage = language.toLowerCase();
            payload.add(getPayloadData(toLowerLanguage , hostUrl));
        }
        return payload;
    }

    private Map<String, Object> getPayloadData(String language , String url){
        Map<String, Object> urlPrimaryPayload = new HashMap<>();
        urlPrimaryPayload.put("format", "mobile");
        urlPrimaryPayload.put("media", "video");
        urlPrimaryPayload.put("quality", 1000);
        urlPrimaryPayload.put("language", language.toLowerCase());
        urlPrimaryPayload.put("locale", "default");
        urlPrimaryPayload.put("encoder", "");
        urlPrimaryPayload.put("protocol", "hls");
        urlPrimaryPayload.put("framesize", "640x360");
        urlPrimaryPayload.put("startTime", "0");
        urlPrimaryPayload.put("endTime", "0");
        urlPrimaryPayload.put("url", url);
        return  urlPrimaryPayload;
    }
}
