delete from BN_JOB_LOCK;
alter table BN_JOB_LOCK add LOCKED_BY_ varchar(55);
alter table BN_JOB_LOCK add PROC_UUID_ varchar(255) unique;
create index IDX_JL_ on BN_JOB_LOCK (LOCKED_BY_, PROC_UUID_);
create index IDX_JL_PID_ on BN_JOB_LOCK (PROC_UUID_);
alter table BN_JOB_LOCK drop column PRI_UUID_;

drop index IDX_JB_LK_;
create index IDX_JB_LK_ on BN_JOB (RETRIES_);
alter table BN_JOB drop column LOCK_;
alter table BN_JOB rename ROOT_INST_UUID_ to PROC_UUID_;

alter table BN_OEI_ add INCOMING_ID_ int;
drop index IDX_LOCK_;
create index IDX_LOCK_ on BN_OEI_ (OVERDUE, LOCKED);

create table BN_MASTER (DBID_ int8 not null, HEARTBEAT_ int8, NODE varchar(50), primary key (DBID_));
create table BN_NODE (DBID_ int8 not null, HEARTBEAT_ int8, NODE varchar(50), STATUS varchar(20), OPTION_ varchar(50), primary key (DBID_));