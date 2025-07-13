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

import java.time.LocalDate;
import java.time.YearMonth;

import Cataphract.API.Config;

/**
 * Provides utilities for printing calendars.
 */
public class Calendar {
    private final Time timeProvider;
    private int month;
    private int year;

    public Calendar() {
        this(new Time());
    }

    public Calendar(Time timeProvider) {
        this.timeProvider = timeProvider;
    }

    /**
     * Prints a calendar for the specified month and year.
     * @param specificMonth The desired month (1-12). If 0 or invalid, uses current month.
     * @param specificYear The desired year. If 0, uses current year.
     */
    public void printCalendar(int specificMonth, int specificYear) {
        try {
            setMonthAndYear(specificMonth, specificYear);
            printCalendar();
        } catch (Exception e) {
            Config.io.printError("Error displaying calendar: " + e.getMessage());
            Config.exceptionHandler.handleException(e);
        }
    }

    private void setMonthAndYear(int specificMonth, int specificYear) {
        try {
            String currentMonth = timeProvider.getDateTimeUsingSpecifiedFormat("MM");
            String currentYear = timeProvider.getDateTimeUsingSpecifiedFormat("yyyy");
            month = (specificMonth == 0 || specificMonth < 1 || specificMonth > 12)
                    ? Integer.parseInt(currentMonth)
                    : specificMonth;
            year = specificYear == 0 ? Integer.parseInt(currentYear) : specificYear;
        } catch (Exception e) {
            Config.io.printError("Error determining month/year: " + e.getMessage());
            Config.exceptionHandler.handleException(e);
            month = Integer.parseInt(timeProvider.getDateTimeUsingSpecifiedFormat("MM"));
            year = Integer.parseInt(timeProvider.getDateTimeUsingSpecifiedFormat("yyyy"));
        }
    }

    private void printCalendar() {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate firstDayOfMonth = yearMonth.atDay(1);
        String currentDate = timeProvider.getDateTimeUsingSpecifiedFormat("dd-MMMM-yyyy  EEEE");

        Config.io.println("\nToday is: " + currentDate + "\n");
        Config.io.printInfo("You are currently viewing " + yearMonth.getMonth().toString() + "-" + yearMonth.getYear() + "\n");
        Config.io.println("Su Mo Tu We Th Fr Sa");

        int initialSpace = firstDayOfMonth.getDayOfWeek().getValue() % 7;
        StringBuilder calendarOutput = new StringBuilder();
        calendarOutput.append(" ".repeat(initialSpace * 3));

        for (int day = 1; day <= yearMonth.lengthOfMonth(); day++) {
            calendarOutput.append(String.format("%2s ", String.format("%02d", day)));
            if ((day + initialSpace) % 7 == 0 || day == yearMonth.lengthOfMonth()) {
                calendarOutput.append("\n");
            }
        }

        Config.io.println(calendarOutput.toString());
    }
}