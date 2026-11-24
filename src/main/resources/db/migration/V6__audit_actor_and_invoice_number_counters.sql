-- Record who performed each audited action. Existing events predate this column.
alter table audit_events add column actor varchar(100) not null default 'system';

-- One row per year with the last issued invoice sequence; locked while drawing a number.
create table invoice_number_counters (
    invoice_year integer primary key,
    last_value bigint not null
);
