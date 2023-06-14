package com.wtv.webcastmanagement.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wtv.webcastmanagement.Exceptions.HandleWebcastException;
import com.wtv.webcastmanagement.entity.Session;
import com.wtv.webcastmanagement.entity.zoom.Meeting;
import com.wtv.webcastmanagement.entity.zoom.Settings;
import com.wtv.webcastmanagement.entity.zoom.ZoomConfig;
import com.wtv.webcastmanagement.entity.zoom.ZoomInviteInfo;
import com.wtv.webcastmanagement.repository.MeetingRepository;
import com.wtv.webcastmanagement.utils.StaticValues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class ZoomService {

    @Value("${zoomApiUrl}")
    String zoomBaseUrl;

    @Value("${zoomToken}")
    String apiToken;
    @Autowired
    private MeetingRepository meetingRepository;

    HttpClient httpClient = HttpClient.newBuilder().build();

    Logger logger = LoggerFactory.getLogger(ZoomService.class);
    ObjectMapper objectMapper = new ObjectMapper();

    public Meeting createMeeting(Session session , boolean edit) {
        List<String> zoomAccounts = ZoomConfig.testAccounts;
        List<Meeting> availableMettings = getMeetings(zoomAccounts);
        List<String> availableHost = checkAvailableHost(availableMettings, zoomAccounts, session.getScheduleLiveStartDate(), session.getDuration());
        boolean occurrence = edit == false ? checkOccurrence(availableMettings, session.getWebcastId()) : false;
        if (occurrence) {
            throw new HandleWebcastException("A meeting with the same webcast id already there", HttpStatus.BAD_REQUEST);
        }
        if (availableHost.size() == 0) {
            throw new HandleWebcastException("Sorry ,In this time period we can't schedule the meeting", HttpStatus.BAD_REQUEST);
        }
        if(edit){
            deleteMeeting(session.getMeetingId());
        }
        Meeting meeting = null;
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            Date dateTime = simpleDateFormat.parse(session.getScheduleLiveStartDate());
            String startTime = simpleDateFormat.format(dateTime);
            Meeting requestBody = new Meeting();
            requestBody.setDuration(Integer.valueOf(session.getDuration()));
            requestBody.setStart_time(startTime);
            requestBody.setType(2);
            Settings settings = new Settings();
            settings.setAuto_recording("cloud");
            settings.setJoin_before_host(true);
            requestBody.setSettings(settings);
            requestBody.setTopic(session.getWebcastId());

            URI uri = URI.create(zoomBaseUrl + "/users/" + availableHost.get(0) + "/meetings");
            HttpRequest httpRequest =
                    HttpRequest.newBuilder()
                            .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody)))
                            .setHeader(StaticValues.Authorization, StaticValues.Bearer + apiToken)
                            .setHeader(StaticValues.content_Type, StaticValues.application_Json)
                            .uri(uri)
                            .build();
            HttpResponse response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            meeting = objectMapper.readValue(response.body().toString(), Meeting.class);

        }catch (ParseException parseException){
            logger.error(parseException.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return meeting;
    }

    public List<Meeting> getMeetings(List<String> accounts) {
        List<Meeting> meetingList = new ArrayList<>();
        for (String account : accounts) {
            URI uri = URI.create(zoomBaseUrl + "/users/" + account + "/meetings");
            HttpRequest httpRequest =
                    HttpRequest.newBuilder()
                            .GET()
                            .setHeader(StaticValues.Authorization, StaticValues.Bearer + apiToken)
                            .setHeader(StaticValues.content_Type, StaticValues.application_Json)
                            .uri(uri)
                            .build();

            try {
                HttpResponse response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
                JsonNode responseNode = objectMapper.readValue(response.body().toString(), JsonNode.class);
                List<Meeting> meetingResponse = objectMapper.readValue(responseNode.get("meetings").toString(), new TypeReference<List<Meeting>>() {
                });
                meetingResponse.forEach(meeting -> {
                    meetingList.add(meeting);
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return meetingList;
    }
    public Meeting editMeeting(Meeting meeting){
              Session session = new Session();
              session.setDuration(String.valueOf(meeting.getDuration()));
              session.setScheduleLiveStartDate(meeting.getStart_time());
              session.setRecording(meeting.getSettings().getAuto_recording());
              session.setWebcastId(meeting.getWebcastId());
              session.setTimeZone(meeting.getTimezone());
              session.setMeetingId(meeting.getId().toString());
              Meeting mettingRes = createMeeting(session , true);
              return mettingRes ;
    }

    public ResponseEntity deleteMeeting(String meetingId){
        URI uri = URI.create(zoomBaseUrl + "/meetings/" + meetingId);
        HttpRequest httpRequest =
                HttpRequest.newBuilder()
                        .DELETE()
                        .setHeader(StaticValues.Authorization, StaticValues.Bearer + apiToken)
                        .setHeader(StaticValues.content_Type, StaticValues.application_Json)
                        .uri(uri)
                        .build();
        try {
            HttpResponse response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if(response.statusCode() == 204){
                return ResponseEntity.ok("deleted");
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("delete the meeting");
    }


    public boolean checkOccurrence(List<Meeting> meetingList, String meetingTopic) {
        for (Meeting meeting : meetingList) {
            if (meeting.getTopic().equals(meetingTopic)) {
                return true;
            }
        }
        return false;
    }

    public List<String> checkAvailableHost(List<Meeting> meetingList, List<String> hosts, String requestedTime, String duration) {

        LocalDateTime startTimeRequested = LocalDateTime.ofInstant(Instant.parse(requestedTime), ZoneOffset.UTC);
        LocalDateTime finishTimeRequested = startTimeRequested.plusMinutes(Integer.valueOf(duration));

        for (Meeting meeting : meetingList) {
            LocalDateTime meetingStart = LocalDateTime.ofInstant(Instant.parse(meeting.getStart_time()), ZoneOffset.UTC);
            LocalDateTime meetingFinish = meetingStart.plusMinutes(meeting.getDuration());

            if ((startTimeRequested.compareTo(meetingStart) >= 0 && startTimeRequested.compareTo(meetingFinish) <= 0) ||
                    (startTimeRequested.compareTo(meetingStart) <= 0 && finishTimeRequested.compareTo(meetingStart) >= 0)) {
                Integer index = hosts.indexOf(meeting.getHost_id());
                hosts.remove(index);
            }
        }
        return hosts;
    }

    public void patchRequestZoom(String url, HashMap<String, String> params) {
        LocalDateTime start = LocalDateTime.now();
        try {
            URI uri = URI.create(zoomBaseUrl + url);
            HttpRequest httpRequest =
                    HttpRequest.newBuilder()
                            .method("PATCH", HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(params)))
                            .setHeader(StaticValues.Authorization, StaticValues.Bearer + apiToken)
                            .setHeader(StaticValues.content_Type, StaticValues.application_Json)
                            .uri(uri)
                            .build();
            HttpResponse response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            //HashMap<String, Object> responseBody = objectMapper.readValue(response.body().toString(), HashMap.class);
            logger.info("zoom patchRequestZoom " + response.statusCode());
            LocalDateTime stop = LocalDateTime.now();
            Long elapsed = stop.until(start, ChronoUnit.HOURS);
        } catch (Exception e) {
            logger.error("error catchblock zoomapiwrapper", e);
            LocalDateTime stop = LocalDateTime.now();
            Long elapsed = stop.until(start, ChronoUnit.HOURS);
        }
    }

    public ResponseEntity patchRequestEditZoomTime(String url, HashMap<String, Object> params) {
        String startTime = params.get("start_time").toString();
        String duration = params.get("duration").toString();
        List<String> zoomAccounts = ZoomConfig.testAccounts;
        List<Meeting> availableMettings = getMeetings(zoomAccounts);
        List<String> availableHost = checkAvailableHost(availableMettings, zoomAccounts, startTime, duration);
        if (availableHost.size() == 0) {
            throw new HandleWebcastException("Sorry ,In this time period we can't schedule the meeting", HttpStatus.BAD_REQUEST);
        }
        try {
            URI uri = URI.create(zoomBaseUrl + url);
            HttpRequest httpRequest =
                    HttpRequest.newBuilder()
                            .method("PATCH", HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(params)))
                            .setHeader(StaticValues.Authorization, StaticValues.Bearer + apiToken)
                            .setHeader(StaticValues.content_Type, StaticValues.application_Json)
                            .uri(uri)
                            .build();
            HttpResponse response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            //HashMap<String, Object> responseBody = objectMapper.readValue(response.body().toString(), HashMap.class);
            logger.info("zoom patchRequestZoom " + response.statusCode());
            if(response.statusCode() == 204){
                return ResponseEntity.ok("updated");
            }else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
            }
        } catch (Exception e) {
            logger.error("error catchblock zoom service", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
        }
    }

    public ResponseEntity getZoomInviteInfo(String webcastId) {
        Meeting meeting = meetingRepository.findMeetingByWebcastId(webcastId);
        ZoomInviteInfo zoomInviteInfo = null;
        if (meeting != null) {
            URI uri = URI.create(zoomBaseUrl + "/meetings/" + meeting.getId() + "/invitation");
            HttpRequest httpRequest =
                    HttpRequest.newBuilder()
                            .GET()
                            .setHeader(StaticValues.Authorization, StaticValues.Bearer + apiToken)
                            .setHeader(StaticValues.content_Type, StaticValues.application_Json)
                            .uri(uri)
                            .build();
            try {
                HttpResponse response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
                zoomInviteInfo = objectMapper.readValue(response.body().toString(), ZoomInviteInfo.class);
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }
        return zoomInviteInfo != null ? ResponseEntity.ok(zoomInviteInfo) :
                ResponseEntity.status(HttpStatus.NOT_FOUND).body("Sorry didn't found the meeting for this webcast " + webcastId);
    }

}
