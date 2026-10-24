package healthcare;

public record AppointmentWorkflow(String appointmentId, String patientId, String clinicianId, String status) {
    public boolean needsAttention() {
        return status.equals("FAILED") || status.equals("MISSED");
    }
}
