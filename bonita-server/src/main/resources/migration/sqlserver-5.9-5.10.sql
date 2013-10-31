delete from BN_JOB_LOCK
go
alter table BN_JOB_LOCK add LOCKED_BY_ varchar(55)
go
alter table BN_JOB_LOCK add PROC_UUID_ varchar(255) unique
go
create index IDX_JL_ on BN_JOB_LOCK (LOCKED_BY_, PROC_UUID_)
go
create index IDX_JL_PID_ on BN_JOB_LOCK (PROC_UUID_)
go
drop index IDX_JL_PRI_UUID_ on BN_JOB_LOCK
go

-- Drop the unique constraint before deleting the column
DECLARE @bn_job_lock_uq_name nvarchar(255), @job_lock_alter_table_sql VARCHAR(4000)
SET @job_lock_alter_table_sql = 'ALTER TABLE BN_JOB_LOCK DROP CONSTRAINT |ConstraintName| '

SELECT @bn_job_lock_uq_name = name FROM sys.key_constraints
WHERE parent_object_id = OBJECT_ID('BN_JOB_LOCK')
AND type = 'UQ'

IF not @bn_job_lock_uq_name IS NULL
BEGIN
	SET @job_lock_alter_table_sql = REPLACE(@job_lock_alter_table_sql, '|ConstraintName|', @bn_job_lock_uq_name)
	EXEC (@job_lock_alter_table_sql)
END

alter table BN_JOB_LOCK drop column PRI_UUID_
go

drop index IDX_JB_LK_ on BN_JOB
go
create index IDX_JB_LK_ on BN_JOB (RETRIES_)
go
alter table BN_JOB drop column LOCK_
go
exec sp_rename 'BN_JOB.ROOT_INST_UUID_', 'PROC_UUID_', 'COLUMN'
go

alter table BN_OEI_ add INCOMING_ID_ numeric(19,0)
go
drop index IDX_LOCK_ on BN_OEI_
go
create index IDX_LOCK_ on BN_OEI_ (OVERDUE, LOCKED)
go

create table BN_MASTER (DBID_ numeric(19,0) identity not null, HEARTBEAT_ numeric(19,0) null, NODE varchar(50) null, primary key (DBID_))
go
create table BN_NODE (DBID_ numeric(19,0) identity not null, HEARTBEAT_ numeric(19,0) null, NODE varchar(50) null, STATUS varchar(20) null, OPTION_ varchar(50) null, primary key (DBID_))
go