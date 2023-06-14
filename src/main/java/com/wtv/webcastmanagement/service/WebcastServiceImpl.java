package com.wtv.webcastmanagement.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wtv.webcastmanagement.Exceptions.HandleLegacyException;
import com.wtv.webcastmanagement.Exceptions.HandleWebcastException;
import com.wtv.webcastmanagement.dto.dto.FileResponse.VideoResponse;
import com.wtv.webcastmanagement.dto.dto.KollectiveResponse;
import com.wtv.webcastmanagement.entity.Session;
import com.wtv.webcastmanagement.entity.Webcast;
import com.wtv.webcastmanagement.entity.WebcastPayload;
import com.wtv.webcastmanagement.entity.ecdn.Ecdn;
import com.wtv.webcastmanagement.entity.legacy.LegacyBroadcast;
import com.wtv.webcastmanagement.entity.legacy.SlideData;
import com.wtv.webcastmanagement.entity.legacy.StreamInfo;
import com.wtv.webcastmanagement.entity.zoom.Meeting;
import com.wtv.webcastmanagement.entity.zoom.Settings;
import com.wtv.webcastmanagement.repository.EcdnRepository;
import com.wtv.webcastmanagement.repository.MeetingRepository;
import com.wtv.webcastmanagement.repository.WebcastRepository;
import com.wtv.webcastmanagement.utils.EnvironmentProperties;
import com.wtv.webcastmanagement.utils.Kollective;
import com.wtv.webcastmanagement.utils.StaticValues;
import com.wtv.webcastmanagement.utils.UpdateWebcastUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.util.Pair;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WebcastServiceImpl implements WebcastService{
    private final LegacyService legacyService;
    private final WebcastRepository webcastRepository;
    private final ZoomService zoomService;
    private final MeetingRepository meetingRepository;
    private final UpdateWebcastUtil updateWebcastUtil;
    private final EcdnRepository ecdnRepository;
    private final RestTemplate restTemplate;
    private final EnvironmentProperties environmentProperties;

    @Value("${SLIDE_CONVERTER}")
    private String slideConverter;

    @Value("${VIDEO_CONVERSION_SERVICE}")
    private String videoConverterUrl;

    @Value("${CONNECTSTUDIO_SESSION_SERVICE}")
    private String connectstudioSessionUrl;

    @Value("${TRAILER_DIR}")
    String trailer_dir;
    private final Kollective kollective;

    Logger logger = LoggerFactory.getLogger(WebcastServiceImpl.class);

    @Override
    public String cloneWebcast(Session session) {
        String webcastId = null;
        Meeting meeting = null;
        String registrationRequired = "no";
        if(session.getMeeting() == null) throw new HandleWebcastException("Please provide meeting type" , HttpStatus.BAD_REQUEST);
        try {
            Pair<String , Boolean> cloneResponse = legacyService.cloneWebcast(session);
            webcastId = cloneResponse.getFirst();
            registrationRequired = cloneResponse.getSecond() == true ? "yes" : "no";
           if(webcastId == null) throw new HandleLegacyException("Fail to create webcast ",HttpStatus.INTERNAL_SERVER_ERROR);
           session.setWebcastId(webcastId);
           if(session.getMeeting() != null){
               if(session.getMeeting().equals("zoom")) {
                   meeting = zoomService.createMeeting(session , false);
                   if (meeting == null)
                       throw new HandleWebcastException("Fail to create meeting link at zoom", HttpStatus.INTERNAL_SERVER_ERROR);
               }
           }
        }catch (HandleLegacyException e){
            throw new HandleLegacyException(e.getMessage(), e.getStatus());
        }
        //Saving data to easy-webcast database
        Webcast webcast = new Webcast();
        webcast.setWebcastId(Integer.valueOf(webcastId));
        webcast.setWebcastType("videoAudioSlide");
        webcast.setInvitedGuest(1000);
        webcast.setRegRequired(registrationRequired);
        if(session.getMeeting() != null){
            webcast.setType(session.getMeeting());
        }else {
            webcast.setType("self");
        }
        webcast.setCreateAt(LocalDateTime.now());
        webcast.setUpdatedAt(session.getUpdatedDate());
        webcast.setECDNActive(false);
        webcast.setEnableChinaCDN(session.getEnableChinaCDN().equals("yes")?true:false);
        webcast.setDuration(session.getDuration());
        webcast.setTimeZone(session.getTimeZone());
        if(session.getBitRate() != null){
            webcast.setBitRate(session.getBitRate());
        }else {
            webcast.setBitRate("1000,720");
        }
        try {
            Webcast webcastResponse = webcastRepository.save(webcast);
            if(webcastResponse == null)
                throw new Exception("Didn't save in easy-webcast");
            if(session.getMeeting() != null){
                if(session.getMeeting().equals(StaticValues.zoom) && meeting != null){
                    meeting.setWebcastId(webcastId);
                    meeting.setCreatedAt(LocalDateTime.now());
                    meetingRepository.save(meeting);
                }
            }
        }catch (Exception e){
            logger.error("Fail to save data in Easy-webcast database with ->"+e.getMessage());
        }
        return webcastId;
    }

    @Override
    public void updateWebcast(Session session,String webcastId) {
        Map<String,Object> legacyPayload = new HashMap<>();
        if( (session.getLegacyStatus() == null)  || (session.getAdditionalLanguages() == null) ){
            throw new HandleWebcastException("Please provide the necessary information for update webcast",HttpStatus.BAD_REQUEST);
        }
        legacyPayload.put(StaticValues.additionalLanguages,session.getAdditionalLanguages());
        if(session.getDefaultLanguage()==null){
            legacyPayload.put(StaticValues.defaultLanguage,"en");
        }else {
            legacyPayload.put(StaticValues.defaultLanguage,session.getDefaultLanguage());
        }
        legacyPayload.put(StaticValues.status,session.getLegacyStatus());
        if(session.getScheduleLiveStartDate() != null){
            if(session.getOndemandOverDate() == null)
                throw new HandleWebcastException("Please provide necessary information missing ondemandOverDate",HttpStatus.BAD_REQUEST);
            legacyPayload.put(StaticValues.scheduleLiveStartDate,session.getScheduleLiveStartDate());
            legacyPayload.put(StaticValues.ondemandOverDate,session.getOndemandOverDate());
        }
        List<String> features = new ArrayList<>();
        features.add("f1");
        features.add("f20");
        features.add("f40");
        features.add("f80");
        legacyPayload.put(StaticValues.features, features);
        if(session.getTitle() != null){
            legacyPayload.put(StaticValues.name,session.getTitle());
        }
        if(session.getBitRate() == null || session.getEnableChinaCDN() == null){
            throw new HandleWebcastException("Invalid Information please provide BitRate and ChinaDelivery",HttpStatus.BAD_REQUEST);
        }
        legacyPayload.put(StaticValues.bitRate,session.getBitRate());
        legacyPayload.put("enableChinaCDN",session.getEnableChinaCDN());
        try {
            legacyService.updateEvent(webcastId,legacyPayload);
        }catch (HandleLegacyException e){
            throw new HandleLegacyException(e.getMessage(),HttpStatus.BAD_REQUEST);
        }catch (Exception e){
            logger.error(e.getMessage());
            throw new HandleLegacyException(e.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @Override
    public List<StreamInfo> getStreamInfo(String webcastId , String status) {
        return legacyService.getStreamInfo(webcastId , status);
    }

    @Override
    public ResponseEntity uploadSlide(MultipartFile file , String fileName , String status ,
                                      String aspectRatio , String prefix , String language , String webcastId) {

        return this.legacyService.uploadSlideUpdated(file , fileName , status , aspectRatio , prefix , language , webcastId);
    }

    @Override
    public ResponseEntity uploadTrailer(String webcastId, MultipartFile file) {

        try {
            LegacyBroadcast broadcast = legacyService.getLegacyEvent(webcastId);
            if(broadcast == null) throw new HandleWebcastException("Fail to found the legacy broadcast" , HttpStatus.NOT_FOUND);
            Webcast webcast = webcastRepository.findByWebcastId(Integer.valueOf(webcastId));
            if(webcast == null) throw new HandleWebcastException("Fail to found the webcast" , HttpStatus.NOT_FOUND);
            if(Objects.equals(webcast.getWebcastType(),StaticValues.audioSlide) ||Objects.equals(webcast.getWebcastType(),StaticValues.audio))
                throw new HandleWebcastException("Sorry this is audio webcast",HttpStatus.BAD_REQUEST);

            List<String> additionalLanguage = broadcast.getAdditionalLanguages();
            additionalLanguage.add(broadcast.getDefaultLanguage());
            boolean cdnStatus = webcast.isEnableChinaCDN();

            //UPLOAD VIDEO TO FILER
            VideoResponse videoResponse = videoConverterService(webcastId , file);
            if(videoResponse == null) throw new HandleWebcastException("Fail to upload video through video converter" , HttpStatus.INTERNAL_SERVER_ERROR);
            String videoFileName = videoResponse.getPayload().getData().getFilename();
            String fileNameWithPath = "easywebcast/"+webcastId +"/"+videoFileName;

            List<StreamInfo> streamInfoList = legacyService.getStreamInfo(webcastId, "preview");
            List<StreamInfo> updatedStreamList = streamInfoList.stream().map(x -> {
                String hostUrl = (cdnStatus ? environmentProperties.NEXT_PUBLIC_MAIN_WOWZA_ONDEMAND_CHINA_URL : environmentProperties.NEXT_PUBLIC_MAIN_WOWZA_ONDEMAND_URL) +"/trailer/vod,"+fileNameWithPath;
                x.setUrl(hostUrl);
                return x;
            }).collect(Collectors.toList());

            if(webcast.isECDNActive() && Objects.equals(webcast.getP2pVendor(),StaticValues.kollective)){
                Optional<Ecdn> ecdn = ecdnRepository.findEcdnByWebcastIdOrCsoOrCo(broadcast.getCo(), broadcast.getCso(), broadcast.getId());
                if(ecdn.isPresent()){
                    if(ecdn.get().getVendor() == null || ecdn.get().getVendor().get(0).getSecrets().size()<2)
                        throw new HandleWebcastException("Fail to get vendor or secret ecdn",HttpStatus.NOT_FOUND);
                    List<StreamInfo> updatedStreamWithEcdn = updatedStreamList.stream().map(stream ->{
                        KollectiveResponse kollectiveStream = kollective.generateKollectiveTokenforOnDemand(
                                broadcast.getId().toString(),
                                broadcast.getName(),
                                ecdn.get().getVendor().get(0).getSecrets().get(0).getValue(),
                                ecdn.get().getVendor().get(0).getSecrets().get(1).getValue(),
                                stream.getUrl()
                        );
                        stream.setUrl("_kollective_,"+kollectiveStream.getToken());
                        return stream;
                    }).collect(Collectors.toList());
                    String url = "/streams/"+webcast.getWebcastId()+"/status/preview";
                    legacyService.changeStreamUrl(url , updatedStreamWithEcdn);
                }else {
                    throw new HandleWebcastException("fail to found ecdn vendor",HttpStatus.NOT_FOUND);
                }
            }else {
                String url = "/streams/"+webcast.getWebcastId()+"/status/preview";
                legacyService.changeStreamUrl(url , updatedStreamList);
            }
        }catch (Exception e){
           throw new HandleWebcastException("Fail to upload video with root cause -> "+e.getMessage() , HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok("Trailer uploaded Successfully");
    }

    @Override
    public ResponseEntity getSlides(String webcastId, String status) {
        return legacyService.getSlides(webcastId , status);
    }

    @Override
    public ResponseEntity deleteSlides(List<SlideData> slideData, String webcastId, String status) {
        return legacyService.deleteSlides(slideData , webcastId , status);
    }

    @Override
    public ResponseEntity changeWebcastType() {
        return legacyService.changeWebcastType();
    }

    @Override
    public void updateWebcastUtil(String webcastPayloadString ,MultipartFile file, String id) {
        try {
            WebcastPayload webcastPayload = new ObjectMapper().readValue(webcastPayloadString , WebcastPayload.class);
            webcastPayload.setWebcastId(Integer.valueOf(id));
            updateWebcastUtil.updateWebcast(webcastPayload , file);
        }catch (Exception e){
            throw new HandleWebcastException(e.getMessage() , HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public ResponseEntity getMeetingInfo(String webcastId) {
        Meeting meeting = meetingRepository.findMeetingByWebcastId(webcastId);
        return meeting!=null? ResponseEntity.ok(meeting):ResponseEntity.status(HttpStatus.NOT_FOUND).body("Sorry didn't found the meeting using webcastId :"+webcastId);
    }

    @Override
    public ResponseEntity getZoomInviteInfo(String webcastId) {
        return zoomService.getZoomInviteInfo(webcastId);
    }
    @Override
    public ResponseEntity editMeeting(Meeting meeting , String webcastId) {
        try {
            Meeting meetingToBeUpdatd = meetingRepository.findMeetingByWebcastId(webcastId);
            if(meeting.getStart_time() == null || meeting.getDuration() == null || meeting.getSettings().getAuto_recording()==null)
                throw new HandleWebcastException("please provide missing fields" , HttpStatus.BAD_REQUEST);
            String url = "/meetings/"+meetingToBeUpdatd.getId();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            Date dateTime = simpleDateFormat.parse(meeting.getStart_time());
            String startTime = simpleDateFormat.format(dateTime);
            Integer duration = meeting.getDuration();
            String autoRecord = meeting.getSettings().getAuto_recording();
            HashMap<String , Object> params = new HashMap<>();
            HashMap<String , String> settingsMap = new HashMap<>();
            settingsMap.put("auto_recording",autoRecord);
            params.put("start_time" , startTime);
            params.put("duration" , duration);
            params.put("settings",settingsMap);
            ResponseEntity editMeetingRes = zoomService.patchRequestEditZoomTime(url , params);

            if(editMeetingRes.getStatusCode() == HttpStatus.OK){
                meetingToBeUpdatd.setStart_time(startTime);
                meetingToBeUpdatd.setDuration(duration);
                Settings settings = meetingToBeUpdatd.getSettings();
                settings.setAuto_recording(autoRecord);
                meetingToBeUpdatd.setSettings(settings);
                meetingToBeUpdatd.setUpdatedAt(LocalDateTime.now());
                Meeting meetingRes = meetingRepository.save(meetingToBeUpdatd);
                return ResponseEntity.ok(meetingRes);
            }
            else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("fail to update meeting");
            }
        }catch (Exception e){
               logger.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @Override
    public ResponseEntity getUpdateWebcastData(String webcastId) {
        try {
            LegacyBroadcast broadcast = legacyService.getLegacyEvent(webcastId);
            if(broadcast == null) throw new Exception("Fail to found the legacy broadcast");
            Webcast webcast = webcastRepository.findByWebcastId(Integer.valueOf(webcastId));
            if(webcast == null){
                logger.info("Updating the easywebcast database-->");
                webcast = updateEasywebcastDatabase(webcastId);
            }
            if(webcast == null) throw new Exception("Fail to found data in easywebcast database");
            WebcastPayload webcastPayload = new WebcastPayload();
            if(webcast.getTimeZone() != null)
            webcastPayload.setTimeZone(webcast.getTimeZone());
            webcastPayload.setWebcastId(Integer.valueOf(webcastId));
            if(webcast.getWebcastType() != null)
            webcastPayload.setWebcastType(webcast.getWebcastType());
            webcastPayload.setInvitedGuest(webcast.getInvitedGuest());
            webcastPayload.setRegRequired(webcast.getRegRequired());
            webcastPayload.setType(webcast.getType());
            webcastPayload.setDuration(webcast.getDuration());
            webcastPayload.setBitRate(webcast.getBitRate());
            webcastPayload.setEnableChinaCDN(webcast.isEnableChinaCDN());
            webcastPayload.setCurrentChinaCdnStatus(webcast.isEnableChinaCDN());
            webcastPayload.setEnableECDNActive(webcast.isECDNActive());
            webcastPayload.setCurrentECDNStatus(webcast.isECDNActive());
            webcastPayload.setChangeWebcastType(false);
            webcastPayload.setP2pVendor(webcast.getP2pVendor());
            webcastPayload.setTitle(broadcast.getName());
            webcastPayload.setCso(broadcast.getCso());
            webcastPayload.setCo(broadcast.getCo());
            webcastPayload.setScheduleLiveStartDate(broadcast.getScheduleLiveStartDate());
            webcastPayload.setOndemandOverDate(broadcast.getOndemandOverDate());
            webcastPayload.setDefaultLanguage(broadcast.getDefaultLanguage());
            webcastPayload.setCurrentDefaultLanguage(broadcast.getDefaultLanguage());
            webcastPayload.setAdditionalLanguage(broadcast.getAdditionalLanguages());
            webcastPayload.setCurrentAdditionalLanguage(broadcast.getAdditionalLanguages());
            webcastPayload.setFeatures(broadcast.getFeatures());
            webcastPayload.setStatus(broadcast.getStatus());
            webcastPayload.setMeeting(webcast.getType());
            return ResponseEntity.ok(webcastPayload);
        }catch (Exception e){
            logger.error("Fail to load data using webcast id "+webcastId + " With -> " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Something went wrong, Fail to load data with cause -> " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity getEcdnVendorInfo(String co, String cso, String webcastId) {
        try {
            Ecdn ecdnValue = this.findingEcdnValues(Integer.valueOf(co),Integer.valueOf(cso),Integer.valueOf(webcastId));
            if(ecdnValue != null && !ecdnValue.getVendor().isEmpty()){
                HashSet<String> vendorNameList = new HashSet<>();
                ecdnValue.getVendor().stream().forEach(vendor->{
                    vendorNameList.add(vendor.getName());
                });
                if(!vendorNameList.isEmpty()){
                    return ResponseEntity.ok(vendorNameList);
                }
            }
        }catch (Exception e){
            logger.error(e.getMessage());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found the ecdn with those co , cso , webcastId");
    }

    @Override
    public ResponseEntity uploadHeadshot(MultipartFile file, String fileName, String status, String aspectRatio, String prefix, String language, String imageType, String webcastId) {
        return this.legacyService.uploadHeadshot(file , fileName , status , aspectRatio , prefix , language ,imageType, webcastId);
    }

    @Override
    public ResponseEntity getHeadShot(String webcastId, String status) {
        return legacyService.getHeadshot(webcastId,status);
    }

    @Override
    public ResponseEntity deleteHeadshot(List<SlideData> slideData, String webcastId, String status) {
        return legacyService.deleteHeadshot(slideData , webcastId , status);
    }

    @Override
    public ResponseEntity updatePrimaryMarkHeadshot(String webcastId, String status, String headshot) {
        return  legacyService.updatePrimaryMarkForHeadShot(webcastId,status,headshot);
    }


    private VideoResponse videoConverterService(String webcastId , MultipartFile file){
        try {
            String filename = webcastId + "_" + file.getOriginalFilename().split("[.]")[0];
            String videoCnvUrl = videoConverterUrl + "/compress";
            MultiValueMap<String,Object> body = new LinkedMultiValueMap<>();
            body.add("encodeWaiting",false);
            body.add("mountPath" , trailer_dir+"/easywebcast/"+webcastId);
            body.add("name" , filename);
            body.add("outputFormat" , "mp4");
            body.add("ffmpegStr" , "-filter:v fps=25 -vf scale=1280:720 -b:v 880k -b:a 128k -c:v h264 -c:a aac -ac 2 -ar 44100");
            body.add("file" , file.getResource());
            HttpHeaders tokenHeader = new HttpHeaders();
            tokenHeader.setContentType(MediaType.MULTIPART_FORM_DATA);
            HttpEntity<MultiValueMap<String,Object>> requestMap = new HttpEntity<>(body,tokenHeader);
            logger.info("calling the service of video converter url -----> "+videoCnvUrl);
            ResponseEntity<VideoResponse> response = new RestTemplate().exchange(videoCnvUrl, HttpMethod.POST,requestMap, VideoResponse.class);
            return response.getBody();
        }catch (Exception e){
            logger.error(e.getMessage());
            return null;
        }
    }

    public ResponseEntity fileConverterPPTX(MultipartFile file){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> body
                = new LinkedMultiValueMap<>();
        File convertedFile = convertMultiPartFileToFile(file);
        body.add("file", new FileSystemResource(convertedFile));
        HttpEntity<MultiValueMap<String, Object>> requestEntity
                = new HttpEntity<>(body, headers);
        String uploadUrl = slideConverter;
        uploadUrl = uploadUrl.replace("/convertToJpg","/ppt-to-pdf");
        try {
            ResponseEntity response = restTemplate
                    .postForEntity(uploadUrl, requestEntity , ByteArrayResource.class);
            convertedFile.delete();
            return ResponseEntity.ok().contentType(response.getHeaders().getContentType()).contentLength(response.getHeaders().getContentLength()).body(response.getBody());
        } catch (Exception e) {
            logger.error("File converter service error "+e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    private Webcast updateEasywebcastDatabase(String webcastId){
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            String getUrlSession = connectstudioSessionUrl + "/webcast-id/"+webcastId;
            HttpEntity<String> requestEntity
                    = new HttpEntity<>(headers);
            ResponseEntity<Session> response = restTemplate
                    .getForEntity(getUrlSession,Session.class,requestEntity);
            if(response.getStatusCode() == HttpStatus.OK && response.getBody() != null){
                Session session = response.getBody();
                Webcast webcast = new Webcast();
                webcast.setWebcastId(Integer.valueOf(webcastId));
                webcast.setType("self");
                webcast.setWebcastType("videoAudioSlide");
                webcast.setBitRate("1000,720");
                webcast.setRegRequired("no");
                webcast.setEnableChinaCDN(Boolean.valueOf(session.getEnableChinaCDN()));
                webcast.setTimeZone(session.getTimeZone()!=null?session.getTimeZone():"");
                webcast.setInvitedGuest(1000);
                webcast.setECDNActive(false);
                webcast.setDuration(session.getDuration()!=null?session.getDuration():"0");
                webcast.setCreateAt(LocalDateTime.now());
                Webcast webcastResponse = webcastRepository.save(webcast);
                return webcastResponse;
            }
        }catch (Exception e){
         logger.error("Fail to update easywebcast data for webcastId "+webcastId + "with root cause--> "+e.getMessage());
        }
        return null;
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

    private Ecdn findingEcdnValues(Integer co , Integer cso , Integer webcast){
            Ecdn ecdn = null;
            ecdn = ecdnRepository.findByEntityId(co);
            if(ecdn !=null){
                return ecdn;
            }
            ecdn = ecdnRepository.findByEntityId(cso);
            if(ecdn != null){
                return  ecdn;
            }
            ecdn = ecdnRepository.findByEntityId(webcast);
            if(ecdn != null){
                return ecdn;
            }
            return ecdn;
    }


}
