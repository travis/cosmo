/*
 * Copyright 2006 Open Source Applications Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.osaf.cosmo.rpc.model;

public class CosmoDate implements Cloneable{

    public static final int MONTH_JANUARY = 0;
    public static final int MONTH_FEBRUARY = 1;
    public static final int MONTH_MARCH = 2;
    public static final int MONTH_APRIL = 3;
    public static final int MONTH_MAY = 4;
    public static final int MONTH_JUNE = 5;
    public static final int MONTH_JULY = 6;
    public static final int MONTH_AUGUST = 7;
    public static final int MONTH_SEPTEMBER = 8;
    public static final int MONTH_OCTOBER = 9;
    public static final int MONTH_NOVEMBER = 10;
    public static final int MONTH_DECEMBER = 11;

    private int year = 0;
    private int month = 0;
    private int date = 0;
    private int hours = 0;
    private int minutes = 0;
    private int seconds = 0;
    private boolean utc = false;
    private String tzId = null;

    public CosmoDate(){

    }

    public int getDate() {
        return date;
    }

    public void setDate(int day) {
        this.date = day;
    }

    public int getMinutes() {
        return minutes;
    }

    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getSeconds() {
        return seconds;
    }

    public void setSeconds(int seconds) {
        this.seconds = seconds;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public boolean isUtc() {
        return utc;
    }

    public void setUtc(boolean utc) {
        this.utc = utc;
    }

    public int getHours() {
        return hours;
    }

    public void setHours(int hours) {
        this.hours = hours;
    }

    public String getTzId() {
        return tzId;
    }

    public void setTzId(String tzId) {
        this.tzId = tzId;
    }

    public CosmoDate clone(){
        try {
            return (CosmoDate) super.clone();
        } catch (CloneNotSupportedException cnse){
            throw new RuntimeException(cnse);
        }
    }

    public boolean equals (Object o){
        CosmoDate that = (CosmoDate) o;
        if (this.utc == that.utc
            && this.year == that.year
            && this.month == that.month
            && this.date == that.date
            && this.hours == that.hours
            && this.seconds == that.seconds){


        }
        return false;
    }
}
