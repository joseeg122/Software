-- FinTrack 360 — esquema. PROTOTIPO EDUCATIVO: todos los datos son simulados.

create table users (
    id            bigserial primary key,
    username      varchar(60)  not null unique,
    password_hash varchar(100) not null,
    full_name     varchar(120) not null,
    role          varchar(30)  not null,
    created_at    timestamp    not null
);

create table persons (
    id              bigserial primary key,
    full_name       varchar(120) not null,
    document        varchar(20)  not null unique,
    document_type   varchar(20)  not null,
    city            varchar(60)  not null,
    birth_date      date         not null,
    occupation      varchar(120) not null,
    overall_status  varchar(60)  not null,
    last_checked_at timestamp,
    run_number      integer      not null default 0
);

create table banks (
    id   bigserial primary key,
    slug varchar(40)  not null unique,
    name varchar(80)  not null
);

create table accounts (
    id        bigserial primary key,
    person_id bigint        not null references persons (id),
    bank_id   bigint        not null references banks (id),
    number    varchar(20)   not null unique,
    type      varchar(20)   not null,
    status    varchar(20)   not null,
    balance   numeric(18,2) not null,
    opened_at date          not null
);

create table transactions (
    id             bigserial primary key,
    account_id     bigint        not null references accounts (id),
    person_id      bigint        not null references persons (id),
    tx_date        timestamp     not null,
    description    varchar(160)  not null,
    type           varchar(10)   not null check (type in ('CREDITO', 'DEBITO')),
    channel        varchar(40)   not null,
    amount         numeric(18,2) not null check (amount > 0),
    balance_before numeric(18,2) not null,
    balance_after  numeric(18,2) not null,
    -- saldo posterior = saldo anterior ± valor
    constraint chk_tx_balance check (
        balance_after = balance_before + case when type = 'CREDITO' then amount else -amount end)
);

create table credits (
    id             bigserial primary key,
    person_id      bigint        not null references persons (id),
    bank_id        bigint        not null references banks (id),
    number         varchar(20)   not null unique,
    product        varchar(80)   not null,
    type           varchar(30)   not null,
    initial_amount numeric(18,2) not null,
    balance        numeric(18,2) not null,
    installment    numeric(18,2) not null,
    rate           numeric(5,2)  not null,
    term_months    integer       not null,
    status         varchar(30)   not null,
    days_past_due  integer       not null default 0,
    restructured   boolean       not null default false,
    opened_at      date          not null,
    closed_at      date
);

create table credit_payments (
    id             bigserial primary key,
    credit_id      bigint        not null references credits (id),
    person_id      bigint        not null references persons (id),
    installment_no integer       not null,
    due_date       date          not null,
    paid_date      date,
    amount         numeric(18,2) not null,
    status         varchar(20)   not null,
    days_late      integer       not null default 0
);

create table credit_history (
    id          bigserial primary key,
    credit_id   bigint       not null references credits (id),
    person_id   bigint       not null references persons (id),
    event_date  date         not null,
    event_type  varchar(30)  not null,
    description varchar(255) not null
);

create table credit_scores (
    id               bigserial primary key,
    person_id        bigint    not null references persons (id),
    score            integer   not null,
    max_score        integer   not null,
    calculated_at    timestamp not null,
    payment_history  integer   not null,
    debt_level       integer   not null,
    card_utilization integer   not null,
    credit_age       integer   not null,
    active_credits   integer   not null,
    delinquency      integer   not null
);

create table sources (
    id           bigserial primary key,
    code         varchar(60)  not null unique,
    name         varchar(160) not null,
    category     varchar(30)  not null,
    empty_status varchar(20)  not null,
    city         varchar(60),
    bank_id      bigint references banks (id)
);

create table due_diligence_checks (
    id            bigserial primary key,
    person_id     bigint      not null references persons (id),
    run_number    integer     not null,
    started_at    timestamp   not null,
    finished_at   timestamp,
    status        varchar(40) not null,
    total_sources integer     not null default 0,
    with_findings integer     not null default 0,
    unavailable   integer     not null default 0,
    triggered_by  varchar(60) not null
);

create table source_results (
    id         bigserial primary key,
    check_id   bigint       not null references due_diligence_checks (id),
    person_id  bigint       not null references persons (id),
    source_id  bigint       not null references sources (id),
    status     varchar(20)  not null,
    match_type varchar(20)  not null,
    matches    integer      not null default 0,
    summary    varchar(255) not null,
    checked_at timestamp    not null,
    unique (person_id, source_id)
);

create table sanctions (
    id           bigserial primary key,
    person_id    bigint       not null references persons (id),
    source_id    bigint       not null references sources (id),
    list_type    varchar(20)  not null,
    matched_name varchar(120) not null,
    match_type   varchar(20)  not null,
    program      varchar(120) not null,
    listed_at    date         not null,
    detail       varchar(255) not null
);

create table pep_records (
    id           bigserial primary key,
    person_id    bigint       not null references persons (id),
    source_id    bigint       not null references sources (id),
    matched_name varchar(120) not null,
    position     varchar(120) not null,
    entity       varchar(120) not null,
    match_type   varchar(20)  not null,
    from_date    date         not null,
    to_date      date,
    detail       varchar(255) not null
);

create table background_checks (
    id           bigserial primary key,
    person_id    bigint       not null references persons (id),
    source_id    bigint       not null references sources (id),
    record_type  varchar(60)  not null,
    reference    varchar(40)  not null,
    matched_name varchar(120) not null,
    match_type   varchar(20)  not null,
    record_date  date         not null,
    status       varchar(30)  not null,
    detail       varchar(255) not null
);

create table judicial_processes (
    id               bigserial primary key,
    person_id        bigint       not null references persons (id),
    source_id        bigint       not null references sources (id),
    process_number   varchar(40)  not null unique,
    process_type     varchar(60)  not null,
    court            varchar(160) not null,
    city             varchar(60)  not null,
    plaintiff        varchar(120) not null,
    defendant        varchar(120) not null,
    status           varchar(20)  not null,
    last_action      varchar(200) not null,
    last_action_date date         not null,
    filed_at         date         not null,
    match_type       varchar(20)  not null,
    legal_collection boolean      not null default false
);

create table lawsuits (
    id             bigserial primary key,
    person_id      bigint        not null references persons (id),
    process_id     bigint        not null references judicial_processes (id),
    lawsuit_number varchar(40)   not null unique,
    claim_type     varchar(80)   not null,
    plaintiff      varchar(120)  not null,
    defendant      varchar(120)  not null,
    amount         numeric(18,2) not null,
    status         varchar(20)   not null,
    filed_at       date          not null
);

create table legal_measures (
    id           bigserial primary key,
    person_id    bigint        not null references persons (id),
    process_id   bigint        not null references judicial_processes (id),
    measure_type varchar(30)   not null,
    asset        varchar(160)  not null,
    amount       numeric(18,2) not null,
    status       varchar(20)   not null,
    ordered_at   date          not null
);

create table traffic_records (
    id          bigserial primary key,
    person_id   bigint        not null references persons (id),
    source_id   bigint        not null references sources (id),
    record_type varchar(30)   not null,
    reference   varchar(40)   not null,
    city        varchar(60)   not null,
    record_date date          not null,
    amount      numeric(18,2) not null,
    status      varchar(20)   not null,
    match_type  varchar(20)   not null
);

create table reputational_news (
    id           bigserial primary key,
    person_id    bigint       not null references persons (id),
    source_id    bigint       not null references sources (id),
    title        varchar(200) not null,
    published_at date         not null,
    outlet       varchar(120) not null,
    category     varchar(40)  not null,
    level        varchar(20)  not null,
    status       varchar(30)  not null,
    match_type   varchar(20)  not null
);

create table alerts (
    id          bigserial primary key,
    person_id   bigint       not null references persons (id),
    severity    varchar(20)  not null,
    title       varchar(160) not null,
    description varchar(255) not null,
    category    varchar(40)  not null,
    created_at  timestamp    not null,
    status      varchar(20)  not null
);

create table comments (
    id         bigserial primary key,
    person_id  bigint       not null references persons (id),
    author     varchar(60)  not null,
    body       varchar(500) not null,
    created_at timestamp    not null,
    updated_at timestamp    not null
);

create table reports (
    id           bigserial primary key,
    code         varchar(60) not null unique,
    person_id    bigint      not null references persons (id),
    generated_at timestamp   not null,
    generated_by varchar(60) not null,
    check_status varchar(60) not null,
    snapshot     text        not null
);

create table timeline_events (
    id          bigserial primary key,
    person_id   bigint       not null references persons (id),
    event_type  varchar(40)  not null,
    description varchar(255) not null,
    occurred_at timestamp    not null
);

create table audit_log (
    id         bigserial primary key,
    username   varchar(60)  not null,
    action     varchar(60)  not null,
    detail     varchar(255) not null,
    created_at timestamp    not null
);

create index idx_accounts_person on accounts (person_id);
create index idx_transactions_person on transactions (person_id, tx_date desc);
create index idx_credits_person on credits (person_id);
create index idx_payments_person on credit_payments (person_id);
create index idx_results_person on source_results (person_id);
create index idx_processes_person on judicial_processes (person_id);
create index idx_alerts_person on alerts (person_id);
