package com.ctrip.xpipe.redis.console.migration.status.migration;

import com.ctrip.xpipe.redis.console.migration.model.MigrationCluster;
import com.ctrip.xpipe.redis.console.migration.status.MigrationStatus;

/**
 * @author shyin
 *
 * Dec 8, 2016
 */
public class MigrationSuccessState extends AbstractMigrationState {
	
	public MigrationSuccessState(MigrationCluster holder) {
		super(holder, MigrationStatus.Success);
		this.setNextAfterSuccess(this)
			.setNextAfterFail(this);
	}

	@Override
	protected void doRollback() {
		throw new  UnsupportedOperationException("already success, can not tryRollback");
	}

	@Override
	public void doAction() {
		try{
			getHolder().stop();
		} catch (Exception e) {
			logger.info("[doAction][{}] stop fail", getHolder().clusterName());
		} finally {
			markDone();
		}
	}

	@Override
	public void refresh() {
		// Nothing to do
		logger.debug("[MigrationSuccess]{}", getHolder().clusterName());
	}

}
