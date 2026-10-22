package kr.dbinc.praise;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/entries")
public class EntryController {

    private static final int MAX_BODY_BYTES = 8 * 1024;
    private static final int MAX_ENTRIES = 200;
    private static final int MAX_NAME = 40;
    private static final int MAX_MESSAGE = 500;

    private final EntryRepository entries;
    private final ObjectMapper mapper;

    public EntryController(EntryRepository entries, ObjectMapper mapper) {
        this.entries = entries;
        this.mapper = mapper;
    }

    @GetMapping
    public Map<String, List<Map<String, Object>>> list() {
        return Map.of("entries", entries.latest(MAX_ENTRIES));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(HttpServletRequest request) throws IOException {
        requireJson(request.getContentType());
        JsonNode body = parse(readBody(request));
        String name = field(body, "name", "보내는 사람", MAX_NAME);
        String message = field(body, "message", "칭찬 한마디", MAX_MESSAGE);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("entry", entries.insert(name, message)));
    }

    private static void requireJson(String contentType) {
        try {
            if (contentType != null
                    && MediaType.APPLICATION_JSON.isCompatibleWith(MediaType.parseMediaType(contentType))) {
                return;
            }
        } catch (IllegalArgumentException ignored) {
            // 아래에서 같은 415로 답해요.
        }
        throw new ApiException(415, "JSON(application/json)으로 보내 주세요.");
    }

    private static byte[] readBody(HttpServletRequest request) throws IOException {
        if (request.getContentLengthLong() > MAX_BODY_BYTES) {
            throw tooLarge();
        }
        try (InputStream in = request.getInputStream()) {
            byte[] data = in.readNBytes(MAX_BODY_BYTES + 1);
            if (data.length > MAX_BODY_BYTES) {
                throw tooLarge();
            }
            return data;
        }
    }

    private static ApiException tooLarge() {
        return new ApiException(413, "본문이 너무 커요. 8KiB 이하로 보내 주세요.");
    }

    private JsonNode parse(byte[] data) {
        try {
            JsonNode node = mapper.readTree(data);
            if (node == null || !node.isObject()) {
                throw new ApiException(400, "JSON 객체로 보내 주세요.");
            }
            return node;
        } catch (JsonProcessingException e) {
            throw new ApiException(400, "JSON 형식이 올바르지 않아요.");
        } catch (IOException e) {
            throw new ApiException(400, "본문을 읽을 수 없어요.");
        }
    }

    private static String field(JsonNode body, String key, String label, int max) {
        JsonNode node = body.get(key);
        if (node == null || !node.isTextual()) {
            throw new ApiException(400, label + "(" + key + ")은 문자열이어야 해요.");
        }
        String value = node.asText().strip();
        int length = value.codePointCount(0, value.length());
        if (length < 1 || length > max) {
            throw new ApiException(400, label + "(" + key + ")은 1~" + max + "자로 적어 주세요.");
        }
        return value;
    }
}
