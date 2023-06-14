package com.wtv.webcastmanagement.service;

import com.wtv.webcastmanagement.Exceptions.HandleLegacyException;
import com.wtv.webcastmanagement.dto.dto.others.FileConverterResponse;
import com.wtv.webcastmanagement.dto.dto.others.HeadshotResponseBody;
import com.wtv.webcastmanagement.dto.dto.primaryMark.Frame;
import com.wtv.webcastmanagement.dto.dto.primaryMark.MarkResponse;
import com.wtv.webcastmanagement.entity.Session;
import com.wtv.webcastmanagement.entity.Webcast;
import com.wtv.webcastmanagement.entity.legacy.*;
import com.wtv.webcastmanagement.entity.legacy.sysText.SystemTextData;
import com.wtv.webcastmanagement.utils.StaticValues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.util.Pair;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LegacyService {
    @Value("${stream_studio.legacy.base_url}")
    private String legacyBaseUrl;

    @Value("${stream_studio.legacy.username}")
    private String legacyUserName;

    @Value("${stream_studio.legacy.password}")
    private String legacyPassword;

    @Value("${stream_studio.legacy.application_id}")
    private String legacyApplicationId;

    @Value("${stream_studio.legacy.master_template}")
    private String legacyMasterPlate;

    @Value("${stream_studio.legacy.master_template_nsr}")
    private String legacyMasterPlateNSR;

    @Value("${stream_studio.legacy.organization_id_nsr}")
    private String nsrOrganizationId;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${MAIN_WOWZA_ONDEMAND_CHINA_URL}")
    String wowzaOndemandChinaUrl;
    @Value("${NEXT_PUBLIC_MAIN_WOWZA_ONDEMAND_URL}")
    String wowzaOndemandUrl;
    private String legacyAccessToken = null;
    @Value("${SLIDE_CONVERTER}")
    private String slideConverter;
    @Autowired
    private GenerateStreamUrl generateStreamUrl;

    Logger logger = LoggerFactory.getLogger(LegacyService.class);

    public String getLegacyAccessToken() {
        if (this.legacyAccessToken == null) {
            String accessToken = loginToLegacySteamStudio();
            if (accessToken == null) {
                throw new HandleLegacyException("Legacy API authentication is failed", HttpStatus.UNAUTHORIZED);
            }
            this.legacyAccessToken = accessToken;
        }
        return legacyAccessToken;
    }

    private HttpHeaders accessHeaders(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(StaticValues.Authorization, StaticValues.Bearer + accessToken);
        headers.set(StaticValues.content_Type, StaticValues.application_Json);
        return headers;
    }

    public String loginToLegacySteamStudio() {
        Map<String, String> loginPayload = new HashMap<>();
        loginPayload.put("userName", legacyUserName);
        loginPayload.put("password", legacyPassword);
        loginPayload.put("applicationId", legacyApplicationId);

        String loginApiEndpoint = legacyBaseUrl + "/user/login";
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Map> requestEntity = new HttpEntity<>(loginPayload, headers);

        ResponseEntity<LoginResponse> loginResponse = restTemplate.exchange(loginApiEndpoint, HttpMethod.PUT, requestEntity, LoginResponse.class, loginPayload);
        if (loginResponse.getStatusCodeValue() == 200) {
            this.legacyAccessToken = Objects.requireNonNull(loginResponse.getBody()).access_token;
            return this.getLegacyAccessToken();
        } else {
            logger.warn("Fail to login legacy service");
            return null;
        }
    }

    public Pair<String,Boolean> cloneWebcast(Session session) {

        Map<String, Object> clonePayload = new HashMap<>();
        clonePayload.put("title", session.getTitle());
        clonePayload.put("cso", session.getLegacyClientId());
        clonePayload.put(StaticValues.scheduleLiveStartDate, session.getScheduleLiveStartDate());
        clonePayload.put(StaticValues.ondemandOverDate, session.getOndemandOverDate());
        clonePayload.put(StaticValues.additionalLanguages, session.getAdditionalLanguages());
        clonePayload.put(StaticValues.defaultLanguage, session.getDefaultLanguage());
        boolean registrationRequired = false;

        String masterTemplateId = legacyMasterPlate;
        // set NSR master template
        if (session.getOrganizationId().equals(nsrOrganizationId)) {
            masterTemplateId = legacyMasterPlateNSR;
        }
        String accessToken = this.loginToLegacySteamStudio();
        LegacyBroadcast legacyBroadcast = this.getLegacyEvent(masterTemplateId);
        if(legacyBroadcast != null){
            clonePayload.put(StaticValues.features, legacyBroadcast.getFeatures());
            if(legacyBroadcast.getFeatures().contains("f1"))
                registrationRequired = true;
        }
        String cloneApiEndpoint = legacyBaseUrl + "/event/" + masterTemplateId + "/clone";
        HttpHeaders headers = accessHeaders(accessToken);
        HttpEntity<Map> requestEntity = new HttpEntity<>(clonePayload, headers);

        ResponseEntity<String> cloneResponse = restTemplate.exchange(cloneApiEndpoint, HttpMethod.PUT, requestEntity, String.class, clonePayload);

        if (cloneResponse.getStatusCodeValue() == 500) {
            this.legacyAccessToken = null;
            accessToken = this.getLegacyAccessToken();
            headers = accessHeaders(accessToken);
            requestEntity = new HttpEntity<>(headers);
            cloneResponse = restTemplate.exchange(cloneApiEndpoint, HttpMethod.PUT, requestEntity, String.class, clonePayload);
        }
        if (cloneResponse.getStatusCodeValue() != 404) {
            String webcastId = cloneResponse.getBody();
            changeWebcastStatusToPreview(webcastId, clonePayload, accessToken);
            clonePayload.put("enableChinaCDN", session.getEnableChinaCDN());
            clonePayload.put("bitRate", session.getBitRate());
            clonePayload.put("meeting",session.getMeeting());
            fixWebcastStreamUrls(webcastId, clonePayload, accessToken);
            return Pair.of(webcastId,registrationRequired);
        } else {
            throw new HandleLegacyException("Error during cloning webcast! Please contact admin!!", HttpStatus.BAD_REQUEST);
        }
    }

    public void fixWebcastStreamUrls(String webcastId, Map clonePayload, String accessToken) {
        String chinaStatus = (String) clonePayload.get("enableChinaCDN");
        boolean enableChinaCDN = chinaStatus == "true" ? true : false;
        String meetingType = (String) clonePayload.get("meeting");
        ArrayList<Map> live_payload = new ArrayList<>();
        ArrayList<Map> preview_payload = new ArrayList<>();
        ArrayList<Map> ondemand_payload = new ArrayList<>();
        if (Objects.equals(meetingType , StaticValues.zoom)) {
            live_payload = generateStreamUrl.generateStreamZoomUrls(webcastId, clonePayload , enableChinaCDN);
            preview_payload = generateStreamUrl.generateStreamZoomPreviewUrls(webcastId, clonePayload , enableChinaCDN);
            ondemand_payload = generateStreamUrl.generateStreamZoomPreviewUrls(webcastId, clonePayload , enableChinaCDN);

        } else {
            live_payload = generateStreamUrl.generateStreamUrlsUpdate(webcastId, clonePayload , enableChinaCDN);
            preview_payload = generateStreamUrl.generateStreamPreviewUrls(webcastId, clonePayload , enableChinaCDN);
            ondemand_payload = generateStreamUrl.generateStreamPreviewUrls(webcastId, clonePayload , enableChinaCDN);
        }
        String fixUrlApiEndpointLive = legacyBaseUrl + "/streams/" + webcastId + "/status/live";
        String fixUrlApiEndpointPreview = legacyBaseUrl + "/streams/" + webcastId + "/status/preview";
        String fixUrlApiEndpointOndemand= legacyBaseUrl + "/streams/" + webcastId + "/status/ondemand";
        HttpHeaders headers = accessHeaders(accessToken);

        HttpEntity<Object> requestEntity = new HttpEntity<>(live_payload, headers);
        restTemplate.exchange(fixUrlApiEndpointLive, HttpMethod.PUT, requestEntity, Object.class, live_payload);

        requestEntity = new HttpEntity<>(preview_payload, headers);
        restTemplate.exchange(fixUrlApiEndpointPreview, HttpMethod.PUT, requestEntity, Object.class, preview_payload);

        requestEntity = new HttpEntity<>(ondemand_payload, headers);
        restTemplate.exchange(fixUrlApiEndpointOndemand, HttpMethod.PUT, requestEntity, Object.class, ondemand_payload);
    }

    public void changeWebcastStatusToPreview(String webcastId, Map clonePayload, String accessToken) {
        Map<String, Object> statusChangePayload = new HashMap<>();
        statusChangePayload.put(StaticValues.status, StaticValues.preview);
        statusChangePayload.put(StaticValues.additionalLanguages, clonePayload.get(StaticValues.additionalLanguages));
        statusChangePayload.put(StaticValues.features, clonePayload.get(StaticValues.features));

        String changeStatusApiEndpoint = legacyBaseUrl + "/event/" + webcastId;
        HttpHeaders headers = accessHeaders(accessToken);
        HttpEntity<Object> requestEntity = new HttpEntity<>(statusChangePayload, headers);

        restTemplate.exchange(changeStatusApiEndpoint, HttpMethod.PUT, requestEntity, Object.class, statusChangePayload);
    }


    public void updateEvent(String webcastId, Map payload) {
        String accessToken = getLegacyAccessToken();
        Map<String, Object> changePayload = new HashMap<>();
        if (payload.get(StaticValues.scheduleLiveStartDate) != null) {
            changePayload.put(StaticValues.scheduleLiveStartDate, payload.get(StaticValues.scheduleLiveStartDate));
            changePayload.put(StaticValues.ondemandOverDate, payload.get(StaticValues.ondemandOverDate));
        }
        if (payload.get(StaticValues.name) != null) {
            changePayload.put(StaticValues.name, payload.get(StaticValues.name));
        }
        changePayload.put(StaticValues.status, payload.get(StaticValues.status));
        changePayload.put(StaticValues.additionalLanguages, payload.get(StaticValues.additionalLanguages));
        changePayload.put(StaticValues.features, payload.get(StaticValues.features));
        String updateWebcastApiEndpoint = legacyBaseUrl + "/event/" + webcastId;
        HttpHeaders headers = accessHeaders(accessToken);
        HttpEntity<Object> requestEntity = new HttpEntity<>(changePayload, headers);
        try {
            ResponseEntity responseEntity = restTemplate.exchange(updateWebcastApiEndpoint, HttpMethod.PUT, requestEntity, Object.class, changePayload);
            if (responseEntity.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new HandleLegacyException("Legacy error : " + responseEntity.getBody().toString(), responseEntity.getStatusCode());
            }
            fixWebcastStreamUrls(webcastId, payload, accessToken);

        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new HandleLegacyException(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    public List<StreamInfo> getStreamInfo(String webcastId, String status) {
        String accessToken = getLegacyAccessToken();
        HttpHeaders headers = accessHeaders(accessToken);
        HttpEntity<Object> requestEntity = new HttpEntity<>(headers);
        String getStreamInfoEndpoint = legacyBaseUrl + "/streams/" + webcastId + "/status/" + status;
        ResponseEntity<StreamInfo[]> streamInfoListResponse = null;
        try {
            streamInfoListResponse = restTemplate.exchange(getStreamInfoEndpoint, HttpMethod.GET, requestEntity, StreamInfo[].class);
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new HandleLegacyException(e.getMessage(), HttpStatus.NOT_FOUND);
        }
        return Arrays.asList(streamInfoListResponse.getBody());
    }

    public ResponseEntity uploadSlideUpdated(MultipartFile file, String fileName, String status,
                                      String aspectRatio, String prefix, String language, String webcastTicket) {

        try {
            fileName = LocalDateTime.now() + file.getOriginalFilename();
            MultiValueMap<String, Object> body
                    = new LinkedMultiValueMap<>();
            File convertedFile = convertMultiPartFileToFile(file);
            body.add("file", new FileSystemResource(convertedFile));
            body.add("fileName", fileName);
            body.add("status", status);
            body.add("aspectRatio", aspectRatio);
            body.add("prefix", prefix);
            body.add("language", language);
            body.add("webcastId",webcastTicket);
            FileConverterResponse fileConverterResponse = getConverterServiceRes(body);

            if(fileConverterResponse != null){
                String initialSlideNumber = "";
                List<String> images = fileConverterResponse.getImages();
                if(images.size()>0){
                    String firstImage = images.get(0).contains("Thumbs.db") ? images.get(1) : images.get(0);
                    initialSlideNumber = firstImage.split(prefix+"_")[1].replace(".jpg","");
                }
                String firstSlide = prefix + "_" + initialSlideNumber;

                if(status.equals(StaticValues.preview)){
                    updatePrimaryMarkForSlide(webcastTicket , StaticValues.preview , firstSlide);
                }
                if(status.equals(StaticValues.live)){
                    updatePrimaryMarkForSlide(webcastTicket , StaticValues.live , firstSlide);
                    updatePrimaryMarkForSlide(webcastTicket , StaticValues.ondemand , firstSlide);
                }
            }
            convertedFile.delete();
            return fileConverterResponse == null? ResponseEntity.status(HttpStatus.NOT_FOUND).body("Something went wrong to file converter service"):
                    ResponseEntity.ok("slides uploaded successfully ");
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getRawStatusCode()).headers(e.getResponseHeaders())
                    .body(e.getResponseBodyAsString());
        } catch (Exception e) {
            logger.error("Something went wrong during upload silde with -> " + e.getMessage());
            throw new HandleLegacyException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity getSlides(String webcastId, String status) {
        String accessToken = getLegacyAccessToken();
        HttpHeaders headers = accessHeaders(accessToken);
        HttpEntity<Object> requestEntity = new HttpEntity<>(headers);
        String getStreamInfoEndpoint = legacyBaseUrl + "/event/" + webcastId + "/slides?status=" + status;
        ResponseEntity<SlideResponse[]> slideListResponse = null;
        try {
            slideListResponse = restTemplate.exchange(getStreamInfoEndpoint, HttpMethod.GET, requestEntity, SlideResponse[].class);
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new HandleLegacyException(e.getMessage(), HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(slideListResponse.getBody());
    }

    public ResponseEntity deleteSlides(List<SlideData> slideData, String webcastId, String status) {
        String accessToken = getLegacyAccessToken();
        HttpHeaders headers = accessHeaders(accessToken);
        HttpEntity<Object> requestEntity = new HttpEntity<>(slideData, headers);
        String getSlideDeleteInfoEndpoint = legacyBaseUrl + "/event/" + webcastId + "/slides?status=" + status;
        ResponseEntity response = null;
        try {
            response = restTemplate.exchange(getSlideDeleteInfoEndpoint, HttpMethod.DELETE, requestEntity, ResponseEntity.class);
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new HandleLegacyException(e.getMessage(), HttpStatus.NOT_FOUND);
        }
        return response;
    }

    public ResponseEntity changeWebcastType(){
        String coId ="1210";
        String csoId = "602";
        String webcastId = "15146";
        String accessToken = getLegacyAccessToken();
        HttpHeaders headers = accessHeaders(accessToken);
        HttpEntity<Object> requestEntity = new HttpEntity<>(headers);
        String getSystemTextUrl = legacyBaseUrl + "/content/" + coId +"-"+ csoId +"-"+ webcastId +"/systemText?cascaded=true";
        ResponseEntity<SystemTextData> systemTextData = null;
        try {
            systemTextData = restTemplate.exchange(getSystemTextUrl, HttpMethod.GET, requestEntity, SystemTextData.class);
        } catch (Exception e) {}
        return ResponseEntity.ok(systemTextData.getBody());
    }

    public ResponseEntity setWebcastTypeInSystemText(String co , String cso , String webcastId , String webcastType){
        String accessToken = getLegacyAccessToken();
        HttpHeaders headers = accessHeaders(accessToken);
        HttpEntity<Object> requestEntity = new HttpEntity<>(headers);
        String systemTextUrl = legacyBaseUrl + "/content/" + co +"-"+ cso +"-"+ webcastId +"/systemText?cascaded=true";
        ResponseEntity<SystemTextData> systemTextData = null;
        try {
            systemTextData = restTemplate.exchange(systemTextUrl, HttpMethod.GET, requestEntity, SystemTextData.class);
            SystemTextData systemTextDataResponse = systemTextData.getBody();
            //Changing the systemText
            if(!systemTextDataResponse.getPages().isEmpty()){
                systemTextDataResponse.setPages(systemTextDataResponse.getPages().stream().map(x->{
                    if(x.getId().equals("global")){
                        x.setEntries(x.getEntries().stream().map(y->{
                            if(y.getId().equals("webcastType")){
                                y.setTexts(y.getTexts().stream().map(z->{
                                    z.setText(webcastType);
                                    return z;
                                }).collect(Collectors.toList()));
                                return y;
                            }
                            return y;
                        }).collect(Collectors.toList()));
                        return x;
                    }
                    return x;
                }).collect(Collectors.toList()));
            }

            requestEntity = new HttpEntity<>(systemTextDataResponse , headers);
            systemTextUrl = legacyBaseUrl + "/content/" + co +"-"+ cso +"-"+ webcastId +"/systemText";
            restTemplate.exchange(systemTextUrl, HttpMethod.PUT, requestEntity, SystemTextData.class);

            return ResponseEntity.ok(systemTextDataResponse);

        } catch (Exception e) {
            logger.warn(e.getMessage());
        }
        return ResponseEntity.ok(systemTextData.getBody());
    }

    public void updateOnlyEvent(String webcastId,HashMap<String , Object> payload){
        String accessToken = getLegacyAccessToken();
        Map<String, Object> changePayload = new HashMap<>();
        if (payload.get(StaticValues.scheduleLiveStartDate) != null) {
            changePayload.put(StaticValues.scheduleLiveStartDate, payload.get(StaticValues.scheduleLiveStartDate));
            changePayload.put(StaticValues.ondemandOverDate, payload.get(StaticValues.ondemandOverDate));
        }
        if (payload.get(StaticValues.name) != null) {
            changePayload.put(StaticValues.name, payload.get(StaticValues.name));
        }
        changePayload.put(StaticValues.status, payload.get(StaticValues.status));
        changePayload.put(StaticValues.defaultLanguage , payload.get(StaticValues.defaultLanguage));
        changePayload.put(StaticValues.additionalLanguages, payload.get(StaticValues.additionalLanguages));
        changePayload.put(StaticValues.features, payload.get(StaticValues.features));
        String updateWebcastApiEndpoint = legacyBaseUrl + "/event/" + webcastId;
        HttpHeaders headers = accessHeaders(accessToken);
        HttpEntity<Object> requestEntity = new HttpEntity<>(changePayload, headers);
        try {
            ResponseEntity responseEntity = restTemplate.exchange(updateWebcastApiEndpoint, HttpMethod.PUT, requestEntity, Object.class, changePayload);
            if (responseEntity.getStatusCode() == HttpStatus.NOT_FOUND) {
                logger.error("Fail to update webcast to legacy");
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    public LegacyBroadcast getLegacyEvent(String webcastId){
        String accessToken = loginToLegacySteamStudio();
        HttpHeaders headers = accessHeaders(accessToken);
        HttpEntity<Object> requestEntity = new HttpEntity<>(headers);
        String enpointOfBroadcast = legacyBaseUrl + "/event/" + webcastId + "/";
        ResponseEntity<LegacyBroadcast> legacyBroadcastResponse = null;
        try {
            legacyBroadcastResponse = restTemplate.exchange(enpointOfBroadcast, HttpMethod.GET, requestEntity, LegacyBroadcast.class);
            if(legacyBroadcastResponse.getBody()!=null){
                return legacyBroadcastResponse.getBody();
            }
        }catch (Exception e){
            logger.error("Legacy Call Not Working "+e.getMessage());
        }
        return null;
    }

    public void putRequestWithoutJSONResponse(String entityId){
        try {
            String accessToken = getLegacyAccessToken();
            HttpHeaders headers = accessHeaders(accessToken);
            HttpEntity<Object> requestEntity = new HttpEntity<>(true,headers);
            String getHierarchyApi = legacyBaseUrl + "/hierarchy/suborganization/"+entityId+"/csoProperty/stst.p2p.ecdn.peer5";
            restTemplate.exchange(getHierarchyApi, HttpMethod.PUT, requestEntity, Object.class);
        }catch (Exception e){

        }
    }
    public void putRequestWithoutJSONResponseEvent(String webcastId , String base_url){
        try {
            String accessToken = getLegacyAccessToken();
            HttpHeaders headers = accessHeaders(accessToken);
            HttpEntity<Object> requestEntity = new HttpEntity<>(base_url ,headers);
            String getHierarchyApi = legacyBaseUrl + "/event/"+webcastId+"/property/stst.htmlstatic.baseurl";
            ResponseEntity<Object> responseEntity = restTemplate.exchange(getHierarchyApi, HttpMethod.PUT, requestEntity, Object.class);
            if(responseEntity.getStatusCode() == HttpStatus.OK){

            }
        }catch (Exception e){
          logger.error(e.getMessage());
        }
    }

    public ResponseEntity uploadHeadshot(MultipartFile file, String fileName, String status,
                                      String aspectRatio, String prefix, String language,String imageType, String webcastId) {

        try {
            String accessToken = getLegacyAccessToken();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.set("Authorization", "Bearer " + accessToken);
            MultiValueMap<String, Object> body
                    = new LinkedMultiValueMap<>();
            File convertedFile = convertMultiPartFileToFile(file);
            body.add("file", new FileSystemResource(convertedFile));
            body.add("fileName", generateFileId(fileName,webcastId));
            body.add("status", status);
            body.add("aspectRatio", aspectRatio);
            body.add("prefix", prefix);
            body.add("language", language);
            body.add("imageType" , imageType);
            HttpEntity<MultiValueMap<String, Object>> requestEntity
                    = new HttpEntity<>(body, headers);
            String uploadUrl = legacyBaseUrl + "/event/" + webcastId + "/uploadslides";
            ResponseEntity response = restTemplate
                    .postForEntity(uploadUrl, requestEntity, String.class);
            convertedFile.delete();
            if (response.getStatusCode() == HttpStatus.OK) {
                return ResponseEntity.ok("File uploaded successfully");
            } else {
                return ResponseEntity.badRequest().body("Fail to upload file. please provide right information");
            }
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getRawStatusCode()).headers(e.getResponseHeaders())
                    .body(e.getResponseBodyAsString());
        } catch (Exception e) {
            logger.error("Something went wrong during upload Headshot with -> " + e.getMessage());
            throw new HandleLegacyException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity getHeadshot(String webcastId, String status) {
        String accessToken = getLegacyAccessToken();
        HttpHeaders headers = accessHeaders(accessToken);
        HttpEntity<Object> requestEntity = new HttpEntity<>(headers);
        String getStreamInfoEndpoint = legacyBaseUrl + "/event/" + webcastId + "/slides?status=" + status + "&imageType=audioonly";
        ResponseEntity<SlideResponse[]> slideListResponse = null;
        try {
            slideListResponse = restTemplate.exchange(getStreamInfoEndpoint, HttpMethod.GET, requestEntity, SlideResponse[].class);
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new HandleLegacyException(e.getMessage(), HttpStatus.NOT_FOUND);
        }
        HeadshotResponseBody headshotResponseBody = new HeadshotResponseBody();
        headshotResponseBody.setPrimaryHeadshot(getSelectedHeadshot(webcastId,status));
        headshotResponseBody.setSlideResponseBody(Arrays.asList(slideListResponse.getBody()));
        return ResponseEntity.ok(headshotResponseBody);
    }

    public ResponseEntity deleteHeadshot(List<SlideData> slideData, String webcastId, String status) {
        String accessToken = getLegacyAccessToken();
        HttpHeaders headers = accessHeaders(accessToken);
        HttpEntity<Object> requestEntity = new HttpEntity<>(slideData, headers);
        String getSlideDeleteInfoEndpoint = legacyBaseUrl + "/event/" + webcastId + "/slides?status=" + status + "&imageType=audioonly";
        ResponseEntity response = null;
        try {
            response = restTemplate.exchange(getSlideDeleteInfoEndpoint, HttpMethod.DELETE, requestEntity, ResponseEntity.class);
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new HandleLegacyException(e.getMessage(), HttpStatus.NOT_FOUND);
        }
        return response;
    }

    public ResponseEntity updatePrimaryMarkForHeadShot(String webcastId , String status , String headshot){
        try {
            List<MarkResponse> primaryMarker = getPrimaryMarker(webcastId,status);
            List<MarkResponse> primaryMarkerUpdated = primaryMarker.stream().filter(mark -> {
                if(mark.frame.stream().filter(x->x.getType().equals("HeadshotMark")).map(y->y.refId = headshot).collect(Collectors.toList()).size()>0)
                    return true;
                return false;
            }).collect(Collectors.toList());

            ResponseEntity response = updatePrimaryMarker(primaryMarkerUpdated,webcastId,status);

            return response.getStatusCode() == HttpStatus.OK ? ResponseEntity.ok(response.getBody()) :
                    ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Fail to update primary marker ");
        }catch (Exception e){
            throw new HandleLegacyException(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    private List<MarkResponse> getPrimaryMarker(String webcastId , String status){
        String accessToken = getLegacyAccessToken();
        HttpHeaders headers = accessHeaders(accessToken);
        HttpEntity<Object> requestEntity = new HttpEntity<>(headers);
        String getPrimaryMarkerUrl = legacyBaseUrl + "/event/" + webcastId + "/marks?status=" + status;
        ResponseEntity<MarkResponse[]> markResponseList = null;
        try {
            markResponseList = restTemplate.exchange(getPrimaryMarkerUrl, HttpMethod.GET, requestEntity, MarkResponse[].class);
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new HandleLegacyException(e.getMessage(), HttpStatus.NOT_FOUND);
        }
      return Arrays.asList(markResponseList.getBody());
    }

    private ResponseEntity updatePrimaryMarker(List<MarkResponse> markResponseList , String webcastId , String status){
        String accessToken = getLegacyAccessToken();
        HttpHeaders headers = accessHeaders(accessToken);
        HttpEntity<Object> requestEntity = new HttpEntity<>(markResponseList,headers);
        String getPrimaryMarkerUrl = legacyBaseUrl + "/event/" + webcastId + "/marks/" + status;
        ResponseEntity<HashMap> markResponseUpdated = null;
        try {
            markResponseUpdated = restTemplate.exchange(getPrimaryMarkerUrl, HttpMethod.POST, requestEntity, HashMap.class);
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new HandleLegacyException(e.getMessage(), HttpStatus.NOT_FOUND);
        }
        return markResponseUpdated;
    }


    private ResponseEntity updatePrimaryMarkForSlide(String webcastTicket , String status , String firstSlide){
        try {
            String webcastId = webcastTicket.split("-")[2];
            List<MarkResponse> primaryMarker = getPrimaryMarker(webcastId,status);
            List<MarkResponse> primaryMarkerUpdated = primaryMarker.stream().filter(mark -> {
                if(mark.frame.stream().filter(x->x.getType().equals("SlideMark")).map(y->{
                    y.refId =firstSlide;
                    y.url = "/ststapi/event/"+webcastId+"/slideImages?imageType=slide&status="+status+"&ticket="+webcastTicket+"&image="+firstSlide+"&target=en&size=small";
                    return y;
                        }).collect(Collectors.toList()).size()>0)
                    return true;
                return false;
            }).collect(Collectors.toList());

            ResponseEntity response = updatePrimaryMarker(primaryMarkerUpdated,webcastId,status);

            return response.getStatusCode() == HttpStatus.OK ? ResponseEntity.ok(response.getBody()) :
                    ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Fail to update primary marker ");
        }catch (Exception e){
            throw new HandleLegacyException(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private FileConverterResponse getConverterServiceRes(MultiValueMap<String, Object> body){
        String accessToken = getLegacyAccessToken();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity<MultiValueMap<String, Object>> requestEntity
                = new HttpEntity<>(body, headers);
        String uploadUrl = slideConverter;
        try {
            logger.info("calling the service of converter url -----> "+uploadUrl);
            ResponseEntity<FileConverterResponse> response = restTemplate
                    .postForEntity(uploadUrl, requestEntity, FileConverterResponse.class);
            return  response.getBody();
        } catch (Exception e) {
            logger.error("File converter service error "+e.getMessage());
        }
        return null;
    }


    public void fixPreviewStreamUrl(String webcastId, Webcast webcast, String fileName) {
        List<StreamInfo> streamInfoList = this.getStreamInfo(webcastId, "preview");
        List<StreamInfo> updatedStreamList = streamInfoList.stream().map(x -> {
            x.setUrl((webcast.isEnableChinaCDN() ? "cdn-" + wowzaOndemandChinaUrl : wowzaOndemandUrl) + "/" + "trailer" + "/vod,easywebcast/" + webcastId + '/' + fileName);
            return x;
        }).collect(Collectors.toList());
        String accessToken = this.getLegacyAccessToken();
        String fixUrlApiEndpoint = legacyBaseUrl + "/streams/" + webcastId + "/status/preview";
        HttpHeaders headers = accessHeaders(accessToken);
        HttpEntity<Object> requestEntity = new HttpEntity<>(updatedStreamList, headers);
        restTemplate.exchange(fixUrlApiEndpoint, HttpMethod.PUT, requestEntity, Object.class, updatedStreamList);

    }

    public void changeStreamUrl(String url, List<StreamInfo> streamInfoList) {
        try {
            String accessToken = this.getLegacyAccessToken();
            String fixUrlApiEndpoint = legacyBaseUrl + url;
            HttpHeaders headers = accessHeaders(accessToken);
            HttpEntity<Object> requestEntity = new HttpEntity<>(streamInfoList, headers);
            restTemplate.exchange(fixUrlApiEndpoint, HttpMethod.PUT, requestEntity, Object.class);
        }catch (Exception e){

        }
    }

    private File convertMultiPartFileToFile(final MultipartFile multipartFile) {
        final File file = new File(multipartFile.getOriginalFilename());
        try (final FileOutputStream outputStream = new FileOutputStream(file)) {
            outputStream.write(multipartFile.getBytes());
        } catch (final IOException ex) {
            logger.warn("SOMETHING WENT WRONG DURING MULTIPART FILE TO FILE CONVERSION WITH -> " + ex.getMessage());
        }
        return file;
    }

    private String generateFileId(String fileName,String webcastId){
        Random random = new Random();
        String splitName[] = fileName.split("[.]");
        String extension = splitName[splitName.length-1];
        String generatedId = String.valueOf(Math.abs(random.nextLong()))+"headshot_"+webcastId+"."+extension;
        return  generatedId;
    }

    private String getSelectedHeadshot(String webcastId , String status){
        List<MarkResponse> primaryMarker = getPrimaryMarker(webcastId,status);
        for (MarkResponse markResponse : primaryMarker){
            for(Frame frame : markResponse.getFrame()){
                if(Objects.equals(frame.getType(),"HeadshotMark")){
                    return frame.getRefId();
                }
            }
        }
        return null;
    }

}
