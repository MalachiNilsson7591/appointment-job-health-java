package healthcare;

public final class PatientSafeNotifierTest {
    public static void main(String[] args) throws Exception {
        final int[] captures = {0};
        ErrorReporter errors = payload -> {
            if (!payload.contains("ScheduledAppointmentFailure")) throw new AssertionError(payload);
            captures[0]++;
        };
        PatientSafeNotifier notifier = new PatientSafeNotifier(errors);
        String result = notifier.decide(new AppointmentWorkflow("apt-17", "patient-4", "clinician-2", "FAILED"));
        if (!result.equals("STAFF_ONLY_NOTIFICATION")) throw new AssertionError(result);
        if (captures[0] != 1) throw new AssertionError("capture count: " + captures[0]);
        String quiet = notifier.decide(new AppointmentWorkflow("apt-18", "patient-5", "clinician-2", "CONFIRMED"));
        if (!quiet.equals("NO_NOTIFICATION")) throw new AssertionError(quiet);
        System.out.println("appointment decision test passed");
    }
}
