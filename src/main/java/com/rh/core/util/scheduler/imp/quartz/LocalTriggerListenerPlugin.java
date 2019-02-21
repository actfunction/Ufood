/*
 * Copyright (c) 2011 Ruaho All rights reserved.
 */

package com.rh.core.util.scheduler.imp.quartz;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.Trigger.CompletedExecutionInstruction;
import org.quartz.TriggerListener;
import org.quartz.impl.matchers.EverythingMatcher;
import org.quartz.spi.SchedulerPlugin;

/**
 * local job's trigger history for quartz
 * @author wanglong
 */

public class LocalTriggerListenerPlugin implements SchedulerPlugin, TriggerListener {

	/*
	 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	 * 
	 * Data members.
	 * 
	 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	 */

	/** plugin name */
	private String name;

	/** log */
	@SuppressWarnings("unused")
    private static Log log = LogFactory.getLog(LocalTriggerListenerPlugin.class);

	/**
	 * <p>
	 * Called during creation of the <code>Scheduler</code> in order to give the <code>SchedulerPlugin</code>
	 * a chance to initialize.
	 * </p>
	 * @param pname plugin name
	 * @param scheduler schedule instance
	 * @throws SchedulerException if there is an error initializing.
	 */
	public void initialize(String pname, Scheduler scheduler) throws SchedulerException {
		this.name = pname;

		scheduler.getListenerManager().addTriggerListener(this, EverythingMatcher.allTriggers());
	}

	/**
	 * when this plugin start call this method
	 */
	public void start() {
		// do nothing...
	}

	/**
	 * <p>
	 * Called in order to inform the <code>SchedulerPlugin</code> that it should free up all of it's resources
	 * because the scheduler is shutting down.
	 * </p>
	 */
	public void shutdown() {
		// nothing to do...
	}

	/*
	 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	 * 
	 * TriggerListener Interface.
	 * 
	 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	 */

	/**
	 * get this plugin name
	 * @return plugin name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @see org.quartz.TriggerListener#triggerFired(org.quartz.Trigger, org.quartz.JobExecutionContext)
	 * @param trigger the trigger
	 * @param context job execution context
	 */
	public void triggerFired(Trigger trigger, JobExecutionContext context) {

//		log.debug("triggerFired , update trigger state...");
		update(trigger, "RUNNING");
		// getLog().info(MessageFormat.format(getTriggerFiredMessage(), args));
	}

	/**
	 * @see org.quartz.TriggerListener#triggerMisfired(org.quartz.Trigger)
	 * @param trigger Trigger
	 */
	public void triggerMisfired(Trigger trigger) {

		// getLog().info(MessageFormat.format(getTriggerMisfiredMessage(), args));
	}

	/**
	 * @see org.quartz.TriggerListener#triggerComplete(org.quartz.Trigger, org.quartz.JobExecutionContext,
	 *      org.quartz.Trigger.CompletedExecutionInstruction)
	 * @param trigger Trigger
	 * @param context job execution context
	 * @param triggerInstructionCode trigger status
	 */
	public void triggerComplete(Trigger trigger, JobExecutionContext context,
			CompletedExecutionInstruction triggerInstructionCode) {
		//log.debug("triggerComplete , update trigger state...");
		if (triggerInstructionCode == CompletedExecutionInstruction.DELETE_TRIGGER) {
			// "DELETE TRIGGER";
			delete(trigger);
		} else if (triggerInstructionCode == CompletedExecutionInstruction.RE_EXECUTE_JOB) {
			// "RE-EXECUTE JOB";
			update(trigger, "RUNNING");
		} else if (triggerInstructionCode == CompletedExecutionInstruction.SET_ALL_JOB_TRIGGERS_COMPLETE) {
			// "SET ALL OF JOB'S TRIGGERS COMPLETE";
			delete(trigger);
		} else if (triggerInstructionCode == CompletedExecutionInstruction.SET_TRIGGER_COMPLETE) {
			// "SET THIS TRIGGER COMPLETE";
			delete(trigger);
		}

		// getLog().info(MessageFormat.format(getTriggerCompleteMessage(), args));
	}

	/**
	 * @see org.quartz.TriggerListener#vetoJobExecution(org.quartz.Trigger, org.quartz.JobExecutionContext)
	 * @param trigger Trigger
	 * @param context job execution context
	 * @return false
	 */
	public boolean vetoJobExecution(Trigger trigger, JobExecutionContext context) {
		return false;
	}

	/**
	 * update the trigger state
	 * @param trigger the trigger
	 * @param state the state string
	 */
	private void update(Trigger trigger, String state) {
		// Object[] args = {
		// trigger.getKey().getName(), trigger.getKey().getGroup(),
		// trigger.getPreviousFireTime(), trigger.getNextFireTime(),
		// new java.util.Date(), context.getJobDetail().getKey().getName(),
		// context.getJobDetail().getKey().getGroup(),
		// Integer.valueOf(context.getRefireCount())
		// };
//		Bean param = new Bean().set("TRIGGER_STATE", state);
//		param.setId(trigger.getKey().getName());
//		if (null != trigger.getStartTime()) {
//			param.set("START_TIME", new Timestamp(trigger.getStartTime().getTime()));
//		}
//		if (null != trigger.getEndTime()) {
//			param.set("END_TIME", new Timestamp(trigger.getEndTime().getTime()));
//		}
//		if (trigger instanceof SimpleTrigger) {
//			SimpleTrigger sTrig = (SimpleTrigger) trigger;
//			param.set("SIMPLE_TRIGGER_REPEAT_COUNT", sTrig.getRepeatCount());
//		}
//		//
	}

	/**
	 * delete the trigger
	 * @param trigger the trigger
	 */
	private void delete(Trigger trigger) {
	    
	}

	/**
	 * update all of job's triggers state
	 * @param trigger <CODE>Trigger</CODE>
	 * @param state the state String
	 */
	// private void updates(Trigger trigger, String state) {
	// Bean param = new Bean().set("TRIGGER_STATE", state);
	// Bean whereBean = new Bean();
	// whereBean.set("JOB_NAME", trigger.getJobKey().getName());
	// ServDao.updates(CURRENT_SERV, param, whereBean);
	// }

}
