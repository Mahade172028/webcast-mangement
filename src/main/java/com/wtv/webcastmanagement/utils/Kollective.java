package com.wtv.webcastmanagement.utils;

import com.wtv.webcastmanagement.dto.dto.KollectiveResponse;
import com.wtv.webcastmanagement.entity.Kollective.Source;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
@Slf4j
@Component
public class Kollective {
    @Autowired
    private EnvironmentProperties environmentProperties;
    public KollectiveResponse generateKollectiveTokenforZoom(
            String lang, String bitRate,String id,String title,String tenantId,String tokenId,boolean enableChinaCDN){
        String higherBitRate = bitRate.split(",")[0];
        List<Source> KollectiveSource = new ArrayList<>();
        Source source = new Source();
        source.setUrl("https://"+(enableChinaCDN ? environmentProperties.kChinaZoomEcdnUrl() : environmentProperties.kMainZoomWowzaURL())+higherBitRate+"/"+id+"_"+lang+"_p_"+higherBitRate+"playlist.m3u8");
        KollectiveSource.add(source);
        KollectiveTokenGenerator kollectiveTokenGenerator = new KollectiveTokenGenerator();
        try {
            return kollectiveTokenGenerator.getKollectiveContentTokenMultipleSourceWithSecrets(title, title, KollectiveSource, tenantId, tokenId);
        }catch (Exception e){
          log.error(e.getMessage());
        }
        return null;
    }

    public KollectiveResponse generateKollectiveTokenforSelf(
            String lang, String bitRate, String id, String title, String tenantId, String tokenId, boolean enableChinaCDN) {
        String higherBitrate = bitRate.split(",")[0];
        String lowerBitrate = bitRate.split(",")[1];
        String url = null;
        url = "https://"+ environmentProperties.kMainUrl()+"/amlst:list_"+id+"_"+lang+"_"+higherBitrate+"_"+lowerBitrate+"_"+higherBitrate+"_"+lowerBitrate+"_t77t_.smil/playlist.m3u8?&stream_"+higherBitrate+"="+id+"_"+lang+"_p_"+higherBitrate+"&stream_"+lowerBitrate+"="+id+"_"+lang+"_p_"+lowerBitrate+"&stream_"+higherBitrate+"=https%3a%2f%2f"+ environmentProperties.NEXT_PUBLIC_BACKUP_WOWZA_KOLLECTIVE_URL+"%2f"+ environmentProperties.NEXT_PUBLIC_MAIN_WOWZA_APPLICATION+"%2c"+id+"_"+lang+"_b_"+higherBitrate+"&stream_"+lowerBitrate+"=https%3a%2f%2f"+ environmentProperties.NEXT_PUBLIC_BACKUP_WOWZA_KOLLECTIVE_URL+"%2f"+ environmentProperties.NEXT_PUBLIC_MAIN_WOWZA_APPLICATION+"%2c"+id+"_"+lang+"_b_"+lowerBitrate+"&rs=1&rlocal=https%3a%2f%2f"+ environmentProperties.NEXT_PUBLIC_MAIN_WOWZA_KOLLECTIVE_URL+"%2f"+ environmentProperties.NEXT_PUBLIC_MAIN_WOWZA_APPLICATION+"%2f";

        if(enableChinaCDN){
            url = "https://"+ environmentProperties.kChinaMainUrl()+"/amlst:list_"+id+"_"+lang+"_"+higherBitrate+"_"+lowerBitrate+"_"+higherBitrate+"_"+lowerBitrate+"_t77t_.smil/playlist.m3u8?&stream_"+higherBitrate+"="+id+"_"+lang+"_p_"+higherBitrate+"&stream_"+lowerBitrate+"="+id+"_"+lang+"_p_"+lowerBitrate+"&stream_"+higherBitrate+"=https%3a%2f%2f"+ environmentProperties.NEXT_PUBLIC_BACKUP_WOWZA_CHINA_KOLLECTIVE_URL+"%2f"+ environmentProperties.NEXT_PUBLIC_MAIN_WOWZA_APPLICATION+"%2c"+id+"_"+lang+"_b_"+higherBitrate+"&stream_"+lowerBitrate+"=https%3a%2f%2f"+ environmentProperties.NEXT_PUBLIC_BACKUP_WOWZA_CHINA_KOLLECTIVE_URL+"%2f"+ environmentProperties.NEXT_PUBLIC_MAIN_WOWZA_APPLICATION+"%2c"+id+"_"+lang+"_b_"+lowerBitrate+"&rs=1&rlocal=https%3a%2f%2f"+ environmentProperties.NEXT_PUBLIC_MAIN_WOWZA_CHINA_KOLLECTIVE_URL+"%2f"+ environmentProperties.NEXT_PUBLIC_MAIN_WOWZA_APPLICATION+"%2f";
        }

        List<Source> KollectiveSource = new ArrayList<>();
        Source source = new Source();
        source.setUrl(url);
        KollectiveSource.add(source);
        KollectiveTokenGenerator kollectiveTokenGenerator = new KollectiveTokenGenerator();
        try {
            return kollectiveTokenGenerator.getKollectiveContentTokenMultipleSourceWithSecrets(title, title, KollectiveSource, tenantId, tokenId);
        }catch (Exception e){

        }
        return null;
    }


    public KollectiveResponse generateKollectiveTokenforSelfAudioOnly(
            String lang, String bitRate, String id, String title, String tenantId, String tokenId, boolean enableChinaCDN) {
        String bitrate = bitRate.split(",")[0];
        String url = null;

        url = "https://"+ environmentProperties.mainUrl()+"/amlst:list_"+id+"_"+lang+"_"+id+"_"+lang+"_b_"+bitrate+"_"+id+"_"+lang+"_p_"+bitrate+"_t77t_.smil/playlist.m3u8?&stream_"+bitrate+"="+id+"_"+lang+"_b_"+bitrate+"&stream_"+bitrate+"=https%3a%2f%2f"+ environmentProperties.NEXT_PUBLIC_BACKUP_WOWZA_KOLLECTIVE_URL+"%2f"+ environmentProperties.NEXT_PUBLIC_MAIN_WOWZA_APPLICATION+"%2c"+id+"_"+lang+"_p_"+bitrate+"&rs=1&rlocal=https%3a%2f%2f"+ environmentProperties.NEXT_PUBLIC_BACKUP_WOWZA_KOLLECTIVE_URL+"%2f"+ environmentProperties.NEXT_PUBLIC_MAIN_WOWZA_APPLICATION+"%2f";

        if(enableChinaCDN){
            url = "https://"+ environmentProperties.chinaMainUrl()+"/amlst:list_"+id+"_"+lang+"_"+id+"_"+lang+"_b_"+bitrate+"_"+id+"_"+lang+"_p_"+bitrate+"_t77t_.smil/playlist.m3u8?&stream_"+bitrate+"="+id+"_"+lang+"_b_"+bitrate+"&stream_"+bitrate+"=https%3a%2f%2f"+ environmentProperties.NEXT_PUBLIC_MAIN_WOWZA_CHINA_KOLLECTIVE_URL+"%2f"+ environmentProperties.NEXT_PUBLIC_MAIN_WOWZA_APPLICATION+"%2c"+id+"_"+lang+"_p_"+bitrate+"&rs=1&rlocal=https%3a%2f%2f"+ environmentProperties.NEXT_PUBLIC_BACKUP_WOWZA_CHINA_KOLLECTIVE_URL+"%2f"+ environmentProperties.NEXT_PUBLIC_MAIN_WOWZA_APPLICATION+"%2f";
        }

        List<Source> KollectiveSource = new ArrayList<>();
        Source source = new Source();
        source.setUrl(url);
        KollectiveSource.add(source);
        KollectiveTokenGenerator kollectiveTokenGenerator = new KollectiveTokenGenerator();
        try {
            return kollectiveTokenGenerator.getKollectiveContentTokenMultipleSourceWithSecrets(title, title, KollectiveSource, tenantId, tokenId);
        }catch (Exception e){

        }
        return null;
    }

    public KollectiveResponse generateKollectiveTokenforOnDemand(
            String id,String title,String tenantId,String tokenId,String customURL){
        String prefix = "https://";
        customURL = customURL.trim();
        if(customURL.startsWith(prefix)) customURL = customURL.replace(prefix,"");
        if(!customURL.startsWith("cdn-")) customURL = "cdn-" +customURL;

        String url = prefix + customURL+ "/playlist.m3u8";
        url = url.replace(",", "/");
        List<Source> kollectiveSource = new ArrayList<>();
        Source source = new Source();
        source.setUrl(url);
        kollectiveSource.add(source);
        KollectiveTokenGenerator kollectiveTokenGenerator = new KollectiveTokenGenerator();
        try {
            return kollectiveTokenGenerator.getKollectiveContentTokenMultipleSourceWithSecrets(title, title, kollectiveSource, tenantId, tokenId);
        }catch (Exception e){
         log.error("kollective ondemand error " + e.getMessage());
        }
        return null;
    }


}
