# Architecture

The domain uses SupportTicket and TechnicalVisit aggregates and JPA entities. Application repository ports are implemented by Spring Data adapters. IAM owns identity/session validation and delegated authorization; Installation owns current meter ownership. Calls use authenticated HTTP with bounded timeouts. Only USER and SUPERADMIN identities are accepted.

Tickets and visits have independent lifecycles. Technical completion resolves the ticket but never closes it or waits for a financial result. Billing owns coverage, charges, payment and fiscal documents. Completion and its outbox entry are committed locally together. HTTP delivery retries with the same event ID. Optional MQTT delivery uses a separate receipt. No gRPC is used.

Photographs are private files in the configured persistent volume. PostgreSQL stores generated object keys and uploader metadata. Files are accessed through authorized ticket routes.
