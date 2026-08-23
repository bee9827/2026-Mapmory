package com.mapmory.backend.tag;

import com.mapmory.backend.common.exception.BusinessException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.text.Normalizer;
import java.util.Locale;

@Embeddable
record TagName(
        @Column(name = "name", nullable = false, length = 30)
        String displayName,

        @Column(name = "name_key", nullable = false, length = 60)
        String nameKey
) {
    private static final int MAX_DISPLAY_NAME_LENGTH = 30;
    private static final int MAX_NAME_KEY_LENGTH = 60;
    private static final String FORBIDDEN_CHARACTERS = "#";

    TagName {
        validatePersistedState(displayName, nameKey);
    }

    static TagName from(String rawName) {
        validateNotNull(rawName);

        String displayName = normalizeDisplayName(rawName);
        return new TagName(displayName, createNameKey(displayName));
    }

    private static void validatePersistedState(String displayName, String nameKey) {
        validateDisplayName(displayName);
        validateNameKey(displayName, nameKey);
    }

    private static void validateDisplayName(String displayName) {
        validateNotNull(displayName);
        validateDisplayNameLength(displayName);
        validateForbiddenCharacters(displayName);
        boolean isNormalizedDisplayName = displayName.equals(normalizeDisplayName(displayName));
        if (!isNormalizedDisplayName) {
            throwInvalidTagName();
        }
    }

    private static void validateNameKey(String displayName, String nameKey) {
        validateNotNull(nameKey);
        validateNameKeyLength(nameKey);

        boolean hasMatchingNameKey = createNameKey(displayName).equals(nameKey);
        if (!hasMatchingNameKey) {
            throwInvalidTagName();
        }
    }

    private static String normalizeDisplayName(String rawName) {
        String withoutOuterWhitespace = rawName.strip();
        String withSingleSpaces = collapseConsecutiveWhitespace(withoutOuterWhitespace);
        return normalizeUnicode(withSingleSpaces);
    }

    private static void validateNotNull(String rawName) {
        if (rawName == null) {
            throwInvalidTagName();
        }
    }

    private static String collapseConsecutiveWhitespace(String name) {
        return name.replaceAll("\\p{javaWhitespace}+", " ");
    }

    private static String normalizeUnicode(String name) {
        return Normalizer.normalize(name, Normalizer.Form.NFC);
    }

    private static void validateDisplayNameLength(String name) {
        int length = name.codePointCount(0, name.length());
        if (length < 1 || length > MAX_DISPLAY_NAME_LENGTH) {
            throwInvalidTagName();
        }
    }

    private static void validateNameKeyLength(String nameKey) {
        int length = nameKey.codePointCount(0, nameKey.length());
        if (length > MAX_NAME_KEY_LENGTH) {
            throwInvalidTagName();
        }
    }

    private static void validateForbiddenCharacters(String name) {
        boolean containsForbiddenCharacter = name.codePoints()
                .anyMatch(codePoint -> FORBIDDEN_CHARACTERS.indexOf(codePoint) >= 0);
        boolean containsControlCharacter = name.codePoints().anyMatch(Character::isISOControl);
        if (containsForbiddenCharacter || containsControlCharacter) {
            throwInvalidTagName();
        }
    }

    private static String createNameKey(String displayName) {
        return displayName.toLowerCase(Locale.ROOT);
    }

    private static void throwInvalidTagName() {
        throw new BusinessException(TagErrorCode.INVALID_TAG_NAME);
    }
}
