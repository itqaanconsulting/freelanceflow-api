create table invoices (
    id uuid primary key,
    project_id uuid not null,
    invoice_number varchar(40) not null,
    issue_date date not null,
    due_date date not null,
    status varchar(30) not null,
    currency varchar(3) not null,
    total_amount numeric(12, 2) not null,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null,
    constraint fk_invoices_project foreign key (project_id) references projects (id)
);

create unique index ux_invoices_invoice_number on invoices (invoice_number);
create index ix_invoices_project_id on invoices (project_id);
create index ix_invoices_status on invoices (status);

create table invoice_lines (
    id uuid primary key,
    invoice_id uuid not null,
    time_entry_id uuid not null,
    description varchar(1000) not null,
    work_date date not null,
    hours numeric(5, 2) not null,
    hourly_rate numeric(12, 2) not null,
    line_amount numeric(12, 2) not null,
    constraint fk_invoice_lines_invoice foreign key (invoice_id) references invoices (id),
    constraint fk_invoice_lines_time_entry foreign key (time_entry_id) references time_entries (id)
);

create unique index ux_invoice_lines_time_entry_id on invoice_lines (time_entry_id);

alter table time_entries add column invoice_id uuid;
alter table time_entries add constraint fk_time_entries_invoice foreign key (invoice_id) references invoices (id);
create index ix_time_entries_invoice_id on time_entries (invoice_id);
