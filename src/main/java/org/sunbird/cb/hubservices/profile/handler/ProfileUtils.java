package org.sunbird.cb.hubservices.profile.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.sunbird.cb.hubservices.model.RegistryRequest;
import org.sunbird.cb.hubservices.util.Constants;

@Component
public class ProfileUtils {
    static final RestTemplate restTemplate = new RestTemplate();

    public static enum URL {
        CREATE("/add"), READ("/read"),
        SEARCH("/search"), UPDATE("/update");
        private String value;

        private URL(String value) { this.value = value; }
        public String getValue() {
            return this.value;
        }
    }
    public static List<String> getUserDefaultFields() {
        List<String> userFields = new ArrayList<>();
        userFields.add(Constants.PROFILE_DETAILS_PROFESSIOANAL_DETAILS);
        userFields.add(Constants.PROFILE_DETAILS_EMPLOYMENT_DETAILS);
        userFields.add(Constants.PROFILE_DETAILS_PERSONAL_DETAILS);
        userFields.add(Constants.USER_ID);
        userFields.add(Constants.STATUS);
        return userFields;
    }
    private static final String UTIL_CLASS = "Utility class";

    public enum STATUS { APPROVED, REJECTED, PENDING }

    public static class Status {
        private Status() {
            throw new IllegalStateException(UTIL_CLASS);
        }
        public static final String APPROVED = "Approved";
        public static final String REJECTED = "Rejected";
        public static final String PENDING = "Pending";
        public static final String DELETED = "Deleted";

    }


    public static class Profile {
        private Profile() {
            throw new IllegalStateException(UTIL_CLASS);
        }
        public static final String USER_PROFILE = "UserProfile";
        public static final String ID = "id";
        public static final String AT_ID = "@id";
        public static final String USER_ID = "userId";
        public static final String OSID = "osid";
        public static final String FILTERs = "filters";
        public static final String REQUEST = "request";
        public static final String ENTITY_TYPE = "entityType";
        public static final String PROFILE_DETAILS = "profileDetails";

    }

    public static void merge(Map<String,Object> mapLeft, Map<String,Object> mapRight) {
        // go over all the keys of the right map
        for (String key : mapRight.keySet()) {

            Object ml = mapLeft.get(key);
            Object mr = mapRight.get(key);
            // if the left map already has this key, merge the maps that are behind that key
            if (mapLeft.containsKey(key) && ml instanceof HashMap) {
                merge((Map<String, Object>) ml, (Map<String, Object>) mr);

            } else if(mapLeft.containsKey(key) && !(mapLeft.get(key) instanceof HashMap)){
                mapLeft.put(key, mapRight.get(key));
            }else {
                // otherwise just add the map under that key
                mapLeft.put(key, mapRight.get(key));
            }
        }
    }


    public static void mergeLeaf(Map<String,Object> mapLeft, Map<String,Object> mapRight, String leafKey, String id) {
        // go over all the keys of the right map

        for (String key : mapLeft.keySet()) {

            if(key.equalsIgnoreCase(leafKey) && (mapLeft.get(key) instanceof ArrayList)){

                ((ArrayList)mapLeft.get(key)).removeIf(o -> ((Map)o).get("osid").toString().equalsIgnoreCase(id));
                ((ArrayList)mapLeft.get(key)).add(mapRight);

            }
            if(key.equalsIgnoreCase(leafKey) && (mapLeft.get(key) instanceof HashMap)){
                mapLeft.put(key, mapRight);

            }

        }
    }

    public static ResponseEntity getResponseEntity(String baseUrl, String endPoint, RegistryRequest registryRequest){
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.add(Constants.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        HttpEntity<RegistryRequest> requestEntity = new HttpEntity<>(registryRequest, requestHeaders);

        ResponseEntity responseEntity = restTemplate.exchange(
                baseUrl + endPoint,
                HttpMethod.POST,
                requestEntity,
                Map.class
        );

        return new ResponseEntity<>(responseEntity.getBody(), responseEntity.getStatusCode());
    }


}
