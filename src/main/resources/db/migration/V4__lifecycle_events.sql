CREATE TABLE support_lifecycle_outbox(id uuid PRIMARY KEY,action varchar(100) NOT NULL,resource_id uuid NOT NULL,actor_id uuid,created_at timestamptz NOT NULL DEFAULT now(),delivered_at timestamptz);
ALTER TABLE support_inbox ADD COLUMN visit_id uuid;
CREATE UNIQUE INDEX one_coverage_receipt ON support_inbox(visit_id);

DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname='ticket_replies_ticket_id_fkey' AND conrelid='ticket_replies'::regclass) THEN ALTER TABLE ticket_replies ADD CONSTRAINT ticket_replies_ticket_id_fkey FOREIGN KEY (ticket_id) REFERENCES support_tickets; END IF; END $$;

DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname='ticket_photos_ticket_id_fkey' AND conrelid='ticket_photos'::regclass) THEN ALTER TABLE ticket_photos ADD CONSTRAINT ticket_photos_ticket_id_fkey FOREIGN KEY (ticket_id) REFERENCES support_tickets; END IF; END $$;

DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname='technical_visits_ticket_id_fkey' AND conrelid='technical_visits'::regclass) THEN ALTER TABLE technical_visits ADD CONSTRAINT technical_visits_ticket_id_fkey FOREIGN KEY (ticket_id) REFERENCES support_tickets; END IF; END $$;

DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname='technician_assignments_visit_id_fkey' AND conrelid='technician_assignments'::regclass) THEN ALTER TABLE technician_assignments ADD CONSTRAINT technician_assignments_visit_id_fkey FOREIGN KEY (visit_id) REFERENCES technical_visits; END IF; END $$;

DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname='technician_assignments_technician_id_fkey' AND conrelid='technician_assignments'::regclass) THEN ALTER TABLE technician_assignments ADD CONSTRAINT technician_assignments_technician_id_fkey FOREIGN KEY (technician_id) REFERENCES technicians; END IF; END $$;

DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname='visit_diagnoses_visit_id_fkey' AND conrelid='visit_diagnoses'::regclass) THEN ALTER TABLE visit_diagnoses ADD CONSTRAINT visit_diagnoses_visit_id_fkey FOREIGN KEY (visit_id) REFERENCES technical_visits; END IF; END $$;

DO $$ BEGIN IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname='service_ratings_ticket_id_fkey' AND conrelid='service_ratings'::regclass) THEN ALTER TABLE service_ratings ADD CONSTRAINT service_ratings_ticket_id_fkey FOREIGN KEY (ticket_id) REFERENCES support_tickets; END IF; END $$;
