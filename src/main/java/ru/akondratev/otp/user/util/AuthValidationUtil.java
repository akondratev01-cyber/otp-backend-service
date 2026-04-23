package ru.akondratev.otp.user.util;

import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

public final class AuthValidationUtil {
    private static final Pattern LOGIN_PATTERN =
            Pattern.compile("^[a-zA-Z0-9._-]{3,50}$");

    private AuthValidationUtil() {
    }

    public static String validateLogin(String login) {
        if (login == null) {
            return "Необходимо указать логин";
        }

        String normalizedLogin = login.trim();

        if (normalizedLogin.isEmpty()) {
            return "Необходимо указать логин";
        }

        if (normalizedLogin.length() < 3) {
            return "Длина логина должна быть не менее 3 символов";
        }

        if (normalizedLogin.length() > 50) {
            return "Длина логина должна быть не более 50 символов";
        }

        if (!LOGIN_PATTERN.matcher(normalizedLogin).matches()) {
            return "Логин может содержать только латинские буквы, цифры, дефисы и подчеркивания";
        }

        return null;
    }

    public static String validatePassword(String password) {
        if (password == null) {
            return "Необходимо указать пароль";
        }

        if (password.isBlank()) {
            return "Необходимо указать пароль";
        }

        if (password.length() < 8) {
            return "Длина пароля должна быть не менее 8 символов";
        }

        byte[] utf8 = password.getBytes(StandardCharsets.UTF_8);
        if (utf8.length > 72) {
            return "Пароль не может превышать 72 байта в UTF-8 для алгоритма bcrypt";
        }

        return null;
    }
}