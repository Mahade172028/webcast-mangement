package com.wtv.webcastmanagement.utils;

import com.wtv.webcastmanagement.dto.dto.KollectiveResponse;
import com.wtv.webcastmanagement.entity.Kollective.Items;
import com.wtv.webcastmanagement.entity.Kollective.KollectiveBody;
import com.wtv.webcastmanagement.entity.Kollective.Source;
import com.wtv.webcastmanagement.entity.KollectiveInfo;
import com.wtv.webcastmanagement.repository.KollectiveInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
public class KollectiveTokenGenerator {
    @Autowired
    private KollectiveInfoRepository kollectiveInfoRepository;
    private String KOLLECTIVE_URL = "https://content.kollective.app/api/content/";

    public KollectiveResponse getKollectiveContentTokenMultipleSourceWithSecrets(
            String title, String description, List<Source> sources, String tenantId, String tokenId){
        RestTemplate restTemplate= new RestTemplate();
    KollectiveBody tempBody = new KollectiveBody();
    tempBody.setServiceToken(tokenId);
    Items item = new Items();
    item.setTitle(title);
    item.setSources(sources);
    item.setDescription(description);
    tempBody.setItem(item);
    HttpHeaders headers = new HttpHeaders();
    headers.set(StaticValues.content_Type, StaticValues.application_Json);
    HttpEntity<Object> requestEntity = new HttpEntity<>(tempBody ,headers);
    String kollective_url = KOLLECTIVE_URL + tenantId;
    ResponseEntity<KollectiveResponse> responseEntity = null;
    try {
        responseEntity = restTemplate.exchange(kollective_url, HttpMethod.POST, requestEntity, KollectiveResponse.class);
        if(requestEntity.getBody() != null){
            KollectiveResponse kollectiveResponse = responseEntity.getBody();
            KollectiveInfo kollectiveInfo = new KollectiveInfo();
            kollectiveInfo.setMoid(kollectiveResponse.getLegacy().getMoid());
            kollectiveInfo.setUrl(kollectiveResponse.getItems().size()>0?kollectiveResponse.getItems().get(0).getUrl() : "");
            kollectiveInfo.setTenantId(kollectiveResponse.getItems().size() > 0? kollectiveResponse.getItems().get(0).getTenantId(): "");
            kollectiveInfo.setTitle(kollectiveResponse.getItems().size() > 0? kollectiveResponse.getItems().get(0).getTitle(): "");
            kollectiveInfo.setContentToken(kollectiveResponse.getContentToken());
            kollectiveInfo.setKollectiveUrl("_kollective_," + kollectiveResponse.getContentToken());
            kollectiveInfoRepository.save(kollectiveInfo);
        }
    }catch (Exception e){

    }
    return responseEntity.getBody();
    }
}
