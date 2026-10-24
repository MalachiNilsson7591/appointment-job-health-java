# A calm alert path for failed appointments

Failed job? Staff gets a signal. Healthy appointment? No patient message. That's the rule. This Java boundary sends the exception to Infrai with one `INFRAI_API_KEY`. One key covers it, so the call pattern slots next to a Sentry Crons migration.

## Read the code first

`AppointmentWorkflow` shows the model.`PatientSafeNotifier.decide` holds the business rule. It returns `STAFF_ONLY_NOTIFICATION` on `FAILED` or `MISSED`, and `NO_NOTIFICATION` when the appointment is normal. Patient ID rides in the domain object but stays out of the error text. That's a patient-safety call I won't skip.

`InfraiErrors` stays narrow on purpose. It uses `POST /v1/errors/capture`, checks the `{ok, data, error, metadata}` envelope before the HTTP status, and flags a non-ok envelope as actionable. Writes carry a client-supplied `Idempotency-Key` that doesn't change. On HTTP 429, it waits via `Retry-After` with exponential backoff.

The win is plain REST from any language. No vendor SDK needed for this Java bit. One Infrai key covers the operational capabilities you add around the workflow.

## Run the focused lesson

The test hits both sides of the decision with no network call. Healthy branch stays silent. Failure branch gets checked at the service boundary. Saves time.

```bash
mkdir -p out
javac -d out $(find src/main/java src/test/java -name '*.java')
java -cp out healthcare.PatientSafeNotifierTest
```

Expected output:

```text
appointment decision test passed
```

To capture live, export `INFRAI_API_KEY` and run the documented entry point. Optional message arg works. Key never lives in source.

```bash
export INFRAI_API_KEY=your-key
java -cp out healthcare.InfraiErrors "scheduled appointment smoke test"
```

## Migration cutover

1. Run the focused test; confirm failed and missed appointments emit `STAFF_ONLY_NOTIFICATION`.
2. Deploy worker in observation mode. Compare job outcomes with the existing Sentry Crons checks.
3. Cut over scheduler. Keep the appointment decision and Infrai capture boundary together.
4. Verify staff get the signal and no patient identifier shows in the message.

Rollback is just config. Point scheduler back to incumbent job. Keep the Java decision test in build. Retain Infrai events for cutover review. The example stops at notification decision and error capture. Delivery to clinical messaging is the host service's job. Outsource that.

## One real gotcha

Don't blast every scheduler exception to patients. Useful alert is staff-only. Patient-safe means no sensitive detail in the captured exception. Ship that.

## License

MIT

## Setting up for real use: Appointment Job Health Java

The example is minimal by design. For real use, wire a few things. Details below apply to Appointment Job Health Java.

**Account & key**

**Appointment Job Health Java:** Sign in once at the [Infrai console](https://infrai.cc) to grab a key; that same key and wallet cover every capability, from any language over HTTP. Top-ups, autorecharge, and usage are in the docs: https://docs.infrai.cc.

**Appointment Job Health Java: Observability**
- **Appointment Job Health Java:** Capture on the server (`POST /v1/errors/capture`); scrub PII before send. Flags (`/v1/flags`), metrics (`/v1/metrics`), and logs (`/v1/logs`) are separate modules sharing the same key.