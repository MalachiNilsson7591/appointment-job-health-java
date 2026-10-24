package healthcare;

import java.io.IOException;

public final class PatientSafeNotifier {
    private final ErrorReporter errors;

    public PatientSafeNotifier(ErrorReporter errors) {
        this.errors = errors;
    }

    public String decide(AppointmentWorkflow appointment) throws IOException, InterruptedException {
        if (!appointment.needsAttention()) return "NO_NOTIFICATION";
        String payload = "{\"exception\":{\"type\":\"ScheduledAppointmentFailure\",\"message\":\"appointment "
                + escapeJson(appointment.appointmentId()) + " requires operational review\"}}";
        errors.capture(payload);
        return "STAFF_ONLY_NOTIFICATION";
    }

    private static String escapeJson(String value) {
        StringBuilder escaped = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char character = value.charAt(i);
            switch (character) {
                case '"' -> escaped.append("\\\"");
                case '\\' -> escaped.append("\\\\");
                case '\b' -> escaped.append("\\b");
                case '\f' -> escaped.append("\\f");
                case '\n' -> escaped.append("\\n");
                case '\r' -> escaped.append("\\r");
                case '\t' -> escaped.append("\\t");
                default -> {
                    if (character < 0x20) {
                        escaped.append(String.format("\\u%04x", (int) character));
                    } else {
                        escaped.append(character);
                    }
                }
            }
        }
        return escaped.toString();
    }
}
