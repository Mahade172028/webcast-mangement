package com.wtv.webcastmanagement.controller;

import com.wtv.webcastmanagement.entity.Session;
import com.wtv.webcastmanagement.entity.legacy.SlideData;
import com.wtv.webcastmanagement.entity.legacy.StreamInfo;
import com.wtv.webcastmanagement.entity.zoom.Meeting;
import com.wtv.webcastmanagement.service.WebcastService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class WebcastController {

    @Autowired
    private WebcastService webcastService;

    @GetMapping("/ping")
    public String ping(){
        return "pong";
    }

    @PostMapping("/webcast/clone")
    public ResponseEntity cloneWebcast(@RequestBody Session session){
        String cratedWebcastId = webcastService.cloneWebcast(session);
        return new ResponseEntity(cratedWebcastId, HttpStatus.OK);
    }

    @PutMapping("/webcast/{id}")
    public ResponseEntity updateWebcast(@RequestBody Session session,@PathVariable("id") String webcastId){
        webcastService.updateWebcast(session,webcastId);
        return new ResponseEntity("Webcast has been updated ", HttpStatus.OK);
    }
    @GetMapping("/webcast/streams/{id}")
    public ResponseEntity getStreamInfo(@PathVariable("id") String webcastId , @RequestParam("status") String status){
        List<StreamInfo> streamInfoList = webcastService.getStreamInfo(webcastId , status);
        return new ResponseEntity(streamInfoList , HttpStatus.OK);
    }

    @PostMapping("/webcast/{id}/uploadslide")
    public  ResponseEntity uploadSlide(@RequestParam("file")MultipartFile file ,
                                       @RequestParam(value = "fileName" , required = false) String fileName ,
                                       @RequestParam("status") String status,
                                       @RequestParam(value = "aspectRatio",required = false , defaultValue = "16:3") String aspectRatio,
                                       @RequestParam(value = "prefix")String prefix,
                                       @RequestParam("language") String language,
                                       @PathVariable("id") String webcastId
                                       ){
        return this.webcastService.uploadSlide(file , fileName , status , aspectRatio , prefix , language , webcastId);
    }
    @PostMapping("/webcast/upload-trailer/{webcastId}")
    public  ResponseEntity uploadTrailer(@PathVariable("webcastId") String webcastId,@RequestParam("file") MultipartFile multipartFile){
        return this.webcastService.uploadTrailer(webcastId , multipartFile);
    }

    @GetMapping("/webcast/{webcastId}/slides")
    public ResponseEntity getSlides(@PathVariable("webcastId") String webcastId , @RequestParam("status") String status){
        return webcastService.getSlides(webcastId , status);
    }

    @DeleteMapping("/webcast/{webcastId}/slides")
    public ResponseEntity deleteSlides(@PathVariable("webcastId") String webcastId , @RequestBody List<SlideData> slideData, @RequestParam("status") String status){
        return webcastService.deleteSlides(slideData ,webcastId , status);
    }

    @GetMapping("/webcast/systemText")
    public ResponseEntity getSystemText(){
        return webcastService.changeWebcastType();
    }

    @PutMapping("/webcast/update-webcast/{id}")
    public void  updateWebcastUtil(@RequestParam("data") String webcastPayloadString ,@RequestParam(value = "file" , required = false) MultipartFile multipartFile ,  @PathVariable("id") String id ){
        webcastService.updateWebcastUtil(webcastPayloadString , multipartFile ,id);
    }
    @GetMapping("/webcast/get-meetingInfo/{webcastId}")
    public ResponseEntity getMettingInfo(@PathVariable("webcastId") String webcastId){
      return webcastService.getMeetingInfo(webcastId);
    }
    @GetMapping("/webcast/get-zoomInviteInfo/{webcastId}")
    public ResponseEntity getZoomInviteInfo(@PathVariable("webcastId") String webcastId){
        return webcastService.getZoomInviteInfo(webcastId);
    }
    @PutMapping("/webcast/editZoomMeeting/{webcastId}")
    public ResponseEntity editZoomMeeting(@RequestBody Meeting meeting , @PathVariable("webcastId") String webcastId ){
        return webcastService.editMeeting(meeting , webcastId);
    }
    @GetMapping("/webcast/getWebcastData/{webcastId}")
    public ResponseEntity getUpdateWebcastData(@PathVariable String webcastId){
        return webcastService.getUpdateWebcastData(webcastId);
    }
    @GetMapping("/webcast/getEcdnVendorInfo")
    public ResponseEntity getUpdateWebcastData(@RequestParam("CO") String co , @RequestParam("CSO") String cso , @RequestParam("WebcastId")String webcastId){
        return webcastService.getEcdnVendorInfo(co, cso, webcastId);
    }

    @PostMapping("/webcast/{id}/uploadHeadshot")
    public  ResponseEntity uploadHeadshot(@RequestParam("file")MultipartFile file ,
                                       @RequestParam("fileName") String fileName ,
                                       @RequestParam("status") String status,
                                       @RequestParam(value = "aspectRatio",required = false , defaultValue = "16:3") String aspectRatio,
                                       @RequestParam(value = "prefix",required = false , defaultValue = "")String prefix,
                                       @RequestParam("language") String language,
                                       @PathVariable("id") String webcastId, @RequestParam("imageType") String imageType

    ){
        return this.webcastService.uploadHeadshot(file , fileName , status , aspectRatio , prefix , language ,imageType, webcastId);
    }

    @GetMapping("/webcast/{webcastId}/headShot")
    public ResponseEntity getHeadshot(@PathVariable("webcastId") String webcastId , @RequestParam("status") String status){
        return webcastService.getHeadShot(webcastId , status);
    }

    @DeleteMapping("/webcast/{webcastId}/headShot")
    public ResponseEntity deleteHeadshot(@PathVariable("webcastId") String webcastId , @RequestBody List<SlideData> slideData, @RequestParam("status") String status){
        return webcastService.deleteHeadshot(slideData ,webcastId , status);
    }

    @PutMapping("/webcast/selectHeadshot/{webcastId}")
    public ResponseEntity selectHeadshot(@PathVariable("webcastId") String webcastId,
                                         @RequestParam("status") String status ,
                                         @RequestParam("headshotId") String headShotId){
        return webcastService.updatePrimaryMarkHeadshot(webcastId,status,headShotId);
    }

    @RequestMapping(value = "/webcast/fileConvertPPTX", method = RequestMethod.POST)
    public ResponseEntity fileConvertPPTX(@RequestParam("file") MultipartFile file){
        return webcastService.fileConverterPPTX(file);
    }


}
