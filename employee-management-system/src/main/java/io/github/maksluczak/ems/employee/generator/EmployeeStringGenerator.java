package io.github.maksluczak.ems.employee.generator;

import org.passay.CharacterData;
import org.passay.CharacterRule;
import org.passay.EnglishCharacterData;
import org.passay.PasswordGenerator;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class EmployeeStringGenerator {

    private static final String upperCase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String lowerCase = upperCase.toLowerCase();
    private static final String digits = "0123456789";
    private static final String allChars = upperCase + lowerCase + digits;

    private static final SecureRandom random = new SecureRandom();

    public String generateEmployeeId() {
        StringBuilder id = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            id.append(allChars.charAt(random.nextInt(allChars.length())));
        }

        List<Character> letters = id.chars()
                .mapToObj(c -> (char) c)
                .collect(Collectors.toList());
        Collections.shuffle(letters, random);

        return letters.stream()
                .map(String::valueOf)
                .collect(Collectors.joining());
    }

    public String generateEmployeeSecurePassword() {
        PasswordGenerator gen = new PasswordGenerator();

        CharacterRule lowerCase = new CharacterRule(EnglishCharacterData.LowerCase, 2);
        CharacterRule upperCase = new CharacterRule(EnglishCharacterData.UpperCase, 2);
        CharacterRule digits = new CharacterRule(EnglishCharacterData.Digit, 2);

        CharacterData specialChars = new CharacterData() {
            public String getErrorCode() {
                return "ERROR";
            }

            public String getCharacters() {
                return "!@#$%^&*()_+";
            }
        };
        CharacterRule specials = new CharacterRule(specialChars, 2);

        return gen.generatePassword(8, lowerCase, upperCase, digits, specials);
    }
}
