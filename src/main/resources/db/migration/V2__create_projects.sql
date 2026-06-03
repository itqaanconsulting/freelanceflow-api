create table projects (
    id uuid primary key,
    customer_id uuid not null,
    name varchar(160) not null,
    description varchar(1000),
    hourly_rate numeric(12, 2) not null,
    currency varchar(3) not null,
    status varchar(30) not null,
    start_date date,
    end_date date,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null,
    constraint fk_projects_customer foreign key (customer_id) references customers (id)
);

create index ix_projects_customer_id on projects (customer_id);
create unique index ux_projects_customer_name on projects (customer_id, name);
