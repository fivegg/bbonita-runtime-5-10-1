delete from BN_JOB_LOCK;
alter table BN_JOB_LOCK add LOCKED_BY_ varchar2(55 char);
alter table BN_JOB_LOCK add PROC_UUID_ varchar2(255 char);
alter table BN_JOB_LOCK add constraint IDX_JL_PID_ UNIQUE (PROC_UUID_);
create index IDX_JL_ on BN_JOB_LOCK (LOCKED_BY_, PROC_UUID_);
alter table BN_JOB_LOCK drop column PRI_UUID_;

drop index IDX_JB_LK_;
create index IDX_JB_LK_ on BN_JOB (RETRIES_);
alter table BN_JOB drop column LOCK_;
alter table BN_JOB rename column ROOT_INST_UUID_ to PROC_UUID_;
create index IDX_OE_ID on BN_JOB (OE_ID_);

alter table BN_OEI_ add INCOMING_ID_ number(19,0);
drop index IDX_LOCK_;
create index IDX_LOCK_ on BN_OEI_ (OVERDUE, LOCKED);

create table BN_MASTER (DBID_ number(19,0) not null, HEARTBEAT_ number(19,0), NODE varchar2(50 char), primary key (DBID_));
create table BN_NODE (DBID_ number(19,0) not null, HEARTBEAT_ number(19,0), NODE varchar2(50 char), STATUS varchar2(20 char), OPTION_ varchar2(50 char), primary key (DBID_));