/*
 * A Cross Platform OS Shell
 * Powered By Truncheon Core
 *
 * This file is part of the Cataphract project.
 * Copyright (C) 2024 DAK404 (https://github.com/DAK404)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */

package Cataphract.API.Minotaur;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;

import Cataphract.API.Config;

/**
 * Provides cryptographic hashing utilities for strings and files.
 */
public class Cryptography {
    private final HashProvider hashProvider;

    public Cryptography() {
        this(new DefaultHashProvider());
    }

    public Cryptography(HashProvider hashProvider) {
        this.hashProvider = hashProvider;
    }

    /**
     * Hashes a string using MD5 (insecure, for legacy use only).
     * @param input The string to hash.
     * @return The MD5 hash, or null on error.
     */
    public String stringToMD5(String input) {
        Config.io.printWarning("MD5 is insecure and should not be used for cryptographic purposes.");
        return hashString(input, "MD5");
    }

    /**
     * Hashes a string using SHA-256.
     * @param input The string to hash.
     * @return The SHA-256 hash, or null on error.
     */
    public String stringToSHA_256(String input) {
        return hashString(input, "SHA-256");
    }

    /**
     * Hashes a string using SHA3-256.
     * @param input The string to hash.
     * @return The SHA3-256 hash, or null on error.
     */
    public String stringToSHA3_256(String input) {
        return hashString(input, "SHA3-256");
    }

    /**
     * Hashes a file using MD5 (insecure, for legacy use only).
     * @param file The file to hash.
     * @return The MD5 hash, or null on error.
     */
    public String fileToMD5(File file) {
        Config.io.printWarning("MD5 is insecure and should not be used for cryptographic purposes.");
        return hashFile(file, "MD5");
    }

    /**
     * Hashes a file using SHA-256.
     * @param file The file to hash.
     * @return The SHA-256 hash, or null on error.
     */
    public String fileToSHA_256(File file) {
        return hashFile(file, "SHA-256");
    }

    /**
     * Hashes a file using SHA3-256.
     * @param file The file to hash.
     * @return The SHA3-256 hash, or null on error.
     */
    public String fileToSHA3_256(File file) {
        return hashFile(file, "SHA3-256");
    }

    private String hashString(String input, String algorithm) {
        if (input == null) {
            Config.io.printError("Cannot hash null string.");
            return null;
        }
        try {
            byte[] hashedBytes = hashProvider.hashString(input, algorithm);
            return convertByteArrayToHexString(hashedBytes);
        } catch (Exception e) {
            Config.io.printError("Error hashing string with " + algorithm + ": " + e.getMessage());
            Config.exceptionHandler.handleException(e);
            return null;
        }
    }

    private String hashFile(File file, String algorithm) {
        if (file == null || !file.exists()) {
            Config.io.printError("File does not exist or is null.");
            return null;
        }
        try {
            byte[] hashedBytes = hashProvider.hashFile(file, algorithm);
            return convertByteArrayToHexString(hashedBytes);
        } catch (Exception e) {
            Config.io.printError("Error hashing file with " + algorithm + ": " + e.getMessage());
            Config.exceptionHandler.handleException(e);
            return null;
        }
    }

    private String convertByteArrayToHexString(byte[] arrayBytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : arrayBytes) {
            hexString.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
        }
        return hexString.toString();
    }
}

/**
 * Interface for cryptographic hashing operations.
 */
interface HashProvider {
    byte[] hashString(String input, String algorithm) throws Exception;
    byte[] hashFile(File file, String algorithm) throws Exception;
}

/**
 * Default implementation of HashProvider using Java's MessageDigest.
 */
class DefaultHashProvider implements HashProvider {
    @Override
    public byte[] hashString(String input, String algorithm) throws Exception {
        MessageDigest digest = MessageDigest.getInstance(algorithm);
        return digest.digest(input.getBytes("UTF-8"));
    }

    @Override
    public byte[] hashFile(File file, String algorithm) throws Exception {
        try (FileInputStream inputStream = new FileInputStream(file)) {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
            return digest.digest();
        }
    }
}