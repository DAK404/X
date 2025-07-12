/*
*                                                      |
*                                                     ||
*  |||||| ||||||||| |||||||| ||||||||| |||||||  |||  ||| ||||||| |||||||||  |||||| ||||||||
* |||            ||    |||          ||       || |||  |||       ||       || |||        |||
* |||      ||||||||    |||    ||||||||  ||||||  ||||||||  ||||||  |||||||| |||        |||
* |||      |||  |||    |||    |||  |||  |||     |||  |||  ||  ||  |||  ||| |||        |||
*  ||||||  |||  |||    |||    |||  |||  |||     |||  |||  ||   || |||  |||  ||||||    |||
*                                               ||
*                                               |
*
* A Cross Platform OS Shell
* Powered By Truncheon Core
*/

/*
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
import java.io.UnsupportedEncodingException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
* A class implementing cryptographic hashing methods for files and literals
*
* @author DAK404 (https://github.com/DAK404)
* @version 2.1.4 (11-October-2023, Cataphract)
* @since 0.0.1 (Mosaic 1.0)
*/
public class Cryptography
{

    /**
    * Sole constructor. (For invocation by subclass constructors, typically implicit.)
    */
    public Cryptography()
    {
    }

    // ------------------------------------------------------------------------------------ //
    //                                  PUBLIC API METHODS                                  //
    // ------------------------------------------------------------------------------------ //

    /**
    * Hashing API which will convert a string to an MD5 hash
    *
    * @param input The string that is to be encoded
    * @return String Returns the MD5 hash
    * @throws Exception Throws any exceptions encountered during runtime.
    */
    public final static String stringToMD5(String input)throws Exception
    {
        return hashString(input, "MD5");
    }

    /**
    * Hashing API which will convert a string to an SHA3-256 hash
    *
    * @param input The String that is to be encoded
    * @return String Returns the SHA3-256 hash
    * @throws Exception Throws any exceptions encountered during runtime.
    */
    public final static String stringToSHA_256(String input) throws Exception
    {
        return hashString(input, "SHA-256");
    }

    /**
    * Hashing API which will convert a string to an SHA3-256 hash
    *
    * @param input The String that is to be encoded
    * @return String Returns the SHA3-256 hash
    * @throws Exception Throws any exceptions encountered during runtime.
    */
    public final static String stringToSHA3_256(String input) throws Exception
    {
        return hashString(input, "SHA3-256");
    }

    /**
    * Hashing API which will return the MD5 hash value of a given file
    *
    * @param fileName The file to be hashed
    * @return String Returns the MD5 hash
    * @throws Exception Throws any exceptions encountered during runtime.
    */
    public final static String fileToMD5(File fileName) throws Exception
    {
        return hashFile(fileName, "MD5");
    }

    /**
    * Hashing API which will return the SHA3-256 hash value of a given file
    *
    * @param fileName The file to be hashed
    * @return String Returns the SHA3-256 hash
    * @throws Exception Throws any exceptions encountered during runtime.
    */
    public final static String fileToSHA_256(File fileName) throws Exception
    {
        return hashFile(fileName, "SHA-256");
    }

    /**
    * Hashing API which will return the SHA3-256 hash value of a given file
    *
    * @param fileName The file to be hashed
    * @return String Returns the SHA3-256 hash
    * @throws Exception Throws any exceptions encountered during runtime.
    */
    public final static String fileToSHA3_256(File fileName) throws Exception
    {
        return hashFile(fileName, "SHA3-256");
    }

    // ------------------------------------------------------------------------------------ //
    //                                   API BACKEND CODE                                   //
    // ------------------------------------------------------------------------------------ //

    /**
    * Convert the byte array to a Hex String
    *
    * @param arrayBytes The input byte array
    * @return String The Hex String
    */
    private final static String convertByteArrayToHexString(byte[] arrayBytes)
    {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < arrayBytes.length; i++)
        stringBuffer.append(Integer.toString((arrayBytes[i] & 0xff) + 0x100, 16).substring(1));
        return stringBuffer.toString();
    }

    /**
    * The logic to hash a file.
    *
    * @param file The input file object
    * @param algorithm The algorithm to be used to hash the file
    * @return String result The hashed string
    * @throws Exception Throws any exceptions encountered during runtime.
    */
    private final static String hashFile(File file, String algorithm)throws Exception
    {
        String result = null;

        if(file.exists())
        {
            try (FileInputStream inputStream = new FileInputStream(file))
            {
                MessageDigest digest = MessageDigest.getInstance(algorithm);

                byte[] bytesBuffer = new byte[1024];
                int bytesRead = -1;

                while ((bytesRead = inputStream.read(bytesBuffer)) != -1)
                {
                    digest.update(bytesBuffer, 0, bytesRead);
                }

                byte[] hashedBytes = digest.digest();

                result = convertByteArrayToHexString(hashedBytes);
            }
            catch (NoSuchAlgorithmException E)
            {
                Cataphract.API.IOStreams.printError("Unsupported Algorithm.\n\n");
                E.printStackTrace();
            }
        }
        return result;
    }

    /**
    * Logic to hash a string
    *
    * @param message The string to be hashed
    * @param algorithm The algorithm to be used to hash the string
    * @return String result The hashed string
    * @throws Exception Throws any exceptions encountered during runtime.
    */
    private final static String hashString(String message, String algorithm)throws Exception
    {
        String result = null;
        if(message != null)
        {
            try
            {
                MessageDigest digest = MessageDigest.getInstance(algorithm);
                byte[] hashedBytes = digest.digest(message.getBytes("UTF-8"));

                result =  convertByteArrayToHexString(hashedBytes);
            }
            catch (NoSuchAlgorithmException | UnsupportedEncodingException E)
            {
                Cataphract.API.IOStreams.printError("Unsupported Algorithm or Encoding.\n\n");
                E.printStackTrace();
            }
        }
        return result;
    }
}