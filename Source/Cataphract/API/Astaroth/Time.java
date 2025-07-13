/*
*                                                      |
*                                                     ||
*  |||||| ||||||||| |||||||| ||||||||| |||||||  |||  ||| ||||||| |||||||||  |||||| |||||||||
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

package Cataphract.API.Astaroth;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import Cataphract.API.Config;

/**
 * Provides utilities for handling date and time operations.
 */
public class Time {
    private final TimeProvider timeProvider;

    public Time() {
        this(new DefaultTimeProvider());
    }

    public Time(TimeProvider timeProvider) {
        this.timeProvider = timeProvider;
    }

    /**
     * Gets the current time in "HH:mm:ss" format.
     * @return The formatted time string.
     */
    public String getTime() {
        return getDateTimeUsingSpecifiedFormat("HH:mm:ss");
    }

    /**
     * Gets the current date/time in the specified format.
     * @param format The desired format (e.g., "yyyy-MM-dd HH:mm:ss").
     * @return The formatted date/time string, or empty string on error.
     */
    public String getDateTimeUsingSpecifiedFormat(String format) {
        try {
            LocalDateTime dateTime = timeProvider.getCurrentDateTime();
            return dateTime.format(DateTimeFormatter.ofPattern(format));
        } catch (Exception e) {
            Config.io.printError("Invalid Date/Time Format Detected! Please enter a valid Date/Time format.");
            Config.exceptionHandler.handleException(e);
            return "";
        }
    }

    /**
     * Gets the current Unix epoch timestamp (seconds since January 1, 1970).
     * @return The epoch timestamp.
     */
    public long getUnixEpoch() {
        return timeProvider.getCurrentInstant().getEpochSecond();
    }
}

/**
 * Interface for time-related operations.
 */
interface TimeProvider {
    LocalDateTime getCurrentDateTime();
    Instant getCurrentInstant();
}

/**
 * Default implementation using system time.
 */
class DefaultTimeProvider implements TimeProvider {
    @Override
    public LocalDateTime getCurrentDateTime() {
        return LocalDateTime.now();
    }

    @Override
    public Instant getCurrentInstant() {
        return Instant.now();
    }
}