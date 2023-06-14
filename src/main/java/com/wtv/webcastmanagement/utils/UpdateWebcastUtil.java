package com.wtv.webcastmanagement.utils;

import com.auth0.jwt.JWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wtv.webcastmanagement.Exceptions.HandleWebcastException;
import com.wtv.webcastmanagement.dto.dto.KollectiveResponse;
import com.wtv.webcastmanagement.entity.Session;
import com.wtv.webcastmanagement.entity.Webcast;
import com.wtv.webcastmanagement.entity.WebcastPayload;
import com.wtv.webcastmanagement.entity.ecdn.Ecdn;
import com.wtv.webcastmanagement.entity.legacy.LegacyBroadcast;
import com.wtv.webcastmanagement.entity.legacy.StreamInfo;
import com.wtv.webcastmanagement.entity.zoom.Meeting;
import com.wtv.webcastmanagement.repository.EcdnRepository;
import com.wtv.webcastmanagement.repository.MeetingRepository;
import com.wtv.webcastmanagement.repository.WebcastRepository;
import com.wtv.webcastmanagement.service.LegacyService;
import com.wtv.webcastmanagement.service.ZoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UpdateWebcastUtil {
    private final EcdnRepository ecdnRepository;
    private final WebcastRepository webcastRepository;
    private final LegacyService legacyService;
    private final AddStream addStream;
    private final Kollective kollective;
    private final MeetingRepository meetingRepository;
    private final ZoomService zoomService;
    private final EnvironmentProperties environmentProperties;

    @Value("${CONNECTSTUDIO_SESSION_SERVICE}")
    private String connectstudioSessionUrl;

    public void updateWebcast(WebcastPayload webcastPayload , MultipartFile file){
        try {
            Webcast findWebcast = webcastRepository.findByWebcastId(webcastPayload.getWebcastId());
            if(findWebcast == null){
                try {
                    Webcast webcastForReg = new Webcast();
                    webcastForReg.setWebcastId(webcastPayload.getWebcastId());
                    webcastForReg.setWebcastType(webcastPayload.getWebcastType());
                    webcastForReg.setBitRate(webcastPayload.getBitRate());
                    webcastForReg.setRegRequired(webcastPayload.getRegRequired());
                    webcastForReg.setType(webcastPayload.getType());
                    webcastForReg.setECDNActive(webcastPayload.isEnableECDNActive());
                    webcastForReg.setP2pVendor(webcastPayload.getP2pVendor());
                    webcastForReg.setInvitedGuest(webcastPayload.getInvitedGuest());
                    webcastForReg.setTimeZone(webcastPayload.getTimeZone());
                    webcastRepository.save(webcastForReg);
                }catch (Exception e){

                }
            }
            else {
                boolean isNoSlideWebcast = List.of(StaticValues.videoAudio, StaticValues.audio).contains(webcastPayload.getWebcastType());
                //SystemTxt starts here
                if(webcastPayload.getChangeWebcastType()){
                    if(isNoSlideWebcast){
                        legacyService.setWebcastTypeInSystemText(
                                webcastPayload.getCo().toString() ,
                                webcastPayload.getCso().toString() ,
                                webcastPayload.getWebcastId().toString() ,
                                StaticValues.videoOnly);
                    }else {
                        legacyService.setWebcastTypeInSystemText(
                                webcastPayload.getCo().toString() ,
                                webcastPayload.getCso().toString() ,
                                webcastPayload.getWebcastId().toString() ,
                                getSystemTextWebcastType(webcastPayload.getWebcastType())
                        );
                    }
                }
                //SystemTxt ends here
                List<String> langs = new ArrayList<>(webcastPayload.getAdditionalLanguage());
                langs.add(webcastPayload.getDefaultLanguage());

                boolean isAudioWebcast = List.of(StaticValues.audioSlide, StaticValues.audio).contains(webcastPayload.getWebcastType());
                boolean isCurrentAudioWebcast = List.of(StaticValues.audioSlide, StaticValues.audio).contains(findWebcast.getWebcastType());

                LegacyBroadcast legacyBroadcast = legacyService.getLegacyEvent(webcastPayload.getWebcastId().toString());

                if(legacyBroadcast != null){
                    List<StreamInfo> liveStream = legacyService.getStreamInfo(webcastPayload.getWebcastId().toString() , StaticValues.live);
                    List<StreamInfo> previewStream = legacyService.getStreamInfo(webcastPayload.getWebcastId().toString() , StaticValues.preview);
                    List<StreamInfo> ondemandStream = legacyService.getStreamInfo(webcastPayload.getWebcastId().toString() , StaticValues.ondemand);

                    List<StreamInfo> liveStream_ = getSteamList(liveStream);
                    List<StreamInfo> previewStream_ = getSteamList(previewStream);
                    List<StreamInfo> ondemandStream_ = getSteamList(ondemandStream);

                    HashMap<String,Object> payload = new HashMap<>();
                    payload.put(StaticValues.name , webcastPayload.getTitle());
                    payload.put(StaticValues.scheduleLiveStartDate, webcastPayload.getScheduleLiveStartDate());
                    payload.put(StaticValues.ondemandOverDate , webcastPayload.getOndemandOverDate());
                    payload.put(StaticValues.status,webcastPayload.getStatus());
                    payload.put(StaticValues.defaultLanguage , webcastPayload.getDefaultLanguage());
                    payload.put(StaticValues.additionalLanguages , webcastPayload.getAdditionalLanguage());
                    payload.put(StaticValues.features , webcastPayload.getRegRequired() == "yes" ? List.of("f1", "f20", "f40", "f50") : List.of("f20", "f40", "f50"));
                    legacyService.updateOnlyEvent(webcastPayload.getWebcastId().toString() , payload);
                    legacyService.putRequestWithoutJSONResponseEvent(
                            webcastPayload.getWebcastId().toString(),
                            webcastPayload.isEnableChinaCDN() ? environmentProperties.HTML_STATIC_CHINA__BASE_URL : environmentProperties.HTML_STATIC_BASE_URL);
                    //UPDATE CONNECTSTUDIO DATA
                    updateConnectStudioSession(webcastPayload.getWebcastId().toString() , webcastPayload , file);
                    //END HERE
                    Webcast webcastToBeSave = findWebcast;
                    webcastToBeSave.setTimeZone(webcastPayload.getTimeZone());
                    webcastToBeSave.setWebcastType(webcastPayload.getWebcastType());
                    webcastToBeSave.setInvitedGuest(webcastPayload.getInvitedGuest());
                    webcastToBeSave.setP2pVendor(webcastPayload.getP2pVendor());
                    webcastToBeSave.setBitRate(webcastPayload.getBitRate());
                    webcastToBeSave.setDuration(webcastPayload.getDuration());
                    webcastToBeSave.setECDNActive(webcastPayload.isEnableECDNActive());
                    webcastToBeSave.setEnableChinaCDN(webcastPayload.isEnableChinaCDN());
                    webcastToBeSave.setRegRequired(webcastPayload.getRegRequired());
                    webcastRepository.save(webcastToBeSave);

                    Ecdn ecdnEntry = getEcdn(legacyBroadcast);

                    if(webcastPayload.isEnableECDNActive() && Objects.equals(webcastPayload.getP2pVendor(),"peer5")){
                        Ecdn clientECDN = ecdnRepository.findByEntityId(webcastPayload.getCso());
                        Ecdn orgECDN = ecdnRepository.findByEntityId(webcastPayload.getCo());
                        if(clientECDN != null && clientECDN.getVendor().size() > 0){
                            legacyService.putRequestWithoutJSONResponse(clientECDN.getEntityId().toString());
                        }
                        if(orgECDN != null && orgECDN.getVendor().size() > 0){
                            legacyService.putRequestWithoutJSONResponse(orgECDN.getEntityId().toString());
                        }
                    }

                    Function<List<String> , List<List<StreamInfo>>> generateStreamsBasedOnLanguages = (languages)->{
                        List<StreamInfo> generatedStreamData_preview = new ArrayList<>();
                        List<StreamInfo> generatedStreamData_live = new ArrayList<>();
                        List<StreamInfo> generatedStreamData_onDemand = new ArrayList<>();

                        for(String l : languages){
                            String url = null;
                            if(isAudioWebcast == isCurrentAudioWebcast && webcastPayload.getChangeWebcastType()==false){
                                for (String pu : previewStream.stream().filter(x->x.getUrl()!=null).map(y->y.getUrl()).collect(Collectors.toList())){
                                    String spliteUrl[] = pu.split(",");
                                    try {
                                        JWT.decode(spliteUrl[1]);
                                    }catch (Exception e){
                                        url = pu;
                                        break;
                                    }
                                }
                            }

                            StreamInfo preview = isAudioWebcast ?
                                    addStream.audioPreviewStreams(webcastPayload.isEnableChinaCDN(), l, url) :
                                    addStream.videoPreviewStreams(webcastPayload.getBitRate(), webcastPayload.isEnableChinaCDN(), l, url);
                            generatedStreamData_preview.add(preview);

                            StreamInfo onDemand = isAudioWebcast ?
                                    addStream.audioOnDemandStreams(webcastPayload.isEnableChinaCDN(), l) :
                                    addStream.videoOnDemandStreams(webcastPayload.getBitRate(), webcastPayload.isEnableChinaCDN(), l);
                            generatedStreamData_onDemand.add(onDemand);
                        }

                        if (webcastPayload.isEnableECDNActive() && Objects.equals(webcastPayload.getP2pVendor(),StaticValues.kollective)){
                            List<StreamInfo> __genPreview = new ArrayList<>();
                            for (StreamInfo gp : generatedStreamData_preview) {
                                KollectiveResponse kollectiveStream = kollective.generateKollectiveTokenforOnDemand(
                                        legacyBroadcast.getId().toString(),
                                        legacyBroadcast.getName(),
                                        ecdnEntry.getVendor().get(0).getSecrets().get(0).getValue(),
                                        ecdnEntry.getVendor().get(0).getSecrets().get(1).getValue(),
                                        gp.getUrl()
                                );
                                gp.setUrl("_kollective_,"+kollectiveStream.getToken());
                                __genPreview.add(gp);
                            }
                            generatedStreamData_preview = __genPreview;

                            List<StreamInfo> __genOndemand = new ArrayList<>();
                            for (StreamInfo gp : generatedStreamData_onDemand) {
                                KollectiveResponse kollectiveStream = kollective.generateKollectiveTokenforOnDemand(
                                        legacyBroadcast.getId().toString(),
                                        legacyBroadcast.getName(),
                                        ecdnEntry.getVendor().get(0).getSecrets().get(0).getValue(),
                                        ecdnEntry.getVendor().get(0).getSecrets().get(1).getValue(),
                                        gp.getUrl()
                                );
                                gp.setUrl("_kollective_,"+kollectiveStream.getToken());
                                __genOndemand.add(gp);
                            }
                            generatedStreamData_onDemand = __genOndemand;
                        }

                        List<StreamInfo> liveStreamData = new ArrayList<>();
                        if(findWebcast.getType().equals("zoom")){
                            if(webcastPayload.isEnableECDNActive() && Objects.equals(webcastPayload.getP2pVendor(),StaticValues.kollective)){
                                liveStreamData = isAudioWebcast ? addStream.generateAudioZoomStreamWithKollectiveECDN(
                                            languages,
                                            webcastPayload.getMeeting(),
                                            webcastPayload.getBitRate(),
                                            webcastPayload.getWebcastId().toString(),
                                            webcastPayload.getTitle(),
                                            ecdnEntry,
                                            webcastPayload.isEnableChinaCDN()):
                                        addStream.generateZoomStreamWithKollectiveECDN(
                                            languages,
                                            webcastPayload.getMeeting(),
                                            webcastPayload.getBitRate(),
                                            webcastPayload.getWebcastId().toString(),
                                            webcastPayload.getTitle(),
                                            ecdnEntry,
                                            webcastPayload.isEnableChinaCDN());
                            }
                            if(!webcastPayload.isEnableECDNActive()){
                                Meeting meeting = meetingRepository.findMeetingByWebcastId(webcastPayload.getWebcastId().toString());
                                if(meeting != null && meeting.getId() != null){
                                    String url = "/meetings/"+meeting.getId()+"/livestream";
                                    HashMap<String , String> body = new HashMap<>();
                                    body.put("stream_url" , "rtmp://"+ environmentProperties.NEXT_PUBLIC_MAIN_WOWZA_ZOOM_STREAM_URL+"/"+ environmentProperties.NEXT_PUBLIC_MAIN_WOWZA_APPLICATION_ZOOM + Integer.valueOf(webcastPayload.getBitRate().split(",")[0]));
                                    body.put("stream_key" , webcastPayload.getWebcastId() +"_en_p_9999");
                                    body.put("page_url","https://www.wtvglobal.com");
                                    zoomService.patchRequestZoom(url , body);
                                }
                                liveStreamData = isAudioWebcast ?
                                        addStream.generateAudioZoomStream(
                                                webcastPayload.getBitRate(),
                                                languages,
                                                webcastPayload.getWebcastId().toString(),
                                                webcastPayload.isEnableChinaCDN(),
                                                new ArrayList<>()
                                        ) :
                                        addStream.generateZoomStream(
                                                webcastPayload.getBitRate(),
                                                languages,
                                                webcastPayload.getWebcastId().toString(),
                                                webcastPayload.isEnableChinaCDN(),
                                                new ArrayList<>());
                            }
                        }else {
                            if (webcastPayload.isEnableECDNActive() && Objects.equals(webcastPayload.getP2pVendor(),StaticValues.kollective)) {
                                liveStreamData = isAudioWebcast ?
                                        addStream.generateAudioStreamWithKollectiveECDN(
                                                languages,
                                                webcastPayload.getMeeting(),
                                                webcastPayload.getBitRate(),
                                                legacyBroadcast.getId().toString(),
                                                legacyBroadcast.getName(),
                                                ecdnEntry,
                                                webcastPayload.isEnableChinaCDN()
                                        ) :
                                        addStream.generateStreamWithKollectiveECDN(
                                                languages,
                                                webcastPayload.getMeeting(),
                                                webcastPayload.getBitRate(),
                                                legacyBroadcast.getId().toString(),
                                                legacyBroadcast.getName(),
                                                ecdnEntry,
                                                webcastPayload.isEnableChinaCDN());
                            }
                            if (!webcastPayload.isEnableECDNActive()) {
                                liveStreamData = isAudioWebcast ? addStream.audioLiveStreams(
                                        languages,
                                        legacyBroadcast.getId().toString(),
                                        webcastPayload.isEnableChinaCDN(),
                                        new ArrayList<>()
                                ) : addStream.generateStream(
                                        webcastPayload.getBitRate(),
                                        languages,
                                        legacyBroadcast.getId().toString(),
                                        webcastPayload.isEnableChinaCDN(),
                                        new ArrayList<>()
                                );
                            }
                        }
                        if (!liveStreamData.isEmpty()) {
                            generatedStreamData_live = liveStreamData;
                        }
                        List<List<StreamInfo>> generatedStreamDataAll = new ArrayList<>();
                        generatedStreamDataAll.add(generatedStreamData_preview);
                        generatedStreamDataAll.add(generatedStreamData_live);
                        generatedStreamDataAll.add(generatedStreamData_onDemand);
                        return generatedStreamDataAll;
                    };

                    Function<String,Boolean> checkShouldResetAllLink = (t)->{
                        return (webcastPayload.getWebcastType() == null ||
                                //RULE::  if manually want to change the webcastType
                                webcastPayload.getChangeWebcastType() ||
                                //RULE:: if webcastType gets changed from audio to video or vice-versa
                                isAudioWebcast != isCurrentAudioWebcast ||
                                //RULE:: Deactivate ECDN from activate state
                                (
                                        webcastPayload.isCurrentECDNStatus() &&
                                                !webcastPayload.isEnableECDNActive() &&
                                                webcastPayload.getP2pVendor() == null
                                ) ||
                                //RULE:: Deactivate china cdn (china inactive + kollective true + previous kollective true
                                (
                                        (webcastPayload.isCurrentChinaCdnStatus() != webcastPayload.isEnableChinaCDN()) &&
                                                webcastPayload.isEnableECDNActive() && webcastPayload.isCurrentECDNStatus()
                                ));
                    };

                    if(checkShouldResetAllLink.apply("")){
                        List<List<StreamInfo>> streams = generateStreamsBasedOnLanguages.apply(langs);
                        String url = "/streams/"+webcastPayload.getWebcastId()+"/status/preview";
                        legacyService.changeStreamUrl(url , streams.get(0));
                        url = "/streams/"+webcastPayload.getWebcastId()+"/status/ondemand";
                        legacyService.changeStreamUrl(url,streams.get(2));
                        if(!streams.get(1).isEmpty()){
                            url = "/streams/"+webcastPayload.getWebcastId()+"/status/live";
                            legacyService.changeStreamUrl(url , streams.get(1));
                        }
                    }
                    // this else block will update the stream urls according to the various condition
                    else {
                        List<String> currentAdditionalLanguages = webcastPayload.getCurrentAdditionalLanguage();
                        currentAdditionalLanguages.add(webcastPayload.getCurrentDefaultLanguage());
                        List<String> newLanguage = langs.stream().filter(ln->!currentAdditionalLanguages.contains(ln)).collect(Collectors.toList());
                        List<String> removedLanguage = currentAdditionalLanguages.stream().filter(ln->!langs.contains(ln)).collect(Collectors.toList());

                        // removed language part handle
                        List<StreamInfo> acceptedLiveStream = liveStream_.stream().filter(stm -> !removedLanguage.contains(stm.language)).collect(Collectors.toList());
                        List<StreamInfo> acceptedPreviewStream = previewStream_.stream().filter(stm->!removedLanguage.contains(stm.language)).collect(Collectors.toList());
                        List<StreamInfo> acceptedOndemandStream = ondemandStream_.stream().filter(stm->!removedLanguage.contains(stm.language)).collect(Collectors.toList());

                        //RULE:: Activate ECDN from none to kollective
                        if (
                                (!webcastPayload.isCurrentECDNStatus() && webcastPayload.isEnableECDNActive()) || (webcastPayload.isCurrentChinaCdnStatus() != webcastPayload.isEnableChinaCDN())
                        ){
                            List<StreamInfo> __apsList = new ArrayList<>();
                            for(StreamInfo aps : acceptedPreviewStream ){
                                StreamInfo __aps = aps;
                                if(aps.url.startsWith("_kollective_")){
                                    __aps = isAudioWebcast ?
                                            addStream.audioPreviewStreams(
                                                    !!webcastPayload.isEnableChinaCDN(),
                                                    aps.getLanguage(),
                                                    aps.getUrl()
                                            ):
                                            addStream.videoPreviewStreams(
                                                    webcastPayload.getBitRate(),
                                                    webcastPayload.isEnableChinaCDN(),
                                                    aps.getLanguage(),
                                                    aps.getUrl()
                                            );
                                }
                                else {
                                    __aps.url = isAudioWebcast ? addStream.convertAudioPreviewStreams(
                                            webcastPayload.isEnableChinaCDN(),
                                            aps.getUrl()
                                    ) : addStream.convertVideoPreviewStreams(
                                            !!webcastPayload.isEnableChinaCDN(),
                                            aps.getUrl()
                                    );
                                }
                                __apsList.add(__aps);
                            }
                            acceptedPreviewStream = __apsList;

                            List<StreamInfo> __aosList = new ArrayList<>();
                            for(StreamInfo aos : acceptedOndemandStream ){
                                StreamInfo __aos = aos;
                                if(aos.url.startsWith("_kollective_")){
                                    __aos = isAudioWebcast ?
                                            addStream.audioOnDemandStreams(
                                                    !!webcastPayload.isEnableChinaCDN(),
                                                    aos.getLanguage()
                                            ):
                                            addStream.videoOnDemandStreams(
                                                    webcastPayload.getBitRate(),
                                                    webcastPayload.isEnableChinaCDN(),
                                                    aos.getLanguage()
                                            );
                                }
                                else {
                                    __aos.url = isAudioWebcast ? addStream.convertAudioOnDemandStreams(
                                            webcastPayload.isEnableChinaCDN(),
                                            aos.getUrl()
                                    ) : addStream.convertVideoOnDemandStreams(
                                            !!webcastPayload.isEnableChinaCDN(),
                                            aos.getUrl()
                                    );
                                }
                                __aosList.add(__aos);
                            }
                            acceptedOndemandStream = __aosList;

                            if(findWebcast.getType().equals("zoom") && !webcastPayload.isEnableECDNActive() && (!Objects.equals(webcastPayload.getP2pVendor(),StaticValues.kollective)||webcastPayload.getP2pVendor()==null)){
                                Meeting meeting = meetingRepository.findMeetingByWebcastId(webcastPayload.getWebcastId().toString());
                                if(meeting != null && meeting.getId() != null){
                                    String url = "/meetings/"+meeting.getId()+"/livestream";
                                    HashMap<String , String> body = new HashMap<>();
                                    body.put("stream_url" , "rtmp://"+ environmentProperties.NEXT_PUBLIC_MAIN_WOWZA_ZOOM_STREAM_URL + "/" + environmentProperties.NEXT_PUBLIC_MAIN_WOWZA_APPLICATION_ZOOM + Integer.valueOf(webcastPayload.getBitRate().split(",")[0]));
                                    body.put("stream_key" , webcastPayload.getWebcastId() +"_en_p_9999");
                                    body.put("page_url","https://www.wtvglobal.com");
                                    zoomService.patchRequestZoom(url , body);
                                }
                            }

                            List<StreamInfo> liveButKollective = acceptedLiveStream.stream().filter(st->st.getUrl().startsWith("_kollective_")).collect(Collectors.toList());

                            List<StreamInfo> live = new ArrayList<>();
                            for(StreamInfo als : acceptedLiveStream.stream().filter(u->!u.getUrl().startsWith("_kollective_")).collect(Collectors.toList())){
                                String newLiveUrl = addStream.convertLiveStreams(
                                        webcastPayload.isEnableChinaCDN(),
                                        als.getLanguage(),
                                        als.getUrl(),
                                        findWebcast.getType()
                                );
                                als.setUrl(newLiveUrl);
                                live.add(als);
                            }
                            acceptedLiveStream = live;

                            live = new ArrayList<>();
                            for (StreamInfo lbk : liveButKollective) {
                                List<StreamInfo> reCreateLiveStreamData = null;
                                if (findWebcast.getType().equals("zoom")) {
                                    reCreateLiveStreamData = isAudioWebcast ?
                                            addStream.generateAudioZoomStream(
                                                    webcastPayload.getBitRate(),
                                                    List.of(lbk.getLanguage()),
                                                    webcastPayload.getWebcastId().toString(),
                                                    webcastPayload.isEnableChinaCDN(),
                                                    new ArrayList<>())
                                            :
                                            addStream.generateZoomStream(
                                                    webcastPayload.getBitRate(),
                                                    List.of(lbk.getLanguage()),
                                                    webcastPayload.getWebcastId().toString(),
                                                    webcastPayload.isEnableChinaCDN(),
                                                    new ArrayList<>()
                                            );
                                } else {
                                    reCreateLiveStreamData = isAudioWebcast ?
                                            addStream.audioLiveStreams(
                                                    List.of(lbk.getLanguage()),
                                                    webcastPayload.getWebcastId().toString(),
                                                    webcastPayload.isEnableChinaCDN(),
                                                    new ArrayList<>()
                                            ) :
                                            addStream.generateStream(
                                                    webcastPayload.getBitRate(),
                                                    List.of(lbk.getLanguage()),
                                                    webcastPayload.getWebcastId().toString(),
                                                    webcastPayload.isEnableChinaCDN(),
                                                    new ArrayList<>());
                                }
                                live.addAll(reCreateLiveStreamData);
                            }
                            acceptedLiveStream.addAll(live);

                            if (Objects.equals(webcastPayload.getP2pVendor(),StaticValues.kollective)) {
                                List<StreamInfo> __acceptedPreviewStreams = new ArrayList<>();
                                for (StreamInfo gp : acceptedPreviewStream) {
                                    StreamInfo __gp = gp;
                                    KollectiveResponse kollectiveStream = kollective.generateKollectiveTokenforOnDemand(
                                            webcastPayload.getWebcastId().toString(),
                                            webcastPayload.getTitle(),
                                            ecdnEntry.getVendor().get(0).getSecrets().get(0).getValue(),
                                            ecdnEntry.getVendor().get(0).getSecrets().get(1).getValue(),
                                            gp.getUrl()
                                    );
                                    __gp.setUrl("_kollective_," + kollectiveStream.getToken());
                                    __acceptedPreviewStreams.add(__gp);
                                }
                                acceptedPreviewStream = __acceptedPreviewStreams;

                                List<StreamInfo> __acceptedOnDemandStreams = new ArrayList<>();
                                for (StreamInfo go : acceptedOndemandStream) {
                                    StreamInfo __go = go;
                                    KollectiveResponse kollectiveStream = kollective.generateKollectiveTokenforOnDemand(
                                            webcastPayload.getWebcastId().toString(),
                                            webcastPayload.getTitle(),
                                            ecdnEntry.getVendor().get(0).getSecrets().get(0).getValue(),
                                            ecdnEntry.getVendor().get(0).getSecrets().get(1).getValue(),
                                            go.getUrl()
                                    );
                                    __go.setUrl("_kollective_," + kollectiveStream.getToken());
                                    __acceptedOnDemandStreams.add(__go);
                                }
                                acceptedOndemandStream = __acceptedOnDemandStreams;

                                List<StreamInfo> __acceptedLiveStreams = new ArrayList<>();
                                // remove backup strings when kollective mode enabled from the none mode
                                for (StreamInfo gl : acceptedLiveStream.stream().filter(als -> als.getUrl().contains("_"+als.getLanguage()+"_p_")).collect(Collectors.toList())) {
                                    StreamInfo __gl = gl;
                                    KollectiveResponse kollectiveStream = null;
                                    if (findWebcast.getType().equals("zoom")) {
                                        kollectiveStream = kollective.generateKollectiveTokenforZoom(
                                                gl.getLanguage(),
                                                webcastPayload.getBitRate(),
                                                webcastPayload.getWebcastId().toString(),
                                                webcastPayload.getTitle(),
                                                ecdnEntry.getVendor().get(0).getSecrets().get(0).getValue(),
                                                ecdnEntry.getVendor().get(0).getSecrets().get(1).getValue(),
                                                webcastPayload.isEnableChinaCDN()
                                        );
                                    } else {
                                        kollectiveStream = isAudioWebcast ?
                                                kollective.generateKollectiveTokenforSelfAudioOnly(
                                                        gl.getLanguage(),
                                                        webcastPayload.getBitRate(),
                                                        webcastPayload.getWebcastId().toString(),
                                                        webcastPayload.getTitle(),
                                                        ecdnEntry.getVendor().get(0).getSecrets().get(0).getValue(),
                                                        ecdnEntry.getVendor().get(0).getSecrets().get(1).getValue(),
                                                        webcastPayload.isEnableChinaCDN()
                                                ) :
                                                kollective.generateKollectiveTokenforSelf(
                                                        gl.getLanguage(),
                                                        webcastPayload.getBitRate(),
                                                        webcastPayload.getWebcastId().toString(),
                                                        webcastPayload.getTitle(),
                                                        ecdnEntry.getVendor().get(0).getSecrets().get(0).getValue(),
                                                        ecdnEntry.getVendor().get(0).getSecrets().get(1).getValue(),
                                                        webcastPayload.isEnableChinaCDN()
                                                );
                                    }
                                    __gl.setUrl("_kollective_," + kollectiveStream.getToken());
                                    __acceptedLiveStreams.add(__gl);
                                }
                                acceptedLiveStream = __acceptedLiveStreams;
                            }
                        }

                        if (!newLanguage.isEmpty()) {
                            List<List<StreamInfo>> __newStreams = generateStreamsBasedOnLanguages.apply(newLanguage);
                            acceptedPreviewStream.addAll(__newStreams.get(0));
                            acceptedOndemandStream.addAll(__newStreams.get(2));
                            if (!__newStreams.get(1).isEmpty()) {
                                acceptedLiveStream.addAll(__newStreams.get(1));
                            }
                        }

                        String url = "/streams/"+webcastPayload.getWebcastId()+"/status/preview";
                        legacyService.changeStreamUrl(url , acceptedPreviewStream);
                        url = "/streams/"+webcastPayload.getWebcastId()+"/status/ondemand";
                        legacyService.changeStreamUrl(url,acceptedOndemandStream);
                        if(!acceptedLiveStream.isEmpty()){
                            url = "/streams/"+webcastPayload.getWebcastId()+"/status/live";
                            legacyService.changeStreamUrl(url , acceptedLiveStream);
                        }
                    }
                }
                else {
                    throw new HandleWebcastException("Event has not accessed from legacy api", HttpStatus.FORBIDDEN);
                }
            }
        }catch (Exception e){
            throw new HandleWebcastException("User Input Error" , HttpStatus.BAD_REQUEST);
        }
    }

    private String getSystemTextWebcastType(String toBeCheckedWebcastType){
        switch (toBeCheckedWebcastType) {
            case "videoAudioSlide":
            case "audioSlide":
                return "videoSlides";
            case "videoAudio":
            case "audio":
                return "videoOnly";
            default:
                return toBeCheckedWebcastType;
        }
    };

    private Ecdn getEcdn(LegacyBroadcast legacyBroadcast){

        Ecdn defaultEcdn = new Ecdn();
        defaultEcdn.setId("");
        defaultEcdn.setCascadeLevel("organisation");
        defaultEcdn.setVendor(new ArrayList<>());

        try {
            Ecdn webcastEcdn = ecdnRepository.findByEntityId(legacyBroadcast.getId());
            Ecdn clientEcdn = ecdnRepository.findByEntityId(legacyBroadcast.getCso());
            Ecdn orgEcdn = ecdnRepository.findByEntityId(legacyBroadcast.getCo());

            if(webcastEcdn != null)
            {
                if(webcastEcdn.getVendor().size() > 0){
                     defaultEcdn.setId(webcastEcdn.getId());
                     defaultEcdn.setVendor(webcastEcdn.getVendor());
                     defaultEcdn.setCascadeLevel("webcast");
                     return defaultEcdn;
                }else {
                    defaultEcdn.setId(webcastEcdn.getId());
                    defaultEcdn.setVendor(new ArrayList<>());
                    defaultEcdn.setCascadeLevel("organization");
                    return defaultEcdn;
                }
            }
            if(clientEcdn != null)
            {
                if(clientEcdn.getVendor().size() > 0){
                    defaultEcdn.setId(clientEcdn.getId());
                    defaultEcdn.setVendor(clientEcdn.getVendor());
                    defaultEcdn.setCascadeLevel("client");
                    return defaultEcdn;
                }else {
                    defaultEcdn.setId(clientEcdn.getId());
                    defaultEcdn.setVendor(new ArrayList<>());
                    defaultEcdn.setCascadeLevel("organization");
                    return defaultEcdn;
                }
            }
            if(orgEcdn != null)
            {
                if(orgEcdn.getVendor().size() > 0){
                    defaultEcdn.setId(orgEcdn.getId());
                    defaultEcdn.setVendor(orgEcdn.getVendor());
                    defaultEcdn.setCascadeLevel("org");
                    return defaultEcdn;
                }else {
                    defaultEcdn.setId(orgEcdn.getId());
                    defaultEcdn.setVendor(new ArrayList<>());
                    defaultEcdn.setCascadeLevel("organization");
                    return defaultEcdn;
                }
            }
            return defaultEcdn;
        }catch (Exception e){
            return defaultEcdn;
        }
    }

    private List<StreamInfo> getSteamList(List<StreamInfo> list){
        List<StreamInfo> newList = new ArrayList<>();
        list.forEach(x->{
            try {
                newList.add((StreamInfo) x.clone());
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
        });
        return  newList;
    }

    public void updateConnectStudioSession(String webcastId , WebcastPayload data , MultipartFile file){
        String sessionUrl = connectstudioSessionUrl;
        Session session = new Session();
        session.setTitle(data.getTitle());
        session.setScheduleLiveStartDate(data.getScheduleLiveStartDate());
        session.setOndemandOverDate(data.getOndemandOverDate());
        if(data.getTimeZone() != null){
            session.setTimeZone(data.getTimeZone());
        }
        session.setAdditionalLanguages(data.getAdditionalLanguage());
        session.setEnableChinaCDN(data.isEnableChinaCDN()?"true":"false");
        session.setDuration(data.getDuration());
        try {
            MultiValueMap<String,Object> body = new LinkedMultiValueMap<>();
            body.add("data",new ObjectMapper().writeValueAsString(session));
            if(file != null){
                body.add("file",file.getResource());
            }
            HttpHeaders tokenHeader = new HttpHeaders();
            tokenHeader.setContentType(MediaType.MULTIPART_FORM_DATA);
            HttpEntity<MultiValueMap<String,Object>> requestMap = new HttpEntity<>(body,tokenHeader);
            String uri = sessionUrl + "/update-session/" + webcastId;
            log.info("calling the service of Session service url -----> "+uri);
            ResponseEntity response = new RestTemplate().exchange(uri, HttpMethod.PUT,requestMap,String.class);
        }catch (Exception e){
            log.error("fail to update session " +e.getMessage());
        }
    }
}
