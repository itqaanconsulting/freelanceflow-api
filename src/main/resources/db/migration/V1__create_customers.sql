create table customers (
    id uuid primary key,
    company_name varchar(160) not null,
    contact_name varchar(120),
    email varchar(160) not null,
    phone varchar(40),
    vat_number varchar(40),
    street varchar(160),
    city varchar(120),
    country varchar(80),
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null
);

create unique index ux_customers_email on customers (email);
