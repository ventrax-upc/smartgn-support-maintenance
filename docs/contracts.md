# API contracts

All `/api/v1/support/**` operations require a current IAM JWT. IAM introspection verifies JWT cryptography, issuer, audience, expiry, account state and current session. Support separately verifies meter/function permissions. Public collections use page (default 0), size (default 10, maximum 100). Ticket lists filter authorized scope before pagination. X-Total-Count identifies matching records.

Existing tickets, replies, photos, visits, diagnosis, closure and rating paths are retained. Replies accept body and optional resolve (default false). Only SuperAdmin can resolve written attention; active visits prevent remote resolution. Legacy identity fields must match the server-resolved identity.

Visits require USE_VISITS independently of USE_TICKETS. SuperAdmin manages technicians, assignments, schedule, start and completion. POST `/visits/{id}/schedule` accepts requestedWindowStart/requestedWindowEnd for the confirmed appointment. POST `/visits/{id}/start` begins a scheduled visit. Completion accepts performedWork, diagnosis and finalCost. The principal owner closes a resolved ticket and rates a closed ticket once.

GET `/technicians/{id}` returns an active technician. POST `/tickets/{id}/photos` requires a real JPEG/PNG multipart file, maximum 10 MiB and 25 million decoded pixels. GET `/tickets/{id}/photos/{photoId}/download` requires authorization on that ticket. Client-supplied object references are rejected.

Internal POST `/internal/v1/events` requires the service credential and accepts CoverageCalculated envelopes with event_id, event_type, aggregate_id and payload. Duplicate event IDs must have equivalent business payloads; monetary representations are normalized and checked against the finalized diagnosis. Only real order references are stored.

TechnicalVisitCompleted HTTP delivery uses eventId, visitId, accountId, meterId, completedAt and cost at Billing's `/internal/v1/technical-visits/completions`. Optional MQTT_URL, MQTT_USERNAME and MQTT_PASSWORD enable QoS 1 publication to smartgn/events/support/TechnicalVisitCompleted. Consumers must deduplicate. No delivery is represented as financial confirmation.

MQTT deployment requires an authenticated service principal allowed to publish smartgn/events/support/# and subscribe to smartgn/events/payments/CoverageCalculated. Configure a distinct MQTT_CLIENT_ID for each running instance. Notification consumers and S3 infrastructure are external deployment dependencies; the local service does not claim successful notification delivery.
