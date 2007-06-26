package org.osaf.cosmo.model;

import java.util.Comparator;

/**
 * Compare NoteItems using a rank calculated from TriageStatus.rank
 * or event startDate, or last modified date.
 */
public class NoteItemTriageStatusComparator implements Comparator<NoteItem> {

    boolean reverse = false;
    long pointInTime = 0;
    
    public NoteItemTriageStatusComparator() {
    }
    
    public NoteItemTriageStatusComparator(long pointInTime) {
        this.pointInTime = pointInTime;
    }
    
    public NoteItemTriageStatusComparator(boolean reverse) {
        this.reverse = reverse;
    }
    
    public int compare(NoteItem note1, NoteItem note2) {
        if(note1.getUid().equals(note2.getUid()))
            return 0;
     
        // Calculate a rank based the proximity of a timestamp 
        // (one of triageStatusRank, eventStart, lastModifiedDate) 
        // to a point in time.
        long rank1 = Math.abs(pointInTime - getRank(note1));
        long rank2 = Math.abs(pointInTime - getRank(note2));
        
        if(rank1>rank2)
            return reverse? -1 : 1;
        else
            return reverse? 1 : -1;
    }
    
    /**
     * Calculate rank of NoteItem.  Rank is the absolute value of the
     * triageStatus rank.  If triageStatus rank is not present, then
     * it is the value in milliseconds of the start time of the event.
     * If the note is not an event then it is the last modified date.
     */
    private long getRank(NoteItem note) {
        // Use triageStatusRank * 1000 to normalize to
        // unix timestamp in milliseconds
        if(note.getTriageStatus()!=null && note.getTriageStatus().getRank()!=null)
            return Math.abs(note.getTriageStatus().getRank().longValue())*1000;
        
        // otherwise use startDate
        BaseEventStamp eventStamp = BaseEventStamp.getStamp(note);
        if(eventStamp!=null) {
            return eventStamp.getStartDate().getTime();
        }
        
        // or occurrence date
        if(note instanceof NoteOccurrence) {
            return ((NoteOccurrence) note).getOccurrenceDate().getTime();
        }
        
        // use modified date as a last resort
        return note.getModifiedDate().getTime();
    }
}
