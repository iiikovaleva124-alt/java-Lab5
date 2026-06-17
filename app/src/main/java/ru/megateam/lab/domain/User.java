package ru.megateam.lab.domain;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class User {
    private Long id;
    private String login;
    private String password;

    public User() {
        this.login = "";
        this.password = "";
    }

    public User(String login, String password) {
        this.login = login;
        this.password = hashPassword(password);
    }

    public String getLogin() {
        return login;
    }
    public String getPassword() {
        return password;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256"); //в классе для хеширования создается объект для алгоритма sha-256
            byte[] hash = digest.digest(password.getBytes()); //строка преобразуется в массив байтов, вычисляется хеш от массива
            StringBuilder hexString = new StringBuilder(); //преобразовываем байты в читаемую строку
            //stringbuilder тк размер динамически меняется
            for (byte b : hash) { //цикл по каждому байту хеша
                String hex = Integer.toHexString(0xff & b); //байт преобразуется в беззнаковое число от 0 до 255, потом преобразуется в шестнадцетиричную строку
                if (hex.length() == 1) hexString.append('0'); // проверяет что hex строка из 1 символа, иначе добавляет 0
                hexString.append(hex); // добавляет шестнадцатеричное представление байта к общей строке.
            }
            return hexString.toString(); //преобразует в обычную строку, возвращает хеш как строку из 64 символов
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    public boolean checkPassword(String password) {
        return this.password.equals(hashPassword(password));
    }

    @Override
    public String toString() {
        return "User{login='" + login + "'}";
    }
}