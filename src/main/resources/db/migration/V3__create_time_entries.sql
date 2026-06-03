create table time_entries (
    id uuid primary key,
    project_id uuid not null,
    work_date date not null,
    hours numeric(5, 2) not null,
    description varchar(1000) not null,
    status varchar(30) not null,
    rejection_reason varchar(500),
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null,
    constraint fk_time_entries_project foreign key (project_id) references projects (id)
);

create index ix_time_entries_project_id on time_entries (project_id);
create index ix_time_entries_work_date on time_entries (work_date);
create index ix_time_entries_status on time_entries (status);
