create table audit_events (
    id uuid primary key,
    aggregate_type varchar(60) not null,
    aggregate_id uuid not null,
    event_type varchar(80) not null,
    message varchar(1000) not null,
    created_at timestamp with time zone not null
);

create index ix_audit_events_aggregate on audit_events (aggregate_type, aggregate_id);
create index ix_audit_events_event_type on audit_events (event_type);
create index ix_audit_events_created_at on audit_events (created_at);
