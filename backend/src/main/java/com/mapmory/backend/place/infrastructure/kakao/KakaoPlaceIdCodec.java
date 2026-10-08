package com.mapmory.backend.place.infrastructure.kakao;

import com.mapmory.backend.common.exception.BusinessException;
import com.mapmory.backend.place.application.PlaceErrorCode;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Optional;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Kakao는 장소 ID로 다시 조회하는 API가 없어서, 검색 때 받은 ID·이름·좌표를 장소 ID에 담고 서버가 서명한다.
 * 선택과 저장 때는 서명만 확인하므로 Kakao를 다시 호출하지 않고, 클라이언트가 좌표나 이름을 바꾸면 거절된다.
 * 형식: {@code kakao-} + payload(base64url) + 서명(HMAC-SHA256, base64url 43자). DB place_id(255자)에 맞게 이름을 줄인다.
 */
@Component
public class KakaoPlaceIdCodec {

    public static final String PREFIX = "kakao-";

    private static final int MAX_ID_LENGTH = 255;
    private static final int SIGNATURE_LENGTH = 43;
    private static final String SEPARATOR = "\n";
    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();

    private final byte[] secret;

    public KakaoPlaceIdCodec(@Value("${place.id-signing-secret:${jwt.secret:}}") String secret) {
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
    }

    public static boolean isKakaoPlaceId(String placeId) {
        return placeId != null && placeId.startsWith(PREFIX);
    }

    public String encode(KakaoPlace place) {
        String name = place.name();
        while (true) {
            String payload = ENCODER.encodeToString(String.join(SEPARATOR, place.id(),
                    Double.toString(place.latitude()), Double.toString(place.longitude()), name)
                    .getBytes(StandardCharsets.UTF_8));
            String placeId = PREFIX + payload + sign(payload);
            if (placeId.length() <= MAX_ID_LENGTH || name.isEmpty()) {
                return placeId;
            }
            name = name.substring(0, name.offsetByCodePoints(0, name.codePointCount(0, name.length()) - 1));
        }
    }

    public KakaoPlace decode(String placeId) {
        return tryDecode(placeId).orElseThrow(() -> new BusinessException(PlaceErrorCode.INVALID_PLACE_ID));
    }

    private Optional<KakaoPlace> tryDecode(String placeId) {
        if (!isKakaoPlaceId(placeId) || placeId.length() > MAX_ID_LENGTH
                || placeId.length() <= PREFIX.length() + SIGNATURE_LENGTH) {
            return Optional.empty();
        }
        String body = placeId.substring(PREFIX.length());
        String payload = body.substring(0, body.length() - SIGNATURE_LENGTH);
        String signature = body.substring(body.length() - SIGNATURE_LENGTH);
        if (!MessageDigest.isEqual(sign(payload).getBytes(StandardCharsets.US_ASCII),
                signature.getBytes(StandardCharsets.US_ASCII))) {
            return Optional.empty();
        }
        try {
            String[] parts = new String(DECODER.decode(payload), StandardCharsets.UTF_8).split(SEPARATOR, 4);
            if (parts.length != 4 || parts[3].isBlank()) {
                return Optional.empty();
            }
            double latitude = Double.parseDouble(parts[1]);
            double longitude = Double.parseDouble(parts[2]);
            if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
                return Optional.empty();
            }
            return Optional.of(new KakaoPlace(parts[0], parts[3], null, latitude, longitude));
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }

    private String sign(String payload) {
        if (secret.length == 0) {
            throw new BusinessException(PlaceErrorCode.PLACE_PROVIDER_UNAVAILABLE, "장소 ID 서명 키가 설정되지 않았습니다.");
        }
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            return ENCODER.encodeToString(mac.doFinal(payload.getBytes(StandardCharsets.US_ASCII)));
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
