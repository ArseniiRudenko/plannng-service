create sequence issue_status_id_seq
    as integer;

alter sequence issue_status_id_seq owner to postgres;

create type status as enum ('backlog', 'planned', 'in progress', 'in testing', 'closed');

alter type status owner to postgres;

create type priority as enum ('low', 'medium', 'high', 'showstopper');

alter type priority owner to postgres;

create table peoples
(
    id                serial
        constraint peoples_pk
            primary key,
    first_name        varchar,
    last_name         varchar,
    email             varchar,
    password          varchar,
    phone             varchar,
    is_enabled        boolean                  default false not null,
    is_admin          boolean                  default false not null,
    created_at        timestamp with time zone default now() not null,
    updated_at        timestamp with time zone default now() not null,
    is_email_verified boolean                  default false not null
);

alter table peoples
    owner to postgres;

create unique index peoples_id_uindex
    on peoples (id);

create unique index peoples_email_uindex
    on peoples (email);

create unique index peoples_first_name_last_name_email_uindex
    on peoples (first_name, last_name, email);

create table commits
(
    hash    integer                  not null
        constraint commits_pk
            primary key,
    author  varchar                  not null,
    message text                     not null,
    date    timestamp with time zone not null
);

alter table commits
    owner to postgres;

create unique index commits_hash_uindex
    on commits (hash);

create table tags
(
    id          serial
        constraint tags_pk
            primary key,
    name        varchar                                not null,
    description varchar,
    created_at  timestamp with time zone default now() not null
);

alter table tags
    owner to postgres;

create unique index tags_id_uindex
    on tags (id);

create unique index tags_name_uindex
    on tags (name);

create table project
(
    id          serial
        constraint project_pk
            primary key,
    name        varchar not null,
    description varchar,
    owner       integer not null
        constraint project_peoples_id_fk
            references peoples
            on update cascade on delete restrict
);

alter table project
    owner to postgres;

create table issues
(
    id             serial
        constraint issues_pk
            primary key,
    header         varchar                                                          not null,
    description    text,
    priority       project_track.priority   default 'low'::project_track.priority   not null,
    parent         integer
        constraint issues_issues_id_fk
            references issues
            on update cascade on delete set null,
    created_at     timestamp with time zone default now()                           not null,
    updated_at     timestamp with time zone default now()                           not null,
    estimated_time interval,
    assignee       integer
        constraint issues_peoples_id_fk
            references peoples
            on update cascade on delete set null,
    is_milestone   boolean                  default false                           not null,
    deadline       timestamp with time zone,
    cur_status     project_track.status     default 'backlog'::project_track.status not null,
    created_by     integer                                                          not null
        constraint issues_peoples_id_fk_2
            references peoples,
    updated_by     integer
        constraint issues_peoples_id_fk_3
            references peoples,
    project        integer                                                          not null
        constraint issues_project_id_fk
            references project
);

alter table issues
    owner to postgres;

create unique index issues_id_uindex
    on issues (id);

create unique index issues_name_uindex
    on issues (header);

create table spent_time
(
    id         serial
        constraint spent_time_pk
            primary key,
    issue      integer                                not null
        constraint spent_time_issues_id_fk
            references issues
            on update cascade on delete cascade,
    person     integer                                not null
        constraint spent_time_peoples_id_fk
            references peoples
            on update cascade on delete cascade,
    time       interval                               not null,
    created_at timestamp with time zone default now() not null,
    comment    text,
    on_date    date                     default now(),
    updated_at timestamp with time zone default now() not null
);

alter table spent_time
    owner to postgres;

create unique index spent_time_id_uindex
    on spent_time (id);

create table issue_comments
(
    id         serial
        constraint issue_comments_pk
            primary key,
    created_at timestamp with time zone default now() not null,
    updated_at timestamp with time zone default now() not null,
    author     integer                                not null
        constraint issue_comments_peoples_id_fk
            references peoples
            on update cascade on delete set null,
    content    text                                   not null,
    issue      integer                                not null
        constraint issue_comments_issues_id_fk
            references issues
            on update cascade on delete cascade
);

alter table issue_comments
    owner to postgres;

create unique index issue_comments_id_uindex
    on issue_comments (id);

create table issue_status_log
(
    id         integer                  default nextval('project_track.issue_status_id_seq'::regclass) not null
        constraint issue_status_pk
            primary key,
    status     project_track.status                                                                    not null,
    created_at timestamp with time zone default now()                                                  not null,
    issue      integer                                                                                 not null
        constraint issue_status_issues_id_fk
            references issues
            on update cascade on delete cascade,
    created_by integer                                                                                 not null
        constraint issue_status_log_peoples_id_fk
            references peoples
);

alter table issue_status_log
    owner to postgres;

alter sequence issue_status_id_seq owned by issue_status_log.id;

create unique index issue_status_id_uindex
    on issue_status_log (id);

create table tag_to_issue
(
    tag_id     integer                                not null
        constraint tag_to_issue_tags_id_fk
            references tags
            on update cascade on delete cascade,
    issue_id   integer                                not null
        constraint tag_to_issue_issues_id_fk
            references issues
            on update cascade on delete cascade,
    created_at timestamp with time zone default now() not null
);

alter table tag_to_issue
    owner to postgres;

create unique index tag_to_issue_tag_id_issue_id_uindex
    on tag_to_issue (tag_id, issue_id);

create unique index project_id_uindex
    on project (id);

create table project_membership
(
    person             integer               not null
        constraint project_membership_peoples_id_fk
            references peoples,
    project            integer               not null
        constraint project_membership_project_id_fk
            references project,
    can_manage_members boolean default false not null,
    can_manage_tasks   boolean default false not null
);

alter table project_membership
    owner to postgres;

create unique index project_membership_person_project_uindex
    on project_membership (person, project);

create view chlid_issue_count_per_status(parent, cur_status, count) as
SELECT issues.parent,
       issues.cur_status,
       count(*)::integer AS count
FROM project_track.issues
GROUP BY issues.parent, issues.cur_status;

alter table chlid_issue_count_per_status
    owner to postgres;

create view issue_progress(id, open_issues, closed_issues, completition_percent) as
SELECT COALESCE(cl.parent, op.parent)                                                                            AS id,
       COALESCE(op.open_issues, 0)                                                                               AS open_issues,
       COALESCE(cl.closed_issues, 0)                                                                             AS closed_issues,
       round(COALESCE(cl.closed_issues::numeric, 0.0) /
             (COALESCE(op.open_issues::numeric, 0.0) + COALESCE(cl.closed_issues::numeric, 0.0)) *
             100::numeric)                                                                                       AS completition_percent
FROM (SELECT chlid_issue_count_per_status.parent,
             chlid_issue_count_per_status.count AS closed_issues
      FROM project_track.chlid_issue_count_per_status
      WHERE chlid_issue_count_per_status.cur_status = 'closed'::project_track.status) cl
         FULL JOIN (SELECT chlid_issue_count_per_status.parent,
                           sum(chlid_issue_count_per_status.count)::integer AS open_issues
                    FROM project_track.chlid_issue_count_per_status
                    WHERE chlid_issue_count_per_status.cur_status <> 'closed'::project_track.status
                    GROUP BY chlid_issue_count_per_status.parent) op ON cl.parent = op.parent;

alter table issue_progress
    owner to postgres;

create view spent_time_per_issue(issue, time) as
SELECT spent_time.issue,
       sum(spent_time."time") AS "time"
FROM project_track.spent_time
GROUP BY spent_time.issue;

alter table spent_time_per_issue
    owner to postgres;

create view person_time(id, name, assigned_time, spent_time_this_month, year, month) as
SELECT src.id,
       src.name,
       src.assigned_time,
       src.spent_time_this_month,
       src.year,
       src.month
FROM (SELECT peoples.id,
             peoples.name,
             spent1.person,
             spent1.spent_time_this_month,
             spent1.year,
             spent1.month,
             assigned.assignee,
             assigned.assigned_time
      FROM project_track.peoples
               LEFT JOIN (SELECT spent_time.person,
                                 sum(spent_time."time")                    AS spent_time_this_month,
                                 EXTRACT(year FROM spent_time.created_at)  AS year,
                                 EXTRACT(month FROM spent_time.created_at) AS month
                          FROM project_track.spent_time
                          GROUP BY spent_time.person, (EXTRACT(year FROM spent_time.created_at)),
                                   (EXTRACT(month FROM spent_time.created_at))) spent1 ON spent1.person = peoples.id
               LEFT JOIN (SELECT issues.assignee,
                                 sum(issues.estimated_time) AS assigned_time
                          FROM project_track.issues
                          WHERE issues.cur_status <> 'closed'::project_track.status
                          GROUP BY issues.assignee) assigned ON assigned.assignee = peoples.id) src;

alter table person_time
    owner to postgres;

create view estimation_debrief(id, assignee, estimated_time, spent_time, estimation_miscalc) as
SELECT istpi.id,
       istpi.assignee,
       istpi.estimated_time,
       istpi."time"                        AS spent_time,
       istpi.estimated_time - istpi."time" AS estimation_miscalc
FROM (SELECT issues.id,
             issues.estimated_time,
             issues.assignee,
             spent_time_per_issue."time"
      FROM project_track.issues
               LEFT JOIN project_track.spent_time_per_issue ON issues.id = spent_time_per_issue.issue
      WHERE issues.cur_status = 'closed'::project_track.status
        AND issues.estimated_time IS NOT NULL) istpi;

alter table estimation_debrief
    owner to postgres;

create view issue_daily_projection
            (sample_date, status, issue_origin_ts, status_origin_ts, issue, header, priority, parent, estimated_time,
             assignee, is_milestone, deadline)
as
SELECT rdi.sample_date,
       rdi.status,
       issues.created_at AS issue_origin_ts,
       rdi.created_at    AS status_origin_ts,
       rdi.issue,
       issues.header,
       issues.priority,
       issues.parent,
       issues.estimated_time,
       issues.assignee,
       issues.is_milestone,
       issues.deadline
FROM (SELECT generate_series.generate_series::date                                                            AS sample_date,
             st.id,
             st.status,
             st.created_at,
             st.issue,
             rank() OVER (PARTITION BY st.issue, generate_series.generate_series ORDER BY st.created_at DESC) AS r
      FROM generate_series(now() - '365 days'::interval, now(), '1 day'::interval) generate_series(generate_series)
               LEFT JOIN project_track.issue_status_log st
                         ON generate_series.generate_series >= st.created_at::date) rdi
         JOIN project_track.issues ON rdi.issue = issues.id
WHERE rdi.r = 1;

alter table issue_daily_projection
    owner to postgres;

create view child_issue_status_history(parent, quantity, sample_date, status) as
SELECT issue_daily_projection.parent,
       count(*) AS quantity,
       issue_daily_projection.sample_date,
       issue_daily_projection.status
FROM project_track.issue_daily_projection
GROUP BY issue_daily_projection.parent, issue_daily_projection.sample_date, issue_daily_projection.status;

alter table child_issue_status_history
    owner to postgres;

create view last_month_burndown(parent, quantity, sample_date) as
SELECT child_issue_status_history.parent,
       sum(child_issue_status_history.quantity) AS quantity,
       child_issue_status_history.sample_date
FROM project_track.child_issue_status_history
WHERE child_issue_status_history.status <> 'closed'::project_track.status
  AND child_issue_status_history.sample_date >= (now() - '30 days'::interval)::date
  AND child_issue_status_history.sample_date <= now()::date
GROUP BY child_issue_status_history.parent, child_issue_status_history.sample_date;

alter table last_month_burndown
    owner to postgres;

create function trigger_set_timestamp() returns trigger
    language plpgsql
as
$$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$;

alter function trigger_set_timestamp() owner to postgres;

create trigger set_timestamp_on_update
    before update
    on issues
    for each row
execute procedure trigger_set_timestamp();

create trigger set_timestamp
    before update
    on spent_time
    for each row
execute procedure trigger_set_timestamp();

create trigger set_timestamp
    before update
    on issue_comments
    for each row
execute procedure trigger_set_timestamp();

create function function_set_status_on_insert() returns trigger
    language plpgsql
as
$$
BEGIN
    INSERT INTO
        project_track.issue_status_log(status,issue,created_by)
    VALUES(new.cur_status,new.id,new.created_by);
    RETURN new;
END;
$$;

alter function function_set_status_on_insert() owner to postgres;

create trigger log_status_on_insert
    after insert
    on issues
    for each row
execute procedure function_set_status_on_insert();

create function function_set_issue_cur_status() returns trigger
    language plpgsql
as
$$
BEGIN
    Update project_track.issues set cur_status=new.status,updated_by=new.created_by where id=new.issue;
    RETURN new;
END;
$$;

alter function function_set_issue_cur_status() owner to postgres;

create trigger set_cur_status
    after insert
    on issue_status_log
    for each row
execute procedure function_set_issue_cur_status();

create function reject_if_same_status() returns trigger
    language plpgsql
as
$$
BEGIN
    if new.status =
       (SELECT DISTINCT ON (issue)
            status
        FROM project_track.issue_status_log
        where new.issue=issue
        ORDER BY issue, created_at DESC) then
        return null;
    else
        return new;
    end if;
END;
$$;

alter function reject_if_same_status() owner to postgres;

create trigger filter_same_status
    before insert
    on issue_status_log
    for each row
execute procedure reject_if_same_status();

create function function_add_status_log_on_update() returns trigger
    language plpgsql
as
$$
BEGIN
    INSERT INTO
        project_track.issue_status_log(status,issue,created_by)
    VALUES(new.cur_status,new.id,new.updated_by);
    return new;
END;
$$;

alter function function_add_status_log_on_update() owner to postgres;

create trigger add_status_log_on_update
    after update
        of cur_status
    on issues
    for each row
    when (old.cur_status IS DISTINCT FROM new.cur_status)
execute procedure function_add_status_log_on_update();

create function function_add_member_on_create() returns trigger
    language plpgsql
as
$$
BEGIN
    INSERT INTO
        project_track.project_membership(person, project)
    VALUES(new.organizer,new.id);
    return new;
END;
$$;

alter function function_add_member_on_create() owner to postgres;

create trigger add_membership
    after insert
    on project
    for each row
execute procedure function_add_member_on_create();

