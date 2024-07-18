package com.sparta.springresttemplateclient.service;

import com.sparta.springresttemplateclient.dto.ItemDto;
import com.sparta.springresttemplateclient.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class RestTemplateService {
    private final RestTemplate restTemplate;

    @Autowired //builder에 빨간줄가서 추가해봄 그래도 안되지만 gpt가 이렇게 하라고해서 해봤다
    //RestTemplate을 스프링에서 자동등록하는게 아니라서 생성자 파라미터에서 RestTemplateBuilder를 받아와야함
    //public RestTemplateService(RestTemplate restTemplate){
    public RestTemplateService(RestTemplateBuilder builder) {
        //this.restTemplate = restTemplate;
        this.restTemplate = builder.build();
    }


    //UriComponentsBuilder를 사용하여 uri 객체를 만들 수 있다.
    public ItemDto getCallObject(String query) {
        // 요청 URL 만들기
        URI uri = UriComponentsBuilder
                .fromUriString("http://localhost:7070") //서버의 주소
                .path("/api/server/get-call-obj") //서버 컨트롤러의 구성
                .queryParam("query", query) //uri뒤에 ?붙여서 쿼리 보내는 방법이 이것이다
                .encode()
                .build()
                .toUri();
        log.info("uri = " + uri);

        //getForEntity는 get 방식으로 요청함. 받는 정보는 ItemDto.class 형식으로 받겠다는 의미.
        //restTemplate 사용하면 ItemDto.class 타입으로 자동으로 역직렬화되어 객체형태로 넘어온다 (jackson 안써도, objectMapper안써도,, 이런것처럼 작동함)
        ResponseEntity<ItemDto> responseEntity = restTemplate.getForEntity(uri, ItemDto.class);
        //ResponseEntity 타입으로 받는다. ResponseEntity는 응답시 사용하는 클래스

        log.info("statusCode = " + responseEntity.getStatusCode()); //서버쪽에서 코드가 날라온다.

        return responseEntity.getBody(); //안의 itemDto가 반환됨
    }

    public List<ItemDto> getCallList() {
        // 요청 URL 만들기
        URI uri = UriComponentsBuilder
                .fromUriString("http://localhost:7070")
                .path("/api/server/get-call-list")
                .encode()
                .build()
                .toUri();
        log.info("uri = " + uri);

        ResponseEntity<String> responseEntity = restTemplate.getForEntity(uri, String.class);
        //여러개를 받을 경우 그냥 스트링으로 다 받아서 fromJSONtoItems 로 변환한다

        log.info("statusCode = " + responseEntity.getStatusCode());
        log.info("Body = " + responseEntity.getBody());

        return fromJSONtoItems(responseEntity.getBody());
    }

    public ItemDto postCall(String query) {
        // 요청 URL 만들기
        URI uri = UriComponentsBuilder
                .fromUriString("http://localhost:7070")
                .path("/api/server/post-call/{query}") //pathVariable 방식
                .encode()
                .build()
                .expand(query) //여기 추가됨
                .toUri();
        log.info("uri = " + uri);

        User user = new User("Robbie", "1234");

        ResponseEntity<ItemDto> responseEntity = restTemplate.postForEntity(uri, user, ItemDto.class);
        //두번째 파라미터 : 요청 바디에 포함할 객체. 이 객체는 JSON 또는 XML 등의 형식으로 변환된다.

        log.info("statusCode = " + responseEntity.getStatusCode());

        return responseEntity.getBody();
    }

    public List<ItemDto> exchangeCall(String token) {
        return null;
    }

    //여러개의 data는 이런식으로 넘어온다
  /*  {
        "items":[
        {"title":"Mac","price":3888000},
        {"title":"iPad","price":1230000},
        {"title":"iPhone","price":1550000},
        {"title":"Watch","price":450000},
        {"title":"AirPods","price":350000}
	    ]
    }*/

    public List<ItemDto> fromJSONtoItems(String responseEntity) {
        JSONObject jsonObject = new JSONObject(responseEntity);
        JSONArray items  = jsonObject.getJSONArray("items"); //items를 키값으로하는 배열이 담긴다
        List<ItemDto> itemDtoList = new ArrayList<>();

        for (Object item : items) {
            ItemDto itemDto = new ItemDto((JSONObject) item); //json을 쉽게 다루기 위해 JSONObject 형태로 변환한다.
            //itemDto에 위에 해당하는 생성자를 만들어놓음
            itemDtoList.add(itemDto);
        }

        return itemDtoList;
    }
}