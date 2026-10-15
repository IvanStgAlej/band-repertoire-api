package es.ivanstg.repertoire.rehearsal.dto;

import es.ivanstg.repertoire.rehearsal.Attendance;

public record AttendanceResponse(Long musicianId, String name, boolean attended, String reason) {

    public static AttendanceResponse from(Attendance attendance) {
        var musician = attendance.getMusician();
        return new AttendanceResponse(musician.getId(), musician.getName(), attendance.isAttended(),
                attendance.getReason());
    }
}
