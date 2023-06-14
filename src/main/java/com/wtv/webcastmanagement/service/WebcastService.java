package com.wtv.webcastmanagement.service;

import com.wtv.webcastmanagement.entity.Session;
import com.wtv.webcastmanagement.entity.legacy.SlideData;
import com.wtv.webcastmanagement.entity.legacy.StreamInfo;
import com.wtv.webcastmanagement.entity.zoom.Meeting;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public interface WebcastService {
    String cloneWebcast(Session session);

    void updateWebcast(Session session , String webcastId);

    List<StreamInfo> getStreamInfo(String webcastId , String status);

    ResponseEntity uploadSlide(MultipartFile file ,String fileName , String status , String aspectRatio ,String prefix , String language, String webcastId);

    ResponseEntity uploadTrailer(String webcastId , MultipartFile file);

    ResponseEntity getSlides(String webcastId, String status);

    ResponseEntity deleteSlides(List<SlideData> slideData, String webcastId, String status);

    ResponseEntity changeWebcastType();

    void updateWebcastUtil(String webcastPayloadString ,MultipartFile file, String id);

    ResponseEntity getMeetingInfo(String webcastId);

    ResponseEntity getZoomInviteInfo(String webcastId);

    public ResponseEntity editMeeting(Meeting meeting , String webcastId);

    ResponseEntity getUpdateWebcastData(String webcastId);

    ResponseEntity getEcdnVendorInfo(String co , String cso , String webcastId);

    ResponseEntity uploadHeadshot(MultipartFile file ,String fileName , String status , String aspectRatio ,String prefix , String language,String imageType, String webcastId);

    ResponseEntity getHeadShot(String webcastId, String status);

    ResponseEntity deleteHeadshot(List<SlideData> slideData, String webcastId, String status);

    ResponseEntity updatePrimaryMarkHeadshot(String webcastId , String status , String headshot);

    ResponseEntity fileConverterPPTX(MultipartFile file);

}
