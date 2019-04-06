package com.chatalk.app.util;

import java.util.Comparator;

import com.chatalk.app.models.LogMessage;

/**
 * Comparator used to sort arraylists of logs by date
 * From most recent to oldest
 */
public class ArraySorting implements Comparator<LogMessage> {
    @Override
    public int compare(LogMessage o1, LogMessage o2) {
           return o2.getDate().compareTo(o1.getDate());
   }
}
